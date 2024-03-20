package ultimate.karopapier.transformer;

import java.awt.geom.Point2D;

public class MapTransformer
{
	// @formatter:off
	public static final String TEST_CODE  = "XYXYXYXYXYXYXYXYXYOOOOOOOOOOOOOOOOOOXYXYXYXYXYXYXYXYXYOOOOOOOOOOOOOOOOOO\n"+
											"YXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOOYXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOO\n"+
											"XYXYOYXYXOXYXOXYXYOOOOXOOOOXOOOXOOOOXYXYOYXYXYXOXYXYXYOOOOXOOOOOOXOOOOOO\n"+
											"YXYOYOYXYXOXOXYXYXOOOXOXOOOOXOXOOOOOYXYOYOYXYXOOOXYXYXOOOXOXOOOOXXXOOOOO\n"+
											"XYOYOYOYXYXOXYXYXYOOXOXOXOOOOXOOOOOOXYOOOOOYXOXOXOXYXYOOXXXXXOOXOXOXOOOO\n"+
											"YXYOYOYXYXOXOXYXYXOOOXOXOOOOXOXOOOOOYXYOYOYXYXOOOXYXYXOOOXOXOOOOXXXOOOOO\n"+
											"XYXYOYXYXOXYXOXYXYOOOOXOOOOXOOOXOOOOXYXYOYXYXYXOXYXYXYOOOOXOOOOOOXOOOOOO\n"+
											"YXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOOYXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOO\n"+
											"XYXYXYXYXYXYXYXYXYOOOOOOOOOOOOOOOOOOXYXYXYXYXYXYXYXYXYOOOOOOOOOOOOOOOOOO\n"+
											"YXYXYOYXYXYXOXYXYXOOOOOXOOOOOOXOOOOOYXYXYOYXYXYXOXYXYXOOOOOXOOOOOOXOOOOO\n"+
											"XYXYOOXYXYXYOOXYXYOOOOXXOOOOOOXXOOOO\n"+
											"YXOOOOOXYXYOOOOOYXOOXXXXXOOOOXXXXXOO\n"+
											"XYXOOOOOXYOOOOOYXYOOOXXXXXOOXXXXXOOO\n"+
											"YXYXOOYXYXYXOOYXYXOOOOXXOOOOOOXXOOOO\n"+
											"XYXYOYXYXYXYXOXYXYOOOOXOOOOOOOOXOOOO\n"+
											"YXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOO\n"+
											"XYXYXYXYXYXYXYXYXYOOOOOOOOOOOOOOOOOO\n"+
											"YXYXOXYXYXYXYOYXYXOOOOXOOoOOOOOXOOOO\n"+
											"XYXOOOXYXYXYOYOYXYOOOXXXOOOOOOXOXOOO\n"+
											"YXOOYOOXYXYOYXYOXYOOXXOXXOOOOXOOOXOO\n"+
											"XYXOOOXYXYXYOYOXYXOOOXXXOOOOOOXOXOOO\n"+
											"YXYXOXYXYXYXYOXYXYOOOOXOOOOOOOOXOOOO\n"+
											"XYXYXYXYXYXYXYXYXYOOOOOOOOOOOOOOOOOO\n"+
											"YXYXYXYXYXYXYXYXYXOOOOOOOOOOOOOOOOOO";
	
	public static final String LINE_SEPARATOR = "\n";
	// @formatter:on

	// @formatter:off
	public static double[][] createMatrix(double scale, int rotation, int mapWidth, int mapHeight)
	{
		double sin = Math.sin(rotation * Math.PI / 180.0);
		double cos = Math.cos(rotation * Math.PI / 180.0);
		
		double[][] matrix = new double[][] {
			{ cos*scale, -sin*scale, 0 },
			{ sin*scale, cos*scale, 0 },
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
		char scaledValue, originCenterValue;
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

					if(originX == 10 && originY == 5)
					{
						// @formatter:off
						System.out.println("originX=" + originX + ",originY=" + originY +
								" -> mask=" + Integer.toBinaryString(mask) +
								", mod=" + mod +
								", corner=" + corner +
								", zone=" + zone +
								", fractions=" + (origin.x - originX) + "/" + (origin.y - originY));
						// @formatter:on
					}

					switch(mask)
					{
						// @formatter:off
						// other corner + independent edge										
						case 0b01101111: //	Center
						case 0b01111011: //	Center
						case 0b11011011: //	Center
						case 0b11011110: //	Center
						case 0b11110110: //	Center
						case 0b10110111: //	Center
						case 0b10111101: //	Center
						case 0b11101101: //	Center
						// other corner + opposite corner with edge (grass on street)										
						case 0b00110111: //	Center
						case 0b10011101: //	Center
						case 0b11001101: //	Center
						case 0b01100111: //	Center
						case 0b01110011: //	Center
						case 0b11011001: //	Center
						case 0b11011100: //	Center
						case 0b01110110: //	Center
						// other corner + neighbor corner with edge										
						case 0b00111101: //	Center
						case 0b10010111: //	Center
						case 0b01001111: //	Center
						case 0b11100101: //	Center
						case 0b11010011: //	Center
						case 0b01111001: //	Center
						case 0b11110100: //	Center
						case 0b01011110: //	Center
						// other corner double L shape = 2x2 same										
						case 0b00000111: //	Center
						case 0b11000001: //	Center
						case 0b01110000: //	Center
						case 0b00011100: //	Center
						// other full edge + 2 corners										
						case 0b00010101: //	Center
						case 0b01000101: //	Center
						case 0b01010001: //	Center
						case 0b01010100: //	Center
						// 2 other full edges = 2 opposite same edges										
						case 0b00010001: //	Center
						case 0b01000100: //	Center
						// other corner L shape + opposite corner										
						case 0b00001101: //	Triangle
						case 0b10000101: //	Triangle
						case 0b01000011: //	Triangle
						case 0b01100001: //	Triangle
						case 0b11010000: //	Triangle
						case 0b01011000: //	Triangle
						case 0b00110100: //	Triangle
						case 0b00010110: //	Triangle
						// other corner double L shape + opposite corner										
						case 0b00000101: //	Center
						case 0b01000001: //	Center
						case 0b01010000: //	Center
						case 0b00010100: //	Center
						// single same edge										
						case 0b01000000: //	Center
						case 0b00010000: //	Center
						case 0b00000100: //	Center
						case 0b00000001: //	Center
						// same edge + 1 independent corner										
						case 0b01001000: //	Center
						case 0b01000010: //	Center
						case 0b00010010: //	Center
						case 0b10010000: //	Center
						case 0b10000100: //	Center
						case 0b00100100: //	Center
						case 0b00001001: //	Center
						case 0b00100001: //	Center
						// same edge + 2 independent corner (Y-shape)										
						case 0b01001010: //	Center
						case 0b10010010: //	Center
						case 0b10100100: //	Center
						case 0b00101001: //	Center
						// same edge + adjacent corner + far corner										
						case 0b11001000: //	Check Triangle
						case 0b01100010: //	Check Triangle
						case 0b00110010: //	Check Triangle
						case 0b10011000: //	Check Triangle
						case 0b10001100: //	Check Triangle
						case 0b00100110: //	Check Triangle
						case 0b00100011: //	Check Triangle
						case 0b10001001: //	Check Triangle
						// same edge + adjacent corner + near corner										
						case 0b11000010: //	Check Triangle
						case 0b01101000: //	Check Triangle
						case 0b10110000: //	Check Triangle
						case 0b00011010: //	Check Triangle
						case 0b00101100: //	Check Triangle
						case 0b10000110: //	Check Triangle
						case 0b00001011: //	Check Triangle
						case 0b10100001: //	Check Triangle
						// same edge + adjacent corner + straight edge										
						case 0b11000100: //	Center
						case 0b01100100: //	Center
						case 0b00110001: //	Center
						case 0b00011001: //	Center
						case 0b01001100: //	Center
						case 0b01000110: //	Center
						case 0b00010011: //	Center
						case 0b10010001: //	Center
						// same T shape										
						case 0b11100100: //	Center
						case 0b00111001: //	Center
						case 0b01001110: //	Center
						case 0b10010011: //	Center
						// 2x2 same + 1 edge										
						case 0b01110100: //	Center
						case 0b01110001: //	Center
						case 0b00011101: //	Center
						case 0b01011100: //	Center
						case 0b01000111: //	Center
						case 0b00010111: //	Center
						case 0b11010001: //	Center
						case 0b11000101: //	Center
						// 2x2 same + opposite corner										
						case 0b01110010: //	Center
						case 0b10011100: //	Center
						case 0b00100111: //	Center
						case 0b11001001: //	Center
						// C shape										
						case 0b01101100: //	Center
						case 0b00011011: //	Center
						case 0b11000110: //	Center
						case 0b10110001: //	Center
						// T+L shape										
						case 0b11101100: //	Center
						case 0b11100110: //	Center
						case 0b00111011: //	Center
						case 0b10111001: //	Center
						case 0b11001110: //	Center
						case 0b01101110: //	Center
						case 0b10110011: //	Center
						case 0b10011011: //	Center
						// 3 same edges + corners on the 4th side										
						case 0b11010110: //	Center
						case 0b10110101: //	Center
						case 0b01101101: //	Center
						case 0b01011011: //	Center
						// 2 neighbored same edges + opposite corner (diagonal Y shape)										
						case 0b01010010: //	Check Triangle
						case 0b10010100: //	Check Triangle
						case 0b00100101: //	Check Triangle
						case 0b01001001: //	Check Triangle
						// inner T shape + 1 adjacent corner										
						case 0b01010110: //	Center
						case 0b11010100: //	Center
						case 0b10010101: //	Center
						case 0b00110101: //	Center
						case 0b01100101: //	Center
						case 0b01001101: //	Center
						case 0b01011001: //	Center
						case 0b01010011: //	Center
						// same corner + same corner with edge + edge										
						case 0b10110100: //	Center
						case 0b01011010: //	Center
						case 0b00101101: //	Center
						case 0b10010110: //	Center
						case 0b01001011: //	Center
						case 0b10100101: //	Center
						case 0b11010010: //	Center
						case 0b01101001: //	Center
						// 2 opposite same edges with opposite same corners										
						case 0b11001100: //	Center
						case 0b01100110: //	Center
						case 0b00110011: //	Center
						case 0b10011001: //	Center
						// 3 same corners + edges for the outer corners										
						case 0b10101101: //	Center
						case 0b01101011: //	Center
						case 0b11011010: //	Center
						case 0b10110110: //	Center
							
							scaledValue = originCenterValue;
							break;
						// TODO everything above	
						
						///////////////////////////////
						// different check patterns		
						///////////////////////////////		

						// x shape										
						case 0b10101010: //	Check	
							scaledValue = getCheckValue(original, originX, originY, mod, zone);
							break;
							
						// 3 same corners										
						case 0b10101000: //	Check
							if(corner == Corner.southwest)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}
							scaledValue = getCheckValue(original, originX, originY, mod, zone);
							break;
						case 0b00101010: //	Check
							if(corner == Corner.northwest)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}
							scaledValue = getCheckValue(original, originX, originY, mod, zone);
							break;
						case 0b10001010: //	Check
							if(corner == Corner.northeast)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}
							scaledValue = getCheckValue(original, originX, originY, mod, zone);
							break;
						case 0b10100010: //	Check
							if(corner == Corner.southeast)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = getCheckValue(original, originX, originY, mod, zone);
							break;
							
						// 2 opposite same corners										
						case 0b10001000: //	Check Triangle
							if(corner == Corner.northeast || corner == Corner.southwest)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = getCheckValue(original, originX, originY, mod, zone);
							break;
						case 0b00100010: //	Check Triangle
							if(corner == Corner.northwest || corner == Corner.southeast)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = getCheckValue(original, originX, originY, mod, zone);
							break;
							
						// 2 adjacent same corners										
						case 0b10100000: //	Check Triangle (was Check before)
							if(corner == Corner.southwest || corner == Corner.southeast)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = getCheckValue(original, originX, originY, mod, zone);
							break;
						case 0b00101000: //	Check Triangle (was Check before)
							if(corner == Corner.southwest || corner == Corner.northwest)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = getCheckValue(original, originX, originY, mod, zone);
							break;
						case 0b00001010: //	Check Triangle (was Check before)
							if(corner == Corner.northwest || corner == Corner.northeast)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = getCheckValue(original, originX, originY, mod, zone);
							break;
						case 0b10000010: //	Check Triangle (was Check before)
							if(corner == Corner.southeast || corner == Corner.northeast)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = getCheckValue(original, originX, originY, mod, zone);
							break;
							
						// full same edge, rest checkered										
						case 0b11101010: //	Check Triangle (was Check before)
							if(corner == Corner.southeast || corner == Corner.southwest)
							{
								scaledValue = getCheckValue(original, originX, originY, mod, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b10111010: //	Check Triangle (was Check before)
							if(corner == Corner.northwest || corner == Corner.southwest)
							{
								scaledValue = getCheckValue(original, originX, originY, mod, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b10101110: //	Check Triangle (was Check before)
							if(corner == Corner.northwest || corner == Corner.northeast)
							{
								scaledValue = getCheckValue(original, originX, originY, mod, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b10101011: //	Check Triangle (was Check before)
							if(corner == Corner.southeast || corner == Corner.northeast)
							{
								scaledValue = getCheckValue(original, originX, originY, mod, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
							
						// 2 other edges next to each other										
						case 0b10101111: //	Check (was Center before)
							if(corner == Corner.northeast)
							{
								scaledValue = getCheckValue(original, originX, originY, mod, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b11101011: //	Check (was Center before)
							if(corner == Corner.southeast)
							{
								scaledValue = getCheckValue(original, originX, originY, mod, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b11111010: //	Check (was Center before)
							if(corner == Corner.southwest)
							{
								scaledValue = getCheckValue(original, originX, originY, mod, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b10111110: //	Check (was Center before)
							if(corner == Corner.northwest)
							{
								scaledValue = getCheckValue(original, originX, originY, mod, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
							
						// single same corner										
						case 0b10000000: //	Check
							if(corner == Corner.northwest || corner == Corner.center)
							{
								scaledValue = getCheckValue(original, originX, originY, mod, zone);
								break;
							}	
							scaledValue = getNeighborValue(scaled, originX, originY, zone);
							break;
						case 0b00100000: //	Check
							if(corner == Corner.northeast || corner == Corner.center)
							{
								scaledValue = getCheckValue(original, originX, originY, mod, zone);
								break;
							}	
							scaledValue = getNeighborValue(scaled, originX, originY, zone);
							break;
						case 0b00001000: //	Check
							if(corner == Corner.southeast || corner == Corner.center)
							{
								scaledValue = getCheckValue(original, originX, originY, mod, zone);
								break;
							}	
							scaledValue = getNeighborValue(scaled, originX, originY, zone);
							break;
						case 0b00000010: //	Check
							if(corner == Corner.southwest || corner == Corner.center)
							{
								scaledValue = getCheckValue(original, originX, originY, mod, zone);
								break;
							}	
							scaledValue = getNeighborValue(scaled, originX, originY, zone);
							break;
							
						// 2 other edges next to each other + 1 corner	TODO									
						case 0b00101111: //	Center
						case 0b10100111: //	Center
						case 0b11001011: //	Center
						case 0b11101001: //	Center
						case 0b11110010: //	Center
						case 0b01111010: //	Center
						case 0b10111100: //	Center
						case 0b10011110: //	Center
						// same edge + adjacent corner + 2 corners										
						case 0b11001010: //	Check Triangle
						case 0b01101010: //	Check Triangle
						case 0b10110010: //	Check Triangle
						case 0b10011010: //	Check Triangle
						case 0b10101100: //	Check Triangle
						case 0b10100110: //	Check Triangle
						case 0b00101011: //	Check Triangle
						case 0b10101001: //	Check Triangle

						//////////////////////////////////////////////////////////////		
						// on the edge
						//////////////////////////////////////////////////////////////		

						// other corner L shape										
						case 0b00001111: //	Small Triangle
						case 0b10000111: //	Small Triangle
							if(corner == Corner.northeast)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b11000011: //	Small Triangle
						case 0b11100001: //	Small Triangle
							if(corner == Corner.southeast)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b11110000: //	Small Triangle
						case 0b01111000: //	Small Triangle
							if(corner == Corner.southwest)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b00111100: //	Small Triangle
						case 0b00011110: //	Small Triangle
							if(corner == Corner.northwest)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;

						// other corner + 2 adjacent edges = same double L										
						case 0b00111110: //	Small Triangle
							if(corner == Corner.northwest)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b10001111: //	Small Triangle
							if(corner == Corner.northeast)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b11100011: //	Small Triangle
							if(corner == Corner.southeast)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b11111000: //	Small Triangle
							if(corner == Corner.southwest)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
							
						// full same edge										
						case 0b11100000: //	Center
							if(corner == Corner.southwest || corner == Corner.southeast)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b00111000: //	Center
							if(corner == Corner.southwest || corner == Corner.northwest)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b00001110: //	Center
							if(corner == Corner.northeast || corner == Corner.northwest)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b10000011: //	Center
							if(corner == Corner.northeast || corner == Corner.southeast)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
							
						// same corner + 1 adjacent edge										
						case 0b11000000: //	Triangle
						case 0b00011000: //	Triangle
							if(corner == Corner.southwest)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b01100000: //	Triangle
						case 0b00000011: //	Triangle
							if(corner == Corner.southeast)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b00110000: //	Triangle
						case 0b00000110: //	Triangle
							if(corner == Corner.northwest)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b00001100: //	Triangle
						case 0b10000001: //	Triangle
							if(corner == Corner.northeast)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
							
						// stair shape										
						case 0b11011000: //	Triangle
							if(corner == Corner.southwest)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b00110110: //	Triangle
							if(corner == Corner.northwest)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b10001101: //	Triangle
							if(corner == Corner.northeast)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b01100011: //	Triangle
							if(corner == Corner.southeast)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
							
						// same full edge + corner										
						case 0b11101000: //	Triangle
						case 0b10111000: //	Triangle
							if(corner == Corner.southwest)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b11100010: //	Triangle
						case 0b10100011: //	Triangle
							if(corner == Corner.southeast)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b10001110: //	Triangle
						case 0b10001011: //	Triangle
							if(corner == Corner.northeast)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
						case 0b00101110: //	Triangle
						case 0b00111010: //	Triangle
							if(corner == Corner.northwest)
							{
								scaledValue = getNeighborValue(original, originX, originY, zone);
								break;
							}	
							scaledValue = originCenterValue;
							break;
							
						//////////////////////////////////////////////////////////////		
						// everything below here will be default = center value
						//////////////////////////////////////////////////////////////	
							
						// filled same										
						case 0b11111111:
						// surrounded other										
						case 0b00000000:
						// single other corner										
						case 0b01111111: //	Center
						case 0b11011111: //	Center
						case 0b11110111: //	Center
						case 0b11111101: //	Center
						// single other edge										
						case 0b10111111: //	Center
						case 0b11101111: //	Center
						case 0b11111011: //	Center
						case 0b11111110: //	Center
						// 2 opposite other edges										
						case 0b10111011: //	Center
						case 0b11101110: //	Center
						// other corner + 1 adjacent edge										
						case 0b00111111: //	Center
						case 0b10011111: //	Center
						case 0b11001111: //	Center
						case 0b11100111: //	Center
						case 0b11110011: //	Center
						case 0b11111001: //	Center
						case 0b11111100: //	Center
						case 0b01111110: //	Center
						// 2 independent other corners										
						case 0b01011111: //	Center
						case 0b11010111: //	Center
						case 0b11110101: //	Center
						case 0b01111101: //	Center
						case 0b01110111: //	Center
						case 0b11011101: //	Center
						// 3 independent other corners										
						case 0b01010111: //	Center
						case 0b01011101: //	Center
						case 0b01110101: //	Center
						case 0b11010101: //	Center
						// + shape										
						case 0b01010101: //	Center
						// full other edge = 2 corners + edge in between										
						case 0b00011111: //	Center
						case 0b11000111: //	Center
						case 0b11110001: //	Center
						case 0b01111100: //	Center
							
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

	public static void main(String[] args)
	{

	}
}
