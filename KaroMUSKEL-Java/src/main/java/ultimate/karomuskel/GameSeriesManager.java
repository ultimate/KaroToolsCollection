package ultimate.karomuskel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import muskel2.model.Direction;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.base.Identifiable;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.Options;
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

	@SuppressWarnings("deprecation")
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

	@SuppressWarnings("deprecation")
	public static GameSeries convert(muskel2.model.GameSeries gs2, KaroAPICache karoAPICache)
	{
		GameSeries gs = new GameSeries();
		// universal properties
		gs.setTitle(gs2.title);
		gs.setCreator(karoAPICache.getUser(gs2.creator.id));
		gs.setLoaded(true);
		gs.setPlayers(convert(gs2.players, User.class, karoAPICache));
		gs.setMaps(convert(gs2.maps, Map.class, karoAPICache));
		gs.setGames(convert(gs2.games, karoAPICache));
		gs.setRules(convert(gs2.rules));
		// TODO creator give up
		// TODO ignore invitable
		gs.setMapsByKey(null);
		gs.setPlayersByKey(null);
		gs.setRules(null);
		gs.setRulesByKey(null);
		gs.setSettings(null);
		gs.setTeams(null);
		gs.setTeamsByKey(null);
		// TODO
		return null;
	}

	public static <T extends Identifiable, T2 extends muskel2.model.help.Identifiable> List<T> convert(List<T2> list2, Class<T> cls, KaroAPICache karoAPICache)
	{
		if(list2 == null)
			return null;
		List<T> list = new ArrayList<>(list2.size());
		for(T2 o2 : list2)
			list.add(karoAPICache.get(cls, o2.getId()));
		return list;
	}

	@SuppressWarnings("deprecation")
	public static List<PlannedGame> convert(List<muskel2.model.Game> list2, KaroAPICache karoAPICache)
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
			pg.setOptions(convert(g2.rules).createOptions());
		}
		return list;
	}
	
	@SuppressWarnings("deprecation")
	public static Rules convert(muskel2.model.Rules r2)
	{
		Rules r = new Rules();
		r.setCPs(r2.checkpointsActivated);
		r.setGamesPerPlayer(r2.gamesPerPlayer);
		r.setMaxZzz(r2.maxZzz);
		r.setMinZzz(r2.minZzz);
		r.setNumberOfPlayers(0);
		r.setZzz(r2.zzz);
		r.setDirection(null);
		if(r2.direction == Direction.klassisch)
			r.setDirection(EnumGameDirection.classic);
		else if(r2.direction == Direction.Formula_1)
			r.setDirection(EnumGameDirection.formula1);
		else
			r.setDirection(EnumGameDirection.free);
		if(r2.crashingAllowed == true)
			r.setTC(EnumGameTC.allowed);
		else if(r2.crashingAllowed == false)
			r.setTC(EnumGameTC.forbidden);
		else
			r.setTC(EnumGameTC.free);
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