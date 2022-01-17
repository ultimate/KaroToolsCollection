package ultimate.karopapier.muskelx.core.karoaccess;

import muskel2.core.threads.QueuableThread;
import muskel2.core.threads.ThreadQueue;

public class KaroThreadQueue extends ThreadQueue
{
	private Notifyable	notifyable;

	private boolean			create;

	public KaroThreadQueue(int max, Notifyable notifyable, boolean create, boolean debugEnabled, boolean countEnabled)
	{
		super(max, debugEnabled, countEnabled);
		this.notifyable = notifyable;
		this.create = create;
	}

	@Override
	public void notifyFinished(QueuableThread th)
	{
		if(th instanceof GameCreatorThread && this.notifyable != null)
		{
			if(create)
				this.notifyable.notifyGameCreated(((GameCreatorThread) th).getGame());
			else
				this.notifyable.notifyGameLeft(((GameCreatorThread) th).getGame());
		}
		super.notifyFinished(th);
	}
}
