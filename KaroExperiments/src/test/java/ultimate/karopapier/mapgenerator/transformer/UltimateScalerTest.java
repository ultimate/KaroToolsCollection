package ultimate.karopapier.mapgenerator.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ultimate.karopapier.mapgenerator.MapGeneratorUtil;
import ultimate.karopapier.mapgenerator.transformer.UltimateScaler.Corner;
import ultimate.karopapier.mapgenerator.transformer.UltimateScaler.Zone;

public class UltimateScalerTest
{
	@Test
	public void test_getZone()
	{
		assertEquals(Zone.north, UltimateScaler.getZone(0.5, 0.0));
		assertEquals(Zone.east,  UltimateScaler.getZone(1.0, 0.5));
		assertEquals(Zone.south, UltimateScaler.getZone(0.5, 1.0));
		assertEquals(Zone.west,  UltimateScaler.getZone(0.0, 0.5));
	}
	
	@Test
	public void test_getCorner()
	{
		assertEquals(Corner.northeast, UltimateScaler.getCorner(1.0, 0.0));
		assertEquals(Corner.southeast, UltimateScaler.getCorner(1.0, 1.0));
		assertEquals(Corner.southwest, UltimateScaler.getCorner(0.0, 1.0));
		assertEquals(Corner.northwest, UltimateScaler.getCorner(0.0, 0.0));
		assertEquals(Corner.center,    UltimateScaler.getCorner(0.5, 0.5));
	}
	
	@Test
	public void test_getMask()
	{
		char[][] map;
		
		map = MapGeneratorUtil.toArray("OXOX\nXOXO\nOXOX\nXOXO");
		
		assertEquals(0b10101010, UltimateScaler.getMask(map, 1, 1));
		assertEquals(0b10101010, UltimateScaler.getMask(map, 2, 1));
		assertEquals(0b10101010, UltimateScaler.getMask(map, 2, 2));
		assertEquals(0b10101010, UltimateScaler.getMask(map, 1, 2));
		
		assertEquals(0b11001001, UltimateScaler.getMask(map, 0, 0));
		assertEquals(0b01110010, UltimateScaler.getMask(map, 3, 0));
		assertEquals(0b10011100, UltimateScaler.getMask(map, 3, 3));
		assertEquals(0b00100111, UltimateScaler.getMask(map, 0, 3));
		
		map = MapGeneratorUtil.toArray("XXXXXXXX\nXOOX\nXOOX\nXXXXXXXX");
		
		assertEquals(0b00011100, UltimateScaler.getMask(map, 1, 1));
		assertEquals(0b00000111, UltimateScaler.getMask(map, 2, 1));
		assertEquals(0b11000001, UltimateScaler.getMask(map, 2, 2));
		assertEquals(0b01110000, UltimateScaler.getMask(map, 1, 2));;
		
		assertEquals(0b11110111, UltimateScaler.getMask(map, 0, 0));
		assertEquals(0b11111101, UltimateScaler.getMask(map, 3, 0));
		assertEquals(0b01111111, UltimateScaler.getMask(map, 3, 3));
		assertEquals(0b11011111, UltimateScaler.getMask(map, 0, 3));
	}
	
	@Test
	public void test_getScaledValue()
	{
		char[][] original, transformed;
		double[][] matrix;
		String code;
		
		// test check pattern
		original = MapGeneratorUtil.toArray("OXOX\nXOXO\nOXOX\nXOXO");
		matrix = MapTransformer.createMatrix(0, 2, 2);
		transformed = MapTransformer.transform(original, matrix, new UltimateScaler());
		code = MapGeneratorUtil.toString(transformed);
		
		assertEquals("OOXXOOXX\nOOXOOOXX\nXXOXOXOO\nXXXOXOOO\nOXOXOXXX\nOOXOXOXX\nXXOXXXOO\nXXOOXXOO", code);
		
		// test non-street pattern - scale by 2
		original = MapGeneratorUtil.toArray("YXYX\nXYXY\nYXYX\nXYXY");
		matrix = MapTransformer.createMatrix(0, 2, 2);
		transformed = MapTransformer.transform(original, matrix, new UltimateScaler());
		code = MapGeneratorUtil.toString(transformed);
		
		assertEquals("YYXXYYXX\nYYXXYYXX\nXXYYXXYY\nXXYYXXYY\nYYXXYYXX\nYYXXYYXX\nXXYYXXYY\nXXYYXXYY", code);
		
		// test non-street pattern - scale by 4
		original = MapGeneratorUtil.toArray("YXYX\nXYXY\nYXYX\nXYXY");
		matrix = MapTransformer.createMatrix(0, 4, 4);
		transformed = MapTransformer.transform(original, matrix, new UltimateScaler());
		code = MapGeneratorUtil.toString(transformed);
		
		assertEquals("YYYYXXXXYYYYXXXX\nYYYYXXXXYYYYXXXX\nYYYYXXXXYYYYXXXX\nYYYYXXXXYYYYXXXX\nXXXXYYYYXXXXYYYY\nXXXXYYYYXXXXYYYY\nXXXXYYYYXXXXYYYY\nXXXXYYYYXXXXYYYY\nYYYYXXXXYYYYXXXX\nYYYYXXXXYYYYXXXX\nYYYYXXXXYYYYXXXX\nYYYYXXXXYYYYXXXX\nXXXXYYYYXXXXYYYY\nXXXXYYYYXXXXYYYY\nXXXXYYYYXXXXYYYY\nXXXXYYYYXXXXYYYY", code);
	}
}
