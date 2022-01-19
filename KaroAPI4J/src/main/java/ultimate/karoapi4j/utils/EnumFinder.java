package ultimate.karoapi4j.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.web.URLLoader.BackgroundLoader;

public class EnumFinder
{
	/**
	 * Logger-Instance
	 */
	protected transient static final Logger logger = LoggerFactory.getLogger(EnumFinder.class);
	public static final String QUOT = "\"";

	public static Set<String> findEnums(String raw, String key)
	{
		Set<String> values = new HashSet<>();
		String searchString = QUOT + key + QUOT;
		int index = 0;
		int start, end;
		
		do {
			index = raw.indexOf(searchString, index);
			if(index >= 0)
			{
				start = raw.indexOf(QUOT, index + searchString.length() + 1) + QUOT.length();
				end = raw.indexOf(QUOT, start + QUOT.length());
				values.add(raw.substring(start, end));
				index = end;
			}
		}
		while(index >= 0);
		return values;
	}

	public static void main(String[] args) throws InterruptedException, IOException
	{
		Properties properties = PropertiesUtil.loadProperties(new File("target/test-classes/test.properties"));
		logger.info("properties loaded: " + properties);
		KaroAPI karoAPI = new KaroAPI(properties.getProperty("karoapi.user"), properties.getProperty("karoapi.password"));
		logger.info("KaroAPI initialized");
		
		BackgroundLoader<List<User>> users = karoAPI.getUsers();
		users.doBlocking();
		
		System.out.println("soundfile = " + findEnums(users.getRawResult(), "soundfile"));
		System.out.println("theme     = " + findEnums(users.getRawResult(), "theme"));
		System.out.println("gamesort  = " + findEnums(users.getRawResult(), "gamesort"));
		System.out.println("state     = " + findEnums(users.getRawResult(), "state"));
	}
}
