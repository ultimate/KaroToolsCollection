package muskel2.util;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * Comparator, der zwei Objekte anhand einer bestimmten Methode vergleicht. Die
 * Methode wird dabei �ber ihren Namen angegeben. Durch eine Zus�tzliche
 * Orientierung kann zwischen auf- und absteigend gewechselt werden.
 * 
 * @author ultimate
 * @param <T>
 */
public class MethodComparator<T> implements Comparator<T>, Serializable
{
	/**
	 * Orientierung f�r aufsteigende Sortierung
	 * 
	 * @see org.peu.peu.util.SortUtil#ASCENDING
	 */
	public static final int		ASCENDING			= SortUtil.ASCENDING;
	/**
	 * Orientierung f�r absteigende Sortierung
	 * 
	 * @see org.peu.peu.util.SortUtil#DESCENDING
	 */
	public static final int		DESCENDING			= SortUtil.DESCENDING;

	/**
	 * Default serialVersionUID
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Name der Methode f�r den Vergleich
	 */
	private String				methodName;
	/**
	 * Die Orientierung.
	 * Es sind ASCENDING und DESCENDING empfehlenswert, es sind jedoch auch
	 * andere Faktoren m�glich. 0 ist nicht m�glich, da sonst das Ergebnis immer
	 * 0 ist.
	 * 
	 * @see org.peu.peu.util.SortUtil#ASCENDING
	 * @see org.peu.peu.util.SortUtil#DESCENDING
	 */
	private int					orientation;

	/**
	 * Erstellt einen neuen Comparator mit Methodenname und Orientierung
	 * 
	 * @param methodName - Name der Methode f�r den Vergleich
	 * @param orientation - die Orientierung
	 */
	public MethodComparator(String methodName, int orientation)
	{
		if(orientation == 0) throw new IllegalArgumentException("orientation must not be 0");
		this.methodName = methodName;
		this.orientation = orientation;
	}

	/**
	 * Vergleicht die zwei gegebenen Objekte anhand der definierten Methode. Die
	 * Objekte selbst d�rfen nicht null sein, der R�ckgabewert der Methode
	 * dagegen darf null sein.
	 */
	@SuppressWarnings("unchecked")
	public int compare(T o1, T o2)
	{
		if(o1 == null) throw new IllegalArgumentException("o1 must not be null");
		if(o2 == null) throw new IllegalArgumentException("o2 must not be null");
		try
		{
			Method m1 = o1.getClass().getMethod(this.methodName);
			Comparable<Object> key1 = (Comparable<Object>) m1.invoke(o1);
			Method m2 = o2.getClass().getMethod(this.methodName);
			Comparable<Object> key2 = (Comparable<Object>) m2.invoke(o2);

			if(key1 == null)
			{
				if(key2 == null)
					return 0;
				else
					return -orientation;
			}
			else if(key2 == null)
			{
				return orientation;
			}
			return orientation * key1.compareTo(key2);
		}
		catch(IllegalAccessException e)
		{
		}
		catch(InvocationTargetException e)
		{
		}
		catch(SecurityException e)
		{
		}
		catch(NoSuchMethodException e)
		{
		}
		return 0;
	}
}