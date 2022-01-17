package ultimate.karoapi4j.utils.web;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import ultimate.karoapi4j.utils.sync.Refreshable;
import ultimate.karoapi4j.utils.threads.QueuableThread;
import ultimate.karoapi4j.utils.threads.ThreadQueue;

/**
 * A Thread based URLLoader for loading (and parsing) arbitrary URLs.<br>
 * This URLLoaderThread is subclassed from {@link QueuableThread} in order to be queable in
 * {@link ThreadQueue}s.<br>
 * {@link URLLoaderThread} has to be subclassed to implement {@link URLLoader#parse(String)} for the
 * required Type.
 * 
 * @author ultimate
 * @param <T> - the type of content to load
 */
public abstract class URLLoaderThread<T> extends QueuableThread implements URLLoader<T>
{
	/**
	 * The URL to load
	 */
	protected URL				url;
	/**
	 * A timeout used for loading the URL
	 */
	protected int				timeout;
	/**
	 * The HTTP-Method used to load the URL
	 */
	protected String			method;
	/**
	 * The parameters to pass to the URL on load via POST
	 */
	protected String			parameters;
	/**
	 * The loaded content as a String
	 */
	protected String			result;
	/**
	 * The loaded content in the required Type
	 */
	protected T					result_T;
	/**
	 * The Refreshable to notify after loading
	 */
	protected Refreshable<T>	refreshable;

	/**
	 * Construct a new URLLoaderThread.<br>
	 * The URL will be loaded by GET with no parameters
	 * 
	 * @param url - the URL to load
	 */
	public URLLoaderThread(URL url)
	{
		this(url, 0);
	}

	/**
	 * Construct a new URLLoaderThread.<br>
	 * The URL will be loaded by GET with no parameters (with given timeout).
	 * 
	 * @param url - the URL to load
	 * @param timeout - a timeout used for loading the URL
	 */
	public URLLoaderThread(URL url, int timeout)
	{
		this(url, "GET", null, timeout);
	}

	/**
	 * Construct a new URLLoaderThread.<br>
	 * The URL will be loaded by POST with the given parameters
	 * 
	 * @param url - the URL to load
	 * @param parameters - the parameters to pass to the URL on load
	 */
	public URLLoaderThread(URL url, String parameters)
	{
		this(url, parameters, 0);
	}

	/**
	 * Construct a new URLLoaderThread.<br>
	 * The URL will be loaded by POST with the given parameters (with given timeout).<br>
	 * Method generally should be GET or POST
	 * 
	 * @param url - the URL to load
	 * @param parameter - the parameters to pass to the URL on load
	 * @param timeout - a timeout used for loading the URL
	 */
	public URLLoaderThread(URL url, String parameters, int timeout)
	{
		this(url, "POST", parameters, 0);
	}

	/**
	 * Construct a new URLLoaderThread with custom arguments<br>
	 * 
	 * @param url - the URL to load
	 * @param method - the HTTP-Method used to load the URL
	 * @param parameter - the parameters to pass to the URL on load
	 * @param timeout - a timeout used for loading the URL
	 */
	public URLLoaderThread(URL url, String method, String parameters, int timeout)
	{
		super("URLLoaderThread: " + method + " for " + url + (parameters == null || parameters.equals("") ? "" : "?" + parameters));
		if(url == null)
			throw new IllegalArgumentException("The URL must not be null.");
		if(method == null)
			method = "GET";
		if(parameters == null)
			parameters = "";
		this.url = url;
		this.parameters = parameters;
		this.result = null;
		this.timeout = timeout;
		this.method = method;
	}

	/**
	 * The URL to load
	 * 
	 * @return url
	 */
	public URL getUrl()
	{
		return url;
	}

	/**
	 * The timeout used for loading the URL
	 * 
	 * @return timeout
	 */
	public int getTimeout()
	{
		return timeout;
	}

	/**
	 * The HTTP-Method used to load the URL ("GET" or "POST")
	 * 
	 * @return method
	 */
	public String getMethod()
	{
		return method;
	}

	/**
	 * The parameters to pass to the URL on load via POST
	 * 
	 * @return parameters
	 */
	public String getParameters()
	{
		return parameters;
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.threads.QueuableThread#innerRun()
	 */
	@Override
	public void innerRun()
	{
		try
		{
			/**
			 * An internal Thread used to load the URL once makes restarting and timeout handling
			 * easier.
			 */
			Thread t = new Thread() {
				/*
				 * (non-Javadoc)
				 * @see java.lang.Thread#run()
				 */
				public void run()
				{
					try
					{
						StringBuilder site = new StringBuilder();
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setDoInput(true);
						connection.setUseCaches(false);
						connection.setAllowUserInteraction(false);
						connection.setRequestMethod(method);
							
						if(method.equals("POST"))
						{
							connection.setDoOutput(true);
							connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
							PrintWriter out = new PrintWriter(connection.getOutputStream());
							out.print(parameters);
							out.close();
						}

						connection.connect();
						InputStream is = connection.getInputStream();
						BufferedInputStream bis = new BufferedInputStream(is);
						int curr = bis.read();
						while(curr != -1)
						{
							site.append((char) curr);
							curr = bis.read();
						}
						bis.close();
						is.close();

						result = site.toString();
						result_T = parse(result);
					}
					catch(IOException e)
					{
						result = e.toString();
					}
				}
			};
			t.start();
			if(this.timeout > 0)
				t.join(this.timeout);
			else
				t.join();
		}
		catch(InterruptedException e)
		{
			result = e.toString();
		}

		if(refreshable != null)
		{
			refreshable.onRefresh(result_T);
			refreshable = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.Loader#load()
	 */
	@Override
	public void load()
	{
		load(null);
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.Loader#getLoadedContent()
	 */
	@Override
	public T getLoadedContent()
	{
		return result_T;
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.Loader#load(ultimate.karoapi4j.utils.sync.Refreshable)
	 */
	@Override
	public void load(Refreshable<T> refreshable)
	{
		this.refreshable = refreshable;
		this.start();
	}
}
