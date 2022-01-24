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

public class PropertiesUtil
{
	public static Properties loadProperties(File file) throws IOException
	{
		return loadProperties(file, false);
	}
	
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
	
	public static void storeProperties(File file, Properties properties, String comments) throws IOException
	{
		storeProperties(file, properties, comments, false);
	}
	
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