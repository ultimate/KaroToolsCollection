package ultimate.karoapi4j.utils.sync;

import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.enums.EnumRefreshMode;

/**
 * Base for synchronized collections managing the most relevant access operations.<br>
 * Note:<br>
 * Modifications of the underlying Collection are not allowed. Therefore several operations like
 * add, remove, clear, etc. will throw an {@link UnsupportedOperationException}
 * 
 * @author ultimate
 * @param <E> - the type of the entities inside the Collection 
 * @param <C> - the collection type
 * @param <S> - the Type of Entity to be synchronized (= extending Class)
 */
public class SynchronizedCollection<E, C extends Collection<E>, S extends SynchronizedCollection<E, C, S>> extends BaseSynchronized<Collection<E>, S>
		implements Collection<E>, Synchronized<Collection<E>, S>
{
	/**
	 * Logger-Instance
	 */
	protected transient Logger	logger	= LoggerFactory.getLogger(getClass());

	/**
	 * the underlying collection to refresh
	 */
	protected C					collection;

	/**
	 * Will the content of the collection be cleared on refresh?
	 */
	protected boolean			clearOnRefresh;

	/**
	 * Construct a new synchronized Collection.
	 * 
	 * @see BaseSynchronized#BaseSynchronized(Loader, EnumRefreshMode)
	 * @param loader - the Loader used to load the Content to synchronize from
	 * @param refreshMode - the RefreshMode used for auto refreshing the synchronized entity
	 * @param collection - the underlying collection to refresh
	 * @param clearOnRefresh - should the content of the collection be cleared on refresh
	 */
	@SuppressWarnings("unchecked")
	public SynchronizedCollection(Loader<? extends Collection<E>> loader, EnumRefreshMode refreshMode, C collection, boolean clearOnRefresh)
	{
		super((Loader<Collection<E>>) loader, refreshMode);
		this.collection = collection;
	}

	/**
	 * Will the content of the collection be cleared on refresh?<br>
	 * If true, all content will be removed before adding new content<br>
	 * If false, old content will be retained and only new content will be added (growing
	 * collection)<br>
	 * 
	 * @return true or false
	 */
	public boolean isClearOnRefresh()
	{
		return clearOnRefresh;
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.BaseSynchronized#update(java.lang.Object)
	 */
	@Override
	protected synchronized void update(Collection<E> content)
	{
		if(clearOnRefresh)
		{
			this.collection.clear();
		}
		else
		{
			this.collection.removeAll((Collection<?>) content);
		}
		// TODO merge types
		this.collection.addAll((Collection<? extends E>) content);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#size()
	 */
	@Override
	public int size()
	{
		refreshIfNecessary();
		return collection.size();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		refreshIfNecessary();
		return collection.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#iterator()
	 */
	@Override
	public Iterator<E> iterator()
	{
		refreshIfNecessary();
		return collection.iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#toArray()
	 */
	@Override
	public Object[] toArray()
	{
		refreshIfNecessary();
		return collection.toArray();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(T[] a)
	{
		refreshIfNecessary();
		return collection.toArray(a);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o)
	{
		refreshIfNecessary();
		return collection.contains(o);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c)
	{
		refreshIfNecessary();
		return collection.containsAll(c);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	@Override
	public boolean add(E e)
	{
		throw new UnsupportedOperationException("Synchronized Collections modified from external!");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		throw new UnsupportedOperationException("Synchronized Collections modified from external!");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o)
	{
		throw new UnsupportedOperationException("Synchronized Collections modified from external!");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c)
	{
		throw new UnsupportedOperationException("Synchronized Collections modified from external!");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c)
	{
		throw new UnsupportedOperationException("Synchronized Collections modified from external!");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear()
	{
		throw new UnsupportedOperationException("Synchronized Collections modified from external!");
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public synchronized String toString()
	{
		return getClass().getName() + collection.toString();
	}
}
