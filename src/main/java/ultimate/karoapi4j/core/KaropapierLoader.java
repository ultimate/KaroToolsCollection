package ultimate.karoapi4j.core;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.KaroURLs;
import ultimate.karoapi4j.utils.URLLoaderUtil;
import ultimate.karoapi4j.utils.web.SimpleCookieHandler;

public class KaropapierLoader
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger	logger	= LoggerFactory.getLogger(URLLoaderUtil.class);
	
	/**
	 * The String the login page contains if login was successful
	 */
	public static final String CONTENT_LOGIN_SUCCESSFUL = "Login erfolgreich";

	public KaropapierLoader()
	{
		CookieHandler.setDefault(new SimpleCookieHandler());
	}

	public boolean login(String username, String password) throws IOException
	{
		logger.debug("Performing login: \"" + username + "\"...");
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(KaroURLs.PARAMETER_USERNAME, username);
		parameters.put(KaroURLs.PARAMETER_PASSWORD, password);
		
		String page = URLLoaderUtil.readURL(new URL(KaroURLs.USER_LOGIN), URLLoaderUtil.formatParameters(parameters));

		boolean success = page.contains(CONTENT_LOGIN_SUCCESSFUL) && page.contains(username);

		logger.debug("  " + (success ? "Successful!" : "Failed!"));
		
		return success;
	}
}
