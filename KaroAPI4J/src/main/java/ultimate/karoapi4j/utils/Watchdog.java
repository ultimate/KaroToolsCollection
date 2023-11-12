package ultimate.karoapi4j.utils;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Watchdog implements Runnable
{
	protected transient final Logger	logger	= LogManager.getLogger(getClass());

	private int							interval;
	private int							timeout;
	private boolean						canceled = false;

	private String						lastMessage;
	private long						lastUpdate;

	private Consumer<String>			onTimeout;

	public Watchdog(int interval, int timeout, Consumer<String> onTimeout)
	{
		if(interval > timeout)
			throw new IllegalArgumentException("interval must be less then or equal to timeout");
		if(interval <= 0)
			throw new IllegalArgumentException("interval must be positive");
		if(onTimeout == null)
			throw new IllegalArgumentException("onTimeout must not be null");

		this.interval = interval;
		this.timeout = timeout;

		this.onTimeout = onTimeout;
	}

	@Override
	public void run()
	{
		logger.info("watchdog started");
		this.canceled = false;
		notifyActive("Watchdog started");
		long now;
		do
		{
			try
			{
				Thread.sleep(interval);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			now = System.currentTimeMillis();
		} while(!canceled && now > lastUpdate + timeout);
		
		if(canceled)
		{
			logger.info("watchdog canceled");
			return;
		}	
			
		logger.info("watchdog timeout");
		onTimeout.accept(lastMessage);
	}

	public void cancel()
	{
		logger.info("watchdog canceled");
		this.canceled = true;
	}

	public void notifyActive(String message)
	{
		logger.info("watchdog active: " + message);
		this.lastUpdate = System.currentTimeMillis();
		this.lastMessage = message;
	}
}
