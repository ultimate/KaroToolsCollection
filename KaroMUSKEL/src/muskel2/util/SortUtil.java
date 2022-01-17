package muskel2.util;

import java.util.Collections;
import java.util.List;

/**
 * Util-Klasse zum sortieren von Listen anhand eines bestimmten Properties der enthaltenen Objekte.
 * Zum Vergleich der Objekte wird ein MethodComparator verwendet.
 * 
 * @author ultimate
 */
public abstract class SortUtil
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
	 * @see org.peu.peu.util.help.MethodComparator
	 * @param <T> - der Typ der Liste
	 * @param unsorted - die Liste
	 * @param methodName - der Methodenname, der die Methode angibt anhand der sortiert wird
	 * @return die Liste
	 */
	public static <T> List<T> sortListAscending(List<T> unsorted, String methodName)
	{
		return sortList(unsorted, methodName, ASCENDING);
	}

	/**
	 * Sortiert eine Liste absteigend. Die �bergebene Liste wird direkt sortiert und f�r die
	 * einfachere Verwendung zus�tzlich zur�ckgegeben.
	 * 
	 * @see SortUtil#ASCENDING
	 * @see SortUtil#DESCENDING
	 * @see org.peu.peu.util.help.MethodComparator
	 * @param <T> - der Typ der Liste
	 * @param unsorted - die Liste
	 * @param methodName - der Methodenname, der die Methode angibt anhand der sortiert wird
	 * @return die Liste
	 */
	public static <T> List<T> sortListDescending(List<T> unsorted, String methodName)
	{
		return sortList(unsorted, methodName, DESCENDING);
	}

	/**
	 * Sortiert eine Liste auf oder absteigend. Die �bergebene Liste wird direkt sortiert und f�r
	 * die einfachere Verwendung zus�tzlich zur�ckgegeben.
	 * 
	 * @see org.peu.peu.util.help.MethodComparator
	 * @param <T> - der Typ der Liste
	 * @param unsorted - die Liste
	 * @param methodName - der Methodenname, der die Methode angibt anhand der sortiert wird
	 * @param orientation - die Orientierung
	 * @return die Liste
	 */
	public static <T extends Object> List<T> sortList(List<T> unsorted, String methodName, int orientation)
	{
		Collections.sort(unsorted, new MethodComparator<T>(methodName, orientation));
		return unsorted;
	}
}
