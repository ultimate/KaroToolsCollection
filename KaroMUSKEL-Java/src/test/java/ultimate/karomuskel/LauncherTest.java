package ultimate.karomuskel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.KaroAPI;

public class LauncherTest
{
	private static final String	apiVersion	= "1.1.2";
	private static final String	appVersion	= "3.0.5";

	@Test
	public void test_APIVersion() throws InterruptedException, ExecutionException
	{
		assertNotNull(KaroAPI.getVersion());
		assertEquals(apiVersion, KaroAPI.getVersion());
	}

	@Test
	public void test_AppVersion() throws InterruptedException, ExecutionException
	{
		Launcher.loadConfig(null);

		assertNotNull(KaroAPI.getApplicationName());
		assertEquals("KaroMUSKEL", KaroAPI.getApplicationName());

		assertNotNull(KaroAPI.getApplicationVersion());
		assertEquals(appVersion, KaroAPI.getApplicationVersion());

		assertNotNull(KaroAPI.getUserAgent());
		assertEquals("KaroAPI4J/" + apiVersion + " KaroMUSKEL/" + appVersion + " (Java " + System.getProperty("java.version") + ")", KaroAPI.getUserAgent());
	}
}
