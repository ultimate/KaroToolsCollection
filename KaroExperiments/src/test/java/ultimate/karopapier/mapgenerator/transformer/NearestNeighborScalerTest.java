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
		MapTransformer transformer;
		String code;
		
		// test check pattern
		original = MapGeneratorUtil.toArray("OXOX\nXOXO\nOXOX\nXOXO");
		matrix = MapTransformer.createMatrix(2, 2, 0, 4, 4);
		transformer = new MapTransformer(matrix, new NearestNeighborScaler());
		transformed = transformer.transform(original);
		code = MapGeneratorUtil.toString(transformed);
		
		assertEquals("OOXXOOXX\nOOXXOOXX\nXXOOXXOO\nXXOOXXOO\nOOXXOOXX\nOOXXOOXX\nXXOOXXOO\nXXOOXXOO", code);
	}
}
