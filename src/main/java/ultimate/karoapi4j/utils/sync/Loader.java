package ultimate.karoapi4j.utils.sync;

/**
 * Generic Interface for arbitrary Loaders.<br>
 * Loaders are for example used to load (new) content for synchronized entities from a given source
 * (e.g. an URL).
 * 
 * @author ultimate
 * @param <T> - the type of content to load
 */
public interface Loader<T>
{
	/**
	 * Load the content from the underlying source
	 */
	public void load();

	/**
	 * Load the content from the underlying source and add an Refreshable to be notified afert
	 * loading
	 * 
	 * @see Refreshable#onRefresh(Object)
	 * @param refreshable - the Refreshable to notify
	 */
	public void load(Refreshable<T> refreshable);

	/**
	 * Get the loaded content.<br>
	 * This will return the last known state. If loading is currently in progress this may not be
	 * the newest content (depending on wether the content has changed).
	 * 
	 * @return the last known value for the content
	 */
	public T getLoadedContent();
}
