package ultimate.karopapier.mapgenerator.transformer;

import java.awt.geom.Point2D;

import ultimate.karopapier.mapgenerator.MapGeneratorUtil;

public class MapTransformer
{
	///////////////////////////
	// static
	///////////////////////////
	public static final double[][] IDENTITY = new double[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };

	public static double[][] createMatrix(int rotation, double scaleX, double scaleY, int mapWidth, int mapHeight)
	{
		double[][] matrix = IDENTITY;
		// equivalent to
		// double[][] matrix = new double[][] {
		// { cos*scaleX, -sin*scaleX, 0 },
		// { sin*scaleY, cos*scaleY, 0 },
		// { 0, 0, 1 }
		// };
		matrix = MapTransformer.rotate(matrix, rotation);
		matrix = MapTransformer.scale(matrix, scaleX, scaleY);

		// compensate offset
		// @formatter:off
		Point2D.Double[] transformedCorners = new Point2D.Double[] {
			applyMatrix(matrix, new Point2D.Double( 0, 0 )),
			applyMatrix(matrix, new Point2D.Double( 0, mapHeight )),
			applyMatrix(matrix, new Point2D.Double( mapWidth, 0 )),
			applyMatrix(matrix, new Point2D.Double( mapWidth, mapHeight))
		};
		// @formatter:on
		Point2D.Double min = MapGeneratorUtil.min(transformedCorners);
		double translateX = -min.x - (scaleX < 0 ? 1 : 0);
		double translateY = -min.y - (scaleY < 0 ? 1 : 0);
		matrix = MapTransformer.translate(matrix, translateX, translateY);

		return matrix;
	}

	public static Point2D.Double applyMatrix(double[][] matrix, Point2D.Double point)
	{
		return new Point2D.Double(matrix[0][0] * point.x + matrix[0][1] * point.y + matrix[0][2],
				matrix[1][0] * point.x + matrix[1][1] * point.y + matrix[1][2]);
	}

	public static double[][] copy(double[][] org)
	{
		double[][] copy = new double[org.length][];
		for(int i1 = 0; i1 < 3; i1++)
		{
			copy[i1] = new double[org[i1].length];
			for(int i2 = 0; i2 < 3; i2++)
			{
				copy[i1][i2] = org[i1][i2];
			}
		}
		return copy;
	}

	public static boolean isValid(double[][] m)
	{
		int firstRowLength = m[0].length;
		for(int r = 1; r < m.length; r++)
		{
			if(m[r] == null || m[r].length != firstRowLength)
				return false;
		}
		return true;
	}

	public static double[][] multiply(double[][] matrixA, double[][] matrixB)
	{
		if(!isValid(matrixA))
			throw new IllegalArgumentException("matrixA is not a valid matrix, all rows must be of equal length");
		if(!isValid(matrixB))
			throw new IllegalArgumentException("m2 is not a valid matrix, all rows must be of equal length");

		// check dimensions
		if(matrixA[0].length != matrixB.length)
			throw new IllegalArgumentException("matrix dimensions do not match, must be: if A = m x n, then B = n x p");

		double[][] result = new double[matrixA.length][matrixB[0].length];
		for(int r = 0; r < result.length; r++)
		{
			for(int c = 0; c < result[r].length; c++)
			{
				result[r][c] = 0;
				for(int i = 0; i < result[r].length; i++)
					result[r][c] += matrixA[r][i] * matrixB[i][c];
			}
		}
		return result;
	}

	public static double[][] scale(double[][] matrix, double scaleX, double scaleY)
	{
		// @formatter:off
		double[][] transform = new double[][] {
			{ scaleX, 0, 0 },
			{ 0, scaleY, 0 },
			{ 0, 0, 1 } 
		};
		// @formatter:on
		return multiply(transform, matrix);
	}

	public static double[][] rotate(double[][] matrix, double rotationInDegrees)
	{
		double sin = Math.sin(rotationInDegrees * Math.PI / 180.0);
		double cos = Math.cos(rotationInDegrees * Math.PI / 180.0);
		// @formatter:off
		double[][] transform = new double[][] {
			{ cos, -sin, 0 },
			{ sin, cos, 0 },
			{ 0, 0, 1 } 
		};
		// @formatter:on
		return multiply(transform, matrix);
	}

	public static double[][] translate(double[][] matrix, double translateX, double translateY)
	{
		// @formatter:off
		double[][] transform = new double[][] {
			{ 1, 0, translateX },
			{ 0, 1, translateY },
			{ 0, 0, 1 } 
		};
		// @formatter:on
		return multiply(transform, matrix);
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
		double det = a * e * i + b * f * g + c * d * h - g * e * c - h * f * a - i * d * b;

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

	///////////////////////////
	// non-static
	///////////////////////////

	private double[][]	matrix;
	private double[][]	inv;
	private double		scaleX;
	private double		scaleY;
	private Scaler		scaler;

	public MapTransformer(Scaler scaler)
	{
		this.scaler = scaler;
		this.setMatrix(IDENTITY);
	}

	public MapTransformer(double[][] matrix, Scaler scaler)
	{
		this.scaler = scaler;
		this.setMatrix(matrix);
	}

	public void setMatrix(double[][] matrix)
	{
		if(matrix.length != 3)
			throw new IllegalArgumentException("matrix must be of size 3x3");
		this.matrix = new double[3][3];
		for(int i1 = 0; i1 < 3; i1++)
		{
			if(matrix[i1].length != 3)
				throw new IllegalArgumentException("matrix must be of size 3x3");
			for(int i2 = 0; i2 < 3; i2++)
			{
				this.matrix[i1][i2] = matrix[i1][i2];
			}
		}
		this.inv = invertMatrix(this.matrix);

		Point2D.Double p0 = inverseTransform(new Point2D.Double(0, 0));
		Point2D.Double p1 = inverseTransform(new Point2D.Double(1, 1));
		this.scaleX = 1 / (p1.x - p0.x);
		this.scaleY = 1 / (p1.y - p0.y);
	}

	public void scale(double scaleX, double scaleY)
	{
		this.matrix = scale(this.matrix, scaleX, scaleY);
	}

	public void rotate(double rotationInDegrees)
	{
		this.matrix = rotate(this.matrix, rotationInDegrees);
	}

	public void translate(double translateX, double translateY)
	{
		this.matrix = translate(this.matrix, translateX, translateY);
	}

	public void compensateMapSize(int dimX, int dimY)
	{
		// @formatter:off
		Point2D.Double[] transformedCorners = new Point2D.Double[] {
			applyMatrix(this.matrix, new Point2D.Double( 0, 0 )),
			applyMatrix(this.matrix, new Point2D.Double( 0, dimY )),
			applyMatrix(this.matrix, new Point2D.Double( dimX, 0 )),
			applyMatrix(this.matrix, new Point2D.Double( dimX, dimY))
		};
		// @formatter:on
		Point2D.Double min = MapGeneratorUtil.min(transformedCorners);
		double offsetX = -min.x - (this.scaleX < 0 ? 1 : 0);
		double offsetY = -min.y - (this.scaleY < 0 ? 1 : 0);

		this.translate(offsetX, offsetY);
	}

	public Point2D.Double transform(Point2D.Double point)
	{
		return applyMatrix(matrix, point);
	}

	public Point2D.Double inverseTransform(Point2D.Double point)
	{
		return applyMatrix(inv, point);
	}

	public char[][] transform(char[][] original)
	{
		this.compensateMapSize(original.length, original[0].length);

		int oldSizeY = original.length;
		int oldSizeX = original[0].length;

		// calculate new size
		Point2D.Double[] transformedCorners = new Point2D.Double[] { transform(new Point2D.Double(0, 0)), transform(new Point2D.Double(0, oldSizeY)),
				transform(new Point2D.Double(oldSizeX, 0)), transform(new Point2D.Double(oldSizeX, oldSizeY)) };
		Point2D.Double min = MapGeneratorUtil.min(transformedCorners);
		// System.out.println("min = " + min);
		Point2D.Double max = MapGeneratorUtil.max(transformedCorners);
		// System.out.println("max = " + max);
		int newSizeX = (int) Math.ceil(max.x - min.x);
		int newSizeY = (int) Math.ceil(max.y - min.y);
		// System.out.println("newSize = " + newSizeX + " x " + newSizeY);

		char[][] scaled = new char[newSizeY][];

		for(int y = 0; y < newSizeY; y++)
		{
			scaled[y] = new char[newSizeX];
			for(int x = 0; x < newSizeX; x++)
			{
				Point2D.Double origin = applyMatrix(this.inv, new Point2D.Double(x, y));
				scaled[y][x] = this.scaler.getScaledValue(original, origin.x, origin.y, x, y, scaleX, scaleY);
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
