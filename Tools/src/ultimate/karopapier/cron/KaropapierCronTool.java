package ultimate.karopapier.cron;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TimeZone;

import muskel2.Main;
import muskel2.core.karoaccess.GameCreator;
import muskel2.core.karoaccess.KaropapierLoader;
import muskel2.model.Game;
import muskel2.model.GameSeries;
import muskel2.model.Karopapier;
import muskel2.util.Language;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karoapi4j.wiki.KaroWikiLoader;
import ultimate.karopapier.eval.Eval;

public class KaropapierCronTool
{
	public static final String		DEFAULT_PROPERTIES	= "crontool.properties";
	public static final DateFormat	DATE_FORMAT			= new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	public static final String		PROP_WIKI_PREFIX	= "wiki.file.";
	
	static
	{
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("CET"));
	}

	public static void main(String[] args)
	{
		System.out.println("---------------------------------------------------------------");
		System.out.println("--- EXECUTION " + DATE_FORMAT.format(new Date()));
		System.out.println("---------------------------------------------------------------");

		Language.load("de");

		File propertiesFile;
		if(args != null && args.length > 0)
			propertiesFile = new File(args[0]);
		else
			propertiesFile = new File(DEFAULT_PROPERTIES);

		Properties properties;
		try
		{
			System.out.print("reading properties... ");
			properties = PropertiesUtil.loadProperties(propertiesFile);
			System.out.println("OK");
		}
		catch(IOException e)
		{
			System.err.println("ERROR: could not load properties: " + propertiesFile.getPath());
			return;
		}

		int executions = Integer.parseInt(properties.getProperty("executions"));
		File gameseriesFile = new File(properties.getProperty("gameseries.file"));
		String gameseriesPattern = properties.getProperty("gameseries.pattern");
		boolean gameseriesLeave = Boolean.valueOf(properties.getProperty("gameseries.leave"));
		int gameseriesCreate = Integer.parseInt(properties.getProperty("gameseries.create"));
		String evalClass = properties.getProperty("eval.class");
		String karoUsername = properties.getProperty("karo.username");
		String karoPassword = properties.getProperty("karo.password");
		String wikiUsername = properties.getProperty("wiki.username");
		String wikiPassword = properties.getProperty("wiki.password");
		boolean wikiBot = Boolean.valueOf(properties.getProperty("wiki.bot"));
		Map<String, String> wikiFiles = new HashMap<String, String>();
		for(Object k : properties.keySet())
		{
			String key = (String) k;
			if(key.startsWith(PROP_WIKI_PREFIX))
			{
				String file = key.substring(PROP_WIKI_PREFIX.length());
				String title = properties.getProperty(key);
				wikiFiles.put(file, title);
			}
		}

		executions++;
		System.out.println("current execution: " + executions);
		properties.setProperty("executions", "" + executions);

		GameSeries gs = null;

		// create games
		if(gameseriesFile != null)
		{
			try
			{
				System.out.println("creating games... ");
				if(KaropapierLoader.login(karoUsername, karoPassword))
				{
					Karopapier karopapier = initiateKaropapier();

					gs = loadGameseries(gameseriesFile);

					if(!gs.getCreator().getName().equalsIgnoreCase(karoUsername))
						throw new Exception("user does not match gameseries creator");

					List<Game> allGames = gs.getGames();
					List<Game> gamesToCreate = new ArrayList<Game>();

					String pattern = gameseriesPattern.replace("%{EXEC}", "" + ((executions-1)/gameseriesCreate + 1));
					for(Game g : allGames)
					{
//						System.out.println("checking '" + g.getName() + "' with '" + pattern + "' & created=" + g.isCreated() + " -> " + (g.getName().contains(pattern) && !g.isCreated()));
						if(g.getName().contains(pattern) && !g.isCreated())
							gamesToCreate.add(g);
					}

					if(gamesToCreate.size() > 0)
					{
						System.out.println("games to create:");
						for(Game g : gamesToCreate)
							System.out.println("  " + g.getName());

						GameCreator gc = new GameCreator(karopapier, null);
						gc.createGames(gamesToCreate);
						gc.waitForFinished();
						System.out.println("OK");

						for(Game g : gamesToCreate)
							g.setCreated(true);

						if(gameseriesLeave)
						{
							System.out.println("leaving games... ");
							gc.leaveGames(gamesToCreate, gs.getCreator());
							gc.waitForFinished();
							System.out.println("OK");

							for(Game g : gamesToCreate)
								g.setLeft(true);
						}
						else
						{
							System.out.println("finding game ids... ");
							KaropapierLoader.findIds(gamesToCreate);
							System.out.println("OK");
						}

						File backupFile = new File(gameseriesFile.getPath() + "." + (executions - 1));
						gameseriesFile.renameTo(backupFile);
						saveGameseries(gameseriesFile, gs);
					}
					else
					{
						System.out.println("no games to create");
					}
				}
				else
				{
					System.err.println("FAILED: login failed!");
				}
			}
			catch(Exception e)
			{
				System.err.println("ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}

		// do evaluation
		if(evalClass != null)
		{
			try
			{
				System.out.println("doing evaluation... ");
				Eval e = (Eval) Class.forName(evalClass).newInstance();
				if(gs != null)
					e.prepare(gs, executions);
				e.doEvaluation();
				System.out.println("OK");
			}
			catch(Exception e)
			{
				System.err.println("ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}

		// upload wiki
		if(wikiFiles.size() > 0)
		{
			try
			{
				System.out.println("uploading wiki... ");
				KaroWikiLoader wl = new KaroWikiLoader();
				if(wl.login(wikiUsername, wikiPassword))
				{
					String summary = "Automatische Auswertung " + DATE_FORMAT.format(new Date());
					for(Entry<String, String> e : wikiFiles.entrySet())
					{
						File file = new File(e.getKey());
						if(file.exists())
						{
							System.out.println("uploading " + e.getKey() + " -> " + e.getValue() + " ... ");
							String content = null;
							try
							{
								content = readFile(file);
							}
							catch(IOException ex)
							{
								System.out.println("ERROR: " + ex.getMessage());
								continue;
							}
							if(wl.edit(e.getValue(), content, summary, true, wikiBot))
								System.out.println("OK");
							else
								System.out.println("FAILED");
						}

					}
					wl.logout();
				}
				else
				{
					throw new Exception("login failed!");
				}
			}
			catch(Exception e)
			{
				System.err.println("ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}

		try
		{
			System.out.print("writing updated properties... ");
			File backupFile = new File(propertiesFile.getPath() + ".bak");
			if(!backupFile.exists())
				propertiesFile.renameTo(backupFile);
			PropertiesUtil.storeProperties(propertiesFile, properties, null);
			System.out.println("OK");
		}
		catch(IOException e)
		{
			System.err.println("ERROR: could not write properties: " + propertiesFile.getPath());
			return;
		}

		System.exit(0);
	}

	private static Karopapier initiateKaropapier() throws IOException
	{
		Karopapier karopapier = KaropapierLoader.initiateKaropapier();

		Main.setKaropapier(karopapier);

		return karopapier;
	}

	private static String readFile(File file) throws IOException
	{
		BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
		StringBuffer sb = new StringBuffer();
		try
		{
			int c;
			while((c = fis.read()) != -1)
			{
				sb.append((char) c);
			}
		}
		catch(IOException e)
		{
			throw e;
		}
		finally
		{
			if(fis != null)
				fis.close();
		}
		return sb.toString();
	}

	private static GameSeries loadGameseries(File file) throws IOException, ClassNotFoundException
	{
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ObjectInputStream ois = new ObjectInputStream(bis);

		GameSeries gameSeries = (GameSeries) ois.readObject();

		ois.close();
		bis.close();
		fis.close();

		return gameSeries;
	}

	private static void saveGameseries(File file, GameSeries gameSeries) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		ObjectOutputStream oos = new ObjectOutputStream(bos);

		oos.writeObject(gameSeries);

		oos.flush();
		bos.flush();
		fos.flush();

		oos.close();
		bos.close();
		fos.close();
	}
}