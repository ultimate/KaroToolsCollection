package ultimate.karopapier.eval;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.utils.PropertiesUtil;

public abstract class Eval<T>
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger	logger	= LogManager.getLogger(getClass());

	protected KaroAPI					karoAPI;
	protected Properties				properties;
	protected File						folder;
	protected int						execution;

	public Eval()
	{
	}

	public void prepare(KaroAPI karoAPI, Properties properties, File folder, int execution)
	{
		this.karoAPI = karoAPI;
		this.properties = properties;
		this.folder = folder;
		this.execution = execution;
	}

	public abstract String doEvaluation() throws Exception; // TODO check meaningful return type

	public Map<Integer, Game> loadGameDetails(List<Integer> ids)
	{
		Map<Integer, Game> games = new HashMap<>();

		List<CompletableFuture<?>> cfs = new ArrayList<>(ids.size());
		for(int id : ids)
		{
			cfs.add(this.karoAPI.getGameWithDetails(id).thenAcceptAsync(game -> { games.put(game.getId(), game); }));
		}

		CompletableFuture.allOf(cfs.toArray(new CompletableFuture[ids.size()])).join();

		return games;
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
