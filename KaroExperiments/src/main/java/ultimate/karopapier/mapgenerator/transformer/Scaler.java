package ultimate.karopapier.mapgenerator.transformer;

import java.awt.Point;
import java.awt.geom.Point2D;

import ultimate.karopapier.mapgenerator.MapGeneratorUtil;

/**
 * This class is the base class for different scaling algorithms used in the map transformer
 */
public abstract class Scaler
{
    /**
     * Convenience method to get the value from a map.
     * It will check for the bounds of the map and return the matching edge value if coordinates are outside.
     */
	public static char getRawValue(char[][] map, int x, int y)
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
			MapGeneratorUtil.printMap(map);
			return 'L';
		}
	}

    /**
     * Get the scaled value for a given coordinate based on this Scaler's algorithm.
     * Note 1: while transformed coordinates are integers (because they must represent a valid karo in the transformed map),
     * origin coordinates are float (since because of scaling intermediate values can occur)
     * Note 2: not all scalers might need all variables - the know range of required variables is passed here based on NearestNeighborScaler and UltimateScaler
     */
	public abstract char getScaledValue(char[][] map, Point2D.Double origin, Point transformed, double scaleX, double scaleY);
}
