package ultimate.karoapi4j.utils.sync;

import java.util.List;
import java.util.Map;

import ultimate.karoapi4j.enums.EnumRefreshMode;

/**
 * Implementation for synchronized Maps represented by a List.<br>
 * On Update the list entries will be added to the map using a specified key of the entries for
 * being able to search the List quicker.<br>
 * Note:<br>
 * Modifications of the underlying Map are not allowed. Therefore several operations like
 * put, remove, clear, etc. will throw an {@link UnsupportedOperationException}
 * 
 * @author ultimate
 * @param <K> - the type of the keys inside the Map
 * @param <V> - the type of the values inside the Map
 * @param <M> - the map type
 * @param <S> - the Type of Entity to be synchronized (= extending Class)
 */
public abstract class SynchronizedListMap<K, V, M extends Map<K, V>, S extends SynchronizedListMap<K, V, M, S>> extends BaseSynchronizedMap<K, V, Map<K, V>, List<V>, S>
		implements Map<K, V>, Synchronized<List<V>, S>
{
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
	public SynchronizedListMap(Loader<? extends List<V>> loader, EnumRefreshMode refreshMode, M map, boolean clearOnRefresh)
	{
		super((Loader<List<V>>) loader, refreshMode, map, clearOnRefresh);
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.BaseSynchronized#update(java.lang.Object)
	 */
	@Override
	protected synchronized void update(List<V> content)
	{
		if(clearOnRefresh)
		{
			this.map.clear();
		}
		else
		{
			this.map.values().removeAll(content);
		}
		
		for(V value: content)
		{
			this.map.put(getKey(value), value);
		}
	}
	
	protected abstract K getKey(V value);
}
