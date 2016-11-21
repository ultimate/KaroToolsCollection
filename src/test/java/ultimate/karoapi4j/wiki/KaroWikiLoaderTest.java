package ultimate.karoapi4j.wiki;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;
import ultimate.karoapi4j.utils.PropertiesUtil;

public class KaroWikiLoaderTest extends TestCase
{
	private static String username;
	private static String password;
	
	static {
		try
		{
			Properties p = PropertiesUtil.loadProperties(new File("src/test/resources/wiki.properties"));
			username = p.getProperty("username");
			password = p.getProperty("password");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void testLogin() throws Exception
	{
		KaroWikiLoader wl = new KaroWikiLoader();
		assertTrue(wl.login(username, password));
	}	
	
	public void testSomething() throws Exception
	{
	}
}
