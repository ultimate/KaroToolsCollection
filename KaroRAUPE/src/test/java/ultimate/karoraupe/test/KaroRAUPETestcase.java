package ultimate.karoraupe.test;

import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.utils.PropertiesUtil;

/**
 * Base class for all test cases that need a {@link KaroAPI} instance
 * 
 * @author ultimate
 */
@TestInstance(Lifecycle.PER_CLASS)
public class KaroRAUPETestcase
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger	logger	= LogManager.getLogger(getClass());

	/**
	 * The {@link Properties} used to initiate the {@link KaroAPI}
	 */
	protected static Properties			properties;
	/**
	 * The {@link KaroAPI} instance
	 */
	protected static KaroAPI			karoAPI;

	/**
	 * Load the {@link Properties} and initiate the {@link KaroAPI} instance once per class
	 * 
	 * @throws IOException - if loading the {@link Properties} fails
	 */
	@BeforeAll
	public void setUpOnce() throws IOException
	{
		properties = PropertiesUtil.loadProperties(KaroRAUPETestcase.class, "login.properties");
		logger.info("properties loaded: " + properties);

		karoAPI = new KaroAPI(properties.getProperty("karoAPI.user"), properties.getProperty("karoAPI.password"));
		logger.info("KaroAPI initialized");
	}
}