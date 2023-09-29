package ultimate.karomuskel.special;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumCreatorParticipation;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karoapi4j.model.extended.Team;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.Options;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.Planner;

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

				logger.info(g.getName() + " --> \tcreator = " + (creator != null ? creator.getLogin() : null) + " \thome = " + (home != null ? home.getLogin() : null) + " \tguest = "
						+ (guest != null ? guest.getLogin() : null));
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

	public static void restoreKLC(KaroAPICache cache, File in, File out) throws IOException
	{
		cache.refresh();

		logger.info("loading wiki file: " + in.getAbsolutePath());
		BufferedReader r = new BufferedReader(new FileReader(in));

		User creator = cache.getUser(2248);

		GameSeries gs = new GameSeries(EnumGameSeriesType.KLC);
		gs.setTitle("KLC Saison 18 - ${runde.x} - ${teams} auf Karte ${karte.id}");
		gs.setCreator(creator);
		gs.setCreatorParticipation(EnumCreatorParticipation.leave);
		gs.setIgnoreInvitable(true);
		gs.setRules(new Rules(2, 2, EnumGameTC.forbidden, true, EnumGameDirection.classic));
		gs.getSettings().put("defaultTitle", "KLC Saison xx - ${runde.x} - ${teams} auf Karte ${karte.id}");
		gs.getSettings().put("firstKORound", 16);
		gs.getSettings().put("groups", 4);
		gs.getSettings().put("leagues", 4);
		gs.getSettings().put("round", 35);

		Options options = gs.getRules().createOptions(null);

		List<PlannedGame> games = new ArrayList<>();
		gs.getGames().put("KLC.groupphase", games);
		gs.getPlayersByKey().put("group1", new ArrayList<>());
		gs.getPlayersByKey().put("group2", new ArrayList<>());
		gs.getPlayersByKey().put("group3", new ArrayList<>());
		gs.getPlayersByKey().put("group4", new ArrayList<>());
		gs.getPlayersByKey().put("league1", new ArrayList<>());
		gs.getPlayersByKey().put("league2", new ArrayList<>());
		gs.getPlayersByKey().put("league3", new ArrayList<>());
		gs.getPlayersByKey().put("league4", new ArrayList<>());

		int group = 0;

		String userStart = "{{Benutzer|";
		String userEnd = "}}";
		String leagueStart = "}} (";
		String leagueEnd = ")";
		String mapStart = "{{Karte|";
		String mapEnd = "}}";
		String gidStart = "{{GID|";
		String gidEnd = "}}";

		String line;
		User user, user2;
		Map map;
		PlannedGame pg;
		Game game;
		LinkedHashSet<User> players;
		String username, username2;
		int i1, i2, league, mapId, gid;
		while((line = r.readLine()) != null)
		{
			if(line.startsWith("=== Gruppe "))
			{
				group++;
				logger.debug("  --> group =" + group);
			}
			else if(line.startsWith("|style=\"text-align:right\"|"))
			{
				// |style="text-align:right"|1||style="text-align:left"|{{Benutzer|Thargor}} (4) ||{{Karte|214}}||0||0||0||0||0||0
				i1 = line.indexOf(userStart) + userStart.length();
				i2 = line.indexOf(userEnd, i1);
				username = line.substring(i1, i2);

				i1 = line.indexOf(leagueStart) + leagueStart.length();
				i2 = line.indexOf(leagueEnd, i1);
				league = Integer.parseInt(line.substring(i1, i2));

				i1 = line.indexOf(mapStart) + mapStart.length();
				i2 = line.indexOf(mapEnd, i1);
				mapId = Integer.parseInt(line.substring(i1, i2));

				logger.debug("  --> user  =" + username + "|" + league + "|" + mapId);

				user = cache.getUsersByLogin().get(username.toLowerCase());
				map = cache.getMap(mapId);

				gs.getPlayers().add(user);
				gs.getPlayersByKey().get("group" + group).add(user);
				gs.getPlayersByKey().get("league" + league).add(user);
				gs.getTeams().add(new Team(username, user, map));
			}
			else if(line.startsWith("| {{Benutzer|"))
			{
				logger.debug(line);
				// | {{Benutzer|HX}} (4) || {{Benutzer|Karaser}} (1) || {{Karte|54}} || || || {{GID|137188}}
				i1 = line.indexOf(userStart) + userStart.length();
				i2 = line.indexOf(userEnd, i1);
				username = line.substring(i1, i2);

				i1 = line.indexOf(userStart, i2) + userStart.length();
				i2 = line.indexOf(userEnd, i1);
				username2 = line.substring(i1, i2);

				i1 = line.indexOf(mapStart) + mapStart.length();
				i2 = line.indexOf(mapEnd, i1);
				mapId = Integer.parseInt(line.substring(i1, i2));

				i1 = line.indexOf(gidStart) + gidStart.length();
				i2 = line.indexOf(gidEnd, i1);
				gid = Integer.parseInt(line.substring(i1, i2));

				logger.debug("  --> game  =" + username + "|" + username2 + "|" + mapId + "|" + gid);

				user = cache.getUsersByLogin().get(username.toLowerCase());
				user2 = cache.getUsersByLogin().get(username2.toLowerCase());
				map = cache.getMap(mapId);
				game = cache.getGame(gid);

				logger.debug(user.getId());
				logger.debug(user2.getId());

				players = new LinkedHashSet<>();
				players.add(user);
				players.add(user2);
				players.add(creator);

				pg = new PlannedGame(game.getName(), map, players, options, null);
				pg.setGame(game);
				pg.setCreated(true);
				pg.setLeft(true);

				gs.getGames().get("KLC.groupphase").add(pg);
			}
		}

		r.close();

		logger.info("saving updated file:   " + out.getAbsolutePath());
		GameSeriesManager.store(gs, out);
	}

	public static void updateCCC6_remove_DerFlieger(KaroAPICache cache, File in, File out) throws IOException
	{
		GameSeries gs = GameSeriesManager.load(in, cache);
		
		logger.debug("playersBefore:       " + gs.getPlayers().size());

		List<User> playersUpdated = new ArrayList<>(gs.getPlayers());
		playersUpdated.removeIf(u -> {
			return u.getLogin().equalsIgnoreCase("DerFlieger");
		});
		logger.debug("playersUpdated:      " + playersUpdated.size());
		
		List<PlannedGame> plannedGames = gs.getGames().get("Balanced");		
		logger.debug("plannedGamesBefore:  " + plannedGames.size());
		
		plannedGames.removeIf(pg -> { return !pg.isCreated(); });
		logger.debug("plannedGamesStarted:  " + plannedGames.size());

		List<Map> mapsStarted = new ArrayList<>();
		for(PlannedGame pg: plannedGames)
		{
			if(!mapsStarted.contains(pg.getMap()))
				mapsStarted.add(pg.getMap());
		}
		logger.debug("mapsStarted:          " + mapsStarted.size());
		
		Rules r;
		for(int c = mapsStarted.size(); c < gs.getRulesByKey().size(); c++)
		{
			r = gs.getRulesByKey().get("" + c);
			if(r.getNumberOfPlayers() == 5)
				r.setNumberOfPlayers(4);
		}

		List<PlannedGame> plannedGamesNew = Planner.planSeriesBalanced(gs.getTitle(), gs.getCreator(), playersUpdated, gs.getMapsByKey(), gs.getRulesByKey(), null, gs.getCreatorParticipation());
		logger.debug("plannedGamesNew:     " + plannedGamesNew.size());		
		
		plannedGamesNew.removeIf(pg -> { return mapsStarted.contains(pg.getMap()); });
		logger.debug("plannedGamesChanged:  " + plannedGamesNew.size());
		
		plannedGames.addAll(plannedGamesNew);
		logger.debug("plannedGamesUpdated:  " + plannedGames.size());
		
		GameSeriesManager.store(gs, out);
	}
}
