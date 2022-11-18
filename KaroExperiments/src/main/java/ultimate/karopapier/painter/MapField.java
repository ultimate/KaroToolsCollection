package ultimate.karopapier.painter;

public class MapField
{
	public final int		x;
	public final int		y;

	public final char		symbol;
	public final boolean	road;

	public boolean			reachable;
	public boolean			visited;
	public int				distanceToFinish;

	public MapField(int x, int y, char symbol)
	{
		this.x = x;
		this.y = y;
		this.symbol = symbol;
		this.road = MapLogic.isRoad(this.symbol);
		this.reachable = false;
		this.visited = false;
		this.distanceToFinish = -1;
	}
}
