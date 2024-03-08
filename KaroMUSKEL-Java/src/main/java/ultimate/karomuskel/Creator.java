package ultimate.karomuskel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Generator;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.PlannedGame;

public class Creator
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger	logger						= LogManager.getLogger(getClass());

	private static final int			SLEEP_TIME_ON_SIMULATION	= 500;
	private KaroAPICache				karoAPICache;
	private Random						random						= new Random();
	private AtomicInteger				counter						= new AtomicInteger();

	public Creator(KaroAPICache karoAPICache)
	{
		super();
		this.karoAPICache = karoAPICache;
	}

	public KaroAPICache getKaroAPICache()
	{
		return karoAPICache;
	}

	protected CompletableFuture<Void> createGame(PlannedGame plannedGame, Consumer<PlannedGame> consumer)
	{
		if(plannedGame.isCreated())
			return CompletableFuture.completedFuture(null);

		CompletableFuture<Game> cf;
		if(this.karoAPICache != null && this.karoAPICache.getKaroAPI() != null)
			cf = this.karoAPICache.getKaroAPI().createGame(plannedGame);
		else
			cf = CompletableFuture.supplyAsync(() -> {
				randomSleep();
				if(plannedGame.getMap() instanceof Generator)
					plannedGame.setMap(new Map(10000 + counter.incrementAndGet()));
				return new Game(plannedGame.hashCode() & 0x7FFFF);
			});
		return cf.thenAcceptAsync(createdGame -> {
			this.karoAPICache.cache(createdGame);
			if(createdGame != null)
			{
				plannedGame.setCreated(true);
				plannedGame.setGame(createdGame);
			}
		}).thenAcceptAsync(v -> {
			if(consumer != null)
				consumer.accept(plannedGame);
		}).exceptionally(ex -> {
			logger.error("error creating game", ex);
			plannedGame.setException(ex);
			if(consumer != null)
				consumer.accept(plannedGame);
			return null;
		});
	}

	public CompletableFuture<Void> createGames(List<PlannedGame> plannedGames, Consumer<PlannedGame> consumer)
	{
		// we need to separate lists here
		// 1. for normal games (with an existing map): games can be created in parallel
		List<CompletableFuture<Void>> cfs = new ArrayList<>(plannedGames.size());
		// 2. for games using a map generator: generator has to be called for each game first and then the game can be created
		// NOTE: the next generator can first be called after the map has been used (otherwise it will be overwritten)
		// so we are chaining requests here
		CompletableFuture<Void> cfsWithMapGenerators = CompletableFuture.completedFuture(null);
		cfs.add(cfsWithMapGenerators);

		for(PlannedGame plannedGame : plannedGames)
		{
			// check for map to create - if necessary: chain the CFs
			if(plannedGame.getMap() instanceof Map)
			{
				// call create game immediately
				cfs.add(createGame(plannedGame, consumer));
			}
			else if(plannedGame.getMap() instanceof Generator)
			{
				// call create game after the chain
				cfsWithMapGenerators = cfsWithMapGenerators.thenCompose(v -> {
					return createGame(plannedGame, consumer);
				});
			}
			else
				logger.error("unknown PlaceToRace type: " + plannedGame.getMap());
		}
		return CompletableFuture.allOf(cfs.toArray(new CompletableFuture[cfs.size()]));
	}

	protected CompletableFuture<Void> leaveGame(PlannedGame plannedGame, Consumer<PlannedGame> consumer)
	{
		if(plannedGame.isLeft())
			return CompletableFuture.completedFuture(null);
		if(plannedGame.getGame() == null)
		{
			logger.warn("game reference is null: " + plannedGame.getName());
			return CompletableFuture.completedFuture(null);
		}

		CompletableFuture<Boolean> cf;
		if(this.karoAPICache != null && this.karoAPICache.getKaroAPI() != null)
			cf = this.karoAPICache.getKaroAPI().leaveGame(plannedGame.getGame().getId());
		else
			cf = CompletableFuture.supplyAsync(() -> {
				randomSleep();
				return true;
			});
		return cf.thenAcceptAsync(leftGame -> {
			if(leftGame)
				plannedGame.setLeft(true);
		}).thenAcceptAsync(v -> {
			if(consumer != null)
				consumer.accept(plannedGame);
		}).exceptionally(ex -> {
			logger.error("error creating game", ex);
			plannedGame.setException(ex);
			if(consumer != null)
				consumer.accept(plannedGame);
			return null;
		});
	}

	public CompletableFuture<Void> leaveGames(List<PlannedGame> plannedGames, Consumer<PlannedGame> consumer)
	{
		List<CompletableFuture<Void>> cfs = new ArrayList<>(plannedGames.size());
		for(PlannedGame plannedGame : plannedGames)
			cfs.add(leaveGame(plannedGame, consumer));
		return CompletableFuture.allOf(cfs.toArray(new CompletableFuture[cfs.size()]));
	}

	private void randomSleep()
	{
		sleep(random.nextInt(SLEEP_TIME_ON_SIMULATION));
	}

	private void sleep(int ms)
	{
		try
		{
			Thread.sleep(ms);
		}
		catch(InterruptedException e)
		{
			logger.error(e);
		}
	}
}
