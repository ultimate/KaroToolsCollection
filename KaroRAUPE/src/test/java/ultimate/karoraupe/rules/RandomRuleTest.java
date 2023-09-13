package ultimate.karoraupe.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.rules.Rule.Result;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class RandomRuleTest extends KaroRAUPETestcase
{	
	private RandomRule rule = new RandomRule();

	public static Stream<Arguments> provideConfigAndPossibles()
	{
		List<Move> possibles = new ArrayList<>();
		// assuming the current vector is 2/-1
		possibles.add(new Move(0, 0, 1, -2, "Speed=2.23"));
		possibles.add(new Move(0, 0, 1, -1, "Speed=1.41"));
		possibles.add(new Move(0, 0, 1, 0, "Speed=1.00"));
		possibles.add(new Move(0, 0, 2, -2, "Speed=2.82"));
		possibles.add(new Move(0, 0, 2, -1, "Speed=2.23"));
		possibles.add(new Move(0, 0, 2, 0, "Speed=2.00"));
		possibles.add(new Move(0, 0, 3, -2, "Speed=3.61"));
		possibles.add(new Move(0, 0, 3, -1, "Speed=3.16"));
		possibles.add(new Move(0, 0, 3, 0, "Speed=3.00"));

		//@formatter:off
	    return Stream.of(
			arguments(0.0,  possibles, new double[]{0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00}, false),
			arguments(0.5,  possibles, new double[]{0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00}, false),
			arguments(1.0,  possibles, new double[]{0.00, 0.00, 1.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00}, true),
			arguments(1.5,  possibles, new double[]{0.00, 0.50, 0.50, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00}, true),
			arguments(2.0,  possibles, new double[]{0.00, 0.33, 0.33, 0.00, 0.00, 0.33, 0.00, 0.00, 0.00}, true),
			arguments(2.5,  possibles, new double[]{0.20, 0.20, 0.20, 0.00, 0.20, 0.20, 0.00, 0.00, 0.00}, true),
			arguments(3.0,  possibles, new double[]{0.14, 0.14, 0.14, 0.14, 0.14, 0.14, 0.00, 0.00, 0.14}, true),
			arguments(3.5,  possibles, new double[]{0.13, 0.13, 0.13, 0.13, 0.13, 0.13, 0.00, 0.13, 0.13}, true),
			arguments(4.0,  possibles, new double[]{0.11, 0.11, 0.11, 0.11, 0.11, 0.11, 0.11, 0.11, 0.11}, true),
			arguments(null, possibles, new double[]{0.11, 0.11, 0.11, 0.11, 0.11, 0.11, 0.11, 0.11, 0.11}, true)
	    );
	    //@formatter:on
	}

	@ParameterizedTest
	@MethodSource("provideConfigAndPossibles")
	public void test_evaluate(Double maxSpeed, List<Move> possibles, double[] expectedDistribution, boolean expectedResult)
	{
		logger.debug("maxSpeed = " + maxSpeed);
		assertEquals(possibles.size(), expectedDistribution.length);

		Properties gameConfig = new Properties();
		gameConfig.setProperty(RandomRule.KEY_SPECIAL_RANDOM, "true");
		if(maxSpeed != null)
			gameConfig.setProperty(RandomRule.KEY_SPECIAL_RANDOM_MAXSPEED, "" + maxSpeed);

		Player player = new Player();
		player.setPossibles(possibles);

		int[] selections = new int[possibles.size()];

		int NUMBER_OF_TRIES = 1000;

		Result result;
		int moveIndex;
		for(int i = 0; i < NUMBER_OF_TRIES; i++)
		{
			result = rule.evaluate(null, player, gameConfig);
			assertEquals(expectedResult, result.shallMove());
			
			if(result.getMove() != null)
			{
				moveIndex = possibles.indexOf(result.getMove());
				selections[moveIndex]++;
			}
		}

		double actual;
		for(int p = 0; p < possibles.size(); p++)
		{
			actual = selections[p] / (double) NUMBER_OF_TRIES;
			logger.debug(possibles.get(p) + " expectedOccurence = " + expectedDistribution[p] + ", actual=" + actual);
			assertEquals(expectedDistribution[p], actual, 0.05);
		}
	}

	@Test
	public void test_evaluate_disabled()
	{
		Properties gameConfig = new Properties();

		Result result;

		result = rule.evaluate(null, null, gameConfig);
		assertEquals(null, result.shallMove());

		gameConfig.setProperty(RandomRule.KEY_SPECIAL_RANDOM, "false");		
		result = rule.evaluate(null, null, gameConfig);
		assertEquals(null, result.shallMove());
	}
}
