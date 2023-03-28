package ultimate.karopapier;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karopapier.painter.LineIterator;
import ultimate.karopapier.painter.MapField;
import ultimate.karopapier.painter.MapGrid;
import ultimate.karopapier.painter.MapLogic;
import ultimate.karopapier.painter.MapVector;

public class AllVectors
{
	/**
	 * Logger-Instance
	 */
	protected static transient final Logger logger = LogManager.getLogger(AllVectors.class);

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		File loginProperties = new File(args[0]);
		int gid = Integer.parseInt(args[1]);

		Properties login = PropertiesUtil.loadProperties(loginProperties);

		KaroAPI api = new KaroAPI(login.getProperty("karoAPI.user"), login.getProperty("karoAPI.password"));

		Game game = api.getGameWithDetails(gid).get();

		String mapCode = game.getMap().getCode();

		MapGrid grid = new MapGrid(mapCode);

		List<MapVector> vectors = new ArrayList<>(1000);
		Queue<MapVector> tmp = new LinkedBlockingQueue<>();

		for(MapField field : grid.startFields)
			tmp.add(new MapVector(grid, field, 0, 0));

		MapField finish = grid.finishFields.get(0);

		Function<MapVector, Boolean> touchesFinish = (vector) -> {
			LineIterator iter = new LineIterator(new Point(vector.start.x, vector.start.y), new Point(vector.end.x, vector.end.y));

			Point p;
			MapField f;
			while(iter.hasNext())
			{
				p = iter.next();
				f = grid.grid[p.x][p.y];
				if(f == finish)
					return true;
			}
			return false;
		};

		MapVector v1;
		while(!tmp.isEmpty())
		{
			v1 = tmp.poll();
			v1.possibles = MapLogic.calculatePossibles(grid, v1);

			for(MapVector v2 : v1.possibles)
			{
				if(touchesFinish.apply(v2))
					continue;

				MapVector duplicate = null;
				for(MapVector v3 : vectors)
				{
					if(v3.equals(v2))
					{
						duplicate = v3;
						break;
					}
				}
				if(vectors.contains(v2) && duplicate == null)
				{
					logger.error("oops");
				}
				if(duplicate != null)
				{
					if(duplicate.origins == null)
						duplicate.origins = new LinkedList<>();
					if(!duplicate.origins.contains(v2))
						duplicate.origins.add(v2);
					continue;
				}

				vectors.add(v2);
				tmp.add(v2);
			}

			logger.info("vectors found = " + vectors.size() + " queue size = " + tmp.size());
		}

		List<MapVector> moves = new ArrayList<>(game.getPlayers().get(0).getMoves().size());
		int xStart, yStart;
		MapField start;
		MapVector move;
		for(Move m : game.getPlayers().get(0).getMoves())
		{
			if(m.isCrash())
				continue;
			xStart = m.getX() - m.getXv();
			yStart = m.getY() - m.getYv();
			start = grid.grid[xStart][yStart];
			move = new MapVector(grid, start, m.getXv(), m.getYv());
			if(!moves.contains(move))
				moves.add(move);
		}
		
		logger.info("moves made = " + game.getPlayers().get(0).getMoves().size() + " distinct = " + moves.size());

		vectors.removeAll(moves);
		
		logger.info("remaining vectors = " + vectors.size());
	}
}
