package ultimate.karomuskel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.utils.Version;

public class LauncherTest
{
	private static final Version	apiVersion	= new Version("1.3.1");
	private static final Version	appVersion	= new Version("3.2.2");

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
