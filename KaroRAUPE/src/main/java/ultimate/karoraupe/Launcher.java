package ultimate.karoraupe;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.PropertiesUtil;

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
	protected static transient final Logger	logger		= LogManager.getLogger(Launcher.class);

	/**
	 * Is the KaroRAUPE running in debug mode?
	 */
	private static boolean					debug		= false;
	/**
	 * Is the KaroRAUPE running in scanning mode?
	 */
	private static boolean					scanning	= false;
	/**
	 * The {@link KaroAPI} instance
	 */
	private static KaroAPI					api			= null;
	/**
	 * The {@link KaroAPICache} instance
	 */
	private static KaroAPICache				cache		= null;

	/**
	 * The main to start the KaroMUSKEL.<br>
	 * Supported args:
	 * <ul>
	 * <li>-d = debug-mode (= API mock)</li>
	 * <li>-s = scanning-mode</li>
	 * <li>config-file name</li>
	 * </ul>
	 * 
	 * @param args - see above
	 * @throws IOException
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		logger.info("------------------------------------------------------------------------");
		logger.info("                               KaroRAUPE                                ");
		logger.info("                  Rudimentärer AUto-Pilot ohne Extras                   ");
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
				else if(arg.equalsIgnoreCase("-s"))
					scanning = true;
				else
					configFile = arg;
			}
		}

		Properties config = PropertiesUtil.loadProperties(new File(configFile));
		api = new KaroAPI(config.getProperty("karoAPI.user"), config.getProperty("karoAPI.password"));
//		cache = new KaroAPICache(api, config);
//		cache.refresh().join();
		
		Mover mover = new Mover(api, config, debug);

		logger.info("-------------------------------------------------------------------------");
		logger.info("-------------------------------------------------------------------------");
		logger.info("                         INITIALIZATION COMPLETE                         ");
		logger.info("-------------------------------------------------------------------------");
		logger.info("-------------------------------------------------------------------------");

		logger.info("                           DEBUG    = " + debug + "                      ");
		logger.info("                           SCANNING = " + scanning + "                   ");
		logger.info("-------------------------------------------------------------------------");
		logger.info("-------------------------------------------------------------------------");
		
		User currentUser = api.check().get();
		logger.info("current user = " +  currentUser.getLogin());
		List<Game> dranGames;

		if(scanning)
		{

		}
		else
		{
			dranGames = api.getUserDran(currentUser.getId()).get();
			logger.info("dran games   = " + dranGames.size());
			mover.processGames(dranGames);
		}

		logger.info("-------------------------------------------------------------------------");
		logger.info("-------------------------------------------------------------------------");

		if(KaroAPI.getExecutor() != null)
		{
			logger.info("stopping KaroAPI threads...");
			KaroAPI.getExecutor().shutdownNow();
		}

		api = null;
		cache = null;

		logger.info("                           PROGRAM  TERMINATED                           ");
		logger.info("-------------------------------------------------------------------------");
		logger.info("-------------------------------------------------------------------------");

		System.exit(0);
	}
}