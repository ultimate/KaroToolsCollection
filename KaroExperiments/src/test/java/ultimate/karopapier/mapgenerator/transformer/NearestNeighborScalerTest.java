package ultimate.karopapier.mapgenerator.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ultimate.karopapier.mapgenerator.MapGeneratorUtil;

public class NearestNeighborScalerTest
{
	@Test
	public void test_getScaledValue()
	{
		char[][] original, transformed;
		double[][] matrix;
		String code;
		
		// test check pattern
		original = MapGeneratorUtil.toArray("OXOX\nXOXO\nOXOX\nXOXO");
		matrix = MapTransformer.createMatrix(0, 2, 2);
		transformed = MapTransformer.transform(original, matrix, new NearestNeighborScaler());
		code = MapGeneratorUtil.toString(transformed);
		
		assertEquals("OOXXOOXX\nOOXXOOXX\nXXOOXXOO\nXXOOXXOO\nOOXXOOXX\nOOXXOOXX\nXXOOXXOO\nXXOOXXOO", code);
	}
}
