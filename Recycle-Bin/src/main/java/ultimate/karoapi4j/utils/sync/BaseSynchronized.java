package ultimate.karoapi4j.utils.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.enums.EnumRefreshMode;
import ultimate.karoapi4j.loading.Loader;

/**
 * Abstract base for sychronized entities offering basic functionality like interval or on-access
 * based refreshing.
 * 
 * @author ultimate
 * @param <T> - the Type of Content to synchronize from
 * @param <S> - the Type of Entity to be synchronized (= extending Class)
 */
public abstract class BaseSynchronized<T, S extends BaseSynchronized<T, S>> extends BaseRefreshing<S> implements Synchronized<T, S>
{
	/**
	 * Logger-Instance
	 */
	protected final Logger	logger			= LoggerFactory.getLogger(getClass());

	/**
	 * The Loader used to load the Content to synchronize from
	 * 
	 * @see Synchronized#setLoader(Loader)
	 * @see Synchronized#getLoader()
	 */
	private Loader<T>		loader;

	/**
	 * Is this entity currently in interval based refresh mode?
	 */
	private boolean			refreshing;

	/**
	 * The RefreshMode used for auto refreshing the synchronized entity
	 */
	private EnumRefreshMode	refreshMode;

	/**
	 * An internal thread used to refresh this entity if refresh mode is interval based
	 * 
	 * @see EnumRefreshMode
	 */
	private RefreshThread	refreshThread;

	/**
	 * The time of the last refresh
	 */
	protected long			lastRefreshTime	= -1;

	/**
	 * Construct a new BaseSynchronized with the standard required Arguments
	 * 
	 * @param loader - the Loader used to load the Content to synchronize from
	 * @param refreshMode - the RefreshMode used for auto refreshing the synchronized entity
	 */
	public BaseSynchronized(Loader<T> loader, EnumRefreshMode refreshMode)
	{
		this.setLoader(loader);
		this.setRefreshMode(refreshMode);
		this.refreshing = true;
		this.refreshThread = new RefreshThread();
		this.refreshThread.start();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * ultimate.karoapi4j.utils.sync.Synchronized#setLoader(ultimate.karoapi4j.utils.web.
	 * Loader
	 * )
	 */
	@Override
	public void setLoader(Loader<T> loader)
	{
		if(loader == null)
			throw new IllegalArgumentException("loader must not be null!");
		this.loader = loader;
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.Synchronized#getLoader()
	 */
	@Override
	public Loader<T> getLoader()
	{
		return loader;
	}

	/**
	 * The time of the last refresh
	 * 
	 * @return lastRefreshTime
	 */
	public long getLastRefreshTime()
	{
		return lastRefreshTime;
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.Synchronized#refresh(javax.security.auth.Refreshable)
	 */
	@Override
	public void refresh()
	{
		loader.load(this);
	}

	/**
	 * Internal procedure refreshing this entity whenever it is necessary.<br>
	 * Refreshing is necessary if RefreshMode is set to {@link EnumRefreshMode#onAccess} and this
	 * entity is accessed.
	 * 
	 * @see EnumRefreshMode#onAccess
	 */
	protected void refreshIfNecessary()
	{
		if(this.refreshMode == EnumRefreshMode.onAccess || (this.refreshMode == EnumRefreshMode.onFirstAccess && this.lastRefreshTime == -1))
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
	public void onRefresh(T newContent)
	{
		this.lastRefreshTime = System.currentTimeMillis();
		update(newContent);
		notifyRefreshables();
		synchronized(this)
		{
			this.notifyAll();
		}
	}

	/**
	 * Internal update procedure processing the parsed JSON content.
	 * 
	 * @param content - the parsed JSON content
	 */
	protected abstract void update(T content);

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
