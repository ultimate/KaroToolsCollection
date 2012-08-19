package ultimate.karoapi4j.utils.sync;

import ultimate.karoapi4j.enums.EnumRefreshMode;

/**
 * Interface for (self)-synchronizing entities.
 * 
 * @author ultimate
 * @param <T> - the Type of Content to synchronize from
 * @param <S> - the Type of Entity to be synchronized (= extending Class)
 */
public interface Synchronized<T, S extends Synchronized<T, S>> extends Refreshable<T>, Refreshing<S>
{
	/**
	 * Set the Loader used to load the Content to synchronize from
	 * 
	 * @param loader - the Loader
	 */
	public void setLoader(Loader<T> loader);

	/**
	 * Get the Loader used to load the Content to synchronize from
	 * 
	 * @return the Loader
	 */
	public Loader<T> getLoader();

	/**
	 * Force a refresh (= resynchronization) of this Entity
	 */
	public void refresh();

	/**
	 * Set the RefreshMode used for auto refreshing the synchronized entity
	 * 
	 * @see EnumRefreshMode
	 * @param refreshMode - the selected RefreshMode
	 */
	public void setRefreshMode(EnumRefreshMode refreshMode);

	/**
	 * Get the RefreshMode used for auto refreshing the synchronized entity
	 * 
	 * @see EnumRefreshMode
	 * @return the selected RefreshMode
	 */
	public EnumRefreshMode getRefreshMode();

	/**
	 * Stop auto refreshing for this synchronized entity
	 */
	public void stopRefreshing();
}
