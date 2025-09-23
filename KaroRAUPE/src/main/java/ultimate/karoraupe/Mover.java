package ultimate.karoraupe;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.enums.EnumMoveTrigger;
import ultimate.karoraupe.rules.AfterCrashRule;
import ultimate.karoraupe.rules.AntiAddictRule;
import ultimate.karoraupe.rules.EnabledRule;
import ultimate.karoraupe.rules.FinishedRule;
import ultimate.karoraupe.rules.FollowPlanRule;
import ultimate.karoraupe.rules.MessageRule;
import ultimate.karoraupe.rules.NoPossiblesRule;
import ultimate.karoraupe.rules.RandomRule;
import ultimate.karoraupe.rules.RemuladeRule;
import ultimate.karoraupe.rules.RepeatRule;
import ultimate.karoraupe.rules.Rule;
import ultimate.karoraupe.rules.SingleOptionRule;
import ultimate.karoraupe.rules.StartPositionRule;
import ultimate.karoraupe.rules.TimeoutRule;
import ultimate.karoraupe.rules.UserTurnRule;
import ultimate.karoraupe.rules.Rule.Result;

/**
 * The Mover to check and process games
 * 
 * @author ultimate
 */
public class Mover
{
	/**
	 * Logger-Instance
	 */
	protected static final Logger	logger								= LogManager.getLogger(Mover.class);

	public static final int				TIME_SCALE							= 1000;

	// define all key lower case!
	public static final String			KEY_PREFIX							= "karoraupe";
	public static final String			KEY_TRIGGER							= KEY_PREFIX + ".trigger";
	public static final String			KEY_TIMEOUT							= KEY_PREFIX + ".timeout";
	public static final String			KEY_INTERVAL						= KEY_PREFIX + ".interval";
	public static final String			KEY_MESSAGE							= KEY_PREFIX + ".message";
	public static final String			KEY_STRICT							= KEY_PREFIX + ".strict";

	/**
	 * The default / fallback config
	 */
	private static Properties			defaultConfig;

	static
	{
		// initiate the default / fallback config
		defaultConfig = new Properties();
		// NOTE: store all keys lower case
		defaultConfig.setProperty(KEY_TRIGGER, EnumMoveTrigger.nomessage.toString());
		defaultConfig.setProperty(KEY_TIMEOUT, "300");
		defaultConfig.setProperty(KEY_MESSAGE, "");
		defaultConfig.setProperty(KEY_STRICT, "true");
	}

	/**
	 * The default / fallback config
	 * 
	 * @return
	 */
	public static Properties getDefaultConfig()
	{
		return defaultConfig;
	}

	/**
	 * The {@link KaroAPI} to use
	 */
	private KaroAPI		api;
	/**
	 * The global config (loaded from file)
	 */
	private Properties	globalConfig;
	/**
	 * Is the {@link Mover} set to debug? This will disable move execution
	 */
	private boolean		debug;

	/**
	 * The supported properties for this Mover
	 */
	private Map<String, Class<?>> supportedProperties;
	/**
	 * The rules that this Mover is using
	 */
	private List<Rule> rules;

	/**
	 * Initiate the Mover.<br>
	 * This will re-construct the given config based on the default config (so later calls to getProperty() can fall back on this).
	 * 
	 * @param api - The {@link KaroAPI} to use
	 * @param config - The global config (loaded from file)
	 * @param debug - Is the {@link Mover} set to debug? This will disable move execution
	 */
	public Mover(KaroAPI api, Properties config, boolean debug)
	{
		this.api = api;
		this.debug = debug;		

		this.supportedProperties = new HashMap<>();
		this.supportedProperties.put(KEY_TRIGGER, EnumMoveTrigger.class);
		this.supportedProperties.put(KEY_TIMEOUT, int.class);
		this.supportedProperties.put(KEY_INTERVAL, int.class);
		this.supportedProperties.put(KEY_MESSAGE, String.class);
		this.supportedProperties.put(KEY_STRICT, boolean.class);

		this.rules = new LinkedList<>();
		this.rules.add(new EnabledRule(this.api));
		this.rules.add(new FinishedRule(this.api));
		this.rules.add(new UserTurnRule(this.api));
		this.rules.add(new StartPositionRule(this.api));
		this.rules.add(new NoPossiblesRule(this.api));
		this.rules.add(new AfterCrashRule(this.api));
		this.rules.add(new TimeoutRule(this.api));
		this.rules.add(new AntiAddictRule(this.api));
		this.rules.add(new MessageRule(this.api));
		this.rules.add(new RemuladeRule(this.api));
		this.rules.add(new SingleOptionRule(this.api));
		this.rules.add(new FollowPlanRule(this.api)); // the most important rule!!!
		this.rules.add(new RepeatRule(this.api));
		this.rules.add(new RandomRule(this.api));

		this.globalConfig = new Properties(defaultConfig);
		if(config != null)
		{
			// the properties are not just copied, their keys are converted to lower case for more tolerance
			String value;
			for(String key : config.stringPropertyNames())
			{
				if(!key.toLowerCase().startsWith(KEY_PREFIX))
					continue;
				value = config.getProperty(key);
				if(!isPropertyValid(key, value))
				{
					logger.info("ignoring invalid config: level=global --> " + key + "=" + value);
					continue;
				}
				// NOTE: store all keys lower case
				this.globalConfig.put(key.toLowerCase(), value);
			}
		}		
	}

	/**
	 * The global config (loaded from file)
	 * 
	 * @return
	 */
	public Properties getGlobalConfig()
	{
		return globalConfig;
	}

	/**
	 * The supported properties for this Mover
	 * @return
	 */
	public Map<String, Class<?>> getSupportedProperties()
	{
		return Collections.unmodifiableMap(supportedProperties);
	}

	/**
	 * The rules that this Mover is using
	 * @return
	 */
	public List<Rule> getRules()
	{
		return Collections.unmodifiableList(rules);
	}

	/**
	 * Check whether a property value is valid.<br>
	 * This is important for non-string properties (enums, numbers), to be able to ignore them during copying, so that falling back will work
	 * (otherwise valid defaults will be overwritten with invalid
	 * configs).
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	private boolean isPropertyValid(String key, String value)
	{
		for(Entry<String, Class<?>> sp: supportedProperties.entrySet())
		{
			if(key.equalsIgnoreCase(sp.getKey()))
			{
				try
				{
					return checkValueForType(value, sp.getValue());
				}
				catch(IllegalArgumentException | NullPointerException e)
				{
					return false;
				}
			}
		}
		
		for(Rule rule: rules)
		{
			for(Entry<String, Class<?>> sp: rule.getSupportedProperties().entrySet())
			{
				if(key.equalsIgnoreCase(sp.getKey()))
				{
					try
					{
						return checkValueForType(value, sp.getValue());
					}
					catch(IllegalArgumentException | NullPointerException e)
					{
						return false;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Check that a property value is valid for that type.<br/>
	 * This method will try to cast the value and check it for valid values.
	 * 
	 * @param value
	 * @param type
	 * @return
	 */
	private static boolean checkValueForType(String value, Class<?> type)
	{
		if(type == boolean.class)
		{
			return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
		}
		else if(type == int.class)
		{
			int numberValue = Integer.parseInt(value);
			return numberValue >= 0;
		}
		else if(type == double.class)
		{
			double numberValue = Double.parseDouble(value);
			return numberValue >= 0;
		}
		else if(type == EnumMoveTrigger.class)
		{
			EnumMoveTrigger enumValue = EnumMoveTrigger.valueOf(value);
			return enumValue != EnumMoveTrigger.invalid;
		}
		else if(type == String.class)
		{
			return true;
		}
		else
		{
			logger.warn("unsupported property type: " + type);
			return false;
		}
	}

	/**
	 * Parse the config for a game from the notes.
	 * 
	 * @param gid
	 * @param notes
	 * @return
	 */
	public Properties getGameConfig(int gid, String notes)
	{
		Properties gameConfig = new Properties(globalConfig);

		if(notes == null)
			return gameConfig;

		String[] lines = notes.toLowerCase().split("\\r?\\n");

		String key, value;
		for(String l : lines)
		{
			if(l.startsWith(KEY_PREFIX.toLowerCase()) && l.contains("="))
			{
				key = l.substring(0, l.indexOf("=")).trim();
				value = l.substring(l.indexOf("=") + 1).trim();
				if(!isPropertyValid(key, value))
				{
					logger.info("ignoring invalid config: gid=" + gid + "  --> " + key + "=" + value);
					continue;
				}
				// NOTE: store all keys lower case
				gameConfig.setProperty(key.toLowerCase(), value);
			}
		}

		return gameConfig;
	}

	/**
	 * Process a game
	 * 
	 * @param userId
	 * @param game
	 * @return
	 */
	public boolean processGame(int userId, Game game)
	{
		try
		{
			logger.debug("  GID = " + game.getId() + " --> loading game details");
			game = api.getGame(game.getId(), false, true, true).get();
			
			Properties gameConfig = getGameConfig(game.getId(), game.getNotes());

			// scan players to find the player matching the current user
			Player player = null;
			for(Player p : game.getPlayers())
			{
				if(p.getId() == userId)
				{
					player = p;
					break;
				}
			}				

			Result result;	
			String lastReason = "";		
			int ruleCount = 0;
			for(Rule rule: rules)
			{
				ruleCount++;
				try
				{					
					result = rule.evaluate(game, player, gameConfig);

					logger.debug("  GID = " + game.getId() + " --> #" + fill(ruleCount, 2) + " " + fill(rule.getClass().getSimpleName(), 17) + " --> " + fill(result.shallMove() == null ? "null" : result.shallMove().toString(), 5) + " (" + result.getReason() + ")");

					if(result.shallMove() == null)
					{
						if(result.getReason() != null)
							lastReason = result.getReason();
						continue;
					}
					else if(result.shallMove().booleanValue() == false)
					{
						logger.info("  GID = " + game.getId() + " --> SKIPPING --> " + result.getReason());
						return false;
					}
					else if(result.shallMove().booleanValue() == true)
					{
						Move m = result.getMove();
						if(m == null)
						{
							logger.error("  GID = " + game.getId() + " --> ERROR    --> no move returned");
							return false;
						}

						logger.info("  GID = " + game.getId() + " --> MOVING   --> " + fill(result.getReason(), 14) + " --> vec " + m.getXv() + "|" + m.getYv() + " --> " + m.getX() + "|" + m.getY()
							+ (m.getMsg() != null ? " ... msg='" + m.getMsg() + "'" : ""));

						if(!debug)
							return this.api.move(game.getId(), m).get();
						else
							return true;
					}
				}
				catch(Exception e)
				{
					logger.error("  GID = " + game.getId() + " --> #" + rule.getClass().getSimpleName() + " --> ", e);
				}
			}
			logger.info("  GID = " + game.getId() + " --> SKIPPING --> " + lastReason);
		}
		catch(IllegalArgumentException e)
		{
			logger.debug("  GID = " + game.getId() + " --> SKIPPING --> error in the configuration", e);
		}
		catch(InterruptedException | ExecutionException e)
		{
			logger.error(e);
		}
		return false;
	}

	/**
	 * Method that loads all dran games and processes them.
	 * 
	 * @return number of moves made
	 */
	public int checkAndProcessGames()
	{
		int movesMade = 0;
		try
		{
			int userId = api.check().get().getId();
			List<Game> dranGames = api.getUserDran(userId).get();
			logger.info("dran games   = " + dranGames.size());
			for(Game g : dranGames)
			{
				if(processGame(userId, g))
					movesMade++;
			}
			logger.info("moves made   = " + movesMade);
		}
		catch(InterruptedException | ExecutionException e)
		{
			logger.error("error checking and processing games", e);
			e.printStackTrace();
		}
		return movesMade;
	}

	private static String fill(int i, int length)
	{
		String s = "" + i;
		while(s.length() < length)
		 	s = "0" + i;
		return s;
	}

	private static String fill(String s, int length)
	{
		while(s.length() < length)
		 	s += " ";
		return s;
	}
}
