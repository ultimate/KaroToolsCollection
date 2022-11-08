package ultimate.karomuskel;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.exceptions.KaroAPIException;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.LoginDialog;
import ultimate.karomuskel.ui.MainFrame;

/**
 * This is the Launcher for the KaroMUSKEL. It contains the {@link Launcher#main(String[])} to run the program.<br>
 * On start the Launcher will perform the following steps:
 * <table>
 * <tr>
 * <td>1.</td>
 * <td>{@link Launcher#loadConfig(String)}</td>
 * <td>=&gt;</td>
 * <td>load and apply the configuration</td>
 * </tr>
 * <tr>
 * <td>2.</td>
 * <td>{@link Launcher#login()}</td>
 * <td>=&gt;</td>
 * <td>initialize the {@link KaroAPI} and login the user via a login dialog</td>
 * </tr>
 * <tr>
 * <td>3.</td>
 * <td>{@link Launcher#createCache(KaroAPI, Properties)}</td>
 * <td>=&gt;</td>
 * <td>initialize the {@link KaroAPICache} and pre-load relevant information into memory</td>
 * </tr>
 * <tr>
 * <td>4.</td>
 * <td>{@link Launcher#initUI(KaroAPICache)}</td>
 * <td>=&gt;</td>
 * <td>initialize the UI {@link MainFrame}</td>
 * </tr>
 * </table>
 * 
 * @author ultimate
 */
public class Launcher
{
	/**
	 * Logger-Instance
	 */
	protected static transient final Logger	logger			= LogManager.getLogger(Launcher.class);
	/**
	 * The key for the language in the config
	 */
	public static final String				KEY_LANGUAGE	= "language";
	/**
	 * The key for the max number of {@link KaroAPI} threads in the config
	 * 
	 * @see KaroAPI#setExecutor(java.util.concurrent.Executor)
	 */
	public static final String				KEY_THREADS		= "karoAPI.maxThreads";

	/**
	 * Is the KaroMUSKEL running in debug mode?
	 */
	private static boolean					debug			= false;
	/**
	 * Is the KaroMUSKEL running in special mode?
	 */
	private static boolean					special			= false;
	/**
	 * The UI instance
	 */
	private static MainFrame				gui				= null;
	/**
	 * The {@link KaroAPI} instance
	 */
	private static KaroAPI					api				= null;
	/**
	 * The {@link KaroAPICache} instance
	 */
	private static KaroAPICache				cache			= null;

	/**
	 * The main to start the KaroMUSKEL.<br>
	 * Supported args:
	 * <ul>
	 * <li>-d = debug-mode (= API mock)</li>
	 * <li>-l=?? = the language to use</li>
	 * <li>-t=xx = the max number of threads to use for the {@link KaroAPI}</li>
	 * </ul>
	 * 
	 * @param args - see above
	 */
	public static void main(String[] args)
	{
		logger.info("------------------------------------------------------------------------");
		logger.info("                               KaroMUSKEL                               ");
		logger.info("  Maschinelle-Ultimative-Spielserien-für-Karopapier-Erstellungs-Lösung  ");
		logger.info("------------------------------------------------------------------------");
		logger.info("------------------------------------------------------------------------");

		// defaults
		String configFile = null;

		if(args.length > 0)
		{
			for(String arg : args)
			{
				if(arg.equalsIgnoreCase("-d"))
					debug = true;
				else if(arg.startsWith("-x"))
					special = true;
				else
					configFile = arg;
			}
		}

		if(debug)
		{
			logger.info("                               DEBUG-MODE                               ");
			logger.info("------------------------------------------------------------------------");
			logger.info("------------------------------------------------------------------------");
		}

		Properties config = loadConfig(configFile);
		if(!debug)
		{
			// not setting the API in debug mode will trigger the KaroAPI cache to create dummy instances
			api = login();
		}
		cache = createCache(api, config);
		if(special)
			specialMode(cache, args);
		else
			gui = initUI(cache);

		logger.info("-------------------------------------------------------------------------");
		logger.info("-------------------------------------------------------------------------");
		logger.info("                         INITIALIZATION COMPLETE                         ");
		logger.info("-------------------------------------------------------------------------");
		logger.info("-------------------------------------------------------------------------");
	}

	/**
	 * Load and apply the configuration. This will also configure the {@link Executor} for the {@link KaroAPI}
	 * 
	 * @see KaroAPI#setExecutor(java.util.concurrent.Executor)
	 * @param configFile - the config
	 * @return the loaded config as {@link Properties}
	 */
	static Properties loadConfig(String configFile)
	{
		Properties config;
		try
		{
			if(configFile == null)
			{
				// load default from jar
				logger.info("loading default config file ...");
				config = PropertiesUtil.loadProperties(Launcher.class, "karomuskel.properties");
			}
			else
			{
				// load custom from file
				logger.info("loading config file '" + configFile + "' ...");
				config = PropertiesUtil.loadProperties(new File(configFile));
			}
			GameSeriesManager.setConfig(config);
		}
		catch(IOException e1)
		{
			logger.error("could not load config file '" + configFile + "'");
			exit();
			return null;
		}

		String language = Language.getDefault();
		if(config.containsKey(KEY_LANGUAGE))
			language = config.getProperty(KEY_LANGUAGE);
		Language.load(language);
		KaroAPI.setApplication(Language.getApplicationName(), Language.getApplicationVersion());

		if(config.containsKey(KEY_THREADS))
		{
			try
			{
				int maxThreads = Integer.parseInt(config.getProperty(KEY_THREADS));
				if(maxThreads > 0)
				{
					logger.info("setting KaroAPI thread pool size = " + maxThreads);
					KaroAPI.setExecutor(Executors.newFixedThreadPool(maxThreads));
				}
				else
				{
					logger.error("invalid value for KaroAPI thread pool size = " + maxThreads);
				}
			}
			catch(NumberFormatException e)
			{
				logger.error("invalid value for KaroAPI thread pool size = " + config.getProperty(KEY_THREADS));
			}
		}
		return config;
	}

	/**
	 * Initialize the {@link KaroAPI} and login the user via a login dialog.
	 * 
	 * @see LoginDialog
	 * @return the {@link KaroAPI} instance
	 */
	static KaroAPI login()
	{
		LoginDialog loginDialog = LoginDialog.getInstance();
		KaroAPI api;
		while(true)
		{
			int result = loginDialog.show();
			if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION)
			{
				logger.info("login canceled");
				exit();
				return null;
			}

			logger.info("creating KaroAPI instance: \"" + loginDialog.getUser() + "\" ... ");

			try
			{
				api = new KaroAPI(loginDialog.getUser(), loginDialog.getPassword());
				if(api.check().get() != null)
				{
					logger.info("login successful!");
					break;
				}
				else
				{
					logger.warn("login failed!");
				}
			}
			catch(KaroAPIException e)
			{
				Throwable ioex = e;
				do
				{
					ioex = ioex.getCause();
				} while(ioex != null && !(ioex instanceof IOException));

				if(ioex != null)
					logger.error("login failed: " + ioex.getMessage());
				else
					logger.error("login failed: " + e.getMessage());
			}
			catch(InterruptedException | ExecutionException e)
			{
				logger.error("login failed: " + e.getMessage());
			}
		}
		return api;
	}

	/**
	 * Initialize the {@link KaroAPICache} and pre-load relevant information into memory
	 * 
	 * @param api - the {@link KaroAPI} instance
	 * @param config - the config {@link Properties} loaded previously
	 * @return the {@link KaroAPICache} instance
	 */
	static KaroAPICache createCache(KaroAPI api, Properties config)
	{
		logger.info("initializing cache...");
		KaroAPICache cache = new KaroAPICache(api, config);
		cache.refresh().join();
		logger.info("cache initialized");
		return cache;
	}

	/**
	 * Initialize the UI {@link MainFrame}
	 * 
	 * @param cache - the {@link KaroAPICache} instance
	 * @return the UI {@link MainFrame}
	 */
	static MainFrame initUI(KaroAPICache cache)
	{
		logger.info("launching user interface");
		MainFrame gui = new MainFrame("mainframe.title", cache);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.requestFocus();
		return gui;
	}

	/**
	 * For internal test purposes
	 * 
	 * @param cache
	 * @param args
	 */
	private static void specialMode(KaroAPICache cache, String[] args)
	{
		for(String arg : args)
		{
			if(!arg.startsWith("-x"))
				continue;
			try
			{
				if(arg.equalsIgnoreCase("-xKLC312"))
				{
					logger.info(arg);
					File in = new File("src/test/resources/season17_muskel_ko_runde.json");
					File out = new File("src/test/resources/season17_muskel_ko_runde_update.json");
					GameSeriesUpdater.updateV312KLC(cache, in, out);
				}
			}
			catch(Exception e)
			{
				logger.error(e);
				e.printStackTrace();
			}
		}
		System.exit(0);
	}

	/**
	 * Kill the GUI and terminate the program.
	 */
	public static void exit()
	{
		logger.info("-------------------------------------------------------------------------");
		logger.info("-------------------------------------------------------------------------");

		if(KaroAPI.getExecutor() != null)
		{
			logger.info("stopping KaroAPI threads...");
			KaroAPI.getExecutor().shutdownNow();
		}

		if(gui != null)
		{

			logger.info("stopping GUI...");
			gui.setVisible(false);
			gui.dispose();
			gui = null;
			api = null;
			cache = null;
		}

		logger.info("                           PROGRAM  TERMINATED                           ");
		logger.info("-------------------------------------------------------------------------");
		logger.info("-------------------------------------------------------------------------");

		System.exit(0);
	}
}