package ultimate.karoapi4j;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

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
 * @author ultimate
 */
public class KaroAPI
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger				logger				= LoggerFactory.getLogger(getClass());

	private final URLLoader							KAROPAPIER			= new URLLoader("https://www.karopapier.de");

	private final URLLoader							API					= KAROPAPIER.relative("/api");

	private final URLLoader							USERS				= API.relative("/users");

	private final URLLoader							USER				= API.relative("/user");

	private final URLLoader							CHECK				= USER.relative("/check");

	public static final Parser<String, String>		PARSER_RAW			= (result) -> { return result; };
	
	public static final Parser<String, User>		PARSER_USER			= (result) -> { return JSONUtil.deserialize(result, new TypeReference<User>() {}); };
	
	public static final Parser<String, List<User>>	PARSER_USER_LIST	= (result) -> { return JSONUtil.deserialize(result, new TypeReference<List<User>>() {}); };

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

	public BackgroundLoader<User> check()
	{
		return CHECK.doGet(PARSER_USER);
	}

	public BackgroundLoader<List<User>> getUsers()
	{
		return USERS.doGet(PARSER_USER_LIST);
	}
}
