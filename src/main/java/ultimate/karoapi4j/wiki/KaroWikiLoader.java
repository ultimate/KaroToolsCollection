package ultimate.karoapi4j.wiki;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import ultimate.karoapi4j.KaroURLs;
import ultimate.karoapi4j.KaroWikiURLs;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.URLLoaderUtil;
import ultimate.karoapi4j.utils.web.SimpleCookieHandler;

public class KaroWikiLoader
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger				logger						= LoggerFactory.getLogger(URLLoaderUtil.class);

	/**
	 * The String the login page contains if login was successful
	 */
	public static final String						CONTENT_LOGIN_SUCCESSFUL	= "Login erfolgreich";

	/**
	 * TypeReference for List&lt;Game&gt;
	 */
	public static final TypeReference<List<Game>>	TYPEREF_GAME_LIST			= new TypeReference<List<Game>>() {};

	public KaroWikiLoader()
	{
		if(CookieHandler.getDefault() == null)
			CookieHandler.setDefault(new SimpleCookieHandler());
	}

	public boolean login(String username, String password) throws IOException
	{
		logger.debug("Performing login: \"" + username + "\"...");

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(KaroWikiURLs.PARAMETER_ACTION, KaroWikiURLs.ACTION_LOGIN);
		parameters.put(KaroWikiURLs.PARAMETER_ACTION_LOGIN_USER, username);
		parameters.put(KaroWikiURLs.PARAMETER_ACTION_LOGIN_PASSWORD, password);

		String page = URLLoaderUtil.load(new URL(KaroURLs.USER_LOGIN), URLLoaderUtil.formatParameters(parameters));
		
		System.out.println(page);

		boolean success = page.contains(CONTENT_LOGIN_SUCCESSFUL) && page.contains(username);

		logger.debug("  " + (success ? "Successful!" : "Failed!"));

		return success;
	}

	public List<Game> getGamesByUser(User user) throws IOException
	{
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(KaroURLs.PARAMETER_USER, "" + user.getId());
		parameters.put(KaroURLs.PARAMETER_LIMIT, "" + KaroURLs.PARAMETER_LIMIT_UNLIMITED);

		List<Game> gamesActive = URLLoaderUtil.load(new URL(KaroURLs.GAME_LIST), URLLoaderUtil.formatParameters(parameters), TYPEREF_GAME_LIST);

		parameters.put(KaroURLs.PARAMETER_FINISHED, "true");

		List<Game> gamesFinished = URLLoaderUtil.load(new URL(KaroURLs.GAME_LIST), URLLoaderUtil.formatParameters(parameters), TYPEREF_GAME_LIST);

		List<Game> games = new ArrayList<Game>(gamesActive.size() + gamesFinished.size());
		games.addAll(gamesActive);
		games.addAll(gamesFinished);
		
//		Collections.sort(games, c); // TODO sort by id
		
		return games;
	}

	public void findIds(List<Game> games)
	{
		for(Game g : games)
		{

		}
	}
}
