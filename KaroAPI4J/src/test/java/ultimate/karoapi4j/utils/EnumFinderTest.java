package ultimate.karoapi4j.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.enums.EnumPlayerStatus;
import ultimate.karoapi4j.enums.EnumUserGamesort;
import ultimate.karoapi4j.enums.EnumUserState;
import ultimate.karoapi4j.enums.EnumUserTheme;
import ultimate.karoapi4j.test.KaroAPITestcase;
import ultimate.karoapi4j.utils.web.URLLoader.BackgroundLoader;

public class EnumFinderTest extends KaroAPITestcase
{
	public static Stream<Arguments> provideEnums()
	{
		//@formatter:off
	    return Stream.of(
	        arguments("getUsers", EnumUserTheme.class, "theme"),
	        arguments("getUsers", EnumUserGamesort.class, "gamesort"),
	        arguments("getUsers", EnumUserState.class, "state"),
	        arguments("getGames", EnumGameDirection.class, "startdirection"),
	        arguments("getGames", EnumGameTC.class, 		"crashallowed"),
	        arguments("getGames", EnumPlayerStatus.class, "status")
	    );
	    //@formatter:on
	}

	@ParameterizedTest
	@MethodSource("provideEnums")
	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> void test_findEnums(String apiCall, Class<E> cls, String key)
			throws InterruptedException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Method method = karoAPI.getClass().getMethod(apiCall);
		BackgroundLoader<List<?>> loader = (BackgroundLoader<List<?>>) method.invoke(karoAPI);
		loader.doBlocking();

		Set<String> enumValuesFound = EnumFinder.findEnums(loader.getRawResult(), key);

		logger.info("checking " + cls.getName());
		Enum<E> enumValue;
		for(String name : enumValuesFound)
		{
			enumValue = Enum.valueOf(cls, name);
			logger.debug("string = " + name + " --> " + enumValue);
			assertNotNull(enumValue);
			assertEquals(name, enumValue.name());
		}
		logger.info(cls.getName() + " -> all found values are contained in the enum definition");
	}
}
