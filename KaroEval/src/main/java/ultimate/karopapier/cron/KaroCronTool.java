package ultimate.karopapier.cron;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.KaroWikiAPI;
import ultimate.karoapi4j.exceptions.KaroAPIException;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karomuskel.Creator;
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.ui.Language;
import ultimate.karopapier.eval.Eval;

public class KaroCronTool
{
	/**
	 * Logger-Instance
	 */
	private static transient final Logger	logger				= LogManager.getLogger(KaroCronTool.class);
	public static final String				DEFAULT_PROPERTIES	= "cron.properties";
	public static final DateFormat			DATE_FORMAT			= new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	public static final DateFormat			DATE_FORMAT_SHORT	= new SimpleDateFormat("yyyy.MM.dd");
	public static final String				PROP_WIKI_PREFIX	= "wiki.file.";
	public static final String				PROP_CREATE_PREFIX	= "gameseries.create.";
	public static final String				PROP_CREATE_WHEN	= ".when";
	public static final String				PROP_CREATE_KEY		= ".key";
	public static final String				PROP_CREATE_PATTERN	= ".pattern";

	static
	{
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("CET"));
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args)
	{
		logger.info("---------------------------------------------------------------");
		logger.info("--- EXECUTION " + DATE_FORMAT.format(new Date()));
		logger.info("---------------------------------------------------------------");

		Language.load("de");

		File propertiesFile;
		if(args != null && args.length > 0)
			propertiesFile = new File(args[0]);
		else
			propertiesFile = new File(DEFAULT_PROPERTIES);

		Properties properties = null;
		try
		{
			logger.info("reading properties... ");
			properties = PropertiesUtil.loadProperties(propertiesFile);
		}
		catch(IOException e)
		{
			exit("could not load properties: " + propertiesFile.getPath(), e);
			return;
		}

		int executions = Integer.parseInt(properties.getProperty("executions"));
		File gameseriesFile = new File(properties.getProperty("gameseries.file"));
		boolean gameseriesLeave = Boolean.valueOf(properties.getProperty("gameseries.leave"));
		boolean gameseriesCreate = Boolean.valueOf(properties.getProperty("gameseries.create"));
		String evalClass = properties.getProperty("eval.class");
		String evalPropertiesFile = properties.getProperty("eval.properties");
		String karoUsername = properties.getProperty("karo.username");
		String karoPassword = properties.getProperty("karo.password");
		String wikiUsername = properties.getProperty("wiki.username");
		String wikiPassword = properties.getProperty("wiki.password");
		boolean wikiBot = Boolean.valueOf(properties.getProperty("wiki.bot"));
		boolean wikiUpload = Boolean.valueOf(properties.getProperty("wiki.upload"));

		executions++;
		logger.info("current execution: " + executions);
		properties.setProperty("executions", "" + executions);

		KaroAPI karoAPI = null;
		KaroAPICache karoAPICache = null;
		Creator creator = null;
		try
		{
			logger.info("initiating KaroAPI + cache... ");
			karoAPI = new KaroAPI(karoUsername, karoPassword);
			karoAPICache = new KaroAPICache(karoAPI);
			creator = new Creator(null);//karoAPICache);
		}
		catch(KaroAPIException e)
		{
			exit("could not initiate KaroAPI", e);
		}

		GameSeries gs = null;
		try
		{
			logger.info("loading gameseries... ");
			gs = GameSeriesManager.load(gameseriesFile, karoAPICache);
		}
		catch(IOException e)
		{
			exit("could not load gameseries: " + gameseriesFile.getPath(), e);
			return;
		}

		// check games to create
		if(gameseriesCreate || gameseriesLeave)
		{
			logger.info("checking for games to create / leave... ");
			List<PlannedGame> gamesToCreate = findGamesToCreate(karoAPICache, gs, properties, executions);
			logger.info("found: " + gamesToCreate.size());

			// create games
			if(gamesToCreate.size() > 0)
			{
				if(gamesToCreate.size() > 0 && gameseriesCreate)
				{
					logger.info("creating games... ");
					creator.createGames(gamesToCreate, null).join();
				}
				if(gamesToCreate.size() > 0 && gameseriesLeave)
				{
					logger.info("leaving games... ");
					creator.leaveGames(gamesToCreate, null).join();
				}

				logger.info("saving gameseries & creating backup...");
				File backupFile = new File(gameseriesFile.getPath() + "." + (executions - 1));
				if(backupFile.exists())
				{
					logger.warn("backup already exists");
					backupFile.renameTo(new File(backupFile.getPath() + "." + System.currentTimeMillis()));
				}
				gameseriesFile.renameTo(backupFile);
				gameseriesFile.renameTo(backupFile);
				try
				{
					GameSeriesManager.store(gs, gameseriesFile);
				}
				catch(IOException e)
				{
					exit("could not store gameseries: " + gameseriesFile.getPath(), e);
					return;
				}
			}
		}

		List<File> wikiFiles = null;
		// do evaluation
		if(evalClass != null)
		{
			Properties evalProperties = null;
			if(evalPropertiesFile != null)
			{
				try
				{
					logger.info("reading eval properties... ");
					evalProperties = PropertiesUtil.loadProperties(new File(evalPropertiesFile));
				}
				catch(IOException e)
				{
					exit("could not load eval properties: " + new File(evalPropertiesFile).getPath(), e);
					return;
				}
			}
			try
			{
				logger.info("doing evaluation... ");
				Eval<GameSeries> eval = (Eval<GameSeries>) Class.forName(evalClass).getDeclaredConstructor().newInstance();
				eval.prepare(karoAPICache, gs, evalProperties, new File("."), executions);
				wikiFiles = eval.evaluate();
			}
			catch(Exception e)
			{
				exit("could not instantiate eval class", e);
				return;
			}
		}

		// upload wiki
		if(wikiUpload && wikiFiles != null && wikiFiles.size() > 0)
		{
			logger.info("initating wiki API... ");
			KaroWikiAPI karoWikiAPI = new KaroWikiAPI();
			if(!karoWikiAPI.login(wikiUsername, wikiPassword).join())
			{
				exit("could not login to wiki", null);
				return;
			}

			logger.info("uploading wiki... ");
			String summary = "Automatische Auswertung " + DATE_FORMAT.format(new Date());
			String target;
			for(File wikiFile : wikiFiles)
			{
				if(!wikiFile.exists())
					logger.warn("  " + wikiFile.getPath() + " does not exist");

				target = properties.getProperty(PROP_WIKI_PREFIX + wikiFile.getName());

				try
				{
					if(upload(karoWikiAPI, wikiFile, target, summary, wikiBot))
						logger.info("  " + wikiFile.getPath() + " -> " + target + " OK");
					else
						logger.error("  " + wikiFile.getPath() + " -> " + target + " FAILED");
				}
				catch(Exception e)
				{
					logger.error("  " + wikiFile.getPath() + " -> FAILED" + target, e);
				}
			}

			logger.info("wiki logout... ");
			karoWikiAPI.logout().join();
		}

		try
		{
			logger.info("writing updated properties & creating backup... ");
			File backupFile = new File(propertiesFile.getPath() + "." + (executions - 1));
			if(backupFile.exists())
			{
				logger.warn("backup already exists");
				backupFile.renameTo(new File(backupFile.getPath() + "." + System.currentTimeMillis()));
			}
			propertiesFile.renameTo(backupFile);
			PropertiesUtil.storeProperties(propertiesFile, properties, null);
		}
		catch(IOException e)
		{
			exit("could not write properties: " + propertiesFile.getPath(), e);
			return;
		}

		exit(null, null);
	}

	public static List<PlannedGame> findGamesToCreate(KaroAPICache karoAPICache, GameSeries gs, Properties p, int execution)
	{
		Date today = new Date();
		List<PlannedGame> games = new LinkedList<>();
		List<PlannedGame> gamesTmp;
		String keyS;
		int i;
		for(Object keyO : p.keySet())
		{
			keyS = (String) keyO;
			if(!(keyS.startsWith(PROP_CREATE_PREFIX) && keyS.endsWith(PROP_CREATE_WHEN)))
				continue;
			i = Integer.parseInt(keyS.substring(PROP_CREATE_PREFIX.length(), keyS.indexOf(".", PROP_CREATE_PREFIX.length() + 1)));

			final String when = p.getProperty(PROP_CREATE_PREFIX + i + PROP_CREATE_WHEN);
			final String key = p.getProperty(PROP_CREATE_PREFIX + i + PROP_CREATE_KEY);
			final String pattern = p.getProperty(PROP_CREATE_PREFIX + i + PROP_CREATE_PATTERN);

			if(!isReady(when, execution, today))
				logger.debug("finding games: #" + i + " \t when=" + when + " \tkey=" + key + " \tpattern=" + pattern + " \t-> not yet reached");

			logger.info("finding games: #" + i + " \t when=" + when + " \tkey=" + key + " \tpattern=" + pattern);

			gamesTmp = new LinkedList<>();
			gamesTmp.addAll(gs.getGames().get(key));
			logger.debug("  all games                =" + gamesTmp.size());
			if(pattern != null)
				gamesTmp.removeIf(pg -> { return !pg.getName().contains(pattern); });
			logger.debug("  matching pattern         =" + gamesTmp.size());
			if(pattern != null)
				gamesTmp.removeIf(pg -> { return pg.isCreated() && pg.isLeft(); });
			logger.debug("  neither created nor left =" + gamesTmp.size());

			games.addAll(gamesTmp);
		}
		return games;
	}

	public static boolean isReady(String when, int execution, Date today)
	{
		if(when.contains("."))
		{
			Date create;
			try
			{
				create = DATE_FORMAT.parse(when);
			}
			catch(ParseException e)
			{
				try
				{
					create = DATE_FORMAT_SHORT.parse(when);
				}
				catch(ParseException e1)
				{
					return false;
				}
			}
			return today.after(create);
		}
		else
		{
			try
			{
				int w = Integer.parseInt(when);
				return execution >= w;
			}
			catch(NumberFormatException e)
			{
				return false;
			}
		}
	}

	public static boolean upload(KaroWikiAPI karoWikiAPI, File wikiFile, String target, String summary, boolean bot)
	{
		String content;
		BufferedInputStream bis = null;
		try
		{
			bis = new BufferedInputStream(new FileInputStream(wikiFile));
			content = new String(bis.readAllBytes());
			return karoWikiAPI.edit(target, content, summary, true, bot).join();
		}
		catch(IOException e)
		{
			logger.error("could not read wiki file: " + wikiFile.getAbsolutePath(), e);
			return false;
		}
		finally
		{
			try
			{
				bis.close();
			}
			catch(Exception e)
			{
			}
		}
	}

	private static void exit(String message, Exception e)
	{
		logger.info("exiting...");
		if(message != null)
			logger.info("reason: " + message);
		if(e != null)
			logger.error(e);
		System.exit(0);
	}
}