package ultimate.karomuskel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.PlannedGame;

public class Creator
{
	private static final int	MAX_SLEEP_TIME	= 500;
	private KaroAPICache		karoAPICache;
	private Random				random			= new Random();

	public Creator(KaroAPICache karoAPICache)
	{
		super();
		this.karoAPICache = karoAPICache;
	}

	public KaroAPICache getKaroAPICache()
	{
		return karoAPICache;
	}

	public CompletableFuture<Void> createGame(PlannedGame plannedGame)
	{
		if(plannedGame.isCreated())
			return CompletableFuture.completedFuture(null);

		CompletableFuture<Game> cf;
		if(this.karoAPICache.getKaroAPI() != null)
			cf = this.karoAPICache.getKaroAPI().createGame(plannedGame);
		else
			cf = CompletableFuture.supplyAsync(() -> { randomSleep(); return new Game(plannedGame.hashCode()); });
		return cf.thenAcceptAsync(createdGame -> { plannedGame.setCreated(true); plannedGame.setGame(createdGame); });
	}

	public CompletableFuture<Void> createGames(List<PlannedGame> plannedGames, Consumer<PlannedGame> consumer)
	{
		List<CompletableFuture<Void>> cfs = new ArrayList<>(plannedGames.size());
		for(PlannedGame plannedGame : plannedGames)
			createGame(plannedGame).thenAcceptAsync(v -> { consumer.accept(plannedGame); });
		return CompletableFuture.allOf(cfs.toArray(new CompletableFuture[plannedGames.size()]));
	}

	public CompletableFuture<Void> leaveGame(PlannedGame plannedGame)
	{
		if(plannedGame.isLeft())
			return CompletableFuture.completedFuture(null);

		CompletableFuture<Boolean> cf;
		if(this.karoAPICache.getKaroAPI() != null)
			cf = this.karoAPICache.getKaroAPI().kick(plannedGame.getGame().getId(), karoAPICache.getCurrentUser().getId());
		else
			cf = CompletableFuture.supplyAsync(() -> { randomSleep(); return true; });
		return cf.thenAcceptAsync(leftGame -> {
			if(leftGame)
				plannedGame.setLeft(true);
		});
	}

	public CompletableFuture<Void> leaveGames(List<PlannedGame> plannedGames, Consumer<PlannedGame> consumer)
	{
		List<CompletableFuture<Void>> cfs = new ArrayList<>(plannedGames.size());
		for(PlannedGame plannedGame : plannedGames)
			leaveGame(plannedGame).thenAcceptAsync(v -> { consumer.accept(plannedGame); });
		return CompletableFuture.allOf(cfs.toArray(new CompletableFuture[plannedGames.size()]));
	}

	private void randomSleep()
	{
		try
		{
			Thread.sleep(random.nextInt(MAX_SLEEP_TIME));
		}
		catch(InterruptedException e)
		{
		}
	}
}