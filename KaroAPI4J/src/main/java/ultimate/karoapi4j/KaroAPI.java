package ultimate.karoapi4j;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;

import ultimate.karoapi4j.enums.EnumContentType;
import ultimate.karoapi4j.enums.EnumUserGamesort;
import ultimate.karoapi4j.exceptions.KaroAPIException;
import ultimate.karoapi4j.model.base.Identifiable;
import ultimate.karoapi4j.model.extended.AddictInfo;
import ultimate.karoapi4j.model.official.ChatMessage;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Generator;
import ultimate.karoapi4j.model.official.KarolenderBlatt;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.MovesListEntry;
import ultimate.karoapi4j.model.official.NotesListEntry;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.Smilie;
import ultimate.karoapi4j.model.official.Tag;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.model.official.UserMessage;
import ultimate.karoapi4j.utils.CollectionsUtil;
import ultimate.karoapi4j.utils.JSONUtil;
import ultimate.karoapi4j.utils.JSONUtil.IDLookUp;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karoapi4j.utils.ReflectionsUtil;
import ultimate.karoapi4j.utils.StringUtil;
import ultimate.karoapi4j.utils.URLLoader;
import ultimate.karoapi4j.utils.URLLoader.BackgroundLoader;
import ultimate.karoapi4j.utils.Version;

/**
 * This is the wrapper for accessing the Karo API.<br>
 * <br>
 * Note: Accessing the API requires a user and password for www.karopapier.de which can be passed
 * with the constructor. Afterwards it is
 * recommended to check the successful login using {@link KaroAPI#check()}.<br>
 * <br>
 * Each API call will return a {@link CompletableFuture} which wraps the underlying API call and
 * which then can be used to either load the results
 * either blocking or asynchronously (see {@link URLLoader}).<br>
 * <br>
 * For calls with filter arguments, each argument is applied only if it is set to a non null value.
 * If the argument is null, it will be ignored.
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
	protected static transient final Logger	logger			= LogManager.getLogger(KaroAPI.class);

	///////////////////////////////////////////
	// config & constants & static variables //
	///////////////////////////////////////////

	/**
	 * The config key
	 */
	public static final String				CONFIG_KEY		= "karoAPI";
	/**
	 * The generator key
	 */
	public static final String				GENERATOR_KEY	= "generator";

	/**
	 * The default placeholder for API urls
	 */
	public static final String				PLACEHOLDER		= "$";
	/**
	 * The maximum number of allowed retries
	 */
	public static final int					MAX_RETRIES		= 10;

	/**
	 * The version of the {@link KaroAPI}
	 */
	private static Version					version;
	/**
	 * Init timeout set via property karoAPI.initTimeout
	 */
	private static int						initTimeout		= 30;
	/**
	 * Feature flag "ensureMapSee" set via property karoAPI.ensureMapSeed
	 */
	private static boolean					ensureMapSeed	= true;
	/**
	 * The {@link ExecutorService} used to run all BackgroundLoaders. This {@link ExecutorService}
	 * is static since load balancing shall be possible
	 * across multiple instances of the {@link KaroAPI}.
	 */
	private static ExecutorService			executor		= Executors.newFixedThreadPool(10);

	/**
	 * The name of the application using the {@link KaroAPI}
	 */
	private static String					applicationName;

	/**
	 * The version of the application using the {@link KaroAPI}
	 */
	private static Version					applicationVersion;

	/**
	 * Additional API properties
	 */
	private static Properties				apiProperties;

	static
	{
		// read the version from the properties
		logger.debug("loading KaroAPI version");
		try
		{
			apiProperties = PropertiesUtil.loadProperties(KaroAPI.class, "karoapi4j.properties");

			version = new Version(apiProperties.getProperty(KaroAPI.CONFIG_KEY + ".version"));
			logger.debug("version       = " + version);

			initTimeout = Integer.parseInt(apiProperties.getProperty(KaroAPI.CONFIG_KEY + ".initTimeout"));
			logger.debug("initTimeout   = " + initTimeout);

			ensureMapSeed = Boolean.parseBoolean(apiProperties.getProperty(KaroAPI.CONFIG_KEY + ".ensureMapSeed"));
			logger.debug("ensureMapSeed = " + ensureMapSeed);
		}
		catch(IOException e)
		{
			logger.error("could not determine API version", e);
		}
	}

	/**
	 * @return the version of the {@link KaroAPI}
	 */
	public static Version getVersion()
	{
		return version;
	}

	/**
	 * @return the name of the application using the {@link KaroAPI}
	 */
	public static String getApplicationName()
	{
		return applicationName;
	}

	/**
	 * @return the version of the application using the {@link KaroAPI}
	 */
	public static Version getApplicationVersion()
	{
		return applicationVersion;
	}

	/**
	 * The name of the application using the {@link KaroAPI}
	 * 
	 * @param applicationName - the name
	 */
	public static void setApplication(String applicationName, Version applicationVersion)
	{
		KaroAPI.applicationName = applicationName;
		KaroAPI.applicationVersion = applicationVersion;
	}

	public static String getUserAgent()
	{
		return "KaroAPI4J/" + getVersion() + " " + (applicationName != null ? applicationName : "unknown-application") + "/"
				+ (applicationVersion != null ? applicationVersion : "?") + " (Java " + System.getProperty("java.version") + ")";
	}

	/**
	 * Set a new {@link ExecutorService}:<br>
	 * The {@link ExecutorService} used to run all BackgroundLoaders. This {@link ExecutorService}
	 * is static since load balancing shall be possible
	 * across multiple instances of the {@link KaroAPI}.
	 * 
	 * @param e - the new {@link ExecutorService}
	 */
	public static void setExecutor(ExecutorService e)
	{
		if(e == null)
			throw new IllegalArgumentException("executor must not be null!");
		executor = e;
	}

	/**
	 * Get the current {@link ExecutorService}:<br>
	 * The {@link ExecutorService} used to run all BackgroundLoaders. This {@link ExecutorService}
	 * is static since load balancing shall be possible
	 * across multiple
	 * instances of the {@link KaroAPI}.
	 * 
	 * @return the {@link ExecutorService}
	 */
	public static ExecutorService getExecutor()
	{
		return executor;
	}
	
	/**
	 * Init timeout set via property karoAPI.initTimeout
	 * @return
	 */
	public static int getInitTimeout()
	{
		return initTimeout;
	}

	/**
	 * Overwrite init timeout set via property karoAPI.initTimeout
	 * @param initTimeout
	 */
	public static void setInitTimeout(int initTimeout)
	{
		KaroAPI.initTimeout = initTimeout;
	}
	
	////////////////////
	// parsers needed //
	////////////////////

	public static final Function<String, Void>									PARSER_VOID					= (result) -> {
																												return null;
																											};
	public static final Function<String, String>								PARSER_RAW					= Function.identity();
	public static final Function<String, java.util.Map<String, Object>>			PARSER_GENERIC				= new JSONUtil.Parser<>(
			new TypeReference<java.util.Map<String, Object>>() {});
	public static final Function<String, List<java.util.Map<String, Object>>>	PARSER_GENERIC_LIST			= new JSONUtil.Parser<>(
			new TypeReference<List<java.util.Map<String, Object>>>() {});
	public static final Function<String, User>									PARSER_USER					= new JSONUtil.Parser<>(
			new TypeReference<User>() {});
	public static final Function<String, List<User>>							PARSER_USER_LIST			= new JSONUtil.Parser<>(
			new TypeReference<List<User>>() {});
	public static final Function<String, Game>									PARSER_GAME					= new JSONUtil.Parser<>(
			new TypeReference<Game>() {});
	public static final Function<String, Game>									PARSER_GAME_CONTAINER		= new JSONUtil.ContainerParser<>(
			new TypeReference<Game>() {}, "game");
	public static final Function<String, List<Game>>							PARSER_GAME_LIST			= new JSONUtil.Parser<>(
			new TypeReference<List<Game>>() {});
	public static final Function<String, List<Move>>							PARSER_MOVE_LIST			= new JSONUtil.Parser<>(
			new TypeReference<List<Move>>() {});
	public static final Function<String, List<MovesListEntry>>					PARSER_MOVES_LIST			= new JSONUtil.Parser<>(
			new TypeReference<List<MovesListEntry>>() {});
	public static final Function<String, List<NotesListEntry>>					PARSER_NOTES_LIST			= new JSONUtil.Parser<>(
			new TypeReference<List<NotesListEntry>>() {});
	public static final Function<String, Map>									PARSER_MAP					= new JSONUtil.Parser<>(
			new TypeReference<Map>() {});
	public static final Function<String, List<Map>>								PARSER_MAP_LIST				= new JSONUtil.Parser<>(
			new TypeReference<List<Map>>() {});
	public static final Function<String, List<Generator>>						PARSER_GENERATOR_LIST		= new JSONUtil.Parser<>(
			new TypeReference<List<Generator>>() {});
	public static final Function<String, ChatMessage>							PARSER_CHAT_MESSAGE			= new JSONUtil.Parser<>(
			new TypeReference<ChatMessage>() {});
	public static final Function<String, List<ChatMessage>>						PARSER_CHAT_LIST			= new JSONUtil.Parser<>(
			new TypeReference<List<ChatMessage>>() {});
	public static final Function<String, UserMessage>							PARSER_USER_MESSAGE			= new JSONUtil.Parser<>(
			new TypeReference<UserMessage>() {});
	public static final Function<String, List<UserMessage>>						PARSER_USER_MESSAGE_LIST	= new JSONUtil.Parser<>(
			new TypeReference<List<UserMessage>>() {});
	public static final Function<String, List<KarolenderBlatt>>					PARSER_KAROLENDERBLATT_LIST	= new JSONUtil.Parser<>(
			new TypeReference<List<KarolenderBlatt>>() {});
	public static final Function<String, List<Smilie>>							PARSER_SMILIE_LIST			= new JSONUtil.Parser<>(
			new TypeReference<List<Smilie>>() {});
	public static final Function<String, List<Tag>>								PARSER_TAG_LIST				= new JSONUtil.Parser<>(
			new TypeReference<List<Tag>>() {});
	// this is a litte more complex: transform a list of [{id:1,text:"a"}, ...] to a map where the
	// ids are the keys and the texts are the values
	public static final Function<String, java.util.Map<Integer, String>>		PARSER_NOTES_MAP			= (result) -> {
																												return CollectionsUtil.flattenMap(
																														CollectionsUtil.toMap(
																																PARSER_NOTES_LIST
																																		.apply(result)),
																														"text");
																											};
	public static final Function<String, java.util.Map<Integer, List<Move>>>	PARSER_MOVES_MAP			= (result) -> {
																												return CollectionsUtil.flattenMap(
																														CollectionsUtil.toMap(
																																PARSER_MOVES_LIST
																																		.apply(result)),
																														"moves");
																											};
	// public static final Function<String, java.util.Map<Integer, String>> PARSER_NOTES_LIST =
	// (result) -> {
	// return CollectionsUtil.toMap(PARSER_GENERIC_LIST.apply(result), "id", "text");
	// };
	public static final Function<String, String>								PARSER_NOTE					= (result) -> {
																												return (String) PARSER_GENERIC
																														.apply(result).get("text");
																											};
	public static final Function<String, String>								PARSER_KEY					= (result) -> {
																												return (String) PARSER_GENERIC
																														.apply(result).get("api_key");
																											};

	//////////////
	// api URLs //
	//////////////

	// base
	protected final URLLoader													KAROPAPIER					= new URLLoader(
			"https://www.karopapier.de");
	protected final URLLoader													API							= KAROPAPIER.relative("/api");
	protected final URLLoader													KEY							= API.relative("/key");
	// users
	protected final URLLoader													USERS						= API.relative("/users");
	protected final URLLoader													USER						= USERS.relative("/" + PLACEHOLDER);
	protected final URLLoader													USER_DRAN					= USER.relative("/dran");
	protected final URLLoader													USER_BLOCKERS				= USER.relative("/blockers");
	// current user
	protected final URLLoader													CURRENT_USER				= API.relative("/user");
	protected final URLLoader													CHECK						= CURRENT_USER.relative("/check");
	protected final URLLoader													FAVS						= CURRENT_USER.relative("/favs");
	protected final URLLoader													FAVS_EDIT					= FAVS.relative("/" + PLACEHOLDER);
	protected final URLLoader													BLOCKERS					= API.relative("/blockers");
	protected final URLLoader													NOTES						= API.relative("/notes");
	protected final URLLoader													NOTES_FOR_GAME				= NOTES.relative("/" + PLACEHOLDER);
	protected final URLLoader													PLANNED_MOVES				= API.relative("/planned-moves");
	protected final URLLoader													PLANNED_MOVES_FOR_GAME		= PLANNED_MOVES
			.relative("/" + PLACEHOLDER);
	// addicts
	protected final URLLoader													ADDICTS						= KAROPAPIER
			.relative("/addicts?by=" + PLACEHOLDER);
	// games
	protected final URLLoader													GAMES						= API.relative("/games");
	protected final URLLoader													GAME						= GAMES.relative("/" + PLACEHOLDER);
	protected final URLLoader													GAME_CREATE					= API.relative("/game");
	protected final URLLoader													GAME_MOVE					= KAROPAPIER.relative("/move.php");
	@Deprecated // (since = "3.0.7")
	protected final URLLoader													GAME_KICK					= KAROPAPIER.relative("/kickplayer.php");
	protected final URLLoader													GAME_REFRESH				= KAROPAPIER
			.relative("/showmap.php?GID=" + PLACEHOLDER);
	// maps
	protected final URLLoader													MAPS						= API.relative("/maps");
	protected final URLLoader													MAP							= MAPS.relative("/" + PLACEHOLDER);
	protected final URLLoader													MAP_CODE					= API
			.relative("/mapcode/" + PLACEHOLDER + ".txt");
	// mapimages
	// do not use API as the base here, since we do not need the authentication here
	protected final URLLoader													MAP_IMAGE					= KAROPAPIER
			.relative("/map/" + PLACEHOLDER + ".png");
	// generators
	protected final URLLoader													GENERATORS					= API.relative("/generators");
	protected final URLLoader													GENERATE_CODE				= GENERATORS.relative("/" + PLACEHOLDER);
	protected final URLLoader													GENERATE_MAP				= API.relative("/mapgenerator/generate");
	// chat
	protected final URLLoader													CHAT						= API.relative("/chat");
	protected final URLLoader													CHAT_MESSAGE				= CHAT.relative("/" + PLACEHOLDER);
	protected final URLLoader													CHAT_LAST					= CHAT.relative("/last");
	protected final URLLoader													CHAT_USERS					= CHAT.relative("/users");
	// messaging
	protected final URLLoader													CONTACTS					= API.relative("/contacts");
	protected final URLLoader													MESSAGES					= API
			.relative("/messages/" + PLACEHOLDER);
	// misc
	protected final URLLoader													KAROLENDERBLATT				= API.relative("/karolenderblatt");
	protected final URLLoader													KAROLENDERBLATT_FOR_DATE	= KAROLENDERBLATT
			.relative("/" + PLACEHOLDER);
	protected final URLLoader													SMILIES						= API.relative("/smilies");
	protected final URLLoader													TAGS						= API.relative("/tags");
	protected final URLLoader													TAGS_SUGGESTED				= TAGS.relative("/suggested-tags");

	/**
	 * The number of retries to perform.<br>
	 * Default = 0
	 * 
	 * @see KaroAPI#loadAsync(BackgroundLoader, Function, int)
	 */
	private int																	performRetries				= 0;

	private KaroAPI()
	{
		KAROPAPIER.addRequestProperty("User-Agent", getUserAgent());
	}

	/**
	 * Get an instance for the given API-Key
	 * 
	 * @param apiKey - the API-Key retrieved previously from
	 *            <a href="https://www.karopapier.de/api/key">https://www.karopapier.de/api/key</a>
	 */
	public KaroAPI(String apiKey) throws KaroAPIException
	{
		this();
		try
		{
			setAPIKey(apiKey);
			User user = check().get(initTimeout, TimeUnit.SECONDS);
			if(user == null)
				throw new KaroAPIException("user is null!");
		}
		catch(InterruptedException | ExecutionException | TimeoutException e)
		{
			throw new KaroAPIException("invalid API-Key '" + apiKey + "'", e);
		}
	}

	/**
	 * Get an instance for the given user
	 * 
	 * @param username - the karopapier.de username
	 * @param password - the password
	 */
	public KaroAPI(String username, String password) throws KaroAPIException
	{
		this();
		try
		{
			Boolean loginSuccessful = login(username, password).get(initTimeout, TimeUnit.SECONDS);
			if(!loginSuccessful)
				throw new KaroAPIException("login not successful");
		}
		catch(InterruptedException | ExecutionException | TimeoutException e)
		{
			throw new KaroAPIException("could not login user '" + username + "'", e);
		}
	}

	/**
	 * Initiate and login this {@link KaroAPI} instance for the given user.
	 * 
	 * @param username - the karopapier.de username
	 * @param password - the password
	 * @return success or not
	 */
	protected CompletableFuture<Boolean> login(String username, String password)
	{
		if(username == null || password == null)
			throw new IllegalArgumentException("username and password must not be null!");
		KEY.addRequestProperty("X-Auth-Login", username);
		KEY.addRequestProperty("X-Auth-Password", password);
		return getKey().thenComposeAsync((key) -> {
			if(key == null)
				throw new KaroAPIException("could not retrieve key");

			setAPIKey(key);

			return check().thenApplyAsync((user) -> {
				if(user == null)
					throw new KaroAPIException("check returned no user");
				if(!user.getLogin().equals(username))
					throw new KaroAPIException("check returned wrong user");
				return true;
			});
		});
	}

	protected void setAPIKey(String key)
	{
		KAROPAPIER.addRequestProperty("X-Auth-Key", key);
	}

	/**
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
	 * Non-static wrappeer for calling {@link KaroAPI#loadAsync(BackgroundLoader, Function, int)}
	 * that passes the currently set number of retries for
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

	/**
	 * Asynchronously schedule and execute a {@link BackgroundLoader}.<br>
	 * This method will create {@link CompletableFuture} that is passed to the set
	 * {@link KaroAPI#executor} for asynchronous execution.<br>
	 * It is further capable of appending retries to the {@link CompletableFuture} optionally.
	 * 
	 * @param <T> - the type that the parser will return
	 * @param backgroundLoader - the {@link BackgroundLoader} to execute
	 * @param parser - the parser that shall be used to parse the loaded content
	 * @param retries - the number of <b>additional</b> retries to perform if the first execution
	 *            fails.
	 * @return the {@link CompletableFuture}
	 */
	private <T> CompletableFuture<T> loadAsync(BackgroundLoader backgroundLoader, Function<String, T> parser, int retries)
	{
		// Important: use the set executor to limit the stress on the API
		CompletableFuture<T> cf = CompletableFuture.supplyAsync(backgroundLoader, executor).thenApply(parser);

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
					{
						// return CompletableFuture.failedFuture(t);
						// Java 8 compatibility
						CompletableFuture<T> cf = new CompletableFuture<T>();
						cf.completeExceptionally(t);
						return cf;
					}
				};
			}).thenCompose(Function.identity());
		}
		return cf;
	}

	///////////////////////
	// user
	///////////////////////

	/**
	 * Get the API key for this user
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#KEY
	 * @return the API key
	 */
	public CompletableFuture<String> getKey()
	{
		return loadAsync(KEY.doGet((String) null), PARSER_KEY);
	}

	/**
	 * Reset the API key for this user
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#KEY
	 * @return the...
	 */
	public CompletableFuture<String> resetkey()
	{
		return loadAsync(KEY.doDelete((String) null), PARSER_RAW);
	}

	/**
	 * Check the currently logged in user
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
	 * Each filter is applied only if it is set (not null). If the filter is null, it will be
	 * ignored (see class description).<br>
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
	 * Get the list of notes
	 * 
	 * @return the list of notes
	 */
	public CompletableFuture<java.util.Map<Integer, String>> getNotes()
	{
		return loadAsync(NOTES.doGet(), PARSER_NOTES_MAP);
	}

	/**
	 * Get the notes for a game
	 * 
	 * @param gameId - the game for which to get the notes
	 * @return the notes
	 */
	public CompletableFuture<String> getNote(int gameId)
	{
		return loadAsync(NOTES_FOR_GAME.replace(PLACEHOLDER, gameId).doGet(), PARSER_NOTE);
	}

	/**
	 * Add a note to a game
	 * 
	 * @param gameId - the game to mark as favorite
	 * @param text - the note to add
	 * @return void
	 */
	public CompletableFuture<Void> addNote(int gameId, String text)
	{
		HashMap<String, Object> args = new HashMap<>();
		args.put("text", text);
		return loadAsync(NOTES_FOR_GAME.replace(PLACEHOLDER, gameId).doPut(args, EnumContentType.json), PARSER_VOID);
	}

	/**
	 * Remove a note for a game
	 * 
	 * @param gameId - the game to unmark as favorite
	 * @return void
	 */
	public CompletableFuture<Void> removeNote(int gameId)
	{
		return loadAsync(NOTES_FOR_GAME.replace(PLACEHOLDER, gameId).doDelete(), PARSER_VOID);
	}

	/**
	 * Get the list blockers.<br>
	 * 
	 * @see KaroAPI#BLOCKERS
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
	 * @return the list of users
	 */
	public CompletableFuture<List<User>> getUserBlockers(int userId)
	{
		return loadAsync(USER_BLOCKERS.replace(PLACEHOLDER, userId).doGet(), PARSER_USER_LIST);
	}

	/**
	 * Get the list of planned moves for the current user.<br>
	 *
	 * @see KaroAPI#PLANNED_MOVES
	 * @return the list of users
	 */
	public CompletableFuture<java.util.Map<Integer, List<Move>>> getPlannedMoves()
	{
		return loadAsync(PLANNED_MOVES.doGet(), PARSER_MOVES_MAP);
	}

	/**
	 * Get the list of planned moves for a specific game (and the current user).<br>
	 * 
	 * @see KaroAPI#PLANNED_MOVES
	 * @return the list of users
	 */
	public CompletableFuture<List<Move>> getPlannedMoves(int gameId)
	{
		return loadAsync(PLANNED_MOVES_FOR_GAME.replace(PLACEHOLDER, gameId).doGet(), PARSER_MOVE_LIST);
	}

	/**
	 * Add planned moves to a game
	 * 
	 * @param gameId - the game to mark as favorite
	 * @param moves - the moves to plan
	 * @return void
	 */
	public CompletableFuture<Void> addPlannedMoves(int gameId, List<Move> moves)
	{
		List<java.util.Map<String, Object>> argsList = new LinkedList<>();
		if(moves != null)
		{
			for(Move m : moves)
			{
				HashMap<String, Object> args = new HashMap<>();
				args.put("x", "" + m.getX());
				args.put("y", "" + m.getY());
				args.put("xv", "" + m.getXv());
				args.put("yv", "" + m.getYv());
				argsList.add(args);
			}
		}
		return loadAsync(PLANNED_MOVES_FOR_GAME.replace(PLACEHOLDER, gameId).doPut(argsList, EnumContentType.json), PARSER_VOID);
	}

	/**
	 * Remove planned moves for a game
	 * 
	 * @param gameId - the game to unmark as favorite
	 * @return void
	 */
	public CompletableFuture<Void> removePlannedMoves(int gameId)
	{
		return addPlannedMoves(gameId, null);
		// return loadAsync(PLANNED_MOVES_FOR_GAME.replace(PLACEHOLDER, gameId).doDelete(),
		// PARSER_VOID);
	}

	///////////////////////
	// addicts
	///////////////////////

	public CompletableFuture<java.util.Map<String, AddictInfo>> getAddicts()
	{
		HashMap<String, AddictInfo> addicts = new HashMap<String, AddictInfo>();

		@Deprecated
		Function<String, Void> parser = (html) -> {
			int tableBegin, tableEnd;

			Function<String, String> cellParser = Function.identity();
			Function<String, List<String>> rowParser = (row) -> {
				return StringUtil.processHTMLSection(row, "td", cellParser);
			};

			// process first table
			tableBegin = html.indexOf("<TABLE CLASS=general CELLPADDING=3 WIDTH=98%>");
			tableEnd = html.indexOf("</table>", tableBegin) + "</table>".length();
			List<List<String>> firstTable = StringUtil.processHTMLSection(html.substring(tableBegin, tableEnd), "tr", rowParser);

			AddictInfo ai;
			String login;
			for(List<String> row : firstTable)
			{
				if(row.size() < 10)
					continue;
				try
				{

					login = StringUtil.trimTags(row.get(1));
					synchronized(addicts)
					{
						if(addicts.containsKey(login))
							ai = addicts.get(login);
						else
						{
							ai = new AddictInfo(login);
							addicts.put(login, ai);
						}
					}
					ai.setSignup(Integer.parseInt(StringUtil.trimToNumber(row.get(4))));
					ai.setGamesTotal(Integer.parseInt(StringUtil.trimToNumber(row.get(5))));
					ai.setMovesTotal(Integer.parseInt(StringUtil.trimToNumber(row.get(6))));
					ai.setMovesPerDay(Double.parseDouble(StringUtil.trimToNumber(row.get(7))));
					ai.setWollustMax(Integer.parseInt(StringUtil.trimToNumber(row.get(8))));
					String[] split = row.get(9).split("\\(");
					ai.setKaroMeter(Integer.parseInt(StringUtil.trimToNumber(split[0])));
					ai.setKaroMilliMeterPerHour(Integer.parseInt(StringUtil.trimToNumber(split[1])));

					addicts.put(ai.getLogin(), ai);
				}
				catch(NumberFormatException e)
				{
					logger.error("error parsing AddictInfo", e);
				}
			}

			// process second table
			tableBegin = html.indexOf("<table>", html.indexOf("<h3>WOLLUST, die WOchen-Liste"));
			tableEnd = html.indexOf("</table>", tableBegin) + "</table>".length();
			List<List<String>> secondTable = StringUtil.processHTMLSection(html.substring(tableBegin, tableEnd), "tr", rowParser);

			for(List<String> row : secondTable)
			{
				if(row.size() < 3)
					continue;
				try
				{
					login = StringUtil.trimTags(row.get(1));
					synchronized(addicts)
					{
						if(addicts.containsKey(login))
							ai = addicts.get(login);
						else
						{
							ai = new AddictInfo(login);
							addicts.put(login, ai);
						}
					}
					ai.setWollust(Integer.parseInt(StringUtil.trimToNumber(row.get(2))));
				}
				catch(NumberFormatException e)
				{
					logger.error("error parsing Wollust", e);
				}
			}
			return null;
		};

		// load all possible sortings for addicts to obtain the maximum amount of information possible 
		// @formatter:off
		return CompletableFuture.allOf(
				loadAsync(ADDICTS.replace(PLACEHOLDER, "automoves").doGet(), parser),
				loadAsync(ADDICTS.replace(PLACEHOLDER, "perday").doGet(), parser),
				loadAsync(ADDICTS.replace(PLACEHOLDER, "wollust").doGet(), parser),
				loadAsync(ADDICTS.replace(PLACEHOLDER, "km").doGet(), parser)
			).thenApply(v -> addicts);
		// @formatter:on
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
	 * Each filter is applied only if it is set (not null). If the filter is null, it will be
	 * ignored (see class description).<br>
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
	public CompletableFuture<List<Game>> getGames(Boolean mine, EnumUserGamesort sort, Integer user, Boolean finished, String name, Boolean nameStart,
			Integer limit, Integer offset)
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
			args.put("nameStart", nameStart ? "1" : "0");
		if(limit != null)
			args.put("limit", limit.toString());
		if(offset != null)
			args.put("offset", offset.toString());

		return loadAsync(GAMES.doGet(args), PARSER_GAME_LIST);
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
	 * Get a game by id (with all details
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#GAME
	 * @param gameId - the game id
	 * @return the game
	 */
	public CompletableFuture<Game> getGameWithDetails(int gameId)
	{
		return getGame(gameId, true, true, true);
	}

	/**
	 * Get a game by id with optional additional information.<br>
	 * Each argument is applied only if it is set (not null). If the argument is null, it will be
	 * ignored (see class description).<br>
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#MAP
	 * @param mapId - the map id
	 * @param mapcode - true or false or null
	 * @param players - true or false or null
	 * @param moves - true or false or null
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
	 * Create a new game.
	 * If the map is set to a {@link Generator} the map will be generated first.
	 * Note: Watch out for thread safety: Generated maps are one-time maps. A game has to be started
	 * on the generated map first, before generating the
	 * next map!
	 * 
	 * @param plannedGame - the {@link PlannedGame} to create
	 * @return the created {@link Game}
	 */
	public CompletableFuture<Game> createGame(PlannedGame plannedGame)
	{
		CompletableFuture<Map> ensureMap;

		if(plannedGame.getMap() == null)
			throw new IllegalArgumentException("plannedGame.map must not be null!");
		else if(plannedGame.getMap() instanceof Generator)
			ensureMap = generateMap((Generator) plannedGame.getMap());
		else if(plannedGame.getMap() instanceof Map)
			ensureMap = CompletableFuture.completedFuture((Map) plannedGame.getMap());
		else
			throw new IllegalArgumentException("unknown PlaceToRace type: " + plannedGame.getMap());

		return ensureMap.thenApply(map -> {
			if(map != plannedGame.getMap())
			{
				logger.debug("map generated : " + map.getId());
				// overwrite the map generator with the generated map
				plannedGame.setMap(map);
			}
			String json = JSONUtil.serialize(plannedGame);
			return loadAsync(GAME_CREATE.doPost(json, EnumContentType.json), PARSER_GAME_CONTAINER);
		}).thenCompose(Function.identity());
	}
	
	/**
	 * Check if a (newly created) game exists
	 */
	public CompletableFuture<List<Game>> findGames(PlannedGame g)
	{
		return getGames(null, EnumUserGamesort.gid2, null, null, g.getName(), true, null, null);
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
		return loadAsync(GAME_MOVE.doGet(args), (result) -> {
			return result != null && result.contains("<A HREF=showmap.php?GID=" + gameId + ">Zum Spiel zurück</A>");
		});
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
		return loadAsync(GAME_MOVE.doGet(args), (result) -> {
			return result != null && result.contains("<A HREF=showmap.php?GID=" + gameId + ">Zum Spiel zurück</A>");
		});
	}

	/**
	 * Refresh a game after a crash to trigger the ZZZ
	 * 
	 * @param gameId - the game
	 * @return true if the operation was successful, false otherwise
	 */
	public CompletableFuture<Boolean> refreshAfterCrash(int gameId)
	{
		return loadAsync(GAME_REFRESH.replace(PLACEHOLDER, gameId).doGet(), (result) -> {
			return result != null && result.contains("href=/spiele/" + gameId);
		});
	}

	/**
	 * Kick a player.<br>
	 * Note: kicking foreign players will fail if you are not Didi ;-)
	 * 
	 * @param gameId - the game
	 * @param userId - the user to kick
	 * @return true if the operation was successful, false otherwise
	 */
	@Deprecated // (since = "3.0.7")
	public CompletableFuture<Boolean> kick(int gameId, int userId)
	{
		return leaveGame(gameId);
	}

	/**
	 * Leave a game.<br>
	 * 
	 * @param gameId - the game
	 * @return true if the operation was successful, false otherwise
	 */
	public CompletableFuture<Boolean> leaveGame(int gameId)
	{
		return loadAsync(GAME.replace(PLACEHOLDER, gameId).doDelete(), (result) -> {
			return result != null && result.contains("[]");
		});
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
	 * Each argument is applied only if it is set (not null). If the argument is null, it will be
	 * ignored (see class description).<br>
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
	 * Get a map by id (with all details)
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#MAP
	 * @param mapId - the map id
	 * @return the map
	 */
	public CompletableFuture<Map> getMapWithDetails(int mapId)
	{
		return getMap(mapId, true);
	}

	/**
	 * Get a map by id with optional mapcode.<br>
	 * Each argument is applied only if it is set (not null). If the argument is null, it will be
	 * ignored (see class description).<br>
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
		return loadAsync(MAP_CODE.replace(PLACEHOLDER, mapId).doGet(), PARSER_RAW);
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
	 * Each argument is applied only if it is set (not null). If the argument is null, it will be
	 * ignored (see class description).<br>
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
	 * Each argument is applied only if it is set (not null). If the argument is null, it will be
	 * ignored (see class description).<br>
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
	 * Each argument is applied only if it is set (not null). If the argument is null, it will be
	 * ignored (see class description).<br>
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
	 * Internal method for use by {@link KaroAPI#getMapImage(int)},
	 * {@link KaroAPI#getMapImageByDimension(int, Integer, Integer, Boolean)}, and
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
	// generators
	///////////////////////

	/**
	 * Get all generators
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#GENERATORS
	 * @return the list of all generators
	 */
	public CompletableFuture<List<Generator>> getGenerators()
	{
		return loadAsync(GENERATORS.doGet(), PARSER_GENERATOR_LIST).thenApply(gens -> {
			gens.forEach(g -> {
				if(!"fernschreiber".equalsIgnoreCase(g.getKey()))
					g.getSettings().put("night", false);
			});
			return gens;
		});
	}

	/**
	 * Generate map code by use of a generator
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#GENERATOR_GENERATE_CODE
	 * @return the map code generated
	 */
	public CompletableFuture<String> generateCode(Generator generator)
	{
		HashMap<String, Object> settings = new HashMap<>(generator.getSettings());
		convertValues(settings);
		ensureMapSeed(settings);
		return loadAsync(GENERATE_CODE.replace(PLACEHOLDER, generator.getKey()).parameterize(settings).doGet(), PARSER_RAW);
	}

	/**
	 * Generate a one time map by use of a generator
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#GENERATOR_GENERATE_MAP
	 * @return the map code generated
	 */
	public CompletableFuture<Map> generateMap(Generator generator)
	{
		HashMap<String, Object> settings = new HashMap<>(generator.getSettings());
		settings.put("generator", generator.getKey());
		convertValues(settings);
		ensureMapSeed(settings);
		String json = JSONUtil.serialize(settings);
		return loadAsync(GENERATE_MAP.doPost(json, EnumContentType.json), PARSER_MAP);
	}

	/**
	 * Make sure that the settings contain a seed.
	 * This is necessary if games are created right after another (within a second or less) because
	 * the seed on karopapier is based on server time
	 * with just seconds.
	 * 
	 * @param settings
	 */
	private void ensureMapSeed(HashMap<String, Object> settings)
	{
		logger.debug("ensureMapSeed?" + ensureMapSeed + ", seed?" + settings.containsKey("seed") + ", seed=" + settings.get("seed"));
		if(ensureMapSeed && (!settings.containsKey("seed") || "".equals(settings.get("seed"))))
		{
			// doesn't matter if we use int or string here because both are encoded the same way in
			// the url
			int seed = (int) (settings.hashCode() ^ System.currentTimeMillis());
			logger.debug("generating random seed: " + seed);
			settings.put("seed", seed);
		}
	}

	/**
	 * Make sure that values are understandable by karopapier (e.g. convert booleans to 0 or 1)
	 * 
	 * @param settings
	 */
	private void convertValues(HashMap<String, Object> settings)
	{
		for(Entry<String, Object> e : settings.entrySet())
		{
			if(e.getValue() instanceof Boolean)
				settings.put(e.getKey(), (boolean) e.getValue() ? 1 : 0);
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
	 * Get the chat messages in the specified range
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#CHAT
	 * @param start - the date of the first message to get
	 * @param limit - the number of messages
	 * @return the list of chat messages
	 */
	public CompletableFuture<List<ChatMessage>> getChatMessages(Date start, int limit)
	{
		HashMap<String, Object> args = new HashMap<>();
		args.put("date", new SimpleDateFormat(JSONUtil.DATE_FORMAT).format(start));
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
	 * Get the chat message specified by the given ID
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#CHAT_MESSAGE
	 * @param id - the id of the message to get
	 * @return chat messages
	 */
	public CompletableFuture<ChatMessage> getChatMessage(int id)
	{
		return loadAsync(CHAT_MESSAGE.replace(PLACEHOLDER, id).doGet(), PARSER_CHAT_MESSAGE);
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
	 * Get all messages that have been sent to or received from another user using the in game
	 * messaging system.
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
	@Deprecated // (since = "PATCH IS NOT SUPPORTED")
	CompletableFuture<String> readUserMessage(int userId)
	{
		return loadAsync(MESSAGES.replace(PLACEHOLDER, userId).doPatch(), PARSER_RAW);
	}

	///////////////////////
	// misc
	///////////////////////

	/**
	 * Get all the Karolender-Blatt for a specific date
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#KAROLENDERBLATT_FOR_DATE
	 * @param date - the date to get the Karolenderblatt for
	 * @return the Karolenderblatt entry
	 */
	public CompletableFuture<List<KarolenderBlatt>> getKarolenderBlatt(Date date)
	{
		String dateString = new SimpleDateFormat(JSONUtil.DATE_FORMAT).format(date);
		return loadAsync(KAROLENDERBLATT_FOR_DATE.replace(PLACEHOLDER, dateString).doGet(), PARSER_KAROLENDERBLATT_LIST);
	}

	/**
	 * Get the list of supported smilies
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#SMILIES
	 * @return the list of smilies
	 */
	public CompletableFuture<List<Smilie>> getSmilies()
	{
		return loadAsync(SMILIES.doGet(), PARSER_SMILIE_LIST);
	}

	/**
	 * Get the list of suggested tags
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#TAGS_SUGGESTED
	 * @return the list of tags
	 */
	public CompletableFuture<List<Tag>> getSuggestedTags()
	{
		return loadAsync(TAGS_SUGGESTED.doGet(), PARSER_TAG_LIST);
	}

	///////////////////////
	// load logic
	///////////////////////

	/**
	 * (Re)-Load a given object from the API:<br>
	 * <br>
	 * The object passed must have an ID set (see {@link Identifiable#getId()}. This method then
	 * performs a get for the given object type and ID at
	 * the KaroAPI. When the request is finished, the content of the newly loaded object will be
	 * copied to the original object to updated it.<br>
	 * <br>
	 * Currently supported types are:
	 * <ul>
	 * <li>{@link User}</li>
	 * <li>{@link Game}</li>
	 * <li>{@link Map}</li>
	 * </ul>
	 * Example:
	 * <ol>
	 * <li>you load a user from the backend, say user {"id":9999, "login":"foo","activeGames":10,
	 * ...}</li>
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
	 * <li>you can continue using the original object where it is already in use (no need to replace
	 * it in arrays, lists or references)</li>
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

		return loader.whenComplete((result, th) -> {
			ReflectionsUtil.copyFields(result, object, false);
		});
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
			if(User.class.equals(cls))
				return (T) getUser(id).get();
			else if(Game.class.equals(cls))
				return (T) getGameWithDetails(id).get();
			else if(Map.class.equals(cls))
				return (T) getMapWithDetails(id).get();
			else
				logger.error("unsupported lookup type: " + cls.getName());
		}
		catch(ExecutionException | InterruptedException e)
		{
			logger.error("could not look up " + cls.getName() + " with id " + id, e);
		}
		return null;
	}

	///////////////////////
	// ADDITIONAL PROPERTIES
	///////////////////////

	public static String getStringProperty(String key)
	{
		return apiProperties.getProperty(key);
	}

	public static String getStringProperty(String key, String defaultValue)
	{
		return apiProperties.getProperty(key, defaultValue);
	}

	public static int getIntProperty(String key)
	{
		return getIntProperty(key, 0);
	}

	public static int getIntProperty(String key, int defaultValue)
	{
		try
		{
			return Integer.parseInt(getStringProperty(key));
		}
		catch(NumberFormatException | NullPointerException e)
		{
			logger.warn("exception getting property '" + key + "'", e);
			return defaultValue;
		}
	}

	public static double getDoubleProperty(String key)
	{
		return getDoubleProperty(key, 0.0);
	}

	public static double getDoubleProperty(String key, double defaultValue)
	{
		try
		{
			return Double.parseDouble(getStringProperty(key));
		}
		catch(NumberFormatException | NullPointerException e)
		{
			logger.warn("exception getting property '" + key + "'", e);
			return defaultValue;
		}
	}

	public static boolean getBooleanProperty(String key)
	{
		return Boolean.parseBoolean(getStringProperty(key));
	}
}
