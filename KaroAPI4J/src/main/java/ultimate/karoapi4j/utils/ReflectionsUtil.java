package ultimate.karoapi4j.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util class for some reflections based operations (for example copying fields from one object to another).
 * 
 * @author ultimate
 */
public abstract class ReflectionsUtil
{
	private static final Logger logger = LoggerFactory.getLogger(ReflectionsUtil.class);

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
			catch(NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e)
			{
				logger.warn("error copying field: " + getterName + " -> " + setterName);
				e.printStackTrace();
			}
		}
	}
}
