package ultimate.karoapi4j.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

public class WatchdogTest
{
	private static final long TOLERANCE = 20;
	
	@Test
	public void test_timeout() throws InterruptedException
	{
		// time values
		int interval = 1;
		int timeout = 500;
		int pingInterval = 50;

		// some variables to store the info from the timeout event
		AtomicLong time = new AtomicLong();
		String[] message = new String[1];

		// prepare watchdog
		Watchdog watchdog = new Watchdog(interval, timeout, (msg) -> {
			message[0] = msg;
			time.set(System.currentTimeMillis());
		});

		long start = System.currentTimeMillis();

		CompletableFuture<Void> cf = CompletableFuture.runAsync(watchdog);

		int pings = 5;
		for(int i = 1; i <= pings; i++)
		{
			Thread.sleep(pingInterval);
			watchdog.notifyActive("ping " + i);
		}

		cf.join();

		// values should have been set
		String expectedMessage = "ping " + pings;
		long expectedTime = start + pings * pingInterval + timeout;

		assertEquals(expectedMessage, message[0]);
		assertEquals(expectedTime, time.get(), TOLERANCE);
		
		// check last update
		assertEquals(start + pings * pingInterval, watchdog.getLastUpdate(), TOLERANCE);
	}

	@Test
	public void test_cancel() throws InterruptedException
	{
		// time values
		int interval = 1;
		int timeout = 500;
		int pingInterval = 50;

		// some variables to store the info from the timeout event
		AtomicLong time = new AtomicLong();
		String[] message = new String[1];

		// prepare watchdog
		Watchdog watchdog = new Watchdog(interval, timeout, (msg) -> {
			message[0] = msg;
			time.set(System.currentTimeMillis());
		});

		long start = System.currentTimeMillis();

		CompletableFuture<Void> cf = CompletableFuture.runAsync(watchdog);

		int pings = 5;
		for(int i = 1; i <= pings; i++)
		{
			Thread.sleep(pingInterval);
			watchdog.notifyActive("ping " + i);
		}
		
		watchdog.cancel();

		cf.join();
		
		// values should not have been set
		assertNull(message[0]);
		assertEquals(0, time.get());

		// check last update
		assertEquals(start + pings * pingInterval, watchdog.getLastUpdate(), TOLERANCE);
	}
}
