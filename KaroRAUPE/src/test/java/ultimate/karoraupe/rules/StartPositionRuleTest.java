package ultimate.karoraupe.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.rules.Rule.Result;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class StartPositionRuleTest extends KaroRAUPETestcase
{	
	private StartPositionRule rule = new StartPositionRule();

	@Test
	public void test_evaluate()
	{
		Player player = new Player();
		
		Result result;

		// start position NOT selected
		player.setMotion(null);
		result = rule.evaluate(null, player, null);
		assertFalse(result.shallMove());

		// start position selected
		player.setMotion(new Move(0, 0, null));
		result = rule.evaluate(null, player, null);
		assertNull(result.shallMove());
	}
}
