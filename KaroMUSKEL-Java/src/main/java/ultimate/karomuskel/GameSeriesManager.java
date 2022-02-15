package ultimate.karomuskel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.JButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import muskel2.model.Direction;
import muskel2.model.series.AllCombinationsGameSeries;
import muskel2.model.series.BalancedGameSeries;
import muskel2.model.series.KLCGameSeries;
import muskel2.model.series.KOGameSeries;
import muskel2.model.series.LeagueGameSeries;
import muskel2.model.series.SimpleGameSeries;
import muskel2.model.series.TeamBasedGameSeries;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.base.Identifiable;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karoapi4j.model.extended.Team;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.JSONUtil;
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.screens.GroupWinnersScreen;
import ultimate.karomuskel.ui.screens.HomeMapsScreen;
import ultimate.karomuskel.ui.screens.KOWinnersScreen;
import ultimate.karomuskel.ui.screens.MapsAndRulesScreen;
import ultimate.karomuskel.ui.screens.MapsScreen;
import ultimate.karomuskel.ui.screens.PlayersScreen;
import ultimate.karomuskel.ui.screens.RulesScreen;
import ultimate.karomuskel.ui.screens.SettingsScreen;
import ultimate.karomuskel.ui.screens.SummaryScreen;

@SuppressWarnings("deprecation")
public abstract class GameSeriesManager
{
	/**
	 * Logger-Instance
	 */
	private static final Logger	logger	= LoggerFactory.getLogger(GameSeriesManager.class);
	public static final String	CHARSET	= "UTF-8";
	private static Properties	config;

	/**
	 * Prevent instantiation
	 */
	private GameSeriesManager()
	{

	}

	public static String getStringConfig(String key)
	{
		return config.getProperty(key);
	}

	public static int getIntConfig(String key)
	{
		return Integer.parseInt(getStringConfig(key));
	}

	public static String getStringConfig(EnumGameSeriesType gsType, String key)
	{
		if(gsType == null)
			throw new IllegalArgumentException("invalid GameSeries type");
		return getStringConfig("gameseries." + gsType.toString().toLowerCase() + "." + key);
	}

	public static int getIntConfig(EnumGameSeriesType gsType, String key)
	{
		return Integer.parseInt(getStringConfig(gsType, key));
	}

	public static String getDefaultTitle(EnumGameSeriesType gsType)
	{
		return getStringConfig(gsType, "defaultTitle");
	}

	public static GameSeries load(File file, KaroAPICache karoAPICache) throws IOException, ClassNotFoundException, ClassCastException
	{
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		byte[] bytes = bis.readAllBytes();
		bis.close();
		fis.close();

		boolean v2 = (bytes[0] != '}');
		if(v2)
		{
			return convert(loadV2(file), karoAPICache);
		}
		else
		{
			String content = new String(bytes, CHARSET);
			JSONUtil.setLookUp(karoAPICache);
			GameSeries gs = JSONUtil.deserialize(content, new TypeReference<GameSeries>() {});
			gs.setLoaded(true);
			return gs;
		}
	}

	public static muskel2.model.GameSeries loadV2(File file) throws IOException, ClassNotFoundException, ClassCastException
	{
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ObjectInputStream ois = new ObjectInputStream(bis);

		muskel2.model.GameSeries gs2 = (muskel2.model.GameSeries) ois.readObject();

		ois.close();
		bis.close();
		fis.close();

		return gs2;
	}

	public static GameSeries convert(muskel2.model.GameSeries gs2, KaroAPICache karoAPICache)
	{
		GameSeries gs = new GameSeries();
		// universal properties
		gs.setTitle(gs2.title);
		gs.setCreator(karoAPICache.getUser(gs2.creator.id));
		gs.setLoaded(true);
		gs.setPlayers(convert(gs2.players, User.class, karoAPICache));
		gs.setMaps(convert(gs2.maps, Map.class, karoAPICache));
		gs.setGames(convertGames(gs2.games, karoAPICache));
		gs.setRules(convert(gs2.rules));
		gs.setCreatorGiveUp(gs2.rules.creatorGiveUp);
		gs.setIgnoreInvitable(gs2.rules.ignoreInvitable);
		// type specific properties
		if(gs2 instanceof SimpleGameSeries)
		{
			gs.setType(EnumGameSeriesType.Simple);
			gs.setTeamBased(false);
			gs.set(GameSeries.NUMBER_OF_GAMES, ((SimpleGameSeries) gs2).numberOfGames);
			gs.set(GameSeries.MIN_PLAYERS_PER_GAME, ((SimpleGameSeries) gs2).minPlayersPerGame);
			gs.set(GameSeries.MAX_PLAYERS_PER_GAME, ((SimpleGameSeries) gs2).maxPlayersPerGame);
		}
		else if(gs2 instanceof BalancedGameSeries)
		{
			gs.setType(EnumGameSeriesType.Balanced);
			gs.setTeamBased(false);
			gs.set(GameSeries.NUMBER_OF_MAPS, ((BalancedGameSeries) gs2).numberOfMaps);
			gs.setMapsByKey(convert(((BalancedGameSeries) gs2).mapList, Map.class, karoAPICache));
			gs.setRulesByKey(convert(((BalancedGameSeries) gs2).rulesList));
			// ((BalancedGameSeries) gs2).shuffledPlayers // ignore
		}
		else if(gs2 instanceof KLCGameSeries)
		{
			gs.setType(EnumGameSeriesType.KLC);
			gs.setTeamBased(false);
			gs.set(GameSeries.NUMBER_OF_GROUPS, KLCGameSeries.GROUPS);
			gs.set(GameSeries.NUMBER_OF_LEAGUES, KLCGameSeries.LEAGUES);
			gs.set(GameSeries.CURRENT_ROUND, ((KLCGameSeries) gs2).round);
			gs.setPlayers(convert(((KLCGameSeries) gs2).allPlayers, User.class, karoAPICache));
			gs.getPlayersByKey().put("league1", convert(((KLCGameSeries) gs2).playersLeague1, User.class, karoAPICache));
			gs.getPlayersByKey().put("league2", convert(((KLCGameSeries) gs2).playersLeague2, User.class, karoAPICache));
			gs.getPlayersByKey().put("league3", convert(((KLCGameSeries) gs2).playersLeague3, User.class, karoAPICache));
			gs.getPlayersByKey().put("league4", convert(((KLCGameSeries) gs2).playersLeague4, User.class, karoAPICache));
			gs.getPlayersByKey().put("group1", convert(((KLCGameSeries) gs2).playersGroup1, User.class, karoAPICache));
			gs.getPlayersByKey().put("group2", convert(((KLCGameSeries) gs2).playersGroup2, User.class, karoAPICache));
			gs.getPlayersByKey().put("group3", convert(((KLCGameSeries) gs2).playersGroup3, User.class, karoAPICache));
			gs.getPlayersByKey().put("group4", convert(((KLCGameSeries) gs2).playersGroup4, User.class, karoAPICache));
			gs.getPlayersByKey().put("group5", convert(((KLCGameSeries) gs2).playersGroup5, User.class, karoAPICache));
			gs.getPlayersByKey().put("group6", convert(((KLCGameSeries) gs2).playersGroup6, User.class, karoAPICache));
			gs.getPlayersByKey().put("group7", convert(((KLCGameSeries) gs2).playersGroup7, User.class, karoAPICache));
			gs.getPlayersByKey().put("group8", convert(((KLCGameSeries) gs2).playersGroup8, User.class, karoAPICache));
			gs.getPlayersByKey().put("roundOf16", convert(((KLCGameSeries) gs2).playersRoundOf16, User.class, karoAPICache));
			gs.getPlayersByKey().put("roundOf8", convert(((KLCGameSeries) gs2).playersRoundOf8, User.class, karoAPICache));
			gs.getPlayersByKey().put("roundOf4", convert(((KLCGameSeries) gs2).playersRoundOf4, User.class, karoAPICache));
			gs.getPlayersByKey().put("roundOf2", convert(((KLCGameSeries) gs2).playersRoundOf2, User.class, karoAPICache));
			gs.setMapsByKey(convertHomeMaps(((KLCGameSeries) gs2).homeMaps, Map.class, karoAPICache));
		}
		else if(gs2 instanceof TeamBasedGameSeries)
		{
			gs.setTeamBased(true);
			gs.set(GameSeries.NUMBER_OF_TEAMS, ((TeamBasedGameSeries) gs2).numberOfTeams);
			gs.set(GameSeries.MIN_PLAYERS_PER_TEAM, ((TeamBasedGameSeries) gs2).minPlayersPerTeam);
			gs.set(GameSeries.MAX_PLAYERS_PER_TEAM, ((TeamBasedGameSeries) gs2).maxPlayersPerTeam);
			gs.set(GameSeries.NUMBER_OF_GAMES_PER_PAIR, ((TeamBasedGameSeries) gs2).numberOfGamesPerPair);
			gs.set(GameSeries.USE_HOME_MAPS, ((TeamBasedGameSeries) gs2).useHomeMaps);
			gs.set(GameSeries.SHUFFLE_TEAMS, ((TeamBasedGameSeries) gs2).shuffleTeams);
			gs.set(GameSeries.AUTO_NAME_TEAMS, ((TeamBasedGameSeries) gs2).autoNameTeams);
			gs.set(GameSeries.ALLOW_MULTIPLE_TEAMS, ((TeamBasedGameSeries) gs2).multipleTeams);
			gs.set(GameSeries.USE_CREATOR_TEAM, ((TeamBasedGameSeries) gs2).creatorTeam);
			gs.setTeams(convertTeams(((TeamBasedGameSeries) gs2).teams, karoAPICache));
			gs.getTeamsByKey().put("shuffled", convertTeams(((TeamBasedGameSeries) gs2).teams, karoAPICache));

			if(gs2 instanceof AllCombinationsGameSeries)
			{
				gs.setType(EnumGameSeriesType.AllCombinations);
				gs.set(GameSeries.NUMBER_OF_TEAMS_PER_MATCH, ((AllCombinationsGameSeries) gs2).numberOfTeamsPerMatch);
			}
			else if(gs2 instanceof KOGameSeries)
			{
				gs.setType(EnumGameSeriesType.KO);
				// no additional properties
			}
			else if(gs2 instanceof LeagueGameSeries)
			{
				gs.setType(EnumGameSeriesType.League);
				// no additional properties
			}
			else
			{
				logger.error("unknown type: " + gs2.getClass());
			}
		}
		else
		{
			logger.error("unknown type: " + gs2.getClass());
		}

		return gs;
	}

	protected static <T extends Identifiable, T2 extends muskel2.model.help.Identifiable> List<T> convert(List<T2> list2, Class<T> cls, KaroAPICache karoAPICache)
	{
		if(list2 == null)
			return null;
		List<T> list = new ArrayList<>(list2.size());
		for(T2 o2 : list2)
			list.add(karoAPICache.get(cls, o2.getId()));
		return list;
	}

	protected static List<PlannedGame> convertGames(List<muskel2.model.Game> list2, KaroAPICache karoAPICache)
	{
		if(list2 == null)
			return null;
		List<PlannedGame> list = new ArrayList<>(list2.size());
		for(muskel2.model.Game g2 : list2)
		{
			Game g = karoAPICache.get(Game.class, g2.getId());

			PlannedGame pg = new PlannedGame();
			pg.setName(g2.name);
			pg.setCreated(g2.created);
			pg.setLeft(g2.left);
			pg.setPlayers(convert(g2.players, User.class, karoAPICache));
			pg.setGame(g);
			pg.setMap(g != null ? g.getMap() : karoAPICache.getMap(g2.getId()));
			pg.setOptions(convert(g2.rules).createOptions(null));

			list.add(pg);
		}
		return list;
	}

	protected static List<Team> convertTeams(List<muskel2.model.help.Team> list2, KaroAPICache karoAPICache)
	{
		if(list2 == null)
			return null;
		List<Team> list = new ArrayList<>(list2.size());
		for(muskel2.model.help.Team t2 : list2)
		{
			Team t = new Team(t2.name, convert(t2.players, User.class, karoAPICache));
			if(t2.homeMap != null)
				t.setHomeMap(karoAPICache.getMap(t2.homeMap.id));
			list.add(t);
		}
		return list;
	}

	protected static <T extends Identifiable> java.util.Map<String, List<T>> convertHomeMaps(java.util.Map<Integer, Integer> map2, Class<T> cls, KaroAPICache karoAPICache)
	{
		if(map2 == null)
			return null;
		HashMap<String, List<T>> map = new HashMap<>();
		for(Entry<Integer, Integer> e : map2.entrySet())
			map.put(e.getKey().toString(), Arrays.asList(karoAPICache.get(cls, e.getValue())));
		return map;
	}

	protected static <T extends Identifiable, T2 extends muskel2.model.help.Identifiable> java.util.Map<String, List<T>> convert(java.util.Map<Integer, T2> map2, Class<T> cls,
			KaroAPICache karoAPICache)
	{
		if(map2 == null)
			return null;
		HashMap<String, List<T>> map = new HashMap<>();
		for(Entry<Integer, T2> e : map2.entrySet())
			map.put(e.getKey().toString(), Arrays.asList(karoAPICache.get(cls, e.getValue().getId())));
		return map;
	}

	protected static java.util.Map<String, Rules> convert(java.util.Map<Integer, muskel2.model.Rules> map2)
	{
		if(map2 == null)
			return null;
		HashMap<String, Rules> map = new HashMap<>();
		for(Entry<Integer, muskel2.model.Rules> e : map2.entrySet())
			map.put(e.getKey().toString(), convert(e.getValue()));
		return map;
	}

	protected static Rules convert(muskel2.model.Rules r2)
	{
		Rules r = new Rules();
		r.setCPs(r2.checkpointsActivated);
		r.setGamesPerPlayer(r2.gamesPerPlayer);
		r.setMaxZzz(r2.maxZzz);
		r.setMinZzz(r2.minZzz);
		r.setNumberOfPlayers(0);
		r.setStartdirection(null);
		if(r2.direction == Direction.klassisch)
			r.setStartdirection(EnumGameDirection.classic);
		else if(r2.direction == Direction.Formula_1)
			r.setStartdirection(EnumGameDirection.formula1);
		else if(r2.direction == Direction.egal)
			r.setStartdirection(EnumGameDirection.free);
//		else
//			r.setStartdirection(null);
		if(r2.crashingAllowed == null)
			r.setCrashallowed(null);
		else if(r2.crashingAllowed == true)
			r.setCrashallowed(EnumGameTC.allowed);
		else if(r2.crashingAllowed == false)
			r.setCrashallowed(EnumGameTC.forbidden);
		return r;
	}

	public static void store(GameSeries gs, File file) throws IOException
	{
		String json = JSONUtil.serialize(gs);

		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos);

		bos.write(json.getBytes(CHARSET));

		bos.flush();
		bos.close();
		fos.close();
	}

	public static LinkedList<Screen> initScreens(GameSeries gs, KaroAPICache karoAPICache, Screen startScreen, JButton previousButton, JButton nextButton, boolean loaded)
	{
		LinkedList<Screen> screens = new LinkedList<>();
		if(loaded)
		{
			SummaryScreen s = new SummaryScreen(startScreen, karoAPICache, previousButton, nextButton);
			s.setSkipPlan(true);
			screens.add(s);

			if(gs.getType() == EnumGameSeriesType.KLC)
			{
				int groups = (int) gs.get("groups");
				int leagues = (int) gs.get("leagues");
				int players = (int) groups * leagues;

				int round = (int) gs.get("round");
				screens.getLast().setNextKey("screen.summary.nextko");
				if(round == players)
				{
					screens.add(new GroupWinnersScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new SummaryScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
				}
				else if(round > 2)
				{
					screens.add(new KOWinnersScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new SummaryScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
				}
				round = round / 2;
				gs.set("round", round);
			}
			else if(gs.getType() == EnumGameSeriesType.KO)
			{
				int round = (int) gs.get("round");

				screens.getLast().setNextKey("screen.summary.nextko");
				if(round > 2)
				{
					screens.add(new KOWinnersScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new SummaryScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
				}
			}
		}
		else
		{
			switch(gs.getType())
			{
				case AllCombinations:
				case KO:
				case League:
					screens.add(new SettingsScreen(startScreen, karoAPICache, previousButton, nextButton));
					screens.add(new RulesScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new PlayersScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new HomeMapsScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new MapsScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new SummaryScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					break;
				case Balanced:
					screens.add(new SettingsScreen(startScreen, karoAPICache, previousButton, nextButton));
					screens.add(new RulesScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new PlayersScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new MapsAndRulesScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new SummaryScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					break;
				case KLC:
					screens.add(new SettingsScreen(startScreen, karoAPICache, previousButton, nextButton));
					screens.add(new RulesScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new PlayersScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new HomeMapsScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.getLast().setNextKey("screen.homemaps.nextskip");
					screens.add(new SummaryScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					break;
				case Simple:
					screens.add(new SettingsScreen(startScreen, karoAPICache, previousButton, nextButton));
					screens.add(new RulesScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new PlayersScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new MapsScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new SummaryScreen(screens.getLast(), karoAPICache, previousButton, nextButton));
					break;
			}
		}
		return screens;
	}
}