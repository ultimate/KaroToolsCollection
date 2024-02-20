package ultimate.karoraupe.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.rules.Rule.Result;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class NoPossiblesRuleTest extends KaroRAUPETestcase
{	
	private NoPossiblesRule rule = new NoPossiblesRule(karoAPI);

	public static Stream<Arguments> providePossibles()
	{
		//@formatter:off
	    return Stream.of(
			arguments(null, false),
			arguments(Arrays.asList(), false),
			arguments(Arrays.asList(new Move()), null),
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
		Game game = new Game(123);
		
		Player player = new Player();
		player.setPossibles(possibles);

		Result result = rule.evaluate(game, player, null);
		
		assertNotNull(result);
		assertEquals(expected, result.shallMove());
	}
}
