package muskel2.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public abstract class Language
{
	private static final String	folder		= "lang";
	private static final String	defaultLang	= "de";
	
	private static final String VERSION_HISTORY_INFO_PREFIX = "version.";
	private static final String VERSION_HISTORY_INFO_SUFFIX = ".about";
	private static final String PLACEHOLDER_VERSION = "%%VERSION%%";
	private static final String PLACEHOLDER_HISTORY = "%%HISTORY%%";

	private static Properties	lang;

	private static boolean		debug		= false;

	private static String		about		= null;

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
	
	public static String getVersion()
	{
		return getString("version");
	}

	public static String getAbout()
	{
		if(about == null)
		{
			Version currentVersion = new Version(getVersion());
			about = Language.getString("mainframe.about").replace(PLACEHOLDER_VERSION, currentVersion.toString());

			StringBuilder history = new StringBuilder();
			
			@SuppressWarnings("unchecked")
			Set<String> keys = (Set<String>) (Set<?>) lang.keySet();
			List<Version> versions = new ArrayList<Version>();
			for(String key: keys)
			{
				if(key.startsWith(VERSION_HISTORY_INFO_PREFIX))
				{
					Version version = new Version(key.substring(VERSION_HISTORY_INFO_PREFIX.length(), key.indexOf(VERSION_HISTORY_INFO_SUFFIX))); 
					if(version.compareTo(currentVersion) <= 0)
					{
						versions.add(version);
					}
				}
			}
			Collections.sort(versions, Collections.reverseOrder());			
			System.out.println("Versions = " + versions);
			
			for(Version v: versions)
			{
				history.append(getString(VERSION_HISTORY_INFO_PREFIX + v.toString() + VERSION_HISTORY_INFO_SUFFIX));
			}

			about = about.replace(PLACEHOLDER_HISTORY, history.toString());
		}
		return about;
	}
	
	private static class Version implements Comparable<Version>
	{
		private int major;
		private int minor;
		
		public Version(String version)
		{
			major = Integer.parseInt(version.substring(0, version.indexOf('.')));
			minor = Integer.parseInt(version.substring(version.indexOf('.') + 1));
		}

		@Override
		public String toString()
		{
			return major + "." + minor;
		}

		@Override
		public int compareTo(Version o)
		{
			if(this.major == o.major)
				return this.minor - o.minor;
			return this.major - o.major;
		}		
	}
}
