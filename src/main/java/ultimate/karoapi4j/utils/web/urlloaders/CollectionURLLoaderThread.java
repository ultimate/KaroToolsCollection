package ultimate.karoapi4j.utils.web.urlloaders;

import java.net.URL;
import java.util.Collection;

import com.fasterxml.jackson.core.type.TypeReference;

import ultimate.karoapi4j.utils.JSONUtil;
import ultimate.karoapi4j.utils.web.URLLoaderThread;

/**
 * Implementation of URLLoaderThread for Collections
 * 
 * @author ultimate
 * @param <C> - the Collection-Type
 */
public class CollectionURLLoaderThread<C extends Collection<?>> extends URLLoaderThread<C>
{
	/**
	 * @see URLLoaderThread#URLLoaderThread(URL)
	 */
	public CollectionURLLoaderThread(URL url)
	{
		super(url);
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, int)
	 */
	public CollectionURLLoaderThread(URL url, int timeout)
	{
		super(url, timeout);
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, String)
	 */
	public CollectionURLLoaderThread(URL url, String parameter)
	{
		super(url, parameter);
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, String, int)
	 */
	public CollectionURLLoaderThread(URL url, String parameter, int timeout)
	{
		super(url, parameter, timeout);
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, String, String, int)
	 */
	public CollectionURLLoaderThread(URL url, String method, String parameter, int timeout)
	{
		super(url, method, parameter, timeout);
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.web.URLLoader#parse(java.lang.String)
	 */
	@Override
	public C parse(String refreshed)
	{
		return JSONUtil.deserialize(refreshed, new TypeReference<C>() {});
	}
}
