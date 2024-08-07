package ultimate.karopapier;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.extended.PlaceToRace;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Generator;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.PropertiesUtil;

public class RandomGameCreator
{
	/**
	 * Logger-Instance
	 */
	protected static transient final Logger logger = LogManager.getLogger(RandomGameCreator.class);

	public static void main(String[] args) throws IOException
	{
		File loginProperties = new File(args[0]);
		File gameseriesProperties = new File(args[1]);

		Properties login = PropertiesUtil.loadProperties(loginProperties);
		Properties gameseries = PropertiesUtil.loadProperties(gameseriesProperties);

		logger.info("memory usage: total=" + Runtime.getRuntime().totalMemory() + " \tmax=" + Runtime.getRuntime().maxMemory() + " \tfree=" + Runtime.getRuntime().freeMemory());
		
		// add a "watchdog" to make sure that api initialization does not hang
		final CountDownLatch initComplete = new CountDownLatch(1);
		new Thread(() -> {
			try
			{
				if(initComplete.await(KaroAPI.getInitTimeout()*3/2, TimeUnit.SECONDS))
				{
					logger.info("KaroAPI intialization successful");
					return;
				}
				else
				{
					logger.error("KaroAPI intialization timed out");
					System.exit(1);
				}
			}
			catch(InterruptedException e)
			{
				logger.error("Watchdog interrupted");
			}
		}).start();
		
		KaroAPI api = new KaroAPI(login.getProperty("karoAPI.user"), login.getProperty("karoAPI.password"));
		KaroAPICache cache = new KaroAPICache(api, login);
		cache.refresh().join();

		logger.info("memory usage: total=" + Runtime.getRuntime().totalMemory() + " \tmax=" + Runtime.getRuntime().maxMemory() + " \tfree=" + Runtime.getRuntime().freeMemory());

		// notify the watchdog
		initComplete.countDown();
		
		logger.debug("users: " + cache.getUsers().size());
		logger.debug("maps:  " + cache.getMaps().size());

		int gamesMax = Integer.parseInt(gameseries.getProperty("games.max"));
		int gamesCount = Integer.parseInt(gameseries.getProperty("games.count"));
		int gamesPerExecution = Integer.parseInt(gameseries.getProperty("games.perExecution"));
		int gamesToCreateNow = Math.min(gamesMax - gamesCount, gamesPerExecution);
		logger.info("games to create overall: " + gamesMax);
		logger.info("games created so far:    " + gamesCount);
		logger.info("games per exection:      " + gamesPerExecution);
		logger.info("games to create now:     " + gamesToCreateNow);
		
		if(gamesToCreateNow == 0)
		{
			logger.info("no more games to create - exiting");
			return;
		}

		logger.info("creating rules...");
		Rules rules = new Rules();
		rules.setCps(Boolean.valueOf(gameseries.getProperty("rules.cps")));
		rules.setCrashallowed(EnumGameTC.valueOf(gameseries.getProperty("rules.crashallowed")));
		rules.setMaxZzz(Integer.parseInt(gameseries.getProperty("rules.maxZzz")));
		rules.setMinZzz(Integer.parseInt(gameseries.getProperty("rules.minZzz")));
		rules.setStartdirection(EnumGameDirection.valueOf(gameseries.getProperty("rules.startdirection")));
		logger.info(rules);

		double preferStandards;
		try
		{
			preferStandards = Double.valueOf(gameseries.getProperty("rules.preferStandards"));
		}
		catch(Exception e)
		{
			preferStandards = 0;
		}
		logger.info("preferStandards = " + preferStandards);

		logger.info("creating player list...");
		User creator;
		try
		{
			creator = cache.getUser(Integer.parseInt(gameseries.getProperty("players.creator")));
		}
		catch(NumberFormatException e)
		{
			creator = cache.getUser(gameseries.getProperty("players.creator"));
		}
		
		String[] playerIDs = gameseries.getProperty("players.others").split(",");
		Collection<User> players = cache.getUsers(playerIDs);
		StringBuilder sb = new StringBuilder();
		sb.append("Players:");
		for(User p: players)
		{
			sb.append("\n -> ");
			sb.append(p.getLogin());
		}
		logger.info(sb);

		logger.info("creating games...");
		Random random = new Random();
		int gid = 0;
		for(int i = 1; i <= gamesToCreateNow;)
		{
			// @formatter:off
			gid = createGame(	cache,
						gamesCount + i,
						gameseries.getProperty("name"),
						gameseries.getProperty("map"),
						creator,
						players,
						Integer.parseInt(gameseries.getProperty("players.min")),
						Boolean.valueOf(gameseries.getProperty("map.night")),
						rules,
						random,
						preferStandards
						);
			// @formatter:on
			if(gid > 0)
				i++;
		}

		logger.info("writing updated properties...");
		gameseries.setProperty("games.count", "" + (gamesCount + gamesToCreateNow));
		PropertiesUtil.storeProperties(gameseriesProperties, gameseries, null);

		logger.info("exiting");
		System.exit(0);
	}

	public static int createGame(KaroAPICache cache, int i, String name, String mapID, User creator, Collection<User> players, int minPlayers, boolean allowNightMaps, Rules rules, Random random,
			double preferStandards)
	{
		PlannedGame pg = null;
		try
		{
			PlaceToRace ptr;
			LinkedList<User> playersCopy = new LinkedList<>(players);
			Collections.shuffle(playersCopy);
			Set<User> selectedPlayers = new LinkedHashSet<>();
			StringBuilder sb = new StringBuilder();

			if(mapID.equalsIgnoreCase("%random"))
			{
				do
				{
					ptr = cache.getMaps().toArray(new Map[0])[random.nextInt(cache.getMaps().size())];
				} while((ptr.getPlayers() < minPlayers) || (ptr.isNight() && !allowNightMaps));
			}
			else if(mapID.equalsIgnoreCase("%generated"))
			{
				do
				{
					Generator gen = cache.getGenerators().toArray(new Generator[0])[random.nextInt(cache.getGenerators().size())];
					
					if(gen.getKey().equalsIgnoreCase("fernschreiber"))
					{
						ptr = null;
						continue;
					}
					
					String key;
					Object value;
					logger.info("randomizing generator settings for '" + gen.getKey() + "':");
					for(Entry<String, Object> setting: gen.getSettings().entrySet())
					{
						key = setting.getKey();
						value = setting.getValue();
						if(value instanceof Integer)
						{
							int min = KaroAPI.getIntProperty("generator." + gen.getKey() + "." + key + ".min", 0);
							int max = KaroAPI.getIntProperty("generator." + gen.getKey() + "." + key + ".max", 99);
							int step = KaroAPI.getIntProperty("generator." + gen.getKey() + "." + key + ".step", 1);
							
							value = random.nextInt(max - min + 1) /step*step + min; // use /step*step to round to step if necessary
						}
						else if(value instanceof String)
						{
							if(key.equalsIgnoreCase("seed"))
								value = "" + random.nextInt();
						}
						else if(value instanceof Boolean)
						{
							if(key.equalsIgnoreCase("night") && !allowNightMaps)
								value = false;
							else
								value = random.nextBoolean();
						}
						logger.info(" -> " + key + "=" + value);
						gen.getSettings().put(key, value);
					}
					
					ptr = gen;					
				} while(ptr == null || (ptr.getPlayers() < minPlayers) || (ptr.isNight() && !allowNightMaps));
			}
			else
			{
				ptr = cache.getMap(Integer.parseInt(mapID));
			}

			// remove not invitable players
			boolean night = ptr.isNight();
			playersCopy.removeIf(p -> {
				return !p.isInvitable(night);
			});

			selectedPlayers.add(creator);
			while(selectedPlayers.size() < ptr.getPlayers() && !playersCopy.isEmpty())
				selectedPlayers.add(playersCopy.poll());

			pg = new PlannedGame();
			pg.setName(name.replace("%i", "" + toString(i, 3)));
			pg.setMap(ptr);
			pg.setPlayers(selectedPlayers);
			pg.setOptions(rules.createOptions(random, preferStandards, false));

			sb.append("\n -> name           = " + pg.getName());
			sb.append("\n -> map            = " + pg.getMap());
			sb.append("\n -> players        = " + toString(pg.getPlayers()));
			sb.append("\n -> zzz            = " + pg.getOptions().getZzz());
			sb.append("\n -> crashallowed   = " + pg.getOptions().getCrashallowed());
			sb.append("\n -> cps            = " + pg.getOptions().isCps());
			sb.append("\n -> startdirection = " + pg.getOptions().getStartdirection());

			logger.info("Game #" + i + sb);

			pg.setGame(cache.getKaroAPI().createGame(pg).join());
			
			logger.info(" GID = " + (pg.getGame() != null ? pg.getGame().getId() : "?"));

			return (pg.getGame() != null ? pg.getGame().getId() : 0);
		}
		catch(Exception e)
		{
			logger.error(e);
			e.printStackTrace();
			
			if(pg != null)
			{
				// #233 check if game was really not created (in case of Server error as of 02.07.2024)
				try
				{
					List<Game> candidates  = cache.getKaroAPI().findGames(pg).get();
					if(candidates != null && candidates.size() > 0)
					{
						Game created = candidates.get(0);
						if(created.getName().equals(pg.getName())
							&& created.getMap().equals(pg.getMap())
							// only use games created within the last minute (allow some time tolerance)
							&& created.getStarteddate().after(new Date(System.currentTimeMillis() - 60000)))
						{
							return created.getId();
						}
					}
				}
				catch(Exception e2)
				{
					logger.error(e2);
					e2.printStackTrace();
				}
			}
			
			return 0;
		}
	}

	private static String toString(Set<User> players)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		Iterator<User> iter = players.iterator();
		while(iter.hasNext())
		{
			sb.append("" + iter.next().getId());
			if(iter.hasNext())
				sb.append(", ");
		}
		sb.append(" ]");
		return sb.toString();
	}

	private static String toString(int i, int length)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("" + i);
		while(sb.length() < length)
			sb.insert(0, '0');
		return sb.toString();
	}
}
