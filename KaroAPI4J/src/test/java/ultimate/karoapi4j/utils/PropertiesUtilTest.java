package ultimate.karoapi4j.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.Test;

public class PropertiesUtilTest
{
	@Test
	public void test_loadProperties() throws IOException
	{
		File file = new File("src/test/resources/test.properties");
		Properties p = PropertiesUtil.loadProperties(file);
		assertEquals("testvalue", p.getProperty("testkey"));
	}

	@Test
	public void test_loadPropertiesMultiline() throws Exception
	{
		File file = new File("src/test/resources/test.properties");
		Properties p = PropertiesUtil.loadProperties(file);
		assertEquals("Hey,Whassup?", p.getProperty("multiline"));
		assertEquals("Hey,\nEverything ok?", p.getProperty("multiline2"));
	}

	@Test
	public void test_storeProperties() throws Exception
	{
		Properties p = new Properties();
		p.setProperty("lala", "1");
		p.setProperty("xxxx", "yyyy");
		for(int i = 0; i < 100; i++)
			p.setProperty("x." + i, i + "_x");

		File normal = new File("normal.properties");
		File zipped = new File("zipped.properties");

		PropertiesUtil.storeProperties(normal, p, "some comments", false);
		PropertiesUtil.storeProperties(zipped, p, "some comments", true);

		Properties p1 = PropertiesUtil.loadProperties(normal, false);
		Properties p2 = PropertiesUtil.loadProperties(zipped, true);

		for(Object key : p.keySet())
		{
			assertEquals(p.get(key), p1.get(key));
			assertEquals(p.get(key), p2.get(key));
		}
	}
}