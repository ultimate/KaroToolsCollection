package ultimate.karoapi4j.utils.sync;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import ultimate.karoapi4j.enums.EnumRefreshMode;

/**
 * Generic extension of {@link SynchronizedListMap} using reflections to determine the required key
 * on insertion.<br>
 * 
 * @author ultimate
 * @param <K> - the type of the keys inside the Map
 * @param <V> - the type of the values inside the Map
 * @param <M> - the map type
 * @param <S> - the Type of Entity to be synchronized (= extending Class)
 */
public class GenericSynchronizedListMap<K, V, M extends Map<K, V>, S extends GenericSynchronizedListMap<K, V, M, S>> extends
		SynchronizedListMap<K, V, M, S>
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
	public GenericSynchronizedListMap(Loader<? extends List<V>> loader, EnumRefreshMode refreshMode, M map, boolean clearOnRefresh, String keyGetter)
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
	public GenericSynchronizedListMap(Loader<? extends List<V>> loader, EnumRefreshMode refreshMode, M map, boolean clearOnRefresh, String keyGetter, boolean keyStringsLowerCase)
	{
		super(loader, refreshMode, map, clearOnRefresh);
		this.keyGetter = keyGetter;
		this.keyStringsLowerCase = keyStringsLowerCase;
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.SynchronizedListMap#getKey(java.lang.Object)
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
}
