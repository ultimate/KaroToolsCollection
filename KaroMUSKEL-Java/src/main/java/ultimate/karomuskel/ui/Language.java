package ultimate.karomuskel.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.utils.PropertiesUtil;

public abstract class Language
{
	/**
	 * Logger-Instance
	 */
	private static transient final Logger	logger						= LogManager.getLogger(Language.class);
	private static String					folder						= "lang";
	private static final String				defaultLang					= "de";

	private static final String				VERSION_HISTORY_INFO_PREFIX	= "version.";
	private static final String				VERSION_HISTORY_INFO_SUFFIX	= ".about";
	private static final String				PLACEHOLDER_ARG				= "%%ARG%%";
	private static final String				PLACEHOLDER_VERSION			= "%%VERSION%%";
	private static final String				PLACEHOLDER_HISTORY			= "%%HISTORY%%";

	private static Properties				lang;

	private static boolean					debug						= false;

	private static String					about						= null;

	public static boolean load(String language)
	{
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

	public static String getString(String key, String arg)
	{
		if(debug)
			return key + "@" + arg;
		if(lang == null)
			throw new RuntimeException("No language loaded!");
		String text = lang.getProperty(key);
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

	public static String getAbout()
	{
		if(about == null)
		{
			Version currentVersion = new Version(getApplicationVersion());
			about = Language.getString("mainframe.about").replace(PLACEHOLDER_VERSION, currentVersion.toString());

			StringBuilder history = new StringBuilder();

			@SuppressWarnings("unchecked")
			Set<String> keys = (Set<String>) (Set<?>) lang.keySet();
			List<Version> versions = new ArrayList<Version>();
			for(String key : keys)
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
			logger.info("Versions = " + versions);

			for(Version v : versions)
			{
				history.append(getString(VERSION_HISTORY_INFO_PREFIX + v.toString() + VERSION_HISTORY_INFO_SUFFIX));
			}

			about = about.replace(PLACEHOLDER_HISTORY, history.toString());
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
