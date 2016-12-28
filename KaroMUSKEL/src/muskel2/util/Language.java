package muskel2.util;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public abstract class Language
{
	private static final String	folder		= "lang";
	private static final String	defaultLang	= "de";

	private static Properties	lang;

	private static boolean		debug		= false;

	public static boolean load(String language)
	{
		if("debug".equalsIgnoreCase(language))
		{
			debug = true;
			return true;
		}
		try
		{
			System.out.print("Loading language '" + language + "'... ");
			lang = PropertiesUtil.loadProperties(new File(folder + "/lang_" + language + ".properties"));
			System.out.println("OK");
			return true;
		}
		catch(IOException e)
		{
			System.out.println("ERROR");
			System.out.println("Could not language '" + language + "': " + e.getMessage());
			return false;
		}
	}

	public static String getDefault()
	{
		return defaultLang;
	}

	public static String getString(String key)
	{
		if(debug)
			return key;
		if(lang == null)
			throw new RuntimeException("No language loaded!");
		return lang.getProperty(key);
	}

	public static String getString(String key, String arg)
	{
		if(debug)
			return key + "@" + arg;
		if(lang == null)
			throw new RuntimeException("No language loaded!");
		return lang.getProperty(key, arg);
	}
}
