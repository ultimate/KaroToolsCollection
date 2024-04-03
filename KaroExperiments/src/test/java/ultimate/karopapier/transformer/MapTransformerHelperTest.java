package ultimate.karopapier.transformer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

public class MapTransformerHelperTest
{
	@Test
	public void test_trim()
	{
		char[][] untrimmed = MapTransformerHelper.toArray("XXXXXXX\nXXXXXXX\nXXXXXXX\nXXXOXXX\nXXXXXXX\nXXXXXXX\nXXXXXXX");

		char[][] trimmed11 = MapTransformerHelper.toArray("XXX\nXOX\nXXX");
		assertArrayEquals(trimmed11, MapTransformerHelper.trim(untrimmed, 1, 1));
		
		char[][] trimmed22 = MapTransformerHelper.toArray("XXXXX\nXXXXX\nXXOXX\nXXXXX\nXXXXX");
		assertArrayEquals(trimmed22, MapTransformerHelper.trim(untrimmed, 2, 2));
		
		char[][] trimmed33 = untrimmed;
		assertArrayEquals(trimmed33, MapTransformerHelper.trim(untrimmed, 3, 3));
		
		char[][] trimmed44 = untrimmed;
		assertArrayEquals(trimmed44, MapTransformerHelper.trim(untrimmed, 4, 4));
		
		char[][] trimmed13 = MapTransformerHelper.toArray("XXX\nXXX\nXXX\nXOX\nXXX\nXXX\nXXX");
		assertArrayEquals(trimmed13, MapTransformerHelper.trim(untrimmed, 1, 3));
		
		char[][] trimmed31 = MapTransformerHelper.toArray("XXXXXXX\nXXXOXXX\nXXXXXXX");
		assertArrayEquals(trimmed31, MapTransformerHelper.trim(untrimmed, 3, 1));
	}
}
