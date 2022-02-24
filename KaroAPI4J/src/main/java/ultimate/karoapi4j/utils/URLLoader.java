package ultimate.karoapi4j.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.enums.EnumContentType;
import ultimate.karoapi4j.utils.JSONUtil.Parser;

/**
 * This class provides abstraction to load and parse urls synchronously or asynchronously. Instances of this class can be reused and they can be used
 * to derive sub-loaders for sub-urls or passed parameters using <code>URLLoader#relative(...)</code> or <code>URLLoader#parameterize(...)</code>.
 * When the desired URL has been constructed, a call to one of the do*-method is required to get a {@link BackgroundLoader}. This
 * {@link BackgroundLoader} then can be used to either load the results blocking or asynchronously.<br>
 * <br>
 * For example:
 * <ul>
 * <li>blocking in the main thread by simply calling {@link Supplier#get()}:<br>
 * <code>urlLoader.doGet(...).get();</code></li>
 * <li>blocking in a separate thread by use of a {@link FutureTask}:<br>
 * <code>FutureTask<?> ft = new FutureTask<?>(urlLoader.doGet(...));<br>
 * executor.execute(ft);
 * result = ft.get();</code></li>
 * <li>async using a {@link BackgroundLoader}:<br>
 * <code>BackgroundLoader<?> cf = BackgroundLoader.supplyAsync(urlLoader.doGet(...), executor);<br>
 * cf.whenComplete((result, ex) -> { &#47;* process result *&#47; });</code></li>
 * </ul>
 * Note: alternatively {@link URLLoader#doLoad(HttpURLConnection, String, Map, String, String)} can also be used statically
 * 
 * @author ultimate
 */
public class URLLoader
{
	/////////////////
	// static part //
	/////////////////
	/**
	 * Logger-Instance
	 */
	protected static transient final Logger logger = LogManager.getLogger();

	/**
	 * Constant for use in {@link URLLoader#relative(String)}
	 */
	public static final char				DELIMITER					= '/';
	/**
	 * Constant for use in {@link URLLoader#parameterize(String)}
	 */
	public static final char				PARAMETER					= '?';
	/**
	 * Constant for use in {@link URLLoader#parameterize(String)}
	 */
	public static final char				PARAMETER_CONTINUED			= '&';
	/**
	 * Encoding charset used: UTF-8
	 */
	public static final String				DEFAULT_CHARSET				= "UTF-8";
	/**
	 * The request properties that need to be passed for URL-encoded posts
	 */
	public static final Map<String, String>	POST_PROPERTIES_URL_ENCODED	= new HashMap<>();
	/**
	 * The request properties that need to be passed for JSON-encoded posts
	 */
	public static final Map<String, String>	POST_PROPERTIES_JSON		= new HashMap<>();

	/**
	 * init some constants
	 */
	static
	{
		POST_PROPERTIES_URL_ENCODED.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		POST_PROPERTIES_JSON.put("Content-Type", "application/json; charset=utf-8");
	}

	/**
	 * Format a Map of parameters to a String in the format<br>
	 * <ul>
	 * <li>text: <code>param1=value1&param2=value2&...</code></li>
	 * <li>or</li>
	 * <li>json: <code>{"param1":"value1","param2":"value2",...}</code></li>
	 * </ul>
	 * 
	 * @param parameters - the Map of parameters
	 * @param
	 * @return the formatted String
	 */
	public static String formatParameters(Map<String, Object> parameters, EnumContentType contentType)
	{
		if(contentType == EnumContentType.json)
		{
			return JSONUtil.serialize(parameters);
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			Entry<String, Object> param;
			Iterator<Entry<String, Object>> paramIterator = parameters.entrySet().iterator();
			while(paramIterator.hasNext())
			{
				param = paramIterator.next();
				sb.append(param.getKey());
				sb.append("=");
				sb.append(URLEncoder.encode("" + param.getValue(), StandardCharsets.UTF_8));
				if(paramIterator.hasNext())
					sb.append("&");
			}
			return sb.toString();
		}

	}

	/**
	 * Generic method that is used by all following operations to load the URL when {@link BackgroundLoader#call()} or {@link BackgroundLoader#get()}
	 * is called. Can also be used statically.
	 * 
	 * @param connection - the HttpURLConnection to use
	 * @param method - the HTTP method to use
	 * @param requestProperties - the optional request properties to pass to the connection
	 * @param output - the optional output to post to the connection
	 * @param charset - the charset to use
	 * @return the String loaded from the connection
	 * @throws IOException
	 */
	public static String doLoad(HttpURLConnection connection, String method, Map<String, String> requestProperties, String output, String charset) throws IOException
	{
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setAllowUserInteraction(false);
		connection.setRequestMethod(method);

		if(requestProperties != null)
			for(Entry<String, String> reqProp : requestProperties.entrySet())
				connection.setRequestProperty(reqProp.getKey(), reqProp.getValue());

		if(logger.isDebugEnabled())
		{
			logger.debug(method + " " + connection.getURL() + " -> " + output);
			for(Entry<String, List<String>> rqp : connection.getRequestProperties().entrySet())
				logger.trace(" - " + rqp.getKey() + " = " + rqp.getValue());
		}

		if(output != null)
		{
			connection.setDoOutput(true);
			PrintWriter out = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), charset));
			out.print(output);
			out.flush();
			out.close();
		}

		connection.connect();
		InputStream is = connection.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(is);
		String result = new String(bis.readAllBytes(), charset);

		if(logger.isDebugEnabled())
		{
			logger.debug(method + " " + connection.getURL() + " = " + connection.getResponseCode() + " " + connection.getResponseMessage());
			for(Entry<String, List<String>> hf : connection.getHeaderFields().entrySet())
				logger.trace(" - " + hf.getKey() + " = " + hf.getValue());
			logger.trace(" = " + result);
		}

		return result;
	}

	/////////////////
	// member part //
	/////////////////

	/**
	 * The parent URL loader
	 */
	protected URLLoader				parent;
	/**
	 * The URL to load
	 */
	protected String				url;
	/**
	 * The optional request properties to use
	 * 
	 * @see URLConnection#setRequestProperty(String, String)
	 */
	protected Map<String, String>	requestProperties;
	/**
	 * The charset to use
	 */
	protected String				charset;

	/**
	 * Create a new {@link URLLoader} for the given URL with the {@link URLLoader#DEFAULT_CHARSET}
	 * 
	 * @param url - the URL
	 */
	public URLLoader(String url)
	{
		this(null, url, null, DEFAULT_CHARSET);
	}

	/**
	 * Create a new {@link URLLoader} for the given URL and the given {@link Charset}
	 * 
	 * @param url - the URL
	 * @param charset - the charset name
	 */
	public URLLoader(String url, String charset)
	{
		this(null, url, null, charset);
	}

	/**
	 * Create a new {@link URLLoader} for the given URL and the given request properties and the given {@link Charset}
	 * 
	 * @see URLConnection#setRequestProperty(String, String)
	 * @param url - the URL
	 * @param requestProperties - the optional request properties to use
	 * @param charset - the charset name
	 */
	protected URLLoader(String url, Map<String, String> requestProperties, String charset)
	{
		this(null, url, requestProperties, charset);
	}

	/**
	 * Create a new {@link URLLoader} for the given parent, the given URL and the given request properties and the given {@link Charset}
	 * 
	 * @see URLConnection#setRequestProperty(String, String)
	 * @param parent - the parent URL loader
	 * @param url - the URL
	 * @param requestProperties - the optional request properties to use
	 * @param charset - the charset name
	 */
	protected URLLoader(URLLoader parent, String url, Map<String, String> requestProperties, String charset)
	{
		super();
		this.parent = parent;
		this.url = url;
		this.charset = charset;
		if(requestProperties != null)
			this.requestProperties = requestProperties;
		else
			this.requestProperties = new HashMap<>();
	}

	/**
	 * The parent URL loader
	 * 
	 * @return the parent
	 */
	public URLLoader getParent()
	{
		return parent;
	}

	/**
	 * The URL to load
	 * 
	 * @return the url
	 */
	public String getUrl()
	{
		return url;
	}

	/**
	 * Add a request properties to use
	 * 
	 * @see URLConnection#setRequestProperty(String, String)
	 * @param key - the key
	 * @param value - the value
	 */
	public void addRequestProperty(String key, String value)
	{
		this.requestProperties.put(key, value);
	}

	/**
	 * Get all request properties set
	 * 
	 * @see URLConnection#setRequestProperty(String, String)
	 * @return the map of request properties
	 */
	public Map<String, String> getRequestProperties()
	{
		Map<String, String> properties = new HashMap<>();
		if(this.parent != null)
			properties.putAll(this.parent.getRequestProperties());
		if(this.requestProperties != null)
			properties.putAll(this.requestProperties);
		return properties;
	}

	///////////////////////////////////////////////
	// functions for creating derived URLLoaders //
	///////////////////////////////////////////////

	/**
	 * The charset to use
	 * 
	 * @return the charset name
	 */
	public String getCharset()
	{
		return charset;
	}

	/**
	 * Create a new URLLoader by parameterizing the current URLLoader with the given parameters
	 * 
	 * @param parameters - the parameter map to use
	 * @return the new URLLoader
	 */
	public URLLoader parameterize(Map<String, Object> parameters)
	{
		return parameterize(parameters, null);
	}

	/**
	 * Create a new URLLoader by parameterizing the current URLLoader with the given parameters and optional request properties
	 * 
	 * @see URLLoader#getRequestProperties()
	 * @see URLConnection#setRequestProperty(String, String)
	 * @param parameters - the parameter map to use
	 * @param requestProperties - the optional request properties
	 * @return the new URLLoader
	 */
	public URLLoader parameterize(Map<String, Object> parameters, Map<String, String> requestProperties)
	{
		if(parameters != null && parameters.size() > 0)
			return parameterize(formatParameters(parameters, EnumContentType.text), requestProperties);
		else
			return parameterize((String) null, requestProperties);
	}

	/**
	 * Create a new URLLoader by parameterizing the current URLLoader with the given parameters
	 * 
	 * @param parameters - the parameter string to use
	 * @return the new URLLoader
	 */
	public URLLoader parameterize(String parameters)
	{
		return parameterize(parameters, null);
	}

	/**
	 * Create a new URLLoader by parameterizing the current URLLoader with the given parameters and optional request properties
	 * 
	 * @see URLLoader#getRequestProperties()
	 * @see URLConnection#setRequestProperty(String, String)
	 * @param parameters - the parameter string to use
	 * @param requestProperties - the optional request properties
	 * @return the new URLLoader
	 */
	public URLLoader parameterize(String parameters, Map<String, String> requestProperties)
	{
		if(parameters == null || parameters.length() == 0)
			return this;

		StringBuilder parURL = new StringBuilder();
		// check here if this URLLoader was already parameterized, so another parameterization with ? does not make sense
		if(!this.url.contains("" + PARAMETER))
		{
			// default case (not yet parameterized)
			if(this.url.charAt(this.url.length() - 1) == DELIMITER)
				parURL.append(this.url.substring(0, this.url.length() - 1));
			else
				parURL.append(this.url);

			if(parameters.charAt(0) != PARAMETER)
				parURL.append(PARAMETER);

			parURL.append(parameters);
		}
		else
		{
			// special case (already parameterized) --> append parameters with & instead of ?
			parURL.append(this.url);
			if(parameters.charAt(0) == PARAMETER)
			{
				parURL.append(PARAMETER_CONTINUED);
				parURL.append(parameters.substring(1));
			}
			else
			{
				if(parameters.charAt(0) != PARAMETER_CONTINUED)
					parURL.append(PARAMETER_CONTINUED);
				parURL.append(parameters);
			}
		}

		return new URLLoader(this, parURL.toString(), requestProperties, this.charset);
	}

	/**
	 * Create a new URLLoader with a relative path to current URLLoader
	 * 
	 * @param path - the relative path
	 * @return the new URLLoader
	 */
	public URLLoader relative(String path)
	{
		return relative(path, null);
	}

	/**
	 * Create a new URLLoader with a relative path to current URLLoader and optional request properties
	 * 
	 * @see URLLoader#getRequestProperties()
	 * @see URLConnection#setRequestProperty(String, String)
	 * @param path - the relative path
	 * @param requestProperties - the optional request properties
	 * @return the new URLLoader
	 */
	public URLLoader relative(String path, Map<String, String> requestProperties)
	{
		if(path == null || path.length() == 0)
			return this;

		StringBuilder relURL = new StringBuilder(this.url);

		int delims = 0;
		if(path.charAt(0) == DELIMITER)
			delims++;
		if(url.charAt(url.length() - 1) == DELIMITER)
			delims++;

		switch(delims)
		{
			case 0:
				relURL.append(DELIMITER);
			case 1:
				relURL.append(path);
				break;
			case 2:
				relURL.append(path.substring(1));
		}

		return new URLLoader(this, relURL.toString(), requestProperties, this.charset);
	}

	/**
	 * Create a new URLLoader by replacing parts of the URL
	 * 
	 * @param target - the string part to replace
	 * @param replacement - the replacement
	 * @return the new URLLoader
	 */
	public URLLoader replace(String target, Object replacement)
	{
		return replace(target, replacement, null);
	}

	/**
	 * Create a new URLLoader by replacing parts of the URL
	 * 
	 * @param target - the string part to replace
	 * @param replacement - the replacement
	 * @return the new URLLoader
	 */
	public URLLoader replace(String target, String replacement)
	{
		return replace(target, replacement, null);
	}

	/**
	 * Create a new URLLoader by replacing parts of the URL and optional request properties
	 * 
	 * @see URLLoader#getRequestProperties()
	 * @see URLConnection#setRequestProperty(String, String)
	 * @param target - the string part to replace
	 * @param replacement - the replacement
	 * @param requestProperties - the optional request properties
	 * @return the new URLLoader
	 */
	public URLLoader replace(String target, Object replacement, Map<String, String> requestProperties)
	{
		return replace(target, (replacement != null ? replacement.toString() : null));
	}

	/**
	 * Create a new URLLoader by replacing parts of the URL and optional request properties
	 * 
	 * @see URLLoader#getRequestProperties()
	 * @see URLConnection#setRequestProperty(String, String)
	 * @param target - the string part to replace
	 * @param replacement - the replacement
	 * @param requestProperties - the optional request properties
	 * @return the new URLLoader
	 */
	public URLLoader replace(String target, String replacement, Map<String, String> requestProperties)
	{
		String newURL = this.url.replace(target, replacement);
		return new URLLoader(this.parent, newURL, requestProperties, this.charset);
	}

	//////////////////////////////////////////////////////////////////////////////
	// functions performing operations on the URL (get, post, put, delete, ...) //
	//////////////////////////////////////////////////////////////////////////////

	/**
	 * Convenience for <code>doPost(null);</code>
	 *
	 * @see URLLoader#doPost(String, Parser)
	 * @param <T> - the type of content to load
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link BackgroundLoader} that can be used to load the content
	 */
	public BackgroundLoader doPost()
	{
		return doPost((String) null, EnumContentType.text);
	}

	/**
	 * Create a {@link BackgroundLoader} for a post to the URL represented by this {@link URLLoader}.<br>
	 * Use {@link BackgroundLoader#get()} or {@link BackgroundLoader#call()} to execute the call.
	 *
	 * @param <T> - the type of content to load
	 * @param output - the output to write
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link BackgroundLoader} that can be used to load the content
	 */
	public BackgroundLoader doPost(String output, EnumContentType contentType)
	{
		if(contentType == EnumContentType.json)
			return new BackgroundLoader("POST", POST_PROPERTIES_JSON, output);
		else
			return new BackgroundLoader("POST", POST_PROPERTIES_URL_ENCODED, output);

	}

	/**
	 * Convenience for
	 * <code>doPost(URLLoader.formatParameters(parameters));</code>
	 * 
	 * @see URLLoader#doPost(String, Parser)
	 * @param <T> - the type of content to load
	 * @param parameters - the parameters to write
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link BackgroundLoader} that can be used to load the content
	 */
	public BackgroundLoader doPost(Map<String, Object> parameters, EnumContentType contentType)
	{
		if(parameters != null)
			return doPost(formatParameters(parameters, contentType), contentType);
		else
			return doPost((String) null, contentType);
	}

	/**
	 * Convenience for <code>doGet(null);</code>
	 *
	 * @param <T> - the type of content to load
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link BackgroundLoader} that can be used to load the content
	 */
	public BackgroundLoader doGet()
	{
		return doGet((String) null);
	}

	/**
	 * Create a {@link BackgroundLoader} for a post to the URL represented by this {@link URLLoader}.<br>
	 * Use {@link BackgroundLoader#get()} or {@link BackgroundLoader#call()} to execute the call.
	 *
	 * @param <T> - the type of content to load
	 * @param parameters - the arguments to append to the url
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link BackgroundLoader} that can be used to load the content
	 */
	public BackgroundLoader doGet(String parameters)
	{
		return this.parameterize(parameters).new BackgroundLoader("GET", null, null);
	}

	/**
	 * Convenience for
	 * <code>doGet(URLLoader.formatParameters(parameters));</code>
	 * 
	 * @see URLLoader#doGet(String, Parser)
	 * @param <T> - the type of content to load
	 * @param parameters - the parameters to append to the url
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link BackgroundLoader} that can be used to load the content
	 */
	public BackgroundLoader doGet(Map<String, Object> parameters)
	{
		return this.parameterize(parameters).new BackgroundLoader("GET", null, null);
	}

	/**
	 * Convenience for <code>doPut(null);</code>
	 *
	 * @see URLLoader#doPut(String, Parser)
	 * @param <T> - the type of content to load
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link BackgroundLoader} that can be used to load the content
	 */
	public BackgroundLoader doPut()
	{
		return doPut((String) null);
	}

	/**
	 * Create a {@link BackgroundLoader} for a put to the URL represented by this {@link URLLoader}.<br>
	 * Use {@link BackgroundLoader#get()} or {@link BackgroundLoader#call()} to execute the call.
	 *
	 * @param <T> - the type of content to load
	 * @param output - the output to write
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link BackgroundLoader} that can be used to load the content
	 */
	public BackgroundLoader doPut(String output)
	{
		return new BackgroundLoader("PUT", null, output);
	}

	/**
	 * Convenience for
	 * <code>doPut(URLLoader.formatParameters(parameters));</code>
	 * 
	 * @see URLLoader#doPut(String, Parser)
	 * @param <T> - the type of content to load
	 * @param parameters - the parameters to write
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link BackgroundLoader} that can be used to load the content
	 */
	public BackgroundLoader doPut(Map<String, Object> parameters, EnumContentType contentType)
	{
		if(parameters != null)
			return doPut(formatParameters(parameters, contentType));
		else
			return doPut((String) null);
	}

	/**
	 * Convenience for <code>doDelete(null);</code>
	 *
	 * @see URLLoader#doDelete(String, Parser)
	 * @param <T> - the type of content to load
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link BackgroundLoader} that can be used to load the content
	 */
	public BackgroundLoader doDelete()
	{
		return doDelete((String) null);
	}

	/**
	 * Create a {@link BackgroundLoader} for a delete to the URL represented by this {@link URLLoader}.<br>
	 * Use {@link BackgroundLoader#get()} or {@link BackgroundLoader#call()} to execute the call.
	 *
	 * @param <T> - the type of content to load
	 * @param output - the output to write
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link BackgroundLoader} that can be used to load the content
	 */
	public BackgroundLoader doDelete(String output)
	{
		return new BackgroundLoader("DELETE", null, output);
	}

	/**
	 * Convenience for
	 * <code>doDelete(URLLoader.formatParameters(parameters));</code>
	 * 
	 * @see URLLoader#doDelete(String, Parser)
	 * @param <T> - the type of content to load
	 * @param parameters - the parameters to write
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link BackgroundLoader} that can be used to load the content
	 */
	public BackgroundLoader doDelete(Map<String, Object> parameters, EnumContentType contentType)
	{
		if(parameters != null)
			return doDelete(formatParameters(parameters, contentType));
		else
			return doDelete((String) null);
	}

	/**
	 * Convenience for <code>doPatch(null);</code>
	 *
	 * @see URLLoader#doPatch(String, Parser)
	 * @param <T> - the type of content to load
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link BackgroundLoader} that can be used to load the content
	 */
	@Deprecated(since = "PATCH IS NOT SUPPORTED")
	public BackgroundLoader doPatch()
	{
		// TODO currently PATCH is not supported
		return doPatch((String) null);
	}

	/**
	 * Create a {@link BackgroundLoader} for a patch to the URL represented by this {@link URLLoader}.<br>
	 * Use {@link BackgroundLoader#get()} or {@link BackgroundLoader#call()} to execute the call.
	 *
	 * @param <T> - the type of content to load
	 * @param output - the output to write
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link BackgroundLoader} that can be used to load the content
	 */
	@Deprecated(since = "PATCH IS NOT SUPPORTED")
	public BackgroundLoader doPatch(String output)
	{
		// TODO currently PATCH is not supported
		return new BackgroundLoader("PATCH", null, output);
	}

	/**
	 * Convenience for
	 * <code>doPatch(URLLoader.formatParameters(parameters));</code>
	 * 
	 * @see URLLoader#doPatch(String, Parser)
	 * @param <T> - the type of content to load
	 * @param parameters - the parameters to write
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link BackgroundLoader} that can be used to load the content
	 */
	@Deprecated(since = "PATCH IS NOT SUPPORTED")
	public BackgroundLoader doPatch(Map<String, Object> parameters, EnumContentType contentType)
	{
		// TODO currently PATCH is not supported
		if(parameters != null)
			return doPatch(formatParameters(parameters, contentType));
		else
			return doPatch((String) null);
	}

	/**
	 * This class is a wrapper for the HTTP operation to perform. It is instantiated and returned by the various do* methods:
	 * <ul>
	 * <li>doPost(...)</li>
	 * <li>doGet(...)</li>
	 * <li>doPut(...)</li>
	 * <li>doDelete(...)</li>
	 * <li>doPatch(...)</li>
	 * </ul>
	 * {@link BackgroundLoader} implements {@link Callable} and {@link Supplier} in order to be easily usable
	 * <ul>
	 * <li>blocking,</li>
	 * <li>in {@link Thread}s,</li>
	 * <li>in {@link FutureTask}, or</li>
	 * <li>in {@link CompletableFuture}</li>
	 * </ul>
	 * (See {@link URLLoader} class description for details)
	 * 
	 * @author ultimate
	 */
	public class BackgroundLoader implements Callable<String>, Supplier<String>
	{
		/**
		 * the HTTP method to use
		 */
		private String				method;
		/**
		 * the optional additional request properties to pass to the connection
		 */
		private Map<String, String>	additionalRequestProperties;
		/**
		 * the optional output to post to the connection
		 */
		private String				output;
		/**
		 * the result loaded
		 */
		private String				result;
		/**
		 * the {@link HttpURLConnection} used
		 */
		private HttpURLConnection	connection;

		/**
		 * Create a new BackgroundLoader
		 * 
		 * @param method - the HTTP method to use
		 * @param additionalRequestProperties - the optional additional request properties to pass to the connection<br>
		 *            (Note: request properties already set for the wrapping {@link URLLoader} will be overwritten if keys collide)
		 * @param output - the optional output to post to the connection
		 */
		public BackgroundLoader(String method, Map<String, String> additionalRequestProperties, String output)
		{
			super();
			this.method = method;
			this.additionalRequestProperties = new HashMap<>();
			if(additionalRequestProperties != null)
				this.additionalRequestProperties.putAll(additionalRequestProperties);
			this.output = output;
		}

		/**
		 * The URL represented by the wrapping {@link URLLoader}
		 * 
		 * @return the url
		 */
		public String getUrl()
		{
			return url;
		}

		/**
		 * the HTTP method to use
		 * 
		 * @return the method
		 */
		public String getMethod()
		{
			return this.method;
		}

		/**
		 * the optional additional request properties to pass to the connection
		 * 
		 * @return the request properties map
		 */
		public Map<String, String> getAdditionalRequestProperties()
		{
			return this.additionalRequestProperties;
		}

		/**
		 * the all request properties to pass to the connection (including those from the wrapping URLLoader)
		 * 
		 * @return the request properties map
		 */
		public Map<String, String> getAllRequestProperties()
		{
			Map<String, String> prop = getRequestProperties();
			if(this.additionalRequestProperties != null)
				prop.putAll(this.additionalRequestProperties);
			return prop;
		}

		/**
		 * the optional output to post to the connection
		 * 
		 * @return the output
		 */
		public String getOutput()
		{
			return this.output;
		}

		/**
		 * add more optional request property to this {@link BackgroundLoader}
		 * 
		 * @param additionalRequestProperties - additional request properties
		 */
		public void addRequestProperties(Map<String, String> additionalRequestProperties)
		{
			this.additionalRequestProperties.putAll(additionalRequestProperties);
		}

		/**
		 * Initiate the {@link HttpURLConnection} and load the content using {@link URLLoader#doLoad(HttpURLConnection, String, Map, String, String)}
		 */
		@Override
		public String call() throws IOException
		{
			this.connection = (HttpURLConnection) new URL(url).openConnection();
			this.result = doLoad(this.connection, this.method, this.getAllRequestProperties(), this.output, charset);
			return this.result;
		}

		/**
		 * the {@link HttpURLConnection} used.<br>
		 * Note: the connection is null until {@link BackgroundLoader#call()} is called.
		 * 
		 * @return the connection
		 */
		public HttpURLConnection getConnection()
		{
			return this.connection;
		}

		/**
		 * the result loaded from the URL<br>
		 * Note: the result is null until {@link BackgroundLoader#call()} is called.
		 * 
		 * @return the result
		 */
		public String getResult()
		{
			return this.result;
		}

		/**
		 * Implementation of {@link Supplier}. Will simply forward to {@link BackgroundLoader#call()} and wraps occurring exception into a
		 * {@link RuntimeException} to match the Exception-less method signature
		 */
		@Override
		public String get()
		{
			try
			{
				return call();
			}
			catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
