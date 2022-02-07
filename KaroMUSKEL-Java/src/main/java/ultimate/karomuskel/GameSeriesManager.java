package ultimate.karomuskel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.Screen;

public abstract class GameSeriesManager
{
	/**
	 * Logger-Instance
	 */
	private static final Logger	logger	= LoggerFactory.getLogger(GameSeriesManager.class);
	private static Properties	config;

	/**
	 * Prevent instantiation
	 */
	private GameSeriesManager()
	{

	}

	public static final HashMap<String, Setting<?>> SETTINGS;

	static
	{
		SETTINGS = new HashMap<>();
		addSetting(new Setting<>("numberOfTeamsPerMatch", int.class, EnumGameSeriesType.AllCombinations));

		try
		{
			config = PropertiesUtil.loadProperties(new File("config.properties"));
		}
		catch(IOException e)
		{
			logger.error("error loading config.properties", e);
			config = null;
		}
	}

	private static void addSetting(Setting<?> s)
	{
		SETTINGS.put(s.getKey(), s);
	}

	public static <T> void set(GameSeries gs, String key, T value)
	{
		Setting<?> s = SETTINGS.get(key);
		if(s == null || !s.isApplicable(gs))
			throw new IllegalArgumentException("invalid key '" + key + "' for GameSeries type " + gs.getType());
		if(value != null && !s.getValueType().isAssignableFrom(value.getClass()))
			throw new IllegalArgumentException("invalid value '" + value + "' for key '" + key + "'");
		gs.getSettings().put(key, value);
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(GameSeries gs, String key)
	{
		Setting<T> s = (Setting<T>) SETTINGS.get(key);
		if(s == null || !s.isApplicable(gs))
			throw new IllegalArgumentException("invalid key '" + key + "' for GameSeries type " + gs.getType());

		return (T) gs.getSettings().get(key);
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
		return getStringConfig("gameseries." + gsType.toString().toLowerCase() + "."  + key);
	}

	public static int getIntConfig(EnumGameSeriesType gsType, String key)
	{
		return Integer.parseInt(getStringConfig(gsType, key));
	}

	public static String getDefaultTitle(EnumGameSeriesType gsType)
	{
		return getStringConfig(gsType, "defaultTitle");
	}

	private static class Setting<T>
	{
		private String						key;
		private Class<T>					valueType;
		private List<EnumGameSeriesType>	applicableGamesSeriesType;

		public Setting(String key, Class<T> valueType, EnumGameSeriesType... applicableGamesSeriesType)
		{
			super();
			this.key = key;
			this.valueType = valueType;
			this.applicableGamesSeriesType = Arrays.asList(applicableGamesSeriesType);
		}

		public String getKey()
		{
			return key;
		}

		public Class<T> getValueType()
		{
			return valueType;
		}

		public boolean isApplicable(GameSeries gs)
		{
			return applicableGamesSeriesType.contains(gs.getType());
		}
	}

	public static GameSeries create(String type)
	{
		// TODO
		return null;
	}

	public static GameSeries load(File file) throws IOException, ClassNotFoundException, ClassCastException
	{
		// TODO check for json vs. old
		boolean v2 = true;
		if(v2)
		{
			return convert(loadV2(file));
		}
		else
		{
			// setLoaded(true);
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	public static muskel2.model.GameSeries loadV2(File file) throws IOException, ClassNotFoundException, ClassCastException
	{
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ObjectInputStream ois = new ObjectInputStream(bis);

		muskel2.model.GameSeries gs2 = (muskel2.model.GameSeries) ois.readObject();
		gs2.setLoaded(true);

		ois.close();
		bis.close();
		fis.close();

		return gs2;
	}

	@SuppressWarnings("deprecation")
	public static GameSeries convert(muskel2.model.GameSeries gs2)
	{
		// TODO
		return null;
	}

	public static boolean store(GameSeries gs, File file)
	{
		// TODO
		return false;
	}

	public static Screen initScreens(GameSeries gs, KaroAPICache karoAPICache, Screen startScreen, JButton previousButton, JButton nextButton, boolean loaded)
	{
		// TODO
		return null;
	}
}