package ultimate.karopapier.mapgenerator.transformer;

import java.awt.Point;
import java.awt.geom.Point2D;

import ultimate.karopapier.mapgenerator.MapGeneratorUtil;

public class UltimateScaler extends Scaler
{
	///////////////////////////
	// static
	///////////////////////////

	/**
	 * Get the mask for a karo representing which neighbor karos are of same type or not (street vs.
	 * non street).
	 * The mask is a 8-bit bitmask where the bits from left to right represent the following
	 * neighbors:
	 * - x-1, y-1
	 * - x+0, y-1
	 * - x+1, y-1
	 * - x+1, y+0
	 * - x+1, y+1
	 * - x+0, y+1
	 * - x-1, y+1
	 * - x-1, y+0
	 */
	public static int getMask(char[][] map, int x, int y)
	{
		boolean centerIsStreet = MapGeneratorUtil.isStreet(getRawValue(map, x, y));
		int mask = 0;
		mask += (MapGeneratorUtil.isStreet(getRawValue(map, x - 1, y - 1)) == centerIsStreet ? 128 : 0);
		mask += (MapGeneratorUtil.isStreet(getRawValue(map, x + 0, y - 1)) == centerIsStreet ? 64 : 0);
		mask += (MapGeneratorUtil.isStreet(getRawValue(map, x + 1, y - 1)) == centerIsStreet ? 32 : 0);
		mask += (MapGeneratorUtil.isStreet(getRawValue(map, x + 1, y + 0)) == centerIsStreet ? 16 : 0);
		mask += (MapGeneratorUtil.isStreet(getRawValue(map, x + 1, y + 1)) == centerIsStreet ? 8 : 0);
		mask += (MapGeneratorUtil.isStreet(getRawValue(map, x + 0, y + 1)) == centerIsStreet ? 4 : 0);
		mask += (MapGeneratorUtil.isStreet(getRawValue(map, x - 1, y + 1)) == centerIsStreet ? 2 : 0);
		mask += (MapGeneratorUtil.isStreet(getRawValue(map, x - 1, y + 0)) == centerIsStreet ? 1 : 0);
		return mask;
	}

	/**
	 * internal enum used to represent corners within a karo
	 */
	public enum Corner
	{
		center, northeast, southeast, southwest, northwest
	}

	/**
	 * Get the corner an intermediate coordinate is in.
	 * Possible corners are NORTHEAST, SOUTHEAST, SOUTHWEST, NORTHWEST, and CENTER
	 */
	public static Corner getCorner(double xd, double yd)
	{
		if(xd + yd >= 1.5)
			return Corner.southeast;
		if(xd + yd < 0.5) // use < instead of <= to allow better symmetry
			return Corner.northwest;
		if(xd - yd >= 0.5)
			return Corner.northeast;
		if(xd - yd < -0.5) // use < instead of <= to allow better symmetry
			return Corner.southwest;
		return Corner.center;
	}

	/**
	 * internal enum used to represent zones within a karo
	 */
	public enum Zone
	{
		north, east, south, west
	}

	/**
	 * Get the zone an intermediate coordinate is in.
	 * Possible zones are NORTH, EAST, SOUTH, and WEST.
	 */
	public static Zone getZone(double xd, double yd)
	{
		if(xd + yd > 1)
			if(xd - yd > 0)
				return Zone.east;
			else
				return Zone.south;
		else if(xd - yd > 0)
			return Zone.north;
		else
			return Zone.west;
	}

	/**
	 * Get the value of the neighbor karo based on the zone.
	 */
	public static char getNeighborValue(char[][] map, int x, int y, Zone zone)
	{
		if(zone == Zone.north)
			return getRawValue(map, x + 0, y - 1);
		if(zone == Zone.east)
			return getRawValue(map, x + 1, y + 0);
		if(zone == Zone.south)
			return getRawValue(map, x + 0, y + 1);
		if(zone == Zone.west)
			return getRawValue(map, x - 1, y + 0);
		return getRawValue(map, x, y);
	}

	/**
	 * Get the check value that should be used based on the zone and the mod counter.
	 * Note: the mod counter determines whether a check karo should be street or non-street to
	 * preserve repetitions of karos.
	 */
	public static char getCheckValue(char[][] map, int x, int y, int mod, Zone zone)
	{
		// System.out.println("x=" + x + ", y=" + y + ", mod=" + mod + ", zone=" + zone);
		if(mod == (x + y) % 2)
			return getRawValue(map, x, y);
		else
			return getNeighborValue(map, x, y, zone);
	}

	///////////////////////////
	// non-static
	///////////////////////////

	@Override
	public char getScaledValue(char[][] map, Point2D.Double origin, Point transformed, double scaleX, double scaleY)
	{
		int xi = (int) origin.getX();
		int yi = (int) origin.getY();

		Corner corner = getCorner(origin.getX() - xi + Math.abs(1 / scaleX) / 2, origin.getY() - yi + Math.abs(1 / scaleY) / 2);
		Zone zone = getZone(origin.getX() - xi, origin.getY() - yi);

		int mask = getMask(map, xi, yi);
		int mod = (transformed.x + transformed.y) % 2;
		// we need to offset mod by 1 if only 1 axis was mirrored
		if(scaleX < 0 ^ scaleY < 0)
			mod = (mod + 1) % 2;

		char originCenterValue = getRawValue(map, xi, yi);
		char originCheckValue = getCheckValue(map, xi, yi, mod, zone);
		char originNeighborValue = getNeighborValue(map, xi, yi, zone);

		switch(mask)
		{
			// @formatter:off
			
			///////////////////////////////
			// different check patterns		
			///////////////////////////////	
			
			// x shape										
			case 0b10101010: 	return originCheckValue;
			// 3 same corners										
			case 0b10101000:	return (corner == Corner.southwest ? originNeighborValue : originCheckValue);
			case 0b00101010:	return (corner == Corner.northwest ? originNeighborValue : originCheckValue);
			case 0b10001010:	return (corner == Corner.northeast ? originNeighborValue : originCheckValue);
			case 0b10100010:	return (corner == Corner.southeast ? originNeighborValue : originCheckValue);							
			// 2 opposite same corners										
			case 0b10001000:	return (corner == Corner.northeast || corner == Corner.southwest ? originNeighborValue : originCheckValue);
			case 0b00100010:	return (corner == Corner.northwest || corner == Corner.southeast ? originNeighborValue : originCheckValue);							
			// 2 adjacent same corners										
			case 0b10100000:	return (corner == Corner.southwest || corner == Corner.southeast ? originNeighborValue : originCheckValue);
			case 0b00101000:	return (corner == Corner.southwest || corner == Corner.northwest ? originNeighborValue : originCheckValue);
			case 0b00001010:	return (corner == Corner.northwest || corner == Corner.northeast ? originNeighborValue : originCheckValue);
			case 0b10000010:	return (corner == Corner.southeast || corner == Corner.northeast ? originNeighborValue : originCheckValue);							
			// full same edge, rest checkered										
			case 0b11101010:	return (corner == Corner.southeast || corner == Corner.southwest ? originCheckValue : originCenterValue);
			case 0b10111010:	return (corner == Corner.northwest || corner == Corner.southwest ? originCheckValue : originCenterValue);
			case 0b10101110:	return (corner == Corner.northwest || corner == Corner.northeast ? originCheckValue : originCenterValue);
			case 0b10101011:	return (corner == Corner.southeast || corner == Corner.northeast ? originCheckValue : originCenterValue);							
			// 2 other edges next to each other										
			case 0b10101111:	return (corner == Corner.northeast ? originCheckValue : originCenterValue);
			case 0b11101011:	return (corner == Corner.southeast ? originCheckValue : originCenterValue);
			case 0b11111010:	return (corner == Corner.southwest ? originCheckValue : originCenterValue);
			case 0b10111110:	return (corner == Corner.northwest ? originCheckValue : originCenterValue);							
			// single same corner										
			case 0b10000000:	return (corner == Corner.northwest || corner == Corner.center ? originCheckValue : originNeighborValue);
			case 0b00100000:	return (corner == Corner.northeast || corner == Corner.center ? originCheckValue : originNeighborValue);
			case 0b00001000:	return (corner == Corner.southeast || corner == Corner.center ? originCheckValue : originNeighborValue);
			case 0b00000010:	return (corner == Corner.southwest || corner == Corner.center ? originCheckValue : originNeighborValue);							
			// same edge + adjacent corner + 2 corners										
			case 0b01101010:
			case 0b11001010:	return (corner == Corner.southeast || corner == Corner.southwest ? originCheckValue : originCenterValue);
			case 0b10011010:	
			case 0b10110010:	return (corner == Corner.northwest || corner == Corner.southwest ? originCheckValue : originCenterValue);
			case 0b10100110:
			case 0b10101100:	return (corner == Corner.northwest || corner == Corner.northeast ? originCheckValue : originCenterValue);
			case 0b10101001:
			case 0b00101011:	return (corner == Corner.southeast || corner == Corner.northeast ? originCheckValue : originCenterValue);

			// 2 neighbored same edges + opposite corner (diagonal Y shape)										
			case 0b01010010:	return (corner == Corner.southwest ? originCheckValue : originCenterValue);
			case 0b10010100:	return (corner == Corner.northwest ? originCheckValue : originCenterValue);
			case 0b00100101:	return (corner == Corner.northeast ? originCheckValue : originCenterValue);
			case 0b01001001:	return (corner == Corner.southeast ? originCheckValue : originCenterValue);						
			// 2x2 same + opposite corner										
			case 0b01110010:	return (corner == Corner.southwest ? originCheckValue : originCenterValue);
			case 0b10011100:	return (corner == Corner.northwest ? originCheckValue : originCenterValue);
			case 0b00100111:	return (corner == Corner.northeast ? originCheckValue : originCenterValue);
			case 0b11001001:	return (corner == Corner.southeast ? originCheckValue : originCenterValue);						
			// 3 same corners + edges for the outer corners	= W + opposite corner									
			case 0b10101101:	return (corner == Corner.northeast ? originCheckValue : originCenterValue);
			case 0b01101011:	return (corner == Corner.southeast ? originCheckValue : originCenterValue);
			case 0b11011010:	return (corner == Corner.southwest ? originCheckValue : originCenterValue);
			case 0b10110110:	return (corner == Corner.northwest ? originCheckValue : originCenterValue);
			// same edge + adjacent corner + far corner										
			case 0b11001000:	return (corner == Corner.southwest ? originNeighborValue : (corner == Corner.southeast ? originCheckValue : originCenterValue));
			case 0b01100010:	return (corner == Corner.southeast ? originNeighborValue : (corner == Corner.southwest ? originCheckValue : originCenterValue));
			case 0b00110010:	return (corner == Corner.northwest ? originNeighborValue : (corner == Corner.southwest ? originCheckValue : originCenterValue));
			case 0b10011000:	return (corner == Corner.southwest ? originNeighborValue : (corner == Corner.northwest ? originCheckValue : originCenterValue));
			case 0b10001100:	return (corner == Corner.northeast ? originNeighborValue : (corner == Corner.northwest ? originCheckValue : originCenterValue));
			case 0b00100110:	return (corner == Corner.northwest ? originNeighborValue : (corner == Corner.northeast ? originCheckValue : originCenterValue));
			case 0b00100011:	return (corner == Corner.southeast ? originNeighborValue : (corner == Corner.northeast ? originCheckValue : originCenterValue));
			case 0b10001001:	return (corner == Corner.northeast ? originNeighborValue : (corner == Corner.southeast ? originCheckValue : originCenterValue));
			// same full edge + corner	
			case 0b11101000: 	return (corner == Corner.southwest ? originNeighborValue : (corner == Corner.southeast ? originCheckValue : originCenterValue));
			case 0b11100010:	return (corner == Corner.southeast ? originNeighborValue : (corner == Corner.southwest ? originCheckValue : originCenterValue));
			case 0b10111000:	return (corner == Corner.southwest ? originNeighborValue : (corner == Corner.northwest ? originCheckValue : originCenterValue));
			case 0b00111010:	return (corner == Corner.northwest ? originNeighborValue : (corner == Corner.southwest ? originCheckValue : originCenterValue));
			case 0b10001110:	return (corner == Corner.northeast ? originNeighborValue : (corner == Corner.northwest ? originCheckValue : originCenterValue));
			case 0b00101110:	return (corner == Corner.northwest ? originNeighborValue : (corner == Corner.northeast ? originCheckValue : originCenterValue));
			case 0b10100011:	return (corner == Corner.southeast ? originNeighborValue : (corner == Corner.northeast ? originCheckValue : originCenterValue));
			case 0b10001011:	return (corner == Corner.northeast ? originNeighborValue : (corner == Corner.southeast ? originCheckValue : originCenterValue));
			// same edge + adjacent corner + near corner
			case 0b11000010:	return (corner == Corner.southwest ? originCheckValue : originCenterValue);
			case 0b01101000:	return (corner == Corner.southeast ? originCheckValue : originCenterValue);
			case 0b10110000:	return (corner == Corner.northwest ? originCheckValue : originCenterValue);
			case 0b00011010:	return (corner == Corner.southwest ? originCheckValue : originCenterValue);
			case 0b00101100:	return (corner == Corner.northeast ? originCheckValue : originCenterValue);
			case 0b10000110:	return (corner == Corner.northwest ? originCheckValue : originCenterValue);
			case 0b00001011:	return (corner == Corner.southeast ? originCheckValue : originCenterValue);
			case 0b10100001:	return (corner == Corner.northeast ? originCheckValue : originCenterValue);
			// same edge + 1 independent corner	
			case 0b01001000:	return (corner == Corner.southeast ? originCheckValue : (corner == Corner.southwest ? originNeighborValue : originCenterValue));
			case 0b01000010:	return (corner == Corner.southwest ? originCheckValue : (corner == Corner.southeast ? originNeighborValue : originCenterValue));
			case 0b00010010:	return (corner == Corner.southwest ? originCheckValue : (corner == Corner.northwest ? originNeighborValue : originCenterValue));
			case 0b10010000:	return (corner == Corner.northwest ? originCheckValue : (corner == Corner.southwest ? originNeighborValue : originCenterValue));
			case 0b10000100:	return (corner == Corner.northwest ? originCheckValue : (corner == Corner.northeast ? originNeighborValue : originCenterValue));
			case 0b00100100:	return (corner == Corner.northeast ? originCheckValue : (corner == Corner.northwest ? originNeighborValue : originCenterValue));
			case 0b00100001:	return (corner == Corner.northeast ? originCheckValue : (corner == Corner.southeast ? originNeighborValue : originCenterValue));
			case 0b00001001:	return (corner == Corner.southeast ? originCheckValue : (corner == Corner.northeast ? originNeighborValue : originCenterValue));
			// same corner + same corner with edge + edge	
			case 0b10110100:	return (corner == Corner.northwest ? originCheckValue : originCenterValue);
			case 0b01011010:	return (corner == Corner.southwest ? originCheckValue : originCenterValue);
			case 0b00101101:	return (corner == Corner.northeast ? originCheckValue : originCenterValue);
			case 0b10010110:	return (corner == Corner.northwest ? originCheckValue : originCenterValue);
			case 0b01001011:	return (corner == Corner.southeast ? originCheckValue : originCenterValue);
			case 0b10100101:	return (corner == Corner.northeast ? originCheckValue : originCenterValue);
			case 0b11010010:	return (corner == Corner.southwest ? originCheckValue : originCenterValue);
			case 0b01101001:	return (corner == Corner.southeast ? originCheckValue : originCenterValue);
			// 2 other edges next to each other + 1 adjacent corner	
			case 0b00101111:
			case 0b10100111:	return (corner == Corner.northeast ? originCheckValue : originCenterValue);
			case 0b11001011:
			case 0b11101001:	return (corner == Corner.southeast ? originCheckValue : originCenterValue);
			case 0b11110010:
			case 0b01111010:	return (corner == Corner.southwest ? originCheckValue : originCenterValue);
			case 0b10111100:
			case 0b10011110:	return (corner == Corner.northwest ? originCheckValue : originCenterValue);
			// same edge + 2 independent corner (Y-shape)										
			case 0b01001010: 	return (corner == Corner.southeast || corner == Corner.southwest ? originCheckValue : originCenterValue);
			case 0b10010010: 	return (corner == Corner.northwest || corner == Corner.southwest ? originCheckValue : originCenterValue);
			case 0b10100100: 	return (corner == Corner.northwest || corner == Corner.northeast ? originCheckValue : originCenterValue);
			case 0b00101001: 	return (corner == Corner.southeast || corner == Corner.northeast ? originCheckValue : originCenterValue);
				
			//////////////////////////////////////////////////////////////		
			// on the edge
			//////////////////////////////////////////////////////////////		

			// other corner L shape										
			case 0b00001111: 
			case 0b10000111:	return (corner == Corner.northeast ? originNeighborValue : originCenterValue);
			case 0b11000011: 
			case 0b11100001:	return (corner == Corner.southeast ? originNeighborValue : originCenterValue);
			case 0b11110000: 
			case 0b01111000:	return (corner == Corner.southwest ? originNeighborValue : originCenterValue);
			case 0b00111100: 
			case 0b00011110:	return (corner == Corner.northwest ? originNeighborValue : originCenterValue);
			// other corner + 2 adjacent edges = same double L										
			case 0b00111110:	return (corner == Corner.northwest ? originNeighborValue : originCenterValue);
			case 0b10001111:	return (corner == Corner.northeast ? originNeighborValue : originCenterValue);
			case 0b11100011:	return (corner == Corner.southeast ? originNeighborValue : originCenterValue);
			case 0b11111000:	return (corner == Corner.southwest ? originNeighborValue : originCenterValue);
			// full same edge										
			case 0b11100000:	return (corner == Corner.southwest || corner == Corner.southeast ? originNeighborValue : originCenterValue);
			case 0b00111000:	return (corner == Corner.southwest || corner == Corner.northwest ? originNeighborValue : originCenterValue);
			case 0b00001110:	return (corner == Corner.northeast || corner == Corner.northwest ? originNeighborValue : originCenterValue);
			case 0b10000011:	return (corner == Corner.northeast || corner == Corner.southeast ? originNeighborValue : originCenterValue);
			// same corner + 1 adjacent edge										
			case 0b11000000:
			case 0b00011000:	return (corner == Corner.southwest ? originNeighborValue : originCenterValue);
			case 0b01100000:
			case 0b00000011:	return (corner == Corner.southeast ? originNeighborValue : originCenterValue);
			case 0b00110000:
			case 0b00000110:	return (corner == Corner.northwest ? originNeighborValue : originCenterValue);
			case 0b00001100:
			case 0b10000001:	return (corner == Corner.northeast ? originNeighborValue : originCenterValue);
			// stair shape										
			case 0b11011000:	return (corner == Corner.southwest ? originNeighborValue : originCenterValue);
			case 0b00110110:	return (corner == Corner.northwest ? originNeighborValue : originCenterValue);
			case 0b10001101:	return (corner == Corner.northeast ? originNeighborValue : originCenterValue);
			case 0b01100011:	return (corner == Corner.southeast ? originNeighborValue : originCenterValue);
			// other corner L shape + opposite corner = Tetris zick-zack-block									
			case 0b00001101:
			case 0b10000101:	return (corner == Corner.northeast ? originNeighborValue : originCenterValue);
			case 0b01000011:
			case 0b01100001:	return (corner == Corner.southeast ? originNeighborValue : originCenterValue);
			case 0b11010000:
			case 0b01011000:	return (corner == Corner.southwest ? originNeighborValue : originCenterValue);
			case 0b00110100:
			case 0b00010110:	return (corner == Corner.northwest ? originNeighborValue : originCenterValue);
			
			//////////////////////////////////////////////////////////////		
			// everything below here will be default = center value
			//////////////////////////////////////////////////////////////	
				
			// filled same										
			case 0b11111111:	//return (corner == Corner.southwest ? originNeighborValue : originCenterValue);
			// surrounded other										
			case 0b00000000:
			// single other corner										
			case 0b01111111:
			case 0b11011111:
			case 0b11110111:
			case 0b11111101:
			// single other edge										
			case 0b10111111:
			case 0b11101111:
			case 0b11111011:
			case 0b11111110:
			// 2 opposite other edges										
			case 0b10111011:
			case 0b11101110:
			// other corner + 1 adjacent edge										
			case 0b00111111:
			case 0b10011111:
			case 0b11001111:
			case 0b11100111:
			case 0b11110011:
			case 0b11111001:
			case 0b11111100:
			case 0b01111110:
			// 2 independent other corners										
			case 0b01011111:
			case 0b11010111:
			case 0b11110101:
			case 0b01111101:
			case 0b01110111:
			case 0b11011101:
			// 3 independent other corners										
			case 0b01010111:
			case 0b01011101:
			case 0b01110101:
			case 0b11010101:
			// + shape										
			case 0b01010101:
			// full other edge = 2 corners + edge in between										
			case 0b00011111:
			case 0b11000111:
			case 0b11110001:
			case 0b01111100:
			// other corner + independent edge										
			case 0b01101111:
			case 0b01111011:
			case 0b11011011:
			case 0b11011110:
			case 0b11110110:
			case 0b10110111:
			case 0b10111101:
			case 0b11101101:
			// other corner + opposite corner with edge									
			case 0b00110111:
			case 0b10011101:
			case 0b11001101:
			case 0b01100111:
			case 0b01110011:
			case 0b11011001:
			case 0b11011100:
			case 0b01110110:
			// other corner + neighbor corner with edge										
			case 0b00111101:
			case 0b10010111:
			case 0b01001111:
			case 0b11100101:
			case 0b11010011:
			case 0b01111001:
			case 0b11110100:
			case 0b01011110:
			// other corner double L shape = 2x2 same										
			case 0b00000111:
			case 0b11000001:
			case 0b01110000:
			case 0b00011100:
			// other full edge + 2 corners = small T										
			case 0b00010101:
			case 0b01000101: 
			case 0b01010001:
			case 0b01010100:
			// 2 other full edges = 2 opposite same edges										
			case 0b00010001:
			case 0b01000100:
			// same T shape										
			case 0b11100100:
			case 0b00111001:
			case 0b01001110:
			case 0b10010011:
			// same C shape										
			case 0b01101100:
			case 0b00011011:
			case 0b11000110:
			case 0b10110001:
			// single same edge										
			case 0b01000000:
			case 0b00010000:
			case 0b00000100:
			case 0b00000001:
			// 2x2 same + 1 edge										
			case 0b01110100:
			case 0b01110001:
			case 0b00011101:
			case 0b01011100:
			case 0b01000111:
			case 0b00010111:
			case 0b11010001:
			case 0b11000101:
			// T+L shape										
			case 0b11101100:
			case 0b11100110:
			case 0b00111011:
			case 0b10111001:
			case 0b11001110:
			case 0b01101110:
			case 0b10110011:
			case 0b10011011:
			// 3 same edges + corners on the 4th side										
			case 0b11010110:
			case 0b10110101:
			case 0b01101101:
			case 0b01011011:
			// small T shape + 1 adjacent corner										
			case 0b01010110:
			case 0b11010100:
			case 0b10010101:
			case 0b00110101:
			case 0b01100101:
			case 0b01001101:
			case 0b01011001:
			case 0b01010011:
			// 2 opposite same edges with opposite same corners										
			case 0b11001100:
			case 0b01100110:
			case 0b00110011:
			case 0b10011001:							
			// other corner double L shape + opposite corner = small L										
			case 0b00000101:
			case 0b01000001:
			case 0b01010000:
			case 0b00010100:
			// same edge + adjacent corner + straight edge = L shape										
			case 0b11000100:
			case 0b01100100:
			case 0b00110001:
			case 0b00011001:
			case 0b01001100:
			case 0b01000110:
			case 0b00010011:
			case 0b10010001:
				
			// default
			default:
				return originCenterValue;
			// @formatter:on
		}
	}
}
