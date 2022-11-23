package ultimate.karopapier.painter;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JFrame;

public class MapFrame extends JFrame implements Runnable
{
	private static final long	serialVersionUID	= 1L;

	public static final int		GRID_SIZE			= 20;
	public static final int		TITLE_BAR			= 20;

	private Canvas				canvas;
	private MapGrid				grid;
	private List<MapField>		highlights;
	private List<MapVector>		path;

	private int					refreshRate;
	private boolean				debug;

	private boolean				shutdown;
	private Thread				refreshThread;

	public MapFrame(MapGrid grid, int refreshRate)
	{
		this.grid = grid;

		int width = grid.width * GRID_SIZE;
		int height = grid.height * GRID_SIZE + TITLE_BAR;

		this.setSize(width, height);
		this.setTitle("PathFinder Visualization");
		this.setLayout(new FlowLayout());

		this.canvas = new Canvas();
		this.canvas.setPreferredSize(new Dimension(width, height));

		this.getContentPane().add(this.canvas);

		this.setVisible(true);
		this.requestFocus();

		this.shutdown = false;
		this.refreshRate = refreshRate;
		this.refreshThread = new Thread(this);
		this.refreshThread.start();
	}

	@Override
	public void run()
	{
		while(!shutdown)
		{
			this.repaintMap();
			try
			{
				Thread.sleep(refreshRate);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void dispose()
	{
		this.shutdown = true;
		try
		{
			this.refreshThread.join(this.refreshRate * 2);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		super.dispose();
	}

	public boolean isDebug()
	{
		return debug;
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	public List<MapVector> getPath()
	{
		return path;
	}

	public void setPath(List<MapVector> path)
	{
		this.path = path;
	}

	public List<MapField> getHighlights()
	{
		return highlights;
	}

	public void setHighlights(List<MapField> highlights)
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
				if(grid.grid[x][y].breakpoint)
				{
					g.setColor(new Color(255, 255, 255));
					drawField(x, y, g);
				}
				else if(grid.grid[x][y].road)
				{
					g.setColor(new Color(127, 127, 127));
					drawField(x, y, g);
				}

				if(debug)
				{
					g.setColor(new Color(255, 255, 255));
					g.setFont(new Font("TimesRoman", Font.PLAIN, GRID_SIZE / 2));
					g.drawString("" + grid.grid[x][y].distanceToFinish_straight, x * GRID_SIZE + 1, (int) ((y + 0.5) * GRID_SIZE - 1));
					g.drawString("" + grid.grid[x][y].distanceToFinish_diagonal, x * GRID_SIZE + 1, (y + 1) * GRID_SIZE - 1);
					g.setColor(grid.grid[x][y].reachable ? new Color(0, 255, 0) : new Color(255, 0, 0));
					g.drawRect(x * GRID_SIZE, y * GRID_SIZE, GRID_SIZE - 1, GRID_SIZE - 1);
				}
			}
		}
	}

	protected void drawField(int x, int y, Graphics g)
	{
		g.fillRect(x * GRID_SIZE, y * GRID_SIZE, GRID_SIZE - 1, GRID_SIZE - 1);
	}

	protected void drawPath(List<MapVector> path, Graphics g)
	{
		MapVector v;
		for(int i = 0; i < path.size(); i++)
		{
			v = path.get(i);
			g.drawLine(v.start.x * GRID_SIZE + GRID_SIZE / 2, v.start.y * GRID_SIZE + GRID_SIZE / 2, v.end.x * GRID_SIZE + GRID_SIZE / 2, v.end.y * GRID_SIZE + GRID_SIZE / 2);
			drawPoint(v.start.x, v.start.y, g);
		}
		;
	}

	protected void drawPoint(int x, int y, Graphics g)
	{
		g.fillRect(x * GRID_SIZE + GRID_SIZE / 2 - 1, y * GRID_SIZE + GRID_SIZE / 2 - 1, 3, 3);
	}
}
