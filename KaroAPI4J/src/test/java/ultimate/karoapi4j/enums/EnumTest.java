package ultimate.karoapi4j.enums;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

public class EnumTest
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger logger = LogManager.getLogger(getClass());

	@Test
	public void test_EnumGameTC_values() throws InterruptedException, ExecutionException
	{
		EnumGameTC[] original = EnumGameTC.values();

		EnumGameTC[] withRandom = EnumGameTC.values(true);
		EnumGameTC[] withoutRandom = EnumGameTC.values(false);

		logger.info("original      = " + Arrays.asList(original));
		logger.info("withRandom    = " + Arrays.asList(withRandom));
		logger.info("withoutRandom = " + Arrays.asList(withoutRandom));

		assertArrayEquals(original, withRandom);

		assertEquals(original.length - 1, withoutRandom.length);

		boolean found;
		boolean foundRandom;
		for(EnumGameTC e : original)
		{
			if(e.name().equals("random"))
				continue;

			found = false;
			foundRandom = false;

			for(EnumGameTC e2 : withoutRandom)
			{
				if(e2 == e)
					found = true;
				if(e2 == EnumGameTC.random)
					foundRandom = true;
			}

			assertTrue(found);
			assertFalse(foundRandom);
		}
	}

	@Test
	public void test_EnumGameDirection_values() throws InterruptedException, ExecutionException
	{
		EnumGameDirection[] original = EnumGameDirection.values();

		EnumGameDirection[] withRandom = EnumGameDirection.values(true);
		EnumGameDirection[] withoutRandom = EnumGameDirection.values(false);

		logger.info("original      = " + Arrays.asList(original));
		logger.info("withRandom    = " + Arrays.asList(withRandom));
		logger.info("withoutRandom = " + Arrays.asList(withoutRandom));

		assertArrayEquals(original, withRandom);

		assertEquals(original.length - 1, withoutRandom.length);

		boolean found;
		boolean foundRandom;
		for(EnumGameDirection e : original)
		{
			if(e.name().equals("random"))
				continue;

			found = false;
			foundRandom = false;

			for(EnumGameDirection e2 : withoutRandom)
			{
				if(e2 == e)
					found = true;
				if(e2 == EnumGameDirection.random)
					foundRandom = true;
			}

			assertTrue(found);
			assertFalse(foundRandom);
		}
	}
}
