package ultimate.karopapier.painter;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.utils.PropertiesUtil;

public class PainterTester
{
	/**
	 * Logger-Instance
	 */
	protected transient static final Logger	logger					= LogManager.getLogger(PainterTester.class);
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		// parse arguments
		Properties properties = PropertiesUtil.loadProperties(new File(args[0]));
		int mapID = Integer.parseInt(args[1]);
		
		// initiate the API
		KaroAPI api = new KaroAPI(properties.getProperty("karoAPI.user"), properties.getProperty("karoAPI.password"));

		// load the map from the API
		String mapCode = api.getMapCode(mapID).get();
		
		logger.debug("map code =\n" + mapCode);
		
		// initiate the grid
		MapGrid grid = new MapGrid(mapCode);
		
		MapFrame frame = new MapFrame(grid);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
		
		// make a start selection move
		MapVector startSelected = new MapVector(grid, grid.startFields.get(0), 0, 0);
		
		logger.debug("start @ x=" + startSelected.end.x + ", y=" + startSelected.end.y);
		
		// create a pathfinder
		PathFinder pathFinder = new PathFinder(grid, null, startSelected);
		
		frame.setPath(pathFinder.getPath());
		frame.repaintMap();
		
		int step;
		long time = System.currentTimeMillis();
		while(!pathFinder.isFinished())
		{
			step = pathFinder.step();

			// if(step % 1000 == 0)
			if(System.currentTimeMillis() > time + 100)
			{
				frame.repaintMap();
				time = System.currentTimeMillis();
			}
		}
	}
}
