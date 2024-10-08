package ultimate.karoraupe.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.rules.Rule.Result;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class AfterCrashRuleTest extends KaroRAUPETestcase
{	
	private AfterCrashRule rule = new AfterCrashRule(karoAPI);

	private static Move createCrash()
	{
		Move m = new Move(randomCoord(), randomCoord(), 0, 0, null);
		m.setCrash(true);
		return m;
	}

	public static Stream<Arguments> provideMoves()
	{
		//@formatter:off
	    return Stream.of(
			arguments(null, null),
			// xv == 0 && yv != 0
			arguments(createCrash(), false),
			arguments(createCrash(), false),
			arguments(createCrash(), false),
			arguments(createCrash(), false),
			arguments(createCrash(), false),
			// xv == 0 && yv != 0
			arguments(new Move(randomCoord(), randomCoord(), 0, randomVecNonZero(), null), null),
			arguments(new Move(randomCoord(), randomCoord(), 0, randomVecNonZero(), null), null),
			arguments(new Move(randomCoord(), randomCoord(), 0, randomVecNonZero(), null), null),
			arguments(new Move(randomCoord(), randomCoord(), 0, randomVecNonZero(), null), null),
			arguments(new Move(randomCoord(), randomCoord(), 0, randomVecNonZero(), null), null),
			// xv != 0 && yv == 0
			arguments(new Move(randomCoord(), randomCoord(), randomVecNonZero(), 0, null), null),
			arguments(new Move(randomCoord(), randomCoord(), randomVecNonZero(), 0, null), null),
			arguments(new Move(randomCoord(), randomCoord(), randomVecNonZero(), 0, null), null),
			arguments(new Move(randomCoord(), randomCoord(), randomVecNonZero(), 0, null), null),
			arguments(new Move(randomCoord(), randomCoord(), randomVecNonZero(), 0, null), null),
			// xv != 0 && yv != 0
			arguments(new Move(randomCoord(), randomCoord(), randomVecNonZero(), randomVecNonZero(), null), null),
			arguments(new Move(randomCoord(), randomCoord(), randomVecNonZero(), randomVecNonZero(), null), null),
			arguments(new Move(randomCoord(), randomCoord(), randomVecNonZero(), randomVecNonZero(), null), null),
			arguments(new Move(randomCoord(), randomCoord(), randomVecNonZero(), randomVecNonZero(), null), null),
			arguments(new Move(randomCoord(), randomCoord(), randomVecNonZero(), randomVecNonZero(), null), null)
	    );
	    //@formatter:on
	}

	@ParameterizedTest
	@MethodSource("provideMoves")
	public void test_evaluate(Move move, Boolean expected)
	{
		Player player = new Player();
		player.setMotion(move);

		Properties gameConfig = new Properties();

		Result result;

		// from zero = false
		gameConfig.setProperty(AfterCrashRule.KEY_FROMZERO, "false");		
		result = rule.evaluate(null, player, gameConfig);
		
		assertNotNull(result);
		assertEquals(expected, result.shallMove());

		// from zero = true
		gameConfig.setProperty(AfterCrashRule.KEY_FROMZERO, "true");		
		result = rule.evaluate(null, player, gameConfig);
		
		assertNotNull(result);
		assertEquals(null, result.shallMove());
	}
}
