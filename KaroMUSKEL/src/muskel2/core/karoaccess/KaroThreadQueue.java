package muskel2.core.karoaccess;

import muskel2.core.threads.QueuableThread;
import muskel2.core.threads.ThreadQueue;
import muskel2.gui.screens.SummaryScreen;

public class KaroThreadQueue extends ThreadQueue
{
	private SummaryScreen	summaryScreen;

	private boolean			create;

	public KaroThreadQueue(int max, SummaryScreen summaryScreen, boolean create, boolean debugEnabled, boolean countEnabled)
	{
		super(max, debugEnabled, countEnabled);
		this.summaryScreen = summaryScreen;
		this.create = create;
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
	}
}
