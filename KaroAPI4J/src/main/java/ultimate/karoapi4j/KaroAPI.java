package ultimate.karoapi4j;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.JSONUtil;
import ultimate.karoapi4j.utils.web.Parser;
import ultimate.karoapi4j.utils.web.URLLoader;
import ultimate.karoapi4j.utils.web.URLLoader.BackgroundLoader;

/**
 * This is the wrapper for accessing the Karo API.<br>
 * Accessing the API requires a user and password for www.karopapier.de which can be passed with the constructor. Afterwards it is
 * recommended to check the successful login using {@link KaroAPI#check()}
 *
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public class KaroAPI
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger				logger				= LoggerFactory.getLogger(getClass());

	private static final String						PLACEHOLDER			= "$";

	// api URLs
	private static final URLLoader					KAROPAPIER			= new URLLoader("https://www.karopapier.de");
	private static final URLLoader					API					= KAROPAPIER.relative("/api");

	private static final URLLoader					USERS				= API.relative("/users");
	private static final URLLoader					USER				= USERS.relative("/" + PLACEHOLDER);
	private static final URLLoader					USER_DRAN			= USER.relative("/dran");
	private static final URLLoader					USER_BLOCKERS		= USER.relative("/blockers");

	private static final URLLoader					CURRENT_USER		= API.relative("/user");
	private static final URLLoader					CHECK				= CURRENT_USER.relative("/check");
	private static final URLLoader					FAVS				= CURRENT_USER.relative("/favs");
	private static final URLLoader					BLOCKERS			= API.relative("/blockers");
	private static final URLLoader					NOTES				= API.relative("/notes");
	private static final URLLoader					PLANNED_MOVES		= API.relative("/planned-moves");

	private static final URLLoader					GAMES				= API.relative("/games");
	private static final URLLoader					GAME				= GAMES.relative("/" + PLACEHOLDER);

	private static final URLLoader					MAPS				= API.relative("/maps");
	private static final URLLoader					MAP					= MAPS.relative("/" + PLACEHOLDER);

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
		KAROPAPIER.addRequestProperty("X-Auth-Login", user);
		KAROPAPIER.addRequestProperty("X-Auth-Password", password);
	}

	// TODO add load balancing via ThreadQueue

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
	 * Get a user by id
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @see KaroAPI#USER
	 * @return the currently logged in user
	 */
	public BackgroundLoader<User> getUser(int id)
	{
		return USER.replace(PLACEHOLDER, "" + id).doGet(PARSER_USER);
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
	 * Each filter is applied only if it is set (not null). If the filter is null, it will be ignored.<br>
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
	 * @see KaroAPI#USERS
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
}
