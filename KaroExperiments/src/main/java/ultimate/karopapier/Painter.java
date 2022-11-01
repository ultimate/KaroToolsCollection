package ultimate.karopapier;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Painter
{
	public static final char	dr	= '┏';
	public static final char	dl	= '┓';
	public static final char	ur	= '┗';
	public static final char	ul	= '┛';
	public static final char	u	= '╹';
	public static final char	d	= '╻';
	public static final char	r	= '╺';
	public static final char	l	= '╸';
	public static final char	v	= '┃';
	public static final char	h	= '━';

	public static void main(String[] args)
	{
		//@formatter:off
		String map =  "PXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"
					+ "PXXXXOOOOOOOFSOOOOOOOOOOOOOOOOOOOOOO1OOOOOOOOOOOOOOOXXXXXXXX\n"
					+ "PXXOOOOOOOOOFSOOOOOOOOOOOOOOOOOOOOO1O1OOOOOOOOOOOOOOOOOXXXXX\n"
					+ "PXOOOOOOOOOOFSOOOOOOOOOOOOOOOOOOOO1O1O1OOOOOOOOOOOOOOOOOOXXX\n"
					+ "PXOOOOOOOOOOFSOOOOOOOOOOOOOOOOOOO1O1O1O1OOOOOOOOOOOOOOOOOOOX\n"
					+ "XOOOOOOOOOOOFSOOOOOOOOOOOOOOOOOO1O1O1O1O1OOOOOOOOOOOOOOOOOOX\n"
					+ "XXOOOOOOOOXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOOX\n"
					+ "XOOOOOOOXXXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOOXXXXXXXOOOOOOOOX\n"
					+ "XOOOOOOXXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOOOOOXXXXXXOOOOOOOXX\n"
					+ "X77777XXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOOOOOOOOXXXOOOOOOOOXX\n"
					+ "XOOOOOXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOXXOOOOOOO33XXX2O2O2O2XX\n"
					+ "XOOOOOOXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOXXXXXOO3333OXX2222222XX\n"
					+ "XOOOOOOOOXXXXXXXXXXXXXXXXXXXXOOOOOOOXXXXXXXX333OOOOOOOOOOXXX\n"
					+ "XOOOOOOOOXXXXXXXXXXXXXXXXXXXXX444444XXXXXXXXXOOOOOOOOOOOOXXX\n"
					+ "XXOOOOOOOOOXXXXXXXXXXXXXXXXXXXXOOOOOOXXXXXXXXOOOOOOOOOOOXXXX\n"
					+ "XXXOOOOOOO666XXXXXXXXXXXXXXXXXXXOOOOOOOXXXXXXXXOOOOOOOOXXXXX\n"
					+ "XXXOOOOOO66OOOOOOOOOOXXXXXXXXXXXXOOOOOOOXXXXXXXXXXXXXXXXXXXX\n"
					+ "XXXOOOOOO6OOOOOOOOOOOOOOXXXXXXXXOOOOOOOOXXXXXXXXXXXXXXXXXXXX\n"
					+ "XXXXXXXXX6OOOOOOOOOOOOOOOOXXXOOOOOOOOOOOXXXXXXXXXXXXXXXXXXXX\n"
					+ "XXXXXXXXXXXXXXXXOOOOOOOOOO555OOOOOOOOOOXXXXXXXXXXXXXXXXXXXXX\n"
					+ "XXXXXXXXXXXXXXXXXXXOOOOOOO555OOOOOOOOXXXXXXXXXXXXXXXXXXXXXXX\n"
					+ "XXXXXXXXXXXXXXXXXXXXXOOOOO555OOOOOOXXXXXXXXXXXXXXXXXXXXXXXXX\n"
					+ "XXXXXXXXXXXXXXXXXXXXXXXXXO555OOOXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"
					+ "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"
					+ "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
		//@formatter:on

		String[] rows = map.split("\n");
		Field[][] grid = new Field[rows[0].length()][];
		for(int c = 0; c < rows[0].length(); c++)
			grid[c] = new Field[rows.length];

		int r = 0;
		for(String row : rows)
		{
			for(int c = 0; c < row.length(); c++)
				grid[c][r] = new Field(c, r, isRoad(row.charAt(c)));
			r++;
		}
		List<Field> path;
		Section section;
		
		printPath(grid, Arrays.asList(grid[0][0], grid[1][0], grid[1][1], grid[2][1], grid[2][0], grid[3][0], grid[4][0], grid[5][0], grid[5][1], grid[5][2], grid[5][3], grid[5][4], grid[5][5], grid[4][5], grid[3][5], grid[3][4], grid[2][4], grid[2][5], grid[1][5], grid[1][4], grid[0][4]));

		section = new Section(0, 0, 1, 1);
		path = processSection(grid, section, null, null);
		printPath(grid, path);

		section = new Section(13, 1, 36, 5);
		path = processSection(grid, section, grid[13][1], grid[0][16]);
		printPath(grid, path);

		section = new Section(37, 1, 10, 7);
		path = processSection(grid, section, null, null);
		printPath(grid, path);
	}

	public static List<Field> processSection(Field[][] grid, Section section, Field /* vector */ start, Field target)
	{
		System.out.println("minX=" + section.getMinX() + ", maxX=" + section.getMaxX());
		System.out.println("minY=" + section.getMinY() + ", maxY=" + section.getMaxY());

		System.out.println(isRoadRect(grid, section));

		if(start == null)
			return null;

		List<Field> path = new LinkedList<>();
		return path;
	}

	public static void printPath(Field[][] grid, List<Field> path)
	{
		if(path == null)
			return;

		resetGrid(grid);

		Field preprevious = null;
		Field previous = null;
		for(Field current : path)
		{
			if(previous != null)
			{
				if(preprevious != null)
				{
					previous.symbol = '?';
					if(previous.x == current.x)
					{
						if(previous.y < current.y)
						{
							if(preprevious.x == previous.x)
							{
								if(preprevious.y < previous.y)
									previous.symbol = v;
								else
									previous.symbol = u;
							}
							else if(preprevious.y == previous.y)
							{
								if(preprevious.x < previous.x)
									previous.symbol = dl;
								else
									previous.symbol = dr;
							}
						}
						else
						{
							if(preprevious.x == previous.x)
							{
								if(preprevious.y < previous.y)
									previous.symbol = d;
								else
									previous.symbol = v;
							}
							else if(preprevious.y == previous.y)
							{
								if(preprevious.x < previous.x)
									previous.symbol = ul;
								else
									previous.symbol = ur;
							}
						}
					}
					else if(previous.y == current.y)
					{
						if(previous.x < current.x)
						{
							if(preprevious.y == previous.y)
							{
								if(preprevious.x < previous.x)
									previous.symbol = h;
								else
									previous.symbol = l;
							}
							else if(preprevious.x == previous.x)
							{
								if(preprevious.y < previous.y)
									previous.symbol = ur;
								else
									previous.symbol = dr;
							}
						}
						else
						{
							if(preprevious.y == previous.y)
							{
								if(preprevious.x < previous.x)
									previous.symbol = r;
								else
									previous.symbol = h;
							}
							else if(preprevious.x == previous.x)
							{
								if(preprevious.y < previous.y)
									previous.symbol = ul;
								else
									previous.symbol = dl;
							}
						}
					}
				}
				else
				{
					previous.symbol = '?';
					if(previous.x == current.x)
					{
						if(previous.y < current.y)
							previous.symbol = u;
						else
							previous.symbol = d;
					}
					else if(previous.y == current.y)
					{
						if(previous.x < current.x)
							previous.symbol = r;
						else
							previous.symbol = l;
					}
				}
			}
			preprevious = previous;
			previous = current;
		}

		StringBuilder sb = new StringBuilder();
		for(int y = 0; y < grid[y].length; y++)
		{
			for(int x = 0; x < grid.length; x++)
			{
				sb.append(grid[x][y].symbol);
			}
			sb.append("\n");
		}
		System.out.println(sb);
	}

	public static void resetGrid(Field[][] grid)
	{
		for(Field[] col : grid)
		{
			for(Field cell : col)
			{
				cell.resetSymbol();
			}
		}
	}

	public static class Field extends Point
	{
		private boolean	road;
		private boolean	visited;
		private char	symbol;

		public Field(int x, int y, boolean road)
		{
			super(x, y);
			this.road = road;
		}

		public void resetSymbol()
		{
			if(this.road)
				symbol = 'O';
			else
				symbol = 'X';
		}
	}

	public static class Section extends Rectangle
	{
		public Section(int x, int y, int w, int h)
		{
			super(x, y, w, h);
		}
	}

	public static boolean isRoad(char c)
	{
		switch(c)
		{
			case 'S':
			case 'O':
			case 'F':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				return true;
			default:
				return false;
		}
	}

	public static boolean isRoadRect(Field[][] grid, Section section)
	{
		for(int x = (int) section.getMinX(); x < section.getMaxX(); x++)
		{
			for(int y = (int) section.getMinY(); y < section.getMaxY(); y++)
			{
				// System.out.println(x + "|" + y);
				if(!grid[x][y].road)
					return false;
			}
		}
		return true;
	}
}
