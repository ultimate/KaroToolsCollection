package ultimate.karoapi4j.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Util-Klasse zum sortieren von Listen anhand eines bestimmten Properties der enthaltenen Objekte.
 * Zum Vergleich der Objekte wird ein MethodComparator verwendet.
 * 
 * @author ultimate
 */
public abstract class CollectionsUtil
{
	/**
	 * Orientierung f�r aufsteigende Sortierung
	 */
	public static final int	ASCENDING	= 1;
	/**
	 * Orientierung f�r abfsteigende Sortierung
	 */
	public static final int	DESCENDING	= -1;

	/**
	 * Sortiert eine Liste aufsteigend. Die �bergebene Liste wird direkt sortiert und f�r die
	 * einfachere Verwendung zus�tzlich zur�ckgegeben.
	 * 
	 * @see MethodComparator
	 * @param <T> - der Typ der Liste
	 * @param unsorted - die Liste
	 * @param methodName - der Methodenname, der die Methode angibt anhand der sortiert wird
	 * @return die Liste
	 */
	public static <T> List<T> sortAscending(List<T> unsorted, String methodName)
	{
		return sort(unsorted, methodName, ASCENDING);
	}

	/**
	 * Sortiert eine Liste absteigend. Die �bergebene Liste wird direkt sortiert und f�r die
	 * einfachere Verwendung zus�tzlich zur�ckgegeben.
	 * 
	 * @see CollectionsUtil#ASCENDING
	 * @see CollectionsUtil#DESCENDING
	 * @see MethodComparator
	 * @param <T> - der Typ der Liste
	 * @param unsorted - die Liste
	 * @param methodName - der Methodenname, der die Methode angibt anhand der sortiert wird
	 * @return die Liste
	 */
	public static <T> List<T> sortDescending(List<T> unsorted, String methodName)
	{
		return sort(unsorted, methodName, DESCENDING);
	}

	/**
	 * Sortiert eine Liste auf oder absteigend. Die �bergebene Liste wird direkt sortiert und f�r
	 * die einfachere Verwendung zus�tzlich zur�ckgegeben.
	 * 
	 * @see MethodComparator
	 * @param <T> - der Typ der Liste
	 * @param unsorted - die Liste
	 * @param methodName - der Methodenname, der die Methode angibt anhand der sortiert wird
	 * @param orientation - die Orientierung
	 * @return die Liste
	 */
	public static <T> List<T> sort(List<T> unsorted, String methodName, int orientation)
	{
		Collections.sort(unsorted, new MethodComparator<T>(methodName, orientation));
		return unsorted;
	}
	
	public static <T> boolean equals(Collection<T> c1, Collection<T> c2, String methodName)
	{
		return equals(c1, c2, new MethodComparator<T>(methodName, 1));
	}
	
	public static <T> boolean equals(Collection<T> c1, Collection<T> c2, Comparator<T> comparator)
	{
		if(c1.size() != c2.size())
			return false;
		
		Iterator<T> i1 = c1.iterator();
		Iterator<T> i2 = c2.iterator();
		T o1, o2;
		
		boolean equals = true;
		while(i1.hasNext() && i2.hasNext())
		{
			o1 = i1.next();
			o2 = i2.next();
			if(comparator.compare(o1, o2) != 0)
			{
				equals = false;
				break;
			}
		}
		return equals;
	}
	
	public static <T> boolean contains(Collection<T> collection, Predicate<T> predicate)
	{
		boolean found = false;
		for(T entity: collection)
		{
			if(predicate.test(entity))
			{
				found = true;
				break;
			}
		}
		return found;
	}
	
	public static <T> int count(Collection<T> collection, Predicate<T> predicate)
	{
		int count = 0;
		for(T entity: collection)
		{
			if(predicate.test(entity))
				count++;
		}
		return count;
	}
}