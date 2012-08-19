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
 * @param <E> - the Collection-Entry-Type
 */
public class CollectionURLLoaderThread<E> extends URLLoaderThread<Collection<E>>
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
	public Collection<E> parse(String refreshed)
	{
		return JSONUtil.deserialize(refreshed, new TypeReference<Collection<E>>() {});
	}
}
