package ultimate.karopapier.mapgenerator.transformer;

public class NearestNeighborScaler extends Scaler
{
	@Override
	public char getScaledValue(char[][] map, double originX, double originY, int transformedX, int transformedY, double scaleX, double scaleY)
	{
		return getRawValue(map, (int) originX, (int) originY);
	}
}
