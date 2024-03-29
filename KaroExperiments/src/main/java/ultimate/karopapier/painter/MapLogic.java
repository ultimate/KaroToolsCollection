package ultimate.karopapier.painter;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class MapLogic
{
	/**
	 * Logger-Instance
	 */
	protected transient static final Logger logger = LogManager.getLogger(MapLogic.class);

	private MapLogic()
	{

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

	public static boolean isStart(char c)
	{
		return c == 'S';
	}

	public static boolean isFinish(char c)
	{
		return c == 'F';
	}

	public static boolean isRoadRect(MapGrid grid, Rectangle rect)
	{
		if(rect == null)
			rect = new Rectangle(0, 0, grid.width, grid.height);
		return countRoadFields(grid, rect) == rect.height * rect.width;
	}

	public static int countRoadFields(MapGrid grid, Rectangle rect)
	{
		if(rect == null)
			rect = new Rectangle(0, 0, grid.width, grid.height);

		int roadFields = 0;
		for(int x = rect.x; x < rect.x + rect.width; x++)
		{
			for(int y = rect.y; y < rect.y + rect.height; y++)
			{
				if(grid.grid[x][y].road && grid.grid[x][y].reachable)
					roadFields++;
			}
		}
		return roadFields;
	}

	public static int countVisitedFields(MapGrid grid, Rectangle rect)
	{
		if(rect == null)
			rect = new Rectangle(0, 0, grid.width, grid.height);

		int visitedFields = 0;
		for(int x = rect.x; x < rect.x + rect.width; x++)
		{
			for(int y = rect.y; y < rect.y + rect.height; y++)
			{
				if(grid.grid[x][y].road && grid.grid[x][y].visited)
					visitedFields++;
			}
		}
		return visitedFields;
	}

	public static boolean isValid(MapGrid grid, MapVector vector)
	{
		// check the bounds of the map
		if(!isInRect(vector, new Rectangle(0, 0, grid.width, grid.height)))
			return false;

		LineIterator iter = new LineIterator(new Point(vector.start.x, vector.start.y), new Point(vector.end.x, vector.end.y));

		Point p;
		MapField f;
		while(iter.hasNext())
		{
			p = iter.next();
			f = grid.grid[p.x][p.y];
			if(!f.road)
			{
				// logger.debug("touching gras");
				return false;
			}
		}
		return true;
	}

	public static boolean isInRect(MapVector vector, Rectangle rect)
	{
		if(vector.start.x < rect.x || vector.start.x >= rect.x + rect.width)
			return false;
		if(vector.start.y < rect.y || vector.start.y >= rect.y + rect.height)
			return false;
		if(vector.end.x < rect.x || vector.end.x >= rect.x + rect.width)
			return false;
		if(vector.end.y < rect.y || vector.end.y >= rect.y + rect.height)
			return false;
		return true;
	}

	public static void calculateReachableFields(MapGrid grid)
	{
		// find starts and finishes
		LinkedList<MapField> startFieldQueue = new LinkedList<>();
		LinkedList<MapField> finishFieldQueue = new LinkedList<>();

		for(int x = 0; x < grid.width; x++)
		{
			for(int y = 0; y < grid.height; y++)
			{
				if(isFinish(grid.grid[x][y].symbol))
				{
					grid.grid[x][y].distanceToFinish_straight = 0;
					grid.grid[x][y].distanceToFinish_diagonal = 0;
					finishFieldQueue.add(grid.grid[x][y]);
				}
				if(isStart(grid.grid[x][y].symbol))
				{
					grid.grid[x][y].reachable = true;
					startFieldQueue.add(grid.grid[x][y]);
				}
			}
		}

		MapField current, candidate;

		// mark fields which are reachable from a start field
		while(startFieldQueue.size() > 0)
		{
			current = startFieldQueue.poll();
			for(int dx = -1; dx <= 1; dx++)
			{
				for(int dy = -1; dy <= 1; dy++)
				{
					if(current.x + dx < 0 || current.x + dx >= grid.width)
						continue; // out of bounds
					if(current.y + dy < 0 || current.y + dy >= grid.height)
						continue; // out of bounds

					candidate = grid.grid[current.x + dx][current.y + dy];

					if(!candidate.road)
						continue;
					if(candidate.reachable)
						continue; // already handled

					candidate.reachable = true;
					startFieldQueue.add(candidate);
				}
			}
		}
		// calculate distance to finish
		while(finishFieldQueue.size() > 0)
		{
			current = finishFieldQueue.poll();
			int candidateDistance_s, candidateDistance_d;
			boolean smallerDistanceFound;
			for(int dx = -1; dx <= 1; dx++)
			{
				for(int dy = -1; dy <= 1; dy++)
				{
					if(current.x + dx < 0 || current.x + dx >= grid.width)
						continue; // out of bounds
					if(current.y + dy < 0 || current.y + dy >= grid.height)
						continue; // out of bounds

					candidate = grid.grid[current.x + dx][current.y + dy];

					if(!candidate.road)
						continue;
					if(!candidate.reachable)
						continue;
					if(current.symbol == 'F' && candidate.symbol == 'S')
						continue; // S next to F

					smallerDistanceFound = false;

					candidateDistance_s = current.distanceToFinish_straight + Math.abs(dx) + Math.abs(dy);
					if(candidate.distanceToFinish_straight >= 0)
					{
						// already handled
						if(candidate.distanceToFinish_straight > candidateDistance_s)
							candidate.distanceToFinish_straight = candidateDistance_s;
					}
					else
					{
						candidate.distanceToFinish_straight = candidateDistance_s;
						smallerDistanceFound = true;
					}

					candidateDistance_d = current.distanceToFinish_diagonal + 1;
					if(candidate.distanceToFinish_diagonal >= 0)
					{
						// already handled
						if(candidate.distanceToFinish_diagonal > candidateDistance_d)
							candidate.distanceToFinish_diagonal = candidateDistance_d;
					}
					else
					{
						candidate.distanceToFinish_diagonal = candidateDistance_d;
						smallerDistanceFound = true;
					}

					if(smallerDistanceFound)
						finishFieldQueue.add(candidate);
				}
			}
		}
	}

	public static LinkedList<MapVector> calculatePossibles(MapGrid grid, MapVector currentVector)
	{
		LinkedList<MapVector> possibles = new LinkedList<>();

		int newdx, newdy;
		for(int ddx = -1; ddx <= 1; ddx++)
		{
			newdx = currentVector.dx + ddx;
			for(int ddy = -1; ddy <= 1; ddy++)
			{
				newdy = currentVector.dy + ddy;
				if(currentVector.end.x + newdx < 0 || currentVector.end.x + newdx >= grid.width)
					continue;
				if(currentVector.end.y + newdy < 0 || currentVector.end.y + newdy >= grid.height)
					continue;
				if(newdx == 0 && newdy == 0)
					continue;
				possibles.add(new MapVector(grid, currentVector.end, newdx, newdy));
			}

		}
		// remove all that are not valid
		possibles.removeIf(v -> {
			return !isValid(grid, v);
		});

		if(possibles.size() == 0)
			possibles.add(new MapVector(grid, currentVector.end, 0, 0)); // ZZZ=0

		return possibles;
	}
}
