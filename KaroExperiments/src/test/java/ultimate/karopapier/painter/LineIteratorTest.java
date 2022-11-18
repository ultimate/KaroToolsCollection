package ultimate.karopapier.painter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

public class LineIteratorTest
{
	protected transient final Logger	logger	= LogManager.getLogger(getClass());
	private final int					GRID	= 20;

	@Test
	public void test_iteration_horizontal_positive()
	{
		LineIterator iter = new LineIterator(new Point(GRID, GRID), new Point(2 * GRID, GRID));
		logger.debug("iterating: from " + iter.getStart() + " to " + iter.getEnd());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point(GRID + i, GRID), iter.next());
		}
		assertFalse(iter.hasNext());
	}

	@Test
	public void test_iteration_horizontal_negative()
	{
		LineIterator iter = new LineIterator(new Point(GRID, GRID), new Point(0, GRID));
		logger.debug("iterating: from " + iter.getStart() + " to " + iter.getEnd());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point(GRID - i, GRID), iter.next());
		}
		assertFalse(iter.hasNext());
	}

	@Test
	public void test_iteration_vertical_positive()
	{
		LineIterator iter = new LineIterator(new Point(GRID, GRID), new Point(GRID, 2 * GRID));
		logger.debug("iterating: from " + iter.getStart() + " to " + iter.getEnd());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point(GRID, GRID + i), iter.next());
		}
		assertFalse(iter.hasNext());
	}

	@Test
	public void test_iteration_vertical_negative()
	{
		LineIterator iter = new LineIterator(new Point(GRID, GRID), new Point(GRID, 0));
		logger.debug("iterating: from " + iter.getStart() + " to " + iter.getEnd());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point(GRID, GRID - i), iter.next());
		}
		assertFalse(iter.hasNext());
	}

	@Test
	public void test_iteration_diagonal_positive_positive()
	{
		LineIterator iter = new LineIterator(new Point(GRID, GRID), new Point(2 * GRID, 2 * GRID));
		logger.debug("iterating: from " + iter.getStart() + " to " + iter.getEnd());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point(GRID + i, GRID + i), iter.next());
		}
		assertFalse(iter.hasNext());
	}

	@Test
	public void test_iteration_diagonal_positive_negative()
	{
		LineIterator iter = new LineIterator(new Point(GRID, GRID), new Point(2 * GRID, 0));
		logger.debug("iterating: from " + iter.getStart() + " to " + iter.getEnd());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point(GRID + i, GRID - i), iter.next());
		}
		assertFalse(iter.hasNext());
	}

	@Test
	public void test_iteration_diagonal_negative_negative()
	{
		LineIterator iter = new LineIterator(new Point(GRID, GRID), new Point(0, 0));
		logger.debug("iterating: from " + iter.getStart() + " to " + iter.getEnd());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point(GRID - i, GRID - i), iter.next());
		}
		assertFalse(iter.hasNext());
	}

	@Test
	public void test_iteration_diagonal_negative_positive()
	{
		LineIterator iter = new LineIterator(new Point(GRID, GRID), new Point(0, 2 * GRID));
		logger.debug("iterating: from " + iter.getStart() + " to " + iter.getEnd());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point(GRID - i, GRID + i), iter.next());
		}
		assertFalse(iter.hasNext());
	}

	@Test
	public void test_iteration_slope_1()
	{
		LineIterator iter = new LineIterator(new Point(GRID, GRID), new Point((int) (2.0 * GRID), (int) (1.5 * GRID)));
		logger.debug("iterating: from " + iter.getStart() + " to " + iter.getEnd());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point(GRID + i, (int) Math.floor(GRID + i / 2.0)), iter.next());
			logger.debug("" + iter.current());
		}
		assertFalse(iter.hasNext());
	}

	@Test
	public void test_iteration_slope_2()
	{
		LineIterator iter = new LineIterator(new Point(GRID, GRID), new Point((int) (1.5 * GRID), (int) (2.0 * GRID)));
		logger.debug("iterating: from " + iter.getStart() + " to " + iter.getEnd());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point((int) Math.floor(GRID + i / 2.0), GRID + i), iter.next());
			logger.debug("" + iter.current());
		}
		assertFalse(iter.hasNext());
	}

	@Test
	public void test_iteration_slope_3()
	{
		LineIterator iter = new LineIterator(new Point(GRID, GRID), new Point((int) (0.5 * GRID), (int) (2.0 * GRID)));
		logger.debug("iterating: from " + iter.getStart() + " to " + iter.getEnd());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point((int) Math.ceil(GRID - i / 2.0), GRID + i), iter.next());
			logger.debug("" + iter.current());
		}
		assertFalse(iter.hasNext());
	}

	@Test
	public void test_iteration_slope_4()
	{
		LineIterator iter = new LineIterator(new Point(GRID, GRID), new Point((int) (0.0 * GRID), (int) (1.5 * GRID)));
		logger.debug("iterating: from " + iter.getStart() + " to " + iter.getEnd());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point(GRID - i, (int) Math.floor(GRID + i / 2.0)), iter.next());
			logger.debug("" + iter.current());
		}
		assertFalse(iter.hasNext());
	}

	@Test
	public void test_iteration_slope_5()
	{
		LineIterator iter = new LineIterator(new Point(GRID, GRID), new Point((int) (0.0 * GRID), (int) (0.5 * GRID)));
		logger.debug("iterating: from " + iter.getStart() + " to " + iter.getEnd());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point(GRID - i, (int) Math.ceil(GRID - i / 2.0)), iter.next());
			logger.debug("" + iter.current());
		}
		assertFalse(iter.hasNext());
	}

	@Test
	public void test_iteration_slope_6()
	{
		LineIterator iter = new LineIterator(new Point(GRID, GRID), new Point((int) (0.5 * GRID), (int) (0.0 * GRID)));
		logger.debug("iterating: from " + iter.getStart() + " to " + iter.getEnd());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point((int) Math.ceil(GRID - i / 2.0), GRID - i), iter.next());
			logger.debug("" + iter.current());
		}
		assertFalse(iter.hasNext());
	}

	@Test
	public void test_iteration_slope_7()
	{
		LineIterator iter = new LineIterator(new Point(GRID, GRID), new Point((int) (1.5 * GRID), (int) (0.0 * GRID)));
		logger.debug("iterating: from " + iter.getStart() + " to " + iter.getEnd());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point((int) Math.floor(GRID + i / 2.0), GRID - i), iter.next());
			logger.debug("" + iter.current());
		}
		assertFalse(iter.hasNext());
	}

	@Test
	public void test_iteration_slope_8()
	{
		LineIterator iter = new LineIterator(new Point(GRID, GRID), new Point((int) (2.0 * GRID), (int) (0.5 * GRID)));
		logger.debug("iterating: from " + iter.getStart() + " to " + iter.getEnd());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point(GRID + i, (int) Math.ceil(GRID - i / 2.0)), iter.next());
			logger.debug("" + iter.current());
		}
		assertFalse(iter.hasNext());
	}

	@Test
	public void test_inversion()
	{
		LineIterator iter = new LineIterator(new Point(GRID, GRID), new Point(2 * GRID, GRID)).invert();
		logger.debug("iterating: from " + iter.getStart() + " to " + iter.getEnd());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point(2 * GRID - i, GRID), iter.next());
		}
		assertFalse(iter.hasNext());
	}

	@Test
	public void test_reset()
	{
		LineIterator iter = new LineIterator(new Point(GRID, GRID), new Point(2 * GRID, GRID));
		logger.debug("iterating: from " + iter.getStart() + " to " + iter.getEnd());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point(GRID + i, GRID), iter.next());
		}
		assertFalse(iter.hasNext());
		iter.reset();
		assertTrue(iter.hasNext());
		for(int i = 0; i <= GRID; i++)
		{
			assertTrue(iter.hasNext());
			assertEquals(new Point(GRID + i, GRID), iter.next());
		}
		assertFalse(iter.hasNext());
	}
}