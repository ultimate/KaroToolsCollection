package ultimate.karoapi4j.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ReflectionsUtil
{
	private static final Logger logger = LoggerFactory.getLogger(ReflectionsUtil.class);

	/**
	 * Prevent instantiation
	 */
	private ReflectionsUtil()
	{

	}

	public static <T> void copyFields(T from, T to, boolean includeNull)
	{
		if(from == null || to == null)
			throw new IllegalArgumentException("from and to must not be null!");
		@SuppressWarnings("unchecked")
		Class<T> cls = (Class<T>) from.getClass();

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

			try
			{
				Object value = m.invoke(from);
				if(value == null && !includeNull)
					continue;
				logger.debug("copying field: " + getterName + " -> " + setterName + " value = " + value);
				Method setter = cls.getMethod(setterName, m.getReturnType());
				setter.invoke(to, value);
			}
			catch(NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e)
			{
				logger.warn("error copying field: " + getterName + " -> " + setterName);
				e.printStackTrace();
			}
		}
	}
}
