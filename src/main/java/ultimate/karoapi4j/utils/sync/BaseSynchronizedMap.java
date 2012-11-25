package ultimate.karoapi4j.utils.sync;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.enums.EnumRefreshMode;

/**
 * Base for synchronized collections managing the most relevant access operations.<br>
 * Note:<br>
 * Modifications of the underlying Map are not allowed. Therefore several operations like
 * put, remove, clear, etc. will throw an {@link UnsupportedOperationException}
 * 
 * @author ultimate
 * @param <K> - the type of the keys inside the Map
 * @param <V> - the type of the values inside the Map
 * @param <U> - the type of content to update the map
 * @param <M> - the map type
 * @param <S> - the Type of Entity to be synchronized (= extending Class)
 */
public abstract class BaseSynchronizedMap<K, V, M extends Map<K, V>, U, S extends BaseSynchronizedMap<K, V, M, U, S>> extends BaseSynchronized<U, S>
		implements Map<K, V>, Synchronized<U, S>
{
	/**
	 * Logger-Instance
	 */
	protected transient Logger	logger	= LoggerFactory.getLogger(getClass());

	/**
	 * the underlying map to refresh
	 */
	protected M					map;

	/**
	 * Will the content of the map be cleared on refresh?
	 */
	protected boolean			clearOnRefresh;

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
	public BaseSynchronizedMap(Loader<? extends U> loader, EnumRefreshMode refreshMode, M map, boolean clearOnRefresh)
	{
		super((Loader<U>) loader, refreshMode);
		this.map = map;
	}

	/**
	 * Will the content of the map be cleared on refresh?<br>
	 * If true, all content will be removed before adding new content<br>
	 * If false, old content will be retained and only new content will be added (growing
	 * map)<br>
	 * 
	 * @return true or false
	 */
	public boolean isClearOnRefresh()
	{
		return clearOnRefresh;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Map#get(java.lang.Object)
	 */
	@Override
	public V get(Object key)
	{
		refreshIfNecessary();
		return map.get(key);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Map#size()
	 */
	@Override
	public int size()
	{
		refreshIfNecessary();
		return map.size();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Map#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		refreshIfNecessary();
		return map.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(Object key)
	{
		refreshIfNecessary();
		return map.containsKey(key);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(Object value)
	{
		refreshIfNecessary();
		return map.containsValue(value);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Map#entrySet()
	 */
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		refreshIfNecessary();
		return Collections.unmodifiableSet(map.entrySet());
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Map#keySet()
	 */
	@Override
	public Set<K> keySet()
	{
		refreshIfNecessary();
		return Collections.unmodifiableSet(map.keySet());
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Map#values()
	 */
	@Override
	public Collection<V> values()
	{
		refreshIfNecessary();
		return Collections.unmodifiableCollection(map.values());
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public V put(K key, V value)
	{
		throw new UnsupportedOperationException("Synchronized Maps modified from external!");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		throw new UnsupportedOperationException("Synchronized Maps modified from external!");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	@Override
	public V remove(Object key)
	{
		throw new UnsupportedOperationException("Synchronized Maps modified from external!");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Map#clear()
	 */
	@Override
	public void clear()
	{
		throw new UnsupportedOperationException("Synchronized Maps modified from external!");
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public synchronized String toString()
	{
		return getClass().getName() + map.toString();
	}
}
