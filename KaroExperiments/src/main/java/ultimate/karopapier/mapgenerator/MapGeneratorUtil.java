package ultimate.karopapier.mapgenerator;

import java.awt.geom.Point2D;

public abstract class MapGeneratorUtil
{
	public static final String LINE_SEPARATOR = "\n";
	
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
		Point2D.Double max = new Point2D.Double(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		for(Point2D.Double p: points)
		{
			if(p.x > max.x)
				max.x = p.x;
			if(p.y > max.y)
				max.y = p.y;
		}
		return max;
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

	public static void replaceChar(char[][] map, char oldChar, char newChar)
	{
		for(char[] row : map)
		{
			if(row == null)
				continue;
			for(int c = 0; c < row.length; c++)
			{
				if(row[c] == oldChar)
					row[c] = newChar;
			}
		}
	}

	public static void swapChars(char[][] map, char char1, char char2)
	{
		for(char[] row : map)
		{
			if(row == null)
				continue;
			for(int c = 0; c < row.length; c++)
			{
				if(row[c] == char1)
					row[c] = char2;
				else if(row[c] == char2)
					row[c] = char1;
			}
		}
	}

	public static boolean isRoad(char c)
	{
		return "SOF123456789".indexOf(c) != -1;
	}
	
	public static boolean rowContainsRoad(char[] row)
	{
		for(char c : row)
		{
			if(isRoad(c))
				return true;
		}
		return false;
	}
	
	public static boolean colContainsRoad(char[][] map, int col)
	{
		if(col < 0)
			return false;
		for(char[] row : map)
		{
			if(col >= row.length)
				continue;
			if(isRoad(row[col]))
				return true;
		}
		return false;
	}

	public static char[][] trim(char[][] map, int preserveCols, int preserveRows)
	{
		if(preserveCols < 1)
			throw new IllegalArgumentException("preserveCols must be greater than or equal to 1");
		if(preserveRows < 1)
			throw new IllegalArgumentException("preserveRows must be greater than or equal to 1");
		
		int rowOffsetTop = -preserveRows;
		for(int r = 0; r < map.length; r++)
		{
			if(rowContainsRoad(map[r]))
				break;
			rowOffsetTop++;
		}
		if(rowOffsetTop < 0)
			rowOffsetTop = 0;
		
		int rowOffsetBottom = -preserveRows;
		for(int r = map.length - 1; r >= 0; r--)
		{
			if(rowContainsRoad(map[r]))
				break;
			rowOffsetBottom++;
		}
		if(rowOffsetBottom < 0)
			rowOffsetBottom = 0;
		
		int colOffsetLeft = -preserveCols;
		for(int c = 0; c < map[0].length; c++)
		{
			if(colContainsRoad(map, c))
				break;
			colOffsetLeft++;
		}
		if(colOffsetLeft < 0)
			colOffsetLeft = 0;
		
		int colOffsetRight = -preserveCols;
		for(int c = map[0].length - 1; c >= 0; c--)
		{
			if(colContainsRoad(map, c))
				break;
			colOffsetRight++;
		}
		if(colOffsetRight < 0)
			colOffsetRight = 0;
		
		// System.out.println("trimming: top = " + rowOffsetTop + ", bottom = " + rowOffsetBottom);
		// System.out.println("trimming: left = " + colOffsetLeft + ", right  = " + colOffsetRight);
		
		char[][] trimmed = new char[map.length - rowOffsetTop - rowOffsetBottom][];
		for(int oldR = rowOffsetTop, newR = 0; newR < trimmed.length; oldR++, newR++)
		{
			trimmed[newR] = new char[map[0].length - colOffsetLeft - colOffsetRight];
			for(int oldC = colOffsetLeft, newC = 0; newC < trimmed[newR].length; oldC++, newC++)
			{
				trimmed[newR][newC] = map[oldR][oldC];
			}
		}
		
//		printMap(trimmed);
		return trimmed;
	}
}
