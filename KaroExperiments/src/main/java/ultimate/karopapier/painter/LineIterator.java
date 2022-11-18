package ultimate.karopapier.painter;

import java.awt.Point;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * This class represents an {@link Iterator} that is able to iterate from one {@link Point} to
 * another using rasterization of the line in between them. This means all points that are between
 * start and end (both inclusive) are iterated.<br>
 * for example:
 * <ul>
 * <li>horizontal line:
 *
 * <pre>
 * ________
 * _XXXXXX_
 * ________
 * </pre>
 *
 * </li>
 * <li>diagonal line:
 *
 * <pre>
 * ______
 * _X____
 * __X___
 * ___X__
 * ____X_
 * ______
 * </pre>
 *
 * </li>
 * <li>arbitrary line:
 *
 * <pre>
 * ________
 * _X______
 * __XX____
 * ____XX__
 * ______X_
 * ________
 * </pre>
 *
 * </li>
 *
 * @author ultimate
 */
public class LineIterator implements Iterator<Point>, Iterable<Point>
{
	/**
	 * The {@link Logger} for this class
	 */
	protected transient final Logger	logger	= Logger.getLogger(getClass().getName());
	/**
	 * the starting point for iteration
	 */
	private final Point					start;
	/**
	 * the end point for iteration
	 */
	private final Point					end;
	/**
	 * the current iterator position
	 */
	private Point						current;
	/**
	 * the next titerator position
	 */
	private Point						next;
	/**
	 * the total difference in x direction
	 */
	private final int					dx;
	/**
	 * the total difference in y direction
	 */
	private final int					dy;
	/**
	 * the incrementor for x direction
	 */
	private final int					ix;
	/**
	 * the incrementor for y direction
	 */
	private final int					iy;
	/**
	 * the current iteration step
	 */
	private int							step;
	/**
	 * the gradient used to determine whether we are "above" or "below" the theoretic direct line
	 */
	private double						g;

	/**
	 * @param start - the starting point for iteration
	 * @param end - the end point for iteration
	 */
	public LineIterator(Point start, Point end)
	{
		super();
		this.start = start.getLocation();
		this.end = end.getLocation();
		this.dx = end.x - start.x;
		this.dy = end.y - start.y;
		this.ix = (int) Math.signum(dx);
		this.iy = (int) Math.signum(dy);

		this.reset();
	}

	/**
	 * @return the starting point for iteration
	 */
	public Point getStart()
	{
		return start.getLocation();
	}

	/**
	 * @return the end point for iteration
	 */
	public Point getEnd()
	{
		return end.getLocation();
	}

	/**
	 * Reset this iterator to its initial state
	 */
	public void reset()
	{
		this.current = null;
		this.next = start;

		if(Math.abs(dy) > Math.abs(dx))
			this.g = Math.abs(dy) * 0.5 - Math.abs(dx);
		else if(Math.abs(dy) < Math.abs(dx))
			this.g = Math.abs(dy) - Math.abs(dx) * 0.5;
		else
			this.g = 1;

		this.step = -1;
	}

	@Override
	public boolean hasNext()
	{
		return next != null;
	}

	@Override
	public Iterator<Point> iterator()
	{
		return this;
	}

	@Override
	public Point next()
	{
		current = next;

		// calc next
		if(!current.equals(end))
		{
			step++;
			if(Math.abs(dy) > Math.abs(dx))
			{
				if(g <= 0)
				{
					next = new Point(current.x, current.y + iy);
					g += Math.abs(dx);
				}
				else
				{
					next = new Point(current.x + ix, current.y + iy);
					g += (Math.abs(dx) - Math.abs(dy));
				}
			}
			else if(Math.abs(dy) < Math.abs(dx))
			{
				if(g <= 0)
				{
					next = new Point(current.x + ix, current.y);
					g += Math.abs(dy);
				}
				else
				{
					next = new Point(current.x + ix, current.y + iy);
					g += (Math.abs(dy) - Math.abs(dx));
				}
			}
			else
			{
				next = new Point(current.x + ix, current.y + iy);
			}
		}
		else
		{
			step++;
			next = null;
		}

		return current;
	}

	/**
	 * @return the current iterator position
	 */
	public Point current()
	{
		return current;
	}

	/**
	 * @return current.x
	 */
	public int x()
	{
		return current.x;
	}

	/**
	 * @return current.y
	 */
	public int y()
	{
		return current.y;
	}

	/**
	 * @return the current iteration step
	 */
	public int step()
	{
		return step;
	}

	/**
	 * Creates an inverted fresh iterator. The state is not copied!.
	 *
	 * @return the new iterator
	 */
	public LineIterator invert()
	{
		return new LineIterator(end, start);
	}

	@Override
	public String toString()
	{
		return "LineIterator [start=" + start + ", end=" + end + "]";
	}
}