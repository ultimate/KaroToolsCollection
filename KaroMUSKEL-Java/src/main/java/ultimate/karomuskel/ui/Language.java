package ultimate.karomuskel.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.utils.PropertiesUtil;

public abstract class Language
{
	/**
	 * Logger-Instance
	 */
	protected transient static final Logger	logger						= LoggerFactory.getLogger(Language.class);
	private static final String				folder						= "lang";
	private static final String				defaultLang					= "de";

	private static final String				VERSION_HISTORY_INFO_PREFIX	= "version.";
	private static final String				VERSION_HISTORY_INFO_SUFFIX	= ".about";
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
			logger.info("Loading language '" + language + "'... ");
			lang = PropertiesUtil.loadProperties(new File(folder + "/lang_" + language + ".properties"));
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
		return lang.getProperty(key, arg);
	}
	
	public static String getString(EnumGameDirection direction)
	{
		switch(direction)
		{
			case classic:
				return getString("option.direction.klassisch");
			case free:
				return getString("option.direction.egal");
			case formula1:
				return getString("option.direction.formula1");
			default:
				return getString("option.direction.random");
		}
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
	
	public static class Label<V>
	{
		private String label;
		private V value;
		
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