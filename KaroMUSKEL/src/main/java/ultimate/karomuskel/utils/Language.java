package ultimate.karomuskel.utils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import ultimate.karoapi4j.utils.PropertiesUtil;

public abstract class Language
{
	private static final String folder = "lang";
	private static final String defaultLang = "de";
	
	private static Properties lang;
	
	public static boolean load(String language)
	{
		try
		{
			System.out.print("Loading language '" + language + "'... ");
			lang = PropertiesUtil.loadProperties(new File(folder + "/lang_" + language + ".properties"));
			System.out.println("OK");
			return true;
		}
		catch (IOException e)
		{
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
		if(lang == null)
			throw new RuntimeException("No language loaded!");
		return lang.getProperty(key);
	}
	
	public static String getString(String key, String arg)
	{
		if(lang == null)
			throw new RuntimeException("No language loaded!");
		return lang.getProperty(key, arg);
	}
}
