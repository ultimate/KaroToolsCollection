package ultimate.karopapier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.PropertiesUtil;

public class FillGameSeriesGaps
{
	/**
	 * Logger-Instance
	 */
	protected static transient final Logger logger = LogManager.getLogger(RandomGameCreator.class);
	
	public static void main(String[] args) throws IOException
	{
		File loginProperties = new File(args[0]);
		File gameseriesProperties = new File(args[1]);
		String gameSeriesTitle = args[2];
		int firstGID = Integer.parseInt(args[3]);
		int lastGID = Integer.parseInt(args[4]);
		
		Properties login = PropertiesUtil.loadProperties(loginProperties);
		Properties gameseries = PropertiesUtil.loadProperties(gameseriesProperties);

		KaroAPI api = new KaroAPI(login.getProperty("karoAPI.user"), login.getProperty("karoAPI.password"));
		KaroAPICache cache = new KaroAPICache(api, login);
		cache.refresh().join();
		
		List<Game> matches = new ArrayList<>();
		
		logger.info("loading games");
		Game game;
		for(int gid = firstGID; gid <= lastGID; gid++)
		{
			try
			{
				game = api.getGame(gid).get();
				if(game == null)
					break;
				cache.cache(game);
				if(game.getName().startsWith(gameSeriesTitle))
					matches.add(game);
			}
			catch(Exception e)
			{
				break;
			}
			if(gid % 100 == 0)
				System.out.print(".");
		}
		
		logger.info("games loaded = " + cache.getGames().size());
		logger.info("matches      = " + matches.size());
		
		matches.sort(new Comparator<Game>() {
			@Override
			public int compare(Game g1, Game g2)
			{
				return g1.getName().compareTo(g2.getName());
			}
		});
		


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
		
		logger.info("hole spiele nach...");
		String title = gameSeriesTitle + " %i - (fehlende Spiele nachholen)";

		int lastGameNumber = 0;
		int gameNumber;
		int gid;
		Random random = new Random();
		for(Game g: matches)
		{
			logger.info(g.getName() + " -> " + g.getId());
			gameNumber = Integer.parseInt(g.getName().substring(g.getName().lastIndexOf(" ") + 1));
			if(gameNumber >= lastGameNumber + 1)
			{
				for(int gi = lastGameNumber + 1; gi < gameNumber;)
				{
					logger.info("#" + gi + " not found -> creating replacement");
					// @formatter:off
					gid = RandomGameCreator.createGame(cache,
							gi,
							title,
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
						gi++;
				}
				lastGameNumber = gameNumber;
			}
		}
	}
}
