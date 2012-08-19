package ultimate.karoapi4j.utils.sync;

import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.enums.EnumRefreshMode;
import ultimate.karoapi4j.utils.web.URLLoader;

public class SynchronizedCollection<E, C extends Collection<E>, S extends SynchronizedCollection<E, C, S>> extends BaseSynchronized<S> implements Collection<E>, Synchronized<S>
{
	protected transient Logger	logger	= LoggerFactory.getLogger(getClass());
	protected C					collection;

	protected boolean			clearOnRefresh;

	public SynchronizedCollection(URLLoader urlLoader, EnumRefreshMode refreshMode, C collection, boolean clearOnRefresh)
	{
		super(urlLoader, refreshMode);
		this.collection = collection;
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.BaseSynchronized#update(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void update(Object content)
	{
		if(content instanceof Collection)
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
		else
		{
			logger.error("could not update from " + collection.getClass().getName() + ": " + content);
		}
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
	public String toString()
	{
		return getClass().getName() + collection.toString();
	}
}
