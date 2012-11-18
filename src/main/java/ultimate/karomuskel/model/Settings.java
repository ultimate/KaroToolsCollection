package ultimate.karomuskel.model;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karomuskel.utils.Language;

public class Settings
{
	private static final boolean ZIPPED_STORAGE = true;
	
	private String language;
	
	private List<Profile> profiles;

	private Settings()
	{
		this(Language.getDefault(), new LinkedList<Profile>());
	}
	
	private Settings(String language, List<Profile> profiles)
	{
		super();
		this.language = language;
		this.profiles = profiles;
		
		Language.load(getLanguage());
	}

	public String getLanguage()
	{
		return language;
	}

	public List<Profile> getProfiles()
	{
		return profiles;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}
	
	public static Properties toProperties(Settings settings)
	{
		Properties properties = new Properties();
		properties.setProperty("language", settings.getLanguage());
		int profileCount = 0;
		for(Profile p: settings.getProfiles())
		{
			profileCount++;
			properties.setProperty("profile." + profileCount + ".karoUsername", p.getKaroUsername());
			properties.setProperty("profile." + profileCount + ".karoPassword", p.getKaroPassword());
			properties.setProperty("profile." + profileCount + ".wikiUsername", p.getWikiUsername());
			properties.setProperty("profile." + profileCount + ".wikiPassword", p.getWikiPassword());
		}	
		return properties;
	}
	
	public static Settings fromProperties(Properties properties)
	{
		if(properties == null || properties.getProperty("language") == null)
			return new Settings();
		
		List<Profile> profiles = new LinkedList<Profile>();
		int profileCount = 0;
		Profile p;
		while(true)
		{
			profileCount++;
			if(properties.getProperty("profile." + profileCount + ".karoUsername") == null)
				break;
			else
			{
				p = new Profile();
				p.setKaroUsername(properties.getProperty("profile." + profileCount + ".karoUsername"));
				p.setKaroPassword(properties.getProperty("profile." + profileCount + ".karoPassword"));
				p.setWikiUsername(properties.getProperty("profile." + profileCount + ".wikiUsername"));
				p.setWikiPassword(properties.getProperty("profile." + profileCount + ".wikiPassword"));
				profiles.add(p);
			}
		}
		
		return new Settings(properties.getProperty("language"), profiles);
	}
	
	public static Settings load(File file)
	{
		System.out.print("Lade Einstellungen: '" + file.getAbsolutePath() + "'... ");
		try
		{
			Settings s = fromProperties(PropertiesUtil.loadProperties(file, ZIPPED_STORAGE));
			System.out.println("OK");
			return s;
		}
		catch(IOException e)
		{
			System.out.println("Datei nicht gefunden. Neue Einstellungen werden erzeugt!");
			return new Settings();
		}
	}
	
	public static boolean store(File file, Settings settings)
	{
		try
		{
			PropertiesUtil.storeProperties(file, toProperties(settings), null, ZIPPED_STORAGE);
			return true;
		}
		catch(IOException e)
		{
			System.out.println("ERROR");
			System.out.println("Could store settings '" + file.getAbsolutePath() + "': " + e.getMessage());
			return false;
		}
	}
	
	public static void main(String[] args)
	{
		Settings s = new Settings();
		s.setLanguage("en");
		s.getProfiles().add(new Profile("a", "", "c", "d", ""));
		s.getProfiles().add(new Profile("e", "f", "g", "", ""));
		
		File f = new File(".s");
		store(f, s);
		
		Settings s2 = load(f);
		System.out.println(s2.getLanguage());
		System.out.println(s2.getProfiles().get(0).getKaroUsername());
		System.out.println(s2.getProfiles().get(1).getKaroUsername());
	}
}
