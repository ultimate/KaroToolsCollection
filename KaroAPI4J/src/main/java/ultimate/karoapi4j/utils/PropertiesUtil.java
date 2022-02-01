package ultimate.karoapi4j.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
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
		InputStream is = new FileInputStream(file);
		if(zipped)
			is = new GZIPInputStream(is);
		is = new BufferedInputStream(is);
		Properties properties = new Properties();
		properties.load(is);
		is.close();
		return properties;
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
		OutputStream os = new FileOutputStream(file);
		if(zipped)
			os = new GZIPOutputStream(os);
		os = new BufferedOutputStream(os);
		properties.store(os, comments);
		os.flush();
		os.close();
	}
}