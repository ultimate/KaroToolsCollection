package ultimate.karomuskel.special;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumCreatorParticipation;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.Team;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karomuskel.GameSeriesManager;

public abstract class GameSeriesUpdater
{
	/**
	 * Logger-Instance
	 */
	protected static transient final Logger logger = LogManager.getLogger(GameSeriesUpdater.class);

	private GameSeriesUpdater()
	{

	}

	@SuppressWarnings("deprecation")
	public static void updateV312KLC(KaroAPICache cache, File in, File out) throws IOException
	{
		logger.info("loading original file: " + in.getAbsolutePath());
		GameSeries gs = GameSeriesManager.load(in, cache);

		logger.info("setting creator participation...");
		logger.info("creatorGiveUp        = " + gs.isCreatorGiveUp());
		logger.info("creatorParticipation = " + gs.getCreatorParticipation());
		if(gs.getCreatorParticipation() == null)
		{
			gs.setCreatorParticipation(EnumCreatorParticipation.leave);
			logger.info("---------------------> " + gs.getCreatorParticipation());
		}

		logger.info("converting home maps...");
		User user;
		Map map;
		Team t;
		for(Entry<String, List<Map>> homeMapEntry : gs.getMapsByKey().entrySet())
		{
			user = cache.getUser(Integer.parseInt(homeMapEntry.getKey()));
			map = homeMapEntry.getValue().get(0);
			logger.debug("- " + user.getLogin() + " (" + user.getId() + ") -> " + map.getId());
			t = new Team(user.getLogin(), user, map);
			gs.getTeams().add(t);
		}
		logger.info("removing mapsByKey...");
		gs.getMapsByKey().clear();

		logger.info("adding home & guest properties");
		User home, guest, creator;
		User[] users;
		int[] index = new int[3];
		for(Entry<String, List<PlannedGame>> games : gs.getGames().entrySet())
		{
			for(PlannedGame g : games.getValue())
			{
				users = g.getPlayers().toArray(new User[3]);
				index[0] = g.getName().indexOf(users[0].getLogin());
				index[1] = g.getName().indexOf(users[1].getLogin());
				index[2] = g.getName().indexOf(users[2].getLogin());

				creator = null;
				home = null;
				guest = null;
				if(index[0] == -1)
				{
					creator = users[0];
					if(index[1] < index[2])
					{
						home = users[1];
						guest = users[2];
					}
					else
					{
						home = users[2];
						guest = users[1];
					}
				}
				else if(index[1] == -1)
				{
					creator = users[1];
					if(index[0] < index[2])
					{
						home = users[0];
						guest = users[2];
					}
					else
					{
						home = users[2];
						guest = users[0];
					}
				}
				else if(index[2] == -1)
				{
					creator = users[2];
					if(index[0] < index[1])
					{
						home = users[0];
						guest = users[1];
					}
					else
					{
						home = users[1];
						guest = users[0];
					}
				}
				else
				{
					logger.error("no index is 0");
				}

				logger.info(g.getName() + " --> \tcreator = " + (creator != null ? creator.getLogin() : null) + " \thome = " + (home != null ? home.getLogin() : null) + " \tguest = " + (guest != null ? guest.getLogin() : null));
				g.setHome(home.getLogin());
				g.setGuest(guest.getLogin());
				g.getPlayers().clear();
				g.getPlayers().add(home);
				g.getPlayers().add(guest);
				g.getPlayers().add(creator);
			}
		}

		logger.info("saving updated file:   " + out.getAbsolutePath());
		GameSeriesManager.store(gs, out);
	}
}
