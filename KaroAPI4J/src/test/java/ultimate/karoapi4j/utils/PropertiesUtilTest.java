package ultimate.karoapi4j.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

public class PropertiesUtilTest
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger logger = LogManager.getLogger(getClass());

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
		File normal = new File("normal.properties");
		File zipped = new File("zipped.properties");

		BufferedReader br = null;

		try
		{
			Properties p = new Properties();
			p.setProperty("lala", "1");
			p.setProperty("xxxx", "yyyy");
			for(int i = 0; i < 100; i++)
				p.setProperty("x." + i, i + "_x");

			PropertiesUtil.storeProperties(normal, p, "some comments", false);
			PropertiesUtil.storeProperties(zipped, p, "some comments", true);

			Properties p1 = PropertiesUtil.loadProperties(normal, false);
			Properties p2 = PropertiesUtil.loadProperties(zipped, true);

			for(Object key : p.keySet())
			{
				assertEquals(p.get(key), p1.get(key));
				assertEquals(p.get(key), p2.get(key));
			}

			// check sorting (at least for the not zipped file)
			// Note: does not work if the file contains multiline properties
			String previousKey = null, currentKey = null;
			br = new BufferedReader(new FileReader(normal));
			while(br.ready())
			{
				currentKey = br.readLine();
				if(currentKey.startsWith("#")) // comment
					continue;
				currentKey = currentKey.substring(0, currentKey.indexOf("="));
				if(previousKey != null)
				{
					logger.debug("comparing keys: " + currentKey + " > " + previousKey + " ? " + currentKey.compareTo(previousKey));
					assertTrue(currentKey.compareTo(previousKey) > 0);
				}
				previousKey = currentKey;
			}
		}
		finally
		{
			if(br != null)
				br.close();

			normal.delete();
			zipped.delete();
		}
	}
}