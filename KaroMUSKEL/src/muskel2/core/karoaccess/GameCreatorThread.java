package muskel2.core.karoaccess;

import java.net.URL;

import muskel2.core.web.URLLoaderThread;
import muskel2.model.Game;
import muskel2.util.RequestLogger;

public class GameCreatorThread extends URLLoaderThread
{
	private Game			game;
	private String			successMessage;

	private boolean			inDebugMode;

	public GameCreatorThread(Game game, URL url, String parameter, String successMessage, boolean inDebugMode)
	{
		super(url, parameter);
		this.game = game;
		this.successMessage = successMessage;
		this.inDebugMode = inDebugMode;
	}

	@Override
	public void innerRun()
	{
		RequestLogger logger = ((KaroThreadQueue) this.q).getLogger();
		if(!inDebugMode)
		{
			int tries = 0;
			do
			{
				RequestLogger.LogEntry logEntry = null;
				if(logger != null)
					logEntry = logger.add(url, parameter);
				
				super.innerRun();
				
				if(logger != null && logEntry != null)
					logger.log(logEntry, this.result, this.responseCode);

				if(success())
				{
					if(this.game.getId() < 0)
					{
						String gids = "n/a";
						try
						{
							int start = result.indexOf(successMessage) + successMessage.length();
							gids = result.substring(start, result.indexOf("\"", start));
							this.game.setId(Integer.parseInt(gids));
						}
						catch(Exception e)
						{
							System.out.println("Error setting gid: " + gids);
						}
					}
//					System.out.println("Request for game '" + game.getName() + "' successful: responseCode=" + responseCode + " time=" + (end-start));
					return;
				}
//				else
//				{
//					System.out.println("Request for game '" + game.getName() + "' unsuccessful: responseCode=" + responseCode + " time=" + (end-start));
//				}

				try
				{
					// Wait some time before performing the next request.
					Thread.sleep(Math.min(2000, 500 + tries * 100));
					tries++;
					if(this.q instanceof KaroThreadQueue)
						((KaroThreadQueue) q).increaseErrorCount();
				}
				catch(InterruptedException e)
				{
				}
			} while(true);
		}
		else
		{
			try
			{
				Thread.sleep((long) (Math.random() * 1000));
			}
			catch(InterruptedException e)
			{
			}
		}
	}

	public boolean success()
	{
		return result != null && (successMessage == null || result.contains(successMessage));
	}

	public Game getGame()
	{
		return game;
	}
}
