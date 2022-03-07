package ultimate.karomuskel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;

import muskel2.model.Direction;
import muskel2.model.series.AllCombinationsGameSeries;
import muskel2.model.series.BalancedGameSeries;
import muskel2.model.series.KLCGameSeries;
import muskel2.model.series.KOGameSeries;
import muskel2.model.series.LeagueGameSeries;
import muskel2.model.series.SimpleGameSeries;
import muskel2.model.series.TeamBasedGameSeries;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.exceptions.GameSeriesException;
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
import ultimate.karomuskel.ui.screens.StartScreen;
import ultimate.karomuskel.ui.screens.SummaryScreen;

/**
 * Utility class for storing and loading {@link GameSeries}.<br>
 * This class also offers backwards compatibility for {@link muskel2.model.GameSeries} via
 * {@link GameSeriesManager#convert(muskel2.model.GameSeries, KaroAPICache)}.<br>
 * <br>
 * Since some {@link GameSeries} require constants for configuration, this class also provides access to the configuration of those constants via an
 * external {@link Properties} file. This file needs to be set on start of the program via {@link GameSeriesManager#setConfig(Properties)} and then it
 * can be accessed via
 * <ul>
 * <li>{@link GameSeriesManager#getIntConfig(String)}</li>
 * <li>{@link GameSeriesManager#getStringConfig(String)}</li>
 * <li>{@link GameSeriesManager#getIntConfig(EnumGameSeriesType, String)}</li>
 * <li>{@link GameSeriesManager#getStringConfig(EnumGameSeriesType, String)}</li>
 * </ul>
 * 
 * @author ultimate
 */
@SuppressWarnings("deprecation")
public abstract class GameSeriesManager
{
	/**
	 * Logger-Instance
	 */
	protected static transient final Logger	logger							= LogManager.getLogger();
	/**
	 * The charset for the JSON storage of {@link GameSeries}
	 */
	public static final String				CHARSET							= "UTF-8";
	/**
	 * The prefix for the GameSeries configurations
	 */
	public static final String				CONFIG_GAMESERIES_PREFIX		= "gameseries";
	/**
	 * The config delimiter ('.')
	 */
	public static final char				CONFIG_DELIMITER				= '.';
	/**
	 * The config key for "allow creator give up"
	 */
	public static final String				CONFIG_ALLOW_CREATOR_GIVE_UP	= CONFIG_GAMESERIES_PREFIX + CONFIG_DELIMITER + "allow.creatorGiveUp";
	/**
	 * The config key for "allow ignore invitable"
	 */
	public static final String				CONFIG_ALLOW_IGNORE_INVITABLE	= CONFIG_GAMESERIES_PREFIX + CONFIG_DELIMITER + "allow.ignoreInvitable";
	/**
	 * The config file to use
	 */
	private static Properties				config;

	/**
	 * Prevent instantiation
	 */
	private GameSeriesManager()
	{

	}

	/**
	 * Set the config {@link Properties}
	 * 
	 * @param config - the properties
	 */
	public static void setConfig(Properties config)
	{
		GameSeriesManager.config = config;
	}

	/**
	 * Get the config {@link Properties}
	 * 
	 * @return config
	 */
	public static Properties getConfig()
	{
		return config;
	}

	/**
	 * Get a string config by key
	 * 
	 * @param key - the key
	 * @return the config value as String
	 */
	public static String getStringConfig(String key)
	{
		return config.getProperty(key);
	}

	/**
	 * Get a int config by key
	 * 
	 * @param key - the key
	 * @return the config value as int
	 */
	public static int getIntConfig(String key)
	{
		try
		{
			return Integer.parseInt(getStringConfig(key));
		}
		catch(NumberFormatException e)
		{
			return 0;
		}
	}

	/**
	 * Get a boolean config by key
	 * 
	 * @param key - the key
	 * @return the config value as boolean
	 */
	public static boolean getBooleanConfig(String key)
	{
		return Boolean.parseBoolean(getStringConfig(key));
	}

	/**
	 * Get a string config by key for a given {@link EnumGameSeriesType}. This is convienence for
	 * <code>getStringConfig("gameseries." + gsType.toString().toLowerCase() + "." + key);</code>
	 * 
	 * @see GameSeriesManager#getStringConfig(String)
	 * @param gs - the {@link GameSeries}
	 * @param key - the key
	 * @return the config value as String
	 */
	public static String getStringConfig(GameSeries gs, String key)
	{
		if(gs == null || gs.getType() == null)
			throw new IllegalArgumentException("invalid GameSeries");

		if(gs.getSettings().containsKey(key)) // default to settings first
			return gs.get(key).toString();

		return getStringConfig(CONFIG_GAMESERIES_PREFIX + CONFIG_DELIMITER + gs.getType().toString().toLowerCase() + CONFIG_DELIMITER + key);
	}

	/**
	 * Get a int config by key for a given {@link EnumGameSeriesType}. This is convienence for
	 * <code>Integer.parseInt(getStringConfig("gameseries." + gsType.toString().toLowerCase() + "." + key));</code>
	 * 
	 * @see GameSeriesManager#getIntConfig(String)
	 * @param gs - the {@link GameSeries}
	 * @param key - the key
	 * @return the config value as int
	 */
	public static int getIntConfig(GameSeries gs, String key)
	{
		try
		{
			return Integer.parseInt(getStringConfig(gs, key));
		}
		catch(NumberFormatException e)
		{
			return 0;
		}
	}

	/**
	 * Get a int config by key for a given {@link EnumGameSeriesType}. This is convienence for
	 * <code>Integer.parseInt(getStringConfig("gameseries." + gsType.toString().toLowerCase() + "." + key));</code>
	 * 
	 * @see GameSeriesManager#getIntConfig(String)
	 * @param gs - the {@link GameSeries}
	 * @param key - the key
	 * @return the config value as boolean
	 */
	public static boolean getBooleanConfig(GameSeries gs, String key)
	{
		return Boolean.parseBoolean(getStringConfig(gs, key));
	}

	/**
	 * Get the default title a given {@link EnumGameSeriesType}. This is convienence for
	 * <code>getStringConfig(gsType, "defaultTitle");</code>
	 * 
	 * @see GameSeriesManager#getStringConfig(GameSeries, String)
	 * @param gs - the {@link GameSeries}
	 * @return the config value as String
	 */
	public static String getDefaultTitle(GameSeries gs)
	{
		return getStringConfig(gs, "defaultTitle");
	}

	/**
	 * Get the minimum number of players that a map needs to support for the given type of {@link GameSeries}
	 * 
	 * @param gs - the {@link GameSeries}
	 * @return the min number of players per map
	 */
	public static int getMinSupportedPlayersPerMap(GameSeries gs)
	{
		switch(gs.getType())
		{
			case AllCombinations:
			case KO:
			case League:
				return (int) gs.get(GameSeries.MAX_PLAYERS_PER_TEAM) * 2 + 1;
			case KLC:
				return 3;
			case Simple:
				return (int) gs.get(GameSeries.MIN_PLAYERS_PER_GAME);
			case Balanced:
			default:
				return 0;
		}
	}

	/**
	 * Is the given {@link GameSeries} teambased?<br>
	 * Note: under normal conditions the teambased flag only depends on the {@link EnumGameSeriesType} of the {@link GameSeries} (simple switch
	 * statement). But to ensure backwards compatibility in case of changes to the type, this method first checks for a setting "teamBased" in the
	 * gameSeries, which will be set during conversion of old GameSeries.
	 * 
	 * @param gs - the {@link GameSeries}
	 * @return true or false
	 */
	public static boolean isTeamBased(GameSeries gs)
	{
		if(gs.getSettings().containsKey(GameSeries.V2_TEAM_BASED))
			return (boolean) gs.get(GameSeries.V2_TEAM_BASED);

		switch(gs.getType())
		{
			case AllCombinations:
			case KO:
			case League:
				return true;
			case Balanced:
			case KLC:
			case Simple:
			default:
				return false;
		}
	}

	/**
	 * Save a {@link GameSeries} to JSON using the {@link JSONUtil}.<br>
	 * Note: Storing of {@link muskel2.model.GameSeries} is not supported. Please convert it to the new format via
	 * {@link GameSeriesManager#convert(muskel2.model.GameSeries, KaroAPICache)} first.
	 * 
	 * @see JSONUtil#serialize(Object)
	 * @param gs - the {@link GameSeries}
	 * @param file - the {@link File} to store the JSON to
	 * @throws IOException - if storing fails
	 */
	public static void store(GameSeries gs, File file) throws IOException
	{
		logger.debug("copying config to settings");
		String prefix = CONFIG_GAMESERIES_PREFIX + CONFIG_DELIMITER + gs.getType().toString().toLowerCase() + CONFIG_DELIMITER;
		String settingsKey;
		String valueS;
		for(Object key : config.keySet())
		{
			if(((String) key).toLowerCase().startsWith(prefix))
			{
				settingsKey = ((String) key).substring(prefix.length());
				valueS = getStringConfig(gs, settingsKey);
				try
				{
					gs.set(settingsKey, Integer.parseInt(valueS));
				}
				catch(NumberFormatException e)
				{
					gs.set(prefix, valueS);
				}
			}
		}

		logger.info("storing GameSeries to file: " + file.getAbsolutePath());

		String json = JSONUtil.serialize(gs, true);

		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos);

		bos.write(json.getBytes(CHARSET));

		bos.flush();
		bos.close();
		fos.close();
	}

	/**
	 * Load a {@link GameSeries} from a given {@link File}.<br>
	 * References in the JSON file will be resolved using the given {@link KaroAPICache}.<br>
	 * Note: this method is also capable of loading V2 {@link muskel2.model.GameSeries}. Those entities will then automatically be converted to the
	 * new format using {@link GameSeriesManager#convert(muskel2.model.GameSeries, KaroAPICache)}
	 * 
	 * @param file - the {@link File} that contains the {@link GameSeries} or the V2 {@link muskel2.model.GameSeries}
	 * @param karoAPICache - the {@link KaroAPICache} to resolve references
	 * @return the {@link GameSeries}
	 * @throws IOException - if deserialization or loading fails
	 */
	public static GameSeries load(File file, KaroAPICache karoAPICache) throws IOException
	{
		logger.info("loading GameSeries from file: " + file.getAbsolutePath());

		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		byte[] bytes = bis.readAllBytes();
		bis.close();
		fis.close();

		boolean v2 = (bytes[0] != '{');
		if(v2)
		{
			try
			{
				return convert(loadV2(file), karoAPICache);
			}
			catch(ClassNotFoundException e)
			{
				throw new IOException(e);
			}
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

	/**
	 * Load a V2 {@link muskel2.model.GameSeries} (for backwards compatibility)
	 * 
	 * @param file - the {@link File} that contains the V2 {@link muskel2.model.GameSeries}
	 * @return the {@link muskel2.model.GameSeries}
	 * @throws IOException - if deserialization or loading fails
	 * @throws ClassNotFoundException - if loading the class fails
	 */
	public static muskel2.model.GameSeries loadV2(File file) throws IOException, ClassNotFoundException
	{
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ObjectInputStream ois = new ObjectInputStream(bis);

		muskel2.model.GameSeries gs2;

		try
		{
			gs2 = (muskel2.model.GameSeries) ois.readObject();
		}
		catch(InvalidClassException e)
		{
			// try loading SerialVersionUID=1
			logger.warn(e.getMessage());
			if(e.getMessage().equals("muskel2.model.GameSeries; local class incompatible: stream classdesc serialVersionUID = 1, local class serialVersionUID = 2"))
				return loadV2_serialVersionUID1(file);
			else
				throw e;
		}
		finally
		{
			ois.close();
			bis.close();
			fis.close();
		}

		return gs2;
	}

	/**
	 * Load a V2 {@link muskel2.model.GameSeries} (for backwards compatibility) with serialVersionUID = 1
	 * 
	 * @param file - the {@link File} that contains the V2 {@link muskel2.model.GameSeries}
	 * @return the {@link muskel2.model.GameSeries}
	 * @throws IOException - if deserialization or loading fails
	 * @throws ClassNotFoundException - if loading the class fails
	 */
	public static muskel2.model.GameSeries loadV2_serialVersionUID1(File file) throws IOException, ClassNotFoundException
	{
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		CustomInputStream ois = new CustomInputStream(bis);

		muskel2.model.GameSeries gs2;

		try
		{
			gs2 = (muskel2.model.GameSeries) ois.readObject();
		}
		finally
		{
			ois.close();
			bis.close();
			fis.close();
		}

		return gs2;
	}

	/**
	 * @see https://stackoverflow.com/questions/1816559/make-java-runtime-ignore-serialversionuids
	 */
	private static class CustomInputStream extends ObjectInputStream
	{
		ClassLoader loader;

		public CustomInputStream(InputStream in) throws IOException
		{
			super(in);

			ClassLoader prevCl = Thread.currentThread().getContextClassLoader();
			// Create the class loader by using the given URL; use prevCl as parent to maintain current visibility
			loader = URLClassLoader.newInstance(new URL[] { Launcher.class.getClassLoader().getResource("svuid1.jar") }, prevCl);
		}

		@Override
		protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException
		{
			ObjectStreamClass resultClassDescriptor = super.readClassDescriptor(); // initially streams descriptor
			Class streamClass = loader.loadClass(resultClassDescriptor.getName());
			logger.debug(streamClass);
			return ObjectStreamClass.lookup(streamClass);
//			Class<?> localClass; // the class in the local JVM that this descriptor represents.
//			try
//			{
//				localClass = Class.forName(resultClassDescriptor.getName());
//			}
//			catch(ClassNotFoundException e)
//			{
//				logger.error("No local class for " + resultClassDescriptor.getName(), e);
//				return resultClassDescriptor;
//			}
//			ObjectStreamClass localClassDescriptor = ObjectStreamClass.lookup(localClass);
//			if(localClassDescriptor != null)
//			{
//				// only if class implements serializable
//				final long localSUID = localClassDescriptor.getSerialVersionUID();
//				final long streamSUID = resultClassDescriptor.getSerialVersionUID();
//				if(streamSUID != localSUID) // check for serialVersionUID mismatch.
//					resultClassDescriptor = localClassDescriptor; // Use local class descriptor for deserialization
//			}
//			return resultClassDescriptor;
//			ObjectStreamClass resultClassDescriptor = super.readClassDescriptor(); // initially streams descriptor
//			Class<?> localClass; // the class in the local JVM that this descriptor represents.
//			try
//			{
//				localClass = Class.forName(resultClassDescriptor.getName());
//			}
//			catch(ClassNotFoundException e)
//			{
//				logger.error("No local class for " + resultClassDescriptor.getName(), e);
//				return resultClassDescriptor;
//			}
//			ObjectStreamClass localClassDescriptor = ObjectStreamClass.lookup(localClass);
//			if(localClassDescriptor != null)
//			{
//				// only if class implements serializable
//				final long localSUID = localClassDescriptor.getSerialVersionUID();
//				final long streamSUID = resultClassDescriptor.getSerialVersionUID();
//				if(streamSUID != localSUID) // check for serialVersionUID mismatch.
//					resultClassDescriptor = localClassDescriptor; // Use local class descriptor for deserialization
//			}
//			return resultClassDescriptor;
		}
	}

	private static final void setGameSeriesV2SerialVersionUID(long value)
	{
		try
		{
			logger.debug("setting muskel2.model.GameSeries.serialVersionUID = " + value);
			final Field f = muskel2.model.GameSeries.class.getDeclaredField("serialVersionUID");

			AccessController.doPrivileged(new PrivilegedAction<Void>() {
				@Override
				public Void run()
				{
					try
					{
						Field modifiersField = Field.class.getDeclaredField("modifiers");
						modifiersField.setAccessible(true);
						modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
						f.setAccessible(true);

						f.setLong(null, value);
					}
					catch(Exception e)
					{
						throw new RuntimeException(e);
					}
					return null;
				}
			});
			logger.debug("setting muskel2.model.GameSeries.serialVersionUID = " + value + " OK");
		}
		catch(Exception e)
		{
			logger.error("setting muskel2.model.GameSeries.serialVersionUID = " + value + " ERROR", e);
		}
	}

	/**
	 * Convert a V2 {@link muskel2.model.GameSeries} to a {@link GameSeries}.<br>
	 * References in the JSON file will be resolved using the given {@link KaroAPICache}.<br>
	 * 
	 * @param gs2 - the V2 {@link muskel2.model.GameSeries}
	 * @param karoAPICache - the {@link KaroAPICache} to resolve references
	 * @return the converted {@link GameSeries}
	 */
	public static GameSeries convert(muskel2.model.GameSeries gs2, KaroAPICache karoAPICache)
	{
		GameSeries gs;
		// type specific properties first (including instantiation)
		if(gs2 instanceof SimpleGameSeries)
		{
			gs = new GameSeries(EnumGameSeriesType.Simple);
			gs.set(GameSeries.V2_TEAM_BASED, false);
			gs.set(GameSeries.NUMBER_OF_GAMES, ((SimpleGameSeries) gs2).numberOfGames);
			gs.set(GameSeries.MIN_PLAYERS_PER_GAME, ((SimpleGameSeries) gs2).minPlayersPerGame);
			gs.set(GameSeries.MAX_PLAYERS_PER_GAME, ((SimpleGameSeries) gs2).maxPlayersPerGame);
		}
		else if(gs2 instanceof BalancedGameSeries)
		{
			gs = new GameSeries(EnumGameSeriesType.Balanced);
			gs.set(GameSeries.V2_TEAM_BASED, false);
			gs.set(GameSeries.NUMBER_OF_MAPS, ((BalancedGameSeries) gs2).numberOfMaps);
			gs.setMapsByKey(convert(((BalancedGameSeries) gs2).mapList, Map.class, karoAPICache));
			gs.setRulesByKey(convert(((BalancedGameSeries) gs2).rulesList));
			// ((BalancedGameSeries) gs2).shuffledPlayers // ignore
		}
		else if(gs2 instanceof KLCGameSeries)
		{
			gs = new GameSeries(EnumGameSeriesType.KLC);
			gs.set(GameSeries.V2_TEAM_BASED, false);
			gs.set(GameSeries.CONF_KLC_GROUPS, KLCGameSeries.GROUPS);
			gs.set(GameSeries.CONF_KLC_LEAGUES, KLCGameSeries.LEAGUES);
			gs.set(GameSeries.CONF_KLC_FIRST_KO_ROUND, KLCGameSeries.FIRST_KO_ROUND);
			gs.set(GameSeries.CURRENT_ROUND, ((KLCGameSeries) gs2).round);
			gs.setPlayers(convert(((KLCGameSeries) gs2).allPlayers, User.class, karoAPICache));
			gs.getPlayersByKey().put(GameSeries.KEY_LEAGUE + "1", convert(((KLCGameSeries) gs2).playersLeague1, User.class, karoAPICache));
			gs.getPlayersByKey().put(GameSeries.KEY_LEAGUE + "2", convert(((KLCGameSeries) gs2).playersLeague2, User.class, karoAPICache));
			gs.getPlayersByKey().put(GameSeries.KEY_LEAGUE + "3", convert(((KLCGameSeries) gs2).playersLeague3, User.class, karoAPICache));
			gs.getPlayersByKey().put(GameSeries.KEY_LEAGUE + "4", convert(((KLCGameSeries) gs2).playersLeague4, User.class, karoAPICache));
			gs.getPlayersByKey().put(GameSeries.KEY_GROUP + "1", convert(((KLCGameSeries) gs2).playersGroup1, User.class, karoAPICache));
			gs.getPlayersByKey().put(GameSeries.KEY_GROUP + "2", convert(((KLCGameSeries) gs2).playersGroup2, User.class, karoAPICache));
			gs.getPlayersByKey().put(GameSeries.KEY_GROUP + "3", convert(((KLCGameSeries) gs2).playersGroup3, User.class, karoAPICache));
			gs.getPlayersByKey().put(GameSeries.KEY_GROUP + "4", convert(((KLCGameSeries) gs2).playersGroup4, User.class, karoAPICache));
			gs.getPlayersByKey().put(GameSeries.KEY_GROUP + "5", convert(((KLCGameSeries) gs2).playersGroup5, User.class, karoAPICache));
			gs.getPlayersByKey().put(GameSeries.KEY_GROUP + "6", convert(((KLCGameSeries) gs2).playersGroup6, User.class, karoAPICache));
			gs.getPlayersByKey().put(GameSeries.KEY_GROUP + "7", convert(((KLCGameSeries) gs2).playersGroup7, User.class, karoAPICache));
			gs.getPlayersByKey().put(GameSeries.KEY_GROUP + "8", convert(((KLCGameSeries) gs2).playersGroup8, User.class, karoAPICache));
			gs.getPlayersByKey().put(GameSeries.KEY_ROUND + "16", convert(((KLCGameSeries) gs2).playersRoundOf16, User.class, karoAPICache));
			gs.getPlayersByKey().put(GameSeries.KEY_ROUND + "8", convert(((KLCGameSeries) gs2).playersRoundOf8, User.class, karoAPICache));
			gs.getPlayersByKey().put(GameSeries.KEY_ROUND + "4", convert(((KLCGameSeries) gs2).playersRoundOf4, User.class, karoAPICache));
			gs.getPlayersByKey().put(GameSeries.KEY_ROUND + "2", convert(((KLCGameSeries) gs2).playersRoundOf2, User.class, karoAPICache));
			gs.setMapsByKey(convertHomeMaps(((KLCGameSeries) gs2).homeMaps, Map.class, karoAPICache));
		}
		else if(gs2 instanceof TeamBasedGameSeries)
		{
			if(gs2 instanceof AllCombinationsGameSeries)
			{
				gs = new GameSeries(EnumGameSeriesType.AllCombinations);
				gs.set(GameSeries.NUMBER_OF_TEAMS_PER_MATCH, ((AllCombinationsGameSeries) gs2).numberOfTeamsPerMatch);
			}
			else if(gs2 instanceof KOGameSeries)
			{
				gs = new GameSeries(EnumGameSeriesType.KO);
				// no additional properties
			}
			else if(gs2 instanceof LeagueGameSeries)
			{
				gs = new GameSeries(EnumGameSeriesType.League);
				// no additional properties
			}
			else
			{
				throw new GameSeriesException("unknown type: " + gs2.getClass());
			}
			// team based properties
			gs.set(GameSeries.V2_TEAM_BASED, true);
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
		}
		else
		{
			throw new GameSeriesException("unknown type: " + gs2.getClass());
		}

		// universal properties
		gs.setTitle(gs2.title);
		gs.setCreator(karoAPICache.getUser(gs2.creator.id));
		gs.setLoaded(true);
		if(gs.getPlayers() == null || gs.getPlayers().size() == 0) // only do this if the players have not yet been set (--> KLCGameSeries)
			gs.setPlayers(convert(gs2.players, User.class, karoAPICache));
		gs.setMaps(convert(gs2.maps, Map.class, karoAPICache));
		gs.setGames(convertGames(gs2.games, karoAPICache));
		gs.setRules(convert(gs2.rules));
		gs.setCreatorGiveUp(gs2.rules.creatorGiveUp);
		gs.setIgnoreInvitable(gs2.rules.ignoreInvitable);

		return gs;
	}

	/**
	 * Helper method for conversion
	 * 
	 * @param <T>
	 * @param <T2>
	 * @param list2
	 * @param cls
	 * @param karoAPICache
	 * @return
	 */
	protected static <T extends Identifiable, T2 extends muskel2.model.help.Identifiable> List<T> convert(List<T2> list2, Class<T> cls, KaroAPICache karoAPICache)
	{
		if(list2 == null)
			return null;
		List<T> list = new ArrayList<>(list2.size());
		for(T2 o2 : list2)
			list.add(karoAPICache.get(cls, o2.getId()));
		return list;
	}

	/**
	 * Helper method for conversion
	 * 
	 * @param list2
	 * @param karoAPICache
	 * @return
	 */
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

	/**
	 * Helper method for conversion
	 * 
	 * @param list2
	 * @param karoAPICache
	 * @return
	 */
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

	/**
	 * Helper method for conversion
	 * 
	 * @param <T>
	 * @param map2
	 * @param cls
	 * @param karoAPICache
	 * @return
	 */
	protected static <T extends Identifiable> java.util.Map<String, List<T>> convertHomeMaps(java.util.Map<Integer, Integer> map2, Class<T> cls, KaroAPICache karoAPICache)
	{
		if(map2 == null)
			return null;
		HashMap<String, List<T>> map = new HashMap<>();
		for(Entry<Integer, Integer> e : map2.entrySet())
			map.put(e.getKey().toString(), Arrays.asList(karoAPICache.get(cls, e.getValue())));
		return map;
	}

	/**
	 * Helper method for conversion
	 * 
	 * @param <T>
	 * @param <T2>
	 * @param map2
	 * @param cls
	 * @param karoAPICache
	 * @return
	 */
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

	/**
	 * Helper method for conversion
	 * 
	 * @param map2
	 * @return
	 */
	protected static java.util.Map<String, Rules> convert(java.util.Map<Integer, muskel2.model.Rules> map2)
	{
		if(map2 == null)
			return null;
		HashMap<String, Rules> map = new HashMap<>();
		for(Entry<Integer, muskel2.model.Rules> e : map2.entrySet())
			map.put(e.getKey().toString(), convert(e.getValue()));
		return map;
	}

	/**
	 * Helper method for conversion
	 * 
	 * @param r2
	 * @return
	 */
	protected static Rules convert(muskel2.model.Rules r2)
	{
		Rules r = new Rules();
		r.setCps(r2.checkpointsActivated);
		r.setGamesPerPlayer(r2.gamesPerPlayer);
		r.setMaxZzz(r2.maxZzz);
		r.setMinZzz(r2.minZzz);
		r.setNumberOfPlayers(0);
		if(r2.direction == Direction.klassisch)
			r.setStartdirection(EnumGameDirection.classic);
		else if(r2.direction == Direction.Formula_1)
			r.setStartdirection(EnumGameDirection.formula1);
		else if(r2.direction == Direction.egal)
			r.setStartdirection(EnumGameDirection.free);
		else
			r.setStartdirection(EnumGameDirection.random);
		if(r2.crashingAllowed == null)
			r.setCrashallowed(EnumGameTC.random);
		else if(r2.crashingAllowed == true)
			r.setCrashallowed(EnumGameTC.allowed);
		else if(r2.crashingAllowed == false)
			r.setCrashallowed(EnumGameTC.forbidden);
		return r;
	}

	/**
	 * Initiate the screens for the KaroMUSKEL GUI for a given {@link GameSeries}
	 * 
	 * @param gs - the {@link GameSeries}
	 * @param karoAPICache - the {@link KaroAPICache} to be used by the GUI
	 * @param startScreen - the {@link StartScreen} that triggers the initiation
	 * @param previousButton - the previous Button used in the GUI
	 * @param nextButton - the next Button used in the GUI
	 * @return the {@link List} of screens
	 */
	public static LinkedList<Screen> initScreens(GameSeries gs, KaroAPICache karoAPICache, Screen startScreen, JButton previousButton, JButton nextButton)
	{
		JFrame gui = startScreen.getGui();
		LinkedList<Screen> screens = new LinkedList<>();
		if(gs.isLoaded())
		{
			SummaryScreen s = new SummaryScreen(gui, startScreen, karoAPICache, previousButton, nextButton);
			s.setSkipPlan(true);
			screens.add(s);

			if(gs.getType() == EnumGameSeriesType.KLC)
			{
				int groups = getIntConfig(GameSeries.CONF_KLC_GROUPS);
				int leagues = getIntConfig(GameSeries.CONF_KLC_LEAGUES);
				int players = groups * leagues;

				int round = (int) gs.get(GameSeries.CURRENT_ROUND);
				screens.getLast().setNextKey("screen.summary.nextko");
				if(round == players)
				{
					screens.add(new GroupWinnersScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new SummaryScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
				}
				else if(round > 2)
				{
					screens.add(new KOWinnersScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new SummaryScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
				}
				round = round / 2;
				gs.set(GameSeries.CURRENT_ROUND, round);
			}
			else if(gs.getType() == EnumGameSeriesType.KO)
			{
				int round = (int) gs.get(GameSeries.CURRENT_ROUND);

				screens.getLast().setNextKey("screen.summary.nextko");
				if(round > 2)
				{
					screens.add(new KOWinnersScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new SummaryScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
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
					screens.add(new SettingsScreen(gui, startScreen, karoAPICache, previousButton, nextButton));
					screens.add(new RulesScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new PlayersScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new HomeMapsScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new MapsScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new SummaryScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					break;
				case Balanced:
					screens.add(new SettingsScreen(gui, startScreen, karoAPICache, previousButton, nextButton));
					screens.add(new RulesScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new PlayersScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new MapsAndRulesScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new SummaryScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					break;
				case KLC:
					screens.add(new SettingsScreen(gui, startScreen, karoAPICache, previousButton, nextButton));
					screens.add(new RulesScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new PlayersScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new HomeMapsScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.getLast().setNextKey("screen.homemaps.nextskip");
					screens.add(new SummaryScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					break;
				case Simple:
					screens.add(new SettingsScreen(gui, startScreen, karoAPICache, previousButton, nextButton));
					screens.add(new RulesScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new PlayersScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new MapsScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					screens.add(new SummaryScreen(gui, screens.getLast(), karoAPICache, previousButton, nextButton));
					break;
			}
		}
		return screens;
	}
}