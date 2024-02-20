package ultimate.karoraupe.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoraupe.rules.Rule.Result;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class FinishedRuleTest extends KaroRAUPETestcase
{	
	private FinishedRule rule = new FinishedRule(karoAPI);

	@Test
	public void test_evaluate()
	{
		Game game = new Game();

		Result result;

		game.setFinished(false);
		result = rule.evaluate(game, null, null);
		assertNull(result.shallMove());

		game.setFinished(true);
		result = rule.evaluate(game, null, null);
		assertFalse(result.shallMove());
	}
}
