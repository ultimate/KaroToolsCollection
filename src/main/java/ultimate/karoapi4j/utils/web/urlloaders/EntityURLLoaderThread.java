package ultimate.karoapi4j.utils.web.urlloaders;

import java.net.URL;

import ultimate.karoapi4j.utils.JSONUtil;
import ultimate.karoapi4j.utils.web.URLLoaderThread;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Implementation of URLLoaderThread for arbitrary Entities
 * 
 * @author ultimate
 * @param <E> - the Entity Type
 */
public class EntityURLLoaderThread<E> extends URLLoaderThread<E>
{
	/**
	 * @see URLLoaderThread#URLLoaderThread(URL)
	 */
	public EntityURLLoaderThread(URL url)
	{
		super(url);
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, int)
	 */
	public EntityURLLoaderThread(URL url, int timeout)
	{
		super(url, timeout);
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, String)
	 */
	public EntityURLLoaderThread(URL url, String parameter)
	{
		super(url, parameter);
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, String, int)
	 */
	public EntityURLLoaderThread(URL url, String parameter, int timeout)
	{
		super(url, parameter, timeout);
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, String, String, int)
	 */
	public EntityURLLoaderThread(URL url, String method, String parameter, int timeout)
	{
		super(url, method, parameter, timeout);
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.web.URLLoader#parse(java.lang.String)
	 */
	@Override
	public E parse(String refreshed)
	{
		return JSONUtil.deserialize(refreshed, new TypeReference<E>() {});
	}
}
