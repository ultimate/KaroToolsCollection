package ultimate.karoapi4j;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;

import ultimate.karoapi4j.enums.EnumContentType;
import ultimate.karoapi4j.utils.JSONUtil;
import ultimate.karoapi4j.utils.URLLoader;

/**
 * This is the wrapper for accessing the Karo Wiki API. It provides the basic functionality such as:
 * <ul>
 * <li>{@link KaroWikiAPI#login(String, String)}</li>
 * <li>{@link KaroWikiAPI#logout()}</li>
 * <li>{@link KaroWikiAPI#edit(String, String, String, boolean, boolean)}</li>
 * <li>{@link KaroWikiAPI#getContent(String)}</li>
 * </ul>
 * and other operations, that are necessary for editing
 * <ul>
 * <li>{@link KaroWikiAPI#query(String, String, String, String...)}</li>
 * <li>{@link KaroWikiAPI#queryRevisionProperties(String, String...)}</li>
 * <li>{@link KaroWikiAPI#queryInfoProperties(String, String...)}</li>
 * <li>{@link KaroWikiAPI#getTimestamp(String)}</li>
 * <li>{@link KaroWikiAPI#getToken(String, String)}</li>
 * <li>inlcuding automated captcha answering</li>
 * </ul>
 * Note: the Wiki API itself provides much more operations and informations, but those haven't been implemented here. If you have a feature request,
 * visit https://github.com/ultimate/KaroToolsCollection/issues and create an issue :)
 * <br>
 * Note: Accessing the Wiki API requires a user and password for wiki.karopapier.de. Other than the {@link KaroAPI} this API is cookie based and hence
 * does not support multiple instances, since cookies are handled globally during program execution. So once a
 * {@link KaroWikiAPI#login(String, String)} is performed the user is set for all subsequent operations. If it is required to perform actions for
 * different users, dedicated {@link KaroWikiAPI#logout()} and {@link KaroWikiAPI#login(String, String)} need to be used.
 * <br>
 * Each API call will return a {@link CompletableFuture} which wraps the underlying API call and which then can be used to either load the results
 * either blocking or asynchronously (see {@link URLLoader}).
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public class KaroWikiAPI
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger	logger								= LogManager.getLogger(KaroWikiAPI.class);

	/**
	 * The config key
	 */
	public static final String			CONFIG_KEY							= "karoWIKI";

	//////////////
	// api URLs //
	//////////////

	protected static final URLLoader	KAROWIKI							= new URLLoader("https://wiki.karopapier.de");
	protected static final URLLoader	API									= KAROWIKI.relative("/api.php");

	//////////////////////////
	// parameters & actions //
	//////////////////////////

	public static final String			PARAMETER_ACTION					= "action";
	public static final String			PARAMETER_FORMAT					= "format";

	public static final String			ACTION_LOGIN						= "login";
	public static final String			PARAMETER_ACTION_LOGIN_USER			= "lgname";
	public static final String			PARAMETER_ACTION_LOGIN_PASSWORD		= "lgpassword";
	public static final String			PARAMETER_ACTION_LOGIN_TOKEN		= "lgtoken";

	public static final String			ACTION_LOGOUT						= "logout";
	public static final String			PARAMETER_ACTION_LOGOUT_TOKEN		= "token";

	public static final String			ACTION_QUERY						= "query";
	public static final String			PARAMETER_ACTION_QUERY_META			= "meta";
	public static final String			PARAMETER_ACTION_QUERY_META_TOKENS	= "tokens";
	public static final String			PARAMETER_ACTION_QUERY_PROP			= "prop";
	public static final String			PARAMETER_ACTION_QUERY_PROP_RV		= "revisions";
	public static final String			PARAMETER_ACTION_QUERY_PROP_IN		= "info";
	public static final String			PARAMETER_ACTION_QUERY_TITLES		= "titles";
	public static final String			PARAMETER_ACTION_QUERY_RVPROP		= "rvprop";
	public static final String			PARAMETER_ACTION_QUERY_INPROP		= "inprop";
	public static final String			PARAMETER_ACTION_QUERY_INTOKEN		= "intoken";

	public static final String			ACTION_PARSE						= "parse";
	public static final String			PARAMETER_ACTION_PARSE_PAGE			= "page";
	public static final String			PARAMETER_ACTION_PARSE_PROP			= "prop";
	public static final String			PARAMETER_ACTION_PARSE_PROP_TEXT	= "text";
	public static final String			PARAMETER_ACTION_PARSE_PROP_WIKI	= "wikitext";

	public static final String			ACTION_EDIT							= "edit";
	public static final String			PARAMETER_ACTION_EDIT_TITLE			= "title";
	public static final String			PARAMETER_ACTION_EDIT_TEXT			= "text";
	public static final String			PARAMETER_ACTION_EDIT_TOKEN			= "token";
	public static final String			PARAMETER_ACTION_EDIT_SUMMARY		= "summary";
	public static final String			PARAMETER_ACTION_EDIT_BASETIMESTAMP	= "basetimestamp";
	public static final String			PARAMETER_ACTION_EDIT_CAPTCHAID		= "captchaid";
	public static final String			PARAMETER_ACTION_EDIT_CAPTCHAWORD	= "captchaword";
	public static final String			PARAMETER_ACTION_EDIT_BOT			= "bot";

	public static final String			FORMAT_JSON							= "json";
	public static final String			FORMAT_WIKI							= "wiki";
	public static final String			FORMAT_HTML							= "html";

	/**
	 * @see KaroAPI#getVersion()
	 * @return the version of the {@link KaroAPI}
	 */
	public static String getVersion()
	{
		return KaroAPI.getVersion();
	}

	////////////////////
	// parsers needed //
	////////////////////

	public static final Function<String, Map<String, Object>> PARSER_JSON_OBJECT = new JSONUtil.Parser<>(new TypeReference<Map<String, Object>>() {});

	/**
	 * Default Constructor.<br>
	 * Initializes the {@link CookieHandler}
	 */
	public KaroWikiAPI()
	{
		if(CookieHandler.getDefault() == null)
			CookieHandler.setDefault(new CookieManager());
	}

	/**
	 * Login with username and password
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @param username
	 * @param password
	 * @return true, if the operation was successful, false otherwise
	 */
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

	/**
	 * Logout from the current session
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @return true, if the operation was successful, false otherwise
	 */
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

	/**
	 * Query properties from a given page
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @param title - the title of the page
	 * @param prop
	 * @param propParam
	 * @param propertiesList
	 * @return the map with the queried properties
	 */
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

	/**
	 * Query the revision properties for the given page
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @param title - the title of the page
	 * @param propertiesList
	 * @return the map with the queried properties
	 */
	public CompletableFuture<Map<String, Object>> queryRevisionProperties(String title, String... propertiesList)
	{
		return query(title, PARAMETER_ACTION_QUERY_PROP_RV, PARAMETER_ACTION_QUERY_RVPROP, propertiesList);
	}

	/**
	 * Query the info properties for the given page
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @param title - the title of the page
	 * @param propertiesList
	 * @return the map with the queried properties
	 */
	public CompletableFuture<Map<String, Object>> queryInfoProperties(String title, String... propertiesList)
	{
		return query(title, PARAMETER_ACTION_QUERY_PROP_IN, PARAMETER_ACTION_QUERY_INPROP, propertiesList);
	}

	/**
	 * Edit the page with the given title
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @param title - the title of the page
	 * @param content - the updated content of the page
	 * @param summary - an optional edit summary
	 * @param ignoreConflicts - ignore conflicts? if true, the page will be overwritten regardless of the differences
	 * @param bot - is this a bot?
	 * @return true, if the operation was successful, false otherwise
	 */
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

	/**
	 * Internal operation that is used to handle edit retries (required if a captcha needs to be solved).
	 * 
	 * @param parameters - the parameters to post to the edit page
	 * @param retries - the number of remaining retries
	 * @return true, if the operation was successful, false otherwise
	 */
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

	/**
	 * Parse a given page
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @param title - the title of the page
	 * @param format - 'wiki' or 'html'
	 * @return the map with the queried properties
	 */
	@SuppressWarnings("unchecked")
	public CompletableFuture<Map<String, Object>> parse(String title, String format)
	{
		
		logger.debug("Performing parse format=" + format + " for page \"" + title + "\"...");

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(PARAMETER_ACTION, ACTION_PARSE);
		parameters.put(PARAMETER_FORMAT, FORMAT_JSON);
		parameters.put(PARAMETER_ACTION_PARSE_PAGE, title);
		parameters.put(PARAMETER_ACTION_PARSE_PROP, format);
		parameters.put("disablelimitreport", true);
		parameters.put("disableeditsection", true);
		parameters.put("disabletoc", true);

		//@formatter:off
		return CompletableFuture.supplyAsync(API.doPost(parameters, EnumContentType.text))
				.thenApply(PARSER_JSON_OBJECT)
				.thenApply(jsonObject -> {
					if(jsonObject.containsKey("parse"))
					{
						Map<String, Object> result = (Map<String, Object>) jsonObject.get("parse");
						int id = (int) result.get("pageid");
						logger.debug("  Page existing with id " + id);
						return result;
					}
					else
					{
						logger.debug("  Page not existing");
						return null;
					}
				})
				.exceptionally((ex) -> {
					
					logger.debug("  " + "Failed (" + ex + ")");
					return null;
					
				});
		//@formatter:on
	}

	/**
	 * Get the content of the page with the given title
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @param title - the title of the page
	 * @return the content
	 */
	public CompletableFuture<String> getContent(String title)
	{
		return getContent(title, FORMAT_WIKI);
	}

	/**
	 * Get the content of the page with the given title
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @param title - the title of the page
	 * @param format - 'wiki' or 'html'
	 * @return the content
	 */
	@SuppressWarnings("unchecked")
	public CompletableFuture<String> getContent(String title, String format)
	{
		String formatProp;
		if(FORMAT_HTML.equalsIgnoreCase(format))
			formatProp = PARAMETER_ACTION_PARSE_PROP_TEXT;
		else if(FORMAT_WIKI.equalsIgnoreCase(format))
			formatProp = PARAMETER_ACTION_PARSE_PROP_WIKI;
		else
			throw new IllegalArgumentException("format must be either 'wiki' or 'html'");
		
		//@formatter:off
		return parse(title, formatProp)
				.thenApply(properties -> {
					logger.debug(properties);
					return (String) ((Map<String, Object>) properties.get(formatProp)).get("*");
				});
		//@formatter:on
	}

	/**
	 * Get the timestamp of the page with the given title
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @param title - the title of the page
	 * @return the timestamp
	 */
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

	/**
	 * Get a token for the page with the given title
	 * 
	 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
	 * @param title - the title of the page
	 * @return the token
	 */
	public CompletableFuture<String> getToken(String title, String action)
	{
		//@formatter:off
		return query(title, PARAMETER_ACTION_QUERY_PROP_IN, PARAMETER_ACTION_QUERY_INTOKEN, action)
				.thenApply(properties -> {
					return (String) properties.get(action + "token");
				});
		//@formatter:on
	}

	/**
	 * Provide the answer to the given captcha question.<br>
	 * Currently 4 captcha questions are known, which are hardcoded here.
	 * 
	 * @param question - the question
	 * @return the answer
	 */
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
