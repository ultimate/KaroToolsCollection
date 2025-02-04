package ultimate.karopapier.mapgenerator.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ultimate.karopapier.mapgenerator.MapGeneratorUtil;

public class MapTransformerTest
{
	@Test
	public void test_createMatrix_90degSteps()
	{
		double scaleX = 3;
		double scaleY = -4;

		int rotation;		
		double[][] expected, actual;
		
		// rotation = 0
		rotation = 0;
		// @formatter:off
		expected = new double[][] {
			{ scaleX, 0,      0 },
			{ 0,      scaleY, 0 },
			{ 0,      0,      1 }
		};
		// @formatter:on
		actual = MapTransformer.createMatrix(rotation, scaleX, scaleY);
		assertMatrixEquals(expected, actual);
		
		// rotation = 90
		rotation = 90;
		// @formatter:off
		expected = new double[][] {
			{ 0,      -scaleX, 0 },
			{ scaleY, 0,       0 },
			{ 0,      0,       1 }
		};
		// @formatter:on
		actual = MapTransformer.createMatrix(rotation, scaleX, scaleY);
		assertMatrixEquals(expected, actual);
		
		// rotation = 180
		rotation = 180;
		// @formatter:off
		expected = new double[][] {
			{ -scaleX, 0, 		0 },
			{ 0,       -scaleY, 0 },
			{ 0,       0,       1 }
		};
		// @formatter:on

		actual = MapTransformer.createMatrix(rotation, scaleX, scaleY);
		assertMatrixEquals(expected, actual);
		
		// rotation = 270
		rotation = 270;
		// @formatter:off
		expected = new double[][] {
			{ 0,       scaleX, 0 },
			{ -scaleY, 0,      0 },
			{ 0,       0,      1 }
		};
		// @formatter:on

		actual = MapTransformer.createMatrix(rotation, scaleX, scaleY);
		assertMatrixEquals(expected, actual);
	}
	
	@Test
	public void test_createMatrix_arbitrarySteps()
	{
		int rotation;		
		double sin, cos;
		double[][] expected, actual;
		
		// rotation = 45
		rotation = 45;
		sin = Math.sin(rotation * Math.PI / 180.0);
		cos = Math.cos(rotation * Math.PI / 180.0);
		// @formatter:off
		expected = new double[][] {
			{ cos, -sin, 0 },
			{ sin,  cos, 0},
			{ 0,    0,   1 }
		};
		// @formatter:on
	
		actual = MapTransformer.createMatrix(rotation, 1, 1);
		assertMatrixEquals(expected, actual);
		
		// rotation = 75
		rotation = 75;
		sin = Math.sin(rotation * Math.PI / 180.0);
		cos = Math.cos(rotation * Math.PI / 180.0);
		// @formatter:off
		expected = new double[][] {
			{ cos, -sin, 0 },
			{ sin,  cos, 0},
			{ 0,    0,   1 }
		};
		// @formatter:on
	
		actual = MapTransformer.createMatrix(rotation, 1, 1);
		assertMatrixEquals(expected, actual);
		
		// rotation = 120
		rotation = 120;
		sin = Math.sin(rotation * Math.PI / 180.0);
		cos = Math.cos(rotation * Math.PI / 180.0);
		// @formatter:off
		expected = new double[][] {
			{ cos, -sin, 0 },
			{ sin,  cos, 0},
			{ 0,    0,   1 }
		};
		// @formatter:on
	
		actual = MapTransformer.createMatrix(rotation, 1, 1);
		assertMatrixEquals(expected, actual);
	}

	private static void assertMatrixEquals(double[][] expected, double[][] actual)
	{
//		 System.out.println("expected = ");
//		 for(int i1 = 0; i1 < expected.length; i1++)
//		 {
//		 for(int i2 = 0; i2 < expected[i1].length; i2++)
//		 {
//		 System.out.print(expected[i1][i2]);
//		 System.out.print("\t");
//		 }
//		 System.out.println();
//		 }
//		 System.out.println("actual = ");
//		 for(int i1 = 0; i1 < actual.length; i1++)
//		 {
//		 for(int i2 = 0; i2 < actual[i1].length; i2++)
//		 {
//		 System.out.print(actual[i1][i2]);
//		 System.out.print("\t");
//		 }
//		 System.out.println();
//		 }
		
		assertEquals(expected.length, actual.length, "rows mismatch");
		for(int i1 = 0; i1 < expected.length; i1++)
		{
			assertEquals(expected[i1].length, actual[i1].length, "cols mismatch on row " + i1);
			for(int i2 = 0; i2 < expected[i1].length; i2++)
			{
				assertEquals(expected[i1][i2], actual[i1][i2], 0.001, "cell mismatch at " + i1 + "," + i2);
			}
		}
	}
	
	@Test
	public void test_transform_offsetCorrection()
	{
		String code = "XXX\nXOX\nXXX";
		char[][] original = MapGeneratorUtil.toArray(code);
		String expected = "XXXXXX\nXXXXXX\nXXOOXX\nXXOOXX\nXXXXXX\nXXXXXX";
		
		char[][] transformed;
		String actual;
		Scaler scaler = new NearestNeighborScaler();
		
		int[] rotations = new int[] {0, 90, 180, 270, 360 };
		int[] scales = new int[] {2, -2};
		
		for(int ri = 0; ri < rotations.length; ri++)
		{
			for(int sxi = 0; sxi < scales.length; sxi++)
			{
				for(int syi = 0; syi < scales.length; syi++)
				{
					System.out.println("rot=" + rotations[ri] + ", sx=" + scales[sxi] + ", sy=" + scales[syi]);
					transformed = MapTransformer.transform(original, MapTransformer.createMatrix(rotations[ri], scales[sxi], scales[syi]), scaler);
					actual = MapGeneratorUtil.toString(transformed);
					assertEquals(expected, actual, "rot=" + rotations[ri] + ", sx=" + scales[sxi] + ", sy=" + scales[syi]);
				}
			}
		}
	}
}
