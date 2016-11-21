package ultimate.karoapi4j;

public class KaroWikiURLs
{
	/**
	 * http://wiki.karopapier.de/api.php<br>
	 */
	public static final String	API_BASE							= "http://wiki.karopapier.de/api.php";

	public static final String	PARAMETER_ACTION					= "action";
	public static final String	PARAMETER_FORMAT					= "format";

	public static final String	ACTION_LOGIN						= "login";
	public static final String	PARAMETER_ACTION_LOGIN_USER			= "lgname";
	public static final String	PARAMETER_ACTION_LOGIN_PASSWORD		= "lgpassword";
	public static final String	PARAMETER_ACTION_LOGIN_TOKEN		= "lgtoken";

	public static final String	ACTION_LOGOUT						= "logout";

	public static final String	ACTION_QUERY						= "query";
	public static final String	PARAMETER_ACTION_QUERY_PROP			= "prop";
	public static final String	PARAMETER_ACTION_QUERY_PROP_RV		= "revisions";
	public static final String	PARAMETER_ACTION_QUERY_PROP_IN		= "info";
	public static final String	PARAMETER_ACTION_QUERY_TITLES		= "titles";
	public static final String	PARAMETER_ACTION_QUERY_RVPROP		= "rvprop";
	public static final String	PARAMETER_ACTION_QUERY_INPROP		= "inprop";
	public static final String	PARAMETER_ACTION_QUERY_INTOKEN		= "intoken";

	public static final String	ACTION_EDIT							= "edit";
	public static final String	PARAMETER_ACTION_EDIT_TITLE			= "title";
	public static final String	PARAMETER_ACTION_EDIT_TEXT			= "text";
	public static final String	PARAMETER_ACTION_EDIT_TOKEN			= "token";
	public static final String	PARAMETER_ACTION_EDIT_SUMMARY		= "summary";
	public static final String	PARAMETER_ACTION_EDIT_BASETIMESTAMP	= "basetimestamp";

	public static final String	FORMAT_JSON							= "json";

}
