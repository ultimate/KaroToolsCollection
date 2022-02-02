package ultimate.karoapi4j.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.enums.EnumContentType;
import ultimate.karoapi4j.utils.URLLoader.BackgroundLoader;

public class URLLoaderTest
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger logger = LoggerFactory.getLogger(getClass());

	@Test
	public void test_formatParameters()
	{
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put("key1", "1");
		parameters.put("key2", 2);

		String json = URLLoader.formatParameters(parameters, EnumContentType.json);

		assertEquals("{\"key1\":\"1\",\"key2\":2}", json);

		String text = URLLoader.formatParameters(parameters, EnumContentType.text);

		assertEquals("key1=1&key2=2", text);
	}

	@Test
	public void test_doLoad() throws MalformedURLException, IOException
	{
		String url = "http://www.karopapier.de";
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		
		String result;
		String expected = "<!DOCTYPE html>\n"
				+ "<html>\n"
				+ "    <head>\n"
				+ "        <meta charset=\"UTF-8\"/>\n"
				+ "        <meta name=”theme-color” content=”#333399”>\n"
				+ "        <title>Karopapier - Autofahren wie in der Vorlesung</title>";
		
		// simple get
		result = URLLoader.doLoad(connection, "GET", null, null, "UTF-8");
		assertNotNull(result);
		
		for(int i = 0; i < expected.length(); i++)
			assertEquals(expected.charAt(i), result.charAt(i), "char mismatch at position " + i + ": '" + expected.charAt(i) + "' vs. '" + result.charAt(i)+ "'");
		
		assertTrue(result.startsWith(expected));
	}

	@Test
	public void test_parameterize()
	{
		String url = "http://www.karopapier.de/api/users";

		HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("key1", "1");
		paramMap.put("key2", 2);

		String paramString = "key1=1&key2=2";

		String expected = url + "?" + paramString;

		// with map
		assertEquals(expected, new URLLoader(url).parameterize(paramMap).getUrl());
		assertEquals(expected, new URLLoader(url + "/").parameterize(paramMap).getUrl());

		// with string
		assertEquals(expected, new URLLoader(url).parameterize(paramString).getUrl());
		assertEquals(expected, new URLLoader(url + "/").parameterize(paramString).getUrl());
		assertEquals(expected, new URLLoader(url + "/").parameterize("?" + paramString).getUrl());
	}

	@Test
	public void test_relative()
	{
		String url = "http://www.karopapier.de";
		String path = "api";
		String expected = url + "/" + path;

		// standard case
		assertEquals(expected, new URLLoader(url).relative("/" + path).getUrl());

		// test without /
		assertEquals(expected, new URLLoader(url).relative(path).getUrl());

		// test with double /
		assertEquals(expected, new URLLoader(url + "/").relative("/" + path).getUrl());

		// test with base / only
		assertEquals(expected, new URLLoader(url + "/").relative(path).getUrl());
	}

	@Test
	public void test_replace()
	{
		String url = "http://www.karopapier.de/api/users";
		String placeholder = "$";
		String replacement = "foo";

		String original = url + "/" + placeholder;
		String replaced = url + "/" + replacement;

		assertEquals(replaced, new URLLoader(original).replace(placeholder, replacement).getUrl());
	}

	@Test
	public void test_blocking_get()
	{
		BackgroundLoader l = new URLLoader("http://www.karopapier.de").doGet();
		String result = l.get();
		logger.debug(result);
		assertNotNull(result);
	}

	@Test
	public void test_async_FutureTask() throws InterruptedException, ExecutionException
	{
		BackgroundLoader l = new URLLoader("http://www.karopapier.de").doGet();
		FutureTask<String> ft = new FutureTask<>(l);
		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.execute(ft);
		String result = ft.get();
		logger.debug(result);
		assertNotNull(result);
	}
}
