package ultimate.karoapi4j.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.model.base.Identifiable;

/**
 * Util-Klasse zum sortieren von Listen anhand eines bestimmten Properties der enthaltenen Objekte.
 * Zum Vergleich der Objekte wird ein MethodComparator verwendet.
 * 
 * @author ultimate
 */
public abstract class CollectionsUtil
{
	/**
	 * Logger-Instance
	 */
	protected static transient final Logger logger = LogManager.getLogger(CollectionsUtil.class);
	/**
	 * Orientierung f�r aufsteigende Sortierung
	 */
	public static final int					ASCENDING	= 1;
	/**
	 * Orientierung f�r abfsteigende Sortierung
	 */
	public static final int					DESCENDING	= -1;
	
	/**
	 * prevent instantiation
	 */
	private CollectionsUtil()
	{
		
	}

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

	/**
	 * Check whether two collections are equal in length and content. Therefore a {@link MethodComparator} is used to compare the entries of both
	 * collections pairwise in the order they are contained in the collections.<br>
	 * Short for <code>equals(c1, c2, new MethodComparator<T>(methodName, 1));</code>
	 * 
	 * @see MethodComparator
	 * 
	 * @param <T>
	 * @param c1 - the first collection
	 * @param c2 - the second collection
	 * @param methodName - the method name to use to compare the entries
	 * @return true or false
	 */
	public static <T> boolean equals(Collection<T> c1, Collection<T> c2, String methodName)
	{
		return equals(c1, c2, new MethodComparator<T>(methodName, 1));
	}

	/**
	 * Check whether two collections are equal in length and content. Therefore the given comparator is used to compare the entries of both
	 * collections pairwise in the order they are contained in the collections.
	 * 
	 * @param <T>
	 * @param c1 - the first collection
	 * @param c2 - the second collection
	 * @param comparator - the comparator to use
	 * @return true or false
	 */
	public static <T> boolean equals(Collection<T> c1, Collection<T> c2, Comparator<T> comparator)
	{
		if(c1.size() != c2.size())
			return false;
			
		T o1, o2;
		Iterator<T> i1 = c1.iterator();
		Iterator<T> i2 = c2.iterator();
		while(i1.hasNext() && i2.hasNext())
		{
			o1 = i1.next();
			o2 = i2.next();
			if(comparator.compare(o1, o2) != 0)
				return false;
		}
		return true;
	}

	/**
	 * Check whether two set are equal in length and content. Therefore a {@link MethodComparator} is used to check whether all entries from the first set are contained in the second and vice versa.<br>
	 * Short for <code>equals(s1, s2, new MethodComparator<T>(methodName, 1));</code>
	 * 
	 * @see MethodComparator
	 * 
	 * @param <T>
	 * @param s1 - the first set
	 * @param s2 - the second set
	 * @param methodName - the method name to use to compare the entries
	 * @return true or false
	 */
	public static <T> boolean equals(Set<T> s1, Set<T> s2, String methodName)
	{
		return equals(s1, s2, new MethodComparator<T>(methodName, 1));
	}

	/**
	 * Check whether two sets are equal in length and content. Therefore the given comparator is used to check whether all entries from the first set are contained in the second and vice versa.<br>
	 * 
	 * @param <T>
	 * @param s1 - the first set
	 * @param s2 - the second set
	 * @param comparator - the comparator to use
	 * @return true or false
	 */
	public static <T> boolean equals(Set<T> s1, Set<T> s2, Comparator<T> comparator)
	{
		if(s1.size() != s2.size())
			return false;

		T o1, o2;
		Iterator<T> i1 = s1.iterator();
		boolean found;
		while(i1.hasNext())
		{
			o1 = i1.next();
			found = false;
			Iterator<T> i2 = s2.iterator();
			while(i2.hasNext())
			{
				o2 = i2.next();
				if(comparator.compare(o1, o2) == 0)
				{
					found = true;
					break;
				}
			}
			if(!found)
				return false;
		}
		// vice versa is not really needed if we compare the size in the beginning
		return true;
	}

	/**
	 * Check whether a collection contains an object matching the given {@link Predicate}
	 * 
	 * @param <T>
	 * @param collection - the collection to check
	 * @param predicate - the predicate to apply
	 * @return true or false
	 */
	public static <T> boolean contains(Collection<T> collection, Predicate<T> predicate)
	{
		boolean found = false;
		for(T entity : collection)
		{
			if(predicate.test(entity))
			{
				found = true;
				break;
			}
		}
		return found;
	}

	/**
	 * Cout the occurrences of entreis in a collection that match the given {@link Predicate}
	 * 
	 * @param <T>
	 * @param collection - the collection to check
	 * @param predicate - the predicate to apply
	 * @return the entry count matching the predicate
	 */
	public static <T> int count(Collection<T> collection, Predicate<T> predicate)
	{
		int count = 0;
		for(T entity : collection)
		{
			if(predicate.test(entity))
				count++;
		}
		return count;
	}

	/**
	 * transform a list of objects which are actually key value pairs
	 * <code>[{id:1,text:"a"},{id:2,text:"b"}, ...]</code>
	 * to a map where the ids are the keys and the texts are the values
	 * <code>{1:"a",2:"b",...}</code>
	 * 
	 * @param <T>
	 * @param idList - the original id list
	 * @param idKey - the key for the ids
	 * @param valueKey - the key for the values
	 * @return the map of ids & values
	 */
	@SuppressWarnings("unchecked")
	public static <T> Map<Integer, T> toMap(Collection<Map<String, Object>> idList, String idKey, String valueKey)
	{
		HashMap<Integer, T> map = new HashMap<>();
		for(Map<String, Object> m : idList)
		{
			int key = (int) m.get(idKey);
			T value = (T) m.get(valueKey);
			map.put(key, value);
		}
		return map;
	}

	/**
	 * Transform a list of identifiable objects into a map where the identifier is used as the key.
	 * 
	 * @param <T>
	 * @param list - the list to convert
	 * @return the id-map
	 */
	public static <T extends Identifiable> Map<Integer, T> toMap(Collection<T> list)
	{
		HashMap<Integer, T> map = new HashMap<>();
		for(T object : list)
		{
			Integer id = object.getId();
			if(map.containsKey(id))
				logger.warn("duplicate id: " + id);
			map.put(id, object);
		}
		return map;
	}
	/**
	 * Transform a list of identifiable objects into a map where the identifier is used as the key.
	 * 
	 * @param <T>
	 * @param list - the list to convert
	 * @return the id-map
	 */
	public static <T1 extends Identifiable, T2> Map<Integer, T2> flattenMap(Map<Integer, T1> map, String field)
	{
		HashMap<Integer, T2> flattenedMap = new HashMap<>();
		for(Entry<Integer, T1> entry : map.entrySet())
		{
			flattenedMap.put(entry.getKey(), ReflectionsUtil.getField(entry.getValue(), field));
		}
		return flattenedMap;
	}
	
	public static int[] toIDArray(Collection<? extends Identifiable> list)
	{
		int[] array = new int[list.size()];
		int cursor = 0;
		for(Identifiable i : list)
			array[cursor++] = i.getId();
		return array;
	}
}
