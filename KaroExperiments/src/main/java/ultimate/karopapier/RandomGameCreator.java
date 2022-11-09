package ultimate.karopapier;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karoapi4j.model.official.PlannedGame;
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

		Random random = new Random();
		PlannedGame pg;
		for(int i = 1; i < gamesPerExecution; i++)
		{
			pg = new PlannedGame();
			pg.setName(gameseries.getProperty("name").replace("%i", "" + (gamesCount + i)));
			pg.setMap( todo );
			pg.setPlayers( todo );
			pg.setOptions(rules.createOptions(random));
		}

		gameseries.setProperty("games.count", "" + (gamesCount + gamesPerExecution));
		PropertiesUtil.storeProperties(gameseriesProperties, gameseries, null);
	}
}
