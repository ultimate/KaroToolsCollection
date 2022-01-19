package ultimate.karoapi4j.utils.sync;

import java.util.HashMap;
import java.util.Collection;
import java.util.Map;

import ultimate.karoapi4j.enums.EnumRefreshMode;
import ultimate.karoapi4j.loading.Loader;

/**
 * Implementation for synchronized Maps represented by a Collection.<br>
 * On Update the list entries will be added to the map using a specified key of the entries for
 * being able to search the Collection quicker.<br>
 * Note:<br>
 * Modifications of the underlying Map are not allowed. Therefore several operations like
 * put, remove, clear, etc. will throw an {@link UnsupportedOperationException}
 * 
 * @author ultimate
 * @param <K> - the type of the keys inside the Map
 * @param <V> - the type of the values inside the Map
 */
public abstract class SynchronizedCollectionMap<K, V> extends
		BaseSynchronizedMap<K, V, Collection<V>, SynchronizedCollectionMap<K, V>> implements Map<K, V>, Synchronized<Collection<V>, SynchronizedCollectionMap<K, V>>
{
	/**
	 * Construct a new synchronized Map with a {@link HashMap}.
	 * 
	 * @see BaseSynchronized#BaseSynchronized(Loader, EnumRefreshMode)
	 * @param loader - the Loader used to load the Content to synchronize from
	 * @param refreshMode - the RefreshMode used for auto refreshing the synchronized entity
	 * @param clearOnRefresh - should the content of the map be cleared on refresh
	 */
	public SynchronizedCollectionMap(Loader<? extends Collection<V>> loader, EnumRefreshMode refreshMode, boolean clearOnRefresh)
	{
		super(loader, refreshMode, new HashMap<K, V>(), clearOnRefresh);
	}
	
	/**
	 * Construct a new synchronized Map.
	 * 
	 * @see BaseSynchronized#BaseSynchronized(Loader, EnumRefreshMode)
	 * @param loader - the Loader used to load the Content to synchronize from
	 * @param refreshMode - the RefreshMode used for auto refreshing the synchronized entity
	 * @param map - the underlying map to refresh
	 * @param clearOnRefresh - should the content of the map be cleared on refresh
	 */
	@SuppressWarnings("unchecked")
	public SynchronizedCollectionMap(Loader<? extends Collection<V>> loader, EnumRefreshMode refreshMode, Map<K, V> map, boolean clearOnRefresh)
	{
		super((Loader<Collection<V>>) loader, refreshMode, map, clearOnRefresh);
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.BaseSynchronized#update(java.lang.Object)
	 */
	@Override
	protected synchronized void update(Collection<V> content)
	{
		if(clearOnRefresh)
		{
			this.map.clear();
		}
		else
		{
			this.map.values().removeAll(content);
		}

		for(V value : content)
		{
			this.map.put(getKey(value), value);
		}
	}

	/**
	 * Get the key for the given value.<br>
	 * This key is used to insert the value in the map.
	 * 
	 * @param value - the value to insert
	 * @return the key to use for inserting
	 */
	protected abstract K getKey(V value);
}
