package ultimate.karopapier.eval.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.Player;

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
}
