package ultimate.karopapier.mapgenerator.transformer;

import java.awt.Point;
import java.awt.geom.Point2D;

import ultimate.karopapier.mapgenerator.MapGeneratorUtil;

/**
 * This class contains the basic matrix math operations required to transform coordinates.
 * It can be used to transform maps with a transformation matrix (e.g. scale, rotate, etc.).
 */
public class MapTransformer
{
    /**
     * identity matrix
     */
	static final double[][] IDENTITY = new double[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };

    /**
     * convenience method to create a matrix that rotates and scales
     */
	static double[][] createMatrix(int rotation, double scaleX, double scaleY)
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

		return matrix;
	}

    /**
     * apply a matrix to transform a point (= matrix multiplication)
     */
	static Point2D.Double applyMatrix(double[][] matrix, Point2D point)
	{
		return new Point2D.Double(matrix[0][0] * point.getX() + matrix[0][1] * point.getY() + matrix[0][2],
				matrix[1][0] * point.getX() + matrix[1][1] * point.getY() + matrix[1][2]);
	}
	static double[][] copy(double[][] org)
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

    /**
     * check if a matrix is valid (= all rows must be of equal length)
     */
	static boolean isValid(double[][] m)
	{
		int firstRowLength = m[0].length;
		for(int r = 1; r < m.length; r++)
		{
			if(m[r] == null || m[r].length != firstRowLength)
				return false;
		}
		return true;
	}

    /**
     * multiply two matrices and return a new matrix representing the product
     */
	static double[][] multiply(double[][] matrixA, double[][] matrixB)
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

    /**
     * multiply the input matrix with a scale matrix and return a new matrix representing the product
     */
	static double[][] scale(double[][] matrix, double scaleX, double scaleY)
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

    /**
     * multiply the input matrix with a rotation matrix and return a new matrix representing the product
     */
	static double[][] rotate(double[][] matrix, double rotationInDegrees)
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

    /**
     * multiply the input matrix with a translation matrix and return a new matrix representing the product
     */
	static double[][] translate(double[][] matrix, double translateX, double translateY)
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

    /**
     * calculate the inverted matrix for a given matrix
     */
	static double[][] invert(double[][] matrix)
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
		double det = (a * e * i) + (b * f * g) + (c * d * h) - (g * e * c) - (h * f * a) - (i * d * b);

		if(det == 0)
			throw new IllegalArgumentException("matrix cannot be inverted");

		// https://de.wikipedia.org/wiki/Inverse_Matrix#Explizite_Formeln
		// @formatter:off
		return new double[][] {
			{ (e*i-f*h)/det, (c*h-b*i)/det, (b*f-c*e)/det },
			{ (f*g-d*i)/det, (a*i-c*g)/det, (c*d-a*f)/det },
			{ (d*h-e*g)/det, (b*g-a*h)/det, (a*e-b*d)/det } 
		};
		// @formatter:on
	}

	public static char[][] transform(char[][] original, double[][] matrix, Scaler scaler)
	{
		if(!isValid(matrix))
			throw new IllegalArgumentException("matrixA is not a valid matrix, all rows must be of equal length");
		if(matrix.length != 3 || matrix[0].length != 3)
			throw new IllegalArgumentException("matrix must be of size 3x3");

		// calculate scaling by comparing distance of (0|0) and (1|1)
		double[][] invTmp = invert(matrix);
		Point2D.Double p0 = applyMatrix(invTmp, new Point2D.Double(0, 0));
		Point2D.Double p1 = applyMatrix(invTmp, new Point2D.Double(1, 1));
		double scaleX = 1 / (p1.x - p0.x);
		double scaleY = 1 / (p1.y - p0.y);
		
		// determine bounds of scaled map to compensate map size
		int oldSizeY = original.length;
		int oldSizeX = original[0].length;
		// @formatter:off
		Point2D.Double[] transformedCorners = new Point2D.Double[] {
			applyMatrix(matrix, new Point2D.Double( 0, 0 )),
			applyMatrix(matrix, new Point2D.Double( 0, oldSizeY )),
			applyMatrix(matrix, new Point2D.Double( oldSizeX, 0 )),
			applyMatrix(matrix, new Point2D.Double( oldSizeX, oldSizeY))
		};
		// @formatter:on
		Point2D.Double min = MapGeneratorUtil.min(transformedCorners);
		Point2D.Double max = MapGeneratorUtil.max(transformedCorners);
		// calculate new size
		int newSizeX = (int) Math.ceil(max.x - min.x);
		int newSizeY = (int) Math.ceil(max.y - min.y);
		
		// add compensation to matrix
		// note: by using a temp matrix here, we are also thread safe
		double[][] mat = translate(matrix, -min.x - (scaleX < 0 ? 1 : 0), -min.y - (scaleY < 0 ? 1 : 0));

		// now calculate the invert
		double[][] inv = invert(mat);
		
		// perform scaling
		char[][] scaled = new char[newSizeY][];
		Point2D.Double origin;
		Point transformed;
		for(int y = 0; y < newSizeY; y++)
		{
			scaled[y] = new char[newSizeX];
			for(int x = 0; x < newSizeX; x++)
			{
				transformed = new Point(x, y);
				origin = applyMatrix(inv, transformed);
				scaled[y][x] = scaler.getScaledValue(original, origin, transformed, scaleX, scaleY);
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
