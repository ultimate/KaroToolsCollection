package ultimate.karopapier.transformer;

import java.awt.geom.Point2D;

public class MapTransformer
{
	// @formatter:off
	public static final String TEST_CODE  = "XYXYXYXYXYXYXYXYXYOOOOOOOOOOOOOOOOOOXYXYXYXYXYXYXYXYXYOOOOOOOOOOOOOOOOOOXYXYXYXYXYXYXYXYXYOOOOOOOOOOOOOOOOOO\n"+
											"YXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOOYXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOOYXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOO\n"+
											"XYXYOYXYXOXYXOXYXYOOOOXOOOOXOOOXOOOOXYXYOYXYXYXOXYXYXYOOOOXOOOOOOXOOOOOOXYOYOYOYXOOYOOXYXOXOOXOXOXOOXXOXXOOO\n"+
											"YXYOYOYXYXOXOXYXYXOOOXOXOOOOXOXOOOOOYXYOYOYXYXOOOXYXYXOOOXOXOOOOXXXOOOOOYXOOYOOXYXOOOXYXYXOOOXXOXXOOOXXXOOOO\n"+
											"XYOYOYOYXYXOXYXYXYOOXOXOXOOOOXOOOOOOXYOOOOOYXOXOXOXYXYOOXXXXXOOXOXOXOOOOXYXOOOXYXYXOXYXYXOXOOOXXXOOOOOXOOOOO\n"+
											"YXYOYOYXYXOXOXYXYXOOOXOXOOOOXOXOOOOOYXYOYOYXYXOOOXYXYXOOOXOXOOOOXXXOOOOOYXOOYOOXYXOOOXYXYXOOOXXOXXOOOXXXOOOO\n"+
											"XYXYOYXYXOXYXOXYXYOOOOXOOOOXOOOXOOOOXYXYOYXYXYXOXYXYXYOOOOXOOOOOOXOOOOOOXYOYOYOYXOOYOOXYXOXOOXOXOXOOXXOXXOOO\n"+
											"YXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOOYXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOOYXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOO\n"+
											"XYXYXYXYXYXYXYXYXYOOOOOOOOOOOOOOOOOOXYXYXYXYXYXYXYXYXYOOOOOOOOOOOOOOOOOOXYXYXYXYXYXYXYXYXYOOOOOOOOOOOOOOOOOO\n"+
											"YXYXYOYXYXYXOXYXYXOOOOOXOOOOOOXOOOOOYXOOYOOXYOXYXOYXYXOOXXOXXOOXOOOXOOOOYXOXOXYXOOYXYXOXYXOOXOXOOOXXOOOOXOOO\n"+
											"XYXYOOXYXYXYOOXYXYOOOOXXOOOOOOXXOOOOXYXYOYXYXOYOYOXYXYOOOOXOOOOXOXOXOOOOXYOYOYXYXYOOXYOOXYOOXOXOOOOOXXOOXXOO\n"+
											"YXOOOOOXYXYOOOOOYXOOXXXXXOOOOXXXXXOOYXYOYOYXYXOXOXYXYXOOOXOXOOOOXOXOOOOOYXYOYOYXOOYXYXYXYXOOOXOXOOXXOOOOOOOO\n"+
											"XYXOOOOOXYOOOOOYXYOOOXXXXXOOXXXXXOOOXYXYOYXYXOYOYOXYXYOOOOXOOOOXOXOXOOOOXYXOXOXYXYOOXYXOXYOOOXOXOOOOXXOOOXOO\n"+
											"YXYXOOYXYXYXOOYXYXOOOOXXOOOOOOXXOOOOYXOOYOOYXOXYXOYXYXOOXXOXXOOXOOOXOOOOYXYXYXYXYXYXYXOOYXOOOOOOOOOOOOOOXXOO\n"+
											"XYXYOYXYXYXYXOXYXYOOOOXOOOOOOOOXOOOOXYXYXYXYXYXYXYXYXYOOOOOOOOOOOOOOOOOOXYXYXYXYXYXYXYXYXYOOOOOOOOOOOOOOOOOO\n"+
											"YXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOOYXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOOYXYOYXYOYXYXYXOOYXOOOXOOOXOOOOOOXXOO\n"+
											"XYXYXYXYXYXYXYXYXYOOOOOOOOOOOOOOOOOOXYXOXYXYXYOYXYOYOYOOOXOOOOOOXOOOXOXOXYXOOYOOXYXYXYOYXYOOOXXOXXOOOOOOXOOO\n"+
											"YXYXOXYXYXYXYOYXYXOOOOXOOOOOOOOXOOOOYXOOYXYXYXOOYXOXOXOOXXOOOOOOXXOOXOXOYXYXOXOXYXYXYXYXYXOOOOXOXOOOOOOOOOOO\n"+
											"XYXOOOXYXYXYOYOYXYOOOXXXOOOOOOXOXOOOXYXYOOXYOOXYXYOYOYOOOOXXOOXXOOOOXOXOXYXYXYXYXYXYXYOOXYOOOOOOOOOOOOOOXXOO\n"+
											"YXOOYOOXYXYOYXYOXYOOXXOXXOOOOXOOOXOOYXYXOXYXYOYXYXYXYXOOOOXOOOOXOOOOOOOOYXOOYXYOOXYXYXYOYXOOXXOOOXXOOOOOOXOO\n"+
											"XYXOOOXYXYXYOYOXYXOOOXXXOOOOOOXOXOOOXYXYXYXYXYXYXYOOOYOOOOOOOOOOOOOOXXXOXYXOOYOOXYXYXYXYXYOOOXXOXXOOOOOOOOOO\n"+
											"YXYXOXYXYXYXYOXYXYOOOOXOOOOOOOOXOOOOYXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOOYXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOO\n"+
											"XYXYXYXYXYXYXYXYXYOOOOOOOOOOOOOOOOOOXYXYXYXYXYXYXYXYXYOOOOOOOOOOOOOOOOOOXYXYXYXYXYXYXYXYXYOOOOOOOOOOOOOOOOOO\n"+
											"YXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOOYXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOOYXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOO";
	
	public static final String LINE_SEPARATOR = "\n";
	// @formatter:on

	// @formatter:off
	public static double[][] createMatrix(double scaleX, double scaleY, int rotation, int mapWidth, int mapHeight)
	{
		double sin = Math.sin(rotation * Math.PI / 180.0);
		double cos = Math.cos(rotation * Math.PI / 180.0);
		
		double[][] matrix = new double[][] {
			{ cos*scaleX, -sin*scaleX, 0 },
			{ sin*scaleY, cos*scaleY, 0 },
			{ 0, 0, 1 } 
		};
		
		// compensate offset
		Point2D.Double[] transformedCorners = new Point2D.Double[] {
			applyMatrix(matrix, new Point2D.Double( 0, 0 )),
			applyMatrix(matrix, new Point2D.Double( 0, mapHeight )),
			applyMatrix(matrix, new Point2D.Double( mapWidth, 0 )),
			applyMatrix(matrix, new Point2D.Double( mapWidth, mapHeight))
		};
		Point2D.Double min = min(transformedCorners);
		matrix[0][2] = -min.x;
		matrix[1][2] = -min.y;
		
		return matrix;
	}
	
	public static Point2D.Double min(Point2D.Double... points)
	{
		Point2D.Double min = new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		for(Point2D.Double p: points)
		{
			if(p.x < min.x)
				min.x = p.x;
			if(p.y < min.y)
				min.y = p.y;
		}
		return min;
	}
	
	public static Point2D.Double max(Point2D.Double... points)
	{
		Point2D.Double max = new Point2D.Double(0, 0);
		for(Point2D.Double p: points)
		{
			if(p.x > max.x)
				max.x = p.x;
			if(p.y > max.y)
				max.y = p.y;
		}
		return max;
	}
	
	public static Point2D.Double applyMatrix(double[][] matrix, Point2D.Double point)
	{
		return new Point2D.Double(
				matrix[0][0] * point.x + matrix[0][1] * point.y + matrix[0][2],
				matrix[1][0] * point.x + matrix[1][1] * point.y + matrix[1][2]
		);
	}
	
	public static double[][] invertMatrix(double[][] matrix)
	{
		double a = matrix[0][0];
		double b = matrix[0][1];
		double c = matrix[0][2];
		double d = matrix[1][0];
		double e = matrix[1][1];
		double f = matrix[1][2];
		double g = matrix[2][0];
		double h = matrix[2][1];
		double i = matrix[2][2];
		
		// https://studyflix.de/mathematik/determinante-3x3-2326
		double det = a*e*i+b*f*g+c*d*h-g*e*c-h*f*a-i*d*b;
		
		// https://de.wikipedia.org/wiki/Inverse_Matrix#Explizite_Formeln
		return new double[][] {
			{ (e*i-f*h)/det, (c*h-b*i)/det, (b*f-c*e)/det },
			{ (f*g-d*i)/det, (a*i-c*g)/det, (c*d-a*f)/det },
			{ (d*h-e*g)/det, (b*b-a*h)/det, (a*e-b*d)/det } 
		};
	}
	// @formatter:on

	public static char[][] toArray(String mapCode)
	{
		String[] lines = mapCode.split(LINE_SEPARATOR);
		char[][] array = new char[lines.length][];

		int lineLength = lines[0].length();

		for(int i = 0; i < lines.length; i++)
		{
			while(lines[i].length() < lineLength)
				lines[i] += "L";
			array[i] = lines[i].toCharArray();
		}

		return array;
	}

	public static void printMatrix(double[][] matrix)
	{
		System.out.println(matrix[0][0] + "\t" + matrix[0][1] + "\t" + matrix[0][2]);
		System.out.println(matrix[1][0] + "\t" + matrix[1][1] + "\t" + matrix[1][2]);
		System.out.println(matrix[2][0] + "\t" + matrix[2][1] + "\t" + matrix[2][2]);
	}

	public static void printMap(char[][] map)
	{
		for(char[] row : map)
		{
			if(row == null)
			{
				System.out.println("null");
				continue;
			}
			for(char c : row)
				System.out.print(c);
			System.out.println();
		}
	}

	public static boolean isRoad(char c)
	{
		return "SOF123456789".indexOf(c) != -1;
	}

	public static char getValue(char[][] map, int x, int y)
	{
		try
		{
			if(y < 0)
				y = 0;
			else if(y >= map.length)
				y = map.length - 1;
			if(x < 0)
				x = 0;
			else if(x >= map[y].length)
				x = map[y].length - 1;
			return map[y][x];
		}
		catch(NullPointerException e)
		{
			printMap(map);
			return 'X';
		}
	}

	public static int getMask(char[][] map, int x, int y)
	{
		boolean centerIsRoad = isRoad(getValue(map, x, y));
		int mask = 0;
		mask += (isRoad(getValue(map, x - 1, y - 1)) == centerIsRoad ? 128 : 0);
		mask += (isRoad(getValue(map, x + 0, y - 1)) == centerIsRoad ? 64 : 0);
		mask += (isRoad(getValue(map, x + 1, y - 1)) == centerIsRoad ? 32 : 0);
		mask += (isRoad(getValue(map, x + 1, y + 0)) == centerIsRoad ? 16 : 0);
		mask += (isRoad(getValue(map, x + 1, y + 1)) == centerIsRoad ? 8 : 0);
		mask += (isRoad(getValue(map, x + 0, y + 1)) == centerIsRoad ? 4 : 0);
		mask += (isRoad(getValue(map, x - 1, y + 1)) == centerIsRoad ? 2 : 0);
		mask += (isRoad(getValue(map, x - 1, y + 0)) == centerIsRoad ? 1 : 0);
		return mask;
	}

	public enum Corner
	{
		center, northeast, southeast, southwest, northwest
	}

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

	public enum Zone
	{
		north, east, south, west
	}

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

	public static char getNeighborValue(char[][] map, int x, int y, Zone zone)
	{
		if(zone == Zone.north)
			return getValue(map, x + 0, y - 1);
		if(zone == Zone.east)
			return getValue(map, x + 1, y + 0);
		if(zone == Zone.south)
			return getValue(map, x + 0, y + 1);
		if(zone == Zone.west)
			return getValue(map, x - 1, y + 0);
		return getValue(map, x, y);
	}

	public static char getCheckValue(char[][] map, int x, int y, int mod, Zone zone)
	{
		// System.out.println("x=" + x + ", y=" + y + ", mod=" + mod + ", zone=" + zone);
		if(mod == (x + y) % 2)
			return getValue(map, x, y);
		else
			return getNeighborValue(map, x, y, zone);
	}

	public static char[][] transform(char[][] original, double[][] matrix, boolean smartScale)
	{
		// System.out.println("matrix = ");
		// printMatrix(matrix);
		double[][] inv = invertMatrix(matrix);
		// System.out.println("inv = ");
		// printMatrix(inv);

		int oldSizeY = original.length;
		int oldSizeX = original[0].length;

		// calculate new size
		Point2D.Double[] transformedCorners = new Point2D.Double[] { applyMatrix(matrix, new Point2D.Double(0, 0)),
				applyMatrix(matrix, new Point2D.Double(0, oldSizeY)), applyMatrix(matrix, new Point2D.Double(oldSizeX, 0)),
				applyMatrix(matrix, new Point2D.Double(oldSizeX, oldSizeY)) };
		Point2D.Double max = max(transformedCorners);
		int newSizeX = (int) Math.ceil(max.x);
		int newSizeY = (int) Math.ceil(max.y);

		char[][] scaled = new char[newSizeY][];

		Point2D.Double p0 = applyMatrix(inv, new Point2D.Double(0, 0));
		Point2D.Double p1 = applyMatrix(inv, new Point2D.Double(1, 1));
		double scaleX = p1.x - p0.x;
		double scaleY = p1.y - p0.y;

		System.out.println("scaleX=" + scaleX + ", scaleY=" + scaleY);

		Point2D.Double origin;
		int originX, originY;
		int mask, mod;
		Corner corner;
		Zone zone;
		char scaledValue, originCenterValue, originCheckValue, originNeighborValue;
		for(int y = 0; y < newSizeY; y++)
		{
			scaled[y] = new char[newSizeX];
			for(int x = 0; x < newSizeX; x++)
			{
				origin = applyMatrix(inv, new Point2D.Double(x, y));
				originX = (int) (origin.x);
				originY = (int) (origin.y);

				corner = getCorner(origin.x - originX + scaleX / 2, origin.y - originY + scaleY / 2);
				zone = getZone(origin.x - originX, origin.y - originY);

				originCenterValue = getValue(original, originX, originY);

				if(smartScale)
				{
					mask = getMask(original, originX, originY);
					mod = (x + y) % 2;

					originCheckValue = getCheckValue(original, originX, originY, mod, zone);
					originNeighborValue = getNeighborValue(original, originX, originY, zone);

//					if(false)// originX == 10 && originY == 5)
//					{
//						// @formatter:off
//						System.out.println("originX=" + originX + ",originY=" + originY +
//								" -> mask=" + Integer.toBinaryString(mask) +
//								", mod=" + mod +
//								", corner=" + corner +
//								", zone=" + zone +
//								", fractions=" + (origin.x - originX) + "/" + (origin.y - originY));
//						// @formatter:on
//					}

					switch(mask)
					{
						// @formatter:off
						
						///////////////////////////////
						// different check patterns		
						///////////////////////////////		

						// x shape										
						case 0b10101010: 	scaledValue = originCheckValue;	break;
						// 3 same corners										
						case 0b10101000:	scaledValue = (corner == Corner.southwest ? originNeighborValue : originCheckValue); break;
						case 0b00101010:	scaledValue = (corner == Corner.northwest ? originNeighborValue : originCheckValue); break;
						case 0b10001010:	scaledValue = (corner == Corner.northeast ? originNeighborValue : originCheckValue); break;
						case 0b10100010:	scaledValue = (corner == Corner.southeast ? originNeighborValue : originCheckValue); break;							
						// 2 opposite same corners										
						case 0b10001000:	scaledValue = (corner == Corner.northeast || corner == Corner.southwest ? originNeighborValue : originCheckValue); break;
						case 0b00100010:	scaledValue = (corner == Corner.northwest || corner == Corner.southeast ? originNeighborValue : originCheckValue); break;							
						// 2 adjacent same corners										
						case 0b10100000:	scaledValue = (corner == Corner.southwest || corner == Corner.southeast ? originNeighborValue : originCheckValue); break;
						case 0b00101000:	scaledValue = (corner == Corner.southwest || corner == Corner.northwest ? originNeighborValue : originCheckValue); break;
						case 0b00001010:	scaledValue = (corner == Corner.northwest || corner == Corner.northeast ? originNeighborValue : originCheckValue); break;
						case 0b10000010:	scaledValue = (corner == Corner.southeast || corner == Corner.northeast ? originNeighborValue : originCheckValue); break;							
						// full same edge, rest checkered										
						case 0b11101010:	scaledValue = (corner == Corner.southeast || corner == Corner.southwest ? originCheckValue : originCenterValue); break;
						case 0b10111010:	scaledValue = (corner == Corner.northwest || corner == Corner.southwest ? originCheckValue : originCenterValue); break;
						case 0b10101110:	scaledValue = (corner == Corner.northwest || corner == Corner.northeast ? originCheckValue : originCenterValue); break;
						case 0b10101011:	scaledValue = (corner == Corner.southeast || corner == Corner.northeast ? originCheckValue : originCenterValue); break;							
						// 2 other edges next to each other										
						case 0b10101111:	scaledValue = (corner == Corner.northeast ? originCheckValue : originCenterValue); break;
						case 0b11101011:	scaledValue = (corner == Corner.southeast ? originCheckValue : originCenterValue); break;
						case 0b11111010:	scaledValue = (corner == Corner.southwest ? originCheckValue : originCenterValue); break;
						case 0b10111110:	scaledValue = (corner == Corner.northwest ? originCheckValue : originCenterValue); break;							
						// single same corner										
						case 0b10000000:	scaledValue = (corner == Corner.northwest || corner == Corner.center ? originCheckValue : originNeighborValue); break;
						case 0b00100000:	scaledValue = (corner == Corner.northeast || corner == Corner.center ? originCheckValue : originNeighborValue); break;
						case 0b00001000:	scaledValue = (corner == Corner.southeast || corner == Corner.center ? originCheckValue : originNeighborValue); break;
						case 0b00000010:	scaledValue = (corner == Corner.southwest || corner == Corner.center ? originCheckValue : originNeighborValue); break;							
						// same edge + adjacent corner + 2 corners										
						case 0b01101010:
						case 0b11001010:	scaledValue = (corner == Corner.southeast || corner == Corner.southwest ? originCheckValue : originCenterValue); break;
						case 0b10011010:	
						case 0b10110010:	scaledValue = (corner == Corner.northwest || corner == Corner.southwest ? originCheckValue : originCenterValue); break;
						case 0b10100110:
						case 0b10101100:	scaledValue = (corner == Corner.northwest || corner == Corner.northeast ? originCheckValue : originCenterValue); break;
						case 0b10101001:
						case 0b00101011:	scaledValue = (corner == Corner.southeast || corner == Corner.northeast ? originCheckValue : originCenterValue); break;

						// 2 neighbored same edges + opposite corner (diagonal Y shape)										
						case 0b01010010:	scaledValue = (corner == Corner.southwest ? originCheckValue : originCenterValue); break;
						case 0b10010100:	scaledValue = (corner == Corner.northwest ? originCheckValue : originCenterValue); break;
						case 0b00100101:	scaledValue = (corner == Corner.northeast ? originCheckValue : originCenterValue); break;
						case 0b01001001:	scaledValue = (corner == Corner.southeast ? originCheckValue : originCenterValue); break;						
						// 2x2 same + opposite corner										
						case 0b01110010:	scaledValue = (corner == Corner.southwest ? originCheckValue : originCenterValue); break;
						case 0b10011100:	scaledValue = (corner == Corner.northwest ? originCheckValue : originCenterValue); break;
						case 0b00100111:	scaledValue = (corner == Corner.northeast ? originCheckValue : originCenterValue); break;
						case 0b11001001:	scaledValue = (corner == Corner.southeast ? originCheckValue : originCenterValue); break;						
						// 3 same corners + edges for the outer corners	= W + opposite corner									
						case 0b10101101:	scaledValue = (corner == Corner.northeast ? originCheckValue : originCenterValue); break;
						case 0b01101011:	scaledValue = (corner == Corner.southeast ? originCheckValue : originCenterValue); break;
						case 0b11011010:	scaledValue = (corner == Corner.southwest ? originCheckValue : originCenterValue); break;
						case 0b10110110:	scaledValue = (corner == Corner.northwest ? originCheckValue : originCenterValue); break;
						// same edge + adjacent corner + far corner										
						case 0b11001000:	scaledValue = (corner == Corner.southwest ? originNeighborValue : corner == Corner.southeast ? originCheckValue : originCenterValue); break;
						case 0b01100010:	scaledValue = (corner == Corner.southeast ? originNeighborValue : corner == Corner.southwest ? originCheckValue : originCenterValue); break;
						case 0b00110010:	scaledValue = (corner == Corner.northwest ? originNeighborValue : corner == Corner.southwest ? originCheckValue : originCenterValue); break;
						case 0b10011000:	scaledValue = (corner == Corner.southwest ? originNeighborValue : corner == Corner.northwest ? originCheckValue : originCenterValue); break;
						case 0b10001100:	scaledValue = (corner == Corner.northeast ? originNeighborValue : corner == Corner.northwest ? originCheckValue : originCenterValue); break;
						case 0b00100110:	scaledValue = (corner == Corner.northwest ? originNeighborValue : corner == Corner.northeast ? originCheckValue : originCenterValue); break;
						case 0b00100011:	scaledValue = (corner == Corner.southeast ? originNeighborValue : corner == Corner.northeast ? originCheckValue : originCenterValue); break;
						case 0b10001001:	scaledValue = (corner == Corner.northeast ? originNeighborValue : corner == Corner.southeast ? originCheckValue : originCenterValue); break;
						// same full edge + corner	
						case 0b11101000: 	scaledValue = (corner == Corner.southwest ? originNeighborValue : (corner == Corner.southeast ? originCheckValue : originCenterValue)); break;
						case 0b11100010:	scaledValue = (corner == Corner.southeast ? originNeighborValue : (corner == Corner.southwest ? originCheckValue : originCenterValue)); break;
						case 0b10111000:	scaledValue = (corner == Corner.southwest ? originNeighborValue : (corner == Corner.northwest ? originCheckValue : originCenterValue)); break;
						case 0b00111010:	scaledValue = (corner == Corner.northwest ? originNeighborValue : (corner == Corner.southwest ? originCheckValue : originCenterValue)); break;
						case 0b10001110:	scaledValue = (corner == Corner.northeast ? originNeighborValue : (corner == Corner.northwest ? originCheckValue : originCenterValue)); break;
						case 0b00101110:	scaledValue = (corner == Corner.northwest ? originNeighborValue : (corner == Corner.northeast ? originCheckValue : originCenterValue)); break;
						case 0b10100011:	scaledValue = (corner == Corner.southeast ? originNeighborValue : (corner == Corner.northeast ? originCheckValue : originCenterValue)); break;
						case 0b10001011:	scaledValue = (corner == Corner.northeast ? originNeighborValue : (corner == Corner.southeast ? originCheckValue : originCenterValue)); break;
						// same edge + adjacent corner + near corner
						case 0b11000010:	scaledValue = (corner == Corner.southwest ? originCheckValue : originCenterValue); break;
						case 0b01101000:	scaledValue = (corner == Corner.southeast ? originCheckValue : originCenterValue); break;
						case 0b10110000:	scaledValue = (corner == Corner.northwest ? originCheckValue : originCenterValue); break;
						case 0b00011010:	scaledValue = (corner == Corner.southwest ? originCheckValue : originCenterValue); break;
						case 0b00101100:	scaledValue = (corner == Corner.northeast ? originCheckValue : originCenterValue); break;
						case 0b10000110:	scaledValue = (corner == Corner.northwest ? originCheckValue : originCenterValue); break;
						case 0b00001011:	scaledValue = (corner == Corner.southeast ? originCheckValue : originCenterValue); break;
						case 0b10100001:	scaledValue = (corner == Corner.northeast ? originCheckValue : originCenterValue); break;
						// same edge + 1 independent corner	
						case 0b01001000:	scaledValue = (corner == Corner.southeast ? originCheckValue : (corner == Corner.southwest ? originNeighborValue : originCenterValue)); break;
						case 0b01000010:	scaledValue = (corner == Corner.southwest ? originCheckValue : (corner == Corner.southeast ? originNeighborValue : originCenterValue)); break;
						case 0b00010010:	scaledValue = (corner == Corner.southwest ? originCheckValue : (corner == Corner.northwest ? originNeighborValue : originCenterValue)); break;
						case 0b10010000:	scaledValue = (corner == Corner.northwest ? originCheckValue : (corner == Corner.southwest ? originNeighborValue : originCenterValue)); break;
						case 0b10000100:	scaledValue = (corner == Corner.northwest ? originCheckValue : (corner == Corner.northeast ? originNeighborValue : originCenterValue)); break;
						case 0b00100100:	scaledValue = (corner == Corner.northeast ? originCheckValue : (corner == Corner.northwest ? originNeighborValue : originCenterValue)); break;
						case 0b00100001:	scaledValue = (corner == Corner.northeast ? originCheckValue : (corner == Corner.southeast ? originNeighborValue : originCenterValue)); break;
						case 0b00001001:	scaledValue = (corner == Corner.southeast ? originCheckValue : (corner == Corner.northeast ? originNeighborValue : originCenterValue)); break;
						// same corner + same corner with edge + edge	
						case 0b10110100:	scaledValue = (corner == Corner.northwest ? originCheckValue : originCenterValue); break;
						case 0b01011010:	scaledValue = (corner == Corner.southwest ? originCheckValue : originCenterValue); break;
						case 0b00101101:	scaledValue = (corner == Corner.northeast ? originCheckValue : originCenterValue); break;
						case 0b10010110:	scaledValue = (corner == Corner.northwest ? originCheckValue : originCenterValue); break;
						case 0b01001011:	scaledValue = (corner == Corner.southeast ? originCheckValue : originCenterValue); break;
						case 0b10100101:	scaledValue = (corner == Corner.northeast ? originCheckValue : originCenterValue); break;
						case 0b11010010:	scaledValue = (corner == Corner.southwest ? originCheckValue : originCenterValue); break;
						case 0b01101001:	scaledValue = (corner == Corner.southeast ? originCheckValue : originCenterValue); break;
						// 2 other edges next to each other + 1 adjacent corner	
						case 0b00101111:
						case 0b10100111:	scaledValue = (corner == Corner.northeast ? originCheckValue : originCenterValue); break;
						case 0b11001011:
						case 0b11101001:	scaledValue = (corner == Corner.southeast ? originCheckValue : originCenterValue); break;
						case 0b11110010:
						case 0b01111010:	scaledValue = (corner == Corner.southwest ? originCheckValue : originCenterValue); break;
						case 0b10111100:
						case 0b10011110:	scaledValue = (corner == Corner.northwest ? originCheckValue : originCenterValue); break;
						// same edge + 2 independent corner (Y-shape)										
						case 0b01001010: 	scaledValue = (corner == Corner.southeast || corner == Corner.southwest ? originCheckValue : originCenterValue); break;
						case 0b10010010: 	scaledValue = (corner == Corner.northwest || corner == Corner.southwest ? originCheckValue : originCenterValue); break;
						case 0b10100100: 	scaledValue = (corner == Corner.northwest || corner == Corner.northeast ? originCheckValue : originCenterValue); break;
						case 0b00101001: 	scaledValue = (corner == Corner.southeast || corner == Corner.northeast ? originCheckValue : originCenterValue); break;
							
						//////////////////////////////////////////////////////////////		
						// on the edge
						//////////////////////////////////////////////////////////////		

						// other corner L shape										
						case 0b00001111: 
						case 0b10000111:	scaledValue = (corner == Corner.northeast ? originNeighborValue : originCenterValue); break;
						case 0b11000011: 
						case 0b11100001:	scaledValue = (corner == Corner.southeast ? originNeighborValue : originCenterValue); break;
						case 0b11110000: 
						case 0b01111000:	scaledValue = (corner == Corner.southwest ? originNeighborValue : originCenterValue); break;
						case 0b00111100: 
						case 0b00011110:	scaledValue = (corner == Corner.northwest ? originNeighborValue : originCenterValue); break;
						// other corner + 2 adjacent edges = same double L										
						case 0b00111110:	scaledValue = (corner == Corner.northwest ? originNeighborValue : originCenterValue); break;
						case 0b10001111:	scaledValue = (corner == Corner.northeast ? originNeighborValue : originCenterValue); break;
						case 0b11100011:	scaledValue = (corner == Corner.southeast ? originNeighborValue : originCenterValue); break;
						case 0b11111000:	scaledValue = (corner == Corner.southwest ? originNeighborValue : originCenterValue); break;
						// full same edge										
						case 0b11100000:	scaledValue = (corner == Corner.southwest || corner == Corner.southeast ? originNeighborValue : originCenterValue); break;
						case 0b00111000:	scaledValue = (corner == Corner.southwest || corner == Corner.northwest ? originNeighborValue : originCenterValue); break;
						case 0b00001110:	scaledValue = (corner == Corner.northeast || corner == Corner.northwest ? originNeighborValue : originCenterValue); break;
						case 0b10000011:	scaledValue = (corner == Corner.northeast || corner == Corner.southeast ? originNeighborValue : originCenterValue); break;
						// same corner + 1 adjacent edge										
						case 0b11000000:
						case 0b00011000:	scaledValue = (corner == Corner.southwest ? originNeighborValue : originCenterValue); break;
						case 0b01100000:
						case 0b00000011:	scaledValue = (corner == Corner.southeast ? originNeighborValue : originCenterValue); break;
						case 0b00110000:
						case 0b00000110:	scaledValue = (corner == Corner.northwest ? originNeighborValue : originCenterValue); break;
						case 0b00001100:
						case 0b10000001:	scaledValue = (corner == Corner.northeast ? originNeighborValue : originCenterValue); break;
						// stair shape										
						case 0b11011000:	scaledValue = (corner == Corner.southwest ? originNeighborValue : originCenterValue); break;
						case 0b00110110:	scaledValue = (corner == Corner.northwest ? originNeighborValue : originCenterValue); break;
						case 0b10001101:	scaledValue = (corner == Corner.northeast ? originNeighborValue : originCenterValue); break;
						case 0b01100011:	scaledValue = (corner == Corner.southeast ? originNeighborValue : originCenterValue); break;
						// other corner L shape + opposite corner = Tetris zick-zack-block									
						case 0b00001101:
						case 0b10000101:	scaledValue = (corner == Corner.northeast ? originNeighborValue : originCenterValue); break;
						case 0b01000011:
						case 0b01100001:	scaledValue = (corner == Corner.southeast ? originNeighborValue : originCenterValue); break;
						case 0b11010000:
						case 0b01011000:	scaledValue = (corner == Corner.southwest ? originNeighborValue : originCenterValue); break;
						case 0b00110100:
						case 0b00010110:	scaledValue = (corner == Corner.northwest ? originNeighborValue : originCenterValue); break;
						
						//////////////////////////////////////////////////////////////		
						// everything below here will be default = center value
						//////////////////////////////////////////////////////////////	
							
						// filled same										
						case 0b11111111:	scaledValue = (corner == Corner.southwest ? originNeighborValue : originCenterValue); break;
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
						case 0b00000101: //	Center
						case 0b01000001: //	Center
						case 0b01010000: //	Center
						case 0b00010100: //	Center
						// same edge + adjacent corner + straight edge = L shape										
						case 0b11000100: //	Center
						case 0b01100100: //	Center
						case 0b00110001: //	Center
						case 0b00011001: //	Center
						case 0b01001100: //	Center
						case 0b01000110: //	Center
						case 0b00010011: //	Center
						case 0b10010001: //	Center
							
						// default
						default:
							scaledValue = originCenterValue;
						// @formatter:on
					}
				}
				else
				{
					scaledValue = originCenterValue;
				}
				scaled[y][x] = scaledValue;
			}
		}
		return scaled;
	}

	public static char[][] transform2(char[][] original, double[][] matrix, boolean smartScale)
	{
		// System.out.println("matrix = ");
		// printMatrix(matrix);
		double[][] inv = invertMatrix(matrix);
		// System.out.println("inv = ");
		// printMatrix(inv);

		int oldSizeY = original.length;
		int oldSizeX = original[0].length;

		// calculate new size
		Point2D.Double[] transformedCorners = new Point2D.Double[] { applyMatrix(matrix, new Point2D.Double(0, 0)),
				applyMatrix(matrix, new Point2D.Double(0, oldSizeY)), applyMatrix(matrix, new Point2D.Double(oldSizeX, 0)),
				applyMatrix(matrix, new Point2D.Double(oldSizeX, oldSizeY)) };
		Point2D.Double max = max(transformedCorners);
		int newSizeX = (int) Math.ceil(max.x);
		int newSizeY = (int) Math.ceil(max.y);

		char[][] scaled = new char[newSizeY][];

		Point2D.Double p0 = applyMatrix(inv, new Point2D.Double(0, 0));
		Point2D.Double p1 = applyMatrix(inv, new Point2D.Double(1, 1));
		double scaleX = p1.x - p0.x;
		double scaleY = p1.y - p0.y;

		System.out.println("scaleX=" + scaleX + ", scaleY=" + scaleY);
		
		Point2D.Double origin;
		int originX, originY;
		int mask, mod;
		Corner corner;
		Zone zone;
		char scaledValue, originCenterValue, originCheckValue;
		char originNeighborValueNearest, originNeighborValueNorth, originNeighborValueEast, originNeighborValueSouth, originNeighborValueWest;
		for(int y = 0; y < newSizeY; y++)
		{
			scaled[y] = new char[newSizeX];
			for(int x = 0; x < newSizeX; x++)
			{
				origin = applyMatrix(inv, new Point2D.Double(x, y));
				originX = (int) (origin.x);
				originY = (int) (origin.y);

				corner = getCorner(origin.x - originX + scaleX / 2, origin.y - originY + scaleY / 2);
				zone = getZone(origin.x - originX, origin.y - originY);

				originCenterValue = getValue(original, originX, originY);

				if(smartScale)
				{
					mask = getMask(original, originX, originY);
					mod = (x + y) % 2;

					originCheckValue = getCheckValue(original, originX, originY, mod, zone);
					originNeighborValueNearest = getNeighborValue(original, originX, originY, zone);
					originNeighborValueNorth = getNeighborValue(original, originX, originY, Zone.north);
					originNeighborValueEast = getNeighborValue(original, originX, originY, Zone.east);
					originNeighborValueSouth = getNeighborValue(original, originX, originY, Zone.south);
					originNeighborValueWest = getNeighborValue(original, originX, originY, Zone.west);
					
					switch(corner)
					{
						case northwest:
							if(originNeighborValueNorth == originCenterValue || originNeighborValueWest == originCenterValue)
								scaledValue = originCenterValue;
							else if((mask & 0b11000001) == 0b10000000)
								scaledValue = originCheckValue;
							else
								scaledValue = originNeighborValueNearest;
							break;
						case northeast:
							if(originNeighborValueNorth == originCenterValue || originNeighborValueEast == originCenterValue)
								scaledValue = originCenterValue;
							else if((mask & 0b01110000) == 0b00100000)
								scaledValue = originCheckValue;
							else
								scaledValue = originNeighborValueNearest;
							break;
						case southeast:
							if(originNeighborValueSouth == originCenterValue || originNeighborValueEast == originCenterValue)
								scaledValue = originCenterValue;
							else if((mask & 0b00011100) == 0b00001000)
								scaledValue = originCheckValue;
							else
								scaledValue = originNeighborValueNearest;
							break;
						case southwest:
							if(originNeighborValueSouth == originCenterValue || originNeighborValueWest == originCenterValue)
								scaledValue = originCenterValue;
							else if((mask & 0b00000111) == 0b00000010)
								scaledValue = originCheckValue;
							else
								scaledValue = originNeighborValueNearest;
							break;
						case center:
							boolean checkedNorthEast = (mask & 0b11000001) == 0b10000000;
							boolean checkedSouthEast = (mask & 0b01110000) == 0b00100000;
							boolean checkedSouthWest = (mask & 0b00011100) == 0b00001000;
							boolean checkedNorthWest = (mask & 0b00000111) == 0b00000010;
							int numberOfCheckedCorners = 0;
							if(checkedNorthEast) numberOfCheckedCorners++;
							if(checkedSouthEast) numberOfCheckedCorners++;
							if(checkedSouthWest) numberOfCheckedCorners++;
							if(checkedNorthWest) numberOfCheckedCorners++;
							
							System.out.println(originX + "," + originY + " -> mask=" + Integer.toBinaryString(mask) +  " -> checks=" + numberOfCheckedCorners);
							if(checkedNorthEast && checkedSouthWest) // opposite checked
								scaledValue = originCheckValue;
							else if(checkedSouthEast && checkedNorthWest) // opposite checked
								scaledValue = originCheckValue;
							else if(numberOfCheckedCorners <= 2 && !isRoad(originCenterValue)) // two checked and it is not road
								scaledValue = originCheckValue;
							else
								scaledValue = originCenterValue;
							break;
						default:
							scaledValue = originCenterValue;
							break;
					}
				}
				else
				{
					scaledValue = originCenterValue;
				}
				scaled[y][x] = scaledValue;
			}
		}
		return scaled;
	}

	public static void main(String[] args)
	{

	}
}
