package ultimate.karoapi4j.utils.web.urlloaders;

import java.net.URL;

import ultimate.karoapi4j.utils.web.URLLoaderThread;

/**
 * Implementation of URLLoaderThread for Strings
 * 
 * @author ultimate
 */
public class StringURLLoaderThread extends URLLoaderThread<String>
{
	/**
	 * @see URLLoaderThread#URLLoaderThread(URL)
	 */
	public StringURLLoaderThread(URL url)
	{
		super(url);
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, int)
	 */
	public StringURLLoaderThread(URL url, int timeout)
	{
		super(url, timeout);
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, String)
	 */
	public StringURLLoaderThread(URL url, String parameter)
	{
		super(url, parameter);
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, String, int)
	 */
	public StringURLLoaderThread(URL url, String parameter, int timeout)
	{
		super(url, parameter, timeout);
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, String, String, int)
	 */
	public StringURLLoaderThread(URL url, String method, String parameter, int timeout)
	{
		super(url, method, parameter, timeout);
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.web.URLLoader#parse(java.lang.String)
	 */
	@Override
	public String parse(String refreshed)
	{
		return refreshed;
	}
}
