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
import ultimate.karoapi4j.utils.Version;

public abstract class Language
{
	/**
	 * Logger-Instance
	 */
	private static transient final Logger	logger					= LogManager.getLogger(Language.class);
	private static String					folder					= "lang";
	private static final String				defaultLang				= "de";

	private static final String				PLACEHOLDER_ARG			= "%%ARG%%";
	private static final String				PLACEHOLDER_VERSION		= "%%CURRENTVERSION%%";
	private static final String				PLACEHOLDER_NEWEST		= "%%NEWESTVERSION%%";
	private static final String				PLACEHOLDER_CHANGELOG	= "%%CHANGELOG%%";

	private static final String				PAGE_CHANGELOG			= "KaroMUSKEL/Changelog";

	private static final String				VERSION_START			= ">Version ";
	private static final String				VERSION_DATE_START		= " (";
	private static final String				VERSION_END				= "<";

	private static Properties				lang;

	private static boolean					debug					= false;

	private static String					about					= null;
	private static String					changelog				= null;
	private static Version					availableVersion		= null;

	private static KaroWikiAPI				wikiAPI					= null;

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

	public static Version getApplicationVersion()
	{
		return new Version(getString("karoMUSKEL.version"));
	}

	public static Version getAvailableVersion()
	{
		if(availableVersion == null)
		{
			getChangelog();

			int start = changelog.indexOf(VERSION_START);
			int end;
			String versionString;
			availableVersion = new Version("0");
			Version version;
			while(start > 0)
			{
				start += VERSION_START.length();
				end = changelog.indexOf(VERSION_END, start);

				versionString = changelog.substring(start, end);
				if(versionString.contains(VERSION_DATE_START))
					versionString = versionString.substring(0, versionString.indexOf(VERSION_DATE_START));

				try
				{
					version = new Version(versionString);

					logger.trace("changelog version found: '" + versionString + "' => " + version + "");

					if(version.compareTo(availableVersion) > 0)
						availableVersion = version;
				}
				catch(Exception e)
				{
					logger.warn("changelog version found: '" + versionString + "' => INVALID");
				}

				start = changelog.indexOf(VERSION_START, end + 1);
			}
		}
		return availableVersion;
	}

	public static String insertVersions(String s)
	{
		s = s.replace(PLACEHOLDER_VERSION, getApplicationVersion().toString());
		s = s.replace(PLACEHOLDER_NEWEST, getAvailableVersion().toString());
		return s;
	}

	public static String getChangelog()
	{
		if(debug)
			return "@CHANGELOG";

		if(changelog == null)
		{
			try
			{
				changelog = wikiAPI.getContent(PAGE_CHANGELOG, KaroWikiAPI.FORMAT_HTML).get(10, TimeUnit.SECONDS);

//				System.out.println("---------------------------------------");
//				System.out.println(changelog);
//				System.out.println("---------------------------------------");

			}
			catch(InterruptedException | ExecutionException | TimeoutException e)
			{
				logger.error(e);
				return e.getMessage();
			}
		}
		return changelog;
	}

	public static String getAbout()
	{
		if(about == null)
		{
			about = Language.getString("mainframe.about");
			about = insertVersions(about);
			about = about.replace(PLACEHOLDER_CHANGELOG, getChangelog());
		}
		return about;
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
