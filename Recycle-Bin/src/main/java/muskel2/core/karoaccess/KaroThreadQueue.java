package muskel2.core.karoaccess;

import muskel2.core.threads.ThreadQueue;
import muskel2.util.RequestLogger;
import ultimate.karoapi4j.utils.threads.QueuableThread;
import ultimate.karomuskel.ui.screens.SummaryScreen;

public class KaroThreadQueue extends ThreadQueue
{
	private SummaryScreen	summaryScreen;

	private boolean			create;

	private Integer			errorCount	= 0;

	private Long			startTime	= null;

	private RequestLogger	logger = null;

	public KaroThreadQueue(int max, SummaryScreen summaryScreen, boolean create, boolean debugEnabled, boolean countEnabled, RequestLogger logger)
	{
		super(max, debugEnabled, countEnabled);
		this.summaryScreen = summaryScreen;
		this.create = create;
		this.logger = logger;
	}

	@Override
	public void begin()
	{
		if(this.startTime == null)
			this.startTime = System.currentTimeMillis();
		super.begin();
	}

	@Override
	public void notifyFinished(QueuableThread th)
	{
		if(th instanceof GameCreatorThread && this.summaryScreen != null)
		{
			if(create)
				this.summaryScreen.notifyGameCreated(((GameCreatorThread) th).getGame());
			else
				this.summaryScreen.notifyGameLeft(((GameCreatorThread) th).getGame());
		}

		super.notifyFinished(th);

		if(this.getRemainingThreads() == 0 && this.getRunningThreads() == 0)
		{
			long endTime = System.currentTimeMillis();
			System.out.println("Zeit benötigt: " + (endTime - startTime) + " ms");
			System.out.println("Fehlgeschlagene Anfragen: " + this.getErrorCount());
			this.resetErrorCount();
			this.startTime = null;
		}
	}

	@Override
	protected void launchThread(QueuableThread th)
	{
		try
		{
			// Wait some time in order not to spam the server
			Thread.sleep(100);
		}
		catch(InterruptedException e)
		{
		}
		super.launchThread(th);
	}

	public void increaseErrorCount()
	{
		synchronized(this.errorCount)
		{
			this.errorCount++;
		}
	}

	public int getErrorCount()
	{
		return this.errorCount;
	}

	public void resetErrorCount()
	{
		synchronized(this.errorCount)
		{
			this.errorCount = 0;
		}
	}

	public RequestLogger getLogger()
	{
		return logger;
	}
}
