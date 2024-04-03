package ultimate.karopapier.mapgenerator.transformer;

import java.awt.geom.Point2D;

public class NearestNeighborScaler extends MapTransformer
{
	public NearestNeighborScaler(double[][] matrix)
	{
		super(matrix);
	}

	@Override
	public char getTransformedValue(char[][] original, int transformedX, int transformedY)
	{
		Point2D.Double origin = applyMatrix(inv, new Point2D.Double(transformedX, transformedY));
		int originX = (int) (origin.x);
		int originY = (int) (origin.y);

		return getValue(original, originX, originY);
	}
}
