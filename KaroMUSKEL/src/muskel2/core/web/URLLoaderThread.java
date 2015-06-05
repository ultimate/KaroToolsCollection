package muskel2.core.web;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import muskel2.core.threads.QueuableThread;

public class URLLoaderThread extends QueuableThread
{

	protected URL		url;
	protected String	parameter;
	protected String	result;
	protected String	method;
	protected int		timeout;

	public URLLoaderThread(URL url, String parameter)
	{
		this(url, "POST", parameter, 0);
	}

	public URLLoaderThread(URL url, String method, String parameter)
	{
		this(url, method, parameter, 0);
	}

	public URLLoaderThread(URL url, String parameter, int timeout)
	{
		this(url, "POST", parameter, 0);
	}

	public URLLoaderThread(URL url, String method, String parameter, int timeout)
	{
		super("URLLoaderThread: " + method + " for " + url + (parameter == null || parameter.equals("") ? "" : "?" + parameter));
		if(url == null)
			throw new IllegalArgumentException("The URL must not be null.");
		if(method == null)
			throw new IllegalArgumentException("The method must not be null.");
		if(parameter == null)
			throw new IllegalArgumentException("The parameters must not be null. May be \"\".");
		this.url = url;
		this.parameter = parameter;
		this.result = null;
		this.timeout = timeout;
		this.method = method;
	}

	@Override
	public void innerRun()
	{
		try
		{
			Thread t = new Thread() {
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
							PrintWriter out = new PrintWriter(connection.getOutputStream());
							out.print(parameter);
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
					}
					catch(IOException e)
					{
						result = e.toString();
					}
				}
			};
			t.start();
			if(this.timeout > 0)
				t.join(10000);
			else
				t.join();
		}
		catch(InterruptedException e)
		{
			result = e.toString();
		}
	}

	public String getLoadedURLContent()
	{
		return result;
	}
}
