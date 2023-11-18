package ultimate.karoraupe.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.rules.Rule.Result;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class RepeatRuleTest extends KaroRAUPETestcase
{	
	private RepeatRule rule = new RepeatRule();

	public static Stream<Arguments> provideConfigAndPossibles()
	{
		List<Move> moves = new ArrayList<>();		
		moves.add(new Move(0, 0, "Start"));
		moves.add(new Move(0, 1, 0, 1, null));
		moves.add(new Move(0, 3, 0, 2, null));
		Move crash = new Move(0, 0, null);
		crash.setCrash(true);
		moves.add(crash);
		moves.add(new Move(1, 1, 1, 1, null));
		moves.add(new Move(2, 1, 1, 0, null));
		moves.add(new Move(3, 1, 0, -1, null));
		moves.add(new Move(4, 1, 1, 0, null));

		//@formatter:off
	    return Stream.of(
			arguments(moves, 1, true, null),
			arguments(moves, 2, true, null),
			arguments(moves, 3, true, null),
			arguments(moves, 4, true, null),
			arguments(moves, 5, false, "crash"),
			arguments(moves, 6, false, "not in possibles"),
			arguments(moves, 7, true, null),			
			arguments(moves, 8, false, "not enough"),			
			arguments(moves, 0, false, "invalid")
	    );
	    //@formatter:on
	}

	@ParameterizedTest
	@MethodSource("provideConfigAndPossibles")
	public void test_evaluate(List<Move> moves, int repeatX, Boolean expectedResult, String expectedString)
	{
		logger.info("testing with repeatX = " + repeatX);

		Player player = new Player();
		player.setMoves(moves);
		player.setMotion(moves.get(moves.size()-1));

		// construct possibles
		player.setPossibles(new ArrayList<>(9));
		int x, y, xv, yv;
		for(int dx = -1; dx <= 1; dx++)
		{
			for(int dy = -1; dy <= 1; dy++)
			{
				xv = player.getMotion().getXv() + dx;
				yv = player.getMotion().getYv() + dy;
				x = player.getMotion().getX() + xv;
				y = player.getMotion().getY() + yv;
				player.getPossibles().add(new Move(x, y, xv, yv, null));
			}
		}

		Properties gameConfig = new Properties();
		gameConfig.setProperty(RepeatRule.KEY_SPECIAL_REPEAT, "true");
		gameConfig.setProperty(RepeatRule.KEY_SPECIAL_REPEAT_MOVES, "" + repeatX);

		int index = moves.size() - repeatX;
		logger.debug("move at repeatX = " + repeatX + " => " + (index >= 0 && index < moves.size() ? moves.get(index) : null));

		Result result = rule.evaluate(null, player, gameConfig);

		assertEquals(expectedResult, result.shallMove());
		if(expectedResult == true)
		{
			assertNotNull(result.getMove());
			
			// check index
		}

		if(expectedString != null)
			assertTrue(result.getReason().contains(expectedString));
	}

	@Test
	public void test_evaluate_disabled()
	{
		Properties gameConfig = new Properties();

		Result result;

		result = rule.evaluate(null, null, gameConfig);
		assertEquals(null, result.shallMove());

		gameConfig.setProperty(RepeatRule.KEY_SPECIAL_REPEAT, "false");		
		result = rule.evaluate(null, null, gameConfig);
		assertEquals(null, result.shallMove());
	}
}
