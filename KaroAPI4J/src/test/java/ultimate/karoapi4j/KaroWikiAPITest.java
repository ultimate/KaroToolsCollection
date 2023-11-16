package ultimate.karoapi4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.utils.PropertiesUtil;

public class KaroWikiAPITest
{
	/**
	 * Logger-Instance
	 */
	protected transient static final Logger	logger					= LogManager.getLogger(KaroWikiAPITest.class);

	private static String					username;
	private static String					password;

	public static final String				PAGE_EXISTING			= "Test";
	public static final String				PAGE_TEST_SECTION		= "== Ultimates Test Section ==";
	public static final String				PAGE_NEXT_HEADLINE		= "==";
	public static final String				PAGE_MISSING			= "Asdfasdfasdfasdf";
	public static final String				DOUBLE_LINE_BREAK		= "\n\n";
	private static final int				EDIT_TESTS				= 1;

	public static final String				PAGE_WITH_FIXED_CONTENT	= "Test/Test";
	public static final String				FIXED_CONTENT_WIKI		= "Bitte nicht verändern.\n{{Benutzer|ultimate}} benutzt das hier zum Testen...\n\n=== Kopfzeile ===\n* Aufgezählter Listeneintrag\n* Aufgezählter Listeneintrag\n* Aufgezählter Listeneintrag\n\nEnde";
	public static final String				FIXED_CONTENT_HTML		= "<div class=\"mw-parser-output\"><p>Bitte nicht verändern.\n<a href=\"/Benutzer:Ultimate\" title=\"Benutzer:Ultimate\">ultimate</a> benutzt das hier zum Testen...\n</p>\n<h3><span class=\"mw-headline\" id=\"Kopfzeile\">Kopfzeile</span></h3>\n<ul><li>Aufgezählter Listeneintrag</li>\n<li>Aufgezählter Listeneintrag</li>\n<li>Aufgezählter Listeneintrag</li></ul>\n<p>Ende\n</p></div>";

	static
	{
		try
		{
			Properties p = PropertiesUtil.loadProperties(KaroWikiAPITest.class, "login.properties");
			logger.debug(p);
			username = p.getProperty(KaroWikiAPI.CONFIG_KEY + ".user");
			password = p.getProperty(KaroWikiAPI.CONFIG_KEY + ".password");
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
		assertTrue(wl.login(username, password).get());
		assertTrue(wl.logout().get());
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void test_queryRevisions() throws Exception
	{
		KaroWikiAPI wl = new KaroWikiAPI();
		try
		{
			assertTrue(wl.login(username, password).get());

			Map<String, Object> propertiesValid = wl.queryRevisionProperties(PAGE_EXISTING, "timestamp").get();
			logger.debug(propertiesValid.toString());
			assertEquals(PAGE_EXISTING, propertiesValid.get("title"));
			assertFalse(propertiesValid.containsKey("missing"));
			assertNotNull(propertiesValid.get("pageid"));
			assertNotNull(propertiesValid.get("revisions"));
			assertTrue(propertiesValid.get("revisions") instanceof List);
			assertTrue(((List) propertiesValid.get("revisions")).size() > 0);

			Map<String, Object> propertiesInvalid = wl.queryRevisionProperties(PAGE_MISSING, "timestamp").get();
			logger.debug(propertiesInvalid.toString());
			assertEquals(PAGE_MISSING, propertiesInvalid.get("title"));
			assertTrue(propertiesInvalid.containsKey("missing"));
		}
		finally
		{
			assertTrue(wl.logout().get());
		}
	}

	@Test
	public void test_getTimestamp() throws Exception
	{
		KaroWikiAPI wl = new KaroWikiAPI();
		try
		{
			assertTrue(wl.login(username, password).get());

			String timestamp;

			timestamp = wl.getTimestamp(PAGE_EXISTING).get();
			logger.debug(timestamp);
			assertNotNull(timestamp);

			timestamp = wl.getTimestamp(PAGE_MISSING).get();
			logger.debug(timestamp);
			assertNull(timestamp);

		}
		finally
		{
			assertTrue(wl.logout().get());
		}
	}

	@Test
	public void test_getToken() throws Exception
	{
		KaroWikiAPI wl = new KaroWikiAPI();
		try
		{
			assertTrue(wl.login(username, password).get());

			String token;

			token = wl.getToken(PAGE_EXISTING, "edit").get();
			logger.debug(token);
			assertNotNull(token);

			token = wl.getToken(PAGE_MISSING, "edit").get();
			logger.debug(token);
			assertNotNull(token);

		}
		finally
		{
			assertTrue(wl.logout().get());
		}
	}

	@Test
	public void test_edit() throws Exception
	{
		KaroWikiAPI wl = new KaroWikiAPI();
		try
		{
			assertTrue(wl.login(username, password).get());
			for(int i = 0; i < EDIT_TESTS; i++)
			{
				String content = wl.getContent(PAGE_EXISTING).get();
				assertNotNull(content);

				String newContent;

				int index = content.indexOf(PAGE_TEST_SECTION);
				if(index >= 0)
				{
					index = content.indexOf(PAGE_NEXT_HEADLINE, index + PAGE_TEST_SECTION.length());
					newContent = content.substring(0, index) + "some new line --~~~~" + DOUBLE_LINE_BREAK + content.substring(index);
				}
				else
				{
					newContent = content + DOUBLE_LINE_BREAK + "some new line --~~~~";
				}

				LocalDateTime date = LocalDateTime.now();
				boolean success = wl.edit(PAGE_EXISTING, newContent, "testing wiki API", true, false).get();
				assertTrue(success);

				DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("HH:mm, d. MMM. yyyy", Locale.GERMAN);
				String dateString = dateFormatter.format(date);
				dateString = dateString.replace("Mai.", "Mai");
				String expectedContent;
				if(TimeZone.getDefault().inDaylightTime(new Date()))
					expectedContent = newContent.replace("~~~~", "[[Benutzer:" + username + "|" + username + "]] ([[Benutzer Diskussion:" + username + "|Diskussion]]) " + dateString + " (CEST)");
				else
					expectedContent = newContent.replace("~~~~", "[[Benutzer:" + username + "|" + username + "]] ([[Benutzer Diskussion:" + username + "|Diskussion]]) " + dateString + " (CET)");

				String updatedContent = wl.getContent(PAGE_EXISTING).get();
				assertEquals(expectedContent, updatedContent);
			}
		}
		finally
		{
			assertTrue(wl.logout().get());
		}
	}

	@Test
	public void test_dateFormat() throws Exception
	{
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("HH:mm, d. MMM. yyyy", Locale.GERMAN);

		logger.debug("current date time = " + dateFormatter.format(LocalDateTime.now()));
		assertEquals("12:00, 1. Jan. 2022", dateFormatter.format(LocalDateTime.of(2022, 1, 1, 12, 00)));
		assertEquals("12:00, 1. Feb. 2022", dateFormatter.format(LocalDateTime.of(2022, 2, 1, 12, 00)));
		assertEquals("12:00, 1. Mär. 2022", dateFormatter.format(LocalDateTime.of(2022, 3, 1, 12, 00)));
		assertEquals("12:00, 1. Apr. 2022", dateFormatter.format(LocalDateTime.of(2022, 4, 1, 12, 00)));
		assertEquals("12:00, 1. Mai 2022", dateFormatter.format(LocalDateTime.of(2022, 5, 1, 12, 00)).replace("Mai.", "Mai"));
		assertEquals("12:00, 1. Jun. 2022", dateFormatter.format(LocalDateTime.of(2022, 6, 1, 12, 00)));
		assertEquals("12:00, 1. Jul. 2022", dateFormatter.format(LocalDateTime.of(2022, 7, 1, 12, 00)));
		assertEquals("12:00, 1. Aug. 2022", dateFormatter.format(LocalDateTime.of(2022, 8, 1, 12, 00)));
		assertEquals("12:00, 1. Sep. 2022", dateFormatter.format(LocalDateTime.of(2022, 9, 1, 12, 00)));
		assertEquals("12:00, 1. Okt. 2022", dateFormatter.format(LocalDateTime.of(2022, 10, 1, 12, 00)));
		assertEquals("12:00, 1. Nov. 2022", dateFormatter.format(LocalDateTime.of(2022, 11, 1, 12, 00)));
		assertEquals("12:00, 1. Dez. 2022", dateFormatter.format(LocalDateTime.of(2022, 12, 1, 12, 00)));
	}

	@Test
	public void test_getContent_wiki() throws Exception
	{
		KaroWikiAPI wl = new KaroWikiAPI();
		try
		{
			assertTrue(wl.login(username, password).get());
			String content = wl.getContent(PAGE_WITH_FIXED_CONTENT, KaroWikiAPI.FORMAT_WIKI).get();
			assertNotNull(content);
			assertEquals(FIXED_CONTENT_WIKI, content);
		}
		finally
		{
			assertTrue(wl.logout().get());
		}
	}

	@Test
	public void test_getContent_html() throws Exception
	{
		KaroWikiAPI wl = new KaroWikiAPI();
		try
		{
			assertTrue(wl.login(username, password).get());
			String content = wl.getContent(PAGE_WITH_FIXED_CONTENT, KaroWikiAPI.FORMAT_HTML).get();
			assertNotNull(content);
			
//			System.out.println("----------------------------------------------");
//			System.out.println(content);
//			System.out.println("----------------------------------------------");
			
			assertEquals(FIXED_CONTENT_HTML, content);
		}
		finally
		{
			assertTrue(wl.logout().get());
		}
	}
}
