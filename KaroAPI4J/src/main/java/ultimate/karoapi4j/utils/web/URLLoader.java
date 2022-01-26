package ultimate.karoapi4j.utils.web;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.enums.EnumContentType;
import ultimate.karoapi4j.utils.JSONUtil;

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
 * <li>async using a {@link CompletableFuture}:<br>
 * <code>CompletableFuture<?> cf = CompletableFuture.supplyAsync(urlLoader.doGet(...), executor);<br>
 * cf.whenComplete((result, ex) -> { &#47;* process result *&#47; });</code></li>
 * <li></li>
 * </ul>
 * 
 * @author ultimate
 */
public class URLLoader
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger		logger			= LoggerFactory.getLogger(getClass());

	public static final char				DELIMITER		= '/';
	public static final char				PARAMETER		= '?';

	public static final Map<String, String>	POST_PROPERTIES	= new HashMap<>();

	static
	{
		POST_PROPERTIES.put("Content-Type", "application/x-www-form-urlencoded");
	}

	protected URLLoader				parent;
	/**
	 * The URL to load
	 */
	protected String				url;
	protected Map<String, String>	requestProperties;

	public URLLoader(String url)
	{
		this(null, url, null);
	}

	protected URLLoader(String url, Map<String, String> requestProperties)
	{
		this(null, url, requestProperties);
	}

	protected URLLoader(URLLoader parent, String url, Map<String, String> requestProperties)
	{
		super();
		this.parent = parent;
		this.url = url;
		if(requestProperties != null)
			this.requestProperties = requestProperties;
		else
			this.requestProperties = new HashMap<>();
	}

	public URLLoader getParent()
	{
		return parent;
	}

	public String getUrl()
	{
		return url;
	}

	public void addRequestProperty(String key, String value)
	{
		this.requestProperties.put(key, value);
	}

	public Map<String, String> getRequestProperties()
	{
		Map<String, String> properties = new HashMap<>();
		if(this.parent != null)
			properties.putAll(this.parent.getRequestProperties());
		if(this.requestProperties != null)
			properties.putAll(this.requestProperties);
		return properties;
	}

	public URLLoader parameterize(Map<String, String> parameters)
	{
		return parameterize(parameters, null);
	}

	public URLLoader parameterize(Map<String, String> parameters, Map<String, String> requestProperties)
	{
		if(parameters != null && parameters.size() > 0)
			return parameterize(formatParameters(parameters, EnumContentType.text), requestProperties);
		else
			return parameterize((String) null, requestProperties);
	}

	public URLLoader parameterize(String parameters)
	{
		return parameterize(parameters, null);
	}

	public URLLoader parameterize(String parameters, Map<String, String> requestProperties)
	{
		if(parameters == null || parameters.length() == 0)
			return this;

		StringBuilder parURL = new StringBuilder();
		if(this.url.charAt(this.url.length() - 1) == DELIMITER)
			parURL.append(this.url.substring(0, this.url.length() - 1));
		else
			parURL.append(this.url);

		if(parameters.charAt(0) != PARAMETER)
			parURL.append(PARAMETER);

		parURL.append(parameters);

		return new URLLoader(this, parURL.toString(), requestProperties);
	}

	public URLLoader relative(String path)
	{
		return relative(path, null);
	}

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

		return new URLLoader(this, relURL.toString(), requestProperties);
	}

	public URLLoader replace(String target, Object replacement)
	{
		return replace(target, replacement, null);
	}

	public URLLoader replace(String target, String replacement)
	{
		return replace(target, replacement, null);
	}

	public URLLoader replace(String target, Object replacement, Map<String, String> requestProperties)
	{
		return replace(target, (replacement != null ? replacement.toString() : null));
	}

	public URLLoader replace(String target, String replacement, Map<String, String> requestProperties)
	{
		String newURL = this.url.replace(target, replacement);
		return new URLLoader(this.parent, newURL, requestProperties);
	}

	final String doLoad(HttpURLConnection connection, String method, Map<String, String> additionalRequestProperties, String output) throws IOException
	{
		StringBuilder result = new StringBuilder();
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setAllowUserInteraction(false);
		connection.setRequestMethod(method);

		Map<String, String> prop = getRequestProperties();
		if(additionalRequestProperties != null)
			prop.putAll(additionalRequestProperties);
		for(Entry<String, String> reqProp : prop.entrySet())
			connection.setRequestProperty(reqProp.getKey(), reqProp.getValue());

		if(logger.isDebugEnabled())
			logger.debug(method + " " + url + " -> " + output);

		if(output != null)
		{
			connection.setDoOutput(true);
			PrintWriter out = new PrintWriter(connection.getOutputStream());
			out.print(output);
			out.close();
		}

		connection.connect();
		InputStream is = connection.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(is);
		int curr = bis.read();
		while(curr != -1)
		{
			result.append((char) curr);
			curr = bis.read();
		}
		bis.close();
		is.close();

		return result.toString();
	}

	/**
	 * Convenience for <code>doPost(null, parser);</code>
	 *
	 * @see URLLoader#doPost(String, Parser)
	 * @param <T> - the type of content to load
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link CompletableFuture} that can be used to load the content
	 */
	public <T> BackgroundLoader<T> doPost(Parser<String, T> parser)
	{
		return doPost((String) null, parser);
	}

	/**
	 * Create a {@link URLLoaderThread} for a post to the URL represented by this {@link URLLoader}.<br>
	 * Use {@link URLLoaderThread#doBlocking()} or {@link URLLoaderThread#doAsync(java.util.function.Consumer)} to execute the
	 * call.
	 *
	 * @param <T> - the type of content to load
	 * @param output - the output to write
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link CompletableFuture} that can be used to load the content
	 */
	public <T> BackgroundLoader<T> doPost(String output, Parser<String, T> parser)
	{
		return new BackgroundLoader<T>("POST", POST_PROPERTIES, output, parser);
	}

	/**
	 * Convenience for
	 * <code>doPost(URLLoader.formatParameters(parameters), parser);</code>
	 * 
	 * @see URLLoader#doPost(String, Parser)
	 * @param <T> - the type of content to load
	 * @param parameters - the parameters to write
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link CompletableFuture} that can be used to load the content
	 */
	public <T> BackgroundLoader<T> doPost(Map<String, String> parameters, EnumContentType contentType, Parser<String, T> parser)
	{
		if(parameters != null)
			return doPost(formatParameters(parameters, contentType), parser);
		else
			return doPost((String) null, parser);
	}

	/**
	 * Convenience for <code>doGet(null, parser);</code>
	 *
	 * @param <T> - the type of content to load
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link CompletableFuture} that can be used to load the content
	 */
	public <T> BackgroundLoader<T> doGet(Parser<String, T> parser)
	{
		return doGet((String) null, parser);
	}

	/**
	 * Create a {@link URLLoaderThread} for a post to the URL represented by this {@link URLLoader}.<br>
	 * Use {@link URLLoaderThread#doBlocking()} or {@link URLLoaderThread#doAsync(java.util.function.Consumer)} to execute the
	 * call.
	 *
	 * @param <T> - the type of content to load
	 * @param parameters - the arguments to append to the url
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link CompletableFuture} that can be used to load the content
	 */
	public <T> BackgroundLoader<T> doGet(String parameters, Parser<String, T> parser)
	{
		return this.parameterize(parameters).new BackgroundLoader<>("GET", null, null, parser);
	}

	/**
	 * Convenience for
	 * <code>doGet(URLLoader.formatParameters(parameters), parser);</code>
	 * 
	 * @see URLLoader#doGet(String, Parser)
	 * @param <T> - the type of content to load
	 * @param parameters - the parameters to append to the url
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link CompletableFuture} that can be used to load the content
	 */
	public <T> BackgroundLoader<T> doGet(Map<String, String> parameters, Parser<String, T> parser)
	{
		return this.parameterize(parameters).new BackgroundLoader<>("GET", null, null, parser);
	}

	/**
	 * Convenience for <code>doPut(null, parser);</code>
	 *
	 * @see URLLoader#doPut(String, Parser)
	 * @param <T> - the type of content to load
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link CompletableFuture} that can be used to load the content
	 */
	public <T> BackgroundLoader<T> doPut(Parser<String, T> parser)
	{
		return doPut((String) null, parser);
	}

	/**
	 * Create a {@link URLLoaderThread} for a put to the URL represented by this {@link URLLoader}.<br>
	 * Use {@link URLLoaderThread#doBlocking()} or {@link URLLoaderThread#doAsync(java.util.function.Consumer)} to execute the
	 * call.
	 *
	 * @param <T> - the type of content to load
	 * @param output - the output to write
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link CompletableFuture} that can be used to load the content
	 */
	public <T> BackgroundLoader<T> doPut(String output, Parser<String, T> parser)
	{
		return new BackgroundLoader<T>("PUT", null, output, parser);
	}

	/**
	 * Convenience for
	 * <code>doPut(URLLoader.formatParameters(parameters), parser);</code>
	 * 
	 * @see URLLoader#doPut(String, Parser)
	 * @param <T> - the type of content to load
	 * @param parameters - the parameters to write
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link CompletableFuture} that can be used to load the content
	 */
	public <T> BackgroundLoader<T> doPut(Map<String, String> parameters, EnumContentType contentType, Parser<String, T> parser)
	{
		if(parameters != null)
			return doPut(formatParameters(parameters, contentType), parser);
		else
			return doPut((String) null, parser);
	}

	/**
	 * Convenience for <code>doDelete(null, parser);</code>
	 *
	 * @see URLLoader#doDelete(String, Parser)
	 * @param <T> - the type of content to load
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link CompletableFuture} that can be used to load the content
	 */
	public <T> BackgroundLoader<T> doDelete(Parser<String, T> parser)
	{
		return doDelete((String) null, parser);
	}

	/**
	 * Create a {@link URLLoaderThread} for a delete to the URL represented by this {@link URLLoader}.<br>
	 * Use {@link URLLoaderThread#doBlocking()} or {@link URLLoaderThread#doAsync(java.util.function.Consumer)} to execute the
	 * call.
	 *
	 * @param <T> - the type of content to load
	 * @param output - the output to write
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link CompletableFuture} that can be used to load the content
	 */
	public <T> BackgroundLoader<T> doDelete(String output, Parser<String, T> parser)
	{
		return new BackgroundLoader<T>("DELETE", null, output, parser);
	}

	/**
	 * Convenience for
	 * <code>doDelete(URLLoader.formatParameters(parameters), parser);</code>
	 * 
	 * @see URLLoader#doDelete(String, Parser)
	 * @param <T> - the type of content to load
	 * @param parameters - the parameters to write
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link CompletableFuture} that can be used to load the content
	 */
	public <T> BackgroundLoader<T> doDelete(Map<String, String> parameters, EnumContentType contentType, Parser<String, T> parser)
	{
		if(parameters != null)
			return doDelete(formatParameters(parameters, contentType), parser);
		else
			return doDelete((String) null, parser);
	}

	public class BackgroundLoader<T> implements Callable<T>, Supplier<T>
	{
		private String				method;
		private Map<String, String>	requestProperties;
		private String				output;
		private Parser<String, T>	parser;
		private String				rawResult;
		private T					result;
		private HttpURLConnection	connection;

		public BackgroundLoader(String method, Map<String, String> additionalRequestProperties, String output, Parser<String, T> parser)
		{
			super();
			this.method = method;
			this.requestProperties = new HashMap<>();
			if(additionalRequestProperties != null)
				this.requestProperties.putAll(additionalRequestProperties);
			this.output = output;
			this.parser = parser;
		}

		public String getUrl()
		{
			return url;
		}

		public String getMethod()
		{
			return method;
		}

		public Map<String, String> getRequestProperties()
		{
			return requestProperties;
		}

		public String getOutput()
		{
			return output;
		}

		public Parser<String, T> getParser()
		{
			return parser;
		}

		public void addRequestProperties(Map<String, String> additionalRequestProperties)
		{
			this.requestProperties.putAll(additionalRequestProperties);
		}

		@Override
		public T call() throws IOException
		{
			this.connection = (HttpURLConnection) new URL(url).openConnection();
			this.rawResult = doLoad(connection, this.method, this.requestProperties, this.output);
			this.result = this.parser.parse(this.rawResult);
			return this.result;
		}

		public HttpURLConnection getConnection()
		{
			return connection;
		}

		public String getRaw()
		{
			return this.rawResult;
		}

		@Override
		public T get()
		{
			try
			{
				return call();
			}
			catch(IOException e)
			{
				return null;
			}
		}
	}

	/**
	 * Format a Map of parameters to a String in the format<br>
	 * <ul>
	 * <li>text: <code>param1=value1&param2=value2&...</code></li>
	 * <li>or</li>
	 * <li>json: <code>param1=value1&param2=value2&...</code></li>
	 * </ul>
	 * 
	 * @param parameters - the Map of parameters
	 * @param
	 * @return the formatted String
	 */
	public static String formatParameters(Map<String, String> parameters, EnumContentType contentType)
	{
		if(contentType == EnumContentType.json)
		{
			return JSONUtil.serialize(parameters);
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			Entry<String, String> param;
			Iterator<Entry<String, String>> paramIterator = parameters.entrySet().iterator();
			while(paramIterator.hasNext())
			{
				param = paramIterator.next();
				sb.append(param.getKey());
				sb.append("=");
				sb.append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8));
				if(paramIterator.hasNext())
					sb.append("&");
			}
			return sb.toString();
		}

	}
}
