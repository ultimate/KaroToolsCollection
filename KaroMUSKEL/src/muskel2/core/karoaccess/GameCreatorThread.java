package muskel2.core.karoaccess;

import java.net.URL;

import muskel2.core.web.URLLoaderThread;
import muskel2.model.Game;

public class GameCreatorThread extends URLLoaderThread
{
	private Game	game;
	private String	successMessage;

	private boolean	inDebugMode;

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
		if(!inDebugMode)
		{
			int tries = 0;
			do
			{
				super.innerRun();

				if(success())
					return;

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
