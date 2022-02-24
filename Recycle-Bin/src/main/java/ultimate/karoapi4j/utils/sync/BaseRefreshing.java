package ultimate.karoapi4j.utils.sync;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic Implementation of Refreshing implementing the storage of addable Refreshables.
 * 
 * @see Refreshing#addRefreshable(Refreshable)
 * @see Refreshing#removeRefreshable(Refreshable)
 * @see BaseRefreshing#notifyRefreshables()
 * @author ultimate
 * @param <T> - the Type of the refreshable value
 */
public class BaseRefreshing<T> implements Refreshing<T>
{
	/**
	 * Logger-Instance
	 */
	protected final Logger				logger	= LogManager.getLogger(getClass());

	/**
	 * The List of Refreshables
	 */
	protected List<Refreshable<? super T>>	refreshables;

	/**
	 * Standard Constructor<br>
	 * Initializes a List of Refreshables.
	 */
	public BaseRefreshing()
	{
		super();
		this.refreshables = new LinkedList<Refreshable<? super T>>();
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.Synchronized#addRefreshable(ultimate.karoapi4j.utils.sync.
	 * Refreshable)
	 */
	@Override
	public void addRefreshable(Refreshable<T> refreshable)
	{
		this.refreshables.add(refreshable);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * ultimate.karoapi4j.utils.sync.Synchronized#removeRefreshable(ultimate.karoapi4j.utils.sync
	 * .Refreshable)
	 */
	@Override
	public boolean removeRefreshable(Refreshable<T> refreshable)
	{
		return this.refreshables.remove(refreshable);
	}

	/**
	 * Notify all registered Refreshables by calling {@link Refreshable#onRefresh(Object)}
	 */
	@SuppressWarnings("unchecked")
	public void notifyRefreshables()
	{
		for(Refreshable<? super T> r : this.refreshables)
			r.onRefresh((T) this);
	}
}
