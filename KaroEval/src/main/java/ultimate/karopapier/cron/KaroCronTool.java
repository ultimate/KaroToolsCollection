package ultimate.karopapier.cron;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.KaroWikiAPI;
import ultimate.karoapi4j.exceptions.KaroAPIException;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.official.Game;
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
			karoAPICache = new KaroAPICache(karoAPI, properties);
			creator = new Creator(karoAPICache);
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
			boolean refreshNeeded = false;
			
			Set<PlannedGame> handledGames = new HashSet<>();

			if(gameseriesCreate)
			{
				logger.info("checking for games to create... ");
				List<PlannedGame> gamesToCreate = findGames(karoAPICache, gs, properties, executions, pg -> {
					return !pg.isCreated();
				});
				logger.info("total to create: " + gamesToCreate.size());
				// create games
				if(gamesToCreate.size() > 0)
				{
					logger.info("creating games... ");
					creator.createGames(gamesToCreate, null).join();
					handledGames.addAll(gamesToCreate);
					refreshNeeded = true;
				}
			}

			if(gameseriesLeave)
			{
				logger.info("checking for games to leave... ");
				List<PlannedGame> gamesToLeave = findGames(karoAPICache, gs, properties, executions, pg -> {
					return !pg.isLeft();
				});
				logger.info("total to leave: " + gamesToLeave.size());
				// leave games
				if(gamesToLeave.size() > 0)
				{
					logger.info("leaving games... ");
					creator.leaveGames(gamesToLeave, null).join();
					handledGames.addAll(gamesToLeave);
					refreshNeeded = true;
				}
			}

			if(refreshNeeded)
			{
				logger.info("saving gameseries & creating backup...");
				File backupFile = new File(gameseriesFile.getPath() + "." + (executions - 1));
				if(backupFile.exists())
				{
					logger.warn("backup already exists");
					backupFile.renameTo(new File(backupFile.getPath() + "." + System.currentTimeMillis()));
				}
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

				logger.info("refreshing games to retrieve players...");
				CompletableFuture<Game>[] refreshs = new CompletableFuture[handledGames.size()];
				int i = 0;
				for(PlannedGame pg : handledGames)
				{
					if(pg.getGame() == null)
						logger.warn("game reference is null: " + pg.getName());
					refreshs[i++] = karoAPICache.refresh(pg.getGame());
				}
				CompletableFuture.allOf(refreshs).join();
			}
		}

		List<File> wikiFiles = null;
		// do evaluation
		if(evalClass != null && !evalClass.isEmpty())
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
			catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e)
			{
				exit("could not instantiate eval class", e);
				return;
			}
			catch(Exception e)
			{
				exit("error during evaluation", e);
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

	public static List<PlannedGame> findGames(KaroAPICache karoAPICache, GameSeries gs, Properties p, int execution, Predicate<? super PlannedGame> filter)
	{
		Date today = new Date();
		List<PlannedGame> games = new LinkedList<>();
		List<PlannedGame> gamesTmp;
		String keyS;

		// do this extra step to have the keys sorted
		List<String> entriesFound = new LinkedList<>();
		for(Object keyO : p.keySet())
		{
			keyS = (String) keyO;
			if(!(keyS.startsWith(PROP_CREATE_PREFIX) && keyS.endsWith(PROP_CREATE_WHEN)))
				continue;
			entriesFound.add(keyS.substring(PROP_CREATE_PREFIX.length(), keyS.indexOf(".", PROP_CREATE_PREFIX.length() + 1)));
		}
		Collections.sort(entriesFound);

		// now check all the keys
		int foundWithKey, foundWithPattern, foundToCreate;
		for(String i : entriesFound)
		{
			final String when = p.getProperty(PROP_CREATE_PREFIX + i + PROP_CREATE_WHEN);
			final String key = p.getProperty(PROP_CREATE_PREFIX + i + PROP_CREATE_KEY);
			final String pattern = p.getProperty(PROP_CREATE_PREFIX + i + PROP_CREATE_PATTERN);

			logger.debug("finding games: #" + i + " \t when=" + when + " \tkey=" + key + " \tpattern=" + pattern);

			gamesTmp = new LinkedList<>();
			gamesTmp.addAll(gs.getGames().get(key));
			foundWithKey = gamesTmp.size();
			logger.debug("  games for this key       = " + foundWithKey);
			if(pattern != null)
				gamesTmp.removeIf(pg -> {
					return !pg.getName().contains(pattern);
				});
			foundWithPattern = gamesTmp.size();
			logger.debug("  matching pattern         = " + foundWithPattern);
			if(filter != null)
				gamesTmp.removeIf(filter.negate());
			foundToCreate = gamesTmp.size();
			logger.debug("  matching filter          = " + foundToCreate);

			if(isReady(when, execution, today))
			{
				logger.info("finding games: #" + i + " \t when=" + when + " \tkey=" + key + " (" + foundWithKey + ") \tpattern=" + pattern + " (" + foundWithPattern + ") \t-> " + foundToCreate
						+ " matching now");
				games.addAll(gamesTmp);
			}
			else
			{
				logger.info("finding games: #" + i + " \t when=" + when + " \tkey=" + key + " (" + foundWithKey + ") \tpattern=" + pattern + " (" + foundWithPattern + ") \t-> " + foundToCreate
						+ " matching later (date not yet reached)");
			}
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
		DataInputStream dis = null;
		try
		{
			// updated for java 8 compatibility
			byte[] bytes = new byte[(int) wikiFile.length()];
			dis = new DataInputStream(new FileInputStream(wikiFile));
			dis.readFully(bytes);

			content = new String(bytes);
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
				dis.close();
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
			logger.error("cause: ", e);
		System.exit(0);
	}
}