package ultimate.karoapi4j.utils.web.urlloaders;

import java.net.URL;

import ultimate.karoapi4j.utils.JSONUtil;
import ultimate.karoapi4j.utils.web.URLLoaderThread;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Implementation of URLLoaderThread for arbitrary JSON-Entities<br>
 * 
 * @see JSONUtil#deserialize(String, TypeReference)
 * @author ultimate
 * @param <E> - the Entity Type
 */
public class JSONURLLoaderThread<E> extends URLLoaderThread<E>
{
	/**
	 * A TypeReference for JSON-Deserialization
	 */
	protected TypeReference<E>	typeRef	= new TypeReference<E>() {};

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL)
	 */
	public JSONURLLoaderThread(URL url)
	{
		super(url);
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, int)
	 */
	public JSONURLLoaderThread(URL url, int timeout)
	{
		super(url, timeout);
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, String)
	 */
	public JSONURLLoaderThread(URL url, String parameter)
	{
		super(url, parameter);
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, String, int)
	 */
	public JSONURLLoaderThread(URL url, String parameter, int timeout)
	{
		super(url, parameter, timeout);
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, String, String, int)
	 */
	public JSONURLLoaderThread(URL url, String method, String parameter, int timeout)
	{
		super(url, method, parameter, timeout);
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL)
	 * @param typeRef - a TypeReference for JSON-Deserialization
	 */
	public JSONURLLoaderThread(URL url, TypeReference<E> typeRef)
	{
		this(url);
		if(typeRef == null)
			throw new IllegalArgumentException("typeRef must not be null!");
		this.typeRef = typeRef;
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, int)
	 * @param typeRef - a TypeReference for JSON-Deserialization
	 */
	public JSONURLLoaderThread(URL url, int timeout, TypeReference<E> typeRef)
	{
		this(url, timeout);
		if(typeRef == null)
			throw new IllegalArgumentException("typeRef must not be null!");
		this.typeRef = typeRef;
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, String)
	 * @param typeRef - a TypeReference for JSON-Deserialization
	 */
	public JSONURLLoaderThread(URL url, String parameter, TypeReference<E> typeRef)
	{
		this(url, parameter);
		if(typeRef == null)
			throw new IllegalArgumentException("typeRef must not be null!");
		this.typeRef = typeRef;
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, String, int)
	 * @param typeRef - a TypeReference for JSON-Deserialization
	 */
	public JSONURLLoaderThread(URL url, String parameter, int timeout, TypeReference<E> typeRef)
	{
		this(url, parameter, timeout);
		if(typeRef == null)
			throw new IllegalArgumentException("typeRef must not be null!");
		this.typeRef = typeRef;
	}

	/**
	 * @see URLLoaderThread#URLLoaderThread(URL, String, String, int)
	 * @param typeRef - a TypeReference for JSON-Deserialization
	 */
	public JSONURLLoaderThread(URL url, String method, String parameter, int timeout, TypeReference<E> typeRef)
	{
		this(url, method, parameter, timeout);
		if(typeRef == null)
			throw new IllegalArgumentException("typeRef must not be null!");
		this.typeRef = typeRef;
	}

	/**
	 * The TypeReference for JSON-Deserialization
	 * 
	 * @return typeRef
	 */
	public TypeReference<E> getTypeReference()
	{
		return typeRef;
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.web.URLLoader#parse(java.lang.String)
	 */
	@Override
	public E parse(String refreshed)
	{
		return JSONUtil.deserialize(refreshed, typeRef);
	}
}
