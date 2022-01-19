package ultimate.karoapi4j.utils.sync;

/**
 * Interface for refreshable items.<br>
 * {@link Refreshable#onRefresh(Object)} will be called if the value of type T changed so the entity
 * can update itself.
 * 
 * @author ultimate
 * @param <T> - the value type to be refreshed
 */
public interface Refreshable<T>
{
	/**
	 * The onRefresh-Event called, when the entity of type T has been changed to be able to update
	 * this entity
	 * 
	 * @param refreshed - the refreshed value
	 */
	public void onRefresh(T refreshed);
}
