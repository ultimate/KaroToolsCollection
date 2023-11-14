package ultimate.karomuskel.ui;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroWikiAPI;
import ultimate.karoapi4j.utils.PropertiesUtil;

public abstract class Language
{
	/**
	 * Logger-Instance
	 */
	private static transient final Logger	logger						= LogManager.getLogger(Language.class);
	private static String					folder						= "lang";
	private static final String				defaultLang					= "de";

	private static final String				PLACEHOLDER_ARG				= "%%ARG%%";
	private static final String				PLACEHOLDER_VERSION			= "%%CURRENTVERSION%%";
	private static final String				PLACEHOLDER_NEWEST			= "%%NEWESTVERSION%%";
	private static final String				PLACEHOLDER_CHANGELOG			= "%%CHANGELOG%%";
	
	private static final String				PAGE_CHANGELOG 				= "KaroMUSKEL/Changelog";

	private static Properties				lang;

	private static boolean					debug						= false;

	private static String					about						= null;

	private static KaroWikiAPI				wikiAPI						= null;

	public static boolean load(String language)
	{
		wikiAPI = new KaroWikiAPI();
		if("debug".equalsIgnoreCase(language))
		{
			debug = true;
			return true;
		}
		try
		{
			logger.info("loading language '" + language + "'... ");
			lang = PropertiesUtil.loadProperties(Language.class, folder + "/lang_" + language + ".properties");
			logger.info("language '" + language + "' loaded");
			return true;
		}
		catch(IOException e)
		{
			logger.error("could not language '" + language + "'", e);
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

	public static String getString(String key, int arg)
	{
		return getString(key, "" + arg);
	}

	public static String getString(String key, String arg)
	{
		if(debug)
			return key + "@" + arg;
		if(lang == null)
			throw new RuntimeException("No language loaded!");
		String text = lang.getProperty(key);
		if(arg == null)
			return text;
		if(text == null)
			return text;
		return text.replace(PLACEHOLDER_ARG, arg);
	}

	public static <E> String getString(Class<E> enumType, E crashallowed)
	{
		return getString(enumType.getSimpleName() + "." + crashallowed);
	}

	public static String getApplicationName()
	{
		return getString("karoMUSKEL.name") + (debug ? "-debug" : "");
	}

	public static String getApplicationVersion()
	{
		return getString("karoMUSKEL.version");
	}

	public static String getAvailableVersion()
	{
		return "0.0.0";
	}

	public static String getChangelog()
	{
		if(debug)
			return "@CHANGELOG";
		try
		{
			return wikiAPI.getContent(PAGE_CHANGELOG, KaroWikiAPI.FORMAT_HTML).get(10, TimeUnit.SECONDS);
		}
		catch(InterruptedException | ExecutionException | TimeoutException e)
		{
			logger.error(e);
			return e.getMessage();
		}
	}

	public static String getAbout()
	{
		if(about == null)
		{
			Version currentVersion = new Version(getApplicationVersion());
			Version newestVersion = new Version(getAvailableVersion());
			about = Language.getString("mainframe.about");
			about = about.replace(PLACEHOLDER_VERSION, currentVersion.toString());
			about = about.replace(PLACEHOLDER_NEWEST, newestVersion.toString());
			about = about.replace(PLACEHOLDER_CHANGELOG, getChangelog());
		}
		return about;
	}

	private static class Version implements Comparable<Version>
	{
		private int	major;
		private int	minor;

		public Version(String version)
		{
			int firstDot = version.indexOf('.');
			int secondDot = version.indexOf('.', firstDot + 1);

			major = Integer.parseInt(version.substring(0, firstDot));
			if(secondDot > 0)
				minor = Integer.parseInt(version.substring(firstDot + 1, secondDot));
			else
				minor = Integer.parseInt(version.substring(firstDot + 1));
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

	public static class Label<V>
	{
		private String	label;
		private V		value;

		public Label(String label, V value)
		{
			super();
			this.label = label;
			this.value = value;
		}

		public String getLabel()
		{
			return label;
		}

		public V getValue()
		{
			return value;
		}

		public String toString()
		{
			return label;
		}
	}

}
