package muskel2.util;

public class HtmlUtil
{
	public static String fixHtml(String string)
	{
		string = string.replace("&auml;", "\u00E4");
		string = string.replace("&Auml;", "\u00C4");
		string = string.replace("&ouml;", "\u00F6");
		string = string.replace("&Ouml;", "\u00D6");
		string = string.replace("&uuml;", "\u00FC");
		string = string.replace("&Uuml;", "\u00DC");
		string = string.replace("&szlig;", "\u00DF");
		string = string.replace("&lt;", "<");
		string = string.replace("&gt;", ">");
		string = string.replace("&quot;", "\"");
		string = string.replace("&amp;", "&");
		return string;
	}
}
