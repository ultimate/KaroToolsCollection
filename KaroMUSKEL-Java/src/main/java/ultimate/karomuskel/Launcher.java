package ultimate.karomuskel;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.LoginDialog;
import ultimate.karomuskel.ui.MainFrame;

// TODO javadoc
public class Launcher
{
	/**
	 * Logger-Instance
	 */
	protected static transient final Logger	logger			= LogManager.getLogger();
	public static final String				KEY_LANGUAGE	= "language";
	public static final String				KEY_THREADS		= "karoAPI.maxThreads";

	private static boolean					debug			= false;
	/**
	 * The UI instance
	 */
	private static MainFrame				gui = null;

	private static KaroAPI					api = null;
	private static KaroAPICache				cache = null;

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
		String configFile = "config.properties";

		if(args.length > 0)
		{
			for(String arg : args)
			{
				if(arg.equalsIgnoreCase("-d"))
					debug = true;
				else
					configFile = arg;
			}
		}
		
		if(debug)
		{
			logger.info("                              DEBUG - MODE                              ");
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
		gui = initUI(cache);
		
		logger.info("-------------------------------------------------------------------------");
		logger.info("-------------------------------------------------------------------------");
		logger.info("                         INITIALIZATION COMPLETE                         ");
		logger.info("-------------------------------------------------------------------------");
		logger.info("-------------------------------------------------------------------------");
	}

	static Properties loadConfig(String configFile)
	{
		Properties config;
		try
		{
			logger.info("loading config file '" + configFile + "' ...");
			config = PropertiesUtil.loadProperties(new File(configFile));
			GameSeriesManager.setConfig(config);
		}
		catch(IOException e1)
		{
			logger.error("Could not load config file '" + configFile + "'");
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

			api = new KaroAPI(loginDialog.getUser(), loginDialog.getPassword());
			try
			{
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
			catch(InterruptedException | ExecutionException e)
			{
				logger.error("login failed!", e);
			}
		}
		return api;
	}
	
	static KaroAPICache createCache(KaroAPI api, Properties config)
	{
		logger.info("initializing cache...");
		KaroAPICache cache = new KaroAPICache(api, config);
		cache.refresh().join();
		logger.info("cache initialized");
		return cache;
	}
	
	static MainFrame initUI(KaroAPICache cache)
	{
		logger.info("launching user interface");
		MainFrame gui = new MainFrame("mainframe.title", cache);
		gui.requestFocus();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run()
			{
				// TODO needed?
			}
		});
		return gui;
	}

	public static void exit()
	{
		logger.info("-------------------------------------------------------------------------");
		logger.info("-------------------------------------------------------------------------");

		if(gui != null)
		{
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