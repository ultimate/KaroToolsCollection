package ultimate.karoapi4j.utils.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import ultimate.karoapi4j.enums.EnumRefreshMode;
import ultimate.karoapi4j.utils.web.URLLoader;

public class SynchronizedList<E> extends SynchronizedCollection<E, List<E>> implements List<E>
{
	public SynchronizedList(URLLoader urlLoader, EnumRefreshMode refreshMode, boolean clearOnRefresh)
	{
		super(urlLoader, refreshMode, new ArrayList<E>(), clearOnRefresh);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#get(int)
	 */
	@Override
	public E get(int index)
	{
		refreshIfNecessary();
		return collection.get(index);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object o)
	{
		refreshIfNecessary();
		return collection.indexOf(o);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(Object o)
	{
		refreshIfNecessary();
		return collection.lastIndexOf(o);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#listIterator()
	 */
	@Override
	public ListIterator<E> listIterator()
	{
		refreshIfNecessary();
		return collection.listIterator();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#listIterator(int)
	 */
	@Override
	public ListIterator<E> listIterator(int index)
	{
		refreshIfNecessary();
		return collection.listIterator(index);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#subList(int, int)
	 */
	@Override
	public List<E> subList(int fromIndex, int toIndex)
	{
		refreshIfNecessary();
		return collection.subList(fromIndex, toIndex);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	@Override
	public void add(int index, E element)
	{
		throw new UnsupportedOperationException("Synchronized Collections modified from external!");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	@Override
	public boolean addAll(int index, Collection<? extends E> c)
	{
		throw new UnsupportedOperationException("Synchronized Collections modified from external!");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#remove(int)
	 */
	@Override
	public E remove(int index)
	{
		throw new UnsupportedOperationException("Synchronized Collections modified from external!");
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	@Override
	public E set(int index, E element)
	{
		throw new UnsupportedOperationException("Synchronized Collections modified from external!");
	}

}
