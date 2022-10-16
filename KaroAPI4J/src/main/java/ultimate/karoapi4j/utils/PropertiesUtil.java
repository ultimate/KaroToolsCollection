package ultimate.karoapi4j.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Util class for storing and loading {@link Properties}
 * 
 * @author ultimate
 */
public abstract class PropertiesUtil
{
	/**
	 * prevent instantiation
	 */
	private PropertiesUtil()
	{

	}

	/**
	 * Load {@link Properties} from the given {@link InputStream}
	 * 
	 * @param is - the {@link InputStream}
	 * @param zipped - use zip-format?
	 * @return the {@link Properties}
	 * @throws IOException - if loading fails
	 */
	public static Properties loadProperties(InputStream is, boolean zipped) throws IOException
	{
		if(zipped)
			is = new GZIPInputStream(is);
		is = new BufferedInputStream(is);
		Properties properties = new Properties();
		properties.load(is);
		is.close();
		return properties;
	}

	/**
	 * Load {@link Properties} from the given {@link File}.<br>
	 * Convenience for <code>PropertiesUtil.loadProperties(file, false)</code>
	 * 
	 * @see PropertiesUtil#loadProperties(File, boolean)
	 * @param file - the {@link File}
	 * @return the {@link Properties}
	 * @throws IOException - if loading fails
	 */
	public static Properties loadProperties(File file) throws IOException
	{
		return loadProperties(file, false);
	}

	/**
	 * Load {@link Properties} from the given {@link File}
	 * 
	 * @param file - the {@link File}
	 * @param zipped - use zip-format?
	 * @return the {@link Properties}
	 * @throws IOException - if loading fails
	 */
	public static Properties loadProperties(File file, boolean zipped) throws IOException
	{
		return loadProperties(new FileInputStream(file), zipped);
	}

	/**
	 * Load {@link Properties} with the given name from a file.<br>
	 * Convenience for <code>PropertiesUtil.loadProperties(contextClass, name, false)</code>
	 * 
	 * @see PropertiesUtil#loadProperties(Class, String, boolean)
	 * @param contextClass - the class context used to locate the file
	 * @param fileName - the name of the properties
	 * @return the {@link Properties}
	 * @throws IOException - if loading fails
	 */
	public static Properties loadProperties(Class<?> contextClass, String fileName) throws IOException
	{
		return loadProperties(contextClass, fileName, false);
	}

	/**
	 * Load {@link Properties} with the given name from a file.<br>
	 * This method will look up the matching ressource <code>%name%.properties</code> with the classloader of the class passed as an argument. This
	 * way also properties in jar files can be accessed.<br>
	 * Convenience for
	 * <code>PropertiesUtil.loadProperties(contextClass.getClassLoader().getResourceAsStream(fileName), zipped)</code>
	 * 
	 * @see PropertiesUtil#loadProperties(File, boolean)
	 * @param contextClass - the class context used to locate the file
	 * @param fileName - the name of the properties
	 * @param zipped - use zip-format?
	 * @return the {@link Properties}
	 * @throws IOException - if loading fails
	 */
	public static Properties loadProperties(Class<?> contextClass, String fileName, boolean zipped) throws IOException
	{
		return loadProperties(contextClass.getClassLoader().getResourceAsStream(fileName), zipped);
	}

	/**
	 * Store {@link Properties} to the given {@link File}.<br>
	 * Convenience for <code>PropertiesUtil.storeProperties(file, properties, false)</code>
	 * 
	 * @see PropertiesUtil#storeProperties(File, boolean)
	 * @param file - the {@link File}
	 * @param properties - the {@link Properties} to store
	 * @param comments - optional comments to write
	 * @throws IOException - if storing fails
	 */
	public static void storeProperties(File file, Properties properties, String comments) throws IOException
	{
		storeProperties(file, properties, comments, false);
	}

	/**
	 * Store {@link Properties} to the given {@link File}.<br>
	 * 
	 * @param file - the {@link File}
	 * @param properties - the {@link Properties} to store
	 * @param comments - optional comments to write
	 * @param zipped - use zip-format?
	 * @throws IOException - if storing fails
	 */
	public static void storeProperties(File file, Properties properties, String comments, boolean zipped) throws IOException
	{
		// sort keys for better storing
		Properties tmp = new Properties() {
			private static final long serialVersionUID = 1L;
			
			@SuppressWarnings("unchecked")
			public synchronized Enumeration<Object> keys()
			{
				ArrayList<Object> keys = Collections.list(super.keys());
				Collections.sort((ArrayList<String>) (ArrayList<?>) keys);
				return Collections.enumeration(keys);
			}
			
			// https://stackoverflow.com/a/54355584/4090157
			public synchronized Set<Map.Entry<Object, Object>> entrySet()
			{
				return Collections.synchronizedSet(super.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().toString())).collect(Collectors.toCollection(LinkedHashSet::new)));
			}
		};
		tmp.putAll(properties);

		OutputStream os = new FileOutputStream(file);
		if(zipped)
			os = new GZIPOutputStream(os);
		os = new BufferedOutputStream(os);
		tmp.store(os, comments);
		os.flush();
		os.close();
	}
}