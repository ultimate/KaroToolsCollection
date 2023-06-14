package ultimate.karoraupe;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.enums.EnumMoveTrigger;

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
	protected transient final Logger	logger								= LogManager.getLogger(getClass());

	public static final int				TIME_SCALE							= 1000;

	// define all key lower case!
	public static final String			KEY_PREFIX							= "karoraupe";
	public static final String			KEY_TRIGGER							= KEY_PREFIX + ".trigger";
	public static final String			KEY_TIMEOUT							= KEY_PREFIX + ".timeout";
	public static final String			KEY_INTERVAL						= KEY_PREFIX + ".interval";
	public static final String			KEY_MESSAGE							= KEY_PREFIX + ".message";
	public static final String			KEY_STRICT							= KEY_PREFIX + ".strict";
	public static final String			KEY_SPECIAL_REMULADE				= KEY_PREFIX + ".remulade";
	public static final String			KEY_SPECIAL_REMULADE_MESSAGE		= KEY_SPECIAL_REMULADE + ".message";
	public static final String			KEY_SPECIAL_SINGLEOPTION			= KEY_PREFIX + ".singleoption";
	public static final String			KEY_SPECIAL_SINGLEOPTION_MESSAGE	= KEY_SPECIAL_SINGLEOPTION + ".singleoption";

	/**
	 * {@link DateFormat} for log output
	 */
	private static final DateFormat		DATE_FORMAT							= new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

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
	 * Check whether a property value is valid.<br>
	 * This is important for non-string properties (enums, numbers), to be able to ignore them during copying, so that falling back will work
	 * (otherwise valid defaults will be overwritten with invalid
	 * configs).
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	private static boolean isPropertyValid(String key, String value)
	{
		if(key.equalsIgnoreCase(KEY_TRIGGER))
		{
			// check enum
			try
			{
				EnumMoveTrigger enumValue = EnumMoveTrigger.valueOf(value);
				return enumValue != EnumMoveTrigger.invalid;
			}
			catch(IllegalArgumentException | NullPointerException e)
			{
				return false;
			}
		}
		else if(key.equalsIgnoreCase(KEY_TIMEOUT) || key.equalsIgnoreCase(KEY_INTERVAL))
		{
			// check numbers
			try
			{
				int numberValue = Integer.parseInt(value);
				return numberValue >= 0;
			}
			catch(NumberFormatException | NullPointerException e)
			{
				return false;
			}
		}
		else if(key.equalsIgnoreCase(KEY_STRICT) || key.equalsIgnoreCase(KEY_SPECIAL_REMULADE) || key.equalsIgnoreCase(KEY_SPECIAL_SINGLEOPTION))
		{
			// check booleans
			return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
		}
		else if(key.equalsIgnoreCase(KEY_MESSAGE))
		{
			// nothing to check - this is a string
			return true;
		}
		else
		{
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
			game = api.getGameWithDetails(game.getId()).get();

			if(game.isFinished())
			{
				logger.warn("  GID = " + game.getId() + " --> game is finished");
			}
			else if(game.getNext().getId() != userId)
			{
				logger.warn("  GID = " + game.getId() + " --> wrong user's turn");
			}
			else
			{
				Properties gameConfig = getGameConfig(game.getId(), game.getNotes());

				EnumMoveTrigger trigger = EnumMoveTrigger.valueOf(gameConfig.getProperty(KEY_TRIGGER)).standardize();
				boolean special_remulade = Boolean.valueOf(gameConfig.getProperty(KEY_SPECIAL_REMULADE));
				boolean special_singleOption = Boolean.valueOf(gameConfig.getProperty(KEY_SPECIAL_SINGLEOPTION));
				int timeout = Integer.parseInt(gameConfig.getProperty(KEY_TIMEOUT));

				if(trigger == EnumMoveTrigger.never || trigger == EnumMoveTrigger.invalid)
				{
					logger.debug("  GID = " + game.getId() + " --> SKIPPING --> KaroRAUPE not enabled for this game");
					return false;
				}

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
				if(player == null)
				{
					logger.warn("  GID = " + game.getId() + " --> user not participating in this game");
					return false;
				}

				Move lastPlayerMove = player.getMotion();
				logger.debug("  GID = " + game.getId() + " --> player moved last: "
						+ (lastPlayerMove == null ? "never" : DATE_FORMAT.format(lastPlayerMove.getT()) + " (" + ((new Date().getTime() - lastPlayerMove.getT().getTime()) / TIME_SCALE) + "s ago)"));

				if(lastPlayerMove == null)
				{
					logger.info("  GID = " + game.getId() + " --> SKIPPING --> no start position selected yet");
					return false;
				}

				// scan other players for messages and last move made
				Date lastMoveDate = game.getStarteddate();
				boolean messageFound = false;
				boolean notificationFound = false;
				boolean anybodyMoved = false;

				boolean reProtection = false;
				int repeatX = lastPlayerMove.getX() + lastPlayerMove.getXv();
				int repeatY = lastPlayerMove.getY() + lastPlayerMove.getYv();

				for(Player p : game.getPlayers())
				{
					if(p.getMoves() == null)
						continue;

					if(p.isMoved() && p != player)
						anybodyMoved = true;

					if(p.getMotion() != null && p.getMotion().getX() == repeatX && p.getMotion().getY() == repeatY)
						reProtection = true;

					for(Move m : p.getMoves())
					{
						// look for the last move made in the game
						if(m.getT().after(lastMoveDate))
							lastMoveDate = m.getT();
						// look for messages
						if((lastPlayerMove == null || m.getT().after(lastPlayerMove.getT())) && m.getMsg() != null && !m.getMsg().isEmpty())
						{
							if(isNotification(m.getMsg()))
							{
								notificationFound = true;
							}
							else
							{
								messageFound = true;
								notificationFound = true;
							}
						}
					}
				}

				if(lastPlayerMove.getXv() == 0 && lastPlayerMove.getYv() == 0)
				{
					logger.info("  GID = " + game.getId() + " --> SKIPPING --> restart after crash");
					return false;
				}

				long timeSinceLastMove = (new Date().getTime() - lastMoveDate.getTime()) / TIME_SCALE; // convert to seconds
				logger.debug("  GID = " + game.getId() + " --> others moved last: " + (DATE_FORMAT.format(lastMoveDate) + " (" + timeSinceLastMove + "s ago)"));
				if(timeSinceLastMove < timeout)
				{
					logger.info("  GID = " + game.getId() + " --> SKIPPING --> timeout not yet reached (timeout = " + timeout + "s, last move = " + timeSinceLastMove + "s ago)");
					return false;
				}

				if(notificationFound && trigger == EnumMoveTrigger.nonotification)
				{
					logger.info("  GID = " + game.getId() + " --> SKIPPING --> notification found");
					return false;
				}

				if(messageFound && trigger == EnumMoveTrigger.nomessage)
				{
					logger.info("  GID = " + game.getId() + " --> SKIPPING --> message found");
					return false;
				}

				if(player.getPossibles() == null || player.getPossibles().size() == 0)
				{
					logger.warn("  GID = " + game.getId() + " --> SKIPPING --> possibles = 0 --> can't move");
					return false;
				}

				// check remulade stuff
				Move repeatMove = null;
				for(Move m : player.getPossibles())
				{
					if(m.getXv() == player.getMotion().getXv() && m.getYv() == player.getMotion().getYv())
						repeatMove = m;
				}
				if(isRemuladeGame(game.getName()))
					logger.debug("  GID = " + game.getId() + " --> RemulAde: RE = " + (!anybodyMoved) + ", canRepeat = " + (repeatMove != null) + ", reProtection = " + reProtection + ", activated = "
							+ special_remulade);

				Move m;
				if(isRemuladeGame(game.getName()) && !anybodyMoved && (repeatMove != null) && !reProtection && special_remulade)
				{
					logger.info("  GID = " + game.getId() + " --> SPECIAL  --> REmulAde");
					m = repeatMove;
					if(gameConfig.getProperty(KEY_SPECIAL_REMULADE_MESSAGE) != null && !gameConfig.getProperty(KEY_SPECIAL_REMULADE_MESSAGE).isEmpty())
						m.setMsg(gameConfig.getProperty(KEY_SPECIAL_REMULADE_MESSAGE));
				}
				else if(player.getPossibles().size() == 1 && special_singleOption)
				{
					logger.info("  GID = " + game.getId() + " --> SPECIAL  --> Single-Option");
					m = player.getPossibles().get(0);
					if(gameConfig.getProperty(KEY_SPECIAL_SINGLEOPTION_MESSAGE) != null && !gameConfig.getProperty(KEY_SPECIAL_SINGLEOPTION_MESSAGE).isEmpty())
						m.setMsg(gameConfig.getProperty(KEY_SPECIAL_SINGLEOPTION_MESSAGE));
				}
				else if(game.getPlannedMoves() == null || game.getPlannedMoves().size() == 0)
				{
					logger.debug("  GID = " + game.getId() + " --> SKIPPING --> no planned moves found");
					return false;
				}
				else
				{
					boolean strict = Boolean.valueOf(gameConfig.getProperty(KEY_STRICT));
					List<Move> options = findMove(lastPlayerMove, player.getPossibles(), game.getPlannedMoves(), strict);
					logger.debug("  GID = " + game.getId() + " --> " + options);

					if(options.size() == 0)
					{
						logger.info("  GID = " + game.getId() + " --> SKIPPING --> possibles = " + player.getPossibles().size() + ", matches = 0" + (strict ? " (strict)" : ""));
						return false;
					}
					else if(options.size() > 1)
					{
						logger.info("  GID = " + game.getId() + " --> SKIPPING --> possibles = " + player.getPossibles().size() + ", matches = " + options.size() + (strict ? " (strict)" : ")")
								+ " --> can't decide");
						return false;
					}
					m = options.get(0);
					if(gameConfig.getProperty(KEY_MESSAGE) != null && !gameConfig.getProperty(KEY_MESSAGE).isEmpty())
						m.setMsg(gameConfig.getProperty(KEY_MESSAGE));
				}

				logger.info("  GID = " + game.getId() + " --> MOVING --> vec " + m.getXv() + "|" + m.getYv() + " --> " + m.getX() + "|" + m.getY()
						+ (m.getMsg() != null ? " ... msg='" + m.getMsg() + "'" : ""));

				if(!debug)
					return this.api.move(game.getId(), m).get();
				else
					return true;
			}
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
	 * Match the possibles against the planned moves.<br>
	 * A move will only be selected, if the current move is in the planned moves and has a successor which is also a possible move.
	 * 
	 * @param currentMove
	 * @param possibles
	 * @param plannedMoves
	 * @param strict
	 * @return
	 */
	public List<Move> findMove(Move currentMove, List<Move> possibles, List<Move> plannedMoves, boolean strict)
	{
		List<Move> matches = new ArrayList<>();

		if(plannedMoves != null)
		{
			Move pm, prevpm;
			for(int i = 0; i < plannedMoves.size(); i++)
			{
				pm = plannedMoves.get(i);

				if(strict)
				{
					// previous move must match the current move to allow the planned move
					if(i == 0)
						continue;
					prevpm = plannedMoves.get(i - 1);

					if(prevpm.getX() != currentMove.getX() || prevpm.getY() != currentMove.getY() || prevpm.getXv() != currentMove.getXv() || prevpm.getYv() != currentMove.getYv())
						continue; // previous move does not match
				}

				// look if the planned moves contain the current move

				for(Move possible : possibles)
				{
					if(pm.getX() == possible.getX() && pm.getY() == possible.getY() && pm.getXv() == possible.getXv() && pm.getYv() == possible.getYv())
					{
						matches.add(possible);
						break;
					}
				}
			}
		}
		return matches;
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

	protected static boolean isNotification(String message)
	{
		if(!message.startsWith("-:K"))
			return false;
		else if(!message.endsWith("K:-"))
			return false;

		else if(message.matches("-:KIch bin ausgestiegenK:-"))
			return true;
		else if(message.matches("-:KIch bin von (Didi|KaroMAMA) rausgeworfen wordenK:-"))
			return true;
		else if(message.matches("-:KIch wurde von (Didi|KaroMAMA) rausgeworfenK:-"))
			return true;
		else if(message.matches("-:KIch werde \\d+ Z&uuml;ge zur&uuml;ckgesetztK:-"))
			return true;

		return false;
	}

	protected static boolean isRemuladeGame(String title)
	{
		return title.toLowerCase().replace(" ", "").startsWith("§remulade§");
	}
}
