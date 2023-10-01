package ultimate.karopapier.cron;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;

public class KaroCronToolTest
{
	@Test
	@SuppressWarnings("deprecation")
	public void test_isReady()
	{
		String when;

		when = "5";

		assertFalse(KaroCronTool.isReady(when, 4, null));
		assertTrue(KaroCronTool.isReady(when, 5, null));
		assertTrue(KaroCronTool.isReady(when, 6, null));

		when = "2022.02.15";

		assertFalse(KaroCronTool.isReady(when, 4, new Date(122, 1, 14)));
		assertFalse(KaroCronTool.isReady(when, 4, new Date(122, 1, 14, 12, 0, 0)));
		assertFalse(KaroCronTool.isReady(when, 5, new Date(122, 1, 15)));
		assertTrue(KaroCronTool.isReady(when, 5, new Date(122, 1, 15, 1, 0, 0)));
		assertTrue(KaroCronTool.isReady(when, 5, new Date(122, 1, 15, 12, 0, 0)));
		assertTrue(KaroCronTool.isReady(when, 6, new Date(122, 1, 16)));

		when += " 06:00:00";

		assertFalse(KaroCronTool.isReady(when, 4, new Date(122, 1, 14)));
		assertFalse(KaroCronTool.isReady(when, 4, new Date(122, 1, 14, 12, 0, 0)));
		assertFalse(KaroCronTool.isReady(when, 5, new Date(122, 1, 15)));
		assertFalse(KaroCronTool.isReady(when, 5, new Date(122, 1, 15, 1, 0, 0)));
		assertTrue(KaroCronTool.isReady(when, 5, new Date(122, 1, 15, 12, 0, 0)));
		assertTrue(KaroCronTool.isReady(when, 6, new Date(122, 1, 16)));
	}
}
