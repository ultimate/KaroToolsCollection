package ultimate.karoapi4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.utils.PropertiesUtil;

public class KaroWikiAPITest
{
	private static String		username;
	private static String		password;

	public static final String	PAGE_EXISTING	= "Test";
	public static final String	PAGE_TEST_SECTION	= "== Ultimates Test Section ==";
	public static final String	PAGE_NEXT_HEADLINE	= "==";
	public static final String	PAGE_MISSING	= "Asdfasdfasdfasdf";
	private static final int	EDIT_TESTS		= 1;

	static
	{
		try
		{
			Properties p = PropertiesUtil.loadProperties(new File("src/test/resources/login.properties"));
			username = p.getProperty("karowiki.user");
			password = p.getProperty("karowiki.password");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	@Test
	public void test_login_logout() throws Exception
	{
		KaroWikiAPI wl = new KaroWikiAPI();
		assertTrue(wl.login(username, password));
		assertTrue(wl.logout());
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void test_queryRevisions() throws Exception
	{
		KaroWikiAPI wl = new KaroWikiAPI();
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

	@Test
	public void test_getTimestamp() throws Exception
	{
		KaroWikiAPI wl = new KaroWikiAPI();
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

	@Test
	public void test_getToken() throws Exception
	{
		KaroWikiAPI wl = new KaroWikiAPI();
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

	@Test
	public void test_edit() throws Exception
	{
		KaroWikiAPI wl = new KaroWikiAPI();
		try
		{
			assertTrue(wl.login(username, password));
			for(int i = 0; i < EDIT_TESTS; i++)
			{
				String content = wl.getContent(PAGE_EXISTING);
				assertNotNull(content);
				
				String newContent;
				
				int index = content.indexOf(PAGE_TEST_SECTION);
				if(index >= 0)
				{
					index = content.indexOf(PAGE_NEXT_HEADLINE, index + PAGE_TEST_SECTION.length());
					newContent = content.substring(0, index) + "some new line --~~~~\n\n" + content.substring(index);
				}
				else
				{	
					newContent = content + "\n\nsome new line --~~~~";
				}
				
				boolean success = wl.edit(PAGE_EXISTING, newContent, "testing wiki API", true, false);
				assertTrue(success);

				Date date = new Date();
				DateFormat df = new SimpleDateFormat("HH:mm, dd. MMM YYYY");
				String expectedContent = newContent.replace("~~~~", "[[Benutzer:" + username + "|" + username + "]] ([[Benutzer Diskussion:" + username + "|Diskussion]]) " + df.format(date) + " (CET)");

				String updatedContent = wl.getContent(PAGE_EXISTING);
				assertEquals(expectedContent, updatedContent);
			}
		}
		finally
		{
			assertTrue(wl.logout());
		}
	}
}
