package ultimate.karoraupe.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Properties;

import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.rules.Rule.Result;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class StartPositionRuleTest extends KaroRAUPETestcase
{	
	private StartPositionRule rule = new StartPositionRule(karoAPI);

	@Test
	public void test_evaluate()
	{
		Properties gameConfig = new Properties();
		
		
		Player player = new Player();
		
		Result result;

		// checks enabled
		gameConfig.setProperty(StartPositionRule.KEY_STARTCHECK, "true");

		// start position NOT selected
		player.setMotion(null);
		result = rule.evaluate(null, player, gameConfig);
		assertFalse(result.shallMove());

		// start position selected
		player.setMotion(new Move(0, 0, null));
		result = rule.evaluate(null, player, gameConfig);
		assertNull(result.shallMove());

		// checks disnabled
		gameConfig.setProperty(StartPositionRule.KEY_STARTCHECK, "false");

		// start position NOT selected
		result = rule.evaluate(null, null, gameConfig);
		assertNull(result.shallMove());
	}
}
