package ultimate.karomuskel;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.LoginDialog;
import ultimate.karomuskel.ui.MainFrame;

public class Launcher
{
	/**
	 * Logger-Instance
	 */
	protected static transient final Logger	logger	= LoggerFactory.getLogger(Launcher.class);
	/**
	 * The UI instance
	 */
	private static MainFrame				gui;

	private static KaroAPI					api;
	private static KaroAPICache				cache;

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

		boolean debug = false;

		String language = Language.getDefault();
		if(args.length > 0)
		{
			// TODO allow only one arg for selecting the config file
			for(String arg : args)
			{
				if(arg.equalsIgnoreCase("-d"))
					debug = true;
				else if(arg.toLowerCase().startsWith("-l="))
					language = arg.substring(3);
				else if(arg.toLowerCase().startsWith("-t="))
				{
					String maxThreadsS = null;
					try
					{
						maxThreadsS = arg.substring(3);
						int maxThreads = Integer.parseInt(maxThreadsS);
						if(maxThreads > 0)
						{
							logger.info("Setting KaroAPI thread pool size = " + maxThreads);
							KaroAPI.setExecutor(Executors.newFixedThreadPool(maxThreads));
						}
						else
						{
							logger.error("Invalid value for KaroAPI thread pool size = " + maxThreads);
						}
					}
					catch(NumberFormatException e)
					{
						logger.error("Invalid value for KaroAPI thread pool size = " + maxThreadsS);
					}
				}
			}
		}

		Language.load(language);

		if(debug)
		{
			logger.info("                              DEBUG - MODE                              ");
			logger.info("------------------------------------------------------------------------");
			logger.info("------------------------------------------------------------------------");

			api = null; // this will trigger the KaroAPI cache to create dummy instances
		}
		else
		{
			LoginDialog loginDialog = new LoginDialog();

			while(true)
			{
				int result = loginDialog.show();
				if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION)
				{
					logger.info("login canceled");
					return;
				}

				logger.info("creating KaroAPI instance: \"" + loginDialog.getUser() + "\" ... ");

				api = new KaroAPI(loginDialog.getUser(), loginDialog.getPassword());
				try
				{
					if(api.check().get() != null)
					{
						logger.info("login successful!");
						continue;
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
		}

		cache = new KaroAPICache(api);

		logger.info("-------------------------------------------------------------------------");
		logger.info("initializing cache...");
		cache.refresh().join();
		logger.info("cache initialized");

		logger.info("launching user interface");
		gui = new MainFrame("mainframe.title", cache);
		gui.requestFocus();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run()
			{
				// TODO needed?
			}
		});

		logger.info("initialization complete!");
		logger.info("-------------------------------------------------------------------------");
	}

	public static void exit()
	{
		logger.info("-------------------------------------------------------------------------");
		logger.info("terminating program");

		if(gui != null)
		{
			gui.setVisible(false);
			gui.dispose();
			gui = null;
			api = null;
			cache = null;
		}

		logger.info("program terminated");
		logger.info("-------------------------------------------------------------------------");
		logger.info("-------------------------------------------------------------------------");

		System.exit(0);
	}
}