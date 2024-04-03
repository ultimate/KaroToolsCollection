package ultimate.karopapier.mapgenerator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

import ultimate.karopapier.mapgenerator.MapGeneratorUtil;

public class MapGeneratorUtilTest
{
	@Test
	public void test_trim()
	{
		char[][] untrimmed = MapGeneratorUtil.toArray("XXXXXXX\nXXXXXXX\nXXXXXXX\nXXXOXXX\nXXXXXXX\nXXXXXXX\nXXXXXXX");

		char[][] trimmed11 = MapGeneratorUtil.toArray("XXX\nXOX\nXXX");
		assertArrayEquals(trimmed11, MapGeneratorUtil.trim(untrimmed, 1, 1));
		
		char[][] trimmed22 = MapGeneratorUtil.toArray("XXXXX\nXXXXX\nXXOXX\nXXXXX\nXXXXX");
		assertArrayEquals(trimmed22, MapGeneratorUtil.trim(untrimmed, 2, 2));
		
		char[][] trimmed33 = untrimmed;
		assertArrayEquals(trimmed33, MapGeneratorUtil.trim(untrimmed, 3, 3));
		
		char[][] trimmed44 = untrimmed;
		assertArrayEquals(trimmed44, MapGeneratorUtil.trim(untrimmed, 4, 4));
		
		char[][] trimmed13 = MapGeneratorUtil.toArray("XXX\nXXX\nXXX\nXOX\nXXX\nXXX\nXXX");
		assertArrayEquals(trimmed13, MapGeneratorUtil.trim(untrimmed, 1, 3));
		
		char[][] trimmed31 = MapGeneratorUtil.toArray("XXXXXXX\nXXXOXXX\nXXXXXXX");
		assertArrayEquals(trimmed31, MapGeneratorUtil.trim(untrimmed, 3, 1));
	}
}
