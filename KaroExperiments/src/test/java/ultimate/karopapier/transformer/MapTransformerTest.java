package ultimate.karopapier.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ultimate.karopapier.transformer.MapTransformer.Corner;
import ultimate.karopapier.transformer.MapTransformer.Zone;

public class MapTransformerTest
{
	@Test
	public void test_getZone()
	{
		assertEquals(Zone.north, MapTransformer.getZone(0.5, 0.0));
		assertEquals(Zone.east,  MapTransformer.getZone(1.0, 0.5));
		assertEquals(Zone.south, MapTransformer.getZone(0.5, 1.0));
		assertEquals(Zone.west,  MapTransformer.getZone(0.0, 0.5));
	}
	
	@Test
	public void test_getCorner()
	{
		assertEquals(Corner.northeast, MapTransformer.getCorner(1.0, 0.0));
		assertEquals(Corner.southeast, MapTransformer.getCorner(1.0, 1.0));
		assertEquals(Corner.southwest, MapTransformer.getCorner(0.0, 1.0));
		assertEquals(Corner.northwest, MapTransformer.getCorner(0.0, 0.0));
		assertEquals(Corner.center,    MapTransformer.getCorner(0.5, 0.5));
	}
	
	@Test
	public void test_getMask()
	{
		char[][] map;
		
		map = MapTransformer.toArray("OXOX\nXOXO\nOXOX\nXOXO");
		
		assertEquals(0b10101010, MapTransformer.getMask(map, 1, 1));
		assertEquals(0b10101010, MapTransformer.getMask(map, 2, 1));
		assertEquals(0b10101010, MapTransformer.getMask(map, 2, 2));
		assertEquals(0b10101010, MapTransformer.getMask(map, 1, 2));
		
		assertEquals(0b00001000, MapTransformer.getMask(map, 0, 0));
		assertEquals(0b11111010, MapTransformer.getMask(map, 3, 0));
		assertEquals(0b10000000, MapTransformer.getMask(map, 3, 3));
		assertEquals(0b10101111, MapTransformer.getMask(map, 0, 3));
		
		map = MapTransformer.toArray("XXXX\nXOOX\nXOOX\nXXXX");
		
		assertEquals(0b00011100, MapTransformer.getMask(map, 1, 1));
		assertEquals(0b00000111, MapTransformer.getMask(map, 2, 1));
		assertEquals(0b11000001, MapTransformer.getMask(map, 2, 2));
		assertEquals(0b01110000, MapTransformer.getMask(map, 1, 2));;
		
		assertEquals(0b11110111, MapTransformer.getMask(map, 0, 0));
		assertEquals(0b11111101, MapTransformer.getMask(map, 3, 0));
		assertEquals(0b01111111, MapTransformer.getMask(map, 3, 3));
		assertEquals(0b11011111, MapTransformer.getMask(map, 0, 3));
	}
}
