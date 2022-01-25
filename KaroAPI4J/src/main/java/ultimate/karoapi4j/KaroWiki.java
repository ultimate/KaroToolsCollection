package ultimate.karoapi4j;

import java.net.CookieHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.enums.EnumContentType;
import ultimate.karoapi4j.utils.JSONUtil;
import ultimate.karoapi4j.utils.web.Parser;
import ultimate.karoapi4j.utils.web.SimpleCookieHandler;
import ultimate.karoapi4j.utils.web.URLLoader;

public class KaroWiki
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger							logger								= LoggerFactory.getLogger(KaroWiki.class);

	// api URLs
	private static final URLLoader								KAROWIKI							= new URLLoader("https://wiki.karopapier.de");
	private static final URLLoader								API									= KAROWIKI.relative("/api.php");

	// parameters & actions
	public static final String									PARAMETER_ACTION					= "action";
	public static final String									PARAMETER_FORMAT					= "format";

	public static final String									ACTION_LOGIN						= "login";
	public static final String									PARAMETER_ACTION_LOGIN_USER			= "lgname";
	public static final String									PARAMETER_ACTION_LOGIN_PASSWORD		= "lgpassword";
	public static final String									PARAMETER_ACTION_LOGIN_TOKEN		= "lgtoken";

	public static final String									ACTION_LOGOUT						= "logout";
	public static final String									PARAMETER_ACTION_LOGOUT_TOKEN		= "token";

	public static final String									ACTION_QUERY						= "query";
	public static final String									PARAMETER_ACTION_QUERY_META			= "meta";
	public static final String									PARAMETER_ACTION_QUERY_META_TOKENS	= "tokens";
	public static final String									PARAMETER_ACTION_QUERY_PROP			= "prop";
	public static final String									PARAMETER_ACTION_QUERY_PROP_RV		= "revisions";
	public static final String									PARAMETER_ACTION_QUERY_PROP_IN		= "info";
	public static final String									PARAMETER_ACTION_QUERY_TITLES		= "titles";
	public static final String									PARAMETER_ACTION_QUERY_RVPROP		= "rvprop";
	public static final String									PARAMETER_ACTION_QUERY_INPROP		= "inprop";
	public static final String									PARAMETER_ACTION_QUERY_INTOKEN		= "intoken";

	public static final String									ACTION_EDIT							= "edit";
	public static final String									PARAMETER_ACTION_EDIT_TITLE			= "title";
	public static final String									PARAMETER_ACTION_EDIT_TEXT			= "text";
	public static final String									PARAMETER_ACTION_EDIT_TOKEN			= "token";
	public static final String									PARAMETER_ACTION_EDIT_SUMMARY		= "summary";
	public static final String									PARAMETER_ACTION_EDIT_BASETIMESTAMP	= "basetimestamp";
	public static final String									PARAMETER_ACTION_EDIT_CAPTCHAID		= "captchaid";
	public static final String									PARAMETER_ACTION_EDIT_CAPTCHAWORD	= "captchaword";
	public static final String									PARAMETER_ACTION_EDIT_BOT			= "bot";

	public static final String									FORMAT_JSON							= "json";

	private static final Parser<String, String>					PARSER_RAW							= (result) -> { return result; };
	@SuppressWarnings("unchecked")
	private static final Parser<String, Map<String, Object>>	PARSER_JSON_OBJECT					= (result) -> { return (Map<String, Object>) JSONUtil.deserialize(result); };

	public KaroWiki()
	{
		if(CookieHandler.getDefault() == null)
			CookieHandler.setDefault(new SimpleCookieHandler());
	}

	@SuppressWarnings("unchecked")
	public boolean login(String username, String password) throws InterruptedException
	{
		Map<String, String> parameters;
		Map<String, Object> jsonObject;

		logger.debug("Performing login: \"" + username + "\"...");

		logger.debug("  Obtaining token...");
		parameters = new HashMap<String, String>();
		parameters.put(PARAMETER_ACTION, ACTION_LOGIN);
		parameters.put(PARAMETER_FORMAT, FORMAT_JSON);
		parameters.put(PARAMETER_ACTION_LOGIN_USER, username);

		jsonObject = API.doPost(parameters, EnumContentType.text, PARSER_JSON_OBJECT).doBlocking();

		String token = (String) ((Map<String, Object>) jsonObject.get("login")).get("token");

		logger.debug("  Performing login...");

		parameters = new HashMap<String, String>();
		parameters.put(PARAMETER_ACTION, ACTION_LOGIN);
		parameters.put(PARAMETER_FORMAT, FORMAT_JSON);
		parameters.put(PARAMETER_ACTION_LOGIN_USER, username);
		parameters.put(PARAMETER_ACTION_LOGIN_PASSWORD, password);
		parameters.put(PARAMETER_ACTION_LOGIN_TOKEN, token);

		jsonObject = API.doPost(parameters, EnumContentType.text, PARSER_JSON_OBJECT).doBlocking();

		String result = (String) ((Map<String, Object>) jsonObject.get("login")).get("result");
		String resultUser = (String) ((Map<String, Object>) jsonObject.get("login")).get("lgusername");

		boolean success = "success".equalsIgnoreCase(result) && username.equalsIgnoreCase(resultUser);

		logger.debug("  " + (success ? "Successful!" : "Failed!"));

		return success;
	}

	@SuppressWarnings("unchecked")
	public boolean logout() throws InterruptedException
	{
		Map<String, String> parameters;
		Map<String, Object> jsonObject;
		String json;

		logger.debug("Performing logout...");

		logger.debug("  Obtaining token...");
		parameters = new HashMap<String, String>();
		parameters.put(PARAMETER_ACTION, ACTION_QUERY);
		parameters.put(PARAMETER_ACTION_QUERY_META, PARAMETER_ACTION_QUERY_META_TOKENS);
		parameters.put(PARAMETER_FORMAT, FORMAT_JSON);

		jsonObject = API.doPost(parameters, EnumContentType.text, PARSER_JSON_OBJECT).doBlocking();

		logger.debug("  " + jsonObject);
		String token = (String) ((Map<String, Object>) ((Map<String, Object>) jsonObject.get("query")).get("tokens")).get("csrftoken");

		logger.debug("  Performing logout...");
		parameters = new HashMap<String, String>();
		parameters.put(PARAMETER_ACTION, ACTION_LOGOUT);
		parameters.put(PARAMETER_FORMAT, FORMAT_JSON);
		parameters.put(PARAMETER_ACTION_LOGOUT_TOKEN, token);

		json = API.doPost(parameters, EnumContentType.text, PARSER_RAW).doBlocking();

		boolean success = "{}".equalsIgnoreCase(json);

		logger.debug(json);
		logger.debug("  " + (success ? "Successful!" : "Failed!"));

		return success;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> query(String title, String prop, String propParam, String... propertiesList) throws InterruptedException
	{
		Map<String, String> parameters;
		Map<String, Object> jsonObject;

		StringBuffer properties = new StringBuffer();
		for(String s : propertiesList)
		{
			if(properties.length() > 0)
				properties.append("|");
			properties.append(s);
		}

		logger.debug("Performing prop=" + prop + " for page \"" + title + "\"...");
		parameters = new HashMap<String, String>();
		parameters.put(PARAMETER_ACTION, ACTION_QUERY);
		parameters.put(PARAMETER_FORMAT, FORMAT_JSON);
		parameters.put(PARAMETER_ACTION_QUERY_PROP, prop);
		parameters.put(PARAMETER_ACTION_QUERY_TITLES, title);
		parameters.put(propParam, properties.toString());

		jsonObject = API.doPost(parameters, EnumContentType.text, PARSER_JSON_OBJECT).doBlocking();

		Map<String, Object> pages = (Map<String, Object>) ((Map<String, Object>) jsonObject.get("query")).get("pages");

		if(pages.size() != 1)
		{
			logger.debug("  Wrong number of results!");
			return null;
		}
		else if(pages.containsKey("-1"))
		{
			logger.debug("  Page not existing");
			return (Map<String, Object>) pages.get("-1");
		}
		else
		{
			String id = pages.keySet().iterator().next();
			logger.debug("  Page existing with id " + id);
			return (Map<String, Object>) pages.get(id);
		}
	}

	public Map<String, Object> queryRevisionProperties(String title, String... propertiesList) throws InterruptedException
	{
		return query(title, PARAMETER_ACTION_QUERY_PROP_RV, PARAMETER_ACTION_QUERY_RVPROP, propertiesList);
	}

	public Map<String, Object> queryInfoProperties(String title, String... propertiesList) throws InterruptedException
	{
		return query(title, PARAMETER_ACTION_QUERY_PROP_IN, PARAMETER_ACTION_QUERY_INPROP, propertiesList);
	}

	@SuppressWarnings("unchecked")
	public boolean edit(String title, String content, String summary, boolean ignoreConflicts, boolean bot) throws InterruptedException
	{
		Map<String, String> parameters;
		Map<String, Object> jsonObject;
		boolean success;

		logger.debug("Performing edit of page \"" + title + "\"...");

		logger.debug("  Obtaining token...");
		String token = getToken(title, "edit");

		logger.debug("  Performing edit...");
		parameters = new HashMap<String, String>();
		parameters.put(PARAMETER_ACTION, ACTION_EDIT);
		parameters.put(PARAMETER_FORMAT, FORMAT_JSON);
		parameters.put(PARAMETER_ACTION_EDIT_TITLE, title);
		parameters.put(PARAMETER_ACTION_EDIT_TEXT, content);
		if(summary != null)
			parameters.put(PARAMETER_ACTION_EDIT_SUMMARY, summary);
		if(bot)
			parameters.put(PARAMETER_ACTION_EDIT_BOT, "true");
		if(!ignoreConflicts)
		{
			String baseTimestamp = getTimestamp(title);
			parameters.put(PARAMETER_ACTION_EDIT_BASETIMESTAMP, baseTimestamp);
		}
		parameters.put(PARAMETER_ACTION_EDIT_TOKEN, token);

		jsonObject = API.doPost(parameters, EnumContentType.text, PARSER_JSON_OBJECT).doBlocking();

		String result = (String) ((Map<String, Object>) jsonObject.get("edit")).get("result");
		success = "success".equalsIgnoreCase(result);
		int tries = 0;
		while(!success && tries < 5)
		{
			// handle captcha
			Map<String, Object> captcha = (Map<String, Object>) ((Map<String, Object>) jsonObject.get("edit")).get("captcha");
			if(captcha != null)
			{
				String question = (String) captcha.get("question");
				String id = (String) captcha.get("id");
				String answer = getCaptchaAnswer(question);
				parameters.put(PARAMETER_ACTION_EDIT_CAPTCHAID, id);
				parameters.put(PARAMETER_ACTION_EDIT_CAPTCHAWORD, answer);

				// try again
				jsonObject = API.doPost(parameters, EnumContentType.text, PARSER_JSON_OBJECT).doBlocking();

				result = (String) ((Map<String, Object>) jsonObject.get("edit")).get("result");
				success = "success".equalsIgnoreCase(result);

				tries++;
			}
		}
		logger.debug("  " + (success ? "Successful!" : "Failed!"));
		return success;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getContent(String title) throws InterruptedException
	{
		Map<String, Object> properties = queryRevisionProperties(title, "content");
		if(properties.containsKey("missing"))
			return null;

		List<?> revisions = (List) properties.get("revisions");
		return (String) ((Map<String, Object>) revisions.get(revisions.size() - 1)).get("*");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getTimestamp(String title) throws InterruptedException
	{
		Map<String, Object> properties = queryRevisionProperties(title, "timestamp");
		if(properties.containsKey("missing"))
			return null;

		List<?> revisions = (List) properties.get("revisions");
		return (String) ((Map<String, Object>) revisions.get(revisions.size() - 1)).get("timestamp");
	}

	public String getToken(String title, String action) throws InterruptedException
	{
		Map<String, Object> properties = query(title, PARAMETER_ACTION_QUERY_PROP_IN, PARAMETER_ACTION_QUERY_INTOKEN, action);
		return (String) properties.get(action + "token");
	}

	private String getCaptchaAnswer(String question)
	{
		if("Was steht im Forum hinter uralten Threads?".equals(question))
			return "saualt";
		if("Wer is hier an allem Schuld? (wer hat's programmiert?)".equals(question))
			return "Didi";
		if("Wie heisst der Bot (weiblich), der staendig im Chat rumlabert?".equals(question))
			return "Botrix";
		if("Was erscheint im Chat2.0 fuer ein Bildchen vor den spielegeilen?".equals(question))
			return "Spiegelei";
		return "";
	}
}
