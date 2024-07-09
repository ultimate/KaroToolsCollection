package ultimate.karoapi4j.model.official;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.test.KaroAPITestcase;

public class PlannedGameTest extends KaroAPITestcase
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger logger = LogManager.getLogger(getClass());

	@Test
	public void test_createFromGame()
	{
		Game g = karoAPICache.getGame(136424);
		
		PlannedGame pg = new PlannedGame(g, karoAPICache);
		
		assertEquals(g.getName(), pg.getName());
		assertEquals(g.getMap(), pg.getMap());
		assertEquals(g.getPlayers().size(), pg.getPlayers().size());
		assertNotNull(pg.getOptions());
		assertEquals(g.getZzz(), pg.getOptions().getZzz());
		assertEquals(g.isCps(), pg.getOptions().isCps());
		assertEquals(g.getCrashallowed(), pg.getOptions().getCrashallowed());
		assertEquals(g.getStartdirection(), pg.getOptions().getStartdirection());
		assertEquals(g, pg.getGame());
		assertTrue(pg.isCreated());
		assertEquals(g.getTags(), pg.getTags());
	}
}
