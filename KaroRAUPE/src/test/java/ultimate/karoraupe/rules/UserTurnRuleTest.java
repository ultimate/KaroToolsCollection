package ultimate.karoraupe.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoraupe.rules.Rule.Result;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class UserTurnRuleTest extends KaroRAUPETestcase
{	
	private UserTurnRule rule = new UserTurnRule(karoAPI);

	@Test
	public void test_evaluate()
	{
		Game game = new Game();

		User user1 = new User(1);
		Player player1 = new Player(user1.getId());

		User user2 = new User(2);
		Player player2 = new Player(user2.getId());

		Result result;

		// next = user1, raupe-player = user1
		game.setNext(user1);
		result = rule.evaluate(game, player1, null);
		assertNull(result.shallMove());

		// next = user1, raupe-player = user2
		game.setNext(user1);
		result = rule.evaluate(game, player2, null);
		assertFalse(result.shallMove());

		// next = user2, raupe-player = user1
		game.setNext(user2);
		result = rule.evaluate(game, player1, null);
		assertFalse(result.shallMove());

		// next = user2, raupe-player = user2
		game.setNext(user2);
		result = rule.evaluate(game, player2, null);
		assertNull(result.shallMove());
	}
}
