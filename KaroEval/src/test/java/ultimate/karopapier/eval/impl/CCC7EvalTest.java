package ultimate.karopapier.eval.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karomuskel.GameSeriesManager;

public class CCC7EvalTest
{
	@Test
	public void testGameToLink() {
		List<Player> players = new ArrayList<>();
		CCC7Eval e = new CCC7Eval() {
			protected PlannedGame getGame(int challenge, int game) {
				Game g = new Game();
				g.setId(challenge * 1000 + game);
				g.setName("Game c=" + challenge + ", g=" + game);
				g.setPlayers(players);
				return new PlannedGame(g, null);
			}
		};
		
		assertEquals("1.1.1", trimToNumber(e.gameToLink(0, 0)));
		assertEquals("1.1.2", trimToNumber(e.gameToLink(0, 1)));
		assertEquals("1.1.3", trimToNumber(e.gameToLink(0, 2)));
		assertEquals("1.1.4", trimToNumber(e.gameToLink(0, 3)));
		assertEquals("1.1.5", trimToNumber(e.gameToLink(0, 4)));
		assertEquals("1.2.1", trimToNumber(e.gameToLink(0, 5)));
		assertEquals("1.2.2", trimToNumber(e.gameToLink(0, 6)));
		assertEquals("1.2.3", trimToNumber(e.gameToLink(0, 7)));
		assertEquals("1.2.4", trimToNumber(e.gameToLink(0, 8)));
		assertEquals("1.2.5", trimToNumber(e.gameToLink(0, 9)));
		assertEquals("1.3.1", trimToNumber(e.gameToLink(0, 10)));

		assertEquals("2.1.1", trimToNumber(e.gameToLink(1, 0)));
		assertEquals("2.1.2", trimToNumber(e.gameToLink(1, 1)));
		assertEquals("2.1.3", trimToNumber(e.gameToLink(1, 2)));
		assertEquals("2.1.4", trimToNumber(e.gameToLink(1, 3)));
		assertEquals("2.1.5", trimToNumber(e.gameToLink(1, 4)));
		assertEquals("2.2.1", trimToNumber(e.gameToLink(1, 5)));
		assertEquals("2.2.2", trimToNumber(e.gameToLink(1, 6)));
		assertEquals("2.2.3", trimToNumber(e.gameToLink(1, 7)));
		assertEquals("2.2.4", trimToNumber(e.gameToLink(1, 8)));
		assertEquals("2.2.5", trimToNumber(e.gameToLink(1, 9)));
		assertEquals("2.3.1", trimToNumber(e.gameToLink(1, 10)));
	}

	String start = "|";
	String end = "}}";
	
	private String trimToNumber(String name) {
		return name.substring(name.lastIndexOf(start) + start.length(), name.indexOf(end));
	}
	
	@Test
	public void testGetMap() throws IOException {
		File loginProperties = new File("../KaroExperiments/src/main/resources/login.properties");
		File gameSeriesFile = new File("../CraZZZy Crash Challenge/CCC7/czzzcc7.json");
		File evalPropertiesFile = new File("../CraZZZy Crash Challenge/CCC7/czzzcc7-eval.properties");
		
		// initialize karoapi
		Properties login = PropertiesUtil.loadProperties(loginProperties);
		KaroAPI api = new KaroAPI(login.getProperty("karoAPI.user"), login.getProperty("karoAPI.password"));
		KaroAPICache cache = new KaroAPICache(api, login);
		cache.refresh().join();		
		
		GameSeries gs = GameSeriesManager.load(gameSeriesFile, cache);
		
		// load eval game series

		Properties evalProperties = PropertiesUtil.loadProperties(evalPropertiesFile);
		CCC7Eval e = new CCC7Eval();
		e.prepare(cache, gs, evalProperties, new File("../CraZZZy Crash Challenge/CCC7"), 0);
				
		assertEquals("Kindergarten", e.getMap(0).getName());
		assertEquals("u⁷", e.getMap(1).getName());
		assertEquals("Bodo BAGGER", e.getMap(2).getName());
		assertEquals("Geradeaus", e.getMap(3).getName());
		assertEquals("u⁷", e.getMap(4).getName());
	}
}
