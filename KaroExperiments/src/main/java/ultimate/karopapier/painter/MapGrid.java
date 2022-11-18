package ultimate.karopapier.painter;

import java.util.LinkedList;
import java.util.List;

public class MapGrid
{
	public final int			width;
	public final int			height;
	public final MapField[][]	grid;

	public boolean				debug	= false;

	public final List<MapField>	startFields;
	public final List<MapField>	finishFields;

	public MapGrid(String mapCode)
	{
		String[] rows = mapCode.split("\n");
		this.grid = new MapField[rows[0].length()][];
		for(int c = 0; c < rows[0].length(); c++)
			this.grid[c] = new MapField[rows.length];

		this.width = rows[0].length();
		this.height = rows.length;
		
		this.startFields = new LinkedList<>();
		this.finishFields = new LinkedList<>();

		int r = 0;
		for(String row : rows)
		{
			for(int c = 0; c < row.length(); c++)
			{
				this.grid[c][r] = new MapField(c, r, row.charAt(c));
				if(MapLogic.isStart(row.charAt(c)))
					startFields.add(this.grid[c][r]);
				if(MapLogic.isFinish(row.charAt(c)))
					finishFields.add(this.grid[c][r]);
			}
			r++;
		}

		MapLogic.calculateReachableFields(this);
	}

	public void resetVisited()
	{
		for(int x = 0; x < this.width; x++)
		{
			for(int y = 0; y < this.height; y++)
			{
				this.grid[x][y].visited = false;
			}
		}
	}
}
