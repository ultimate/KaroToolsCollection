package ultimate.karoapi4j.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.utils.URLLoader.BackgroundLoader;

public class URLLoaderTest
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger logger = LoggerFactory.getLogger(getClass());

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
