package ultimate.karoapi4j.utils.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.enums.EnumRefreshMode;
import ultimate.karoapi4j.utils.JSONUtil;
import ultimate.karoapi4j.utils.web.URLLoader;

public abstract class BaseSynchronized implements Synchronized
{
	private final Logger	logger	= LoggerFactory.getLogger(getClass());

	private URLLoader		urlLoader;

	private boolean			refreshing;

	private EnumRefreshMode	refreshMode;

	private RefreshThread	refreshThread;

	public BaseSynchronized(URLLoader urlLoader, EnumRefreshMode refreshMode)
	{
		this.urlLoader = urlLoader;
		this.refreshing = true;
		this.refreshMode = refreshMode;
		this.refreshThread = new RefreshThread();
		this.refreshThread.start();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * ultimate.karoapi4j.utils.sync.Synchronized#setURLLoader(ultimate.karoapi4j.utils.web.
	 * URLLoader
	 * )
	 */
	@Override
	public void setURLLoader(URLLoader urlLoader)
	{
		this.urlLoader = urlLoader;
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.Synchronized#getURLLoader()
	 */
	@Override
	public URLLoader getURLLoader()
	{
		return urlLoader;
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.Synchronized#refresh(javax.security.auth.Refreshable)
	 */
	@Override
	public void refresh()
	{
		urlLoader.loadURL(this);
	}

	protected void refreshIfNecessary()
	{
		if(this.refreshMode == EnumRefreshMode.onAccess)
		{
			refresh();
			synchronized(this)
			{
				try
				{
					this.wait();
				}
				catch(InterruptedException e)
				{
					logger.error("Could not wait for synchronized instance being refreshed!", e);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.Refreshable#onRefresh(java.lang.Object)
	 */
	@Override
	public void onRefresh(String newContent)
	{
		update(JSONUtil.deserialize(newContent));
		synchronized(this)
		{
			this.notifyAll();
		}
	}

	protected abstract void update(Object content);

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.Synchronized#setRefreshMode(ultimate.karoapi4j.enums.
	 * EnumRefreshMode)
	 */
	@Override
	public void setRefreshMode(EnumRefreshMode refreshMode)
	{
		if(refreshMode == null)
			throw new IllegalArgumentException("refreshMode must not be null!");
		this.refreshMode = refreshMode;
		synchronized(this.refreshThread)
		{
			this.refreshThread.notifyAll();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.Synchronized#getRefreshMode()
	 */
	@Override
	public EnumRefreshMode getRefreshMode()
	{
		return refreshMode;
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.Synchronized#stopRefreshing()
	 */
	@Override
	public void stopRefreshing()
	{
		this.refreshing = false;
	}

	/**
	 * Internal Thread used for interval based refreshing
	 * 
	 * @author ultimate
	 */
	private class RefreshThread extends Thread
	{
		/*
		 * (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			while(refreshing)
			{
				while(refreshMode.isIntervalBased())
				{
					try
					{
						Thread.sleep(refreshMode.getInterval_ms());
					}
					catch(InterruptedException e)
					{
						logger.error("Could not sleep for refresh interval!", e);
					}
					refresh();
				}

				synchronized(this)
				{
					try
					{
						this.wait();
					}
					catch(InterruptedException e)
					{
						logger.error("Could not wait for change of refresh mode!", e);
					}
				}
			}
		}
	}
}
