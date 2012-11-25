package ultimate.karoapi4j.utils.sync;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import ultimate.karoapi4j.enums.EnumRefreshMode;

/**
 * Generic extension of {@link SynchronizedCollectionMap} using reflections to determine the required key
 * on insertion.<br>
 * 
 * @author ultimate
 * @param <K> - the type of the keys inside the Map
 * @param <V> - the type of the values inside the Map
 */
public class GenericSynchronizedCollectionMap<K, V> extends SynchronizedCollectionMap<K, V>
{
	protected String	keyGetter;

	protected boolean	keyStringsLowerCase;

	/**
	 * Construct a new synchronized Map.
	 * 
	 * @see BaseSynchronized#BaseSynchronized(Loader, EnumRefreshMode)
	 * @param loader - the Loader used to load the Content to synchronize from
	 * @param refreshMode - the RefreshMode used for auto refreshing the synchronized entity
	 * @param map - the underlying map to refresh
	 * @param clearOnRefresh - should the content of the map be cleared on refresh
	 * @param keyGetter - the name of the getter-Method for the required key
	 */
	public GenericSynchronizedCollectionMap(Loader<? extends Collection<V>> loader, EnumRefreshMode refreshMode, Map<K, V> map, boolean clearOnRefresh,
			String keyGetter)
	{
		this(loader, refreshMode, map, clearOnRefresh, keyGetter, true);
	}

	/**
	 * Construct a new synchronized Map.
	 * 
	 * @see BaseSynchronized#BaseSynchronized(Loader, EnumRefreshMode)
	 * @param loader - the Loader used to load the Content to synchronize from
	 * @param refreshMode - the RefreshMode used for auto refreshing the synchronized entity
	 * @param map - the underlying map to refresh
	 * @param clearOnRefresh - should the content of the map be cleared on refresh
	 * @param keyGetter - the name of the getter-Method for the required key
	 * @param keyStringsLowerCase - should string keys be converted to lower case?
	 */
	public GenericSynchronizedCollectionMap(Loader<? extends Collection<V>> loader, EnumRefreshMode refreshMode, Map<K, V> map, boolean clearOnRefresh,
			String keyGetter, boolean keyStringsLowerCase)
	{
		super(loader, refreshMode, map, clearOnRefresh);
		this.keyGetter = keyGetter;
		this.keyStringsLowerCase = keyStringsLowerCase;
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.SynchronizedCollectionMap#getKey(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected K getKey(V value)
	{
		if(value == null)
			return null;
		try
		{
			K key = (K) value.getClass().getMethod(keyGetter).invoke(value);
			if(key instanceof String && keyStringsLowerCase)
				key = (K) ((String) key).toLowerCase();
			return key;
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch(SecurityException e)
		{
			e.printStackTrace();
		}
		catch(IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch(InvocationTargetException e)
		{
			e.printStackTrace();
		}
		catch(NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * HashMap extension of {@link GenericSynchronizedCollectionMap}
	 * 
	 * @author ultimate
	 * @param <K> - the type of the keys inside the Map
	 * @param <V> - the type of the values inside the Map
	 */
	public static class Hash<K, V> extends GenericSynchronizedCollectionMap<K, V>
	{
		/**
		 * Construct a new synchronized Map with a HashMap.
		 * 
		 * @see BaseSynchronized#BaseSynchronized(Loader, EnumRefreshMode)
		 * @param loader - the Loader used to load the Content to synchronize from
		 * @param refreshMode - the RefreshMode used for auto refreshing the synchronized entity
		 * @param clearOnRefresh - should the content of the map be cleared on refresh
		 * @param keyGetter - the name of the getter-Method for the required key
		 */
		public Hash(Loader<? extends Collection<V>> loader, EnumRefreshMode refreshMode, boolean clearOnRefresh, String keyGetter)
		{
			super(loader, refreshMode, new HashMap<K, V>(), clearOnRefresh, keyGetter);
		}

		/**
		 * Construct a new synchronized Map with a HashMap.
		 * 
		 * @see BaseSynchronized#BaseSynchronized(Loader, EnumRefreshMode)
		 * @param loader - the Loader used to load the Content to synchronize from
		 * @param refreshMode - the RefreshMode used for auto refreshing the synchronized entity
		 * @param clearOnRefresh - should the content of the map be cleared on refresh
		 * @param keyGetter - the name of the getter-Method for the required key
		 * @param keyStringsLowerCase - should string keys be converted to lower case?
		 */
		public Hash(Loader<? extends Collection<V>> loader, EnumRefreshMode refreshMode, boolean clearOnRefresh, String keyGetter,
				boolean keyStringsLowerCase)
		{
			super(loader, refreshMode, new HashMap<K, V>(), clearOnRefresh, keyGetter, keyStringsLowerCase);
		}
	}

	/**
	 * TreeMap extension of {@link GenericSynchronizedCollectionMap}
	 * 
	 * @author ultimate
	 * @param <K> - the type of the keys inside the Map
	 * @param <V> - the type of the values inside the Map
	 */
	public static class Tree<K extends Comparable<K>, V> extends GenericSynchronizedCollectionMap<K, V>
	{
		/**
		 * Construct a new synchronized Map with a TreeMap.
		 * 
		 * @see BaseSynchronized#BaseSynchronized(Loader, EnumRefreshMode)
		 * @param loader - the Loader used to load the Content to synchronize from
		 * @param refreshMode - the RefreshMode used for auto refreshing the synchronized entity
		 * @param clearOnRefresh - should the content of the map be cleared on refresh
		 * @param keyGetter - the name of the getter-Method for the required key
		 */
		public Tree(Loader<? extends Collection<V>> loader, EnumRefreshMode refreshMode, boolean clearOnRefresh, String keyGetter)
		{
			super(loader, refreshMode, new TreeMap<K, V>(), clearOnRefresh, keyGetter);
		}

		/**
		 * Construct a new synchronized Map with a TreeMap.
		 * 
		 * @see BaseSynchronized#BaseSynchronized(Loader, EnumRefreshMode)
		 * @param loader - the Loader used to load the Content to synchronize from
		 * @param refreshMode - the RefreshMode used for auto refreshing the synchronized entity
		 * @param clearOnRefresh - should the content of the map be cleared on refresh
		 * @param keyGetter - the name of the getter-Method for the required key
		 * @param keyStringsLowerCase - should string keys be converted to lower case?
		 */
		public Tree(Loader<? extends Collection<V>> loader, EnumRefreshMode refreshMode, boolean clearOnRefresh, String keyGetter,
				boolean keyStringsLowerCase)
		{
			super(loader, refreshMode, new TreeMap<K, V>(), clearOnRefresh, keyGetter, keyStringsLowerCase);
		}
	}
}
