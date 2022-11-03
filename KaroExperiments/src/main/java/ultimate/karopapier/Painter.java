package ultimate.karopapier;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

public class Painter
{
	public static final char	dr			= '┏';
	public static final char	dl			= '┓';
	public static final char	ur			= '┗';
	public static final char	ul			= '┛';
	public static final char	u			= '╹';
	public static final char	d			= '╻';
	public static final char	r			= '╺';
	public static final char	l			= '╸';
	public static final char	v			= '┃';
	public static final char	h			= '━';

	public static final int		gridSize	= 10;

	public static void main(String[] args)
	{
		//@formatter:off
		String mapString =    "PXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"
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

		JFrame frame = new JFrame();
		frame.setSize(1600, 900);
		frame.setVisible(true);
		frame.setLayout(new FlowLayout());
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});

		String[] rows = mapString.split("\n");
		Field[][] grid = new Field[rows[0].length()][];
		for(int c = 0; c < rows[0].length(); c++)
			grid[c] = new Field[rows.length];

		int r = 0;
		for(String row : rows)
		{
			for(int c = 0; c < row.length(); c++)
				grid[c][r] = new Field(c, r, row.charAt(c));
			r++;
		}

		Map map = new Map(grid);
		frame.getContentPane().add(map);
		frame.requestFocus();

		Section section;

		// printPath(grid, Arrays.asList(grid[0][0], grid[1][0], grid[1][1], grid[2][1], grid[2][0], grid[3][0], grid[4][0], grid[5][0], grid[5][1], grid[5][2], grid[5][3], grid[5][4], grid[5][5],
		// grid[4][5], grid[3][5], grid[3][4], grid[2][4],
		// grid[2][5], grid[1][5], grid[1][4], grid[0][4]));

		section = new Section(0, 0, 1, 1);
		System.out.println(section.contains(new Point(0, 0)));
		processSection(map, section, null, null);
		// printPath(grid, map.path);

		System.out.println("------------------------------------------");

		section = new Section(13, 1, 36, 5);
		processSection(map, section, new Vector(13, 1, 0, 0), grid[48][5]);
		// printPath(grid, map.path);

		System.out.println("------------------------------------------");

		section = new Section(27, 6, 11, 6);
		processSection(map, section, new Vector(37, 7, 0, 0), grid[35][11]);
		// printPath(grid, path);
	}

	public static void processSection(Map map, Section section, Vector start, Field target)
	{
		System.out.println("x=[" + section.getMinX() + "," + section.getMaxX() + "[");
		System.out.println("y=[" + section.getMinY() + "," + section.getMaxY() + "[");

		System.out.println(isRoadRect(map.grid, section));

		if(start == null || target == null)
			return;

		// start and target must be within the section
		if(!section.contains(start))
			throw new IllegalArgumentException("start must be inside the section");
		if(!section.contains(target))
			throw new IllegalArgumentException("target must be inside the section");
		// start and target must be roads
		if(!map.grid[start.x][start.y].road)
			throw new IllegalArgumentException("start must be road: " + map.grid[start.x][start.y].symbol);
		if(!target.road)
			throw new IllegalArgumentException("target must be road: " + target.symbol);
		// start and target must be on the edge of the section
		if(!(start.x == section.getMinX() || start.x == section.getMaxX() - 1 || start.y == section.getMinY() || start.y == section.getMaxY() - 1))
			throw new IllegalArgumentException("start must be on the edge of the section");
		if(!(target.x == section.getMinX() || target.x == section.getMaxX() - 1 || target.y == section.getMinY() || target.y == section.getMaxY() - 1))
			throw new IllegalArgumentException("target must be on the edge of the section");

		if(start.x == target.x && start.y == target.y)
			throw new IllegalArgumentException("start and target must be different");

		map.start = map.grid[start.x][start.y];
		map.target = target;
		map.path = new LinkedList<>();

		map.path.add(start);
		map.grid[start.x][start.y].visited = true;

		Vector next;
		int step = 0;
		while(map.path.size() > 0 && (map.path.getLast().x + map.path.getLast().dx != target.x || map.path.getLast().y + map.path.getLast().dy != target.y || !section.isFilled(map.grid)))
		{
			next = map.path.getLast().next(map.grid);
			System.out.println((next != null ? next.x + "|" + next.y + "->" + next.dx + "|" + next.dy : "-"));
			if(next == null)
			{
				Vector v = map.path.removeLast();
				map.grid[v.x + v.dx][v.y + v.dy].visited = false;
			}
			else if(next.isValid(map.grid, section) && !map.grid[next.x + next.dx][next.y + next.dy].visited)
			{
				map.path.add(next);
				map.grid[next.x + next.dx][next.y + next.dy].visited = true;
			}
			else if(next.dx == 0 && next.dy == 0)
			{
				// ZZZ = 0
				map.path.add(next);
			}
			step++;

			// if(step % 1000 == 0)
			map.repaint();
			try
			{
				Thread.sleep(10);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	protected static List<Field> processRect(Field[][] grid, Section section, Field /* vector */ start, Field target)
	{
		int dx = target.x - start.x;
		int dy = target.y - start.y;

		if(dx == 0)
		{
			// if(dy)
		}
		else if(dy == 0)
		{

		}
		else
		{

		}
		return null;
	}

	public static class Map extends Canvas
	{
		private Field[][]			grid;
		private LinkedList<Vector>	path;
		private Field				start;
		private Field				target;

		public Map(Field[][] grid)
		{
			super();
			this.grid = grid;
		}

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(grid.length * gridSize, grid[0].length * gridSize);
		}

		@Override
		public void paint(Graphics g)
		{
			super.paint(g);
			g.setColor(new Color(0, 255, 0));
			g.fillRect(0, 0, grid.length * gridSize, grid[0].length * gridSize);

			g.setColor(new Color(127, 127, 127));
			for(int x = 0; x < grid.length; x++)
			{
				for(int y = 0; y < grid[x].length; y++)
				{
					if(grid[x][y].road)
						g.fillRect(x * gridSize, y * gridSize, gridSize - 1, gridSize - 1);
				}
			}

			if(path != null)
			{
				g.setColor(new Color(0, 0, 255));
				for(Vector v : path)
				{
					g.drawLine(v.x * gridSize + gridSize / 2, v.y * gridSize + gridSize / 2, (v.x + v.dx) * gridSize + gridSize / 2, (v.y + v.dy) * gridSize + gridSize / 2);
				}
			}
			if(start != null)
			{
				g.setColor(new Color(0, 255, 0));
				g.fillRect(start.x * gridSize + gridSize / 2 - 1, start.y * gridSize + gridSize / 2 - 1, 3, 3);
			}
			if(target != null)
			{
				g.setColor(new Color(255, 0, 0));
				g.fillRect(target.x * gridSize + gridSize / 2 - 2, target.y * gridSize + gridSize / 2 - 2, 3, 3);
			}
		}
	}

	public static class Vector extends Point
	{
		private int					dx;
		private int					dy;

		private LinkedList<Vector>	successors;

		public Vector(int x, int y, int dx, int dy)
		{
			super(x, y);
			this.dx = dx;
			this.dy = dy;
		}

		public Vector next(Field[][] grid)
		{
			if(this.successors == null)
			{
				this.successors = new LinkedList<>();
				this.successors.add(new Vector(x + dx, y + dy, dx - 1, dy - 1));
				this.successors.add(new Vector(x + dx, y + dy, dx - 1, dy + 0));
				this.successors.add(new Vector(x + dx, y + dy, dx - 1, dy + 1));
				this.successors.add(new Vector(x + dx, y + dy, dx + 0, dy - 1));
				this.successors.add(new Vector(x + dx, y + dy, dx + 0, dy + 0));
				this.successors.add(new Vector(x + dx, y + dy, dx + 0, dy + 1));
				this.successors.add(new Vector(x + dx, y + dy, dx + 1, dy - 1));
				this.successors.add(new Vector(x + dx, y + dy, dx + 1, dy + 0));
				this.successors.add(new Vector(x + dx, y + dy, dx + 1, dy + 1));
				// remove all that are 0
				this.successors.removeIf(v -> {
					return v.dx == 0 && v.dy == 0;
				});
				// remove all that are not road
				this.successors.removeIf(v -> {
					return !v.isValid(grid);
				});
				// TODO --> keep all?
				// remove all that are longer than 1/1
				this.successors.removeIf(v -> {
					return Math.abs(v.dx) > 1 || Math.abs(v.dy) > 1;
				});
				// Collections.shuffle((List<?>) this.successors);
				if(this.successors.size() == 0)
					this.successors.add(new Vector(x + dx, y + dy, 0, 0)); // ZZZ=0
			}
			return this.successors.poll();
		}

		public boolean isValid(Field[][] grid)
		{
			return isValid(grid, new Section(0, 0, grid.length, grid[0].length));
		}

		public boolean isValid(Field[][] grid, Section section)
		{
			if(this.x < section.x || this.x >= section.x + section.width)
				return false;
			if(this.y < section.y || this.y >= section.y + section.height)
				return false;
			if(this.x + this.dx < section.x || this.x + this.dx >= section.x + section.width)
				return false;
			if(this.y + this.dy < section.y || this.y + this.dy >= section.y + section.height)
				return false;
			List<Field> fields = new LinkedList<>();
			// fields.add(grid[this.x][this.y]); // start is already visited
			fields.add(grid[this.x + this.dx][this.y + this.dy]); // end
			// TODO add intermediate points
			for(Field f : fields)
			{
				if(!f.road)
				{
					// System.out.println("no road: " + f.symbol);
					return false;
				}
			}
			return true;
		}
	}

	public static class Field extends Point
	{
		private boolean	reachable;
		private boolean	visited;
		private char	symbol;
		private boolean	road;

		public Field(int x, int y, char symbol)
		{
			super(x, y);
			this.symbol = symbol;
			this.reachable = true;
			this.visited = false;
			this.road = isRoad(this.symbol);
		}
	}

	public static class Section extends Rectangle
	{
		public Section(int x, int y, int w, int h)
		{
			super(x, y, w, h);
		}

		public boolean isFilled(Field[][] grid)
		{
			int filled = 0;
			int all = 0;
			for(int x = this.x; x < this.x + this.width; x++)
			{
				for(int y = this.y; y < this.y + this.height; y++)
				{
					if(grid[x][y].road && grid[x][y].reachable)
					{
						all++;
						if(grid[x][y].visited)
							filled++;
					}
				}
			}
			System.out.println("filled: " + filled + " of " + all);
			return filled == all;
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
