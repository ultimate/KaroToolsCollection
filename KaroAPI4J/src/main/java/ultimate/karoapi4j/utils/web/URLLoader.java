package ultimate.karoapi4j.utils.web;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ultimate.karoapi4j.utils.threads.QueuableThread;

/**
 * Extension of Loader for URLLoaders.<br>
 * URLLoaders usually load Strings from the given URL, so transforming this String to the required
 * loaded Type is necessary via {@link URLLoader#parse(String)}
 * 
 * @author ultimate
 */
public class URLLoader
{
	public static final char				DELIMETER		= '/';

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
		if(path.charAt(0) == DELIMETER)
			delims++;
		if(url.charAt(url.length() - 1) == DELIMETER)
			delims++;

		switch(delims)
		{
			case 0:
				relURL.append(DELIMETER);
			case 1:
				relURL.append(path);
				break;
			case 2:
				relURL.append(path.substring(1));
		}

		return new URLLoader(this, relURL.toString(), requestProperties);
	}

	final String doLoad(String method, Map<String, String> additionalRequestProperties, String output) throws IOException
	{
		StringBuilder result = new StringBuilder();
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setAllowUserInteraction(false);
		connection.setRequestMethod(method);

		Map<String, String> prop = getRequestProperties();
		if(additionalRequestProperties != null)
			prop.putAll(additionalRequestProperties);
		for(Entry<String, String> reqProp : prop.entrySet())
			connection.setRequestProperty(reqProp.getKey(), reqProp.getValue());

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
	 * Create a {@link URLLoaderThread} for a post to the URL represented by this {@link URLLoader}.<br>
	 * Use {@link URLLoaderThread#doBlocking()} or {@link URLLoaderThread#doAsync(java.util.function.Consumer)} to execute the
	 * call.
	 *
	 * @param <T> - the type of content to load
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link URLLoaderThread}
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
	 * @param parameters - the parameters to post
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link URLLoaderThread}
	 */
	public <T> BackgroundLoader<T> doPost(Map<String, String> parameters, Parser<String, T> parser)
	{
		if(parameters != null)
			return doPost(formatParameters(parameters), parser);
		else
			return doPost((String) null, parser);
	}

	/**
	 * Create a {@link URLLoaderThread} for a post to the URL represented by this {@link URLLoader}.<br>
	 * Use {@link URLLoaderThread#doBlocking()} or {@link URLLoaderThread#doAsync(java.util.function.Consumer)} to execute the
	 * call.
	 *
	 * @param <T> - the type of content to load
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link URLLoaderThread}
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
	 * @param args - the arguments to append to the url
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link URLLoaderThread}
	 */
	public <T> BackgroundLoader<T> doGet(String args, Parser<String, T> parser)
	{
		URLLoader tmp = this;
		if(args != null && args.length() > 0)
			tmp = this.relative(args);
		return tmp.new BackgroundLoader<>("GET", null, null, parser);
	}

	/**
	 * Convenience for
	 * <code>doGet(URLLoader.formatParameters(parameters), parser);</code>
	 * 
	 * @see URLLoader#doGet(String, Parser)
	 * @param <T> - the type of content to load
	 * @param parameters - the parameters to append to the url
	 * @param parser - the {@link Parser} for the result
	 * @return the {@link URLLoaderThread}
	 */
	public <T> BackgroundLoader<T> doGet(Map<String, String> parameters, Parser<String, T> parser)
	{
		if(parameters != null)
			return doGet(formatParameters(parameters), parser);
		else
			return doGet((String) null, parser);
	}

	public class BackgroundLoader<T> extends QueuableThread<T>
	{
		private String				method;
		private Map<String, String>	requestProperties;
		private String				output;
		private Parser<String, T>	parser;
		private String				result;

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

		public void addRequestProperties(Map<String, String> additionalRequestProperties)
		{
			this.requestProperties.putAll(additionalRequestProperties);
		}

		@Override
		public void innerRun()
		{
			try
			{
				this.result = doLoad(this.method, this.requestProperties, this.output);
			}
			catch(IOException e)
			{
				this.exception = e;
			}
		}

		public String getRawResult()
		{
			return this.result;
		}

		@Override
		public T getResult()
		{
			if(this.getRawResult() != null)
				return this.parser.parse(this.getRawResult());
			else
				return null;
		}
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
