package ultimate.karoapi4j.utils.web.urlloaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.utils.URLLoader;
import ultimate.karoapi4j.utils.sync.Refreshable;
import ultimate.karoapi4j.utils.web.URLLoaderThread;

/**
 * Implementation of {@link Refreshable} that is able to block the access to the loaded URL content.<br>
 * Therefore {@link URLLoader#getLoadedContent()} is wrappend into
 * {@link URLLoaderRefreshable#load()} and the call of that method will be blocked by
 * monitors until the {@link URLLoaderThread} has finished loading.<br>
 * 
 * @author ultimate
 * @param <T> - the value type to be refreshed
 */
public class URLLoaderRefreshable<T> implements Refreshable<T>
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger	logger	= LogManager.getLogger(getClass());

	/**
	 * The URLLoaderThread used to load the required content
	 */
	protected URLLoaderThread<T>		urlLoaderThread;

	/**
	 * Has loading been finished? (for double check in synchronization
	 */
	private boolean						loadingFinished;

	/**
	 * Construct a new URLLoaderRefreshable with a given URLLoaderThread
	 * 
	 * @param urlLoaderThread - the URLLoaderThread
	 */
	public URLLoaderRefreshable(URLLoaderThread<T> urlLoaderThread)
	{
		if(urlLoaderThread == null)
			throw new IllegalArgumentException("The urlLoaderThread must not be null.");
		this.urlLoaderThread = urlLoaderThread;
	}

	/**
	 * Load the content represented by the given URLLoaderThread
	 * 
	 * @see URLLoaderThread#load(Refreshable)
	 * @see URLLoaderThread#getLoadedContent()
	 * @return the content loaded
	 */
	public T load()
	{
		this.loadingFinished = false;
		logger.debug("starting URL loading (" + urlLoaderThread.getUrl() + "?" + urlLoaderThread.getParameters() + ")...");
		this.urlLoaderThread.load(this);
		synchronized(this)
		{
			try
			{
				if(!this.loadingFinished)
					this.wait();
			}
			catch(InterruptedException e)
			{
				logger.error("Synchronization failed!");
			}
		}
		logger.debug("url loaded (" + urlLoaderThread.getUrl() + "?" + urlLoaderThread.getParameters() + ")!");
		return this.urlLoaderThread.getLoadedContent();
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.Refreshable#onRefresh(java.lang.Object)
	 */
	@Override
	public void onRefresh(T refreshed)
	{
		synchronized(this)
		{
			this.notify();
			this.loadingFinished = true;
		}
	}
}
