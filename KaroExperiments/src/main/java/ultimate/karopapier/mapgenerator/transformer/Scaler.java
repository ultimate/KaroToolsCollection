package ultimate.karopapier.mapgenerator.transformer;

import ultimate.karopapier.mapgenerator.MapGeneratorUtil;

public abstract class Scaler
{
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
	
	public abstract char getScaledValue(char[][] map, double originX, double originY, int transformedX, int transformedY, double scaleX, double scaleY);
}
