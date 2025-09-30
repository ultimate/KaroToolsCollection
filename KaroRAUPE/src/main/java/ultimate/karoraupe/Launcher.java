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
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karoapi4j.utils.Version;

/**
 * This is the Launcher for the KaroRAUPE. It works in two modes:
 * <ul>
 * <li>no arg --&gt; one time check & move</li>
 * <li>arg=-c --&gt; scanning mode (will check regularly for games and move)</li>
 * </ul>
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
	 * {@link DateFormat} for log output
	 */
	private static final DateFormat			DATE_FORMAT	= new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

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
	 * The main to start the KaroRAUPE.<br>
	 * Supported args:
	 * <ul>
	 * <li>-d = debug-mode (= API mock)</li>
	 * <li>-s = scanning-mode (will check regularly for games and move)</li>
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

		// parse all arguments
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

		logger.info("-------------------------------------------------------------------------");

		// load properties from args
		logger.info("loading properties: " + configFile);
		Properties config = PropertiesUtil.loadProperties(new File(configFile));

		// add a "watchdog" to make sure that api initialization does not hang
		final CountDownLatch initComplete = new CountDownLatch(1);
		new Thread(() -> {
			try
			{
				if(initComplete.await(KaroAPI.getInitTimeout() * 3 / 2, TimeUnit.SECONDS))
				{
					logger.info("KaroAPI intialization successful");
					return;
				}
				else
				{
					logger.error("KaroAPI intialization timed out");
					System.exit(1);
				}
			}
			catch(InterruptedException e)
			{
				logger.error("Watchdog interrupted");
			}
		}).start();

		// init KaroAPI
		logger.info("initiating KaroAPI");
		KaroAPI.setApplication("KaroRAUPE", getVersion());
		logger.info("user agent = " + KaroAPI.getUserAgent());
		api = new KaroAPI(config.getProperty("karoAPI.user"), config.getProperty("karoAPI.password"));
		// Note: we don't need a KaroAPICache because we always want to have the latest information

		User currentUser = api.check().get();
		logger.info("current user = " + currentUser.getLogin());

		// notify the watchdog
		initComplete.countDown();

		// initiate the mover
		logger.info("initiating Mover");
		Mover mover = new Mover(api, config, debug);

		if(scanning)
		{
			logger.info("starting scanning");
			// scanning mode --> the Mover.checkAndProcessGames will be called periodically
			int interval = Integer.parseInt(mover.getGlobalConfig().getProperty("karoraupe.interval")) * Mover.TIME_SCALE;
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
				public void run()
				{
					latch.countDown();
				}
			});
			latch.await();
		}
		else
		{
			// one time mode
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

	private static Version getVersion()
	{
		File sourceFile = new File(Launcher.class.getProtectionDomain()
				  .getCodeSource()
				  .getLocation()
				  .getPath());
		
		Pattern versionPattern = Pattern.compile("\\d+(\\.\\d+)+");
		Matcher matcher = versionPattern.matcher(sourceFile.getName());
		
		if(matcher.find())
			return new Version(matcher.group());
		
		return null;
	}
}