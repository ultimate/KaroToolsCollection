package ultimate.karoapi4j.utils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.utils.web.URLLoaderThread;
import ultimate.karoapi4j.utils.web.urlloaders.JSONURLLoaderThread;
import ultimate.karoapi4j.utils.web.urlloaders.StringURLLoaderThread;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Utility-Class allowing static URL-loading via {@link URLLoaderThread}.<br>
 * An {@link URLLoaderThread} will be constructed when calling load(..) and the call will wait
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
	public static String load(URL url)
	{
		return load(new StringURLLoaderThread(url));
	}

	/**
	 * Load an URL with an {@link URLLoaderThread}<br>
	 * The URL will be loaded by GET with no parameters (with given timeout).
	 * 
	 * @param url - the URL to load
	 * @param timeout - a timeout used for loading the URL
	 * @return the URL content
	 */
	public static String load(URL url, int timeout)
	{
		return load(new StringURLLoaderThread(url, timeout));
	}

	/**
	 * Load an URL with an {@link URLLoaderThread}<br>
	 * The URL will be loaded by POST with the given parameters
	 * 
	 * @param url - the URL to load
	 * @param parameters - the parameters to pass to the URL on load
	 * @return the URL content
	 */
	public static String load(URL url, String parameter)
	{
		return load(new StringURLLoaderThread(url, parameter));
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
	public static String load(URL url, String parameter, int timeout)
	{
		return load(new StringURLLoaderThread(url, parameter, timeout));
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
	public static String load(URL url, String method, String parameter, int timeout)
	{
		return load(new StringURLLoaderThread(url, method, parameter, timeout));
	}

	/**
	 * Load an URL with an {@link URLLoaderThread} for the required return type<br>
	 * The URL will be loaded by GET with no parameters
	 * 
	 * @param url - the URL to load
	 * @param type - the required type of content
	 * @return the URL content
	 */
	public static <T> T load(URL url, Class<T> type)
	{
		return load(new JSONURLLoaderThread<T>(url));
	}

	/**
	 * Load an URL with an {@link URLLoaderThread}<br>
	 * The URL will be loaded by GET with no parameters (with given timeout).
	 * 
	 * @param url - the URL to load
	 * @param timeout - a timeout used for loading the URL
	 * @param type - the required type of content
	 * @return the URL content
	 */
	public static <T> T load(URL url, int timeout, Class<T> type)
	{
		return load(new JSONURLLoaderThread<T>(url, timeout));
	}

	/**
	 * Load an URL with an {@link URLLoaderThread}<br>
	 * The URL will be loaded by POST with the given parameters
	 * 
	 * @param url - the URL to load
	 * @param parameters - the parameters to pass to the URL on load
	 * @param type - the required type of content
	 * @return the URL content
	 */
	public static <T> T load(URL url, String parameter, Class<T> type)
	{
		return load(new JSONURLLoaderThread<T>(url, parameter));
	}

	/**
	 * Load an URL with an {@link URLLoaderThread}<br>
	 * The URL will be loaded by POST with the given parameters (with given timeout).<br>
	 * Method generally should be GET or POST
	 * 
	 * @param url - the URL to load
	 * @param parameter - the parameters to pass to the URL on load
	 * @param timeout - a timeout used for loading the URL
	 * @param type - the required type of content
	 * @return the URL content
	 */
	public static <T> T load(URL url, String parameter, int timeout, Class<T> type)
	{
		return load(new JSONURLLoaderThread<T>(url, parameter, timeout));
	}

	/**
	 * Load an URL with an {@link URLLoaderThread}<br>
	 * 
	 * @param url - the URL to load
	 * @param method - the HTTP-Method used to load the URL
	 * @param parameter - the parameters to pass to the URL on load
	 * @param timeout - a timeout used for loading the URL
	 * @param type - the required type of content
	 * @return the URL content
	 */
	public static <T> T load(URL url, String method, String parameter, int timeout, Class<T> type)
	{
		return load(new JSONURLLoaderThread<T>(url, method, parameter, timeout));
	}

	/**
	 * Load an URL with an {@link URLLoaderThread} for the required return type<br>
	 * The URL will be loaded by GET with no parameters
	 * 
	 * @param url - the URL to load
	 * @param typeRef - the TypeReference for JSON deserialization
	 * @return the URL content
	 */
	public static <T> T load(URL url, TypeReference<T> typeRef)
	{
		return load(new JSONURLLoaderThread<T>(url, typeRef));
	}

	/**
	 * Load an URL with an {@link URLLoaderThread}<br>
	 * The URL will be loaded by GET with no parameters (with given timeout).
	 * 
	 * @param url - the URL to load
	 * @param timeout - a timeout used for loading the URL
	 * @param typeRef - the TypeReference for JSON deserialization
	 * @return the URL content
	 */
	public static <T> T load(URL url, int timeout, TypeReference<T> typeRef)
	{
		return load(new JSONURLLoaderThread<T>(url, timeout, typeRef));
	}

	/**
	 * Load an URL with an {@link URLLoaderThread}<br>
	 * The URL will be loaded by POST with the given parameters
	 * 
	 * @param url - the URL to load
	 * @param parameters - the parameters to pass to the URL on load
	 * @param typeRef - the TypeReference for JSON deserialization
	 * @return the URL content
	 */
	public static <T> T load(URL url, String parameter, TypeReference<T> typeRef)
	{
		return load(new JSONURLLoaderThread<T>(url, parameter, typeRef));
	}

	/**
	 * Load an URL with an {@link URLLoaderThread}<br>
	 * The URL will be loaded by POST with the given parameters (with given timeout).<br>
	 * Method generally should be GET or POST
	 * 
	 * @param url - the URL to load
	 * @param parameter - the parameters to pass to the URL on load
	 * @param timeout - a timeout used for loading the URL
	 * @param typeRef - the TypeReference for JSON deserialization
	 * @return the URL content
	 */
	public static <T> T load(URL url, String parameter, int timeout, TypeReference<T> typeRef)
	{
		return load(new JSONURLLoaderThread<T>(url, parameter, timeout, typeRef));
	}

	/**
	 * Load an URL with an {@link URLLoaderThread}<br>
	 * 
	 * @param url - the URL to load
	 * @param method - the HTTP-Method used to load the URL
	 * @param parameter - the parameters to pass to the URL on load
	 * @param timeout - a timeout used for loading the URL
	 * @param typeRef - the TypeReference for JSON deserialization
	 * @return the URL content
	 */
	public static <T> T load(URL url, String method, String parameter, int timeout, TypeReference<T> typeRef)
	{
		return load(new JSONURLLoaderThread<T>(url, method, parameter, timeout, typeRef));
	}

	/**
	 * Load an URL with an {@link URLLoaderThread}<br>
	 * 
	 * @param urlLoaderThread - the {@link URLLoaderThread} to use
	 * @return the URL content
	 */
	public static <T> T load(URLLoaderThread<T> urlLoaderThread)
	{
		urlLoaderThread.async(null);
		try
		{
			urlLoaderThread.join2();
		}
		catch(InterruptedException e)
		{
			logger.error("URLLoaderThread has been interrupted!");
			return null;
		}
		return urlLoaderThread.get();
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
			sb.append(encodeParameter(param.getValue()));
			if(paramIterator.hasNext())
				sb.append("&");
		}

		return sb.toString();
	}

	/**
	 * Encode a parameter String for HTTP POSTs.<br>
	 * Thanks to ulli <a href="https://play.google.com/store/apps/developer?id=Ulrich+Obst">(ulli @ google play)</a>
	 * 
	 * @param s - the parameter String to encode
	 * @return the encoded parameters
	 */
	public static String encodeParameter(String s)
	{
		StringBuffer sb = new StringBuffer();

		char c;
		for(int i = 0; i < s.length(); i++)
		{
			c = s.charAt(i);
			if(c < 0x10)
				sb.append("%0").append(Integer.toHexString(c).toUpperCase());
			else if(c < 0x30 || c > 0x7F)
				sb.append('%').append(Integer.toHexString(c).toUpperCase());
			else
				sb.append(c);
		}
		return sb.toString();
	}
}
