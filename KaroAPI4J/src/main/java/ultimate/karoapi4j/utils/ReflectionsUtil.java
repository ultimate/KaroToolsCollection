package ultimate.karoapi4j.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Util class for some reflections based operations (for example copying fields from one object to another).
 * 
 * @author ultimate
 */
public abstract class ReflectionsUtil
{
	private static transient final Logger logger = LogManager.getLogger(ReflectionsUtil.class);

	/**
	 * Prevent instantiation
	 */
	private ReflectionsUtil()
	{

	}

	/**
	 * Copy all fields from one object to another.<br>
	 * Can be used for example, if a target object shall be updated from an API
	 * 
	 * @param <T>
	 * @param source - the source which contains the fields to copy
	 * @param target - the target to copy the fields to
	 * @param includeNull - whether also to copy null values (will overwrite existing values)
	 */
	public static <T> void copyFields(T source, T target, boolean includeNull)
	{
		if(source == null || target == null)
			throw new IllegalArgumentException("from and to must not be null!");
		@SuppressWarnings("unchecked")
		Class<T> cls = (Class<T>) source.getClass();

		Method[] methods = cls.getMethods();

		for(Method m : methods)
		{
			String getterName = m.getName();
			String setterName;
			if(getterName.startsWith("get"))
				setterName = getterName.replaceFirst("get", "set");
			else if(getterName.startsWith("is"))
				setterName = getterName.replaceFirst("is", "set");
			else
				continue;
			
			if(m.getParameterCount() > 0)
				continue;
			if(getterName.equals("getClass"))
				continue;

			try
			{
				Object value = m.invoke(source);
				if(value == null && !includeNull)
					continue;
				logger.debug("copying field: " + getterName + " -> " + setterName + " value = " + value);
				Method setter = cls.getMethod(setterName, m.getReturnType());
				setter.invoke(target, value);
			}
			catch(NoSuchMethodException e)
			{
				logger.trace("ignoring field: " + getterName + " -> no setter");
			}
			catch(IllegalArgumentException | IllegalAccessException | InvocationTargetException e)
			{
				logger.warn("error copying field: " + getterName + " -> " + setterName);
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T, F> F getField(T object, String fieldName)
	{
		String getterName = null;
		Object value = null;
		try
		{
			getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
			Method m = object.getClass().getMethod(getterName);
			value = m.invoke(object);
			return (F) value;
		}
		catch(NoSuchMethodException e)
		{
			logger.error("no such getter: " + getterName);
			e.printStackTrace();
		}
		catch(SecurityException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e)
		{
			logger.error("error accessing field: " + getterName);
			e.printStackTrace();
		}
		catch(ClassCastException e)
		{
			logger.error("field is of wrong type: " + (value == null ? null : value.getClass()));
			e.printStackTrace();
		}
		return null;
	}

	/*
	public static <T> T fromMap(Class<T> classRef, Map<String, Object> map)
	{
		T entity = classRef.getDeclaredConstructor().newInstance();
		
		return entity;
	}
	*/
}
