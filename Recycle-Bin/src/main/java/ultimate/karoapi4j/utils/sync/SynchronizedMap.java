package ultimate.karoapi4j.utils.sync;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ultimate.karoapi4j.enums.EnumRefreshMode;
import ultimate.karoapi4j.loading.Loader;

/**
 * Implementation of synchronized maps managing the standard access operations.<br>
 * Note:<br>
 * Modifications of the underlying Map are not allowed. Therefore several operations like
 * put, remove, clear, etc. will throw an {@link UnsupportedOperationException}
 * 
 * @author ultimate
 * @param <K> - the type of the keys inside the Map
 * @param <V> - the type of the values inside the Map
 */
public class SynchronizedMap<K, V> extends BaseSynchronizedMap<K, V, Map<K, V>, SynchronizedMap<K, V>> implements Map<K, V>,
		Synchronized<Map<K, V>, SynchronizedMap<K, V>>
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
	public SynchronizedMap(Loader<? extends Map<K, V>> loader, EnumRefreshMode refreshMode, Map<K, V> map, boolean clearOnRefresh)
	{
		super((Loader<Map<K, V>>) loader, refreshMode, map, clearOnRefresh);
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.BaseSynchronized#update(java.lang.Object)
	 */
	@Override
	protected synchronized void update(Map<K, V> content)
	{
		if(clearOnRefresh)
		{
			this.map.clear();
		}
		else
		{
			this.map.values().removeAll(content.values());
		}
		// TODO merge types
		this.map.putAll((Map<? extends K, ? extends V>) content);
	}

	/**
	 * HashMap extension of {@link SynchronizedMap}
	 * 
	 * @author ultimate
	 * @param <K> - the type of the keys inside the Map
	 * @param <V> - the type of the values inside the Map
	 */
	public static class Hash<K, V> extends SynchronizedMap<K, V>
	{
		/**
		 * Construct a new synchronized Map with a HashMap.
		 * 
		 * @see BaseSynchronized#BaseSynchronized(Loader, EnumRefreshMode)
		 * @param loader - the Loader used to load the Content to synchronize from
		 * @param refreshMode - the RefreshMode used for auto refreshing the synchronized entity
		 * @param clearOnRefresh - should the content of the map be cleared on refresh
		 */
		public Hash(Loader<? extends Map<K, V>> loader, EnumRefreshMode refreshMode, boolean clearOnRefresh)
		{
			super(loader, refreshMode, new HashMap<K, V>(), clearOnRefresh);
		}
	}

	/**
	 * TreeMap extension of {@link SynchronizedMap}
	 * 
	 * @author ultimate
	 * @param <K> - the type of the keys inside the Map
	 * @param <V> - the type of the values inside the Map
	 */
	public static class Tree<K extends Comparable<K>, V> extends SynchronizedMap<K, V>
	{
		/**
		 * Construct a new synchronized Map with a TreeMap.
		 * 
		 * @see BaseSynchronized#BaseSynchronized(Loader, EnumRefreshMode)
		 * @param loader - the Loader used to load the Content to synchronize from
		 * @param refreshMode - the RefreshMode used for auto refreshing the synchronized entity
		 * @param clearOnRefresh - should the content of the map be cleared on refresh
		 */
		public Tree(Loader<? extends Map<K, V>> loader, EnumRefreshMode refreshMode, boolean clearOnRefresh)
		{
			super(loader, refreshMode, new TreeMap<K, V>(), clearOnRefresh);
		}
	}
}
