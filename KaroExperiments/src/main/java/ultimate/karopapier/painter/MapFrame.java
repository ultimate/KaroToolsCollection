package ultimate.karopapier.painter;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.util.Collection;

import javax.swing.JFrame;

public class MapFrame extends JFrame
{
	private static final long		serialVersionUID	= 1L;

	public static final int			GRID_SIZE			= 20;
	public static final int			TITLE_BAR			= 20;

	private Canvas					canvas;
	private MapGrid					grid;
	private Collection<MapField>	highlights;
	private Collection<MapVector>	path;

	private boolean					debug;

	public MapFrame(MapGrid grid)
	{
		this.grid = grid;

		int width = grid.width * GRID_SIZE;
		int height = grid.height * GRID_SIZE + TITLE_BAR;

		this.setSize(width, height);
		this.setLayout(new FlowLayout());

		this.canvas = new Canvas();
		this.canvas.setPreferredSize(new Dimension(width, height));

		this.getContentPane().add(this.canvas);

		this.setVisible(true);
		this.requestFocus();
	}

	public boolean isDebug()
	{
		return debug;
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	public Collection<MapVector> getPath()
	{
		return path;
	}

	public void setPath(Collection<MapVector> path)
	{
		this.path = path;
	}

	public Collection<MapField> getHighlights()
	{
		return highlights;
	}

	public void setHighlights(Collection<MapField> highlights)
	{
		this.highlights = highlights;
	}

	public void repaintMap()
	{
		Graphics g = canvas.getGraphics();

		g.setColor(new Color(0, 255, 0));
		if(debug)
			g.setColor(new Color(0, 0, 0));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		drawGrid(this.grid, g);

		if(this.path != null && !this.path.isEmpty())
		{
			g.setColor(new Color(0, 0, 255));
			drawPath(this.path, g);
		}
		if(this.highlights != null && !this.highlights.isEmpty())
		{
			g.setColor(new Color(0, 255, 0));
			for(MapField field : this.highlights)
				drawPoint(field.x, field.x, g);
		}
	}

	protected void drawGrid(MapGrid grid, Graphics g)
	{
		for(int x = 0; x < grid.width; x++)
		{
			for(int y = 0; y < grid.height; y++)
			{
				if(grid.grid[x][y].road)
				{
					g.setColor(new Color(127, 127, 127));
					g.fillRect(x * GRID_SIZE, y * GRID_SIZE, GRID_SIZE - 1, GRID_SIZE - 1);
				}

				if(debug)
				{
					g.setColor(new Color(255, 255, 255));
					g.drawString("" + grid.grid[x][y].distanceToFinish, x * GRID_SIZE, (y + 1) * GRID_SIZE);
					g.setColor(grid.grid[x][y].reachable ? new Color(0, 255, 0) : new Color(255, 0, 0));
					g.drawRect(x * GRID_SIZE, y * GRID_SIZE, GRID_SIZE - 1, GRID_SIZE - 1);
				}
			}
		}
	}

	protected void drawPath(Collection<MapVector> path, Graphics g)
	{
		for(MapVector v : path)
		{
			g.drawLine(v.start.x * GRID_SIZE + GRID_SIZE / 2, v.start.y * GRID_SIZE + GRID_SIZE / 2, v.end.x * GRID_SIZE + GRID_SIZE / 2, v.end.y * GRID_SIZE + GRID_SIZE / 2);
			drawPoint(v.start.x, v.start.y, g);
		}
	}

	protected void drawPoint(int x, int y, Graphics g)
	{
		g.fillRect(x * GRID_SIZE + GRID_SIZE / 2 - 1, y * GRID_SIZE + GRID_SIZE / 2 - 1, 3, 3);
	}
}
