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
			throw new IllegalArgumentException("interval (" + interval + ") must be less then or equal to timeout (" + timeout + ")");
		if(interval <= 0)
			throw new IllegalArgumentException("interval (" + interval + ") must be positive");
		if(onTimeout == null)
			throw new IllegalArgumentException("onTimeout must not be null");

		this.interval = interval;
		this.timeout = timeout;

		this.onTimeout = onTimeout;
	}

	public int getTimeout()
	{
		return timeout;
	}

	public String getLastMessage()
	{
		return lastMessage;
	}

	public long getLastUpdate()
	{
		return lastUpdate;
	}

	@Override
	public void run()
	{
		logger.debug("watchdog started");
		this.canceled = false;
		notifyActive("watchdog started");
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
			if(logger.isTraceEnabled())
			{
				logger.trace("now                  = " + now);
				logger.trace("lastUpdate           = " + lastUpdate);
				logger.trace("timeout                                + " + timeout);
				logger.trace("lastUpdate + timeout = " + (lastUpdate + timeout));
			}
		} while(!this.canceled && now < (this.lastUpdate + this.timeout));
		
		if(this.canceled)
		{
			logger.debug("watchdog canceled");
			return;
		}	
			
		logger.debug("watchdog timeout");
		onTimeout.accept(this.lastMessage);
	}

	public void cancel()
	{
		this.canceled = true;
	}

	public void notifyActive(String message)
	{
		logger.info("watchdog event: " + message);
		this.lastUpdate = System.currentTimeMillis();
		this.lastMessage = message;
	}
}
