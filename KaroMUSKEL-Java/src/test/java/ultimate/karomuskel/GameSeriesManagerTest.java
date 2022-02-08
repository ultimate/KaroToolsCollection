package ultimate.karomuskel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import muskel2.model.series.BalancedGameSeries;
import muskel2.model.series.SimpleGameSeries;

@SuppressWarnings("deprecation")
public class GameSeriesManagerTest
{
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Test
	public void test_loadV2() throws ClassNotFoundException, ClassCastException, IOException
	{
		SimpleGameSeries gs_simple = (SimpleGameSeries) GameSeriesManager.loadV2(new File("target/test-classes/simple.muskel2"));
		assertNotNull(gs_simple);
		assertEquals("${spieler.ersteller}s Spieleserie - Spiel ${i} auf Karte ${karte.id}, ${regeln.x}", gs_simple.title);

		BalancedGameSeries gs_balanced = (BalancedGameSeries) GameSeriesManager.loadV2(new File("target/test-classes/balanced.muskel2"));
		assertNotNull(gs_balanced);
		assertEquals("${spieler.ersteller}s Ausgewogene Spieleserie - Spiel ${i} - ${spieler.namen.x} auf Karte ${karte.id}, ${regeln.x}", gs_balanced.title);
	}
}
