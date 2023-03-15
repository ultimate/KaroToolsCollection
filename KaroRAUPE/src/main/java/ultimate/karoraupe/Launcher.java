package ultimate.karoraupe;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
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

	private static final DateFormat		DATE_FORMAT		= new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

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
		logger.info("                          " + DATE_FORMAT.format(new Date()));
		logger.info("                          DEBUG    = " + debug + "                      ");
		logger.info("                          SCANNING = " + scanning + "                   ");

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

		logger.info("-------------------------------------------------------------------------");

		Mover mover = new Mover(api, config, debug);

		User currentUser = api.check().get();
		logger.info("current user = " + currentUser.getLogin());

		if(scanning)
		{
			int interval = Integer.parseInt(mover.getGlobalConfig().getProperty("karoraupe.interval"));
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run()
				{
					mover.checkAndProcessGames();
				}
			}, 0, interval);
			
			CountDownLatch latch = new CountDownLatch(1);
			Runtime.getRuntime().addShutdownHook(new Thread() {
		        public void run() {
		            latch.countDown();
		        }
		    });
			latch.await();
		}
		else
		{
			mover.checkAndProcessGames();
		}

		logger.info("-------------------------------------------------------------------------");

		if(KaroAPI.getExecutor() != null)
		{
			logger.info("stopping KaroAPI threads...");
			KaroAPI.getExecutor().shutdownNow();
		}

		api = null;

		logger.info("                           PROGRAM  TERMINATED                           ");
		logger.info("-------------------------------------------------------------------------");

		System.exit(0);
	}
}