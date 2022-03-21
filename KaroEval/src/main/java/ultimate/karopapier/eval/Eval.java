package ultimate.karopapier.eval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.official.Game;

public class Eval
{
	protected KaroAPI karoAPI;
	
	public Eval(KaroAPI karoAPI)
	{
		this.karoAPI = karoAPI;
	}
	
	public Map<Integer, Game> loadGameDetails(List<Integer> ids)
	{
		Map<Integer, Game> games = new HashMap<>();
		
		List<CompletableFuture<?>> cfs = new ArrayList<>(ids.size());
		for(int id: ids)
		{
			cfs.add(this.karoAPI.getGameWithDetails(id).thenAcceptAsync(game -> {
				games.put(game.getId(), game);
			}));
		}
		
		CompletableFuture.allOf(cfs.toArray(new CompletableFuture[ids.size()])).join();
		
		return games;
	}
	
	
	public String doEvaluation() throws Exception; // TODO check meaningful return type
	
	public void prepare(GameSeries gs, int execution);
	
	
	
}
