package ultimate.karopapier.eval;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

public abstract class Eval<T>
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger	logger	= LogManager.getLogger(getClass());

	protected KaroAPICache				karoAPICache;
	protected T							data;
	protected Properties				properties;
	protected File						folder;
	protected int						execution;

	public Eval()
	{
	}

	public void prepare(KaroAPICache karoAPICache, T data, Properties properties, File folder, int execution)
	{
		this.karoAPICache = karoAPICache;
		this.data = data;
		this.properties = properties;
		this.folder = folder;
		this.execution = execution;
	}

	public abstract List<File> evaluate() throws Exception;

	public void loadGameDetails(List<Game> gamesToLoad)
	{
		List<CompletableFuture<?>> cfs = new ArrayList<>(gamesToLoad.size());
		for(Game game : gamesToLoad)
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

	protected String readFile(String filename) throws IOException
	{
		File file = new File(folder, filename);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		String content = new String(bis.readAllBytes());
		bis.close();
		return content;
	}

	protected File writeFile(String filename, String content) throws IOException
	{
		File file = new File(folder, filename);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
		bos.write(content.getBytes());
		bos.flush();
		bos.close();
		return file;
	}
}
