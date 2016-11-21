package ultimate.karoapi4j.wiki;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;
import ultimate.karoapi4j.KaroWikiURLs;
import ultimate.karoapi4j.utils.PropertiesUtil;

public class KaroWikiLoaderTest extends TestCase
{
	private static String		username;
	private static String		password;

	public static final String	PAGE_EXISTING	= "Test";
	public static final String	PAGE_MISSING	= "Asdfasdfasdfasdf";

	static
	{
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

	public void testLoginAndLogout() throws Exception
	{
		KaroWikiLoader wl = new KaroWikiLoader();
		assertTrue(wl.login(username, password));
		assertTrue(wl.logout());
	}

	@SuppressWarnings("rawtypes")
	public void testQueryRevisions() throws Exception
	{
		KaroWikiLoader wl = new KaroWikiLoader();
		try
		{
			assertTrue(wl.login(username, password));

			Map<String, Object> propertiesValid = wl.queryRevisionProperties(PAGE_EXISTING, "timestamp");
			System.out.println(propertiesValid);
			assertEquals(PAGE_EXISTING, propertiesValid.get("title"));
			assertFalse(propertiesValid.containsKey("missing"));
			assertNotNull(propertiesValid.get("pageid"));
			assertNotNull(propertiesValid.get("revisions"));
			assertTrue(propertiesValid.get("revisions") instanceof List);
			assertTrue(((List) propertiesValid.get("revisions")).size() > 0);

			Map<String, Object> propertiesInvalid = wl.queryRevisionProperties(PAGE_MISSING, "timestamp");
			System.out.println(propertiesInvalid);
			assertEquals(PAGE_MISSING, propertiesInvalid.get("title"));
			assertTrue(propertiesInvalid.containsKey("missing"));
		}
		finally
		{
			assertTrue(wl.logout());
		}
	}

	public void testGetTimestamp() throws Exception
	{
		KaroWikiLoader wl = new KaroWikiLoader();
		try
		{
			assertTrue(wl.login(username, password));

			String timestamp;

			timestamp = wl.getTimestamp(PAGE_EXISTING);
			System.out.println(timestamp);
			assertNotNull(timestamp);

			timestamp = wl.getTimestamp(PAGE_MISSING);
			System.out.println(timestamp);
			assertNull(timestamp);

		}
		finally
		{
			assertTrue(wl.logout());
		}
	}

	public void testGetToken() throws Exception
	{
		KaroWikiLoader wl = new KaroWikiLoader();
		try
		{
			assertTrue(wl.login(username, password));

			String token;

			token = wl.getToken(PAGE_EXISTING, "edit");
			System.out.println(token);
			assertNotNull(token);

			token = wl.getToken(PAGE_MISSING, "edit");
			System.out.println(token);
			assertNotNull(token);

		}
		finally
		{
			assertTrue(wl.logout());
		}
	}
	
	public void testEdit() throws Exception
	{
		KaroWikiLoader wl = new KaroWikiLoader();
		try
		{
			assertTrue(wl.login(username, password));

			String content = wl.getContent(PAGE_EXISTING);
			assertNotNull(content);
			
			String newContent = content + "\n\nsome new line --~~~~";
			
			boolean success = wl.edit(PAGE_EXISTING, newContent, "testing wiki API", true);
			assertTrue(success);
			
			Date date = new Date();
			DateFormat df = new SimpleDateFormat("HH:mm, dd. MMM. YYYY");
			String expectedContent = newContent.replace("~~~~", "[[Benutzer:" + username + "|" + username + "]] " + df.format(date) + " (CET)");
			
			String updatedContent = wl.getContent(PAGE_EXISTING);
			assertEquals(expectedContent, updatedContent);
			
		}
		finally
		{
			assertTrue(wl.logout());
		}
	}
	

	public void testSomething() throws Exception
	{
	}
}
