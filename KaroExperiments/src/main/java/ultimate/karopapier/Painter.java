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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

public class Painter
{
	public static final char	dr						= '┏';
	public static final char	dl						= '┓';
	public static final char	ur						= '┗';
	public static final char	ul						= '┛';
	public static final char	u						= '╹';
	public static final char	d						= '╻';
	public static final char	r						= '╺';
	public static final char	l						= '╸';
	public static final char	v						= '┃';
	public static final char	h						= '━';

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

		Map map = new Map(mapString);
		frame.getContentPane().add(map);
		frame.requestFocus();

		Section section;

		map.reset();
		section = new Section(0, 0, 1, 1);
		System.out.println(section.contains(new Point(0, 0)));
		processSection(map, section, null, null);
		// printPath(grid, map.path);

		System.out.println("------------------------------------------");

//		map.reset();
//		section = new Section(13, 1, 5, 5);
//		processSection(map, section, new Vector(map.grid, map.grid[13][1], 0, 0), map.grid[17][5]);
//		// printPath(grid, map.path);
//
//		System.out.println("------------------------------------------");
//
//		map.reset();
//		section = new Section(13, 1, 36, 5);
//		processSection(map, section, new Vector(map.grid, map.grid[13][1], 0, 0), map.grid[48][5]);
//		// printPath(grid, map.path);
//
//		System.out.println("------------------------------------------");

		map.reset();
		section = new Section(13, 0, 47, 25);
		processSection(map, section, new Vector(map.grid, map.grid[13][1], 0, 0), map.grid[13][18]);
		// printPath(grid, map.path);

		System.out.println("------------------------------------------");

		map.reset();
		section = new Section(27, 6, 11, 6);
		processSection(map, section, new Vector(map.grid, map.grid[37][7], 0, 0), map.grid[35][11]);
		// printPath(grid, path);
	}

	public static void processSection(Map map, Section section, Vector start, Field target)
	{


		map.start = start.start;
		map.target = target;
		map.path = new LinkedList<>();

		map.path.add(start);
		map.grid[start.start.x][start.start.y].visited = true;

		Vector next;
		int step = 0;
		int targetSize = section.size(map.grid);
		Field highestDistanceFieldNotVisited;
		int isolatedFields;
		boolean isolated;
		long time = System.currentTimeMillis();
		while(map.path.size() > 0 && (map.path.getLast().end.x != target.x || map.path.getLast().end.y != target.y || !section.isFilled(map.grid)))
		{
			next = map.path.getLast().next(map);
			// System.out.println((next != null ? next.x + "|" + next.y + "->" + next.dx + "|" + next.dy : "-"));
			if(next == null)
			{
				Vector v = map.path.removeLast();
				v.end.visited = false;
			}
			else
			{
				highestDistanceFieldNotVisited = null;
				for(int x = 0; x < map.width; x++)
				{
					for(int y = 0; y < map.height; y++)
					{
						if(highestDistanceFieldNotVisited == null || !map.grid[x][y].visited && map.grid[x][y].distanceToFinish > highestDistanceFieldNotVisited.distanceToFinish)
							highestDistanceFieldNotVisited = map.grid[x][y];
					}
				}

				isolatedFields = 0;
				for(int x = 0; x < map.width; x++)
				{
					for(int y = 0; y < map.height; y++)
					{
						isolated = true;
						for(int dx = -isolationDistance; dx <= isolationDistance; dx++)
						{
							for(int dy = -isolationDistance; dy <= isolationDistance; dy++)
							{
								if(x + dx < 0 || x + dx >= map.width)
									continue; // out of bounds
								if(y + dy < 0 || y + dy >= map.height)
									continue; // out of bounds
								
								if(next.end.x == x + dx && next.end.y == y + dy) // this is the field that we are heading to
									continue;
								if(map.grid[x + dx][y + dy].reachable && !map.grid[x + dx][y + dy].visited)
								{
									isolated = false;
									break;
								}
							}
						}
					}
				}

				System.out.println(highestDistanceFieldNotVisited.distanceToFinish + " -> " + (next != null ? next.end.distanceToFinish : "-"));

				if(next.end.x == target.x && next.end.y == target.y && map.path.size() < targetSize - 1)
				{
					// ignore target when we have other karos left
				}
				else if(next.end.distanceToFinish < highestDistanceFieldNotVisited.distanceToFinish - allowedDistanceOffset)
				{
					// ignore vector if we have gaps far behing
				}
				else if(isolatedFields > 0)
				{
					// ignore vector if we have isolated a field
				}
				else if(next.isValid(map.grid, section) && !next.end.visited)
				{
					map.path.add(next);
					next.end.visited = true;
				}
				else if(next.dx == 0 && next.dy == 0)
				{
					// ZZZ = 0
					map.path.add(next);
				}
			}
			step++;

			// if(step % 1000 == 0)
			if(System.currentTimeMillis() > time + 100)
			{
				map.repaint();
				time = System.currentTimeMillis();
			}
		}
	}
}
