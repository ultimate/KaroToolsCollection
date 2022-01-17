package ultimate.karoapi4j.utils.sync;

import java.util.Collection;

/**
 * Interface for items that may refresh other entities on change.<br>
 * For this Refreshables can be added and removed whichs {@link Refreshable#onRefresh(Object)}
 * method will be called on a change.
 * 
 * @author ultimate
 * @param <T> - the value type to be refreshed.
 */
public interface Refreshing<T>
{
	/**
	 * Add/register an Refreshable for this entity.
	 * 
	 * @param refreshable - the Refreshable to add
	 */
	public void addRefreshable(Refreshable<T> refreshable);

	/**
	 * Remove/unregister an Refreshable for this entity.
	 * 
	 * @param refreshable - the Refreshable to remove
	 * @return if the collection of refreshables has been modified as specified by
	 *         {@link Collection#remove(Object)}
	 */
	public boolean removeRefreshable(Refreshable<T> refreshable);
}
