package ultimate.karopapier.painter;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PathFinder
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger	logger					= LogManager.getLogger(getClass());

	private MapGrid						grid;
	private Rectangle					section;
	private MapVector					start;
	private LinkedList<MapVector>		path;

	private int							step;

	private int							fieldsToVisit;
	private int							fieldsVisited;

	private MapField					breakPoint;

	public static final int				ALLOWED_DISTANCE_OFFSET	= 5;
	public static final int				ISOLATION_TRESHOLD		= 1;

	public PathFinder(MapGrid grid, Rectangle section, MapVector start)
	{
		if(grid == null)
			throw new IllegalArgumentException("grid must not be null!");
		if(start == null)
			throw new IllegalArgumentException("start must not be null!");

		this.grid = grid;
		this.section = (section != null ? section : new Rectangle(0, 0, grid.width, grid.height));
		this.start = start;

		logger.debug("section: \tx=[" + this.section.getMinX() + "," + this.section.getMaxX() + "[, \ty=[" + this.section.getMinY() + "," + this.section.getMaxY() + "[");
		logger.debug("road fields: " + MapLogic.countRoadFields(this.grid, this.section));

		// start and target must be within the section
		if(!this.section.contains(new Point(this.start.end.x, this.start.end.y)))
			throw new IllegalArgumentException("start must be inside the section");
		// if(!section.contains(target))
		// throw new IllegalArgumentException("target must be inside the section");

		// start and target must be roads
		if(!this.start.end.road)
			throw new IllegalArgumentException("start must be road: " + this.start.end.symbol);
		// if(!target.road)
		// throw new IllegalArgumentException("target must be road: " + target.symbol);

		// start and target must be on the edge of the section
		// if(!(start.end.x == section.getMinX() || start.end.x == section.getMaxX() - 1 || start.end.y == section.getMinY() || start.end.y == section.getMaxY() - 1))
		// throw new IllegalArgumentException("start must be on the edge of the section");
		// if(!(target.x == section.getMinX() || target.x == section.getMaxX() - 1 || target.y == section.getMinY() || target.y == section.getMaxY() - 1))
		// throw new IllegalArgumentException("target must be on the edge of the section");

		// if(start.end.x == target.x && start.end.y == target.y)
		// throw new IllegalArgumentException("start and target must be different");

		this.path = new LinkedList<>();
		this.path.add(this.start);
		this.start.end.visited = true;
		this.step = 0;

		this.fieldsToVisit = MapLogic.countRoadFields(grid, section);
		this.fieldsVisited = 1;
	}

	public MapGrid getGrid()
	{
		return grid;
	}

	public Rectangle getSection()
	{
		return section;
	}

	public MapVector getStart()
	{
		return start;
	}

	public LinkedList<MapVector> getPath()
	{
		return path;
	}

	public int getStep()
	{
		return step;
	}

	public int getFieldsToVisit()
	{
		return fieldsToVisit;
	}

	public int getFieldsVisited()
	{
		return fieldsVisited;
	}

	public boolean isFinished()
	{
		if(this.path.size() == 0)
			return true; // no path left
		if(MapLogic.isFinish(this.path.getLast().end.symbol))
			return true;
		if(MapLogic.countVisitedFields(grid, section) == MapLogic.countRoadFields(grid, section))
			return true;
		return false;
	}

	public MapField getBreakPoint()
	{
		return breakPoint;
	}

	public void setBreakPoint(int x, int y)
	{
		this.breakPoint = this.grid.grid[x][y];
	}

	public int step()
	{
		if(this.breakPoint != null && this.path.getLast().end == this.breakPoint)
		{
			logger.debug("break point reached");
		}

		MapVector next = getNext(this.path.getLast());

		if(next == null)
		{
			MapVector v = this.path.removeLast();
			v.end.visited = false;
			if(v.dx != 0 && v.dy != 0) // ZZZ = 0
				this.fieldsVisited--;
		}
		else if(MapLogic.isFinish(next.end.symbol) && this.fieldsToVisit < this.fieldsVisited)
		{
			// ignore finish when we have other karos left
			logger.debug("ignoring finish");
		}
		else if(next.end.visited && (next.dx != 0 || next.dy != 0))
		{
			// ignore vectors that end on a visited field
			logger.debug("ignoring visited");
		}
		else if(!MapLogic.isInRect(next, this.section))
		{
			// ignore vectors that leave the section
			logger.debug("ignoring out of rect");
		}
		else if(fieldsLeftBehind(next))
		{
			// ignore vectors that leave gaps
			logger.debug("ignore vector if we have gaps far behind");
		}
		else if(fieldsIsolated(next))
		{
			// ignore vectors that leave gaps
			logger.debug("ignore vector if we have isolated fields");
		}
		else
		{
			this.path.add(next);
			next.end.visited = true;
			if(next.dx != 0 && next.dy != 0) // ZZZ = 0
				this.fieldsVisited++;
		}
		return ++step;
	}

	protected MapVector getNext(MapVector currentVector)
	{
		if(currentVector.possibles == null)
		{
			currentVector.possibles = MapLogic.calculatePossibles(this.grid, currentVector);
			sortPossibles(currentVector, currentVector.possibles);
		}
		logger.debug("possibles: " + currentVector.possibles);
		return currentVector.possibles.poll();
	}

	protected boolean fieldsLeftBehind(MapVector next)
	{
		MapField highestDistanceFieldNotVisited;
		MapField field;

		highestDistanceFieldNotVisited = null;
		for(int x = 0; x < this.grid.width; x++)
		{
			for(int y = 0; y < this.grid.height; y++)
			{
				field = this.grid.grid[x][y];
				if(highestDistanceFieldNotVisited == null || !field.visited && field.distanceToFinish_diagonal > highestDistanceFieldNotVisited.distanceToFinish_diagonal)
					highestDistanceFieldNotVisited = field;
			}
		}
		logger.debug(highestDistanceFieldNotVisited.distanceToFinish_diagonal + " -> " + (next != null ? next.end.distanceToFinish_diagonal : "-"));

		return next.end.distanceToFinish_diagonal < highestDistanceFieldNotVisited.distanceToFinish_diagonal - ALLOWED_DISTANCE_OFFSET;
	}

	protected boolean fieldsIsolated(MapVector next)
	{
		int isolatedFields = 0;
		boolean isolated;
		for(int x = 0; x < this.grid.width; x++)
		{
			for(int y = 0; y < this.grid.height; y++)
			{
				if(!this.grid.grid[x][y].road)
					continue;
				if(this.grid.grid[x][y].visited)
					continue;
				
				isolated = true;
				for(int dx = -ISOLATION_TRESHOLD; dx <= ISOLATION_TRESHOLD; dx++)
				{
					for(int dy = -ISOLATION_TRESHOLD; dy <= ISOLATION_TRESHOLD; dy++)
					{
						if(x + dx < 0 || x + dx >= this.grid.width)
							continue; // out of bounds
						if(y + dy < 0 || y + dy >= this.grid.height)
							continue; // out of bounds

						if(next.end.x == x + dx && next.end.y == y + dy) // this is the field that we are heading to
							continue;
						if(this.grid.grid[x + dx][y + dy].reachable && !this.grid.grid[x + dx][y + dy].visited)
						{
							isolated = false;
							break;
						}
					}
					if(!isolated)
						break;
				}
				if(isolated)
				{
					logger.debug("(" + x + "|" + y + ") is isolated");
					isolatedFields++;
				}
			}
		}
		return isolatedFields > 0;
	}

	protected void sortPossibles(MapVector currentVector, LinkedList<MapVector> possibles)
	{
		Collections.sort(possibles, (v1, v2) -> {
			// vector lengths
			int l1 = v1.dx * v1.dx + v1.dy * v1.dy;
			int l2 = v2.dx * v2.dx + v2.dy * v2.dy;

			if(v1.end.distanceToFinish_diagonal == v2.end.distanceToFinish_diagonal) // both fields have the same distance
				return l1 - l2; // shortest vector first // TODO those with the least successors first? (maybe sort only on next (not on getSuccessors)
//			else if(v1.end.distanceToFinish > currentVector.end.distanceToFinish && v2.end.distanceToFinish > currentVector.end.distanceToFinish) // both fields are further than the current
//				return l1 - l2; // shortest vector first
			else if(v1.end.distanceToFinish_diagonal > currentVector.end.distanceToFinish_diagonal - ALLOWED_DISTANCE_OFFSET && v2.end.distanceToFinish_diagonal > currentVector.end.distanceToFinish_diagonal - ALLOWED_DISTANCE_OFFSET) // both fields are further than the current
				return l1 - l2; // shortest vector first
			// else if(v1.end.distanceToFinish > this.end.distanceToFinish) // v1 is further than the current
			// return -1;
			// else if(v2.end.distanceToFinish > this.end.distanceToFinish) // v2 is further than the current
			// return +1;
			else if(v1.end.distanceToFinish_diagonal != v2.end.distanceToFinish_diagonal)
				return -(v1.end.distanceToFinish_diagonal - v2.end.distanceToFinish_diagonal); // furthest field first
			return l1 - l2;
		});
	}
}
