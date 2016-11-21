package ultimate.karoapi4j.wiki;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.KaroWikiURLs;
import ultimate.karoapi4j.utils.JSONUtil;
import ultimate.karoapi4j.utils.URLLoaderUtil;
import ultimate.karoapi4j.utils.web.SimpleCookieHandler;

public class KaroWikiLoader
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger				logger						= LoggerFactory.getLogger(KaroWikiLoader.class);

	public KaroWikiLoader()
	{
		if(CookieHandler.getDefault() == null)
			CookieHandler.setDefault(new SimpleCookieHandler());
	}

	@SuppressWarnings("unchecked")
	public boolean login(String username, String password) throws IOException
	{
		Map<String, String> parameters;
		String json;
		Map<String, Object> jsonObject;

		logger.debug("Obtaining token...");
		parameters = new HashMap<String, String>();
		parameters.put(KaroWikiURLs.PARAMETER_ACTION, KaroWikiURLs.ACTION_LOGIN);
		parameters.put(KaroWikiURLs.PARAMETER_ACTION_LOGIN_USER, username);
		parameters.put(KaroWikiURLs.PARAMETER_FORMAT, KaroWikiURLs.FORMAT_JSON);
		
		json = URLLoaderUtil.load(new URL(KaroWikiURLs.API_BASE), URLLoaderUtil.formatParameters(parameters));
//		System.out.println(json);
		jsonObject = (Map<String, Object>) JSONUtil.deserialize(json);	
		
		String token = (String) ((Map<String, Object>)jsonObject.get("login")).get("token");
		
		logger.debug("Performing login: \"" + username + "\"...");

		parameters = new HashMap<String, String>();
		parameters.put(KaroWikiURLs.PARAMETER_ACTION, KaroWikiURLs.ACTION_LOGIN);
		parameters.put(KaroWikiURLs.PARAMETER_ACTION_LOGIN_USER, username);
		parameters.put(KaroWikiURLs.PARAMETER_ACTION_LOGIN_PASSWORD, password);
		parameters.put(KaroWikiURLs.PARAMETER_ACTION_LOGIN_TOKEN, token);
		parameters.put(KaroWikiURLs.PARAMETER_FORMAT, KaroWikiURLs.FORMAT_JSON);

		json = URLLoaderUtil.load(new URL(KaroWikiURLs.API_BASE), URLLoaderUtil.formatParameters(parameters));
//		System.out.println(json);
		jsonObject = (Map<String, Object>) JSONUtil.deserialize(json);	
		String result = (String) ((Map<String, Object>)jsonObject.get("login")).get("result");
		String resultUser = (String) ((Map<String, Object>)jsonObject.get("login")).get("lgusername");
		
		boolean success = "success".equalsIgnoreCase(result) && username.equalsIgnoreCase(resultUser);

		logger.debug("  " + (success ? "Successful!" : "Failed!"));

		return success;
	}
}
