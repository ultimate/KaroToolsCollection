package ultimate.karoapi4j.utils;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.utils.web.URLLoaderThread;
import ultimate.karoapi4j.utils.web.urlloaders.StringURLLoaderThread;

/**
 * Utility-Class allowing static URL-loading via {@link URLLoaderThread}.<br>
 * An {@link URLLoaderThread} will be constructed when calling readURL(..) and the call will wait
 * until the URL is loaded for returning the result.
 * 
 * @author ultimate
 */
public abstract class URLLoaderUtil
{
	/**
	 * Logger-Instance
	 */
	protected static final Logger	logger	= LoggerFactory.getLogger(URLLoaderUtil.class);

	/**
	 * Load an URL with an {@link URLLoaderThread}<br>
	 * The URL will be loaded by GET with no parameters
	 * 
	 * @param url - the URL to load
	 * @return the URL content
	 */
	public static String readURL(URL url)
	{
		StringURLLoaderThread t = new StringURLLoaderThread(url);
		t.load();
		try
		{
			t.join();
		}
		catch(InterruptedException e)
		{
			logger.error("URLLoaderThread has been interrupted!");
			return null;
		}
		return t.getLoadedContent();
	}

	/**
	 * Load an URL with an {@link URLLoaderThread}<br>
	 * The URL will be loaded by GET with no parameters (with given timeout).
	 * 
	 * @param url - the URL to load
	 * @param timeout - a timeout used for loading the URL
	 * @return the URL content
	 */
	public static String readURL(URL url, int timeout)
	{
		StringURLLoaderThread t = new StringURLLoaderThread(url, timeout);
		t.load();
		try
		{
			t.join();
		}
		catch(InterruptedException e)
		{
			logger.error("URLLoaderThread has been interrupted!");
			return null;
		}
		return t.getLoadedContent();
	}

	/**
	 * Load an URL with an {@link URLLoaderThread}<br>
	 * The URL will be loaded by POST with the given parameters
	 * 
	 * @param url - the URL to load
	 * @param parameters - the parameters to pass to the URL on load
	 * @return the URL content
	 */
	public static String readURL(URL url, String parameter)
	{
		StringURLLoaderThread t = new StringURLLoaderThread(url, parameter);
		t.load();
		try
		{
			t.join();
		}
		catch(InterruptedException e)
		{
			logger.error("URLLoaderThread has been interrupted!");
			return null;
		}
		return t.getLoadedContent();
	}

	/**
	 * Load an URL with an {@link URLLoaderThread}<br>
	 * The URL will be loaded by POST with the given parameters (with given timeout).<br>
	 * Method generally should be GET or POST
	 * 
	 * @param url - the URL to load
	 * @param parameter - the parameters to pass to the URL on load
	 * @param timeout - a timeout used for loading the URL
	 * @return the URL content
	 */
	public static String readURL(URL url, String parameter, int timeout)
	{
		StringURLLoaderThread t = new StringURLLoaderThread(url, parameter, timeout);
		t.load();
		try
		{
			t.join();
		}
		catch(InterruptedException e)
		{
			logger.error("URLLoaderThread has been interrupted!");
			return null;
		}
		return t.getLoadedContent();
	}

	/**
	 * Load an URL with an {@link URLLoaderThread}<br>
	 * 
	 * @param url - the URL to load
	 * @param method - the HTTP-Method used to load the URL
	 * @param parameter - the parameters to pass to the URL on load
	 * @param timeout - a timeout used for loading the URL
	 * @return the URL content
	 */
	public static String readURL(URL url, String method, String parameter, int timeout)
	{
		StringURLLoaderThread t = new StringURLLoaderThread(url, method, parameter, timeout);
		t.load();
		try
		{
			t.join();
		}
		catch(InterruptedException e)
		{
			logger.error("URLLoaderThread has been interrupted!");
			return null;
		}
		return t.getLoadedContent();
	}

	/**
	 * Format a Map of parameters to a String in the format<br>
	 * <code>
	 * param1=value1&param2=value2&...
	 * </code>
	 * 
	 * @param parameters - the Map of parameters
	 * @return the formatted String
	 */
	public static String formatParameters(Map<String, String> parameters)
	{
		StringBuilder sb = new StringBuilder();

		Entry<String, String> param;
		Iterator<Entry<String, String>> paramIterator = parameters.entrySet().iterator();
		while(paramIterator.hasNext())
		{
			param = paramIterator.next();
			sb.append(param.getKey());
			sb.append("=");
			sb.append(param.getValue());
			if(paramIterator.hasNext())
				sb.append("&");
		}

		return sb.toString();
	}
}
