package ultimate.karoraupe.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class MessageRuleTest extends KaroRAUPETestcase
{	
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
