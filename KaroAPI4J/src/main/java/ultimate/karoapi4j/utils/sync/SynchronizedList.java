package ultimate.karoapi4j.utils.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import ultimate.karoapi4j.enums.EnumRefreshMode;

/**
 * Simple extension of {@link SynchronizedCollection} for Lists adding List-functionality.<br>
 * Note:<br>
 * Modifications of the underlying List are not allowed. Therefore several operations like
 * add, remove, clear, etc. will throw an {@link UnsupportedOperationException}
 * 
 * @author ultimate
 * @param <E> - the type of the entities inside the List
 */
public class SynchronizedList<E> extends SynchronizedCollection<E, List<E>, SynchronizedList<E>> implements List<E>
{
	/**
	 * An optional comparator for sorting the list after updating
	 */
	protected Comparator<E>	sortComparator;

	/**
	 * Construct a new SynchronizedList
	 * 
	 * @see SynchronizedCollection#SynchronizedCollection(Loader, EnumRefreshMode, Collection,
	 *      boolean)
	 * @param loader - the Loader used to load the Content to synchronize from
	 * @param refreshMode - the RefreshMode used for auto refreshing the synchronized entity
	 * @param clearOnRefresh - should the content of the collection be cleared on refresh
	 */
	public SynchronizedList(Loader<? extends Collection<E>> loader, EnumRefreshMode refreshMode, boolean clearOnRefresh)
	{
		this(loader, refreshMode, clearOnRefresh, null);
	}

	/**
	 * Construct a new SynchronizedList
	 * 
	 * @see SynchronizedCollection#SynchronizedCollection(Loader, EnumRefreshMode, Collection,
	 *      boolean)
	 * @param loader - the Loader used to load the Content to synchronize from
	 * @param refreshMode - the RefreshMode used for auto refreshing the synchronized entity
	 * @param clearOnRefresh - should the content of the collection be cleared on refresh
	 * @param sortComparator - an optional comparator for sorting the list after updating
	 */
	public SynchronizedList(Loader<? extends Collection<E>> loader, EnumRefreshMode refreshMode, boolean clearOnRefresh, Comparator<E> sortComparator)
	{
		super(loader, refreshMode, new ArrayList<E>(), clearOnRefresh);
		this.sortComparator = sortComparator;
	}

	/**
	 * An optional comparator for sorting the list after updating
	 * 
	 * @return sortComparator
	 */
	public Comparator<E> getSortComparator()
	{
		return sortComparator;
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.SynchronizedCollection#update(java.util.Collection)
	 */
	@Override
	protected synchronized void update(Collection<E> content)
	{
		super.update(content);
		if(this.sortComparator != null)
			Collections.sort(this.collection, this.sortComparator);
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
		return Collections.unmodifiableList(collection.subList(fromIndex, toIndex));
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
