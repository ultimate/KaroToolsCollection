package ultimate.karomuskel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.model.extended.GameSeries;
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

	public static GameSeries load(File file) throws IOException, ClassNotFoundException, ClassCastException
	{
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		byte[] bytes = bis.readAllBytes();
		bis.close();
		fis.close();

		boolean v2 = (bytes[0] != '}');
		if(v2)
		{
			return convert(loadV2(file));
		}
		else
		{
			String content = new String(bytes, CHARSET);
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
	public static GameSeries convert(muskel2.model.GameSeries gs2)
	{
		GameSeries gs = new GameSeries();
//		gs.setCreator(gs2.creator.id); // TODO
		// TODO
		return null;
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