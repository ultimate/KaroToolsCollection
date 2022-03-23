package ultimate.karopapier.eval;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.utils.PropertiesUtil;

public abstract class Eval<T>
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger	logger	= LogManager.getLogger(getClass());

	protected KaroAPICache				karoAPICache;
	protected GameSeries				gameSeries;
	protected Properties				properties;
	protected File						folder;
	protected int						execution;

	public Eval()
	{
	}

	public void prepare(KaroAPICache karoAPICache, GameSeries gameSeries, Properties properties, File folder, int execution)
	{
		this.karoAPICache = karoAPICache;
		this.gameSeries = gameSeries;
		this.properties = properties;
		this.folder = folder;
		this.execution = execution;
	}

	public abstract int doEvaluation() throws Exception; // TODO check meaningful return type

	public void loadGameDetails(List<Game> gamesToLoad)
	{
		List<CompletableFuture<?>> cfs = new ArrayList<>(gamesToLoad.size());
		for(Game game: gamesToLoad)
			cfs.add(this.karoAPICache.refresh(game));

		CompletableFuture.allOf(cfs.toArray(new CompletableFuture[cfs.size()])).join();
	}

	public void loadGameDetails(GameSeries gameSeries)
	{
		List<Game> gamesToLoad = new LinkedList<>();
		for(List<PlannedGame> plannedGames : gameSeries.getGames().values())
		{
			for(PlannedGame plannedGame : plannedGames)
			{
				if(plannedGame.getGame() != null)
					gamesToLoad.add(plannedGame.getGame());
			}
		}
		loadGameDetails(gamesToLoad);
	}
	// HELPERS

	protected void writeProperties(String filename, Properties p) throws IOException
	{
		PropertiesUtil.storeProperties(new File(folder, filename), p, null);
	}

	protected void writeFile(String filename, String content) throws IOException
	{
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(folder, filename)));

		bos.write(content.getBytes());

		bos.flush();
		bos.close();
	}

	protected static double round(double d)
	{
		return Math.round(d * 100) / 100.0;
	}
}
