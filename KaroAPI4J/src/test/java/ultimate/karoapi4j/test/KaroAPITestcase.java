package ultimate.karoapi4j.test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.utils.PropertiesUtil;

@TestInstance(Lifecycle.PER_CLASS)
public class KaroAPITestcase
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger	logger		= LoggerFactory.getLogger(getClass());

	protected static Properties properties;
	protected static KaroAPI karoAPI;
	
	@BeforeAll
	public void setUpOnce() throws IOException
	{
		properties = PropertiesUtil.loadProperties(new File("target/test-classes/login.properties"));
		logger.info("properties loaded: " + properties);
		karoAPI = new KaroAPI(properties.getProperty("karoapi.user"), properties.getProperty("karoapi.password"));
		logger.info("KaroAPI initialized");
	}
}
