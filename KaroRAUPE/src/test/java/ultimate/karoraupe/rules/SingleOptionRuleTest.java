package ultimate.karoraupe.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.rules.Rule.Result;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class SingleOptionRuleTest extends KaroRAUPETestcase
{	
	private SingleOptionRule rule = new SingleOptionRule(karoAPI);

	public static Stream<Arguments> providePossibles()
	{
		//@formatter:off
	    return Stream.of(
			arguments(null, null),
			arguments(Arrays.asList(), null),
			arguments(Arrays.asList(new Move()), true),
			arguments(Arrays.asList(new Move(), new Move()), null),
			arguments(Arrays.asList(new Move(), new Move(), new Move()), null),
			arguments(Arrays.asList(new Move(), new Move(), new Move(), new Move()), null),
			arguments(Arrays.asList(new Move(), new Move(), new Move(), new Move(), new Move()), null)
		);
	    //@formatter:on
	}

	@ParameterizedTest
	@MethodSource("providePossibles")
	public void test_evaluate(List<Move> possibles, Boolean expected)
	{
		Player player = new Player();
		player.setPossibles(possibles);

		Properties gameConfig = new Properties();

		Result result;
		
		// config enabled, no message		
		gameConfig.setProperty("karoraupe.singleoption", "true");
		result = rule.evaluate(null, player, gameConfig);		
		assertNotNull(result);
		assertEquals(expected, result.shallMove());		
		if(expected == Boolean.TRUE)
			assertNotNull(result.getMove());

		// config enabled, with message
		gameConfig.setProperty("karoraupe.singleoption", "true");
		String message = "foo";
		gameConfig.setProperty("karoraupe.singleoption.message", message);		
		result = rule.evaluate(null, player, gameConfig);		
		assertNotNull(result);
		assertEquals(expected, result.shallMove());		
		if(expected == Boolean.TRUE)
		{
			assertNotNull(result.getMove());
			assertEquals(message, result.getMove().getMsg());
		}

		// config disabled
		gameConfig.setProperty("karoraupe.singleoption", "false");
		result = rule.evaluate(null, player, gameConfig);		
		assertNotNull(result);
		assertNull(result.shallMove());
	}
}
