package ultimate.karoapi4j.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.enums.EnumPlayerStatus;
import ultimate.karoapi4j.enums.EnumUserGamesort;
import ultimate.karoapi4j.enums.EnumUserState;
import ultimate.karoapi4j.enums.EnumUserTheme;
import ultimate.karoapi4j.test.KaroAPITestcase;
import ultimate.karoapi4j.utils.URLLoader.BackgroundLoader;

public class EnumFinderTest extends KaroAPITestcase
{
	public static Stream<Arguments> provideEnums()
	{
		//@formatter:off
	    return Stream.of(
	        arguments("USERS", EnumUserTheme.class, 	"theme"),
	        arguments("USERS", EnumUserGamesort.class, 	"gamesort"),
	        arguments("USERS", EnumUserState.class, 	"state"),
	        arguments("GAMES", EnumGameDirection.class, "startdirection"),
	        arguments("GAMES", EnumGameTC.class, 		"crashallowed"),
	        arguments("GAMES", EnumPlayerStatus.class, 	"status")
	    );
	    //@formatter:on
	}

	@ParameterizedTest
	@MethodSource("provideEnums")
	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> void test_findEnums(String apiField, Class<E> cls, String key)
			throws InterruptedException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, ExecutionException
	{
		// use reflections to access the protected content of the API to get the RAW result
		Field field = karoAPI.getClass().getDeclaredField(apiField);
		field.setAccessible(true);
		URLLoader urlLoader = (URLLoader) field.get(karoAPI);	
		
		Method loadMethod = karoAPI.getClass().getDeclaredMethod("loadAsync", BackgroundLoader.class, Function.class);
		loadMethod.setAccessible(true);
		
		CompletableFuture<String> cf = (CompletableFuture<String>) loadMethod.invoke(karoAPI, urlLoader.doGet(), KaroAPI.PARSER_RAW);
		String rawResult = cf.get();
		
		// now we can scan the RAW JSON
		Set<String> enumValuesFound = EnumFinder.findEnums(rawResult, key);

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
