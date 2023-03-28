package ultimate.karopapier.painter;

import java.util.LinkedList;
import java.util.Objects;

public class MapVector
{
	public final MapGrid			grid;

	public final MapField			start;
	public final MapField			end;
	public final int				dx;
	public final int				dy;

	public LinkedList<MapVector>	possibles;
	public LinkedList<MapVector>	origins;

	public MapVector(MapGrid grid, MapField start, int dx, int dy)
	{
		this.grid = grid;
		this.start = start;
		this.end = grid.grid[start.x + dx][start.y + dy];
		this.dx = dx;
		this.dy = dy;
		
		this.possibles = null;
		this.origins = null;
	}

	@Override
	public String toString()
	{
		return "MapVector [(" + start.x + "|" + start.y + ")-(" + dx + "|" + dy + ")->(" + end.x + "|" + end.y + ")]";
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(dx, dy, end, grid, possibles, start);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		MapVector other = (MapVector) obj;
		return dx == other.dx && dy == other.dy && Objects.equals(end, other.end) && Objects.equals(grid, other.grid) && Objects.equals(start, other.start);
	}
}
