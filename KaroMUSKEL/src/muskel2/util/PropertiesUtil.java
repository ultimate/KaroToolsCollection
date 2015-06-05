package muskel2.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtil
{
	public static Properties loadProperties(File file) throws IOException
	{
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		Properties properties = new Properties();
		properties.load(bis);
		bis.close();
		return properties;
	}
}