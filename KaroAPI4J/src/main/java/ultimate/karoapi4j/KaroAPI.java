package ultimate.karoapi4j;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import ultimate.karoapi4j.enums.EnumUserGamesort;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.JSONUtil;
import ultimate.karoapi4j.utils.web.Parser;
import ultimate.karoapi4j.utils.web.URLLoader;
import ultimate.karoapi4j.utils.web.URLLoader.BackgroundLoader;

/**
 * This is the wrapper for accessing the Karo API.<br>
 * <br>
 * Note: Accessing the API requires a user and password for www.karopapier.de which can be passed with the constructor. Afterwards it is
 * recommended to check the successful login using {@link KaroAPI#check()}.<br>
 * <br>
 * Each API call will return a {@link BackgroundLoader} which can then be used to either load the results via
 * {@link BackgroundLoader#doAsync(java.util.function.Consumer)} or {@link BackgroundLoader#doBlocking()}.<br>
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
public class KaroAPI
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger	logger					= LoggerFactory.getLogger(getClass());

	// config & constants
	static final String					PLACEHOLDER				= "$";
	static final BufferedImage			DEFAULT_IMAGE_WHITE;
	static final BufferedImage			DEFAULT_IMAGE_GRAY;
	static final BufferedImage			DEFAULT_IMAGE_BLACK;
	static final int					DEFAULT_IMAGE_SIZE		= 100;
	static final int					DEFAULT_IMAGE_WIDTH		= DEFAULT_IMAGE_SIZE;
	static final int					DEFAULT_IMAGE_HEIGHT	= DEFAULT_IMAGE_SIZE / 2;

	static
	{
		DEFAULT_IMAGE_WHITE = new BufferedImage(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2dw = DEFAULT_IMAGE_WHITE.createGraphics();
		g2dw.setColor(Color.white);
		g2dw.fillRect(0, 0, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);

		DEFAULT_IMAGE_GRAY = new BufferedImage(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2dg = DEFAULT_IMAGE_GRAY.createGraphics();
		g2dg.setColor(Color.gray);
		g2dg.fillRect(0, 0, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);

		DEFAULT_IMAGE_BLACK = new BufferedImage(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2db = DEFAULT_IMAGE_BLACK.createGraphics();
		g2db.setColor(Color.black);
		g2db.fillRect(0, 0, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
	}

	// api URLs
	private static final URLLoader					KAROPAPIER			= new URLLoader("https://www.karopapier.de");
	private static final URLLoader					API					= KAROPAPIER.relative("/api");
	// users
	private static final URLLoader					USERS				= API.relative("/users");
	private static final URLLoader					USER				= USERS.relative("/" + PLACEHOLDER);
	private static final URLLoader					USER_DRAN			= USER.relative("/dran");
	private static final URLLoader					USER_BLOCKERS		= USER.relative("/blockers");
	// current user
	private static final URLLoader					CURRENT_USER		= API.relative("/user");
	private static final URLLoader					CHECK				= CURRENT_USER.relative("/check");
	private static final URLLoader					FAVS				= CURRENT_USER.relative("/favs");
	private static final URLLoader					BLOCKERS			= API.relative("/blockers");
	private static final URLLoader					NOTES				= API.relative("/notes");
	private static final URLLoader					PLANNED_MOVES		= API.relative("/planned-moves");
	// games
	private static final URLLoader					GAMES				= API.relative("/games");
	private static final URLLoader					GAMES3				= API.relative("/games3");
	private static final URLLoader					GAME				= GAMES.relative("/" + PLACEHOLDER);
	// maps
	private static final URLLoader					MAPS				= API.relative("/maps");
	private static final URLLoader					MAP					= MAPS.relative("/" + PLACEHOLDER);
	// mapimages
	// do not use API as the base here, since we do not need the authentication here
	private static final URLLoader					MAP_IMAGE			= KAROPAPIER.relative("/map/" + PLACEHOLDER + ".png");
	// chat
	private static final URLLoader					CHAT				= API.relative("/chat");
	private static final URLLoader					CHAT_LAST			= CHAT.relative("/last");
	private static final URLLoader					CHAT_USERS			= CHAT.relative("/users");
	// messaging
	private static final URLLoader					CONTACTS			= API.relative("/contacts");
	private static final URLLoader					MESSAGES			= API.relative("/messages/" + PLACEHOLDER);

	// parsers needed
	private static final Parser<String, String>		PARSER_RAW			= (result) -> { return result; };
	private static final Parser<String, User>		PARSER_USER			= (result) -> { return JSONUtil.deserialize(result, new TypeReference<User>() {}); };
	private static final Parser<String, List<User>>	PARSER_USER_LIST	= (result) -> { return JSONUtil.deserialize(result, new TypeReference<List<User>>() {}); };
	private static final Parser<String, Game>		PARSER_GAME			= (result) -> { return JSONUtil.deserialize(result, new TypeReference<Game>() {}); };
	private static final Parser<String, List<Game>>	PARSER_GAME_LIST	= (result) -> { return JSONUtil.deserialize(result, new TypeReference<List<Game>>() {}); };
	private static final Parser<String, Map>		PARSER_MAP			= (result) -> { return JSONUtil.deserialize(result, new TypeReference<Map>() {}); };
	private static final Parser<String, List<Map>>	PARSER_MAP_LIST		= (result) -> { return JSONUtil.deserialize(result, new TypeReference<List<Map>>() {}); };
	private static final Parser<String, ?>			PARSER_CHAT			= (result) -> { return JSONUtil.deserialize(result, new TypeReference<Object>() {}); };			// TODO
	private static final Parser<String, List<?>>	PARSER_CHAT_LIST	= (result) -> { return JSONUtil.deserialize(result, new TypeReference<List<Object>>() {}); };	// TODO
	private static final Parser<String, List<?>>	PARSER_MESSAGE_LIST	= (result) -> { return JSONUtil.deserialize(result, new TypeReference<List<Object>>() {}); };	// TODO

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
		API.addRequestProperty("X-Auth-Login", user);
		API.addRequestProperty("X-Auth-Password", password);
	}

	// TODO add load balancing via ThreadQueue

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
	public BackgroundLoader<User> check()
	{
		return CHECK.doGet(PARSER_USER);
	}

	/**
	 * Get all users
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#USERS
	 * @return the list of all users
	 */
	public BackgroundLoader<List<User>> getUsers()
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
	public BackgroundLoader<List<User>> getUsers(String login, Boolean invitable, Boolean desperate)
	{
		HashMap<String, String> args = new HashMap<>();
		if(login != null)
			args.put("login", login);
		if(invitable != null)
			args.put("invitable", invitable ? "1" : "0");
		if(desperate != null)
			args.put("desperate", desperate ? "1" : "0");

		return USERS.doGet(args, PARSER_USER_LIST);
	}

	/**
	 * Get a user by id
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#USER
	 * @param userId - the user id
	 * @return the user
	 */
	public BackgroundLoader<User> getUser(int userId)
	{
		return USER.replace(PLACEHOLDER, userId).doGet(PARSER_USER);
	}

	/**
	 * Get the list of games where the given user is next.<br>
	 * 
	 * @see KaroAPI#USER_DRAN
	 * 
	 * @param userId - the user id
	 * @return the list of games
	 */
	public BackgroundLoader<List<Game>> getUserDran(int userId)
	{
		return USER_DRAN.replace(PLACEHOLDER, userId).doGet(PARSER_GAME_LIST);
	}

	/**
	 * Get the list blockers.<br>
	 * 
	 * @see KaroAPI#BLOCKERS
	 * 
	 * @return the list of users
	 */
	public BackgroundLoader<List<User>> getBlockers()
	{
		return BLOCKERS.doGet(PARSER_USER_LIST);
	}

	/**
	 * Get the list blockers for a specific user.<br>
	 * 
	 * @see KaroAPI#USER_BLOCKERS
	 * 
	 * @return the list of users
	 */
	public BackgroundLoader<List<User>> getUserBlockers(int userId)
	{
		return USER_BLOCKERS.replace(PLACEHOLDER, userId).doGet(PARSER_USER_LIST);
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
	public BackgroundLoader<List<Game>> getGames()
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
	public BackgroundLoader<List<Game>> getGames(Boolean mine, EnumUserGamesort sort, Integer user, Boolean finished, String name, Integer nameStart, Integer limit, Integer offset)
	{
		HashMap<String, String> args = new HashMap<>();
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

		return GAMES3.doGet(args, PARSER_GAME_LIST);
	}

	/**
	 * Get a game by id
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#GAME
	 * @param gameId - the game id
	 * @return the game
	 */
	public BackgroundLoader<Game> getGame(int gameId)
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
	public BackgroundLoader<Game> getGame(int gameId, Boolean mapcode, Boolean players, Boolean moves)
	{
		HashMap<String, String> args = new HashMap<>();
		if(mapcode != null)
			args.put("mapcode", (mapcode ? "1" : "0"));
		if(players != null)
			args.put("players", (players ? "1" : "0"));
		if(moves != null)
			args.put("moves", (moves ? "1" : "0"));

		return GAME.replace(PLACEHOLDER, gameId).doGet(args, PARSER_GAME);
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
	public BackgroundLoader<List<Map>> getMaps()
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
	public BackgroundLoader<List<Map>> getMaps(Boolean mapcode)
	{
		HashMap<String, String> args = new HashMap<>();
		if(mapcode != null)
			args.put("mapcode", (mapcode ? "1" : "0"));

		return MAPS.doGet(args, PARSER_MAP_LIST);
	}

	/**
	 * Get a map by id
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#MAP
	 * @param mapId - the map id
	 * @return the map
	 */
	public BackgroundLoader<Map> getMap(int mapId)
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
	public BackgroundLoader<Map> getMap(int mapId, Boolean mapcode)
	{
		HashMap<String, String> args = new HashMap<>();
		if(mapcode != null)
			args.put("mapcode", (mapcode ? "1" : "0"));

		return MAP.replace(PLACEHOLDER, mapId).doGet(args, PARSER_MAP);
	}

	/**
	 * Get a map code by id
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#MAP
	 * @param mapId - the map id
	 * @return the map code
	 */
	public BackgroundLoader<String> getMapCode(int mapId)
	{
		return MAP.replace(PLACEHOLDER, mapId + ".txt").doGet(PARSER_RAW);
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
		HashMap<String, String> args = new HashMap<>();
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
		HashMap<String, String> args = new HashMap<>();
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
		HashMap<String, String> args = new HashMap<>();
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
	protected BufferedImage getMapImage(int mapId, HashMap<String, String> args)
	{
		try
		{
			// use the doGet mechanism to generate the URL and query the true image location
			BackgroundLoader<String> tmp = MAP_IMAGE.replace(PLACEHOLDER, mapId).doGet(args, PARSER_RAW);
			logger.debug("loading image information for map " + mapId + ": " + tmp.getUrl());
			return ImageIO.read(new URL(tmp.getUrl()));
		}
		catch(Exception e)
		{
			logger.error("unexpected error", e);
			e.printStackTrace();
			// DO NOT use queue here
			BufferedImage image = DEFAULT_IMAGE_GRAY;
			try
			{
				boolean night = getMap(mapId).doBlocking().isNight();
				image = (night ? DEFAULT_IMAGE_BLACK : DEFAULT_IMAGE_WHITE);
			}
			catch(InterruptedException e1)
			{
				logger.error("map does not seem to exist", e1);
				e1.printStackTrace();
			}
			return createSpecialImage(image);
		}
	}

	public <T> BackgroundLoader<T> refresh(T object)
	{
		// TODO
		return null;
	}

	// helper methods

	/**
	 * Create a special image by drawing a "don't sign" on top of the given image...
	 * 
	 * @param image - the original image
	 * @return the specialized image
	 */
	private static BufferedImage createSpecialImage(Image image)
	{
		BufferedImage image2 = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2d = image2.createGraphics();
		g2d.drawImage(image, 0, 0, null);
		g2d.setColor(Color.red);
		int size = (int) Math.min(image2.getWidth() * 0.7F, image2.getHeight() * 0.7F);
		g2d.setStroke(new BasicStroke(size / 7));
		g2d.drawOval((image2.getWidth() - size) / 2, (image2.getHeight() - size) / 2, size, size);
		int delta = (int) (size / 2 * 0.707F);
		g2d.drawLine(image2.getWidth() / 2 - delta, image2.getHeight() / 2 + delta, image2.getWidth() / 2 + delta, image2.getHeight() / 2 - delta);
		return image2;
	}
}
