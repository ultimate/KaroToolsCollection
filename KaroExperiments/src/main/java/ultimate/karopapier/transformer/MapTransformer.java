package ultimate.karopapier.transformer;

public class MapTransformer
{
	// @formatter:off
	public static final String TEST_CODE  = "PXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"+
											"PXXXXOOOOOOOFSOOOOOOOOOOOOOOOOOOOOOO1OOOOOOOOOOOOOOOXXXXXXXX\n"+
											"PXXOOOOOOOOOFSOOOOOOOOOOOOOOOOOOOOO1O1OOOOOOOOOOOOOOOOOXXXXX\n"+
											"PXOOOOOOOOOOFSOOOOOOOOOOOOOOOOOOOO1O1O1OOOOOOOOOOOOOOOOOOXXX\n"+
											"PXOOOOOOOOOOFSOOOOOOOOOOOOOOOOOOO1O1O1O1OOOOOOOOOOOOOOOOOOOX\n"+
											"XOOOOOOOOOOOFSOOOOOOOOOOOOOOOOOO1O1O1O1O1OOOOOOOOOOOOOOOOOOX\n"+
											"XXOOOOOOOOXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOOX\n"+
											"XOOOOOOOXXXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOOXXXXXXXOOOOOOOOX\n"+
											"XOOOOOOXXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOOOOOXXXXXXOOOOOOOXX\n"+
											"X77777XXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOOOOOOOOXXXOOOOOOOOXX\n"+
											"XOOOOOXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOXXOOOOOOO33XXX2O2O2O2XX\n"+
											"XOOOOOOXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOXXXXXOO3333OXX2222222XX\n"+
											"XOOOOOOOOXXXXXXXXXXXXXXXXXXXXOOOOOOOXXXXXXXX333OOOOOOOOOOXXX\n"+
											"XOOOOOOOOXXXXXXXXXXXXXXXXXXXXX444444XXXXXXXXXOOOOOOOOOOOOXXX\n"+
											"XXOOOOOOOOOXXXXXXXXXXXXXXXXXXXXOOOOOOXXXXXXXXOOOOOOOOOOOXXXX\n"+
											"XXXOOOOOOO666XXXXXXXXXXXXXXXXXXXOOOOOOOXXXXXXXXOOOOOOOOXXXXX\n"+
											"XXXOOOOOO66OOOOOOOOOOXXXXXXXXXXXXOOOOOOOXXXXXXXXXXXXXXXXXXXX\n"+
											"XXXOOOOOO6OOOOOOOOOOOOOOXXXXXXXXOOOOOOOOXXXXXXXXXXXXXXXXXXXX\n"+
											"XXXXXXXXX6OOOOOOOOOOOOOOOOXXXOOOOOOOOOOOXXXXXXXXXXXXXXXXXXXX\n"+
											"XXXXXXXXXXXXXXXXOOOOOOOOOO555OOOOOOOOOOXXXXXXXXXXXXXXXXXXXXX\n"+
											"XXXXXXXXXXXXXXXXXXXOOOOOOO555OOOOOOOOXXXXXXXXXXXXXXXXXXXXXXX\n"+
											"XXXXXXXXXXXXXXXXXXXXXOOOOO555OOOOOOXXXXXXXXXXXXXXXXXXXXXXXXX\n"+
											"XXXXXXXXXXXXXXXXXXXXXXXXXO555OOOXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"+
											"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"+
											"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
	
	public static final String LINE_SEPARATOR = "\n";
	// @formatter:on

	// @formatter:off
	public static double[][] createMatrix(double scale, int rotation)
	{
		// TODO
		return new double[][] {
			{ scale, 0, 0 },
			{ 0, scale, 0 },
			{ 0, 0, 1 } 
		};
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

	public static boolean isRoad(char c)
	{
		return "SOF123456789".indexOf(c) != -1;
	}

	public static boolean isCheckBoard(char[][] map, int x, int y)
	{
		if(x <= 0 || y <= 0)
			return false;
		if(x >= map[0].length - 1 || y >= map.length - 1)
			return false;

		boolean centerIsRoad = isRoad(map[y][x]);

		// @formatter:off
		return isRoad(map[y-1][x-1]) == centerIsRoad
			&& isRoad(map[y-1][x  ]) == !centerIsRoad
			&& isRoad(map[y-1][x+1]) == centerIsRoad
			&& isRoad(map[y  ][x-1]) == !centerIsRoad
			&& isRoad(map[y  ][x-0]) == centerIsRoad
			&& isRoad(map[y  ][x+1]) == !centerIsRoad
			&& isRoad(map[y+1][x-1]) == centerIsRoad
			&& isRoad(map[y+1][x-0]) == !centerIsRoad
			&& isRoad(map[y+1][x+1]) == centerIsRoad;
		// @formatter:on
	}

	// TODO partial checkboards

	public static boolean isEdgeBR(char[][] map, int x, int y)
	{
		if(x <= 0 || y <= 0)
			return false;
		if(x >= map[0].length - 1 || y >= map.length - 1)
			return false;

		// @formatter:off
		return isRoad(map[y-1][x-1]) == false
			&& isRoad(map[y-1][x  ]) == false
			&& isRoad(map[y-1][x+1]) == false
			&& isRoad(map[y  ][x-1]) == false
			&& isRoad(map[y  ][x-0]) == false
			&& isRoad(map[y  ][x+1]) == true
			&& isRoad(map[y+1][x-1]) == false
			&& isRoad(map[y+1][x-0]) == true
			&& isRoad(map[y+1][x+1]) == true;
		// @formatter:on
	}

	public static void printMatrix(double[][] matrix)
	{
		System.out.println(matrix[0][0] + "\t" + matrix[0][1] + "\t" + matrix[0][2]);
		System.out.println(matrix[1][0] + "\t" + matrix[1][1] + "\t" + matrix[1][2]);
		System.out.println(matrix[2][0] + "\t" + matrix[2][1] + "\t" + matrix[2][2]);
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
		int newSizeX = (int) (matrix[0][0] * oldSizeX + matrix[0][1] * oldSizeY);
		int newSizeY = (int) (matrix[1][0] * oldSizeX + matrix[1][1] * oldSizeY);

		char[][] scaled = new char[newSizeY][];

		double originXd, originYd;
		int originX, originY;
		double xd, yd;
		for(int y = 0; y < newSizeY; y++)
		{
			scaled[y] = new char[newSizeX];
			for(int x = 0; x < newSizeX; x++)
			{
				originXd = (inv[0][0] * x + inv[0][1] * y + inv[0][2]);
				originYd = (inv[1][0] * x + inv[1][1] * y + inv[1][2]);
				originX = (int) (originXd);
				originY = (int) (originYd);

				xd = originXd - originX;
				yd = originYd - originY;

				originX = Math.min(Math.max(0, originX), oldSizeX);
				originY = Math.min(Math.max(0, originY), oldSizeY);

				if(smartScale)
				{
					// TODO calc mask
					// switch mask
					if(isCheckBoard(original, originX, originY))
					{
						if((x + y) % 2 == (originY + originX) % 2)
							scaled[y][x] = original[originY][originX];
						else
							scaled[y][x] = original[originY - 1][originX];
					}
					else if(isEdgeBR(original, originX, originY))
					{
						if(xd + yd > 1)
							scaled[y][x] = original[originY + 1][originX + 1];
						else
							scaled[y][x] = original[originY][originX];
					}
					else
					{
						scaled[y][x] = original[originY][originX];
					}
				}
				else
				{
					scaled[y][x] = original[originY][originX];
				}
			}
		}
		return scaled;
	}
}
