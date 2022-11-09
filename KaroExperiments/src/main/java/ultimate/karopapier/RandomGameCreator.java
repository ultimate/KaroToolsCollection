package ultimate.karopapier;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.extended.Rules;
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

		KaroAPI api = new KaroAPI(login.getProperty("karoapi.user"), login.getProperty("karoapi.password"));
		KaroAPICache cache = new KaroAPICache(api);
		cache.refresh().join();
		
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
		
		logger.info("creating player list...");
		String[] playerIDs = gameseries.getProperty("players").split(",");
		Set<User> players = new LinkedHashSet<>();
		int id;
		User user;
		StringBuilder sb = new StringBuilder();
		for(String player: playerIDs)
		{
			try
			{
				id = Integer.parseInt(player);
				user = cache.getUser(id);
			}
			catch(NumberFormatException e)
			{
				user = cache.getUser(player);
			}
			
			players.add(user);
			sb.append("\n -> ");
			sb.append(user);
		}
		logger.info("Players:" + sb);

		logger.info("creating games...");
		Random random = new Random();
		PlannedGame pg;
		int mapID;
		for(int i = gamesCount + 1; i <= gamesCount + gamesPerExecution; i++)
		{
			if(gameseries.getProperty("map").equalsIgnoreCase("%random"))
				mapID = random.nextInt(cache.getMaps().size());
			else
				mapID = Integer.parseInt(gameseries.getProperty("map"));

			pg = new PlannedGame();
			pg.setName(gameseries.getProperty("name").replace("%i", "" + i));
			pg.setMap(cache.getMap(mapID));
			pg.setPlayers(players);
			pg.setOptions(rules.createOptions(random));
			
			logger.info("Game #" + i);
			
		}

		gameseries.setProperty("games.count", "" + (gamesCount + gamesPerExecution));
		PropertiesUtil.storeProperties(gameseriesProperties, gameseries, null);
	}
}
