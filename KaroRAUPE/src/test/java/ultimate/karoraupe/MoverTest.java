package ultimate.karoraupe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Options;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoraupe.rules.EnabledRule;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class MoverTest extends KaroRAUPETestcase
{
	private static Stream<Arguments> configExpectations()
	{
		// @formatter:off
		return Stream.of(
					Arguments.of("KaroRAUPE.trigger=always\nKaroRAUPE.timeout=123", 				"always",															"123"),
					Arguments.of("foo\nKaroRAUPE.trigger= immer\nKaroRAUPE.timeout= 456", 			"immer",															"456"),
					Arguments.of("KaroRAUPE.trigger =nomessage\nfoo\nKaroRAUPE.timeout =789", 		"nomessage",														"789"),
					Arguments.of("KaroRAUPE.trigger = nomsg\r\nKaroRAUPE.timeout = 234\r\nfoo", 	"nomsg",															"234"),
					Arguments.of("KaroRAUPE.trigger = keinbordfunk\nKaroRAUPE.timeout = 123", 		"keinbordfunk",														"123"),
					Arguments.of("KaroRAUPE.trigger = keinbordfunk\nKaroRAUPE.timeout = abc", 		"invalid",															Mover.getDefaultConfig().getProperty(Mover.KEY_TIMEOUT)),
					Arguments.of("KaroRAUPE.trigger = never\nKaroRAUPE.timeout = 123", 				"never",															"123"),
					Arguments.of("KaroRAUPE.trigger = never\nKaroRAUPE.timeout = 1m", 				"invalid",															Mover.getDefaultConfig().getProperty(Mover.KEY_TIMEOUT)),
					Arguments.of("\r\nKaroRAUPE.trigger = nie\r\n", 								"nie",																Mover.getDefaultConfig().getProperty(Mover.KEY_TIMEOUT)),
					Arguments.of("KaroRAUPE.trigger = niemals", 									"niemals",															Mover.getDefaultConfig().getProperty(Mover.KEY_TIMEOUT)),
					Arguments.of("KaroRAUPE.trigger = foo", 										"invalid",															Mover.getDefaultConfig().getProperty(Mover.KEY_TIMEOUT)),
					Arguments.of("KaroRAUPE.timeout = 123", 										Mover.getDefaultConfig().getProperty(Mover.KEY_TRIGGER),			"123"),
					Arguments.of("bla bla bla", 													Mover.getDefaultConfig().getProperty(Mover.KEY_TRIGGER), 			Mover.getDefaultConfig().getProperty(Mover.KEY_TIMEOUT))
				);
		// @formatter:on
	}

	@ParameterizedTest
	@MethodSource("configExpectations")
	public void test_getGameConfig(String notes, String trigger, String timeout) throws InterruptedException, ExecutionException
	{
		Mover mover = new Mover(karoAPI, null, true);

		Properties gameConfig = mover.getGameConfig(123456, notes);

		assertNotNull(gameConfig);
		assertEquals(trigger, gameConfig.getProperty(Mover.KEY_TRIGGER));
		assertEquals(timeout, gameConfig.getProperty(Mover.KEY_TIMEOUT));
	}

	@Test
	public void test_processGame() throws InterruptedException, ExecutionException 
	{
		int timeout = 5; // seconds
		int sleep = 500;
		
		User user = karoAPI.check().get();

		PlannedGame plannedGame = new PlannedGame();
		plannedGame.setMap(new Map(10012));
		plannedGame.getPlayers().add(user);
		plannedGame.setName("KaroRAUPE-Test-Game");
		plannedGame.setOptions(new Options(2, true, EnumGameDirection.free, EnumGameTC.free));

		Game game = karoAPI.createGame(plannedGame).get();
		assertNotNull(game);
		assertNotNull(game.getId());
		assertEquals(plannedGame.getName(), game.getName());

		logger.debug("game created: id=" + game.getId() + ", name=" + game.getName());
		int gameId = game.getId();
		int x, y;
		
		Thread.sleep(sleep);

		// load full game -> check players and moves
		game = karoAPI.getGame(gameId, false, true, true).get();
		assertNotNull(game);
		assertEquals(12, game.getPlayers().get(0).getPossibles().size()); // start positions
		
		Thread.sleep(sleep);

		// select start position
		x = 2;
		y = 2;
		assertTrue(karoAPI.selectStartPosition(game.getId(), new Move(x, y, null)).get());

		Thread.sleep(sleep);

		// update -> check players and moves again
		game = karoAPI.getGame(gameId, false, true, true).get();
		assertEquals(8, game.getPlayers().get(0).getPossibles().size());
		assertEquals(x, game.getPlayers().get(0).getMoves().get(game.getPlayers().get(0).getMoves().size() - 1).getX());
		assertEquals(y, game.getPlayers().get(0).getMoves().get(game.getPlayers().get(0).getMoves().size() - 1).getY());

		Thread.sleep(sleep);

		// make one first move (we need that to find the next one)
		x = 3;
		y = 3;
		assertTrue(karoAPI.move(game.getId(), new Move(x, y, 1, 1, null)).get());

		Thread.sleep(sleep);

		// update -> check position
		game = karoAPI.getGame(gameId, false, true, true).get();
		assertEquals(8, game.getPlayers().get(0).getPossibles().size());
		assertEquals(x, game.getPlayers().get(0).getMoves().get(game.getPlayers().get(0).getMoves().size() - 1).getX());
		assertEquals(y, game.getPlayers().get(0).getMoves().get(game.getPlayers().get(0).getMoves().size() - 1).getY());

		// now plan some moves
		List<Move> plannedMoves = new ArrayList<>();
		plannedMoves.add(new Move(3, 3, 1, 1, null)); // this is the first move, we need this to find the next one
		plannedMoves.add(new Move(4, 4, 1, 1, null));
		plannedMoves.add(new Move(5, 5, 1, 1, null));
		karoAPI.addPlannedMoves(gameId, plannedMoves).get();
		
		// and set the config in the notes
		karoAPI.addNote(gameId, "KaroRAUPE.trigger=test\nKaroRAUPE.timeout=" + timeout);

		// now move the planned moves with the Mover
		Mover mover = new Mover(karoAPI, properties, false);
		((EnabledRule) mover.getRules().get(0)).setTest(true);
		
		assertFalse(mover.processGame(user, game));
		x = 3;
		y = 3;

		// update -> check position
		game = karoAPI.getGame(gameId, false, true, true).get();
		assertEquals(8, game.getPlayers().get(0).getPossibles().size());
		assertEquals(x, game.getPlayers().get(0).getMoves().get(game.getPlayers().get(0).getMoves().size() - 1).getX());
		assertEquals(y, game.getPlayers().get(0).getMoves().get(game.getPlayers().get(0).getMoves().size() - 1).getY());

		Thread.sleep(timeout * 1000); // wait for timeout
		
		// move 1st planned move
		assertTrue(mover.processGame(user, game));
		x = 4;
		y = 4;

		// update -> check position
		game = karoAPI.getGame(gameId, false, true, true).get();
		assertEquals(8, game.getPlayers().get(0).getPossibles().size());
		assertEquals(x, game.getPlayers().get(0).getMoves().get(game.getPlayers().get(0).getMoves().size() - 1).getX());
		assertEquals(y, game.getPlayers().get(0).getMoves().get(game.getPlayers().get(0).getMoves().size() - 1).getY());

		Thread.sleep(timeout * 1000); // wait for timeout
		
		// move 2nd planned move (across finish line, this will move the player to 0/0
		assertTrue(mover.processGame(user, game));
		x = 0;
		y = 0;

		// update -> check position
		game = karoAPI.getGame(gameId, false, true, true).get();
		assertNull(game.getPlayers().get(0).getPossibles());
		assertEquals(x, game.getPlayers().get(0).getMoves().get(game.getPlayers().get(0).getMoves().size() - 1).getX());
		assertEquals(y, game.getPlayers().get(0).getMoves().get(game.getPlayers().get(0).getMoves().size() - 1).getY());
		assertTrue(game.isFinished());
	}
}
