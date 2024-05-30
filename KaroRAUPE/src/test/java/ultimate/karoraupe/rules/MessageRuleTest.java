package ultimate.karoraupe.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.rules.Rule.Result;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class MessageRuleTest extends KaroRAUPETestcase
{	
	private MessageRule rule = new MessageRule(karoAPI);

	private static Move createMove(long time, String msg)
	{
		Move m = new Move(0, 0, 0, 0, msg);
		m.setT(new Date(time));
		return m;
	}

	public static Stream<Arguments> provideConfigAndTestState()
	{
		//@formatter:off
	    return Stream.of(
			// no notification, with message
			arguments("nonotification", "foo", -1000, null),
			arguments("nonotification", "foo", +1000, false),
			// no notification, with notification
			arguments("nonotification", "-:KIch bin ausgestiegenK:-", -1000, null),
			arguments("nonotification", "-:KIch bin ausgestiegenK:-", +1000, false),
			// no message, with message
			arguments("nomessage", "foo", -1000, null),
			arguments("nomessage", "foo", +1000, false),
			// no message, with notification
			arguments("nomessage", "-:KIch bin ausgestiegenK:-", -1000, null),
			arguments("nomessage", "-:KIch bin ausgestiegenK:-", +1000, null),
			// always, with message
			arguments("always", "foo", -1000, null),
			arguments("always", "foo", +1000, null),
			// always, with notification
			arguments("always", "-:KIch bin ausgestiegenK:-", -1000, null),
			arguments("always", "-:KIch bin ausgestiegenK:-", +1000, null)
	    );
	    //@formatter:on
	}

	@ParameterizedTest
	@MethodSource("provideConfigAndTestState")
	public void test_evaluate(String configValue, String message, int messageTime, Boolean expected)
	{
		int NUMBER_OF_PLAYERS = 4;
		int REF_TIME = 1000000000;

		// create a game with 4 players
		LinkedList<Player> players = new LinkedList<>();
		Player newPlayer;
		for(int p = 0; p < NUMBER_OF_PLAYERS; p++)
		{
			newPlayer = new Player(p);		
			newPlayer.setMoves(new LinkedList<>());
			players.add(newPlayer);			
		}

		Game game = new Game();
		game.setPlayers(players);

		Player player = players.get(0);
		player.setMotion(createMove(REF_TIME, null));

		Properties gameConfig = new Properties();
		gameConfig.setProperty("karoraupe.trigger", configValue);

		players.get(1).setMoves(Arrays.asList(createMove(REF_TIME - 1000, null)));
		players.get(2).setMoves(Arrays.asList(createMove(REF_TIME + 1000, null)));
		players.get(3).setMoves(Arrays.asList(createMove(REF_TIME + messageTime, message)));
		
		Result result = rule.evaluate(game, player, gameConfig);

		assertEquals(expected, result.shallMove());
	}

	@Test
	public void test_isNotification() throws InterruptedException, ExecutionException 
	{
		Game game;
		Player player;
		Move move;

		// test game 136397
		game = karoAPI.getGame(136397, false, true, true).get();
		
		// Ich bin von KaroMAMA rausgeworfen worden
		player = game.getPlayers().get(1);
		assertEquals("DerFlieger", player.getName());
		move = player.getMotion();
		logger.info(move);
		assertTrue(MessageRule.isNotification(move.getMsg()));		

		// Ich werde 25 Züge zurückgesetzt
		player = game.getPlayers().get(0);
		assertEquals("sparrows bruder", player.getName());
		move = player.getMoves().get(60);
		logger.info(move);
		assertTrue(MessageRule.isNotification(move.getMsg()));	
		
		// test game 136396
		game = karoAPI.getGame(136396, false, true, true).get();
				
		// Ich werde 25 Züge zurückgesetzt (new spelling withouth &uuml;
		player = game.getPlayers().get(0);
		assertEquals("Botrix", player.getName());
		move = player.getMoves().get(208);
		logger.info(move);
		assertTrue(MessageRule.isNotification(move.getMsg()));	
		
		// test game 63430
		game = karoAPI.getGame(63430, false, true, true).get();

		// Ich bin ausgestiegen
		player = game.getPlayers().get(1);
		assertEquals("quabla", player.getName());
		move = player.getMotion();
		logger.info(move);
		assertTrue(MessageRule.isNotification(move.getMsg()));		

		// Ich wurde von KaroMAMA rausgeworfen
		player = game.getPlayers().get(game.getPlayers().size()-1);
		assertEquals("Gwendoline", player.getName());
		move = player.getMotion();
		logger.info(move);
		assertTrue(MessageRule.isNotification(move.getMsg()));		

		// Ich wurde 4 Züge zurückgesetzt
		player = game.getPlayers().get(2);
		assertEquals("Akari", player.getName());
		move = player.getMoves().get(26);
		logger.info(move);
		assertTrue(MessageRule.isNotification(move.getMsg()));	

		// :gold:
		player = game.getPlayers().get(0);
		assertEquals("Wobbel", player.getName());
		move = player.getMoves().get(player.getMoves().size()-2);
		logger.info(move);
		assertFalse(MessageRule.isNotification(move.getMsg()));
		
		// Ne, ohne mich!
		player = game.getPlayers().get(12);
		assertEquals("MrMM", player.getName());
		move = player.getMotion();
		logger.info(move);
		assertFalse(MessageRule.isNotification(move.getMsg()));
	}
}
