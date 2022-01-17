package muskel2.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RequestLogger
{
	private static List<RequestLogger> loggers = new ArrayList<RequestLogger>();
	
	private List<LogEntry>	log	= new LinkedList<LogEntry>();

	private PrintStream		out;

	public RequestLogger(String fileName) throws FileNotFoundException
	{
		out = new PrintStream(new File(fileName));
		
		synchronized(loggers)
		{
			loggers.add(this);
		}
	}

	public synchronized LogEntry add(URL url, String parameters)
	{
		LogEntry entry = new LogEntry(log.size() + 1, url, parameters);
		log.add(entry);
		return entry;
	}

	public synchronized void log(LogEntry entry, String response, int responseCode)
	{
		entry.setResponse(response, responseCode);

		logTo(this.out, entry, false);
		logTo(System.out, entry, true);

		entry.logged = true;
	}

	public static void logTo(PrintStream ps, LogEntry entry, boolean shortMode)
	{
		ps.println("Request " + entry.id + ":");
		ps.println("  url:          " + entry.url);
		ps.println("  parameters:   " + entry.parameters);
		if(shortMode)
			ps.println("  response:     " + (entry.response != null ? entry.response.substring(0, 50).replace("\r", "").replace("\n", "") + " ....... " + entry.response.substring(entry.response.length() - 50).replace("\r", "").replace("\n", "") : null));
		else
			ps.println("  response:     " + (entry.response != null ? entry.response.replace("\r", "").replace("\n", "") : null));
		ps.println("  responseCode: " + entry.responseCode);
		ps.println("  duration:     " + (entry.responseTime - entry.startTime) + "ms");
	}

	public synchronized void close() throws IOException
	{
		System.out.println("Cleaning up & logging pending request...");
		for(LogEntry e : log)
		{
			if(!e.logged)
				log(e, null, -1);
		}

		out.flush();
		out.close();
	}

	public static class LogEntry
	{
		private int		id;
		private URL		url;
		private String	parameters;
		private int		responseCode;
		private String	response;
		private boolean	logged;
		private long	startTime;
		private long	responseTime;

		public LogEntry(int id, URL url, String parameters)
		{
			super();
			this.id = id;
			this.url = url;
			this.parameters = parameters;
			this.responseCode = -1;
			this.response = null;
			this.logged = false;
			this.startTime = System.currentTimeMillis();
			this.responseTime = -1;
		}

		public void setResponse(String response, int responseCode)
		{
			this.response = response;
			this.responseCode = responseCode;
			this.responseTime = System.currentTimeMillis();
		}
	}
	
	public static void cleanUp()
	{
		for(RequestLogger logger: loggers)
		{
			try
			{
				logger.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
