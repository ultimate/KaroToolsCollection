package ultimate.karoapi4j;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import ultimate.karoapi4j.enums.EnumContentType;
import ultimate.karoapi4j.utils.JSONUtil;
import ultimate.karoapi4j.utils.URLLoader;

// TODO javadoc
// note: blocking in main Thread!!!
public class KaroWikiAPI
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger							logger								= LoggerFactory.getLogger(KaroWikiAPI.class);

	// api URLs
	protected static final URLLoader							KAROWIKI							= new URLLoader("https://wiki.karopapier.de");
	protected static final URLLoader							API									= KAROWIKI.relative("/api.php");

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

	public static final Function<String, String>				PARSER_RAW							= Function.identity();
	public static final Function<String, Map<String, Object>>	PARSER_JSON_OBJECT					= new JSONUtil.Parser<>(new TypeReference<Map<String, Object>>() {});

	public KaroWikiAPI()
	{
		if(CookieHandler.getDefault() == null)
			CookieHandler.setDefault(new CookieManager());
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<Boolean> login(String username, String password)
	{
		logger.debug("Performing login: \"" + username + "\"...");
		logger.debug("  Obtaining token...");

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(PARAMETER_ACTION, ACTION_LOGIN);
		parameters.put(PARAMETER_FORMAT, FORMAT_JSON);
		parameters.put(PARAMETER_ACTION_LOGIN_USER, username);

		//@formatter:off
		return CompletableFuture.supplyAsync(API.doPost(parameters, EnumContentType.text))
				.thenApply(PARSER_JSON_OBJECT)
				.thenCompose(jsonObject -> {

					String token = (String) ((Map<String, Object>) jsonObject.get("login")).get("token");
		
					logger.debug("  Performing login...");
		
					parameters.put(PARAMETER_ACTION_LOGIN_PASSWORD, password);
					parameters.put(PARAMETER_ACTION_LOGIN_TOKEN, token);
		
					return CompletableFuture.supplyAsync(API.doPost(parameters, EnumContentType.text));
					
				})
				.thenApply(PARSER_JSON_OBJECT)
				.thenApply(jsonObject -> {
		
					String result = (String) ((Map<String, Object>) jsonObject.get("login")).get("result");
					String resultUser = (String) ((Map<String, Object>) jsonObject.get("login")).get("lgusername");
		
					boolean success = "success".equalsIgnoreCase(result) && username.equalsIgnoreCase(resultUser);
					logger.debug("  " + (success ? "Successful!" : "Failed"));
					return success;
					
				})
				.exceptionally((ex) -> {
					
					logger.debug("  " + "Failed (" + ex + ")");
					return false;
					
				});
		//@formatter:on
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<Boolean> logout()
	{
		logger.debug("Performing logout...");
		logger.debug("  Obtaining token...");

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(PARAMETER_ACTION, ACTION_QUERY);
		parameters.put(PARAMETER_ACTION_QUERY_META, PARAMETER_ACTION_QUERY_META_TOKENS);
		parameters.put(PARAMETER_FORMAT, FORMAT_JSON);

		//@formatter:off
		return CompletableFuture.supplyAsync(API.doPost(parameters, EnumContentType.text))
				.thenApply(PARSER_JSON_OBJECT)
				.thenCompose(jsonObject -> {

					String token = (String) ((Map<String, Object>) ((Map<String, Object>) jsonObject.get("query")).get("tokens")).get("csrftoken");

					logger.debug("  Performing logout...");
					
					parameters.clear();
					parameters.put(PARAMETER_ACTION, ACTION_LOGOUT);
					parameters.put(PARAMETER_FORMAT, FORMAT_JSON);
					parameters.put(PARAMETER_ACTION_LOGOUT_TOKEN, token);
		
					return CompletableFuture.supplyAsync(API.doPost(parameters, EnumContentType.text));
					
				})
				.thenApply(json -> {
		
					boolean success = "{}".equalsIgnoreCase(json);
					logger.debug("  " + (success ? "Successful!" : "Failed"));
					return success;
					
				})
				.exceptionally((ex) -> {
					
					logger.debug("  " + "Failed (" + ex + ")");
					return false;
					
				});
		//@formatter:on
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<Map<String, Object>> query(String title, String prop, String propParam, String... propertiesList)
	{
		String properties = String.join("|", propertiesList);

		logger.debug("Performing prop=" + prop + " for page \"" + title + "\"...");

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(PARAMETER_ACTION, ACTION_QUERY);
		parameters.put(PARAMETER_FORMAT, FORMAT_JSON);
		parameters.put(PARAMETER_ACTION_QUERY_PROP, prop);
		parameters.put(PARAMETER_ACTION_QUERY_TITLES, title);
		parameters.put(propParam, properties);

		//@formatter:off
		return CompletableFuture.supplyAsync(API.doPost(parameters, EnumContentType.text))
				.thenApply(PARSER_JSON_OBJECT)
				.thenApply(jsonObject -> {
					
					return (Map<String, Object>) ((Map<String, Object>) jsonObject.get("query")).get("pages");
					
				})
				.thenApply(pages -> {
					
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
					
				})
				.exceptionally((ex) -> {
					
					logger.debug("  " + "Failed (" + ex + ")");
					return null;
					
				});
		//@formatter:on
	}

	public CompletableFuture<Map<String, Object>> queryRevisionProperties(String title, String... propertiesList)
	{
		return query(title, PARAMETER_ACTION_QUERY_PROP_RV, PARAMETER_ACTION_QUERY_RVPROP, propertiesList);
	}

	public CompletableFuture<Map<String, Object>> queryInfoProperties(String title, String... propertiesList)
	{
		return query(title, PARAMETER_ACTION_QUERY_PROP_IN, PARAMETER_ACTION_QUERY_INPROP, propertiesList);
	}

	public CompletableFuture<Boolean> edit(String title, String content, String summary, boolean ignoreConflicts, boolean bot)
	{
		logger.debug("Performing edit of page \"" + title + "\"...");

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(PARAMETER_ACTION, ACTION_EDIT);
		parameters.put(PARAMETER_FORMAT, FORMAT_JSON);
		parameters.put(PARAMETER_ACTION_EDIT_TITLE, title);
		parameters.put(PARAMETER_ACTION_EDIT_TEXT, content);
		if(summary != null)
			parameters.put(PARAMETER_ACTION_EDIT_SUMMARY, summary);
		if(bot)
			parameters.put(PARAMETER_ACTION_EDIT_BOT, "true");

		//@formatter:off
		logger.debug("  Obtaining token...");
		CompletableFuture<Void> cf = getToken(title, "edit")
				.thenAccept(token -> {
					
					parameters.put(PARAMETER_ACTION_EDIT_TOKEN, token);
					
				});
		
		if(!ignoreConflicts)
		{
			cf = cf.thenComposeAsync(result -> {
				
					logger.debug("  Obtaining Timestamp...");
					return getTimestamp(title);
					
				})
				.thenAccept(baseTimestamp -> {
					
					parameters.put(PARAMETER_ACTION_EDIT_BASETIMESTAMP, baseTimestamp);
					
				});					
		}
		
		return cf.thenComposeAsync(no_value -> {
					return tryEdit(parameters, 5);
				});
		//@formatter:on
	}
	
	@SuppressWarnings("unchecked")
	private CompletableFuture<Boolean> tryEdit(Map<String, Object> parameters, int retries)
	{
		logger.debug("  Performing edit... retries=" + retries);
		//@formatter:off
		return CompletableFuture
				.supplyAsync(API.doPost(parameters, EnumContentType.text))
				.thenApply(PARSER_JSON_OBJECT)
				.thenComposeAsync(jsonObject -> {
					
					String result = (String) ((Map<String, Object>) jsonObject.get("edit")).get("result");
					
					boolean success = "success".equalsIgnoreCase(result);
					logger.debug("  " + (success ? "Successful!" : "Failed"));
					
					if(!success && retries > 0)
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

							logger.debug("  Answering captcha: " + question + " -> " + answer);
							// try again
							return tryEdit(parameters, retries-1);
						}
					}
					return CompletableFuture.completedFuture(success);
					
				});
		//@formatter:on
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public CompletableFuture<String> getContent(String title)
	{
		//@formatter:off
		return queryRevisionProperties(title, "content")
				.thenApply(properties -> {
					if(properties.containsKey("missing"))
						return null;
					List<?> revisions = (List) properties.get("revisions");
					return (String) ((Map<String, Object>) revisions.get(revisions.size() - 1)).get("*");
				});
		//@formatter:on
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public CompletableFuture<String> getTimestamp(String title)
	{
		//@formatter:off
		return queryRevisionProperties(title, "timestamp")
				.thenApply(properties -> {
					if(properties.containsKey("missing"))
						return null;
					List<?> revisions = (List) properties.get("revisions");
					return (String) ((Map<String, Object>) revisions.get(revisions.size() - 1)).get("timestamp");
				});
		//@formatter:on
	}

	public CompletableFuture<String> getToken(String title, String action)
	{
		//@formatter:off
		return query(title, PARAMETER_ACTION_QUERY_PROP_IN, PARAMETER_ACTION_QUERY_INTOKEN, action)
				.thenApply(properties -> {
					return (String) properties.get(action + "token");
				});
		//@formatter:on
	}

	private static String getCaptchaAnswer(String question)
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
