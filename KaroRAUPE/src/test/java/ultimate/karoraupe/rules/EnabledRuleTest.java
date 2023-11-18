package ultimate.karoraupe.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ultimate.karoraupe.rules.Rule.Result;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class EnabledRuleTest extends KaroRAUPETestcase
{	
	private EnabledRule rule = new EnabledRule();

	public static Stream<Arguments> provideConfigAndTestState()
	{
		//@formatter:off
	    return Stream.of(
			// cases expecting NULL
			arguments("always", null, null),
			arguments("immer", null, null),
			arguments("nomessage", null, null),
			arguments("nomsg", null, null),
			arguments("keinbordfunk", null, null),
			arguments("nonotification", null, null),
			arguments("nonote", null, null),
			arguments("nocrashorkick", null, null),
			arguments("keinhinweis", null, null),			
			// cases expecting FALSE
			arguments("never", false, false),
			arguments("nie", false, false),
			arguments("niemals", false, false),
			arguments("invalid", false, false),			
			// cases TEST
			arguments("test", null, false)
	    );
	    //@formatter:on
	}

	@ParameterizedTest
	@MethodSource("provideConfigAndTestState")
	public void test_evaluate(String configValue, Boolean expectedWithTest, Boolean expectedWithoutTest)
	{
		Properties gameConfig = new Properties();
		gameConfig.setProperty("karoraupe.trigger", configValue);

		Result result;
		
		rule.setTest(true);
		result = rule.evaluate(null, null, gameConfig);
		assertEquals(expectedWithTest,result.shallMove());
		
		rule.setTest(false);
		result = rule.evaluate(null, null, gameConfig);
		assertEquals(expectedWithoutTest, result.shallMove());
	}
}
