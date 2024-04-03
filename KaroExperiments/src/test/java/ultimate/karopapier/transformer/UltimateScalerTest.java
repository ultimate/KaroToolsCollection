package ultimate.karopapier.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ultimate.karopapier.transformer.UltimateScaler.Corner;
import ultimate.karopapier.transformer.UltimateScaler.Zone;

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
		
		map = MapTransformerHelper.toArray("OXOX\nXOXO\nOXOX\nXOXO");
		
		assertEquals(0b10101010, UltimateScaler.getMask(map, 1, 1));
		assertEquals(0b10101010, UltimateScaler.getMask(map, 2, 1));
		assertEquals(0b10101010, UltimateScaler.getMask(map, 2, 2));
		assertEquals(0b10101010, UltimateScaler.getMask(map, 1, 2));
		
		assertEquals(0b11001001, UltimateScaler.getMask(map, 0, 0));
		assertEquals(0b01110010, UltimateScaler.getMask(map, 3, 0));
		assertEquals(0b10011100, UltimateScaler.getMask(map, 3, 3));
		assertEquals(0b00100111, UltimateScaler.getMask(map, 0, 3));
		
		map = MapTransformerHelper.toArray("XXXX\nXOOX\nXOOX\nXXXX");
		
		assertEquals(0b00011100, UltimateScaler.getMask(map, 1, 1));
		assertEquals(0b00000111, UltimateScaler.getMask(map, 2, 1));
		assertEquals(0b11000001, UltimateScaler.getMask(map, 2, 2));
		assertEquals(0b01110000, UltimateScaler.getMask(map, 1, 2));;
		
		assertEquals(0b11110111, UltimateScaler.getMask(map, 0, 0));
		assertEquals(0b11111101, UltimateScaler.getMask(map, 3, 0));
		assertEquals(0b01111111, UltimateScaler.getMask(map, 3, 3));
		assertEquals(0b11011111, UltimateScaler.getMask(map, 0, 3));
	}
}
