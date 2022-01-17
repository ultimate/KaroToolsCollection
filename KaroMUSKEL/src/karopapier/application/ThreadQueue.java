package karopapier.application;

import java.util.concurrent.LinkedBlockingQueue;

public class ThreadQueue
{
	private LinkedBlockingQueue<QueuableThread>	q;
	private int									max;
	private Integer								noT;
	private int									currId;
	private boolean								debugEnabled;
	private Object								synco;

	public ThreadQueue(int max, boolean debugEnabled)
	{
		this.q = new LinkedBlockingQueue<QueuableThread>();
		this.max = max;
		this.noT = 0;
		this.currId = 1;
		this.debugEnabled = debugEnabled;
		this.synco = new Object();
	}

	public ThreadQueue(int max)
	{
		this.q = new LinkedBlockingQueue<QueuableThread>();
		this.max = max;
		this.noT = 0;
		this.currId = 1;
		this.debugEnabled = false;
		this.synco = new Object();
	}

	public boolean isDebugEnabled()
	{
		return debugEnabled;
	}

	public void setDebugEnabled(boolean debugEnabled)
	{
		this.debugEnabled = debugEnabled;
	}

	public int getMax()
	{
		return this.max;
	}

	public void addThread(QueuableThread th)
	{
		if(th == null)
			throw new IllegalArgumentException("The Thread must not be null!");
		th.setThreadQueue(this);
		th.setDebugEnabled(this.debugEnabled);
		this.q.add(th);
		launchThreads();
	}

	public void notifyFinished(QueuableThread th)
	{
		if(th == null)
			throw new IllegalArgumentException("The Thread must not be null!");
		synchronized(this.noT)
		{
			this.noT--;
		}
		synchronized(this.synco)
		{
			this.synco.notifyAll();
		}
		launchThreads();
	}

	public void waitForFinisched() throws InterruptedException
	{
		synchronized(this.synco)
		{
			while(this.q.size() > 0)
				this.synco.wait();
		}
	}

	private void launchThreads()
	{
		while(this.noT < this.max)
		{
			QueuableThread th = this.q.poll();
			if(th == null)
				break;
			launchThread(th);
		}
	}

	private void launchThread(QueuableThread th)
	{
		th.start(this.currId++);
		synchronized(this.noT)
		{
			this.noT++;
		}
	}

	public void setMax(int max)
	{
		this.max = max;
	}
}
