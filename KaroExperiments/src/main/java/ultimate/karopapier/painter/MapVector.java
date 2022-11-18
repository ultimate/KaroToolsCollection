package ultimate.karopapier.painter;

import java.util.LinkedList;

public class MapVector
{
	public final MapGrid			grid;

	public final MapField			start;
	public final MapField			end;
	public final int				dx;
	public final int				dy;

	public LinkedList<MapVector>	possibles;

	public MapVector(MapGrid grid, MapField start, int dx, int dy)
	{
		this.grid = grid;
		this.start = start;
		this.end = grid.grid[start.x + dx][start.y + dy];
		this.dx = dx;
		this.dy = dy;
		
		this.possibles = null;
	}

	@Override
	public String toString()
	{
		return "MapVector [(" + start.x + "|" + start.y + ")-(" + dx + "|" + dy + ")->(" + end.x + "|" + end.y + ")]";
	}
}
