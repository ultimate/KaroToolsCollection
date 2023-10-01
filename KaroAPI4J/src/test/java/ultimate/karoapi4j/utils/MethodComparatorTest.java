package ultimate.karoapi4j.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.model.official.Game;

public class MethodComparatorTest
{
	@Test
	public void test_compare()
	{
		MethodComparator<Game> asc = new MethodComparator<>("getId", MethodComparator.ASCENDING);
		MethodComparator<Game> desc = new MethodComparator<>("getId", MethodComparator.DESCENDING);
		
		Game g1 = new Game(1);
		Game g2 = new Game(2);
		Game gn = new Game();
		
		// equal
		
		assertEquals(0, asc.compare(g1, g1));
		assertEquals(0, asc.compare(g2, g2));
		assertEquals(0, asc.compare(gn, gn));
		
		assertEquals(0, desc.compare(g1, g1));
		assertEquals(0, desc.compare(g2, g2));
		assertEquals(0, desc.compare(gn, gn));
		
		// ascending
		
		assertEquals(-1, asc.compare(gn, g1));
		assertEquals(-1, asc.compare(g1, g2));
		assertEquals(-1, asc.compare(gn, g2));
		
		assertEquals(+1, desc.compare(gn, g1));
		assertEquals(+1, desc.compare(g1, g2));
		assertEquals(+1, desc.compare(gn, g2));
		
		// descending
		
		assertEquals(+1, asc.compare(g1, gn));
		assertEquals(+1, asc.compare(g2, g1));
		assertEquals(+1, asc.compare(g2, gn));

		assertEquals(-1, desc.compare(g1, gn));
		assertEquals(-1, desc.compare(g2, g1));
		assertEquals(-1, desc.compare(g2, gn));
	}
}
