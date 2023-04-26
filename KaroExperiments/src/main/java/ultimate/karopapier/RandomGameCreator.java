package ultimate.karopapier;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.extended.Rules;
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

		KaroAPI api = new KaroAPI(login.getProperty("karoAPI.user"), login.getProperty("karoAPI.password"));
		KaroAPICache cache = new KaroAPICache(api, login);
		cache.refresh().join();

		logger.info("memory usage: total=" + Runtime.getRuntime().totalMemory() + " \tmax=" + Runtime.getRuntime().maxMemory() + " \tfree=" + Runtime.getRuntime().freeMemory());

		logger.debug("users: " + cache.getUsers().size());
		logger.debug("maps:  " + cache.getMaps().size());

		int gamesCount = Integer.parseInt(gameseries.getProperty("games.count"));
		int gamesPerExecution = Integer.parseInt(gameseries.getProperty("games.perExecution"));
		logger.info("games created so far: " + gamesCount);
		logger.info("games to create now:  " + gamesPerExecution);

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
		User creator = getUser(cache, gameseries.getProperty("players.creator"));
		String[] playerIDs = gameseries.getProperty("players.others").split(",");
		Set<User> players = new LinkedHashSet<>();
		User user;
		StringBuilder sb = new StringBuilder();
		for(String player : playerIDs)
		{
			if(player.equalsIgnoreCase("%desperate"))
			{
				logger.info("adding desperate players...");
				try
				{
					for(User desperate : api.getUsers(null, null, true).get())
					{
						players.add(desperate);
						sb.append("\n -> ");
						sb.append(desperate);
					}
				}
				catch(InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch(ExecutionException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				user = getUser(cache, player);
				players.add(user);
				sb.append("\n -> ");
				sb.append(user);
			}
		}
		logger.info("Players:" + sb);

		logger.info("creating games...");
		Random random = new Random();
		for(int i = gamesCount + 1; i <= gamesCount + gamesPerExecution; i++)
		{
			// @formatter:off
			createGame(	cache,
						i,
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
		}

		logger.info("writing updated properties...");
		gameseries.setProperty("games.count", "" + (gamesCount + gamesPerExecution));
		PropertiesUtil.storeProperties(gameseriesProperties, gameseries, null);

		logger.info("exiting");
	}

	private static int createGame(KaroAPICache cache, int i, String name, String mapID, User creator, Set<User> players, int minPlayers, boolean allowNightMaps, Rules rules, Random random,
			double preferStandards)
	{
		try
		{
			PlannedGame pg;
			Map map;
			LinkedList<User> playersCopy = new LinkedList<>(players);
			Collections.shuffle(playersCopy);
			Set<User> selectedPlayers = new LinkedHashSet<>();
			StringBuilder sb = new StringBuilder();

			if(mapID.equalsIgnoreCase("%random"))
			{
				do
				{
					map = cache.getMaps().toArray(new Map[0])[random.nextInt(cache.getMaps().size())];
				} while((map.getPlayers() < minPlayers) || (map.isNight() && !allowNightMaps));
			}
			else
			{
				map = cache.getMap(Integer.parseInt(mapID));
			}
			
			// remove not invitable players
			boolean night = map.isNight();
			playersCopy.removeIf(p -> {
				return !p.isInvitable(night);
			});

			selectedPlayers.add(creator);
			while(selectedPlayers.size() < map.getPlayers() && !playersCopy.isEmpty())
				selectedPlayers.add(playersCopy.poll());

			pg = new PlannedGame();
			pg.setName(name.replace("%i", "" + toString(i, 3)));
			pg.setMap(map);
			pg.setPlayers(selectedPlayers);
			pg.setOptions(rules.createOptions(random, preferStandards));

			sb.append("\n -> name           = " + pg.getName());
			sb.append("\n -> map            = " + pg.getMap().getId());
			sb.append("\n -> players        = " + toString(pg.getPlayers()));
			sb.append("\n -> zzz            = " + pg.getOptions().getZzz());
			sb.append("\n -> crashallowed   = " + pg.getOptions().getCrashallowed());
			sb.append("\n -> cps            = " + pg.getOptions().isCps());
			sb.append("\n -> startdirection = " + pg.getOptions().getStartdirection());

			logger.info("Game #" + i + sb);

			pg.setGame(cache.getKaroAPI().createGame(pg).join());

			logger.info(" GID = " + (pg.getGame() != null ? pg.getGame().getId() : "?"));

			return pg.getGame().getId();
		}
		catch(Exception e)
		{
			logger.error(e);
			e.printStackTrace();
			return 0;
		}
	}

	private static User getUser(KaroAPICache cache, String nameOrId)
	{
		try
		{
			return cache.getUser(Integer.parseInt(nameOrId));
		}
		catch(NumberFormatException e)
		{
			return cache.getUser(nameOrId);
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
