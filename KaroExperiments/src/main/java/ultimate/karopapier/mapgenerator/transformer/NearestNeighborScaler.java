package ultimate.karopapier.mapgenerator.transformer;

import java.awt.Point;
import java.awt.geom.Point2D;

public class NearestNeighborScaler extends Scaler
{
	@Override
	public char getScaledValue(char[][] map, Point2D.Double origin, Point transformed, double scaleX, double scaleY)
	{
		return getRawValue(map, (int) origin.getX(), (int) origin.getY());
	}
}
