package ultimate.karopapier.transformer;

import java.awt.geom.Point2D;

public abstract class MapTransformer
{
	///////////////////////////
	// static
	///////////////////////////

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
		// @formatter:off
		Point2D.Double[] transformedCorners = new Point2D.Double[] {
			applyMatrix(matrix, new Point2D.Double( 0, 0 )),
			applyMatrix(matrix, new Point2D.Double( 0, mapHeight )),
			applyMatrix(matrix, new Point2D.Double( mapWidth, 0 )),
			applyMatrix(matrix, new Point2D.Double( mapWidth, mapHeight))
		};
		// @formatter:on
		Point2D.Double min = MapTransformerHelper.min(transformedCorners);
		matrix[0][2] = -min.x - (scaleX < 0 ? 1 : 0);
		matrix[1][2] = -min.y - (scaleY < 0 ? 1 : 0);
		
		return matrix;
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
		
		if(det == 0)
			throw new IllegalArgumentException("matrix cannot be inverted");
		
		// https://de.wikipedia.org/wiki/Inverse_Matrix#Explizite_Formeln
		// @formatter:off
		return new double[][] {
			{ (e*i-f*h)/det, (c*h-b*i)/det, (b*f-c*e)/det },
			{ (f*g-d*i)/det, (a*i-c*g)/det, (c*d-a*f)/det },
			{ (d*h-e*g)/det, (b*b-a*h)/det, (a*e-b*d)/det } 
		};
		// @formatter:on
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
		catch(NullPointerException | ArrayIndexOutOfBoundsException e)
		{
			MapTransformerHelper.printMap(map);
			return 'L';
		}
	}
	
	///////////////////////////
	// non-static
	///////////////////////////
	
	protected double[][] matrix;
	protected double[][] inv;
	protected double scaleX;
	protected double scaleY;

	public MapTransformer(double[][] matrix)
	{
		this.matrix = matrix;
		this.inv = invertMatrix(matrix);

		Point2D.Double p0 = inverseTransform(new Point2D.Double(0, 0));
		Point2D.Double p1 = inverseTransform(new Point2D.Double(1, 1));
		this.scaleX = p1.x - p0.x;
		this.scaleY = p1.y - p0.y;
	}
	
	public Point2D.Double transform(Point2D.Double point)
	{
		return applyMatrix(matrix, point);
	}
	
	public Point2D.Double inverseTransform(Point2D.Double point)
	{
		return applyMatrix(inv, point);
	}
	
	public abstract char getTransformedValue(char[][] original, int transformedX, int transformedY);
		
	public char[][] transform(char[][] original)
	{
		int oldSizeY = original.length;
		int oldSizeX = original[0].length;

		// calculate new size
		Point2D.Double[] transformedCorners = new Point2D.Double[] {
				applyMatrix(matrix, new Point2D.Double(0, 0)),
				applyMatrix(matrix, new Point2D.Double(0, oldSizeY)),
				applyMatrix(matrix, new Point2D.Double(oldSizeX, 0)),
				applyMatrix(matrix, new Point2D.Double(oldSizeX, oldSizeY)) };
		Point2D.Double min = MapTransformerHelper.min(transformedCorners);
//		System.out.println("min = " + min);
		Point2D.Double max = MapTransformerHelper.max(transformedCorners);
//		System.out.println("max = " + max);
		int newSizeX = (int) Math.ceil(max.x - min.x);
		int newSizeY = (int) Math.ceil(max.y - min.y);
//		System.out.println("newSize = " + newSizeX + " x " + newSizeY);

		char[][] scaled = new char[newSizeY][];

		for(int y = 0; y < newSizeY; y++)
		{
			scaled[y] = new char[newSizeX];
			for(int x = 0; x < newSizeX; x++)
			{
				scaled[y][x] = this.getTransformedValue(original, x, y);
			}
		}
		return scaled;
	}

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
	
	// @formatter:on
}
