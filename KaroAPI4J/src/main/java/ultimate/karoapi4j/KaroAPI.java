package ultimate.karoapi4j;

import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import ultimate.karoapi4j.enums.EnumContentType;
import ultimate.karoapi4j.enums.EnumUserGamesort;
import ultimate.karoapi4j.model.base.Identifiable;
import ultimate.karoapi4j.model.official.ChatMessage;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.model.official.UserMessage;
import ultimate.karoapi4j.utils.CollectionsUtil;
import ultimate.karoapi4j.utils.JSONUtil;
import ultimate.karoapi4j.utils.JSONUtil.IDLookUp;
import ultimate.karoapi4j.utils.ReflectionsUtil;
import ultimate.karoapi4j.utils.URLLoader;
import ultimate.karoapi4j.utils.URLLoader.BackgroundLoader;

/**
 * This is the wrapper for accessing the Karo API.<br>
 * <br>
 * Note: Accessing the API requires a user and password for www.karopapier.de which can be passed with the constructor. Afterwards it is
 * recommended to check the successful login using {@link KaroAPI#check()}.<br>
 * <br>
 * Each API call will return a {@link CompletableFuture} which wraps the underlying API call and which then can be used to either load the results
 * either blocking or asynchronously (see {@link URLLoader}).<br>
 * <br>
 * For calls with filter arguments, each argument is applied only if it is set to a non null value. If the argument is null, it will be ignored.
 *
 * For example
 * <ul>
 * <li><code>getUsers(null, null, null)</code> = get all</li>
 * <li><code>getUsers("ab", null, null)</code> = get only those matching "ab"</li>
 * <li><code>getUsers(null, true, null)</code> = get only those who are invitable</li>
 * <li><code>getUsers(null, null, false)</code> = get only those who are not desperate</li>
 * <li><code>getUsers("ab", true, false)</code> = combination of all the 3</li>
 * </ul>
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public class KaroAPI implements IDLookUp
{
	/**
	 * Logger-Instance
	 */
	protected static transient final Logger										logger						= LoggerFactory.getLogger(KaroAPI.class);

	////////////////////////
	// config & constants //
	////////////////////////

	/**
	 * The default placeholder for API urls
	 */
	public static final String													PLACEHOLDER					= "$";
	/**
	 * The maximum number of allowed retries
	 */
	public static final int														MAX_RETRIES					= 10;

	////////////////////
	// parsers needed //
	////////////////////

	public static final Function<String, Void>									PARSER_VOID					= (result) -> { return null; };
	public static final Function<String, String>								PARSER_RAW					= Function.identity();
	public static final Function<String, List<java.util.Map<String, Object>>>	PARSER_GENERIC_LIST			= new JSONUtil.Parser<>(new TypeReference<List<java.util.Map<String, Object>>>() {});
	public static final Function<String, User>									PARSER_USER					= new JSONUtil.Parser<>(new TypeReference<User>() {});
	public static final Function<String, List<User>>							PARSER_USER_LIST			= new JSONUtil.Parser<>(new TypeReference<List<User>>() {});
	public static final Function<String, Game>									PARSER_GAME					= new JSONUtil.Parser<>(new TypeReference<Game>() {});
	public static final Function<String, Game>									PARSER_GAME_CONTAINER		= new JSONUtil.ContainerParser<>(new TypeReference<Game>() {}, "game");
	public static final Function<String, List<Game>>							PARSER_GAME_LIST			= new JSONUtil.Parser<>(new TypeReference<List<Game>>() {});
	public static final Function<String, Map>									PARSER_MAP					= new JSONUtil.Parser<>(new TypeReference<Map>() {});
	public static final Function<String, List<Map>>								PARSER_MAP_LIST				= new JSONUtil.Parser<>(new TypeReference<List<Map>>() {});
	public static final Function<String, ChatMessage>							PARSER_CHAT_MESSAGE			= new JSONUtil.Parser<>(new TypeReference<ChatMessage>() {});
	public static final Function<String, List<ChatMessage>>						PARSER_CHAT_LIST			= new JSONUtil.Parser<>(new TypeReference<List<ChatMessage>>() {});
	public static final Function<String, UserMessage>							PARSER_USER_MESSAGE			= new JSONUtil.Parser<>(new TypeReference<UserMessage>() {});
	public static final Function<String, List<UserMessage>>						PARSER_USER_MESSAGE_LIST	= new JSONUtil.Parser<>(new TypeReference<List<UserMessage>>() {});
	// this is a litte more complex: transform a list of [{id:1,text:"a"}, ...] to a map where the ids are the keys and the texts are the values
	public static final Function<String, java.util.Map<Integer, String>>		PARSER_NOTES				= (result) -> {
		return CollectionsUtil.toMap(PARSER_GENERIC_LIST.apply(result), "id", "text");
	};

	//////////////////
	// static logic //
	//////////////////

	/**
	 * The {@link Executor} used to run all BackgroundLoaders. This {@link Executor} is static since load balancing shall be possible across multiple
	 * instances of the {@link KaroAPI}.
	 */
	private static Executor														executor;

	/**
	 * Set a new {@link Executor}:<br>
	 * The {@link Executor} used to run all BackgroundLoaders. This {@link Executor} is static since load balancing shall be possible across multiple
	 * instances of the {@link KaroAPI}.
	 * 
	 * @param e - the new {@link Executor}
	 */
	public static void setExecutor(Executor e)
	{
		executor = e;
	}

	/**
	 * Get the current {@link Executor}:<br>
	 * The {@link Executor} used to run all BackgroundLoaders. This {@link Executor} is static since load balancing shall be possible across multiple
	 * instances of the {@link KaroAPI}.
	 * 
	 * @return the {@link Executor}
	 */
	public static Executor getExecutor()
	{
		return executor;
	}

	/**
	 * Asynchronously schedule and execute a {@link BackgroundLoader}.<br>
	 * This method will create {@link CompletableFuture} that is passed to the set {@link KaroAPI#executor} for asynchronous execution.<br>
	 * It is further capable of appending retries to the {@link CompletableFuture} optionally.
	 * 
	 * @param <T> - the type that the parser will return
	 * @param backgroundLoader - the {@link BackgroundLoader} to execute
	 * @param parser - the parser that shall be used to parse the loaded content
	 * @param retries - the number of <b>additional</b> retries to perform if the first execution fails.
	 * @return the {@link CompletableFuture}
	 */
	private static <T> CompletableFuture<T> loadAsync(BackgroundLoader backgroundLoader, Function<String, T> parser, int retries)
	{
		CompletableFuture<T> cf;
		// check whether an Executor is set. If not use the default.
		if(executor != null)
			cf = CompletableFuture.supplyAsync(backgroundLoader, executor).thenApply(parser);
		else
			cf = CompletableFuture.supplyAsync(backgroundLoader).thenApply(parser);

		if(retries > 0)
		{
			// check for MAX_RETRIES
			final int remainingTries = (retries > MAX_RETRIES ? MAX_RETRIES : retries);
			// add a completion stage for the retry handling
			// if the loading fails, this will call loadAsync again with a reduced retry-counter
			cf = cf.thenApply(CompletableFuture::completedFuture).exceptionally(new Function<Throwable, CompletableFuture<T>>() {
				public CompletableFuture<T> apply(Throwable t)
				{
					logger.warn("an error occurred - tries remaining: " + remainingTries, t);
					if(remainingTries > 0)
						return loadAsync(backgroundLoader, parser, remainingTries - 1);
					else
						return CompletableFuture.failedFuture(t);
				};
			}).thenCompose(Function.identity());
		}
		return cf;
	}

	//////////////
	// api URLs //
	//////////////

	// base
	protected final URLLoader	KAROPAPIER		= new URLLoader("https://www.karopapier.de");
	protected final URLLoader	API				= KAROPAPIER.relative("/api");
	// users
	protected final URLLoader	USERS			= API.relative("/users");
	protected final URLLoader	USER			= USERS.relative("/" + PLACEHOLDER);
	protected final URLLoader	USER_DRAN		= USER.relative("/dran");
	protected final URLLoader	USER_BLOCKERS	= USER.relative("/blockers");
	// current user
	protected final URLLoader	CURRENT_USER	= API.relative("/user");
	protected final URLLoader	CHECK			= CURRENT_USER.relative("/check");
	protected final URLLoader	FAVS			= CURRENT_USER.relative("/favs");
	protected final URLLoader	FAVS_EDIT		= FAVS.relative("/" + PLACEHOLDER);
	protected final URLLoader	BLOCKERS		= API.relative("/blockers");
	protected final URLLoader	NOTES			= API.relative("/notes");
	protected final URLLoader	NOTES_EDIT		= NOTES.relative("/" + PLACEHOLDER);
	protected final URLLoader	PLANNED_MOVES	= API.relative("/planned-moves");						// TODO
	// games
	protected final URLLoader	GAMES			= API.relative("/games");
	protected final URLLoader	GAMES3			= API.relative("/games3");
	protected final URLLoader	GAME			= GAMES.relative("/" + PLACEHOLDER);
	protected final URLLoader	GAME_CREATE		= API.relative("/game");
	protected final URLLoader	GAME_MOVE		= KAROPAPIER.relative("/move.php");
	protected final URLLoader	GAME_KICK		= KAROPAPIER.relative("/kickplayer.php");
	// maps
	protected final URLLoader	MAPS			= API.relative("/maps");
	protected final URLLoader	MAP				= MAPS.relative("/" + PLACEHOLDER);
	// mapimages
	// do not use API as the base here, since we do not need the authentication here
	protected final URLLoader	MAP_IMAGE		= KAROPAPIER.relative("/map/" + PLACEHOLDER + ".png");
	// chat
	protected final URLLoader	CHAT			= API.relative("/chat");
	protected final URLLoader	CHAT_LAST		= CHAT.relative("/last");
	protected final URLLoader	CHAT_USERS		= CHAT.relative("/users");
	// messaging
	protected final URLLoader	CONTACTS		= API.relative("/contacts");
	protected final URLLoader	MESSAGES		= API.relative("/messages/" + PLACEHOLDER);

	/**
	 * The number of retries to perform.<br>
	 * Default = 0
	 * 
	 * @see KaroAPI#loadAsync(BackgroundLoader, Function, int)
	 */
	private int					performRetries	= 0;

	/**
	 * Get an instance for the given user
	 * 
	 * @param user - the karopapier.de user
	 * @param password - the password
	 */
	public KaroAPI(String user, String password)
	{
		if(user == null || password == null)
			throw new IllegalArgumentException("user and password must not be null!");
		KAROPAPIER.addRequestProperty("X-Auth-Login", user);
		KAROPAPIER.addRequestProperty("X-Auth-Password", password);
	}

	/**
	 * 
	 * The number of retries to perform.<br>
	 * Default = 0
	 * 
	 * @see KaroAPI#loadAsync(BackgroundLoader, Function, int)
	 * @return the number of retries set
	 */
	public int getPerformRetries()
	{
		return performRetries;
	}

	/**
	 * Set the number of retries to perform.<br>
	 * Note: the method checks againt {@link KaroAPI#MAX_RETRIES}
	 * 
	 * @see KaroAPI#loadAsync(BackgroundLoader, Function, int)
	 * @param performRetries - the new value to set
	 */
	public void setPerformRetries(int performRetries)
	{
		if(performRetries < 0)
			throw new IllegalArgumentException("performRetries must be >= 0");
		if(performRetries > MAX_RETRIES)
			throw new IllegalArgumentException("performRetries must be <= " + MAX_RETRIES);
		this.performRetries = performRetries;
	}

	/**
	 * Non-static wrappeer for calling {@link KaroAPI#loadAsync(BackgroundLoader, Function, int)} that passes the currently set number of retries for
	 * this instance.
	 *
	 * @see KaroAPI#loadAsync(BackgroundLoader, Function, int)
	 * @see KaroAPI#getPerformRetries()
	 * @param <T> - the type that the parser will return
	 * @param backgroundLoader - the {@link BackgroundLoader} to execute
	 * @param parser - the parser that shall be used to parse the loaded content
	 * @return the {@link CompletableFuture}
	 */
	private <T> CompletableFuture<T> loadAsync(BackgroundLoader backgroundLoader, Function<String, T> parser)
	{
		return loadAsync(backgroundLoader, parser, performRetries);
	}

	///////////////////////
	// user
	///////////////////////

	/**
	 * Check to currently logged in user
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#CHECK
	 * @return the currently logged in user
	 */
	public CompletableFuture<User> check()
	{
		return loadAsync(CHECK.doGet((String) null), PARSER_USER);
	}

	/**
	 * Get all users
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#USERS
	 * @return the list of all users
	 */
	public CompletableFuture<List<User>> getUsers()
	{
		return getUsers(null, null, null);
	}

	/**
	 * Get the users filtered.<br>
	 * Each filter is applied only if it is set (not null). If the filter is null, it will be ignored (see class description).<br>
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#USERS
	 * @param login - the login filter
	 * @param invitable - the invitable filter
	 * @param desperate - the desperate filter
	 * @return the list of all users filtered by the given criteria
	 */
	public CompletableFuture<List<User>> getUsers(String login, Boolean invitable, Boolean desperate)
	{
		HashMap<String, Object> args = new HashMap<>();
		if(login != null)
			args.put("login", login);
		if(invitable != null)
			args.put("invitable", invitable ? "1" : "0");
		if(desperate != null)
			args.put("desperate", desperate ? "1" : "0");

		return loadAsync(USERS.doGet(args), PARSER_USER_LIST);
	}

	/**
	 * Get a user by id
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#USER
	 * @param userId - the user id
	 * @return the user
	 */
	public CompletableFuture<User> getUser(int userId)
	{
		return loadAsync(USER.replace(PLACEHOLDER, userId).doGet(), PARSER_USER);
	}

	/**
	 * Get the list of games where the given user is next.<br>
	 * 
	 * @see KaroAPI#USER_DRAN
	 * 
	 * @param userId - the user id
	 * @return the list of games
	 */
	public CompletableFuture<List<Game>> getUserDran(int userId)
	{
		return loadAsync(USER_DRAN.replace(PLACEHOLDER, userId).doGet(), PARSER_GAME_LIST);
	}

	/**
	 * Get the list of favorites
	 * 
	 * @return the list of games
	 */
	public CompletableFuture<List<Game>> getFavs()
	{
		return loadAsync(FAVS.doGet(), PARSER_GAME_LIST);
	}

	/**
	 * Add a game to the list of favorites
	 * 
	 * @param gameId - the game to mark as favorite
	 * @return void
	 */
	public CompletableFuture<Void> addFav(int gameId)
	{
		return loadAsync(FAVS_EDIT.replace(PLACEHOLDER, gameId).doPut(), PARSER_VOID);
	}

	/**
	 * Remove a game to the list of favorites
	 * 
	 * @param gameId - the game to unmark as favorite
	 * @return void
	 */
	public CompletableFuture<Void> removeFav(int gameId)
	{
		return loadAsync(FAVS_EDIT.replace(PLACEHOLDER, gameId).doDelete(), PARSER_VOID);
	}

	/**
	 * Get the list of favorites
	 * 
	 * @return the list of games
	 */
	public CompletableFuture<java.util.Map<Integer, String>> getNotes()
	{
		return loadAsync(NOTES.doGet(), PARSER_NOTES);
	}

	/**
	 * Add a game to the list of favorites
	 * 
	 * @param gameId - the game to mark as favorite
	 * @return void
	 */
	public CompletableFuture<Void> addNote(int gameId, String text)
	{
		HashMap<String, Object> args = new HashMap<>();
		args.put("text", text);
		return loadAsync(NOTES_EDIT.replace(PLACEHOLDER, gameId).doPut(args, EnumContentType.json), PARSER_VOID);
	}

	/**
	 * Remove a game to the list of favorites
	 * 
	 * @param gameId - the game to unmark as favorite
	 * @return void
	 */
	public CompletableFuture<Void> removeNote(int gameId)
	{
		return loadAsync(NOTES_EDIT.replace(PLACEHOLDER, gameId).doDelete(), PARSER_VOID);
	}

	/**
	 * Get the list blockers.<br>
	 * 
	 * @see KaroAPI#BLOCKERS
	 * 
	 * @return the list of users
	 */
	public CompletableFuture<List<User>> getBlockers()
	{
		return loadAsync(BLOCKERS.doGet(), PARSER_USER_LIST);
	}

	/**
	 * Get the list blockers for a specific user.<br>
	 * 
	 * @see KaroAPI#USER_BLOCKERS
	 * 
	 * @return the list of users
	 */
	public CompletableFuture<List<User>> getUserBlockers(int userId)
	{
		return loadAsync(USER_BLOCKERS.replace(PLACEHOLDER, userId).doGet(), PARSER_USER_LIST);
	}

	///////////////////////
	// games
	///////////////////////

	/**
	 * Get all games
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#GAMES
	 * @return the list of all games
	 */
	public CompletableFuture<List<Game>> getGames()
	{
		return getGames(null, null, null, null, null, null, null, null);
	}

	/**
	 * Get the games filtered.<br>
	 * Each filter is applied only if it is set (not null). If the filter is null, it will be ignored (see class description).<br>
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#GAMES
	 * @param mine - the mine filter
	 * @param sort - the sort filter
	 * @param user - the user filter
	 * @param finished - the finished filter
	 * @param name - the name filter
	 * @param nameStart - the nameStart filter
	 * @param limit - the limit filter
	 * @param offset - the offset filter
	 * @return the list of all games filtered by the given criteria
	 */
	public CompletableFuture<List<Game>> getGames(Boolean mine, EnumUserGamesort sort, Integer user, Boolean finished, String name, Integer nameStart, Integer limit, Integer offset)
	{
		HashMap<String, Object> args = new HashMap<>();
		if(mine != null)
			args.put("mine", mine ? "1" : "0");
		if(sort != null)
			args.put("sort", sort.name());
		if(user != null)
			args.put("user", user.toString());
		if(finished != null)
			args.put("finished", finished ? "1" : "0");
		if(name != null)
			args.put("name", name);
		if(nameStart != null)
			args.put("nameStart", nameStart.toString());
		if(limit != null)
			args.put("limit", limit.toString());
		if(offset != null)
			args.put("offset", offset.toString());

		return loadAsync(GAMES3.doGet(args), PARSER_GAME_LIST);
	}

	/**
	 * Get a game by id
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#GAME
	 * @param gameId - the game id
	 * @return the game
	 */
	public CompletableFuture<Game> getGame(int gameId)
	{
		return getGame(gameId, null, null, null);
	}

	/**
	 * Get a game by id with optional additional information.<br>
	 * Each argument is applied only if it is set (not null). If the argument is null, it will be ignored (see class description).<br>
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#MAP
	 * @param mapId - the map id
	 * @param mapcode - true or false or null
	 * @param mapcode - true or false or null
	 * @param mapcode - true or false or null
	 * @return the map
	 */
	public CompletableFuture<Game> getGame(int gameId, Boolean mapcode, Boolean players, Boolean moves)
	{
		HashMap<String, Object> args = new HashMap<>();
		if(mapcode != null)
			args.put("mapcode", (mapcode ? "1" : "0"));
		if(players != null)
			args.put("players", (players ? "1" : "0"));
		if(moves != null)
			args.put("moves", (moves ? "1" : "0"));

		return loadAsync(GAME.replace(PLACEHOLDER, gameId).doGet(args), PARSER_GAME);
	}

	/**
	 * Create a new game
	 * 
	 * @param plannedGame - the {@link PlannedGame} to create
	 * @return the created {@link Game}
	 */
	public CompletableFuture<Game> createGame(PlannedGame plannedGame)
	{
		String json = JSONUtil.serialize(plannedGame);
		return loadAsync(GAME_CREATE.doPost(json, EnumContentType.json), PARSER_GAME_CONTAINER);
	}

	/**
	 * Select a start position
	 * 
	 * @param gameId - the game
	 * @param move - the position to select
	 * @return true if the operation was successful, false otherwise
	 */
	public CompletableFuture<Boolean> selectStartPosition(int gameId, Move move)
	{
		HashMap<String, Object> args = new HashMap<>();
		args.put("GID", "" + gameId);
		args.put("startx", "" + move.getX());
		args.put("starty", "" + move.getY());
		if(move.getMsg() != null)
			args.put("movemessage", move.getMsg());
		return loadAsync(GAME_MOVE.doGet(args), (result) -> { return result != null && result.contains("<A HREF=showmap.php?GID=" + gameId + ">Zum Spiel zur&uuml;ck</A>"); });
	}

	/**
	 * Make a move
	 * 
	 * @param gameId - the game
	 * @param move - the move to make
	 * @return true if the operation was successful, false otherwise
	 */
	public CompletableFuture<Boolean> move(int gameId, Move move)
	{
		HashMap<String, Object> args = new HashMap<>();
		args.put("GID", "" + gameId);
		args.put("xpos", "" + move.getX());
		args.put("ypos", "" + move.getY());
		args.put("xvec", "" + move.getXv());
		args.put("yvec", "" + move.getYv());
		if(move.getMsg() != null)
			args.put("movemessage", move.getMsg());
		return loadAsync(GAME_MOVE.doGet(args), (result) -> { return result != null && result.contains("<A HREF=showmap.php?GID=" + gameId + ">Zum Spiel zur&uuml;ck</A>"); });
	}

	/**
	 * Kick a player.<br>
	 * Note: kicking foreign players will fail if you are not Didi ;-)
	 * 
	 * @param gameId - the game
	 * @param move - the move to make
	 * @return true if the operation was successful, false otherwise
	 */
	public CompletableFuture<Boolean> kick(int gameId, int userId)
	{
		HashMap<String, Object> args = new HashMap<>();
		args.put("GID", "" + gameId);
		args.put("UID", "" + userId);
		args.put("sicher", "1");
		return loadAsync(GAME_KICK.doGet(args), (result) -> { return result != null && result.contains("Fertig, Du bist draussen..."); });
	}

	///////////////////////
	// maps
	///////////////////////

	/**
	 * Get all maps
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#MAPS
	 * @return the list of all maps
	 */
	public CompletableFuture<List<Map>> getMaps()
	{
		return getMaps(null);
	}

	/**
	 * Get all maps with or without mapcode.<br>
	 * Each argument is applied only if it is set (not null). If the argument is null, it will be ignored (see class description).<br>
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#MAPS
	 * @param mapcode - true or false
	 * @return the list of all maps with optional mapcode
	 */
	public CompletableFuture<List<Map>> getMaps(Boolean mapcode)
	{
		HashMap<String, Object> args = new HashMap<>();
		if(mapcode != null)
			args.put("mapcode", (mapcode ? "1" : "0"));

		return loadAsync(MAPS.doGet(args), PARSER_MAP_LIST);
	}

	/**
	 * Get a map by id
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#MAP
	 * @param mapId - the map id
	 * @return the map
	 */
	public CompletableFuture<Map> getMap(int mapId)
	{
		return getMap(mapId, null);
	}

	/**
	 * Get a map by id with optional mapcode.<br>
	 * Each argument is applied only if it is set (not null). If the argument is null, it will be ignored (see class description).<br>
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#MAP
	 * @param mapId - the map id
	 * @param mapcode - true or false or null
	 * @return the map
	 */
	public CompletableFuture<Map> getMap(int mapId, Boolean mapcode)
	{
		HashMap<String, Object> args = new HashMap<>();
		if(mapcode != null)
			args.put("mapcode", (mapcode ? "1" : "0"));

		return loadAsync(MAP.replace(PLACEHOLDER, mapId).doGet(args), PARSER_MAP);
	}

	/**
	 * Get a map code by id
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#MAP
	 * @param mapId - the map id
	 * @return the map code
	 */
	public CompletableFuture<String> getMapCode(int mapId)
	{
		return loadAsync(MAP.replace(PLACEHOLDER, mapId + ".txt").doGet(), PARSER_RAW);
	}

	///////////////////////
	// mapimages
	///////////////////////

	/**
	 * Get a map image by id
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#MAP_IMAGE
	 * @param mapId - the map id
	 * @return the map
	 */
	public BufferedImage getMapImage(int mapId)
	{
		return getMapImage(mapId, null);
	}

	/**
	 * Get a map thumb image.<br>
	 * Each argument is applied only if it is set (not null). If the argument is null, it will be ignored (see class description).<br>
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#MAP_IMAGE
	 * @param mapId - the map id
	 * @param thumb - true or false or null
	 * @param cps - true or false or null
	 * @return the map
	 */
	public BufferedImage getMapThumb(int mapId, Boolean cps)
	{
		HashMap<String, Object> args = new HashMap<>();
		args.put("thumb", "1");
		if(cps != null)
			args.put("cps", (cps ? "1" : "0"));

		return getMapImage(mapId, args);
	}

	/**
	 * Get a map by id with optional arguments.<br>
	 * Each argument is applied only if it is set (not null). If the argument is null, it will be ignored (see class description).<br>
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#MAP_IMAGE
	 * @param mapId - the map id
	 * @param size - in px (mandatory)
	 * @param border - in px (optional)
	 * @param cps - true or false or null (optional)
	 * @return the map
	 */
	public BufferedImage getMapImageByPixelSize(int mapId, int size, Integer border, Boolean cps)
	{
		HashMap<String, Object> args = new HashMap<>();
		args.put("size", Integer.toString(size));
		if(border != null)
			args.put("border", border.toString());
		if(cps != null)
			args.put("cps", (cps ? "1" : "0"));

		return getMapImage(mapId, args);
	}

	/**
	 * Get a map by id with optional arguments.<br>
	 * Each argument is applied only if it is set (not null). If the argument is null, it will be ignored (see class description).<br>
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#MAP_IMAGE
	 * @param mapId - the map id
	 * @param width - the width (optional)
	 * @param height - the height (optional)
	 * @param cps - true or false or null (optional)
	 * @return the map
	 */
	public BufferedImage getMapImageByDimension(int mapId, Integer width, Integer height, Boolean cps)
	{
		HashMap<String, Object> args = new HashMap<>();
		if(width != null)
			args.put("width", width.toString());
		if(height != null)
			args.put("height", height.toString());
		if(cps != null)
			args.put("cps", (cps ? "1" : "0"));

		return getMapImage(mapId, args);
	}

	/**
	 * Get a map by id with optional arguments.<br>
	 * Internal method for use by {@link KaroAPI#getMapImage(int)}, {@link KaroAPI#getMapImageByDimension(int, Integer, Integer, Boolean)}, and
	 * {@link KaroAPI#getMapImageByPixelSize(int, int, Integer, Boolean)} instead.
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#MAP_IMAGE
	 * @param mapId - the map id
	 * @param width - the width (optional)
	 * @param height - the height (optional)
	 * @param cps - true or false or null (optional)
	 * @return the map
	 */
	protected BufferedImage getMapImage(int mapId, HashMap<String, Object> args)
	{
		try
		{
			// use the doGet mechanism to generate the URL and query the true image location
			URLLoader tmp = MAP_IMAGE.replace(PLACEHOLDER, mapId).parameterize(args);
			logger.debug("loading image information for map " + mapId + ": " + tmp.getUrl());
			return ImageIO.read(new URL(tmp.getUrl()));
		}
		catch(Exception e)
		{
			logger.error("could not load image", e);
			e.printStackTrace();
			return null;
		}
	}

	///////////////////////
	// chat
	///////////////////////

	/**
	 * Get the chat messages in the specified range
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#CHAT
	 * @param start - the id of the first message to get
	 * @param limit - the number of messages
	 * @return the list of chat messages
	 */
	public CompletableFuture<List<ChatMessage>> getChatMessages(int start, int limit)
	{
		HashMap<String, Object> args = new HashMap<>();
		args.put("start", start);
		args.put("limit", limit);
		return loadAsync(CHAT.parameterize(args).doGet(), PARSER_CHAT_LIST);
	}

	/**
	 * Get the last chat message
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#CHAT_LAST
	 * @return the chat message
	 */
	public CompletableFuture<ChatMessage> getChatLastMessage()
	{
		return loadAsync(CHAT_LAST.doGet(), PARSER_CHAT_MESSAGE);
	}

	/**
	 * Send a new message to the chat
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#CHAT
	 * @param message - the message to send
	 * @return the sent message confirmed by the server
	 */
	public CompletableFuture<ChatMessage> sendChatMessage(String message)
	{
		HashMap<String, Object> args = new HashMap<>();
		args.put("msg", message);
		return loadAsync(CHAT.doPost(args, EnumContentType.json), PARSER_CHAT_MESSAGE);
	}

	/**
	 * Get the active chat users
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#CHAT_USERS
	 * @return the list of users
	 */
	public CompletableFuture<List<User>> getChatUsers()
	{
		return loadAsync(CHAT_USERS.doGet(), PARSER_USER_LIST);
	}

	///////////////////////
	// messaging
	///////////////////////

	/**
	 * Get the users the current user is/was in contact with via the in game messaging system.
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#CONTACTS
	 * @return the list of contact users
	 */
	public CompletableFuture<List<User>> getContacts()
	{
		return loadAsync(CONTACTS.doGet(), PARSER_USER_LIST);
	}

	/**
	 * Send a message using the in game messaging system to another user
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#MESSAGES
	 * @param userId - the user to send the message to
	 * @param message - the message to send
	 * @return the sent message confirmed by the server
	 */
	public CompletableFuture<UserMessage> sendUserMessage(int userId, String message)
	{
		HashMap<String, Object> args = new HashMap<>();
		args.put("text", message);
		return loadAsync(MESSAGES.replace(PLACEHOLDER, userId).doPost(args, EnumContentType.json), PARSER_USER_MESSAGE);
	}

	/**
	 * Get all messages that have been sent to or received from another user using the in game messaging system.
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#MESSAGES
	 * @param userId - the other user
	 * @return the list of messages
	 */
	public CompletableFuture<List<UserMessage>> getUserMessage(int userId)
	{
		return loadAsync(MESSAGES.replace(PLACEHOLDER, userId).doGet(), PARSER_USER_MESSAGE_LIST);
	}

	/**
	 * Mark all messages from a given user as read. (Set uc-flag to 0.)
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#MESSAGES
	 * @param userId - the other user
	 * @return TODO currently PATCH is not supported by {@link HttpURLConnection}
	 */
	@Deprecated(since = "PATCH IS NOT SUPPORTED")
	CompletableFuture<String> readUserMessage(int userId)
	{
		return loadAsync(MESSAGES.replace(PLACEHOLDER, userId).doPatch(), PARSER_RAW);
	}

	///////////////////////
	// load logic
	///////////////////////

	/**
	 * (Re)-Load a given object from the API:<br>
	 * <br>
	 * The object passed must have an ID set (see {@link Identifiable#getId()}. This method then performs a get for the given object type and ID at
	 * the KaroAPI. When the request is finished, the content of the newly loaded object will be copied to the original object to updated it.<br>
	 * <br>
	 * Currently supported types are:
	 * <ul>
	 * <li>{@link User}</li>
	 * <li>{@link Game}</li>
	 * <li>{@link Map}</li>
	 * </ul>
	 * Example:
	 * <ol>
	 * <li>you load a user from the backend, say user {"id":9999, "login":"foo","activeGames":10, ...}</li>
	 * <li>now you create 5 games with that user -&gt; activeGames should increase</li>
	 * <li>by use of {@link KaroAPI#load(Identifiable)} you can update the existing object:
	 * 
	 * <pre>
	 * updated  {"id":9999, "login":"foo","activeGames":15, ...}
	 *                 |              |                  |
	 *                 | unchanged    | unchanged        | original object is updated by copying the value (15)
	 *                 V              V                  V 
	 * original {"id":9999, "login":"foo","activeGames":10, ...}
	 * </pre>
	 * 
	 * </li>
	 * <li>you can continue using the original object where it is already in use (no need to replace it in arrays, lists or references)</li>
	 * </ol>
	 *
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see Identifiable
	 * @param <T>
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Identifiable> CompletableFuture<T> load(T object)
	{
		if(object == null)
			throw new IllegalArgumentException("object must not be null");
		if(object.getId() == null)
			throw new IllegalArgumentException("object id must not be null");

		int id = object.getId();
		CompletableFuture<T> loader;

		if(object instanceof User)
			loader = (CompletableFuture<T>) getUser(id);
		else if(object instanceof Game)
			loader = (CompletableFuture<T>) getGame(id);
		else if(object instanceof Map)
			loader = (CompletableFuture<T>) getMap(id);
		else
			throw new IllegalArgumentException("unsupported type: " + object.getClass());

		return loader.whenComplete((result, th) -> { ReflectionsUtil.copyFields(result, object, false); });
	}

	///////////////////////
	// LOOK UP FUNCTIONALITY
	///////////////////////

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Class<T> cls, int id)
	{
		try
		{
			if(cls.equals(User.class))
				return (T) getUser(id).get();
			else if(cls.equals(Map.class))
				return (T) getMap(id).get();
			else if(cls.equals(Game.class))
				return (T) getGame(id).get();
			else
				logger.error("unsupported lookup type");
		}
		catch(ExecutionException | InterruptedException e)
		{
			logger.error("could not look up " + cls.getName() + " with id " + id, e);
		}
		return null;
	}
}
