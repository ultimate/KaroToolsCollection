package muskel2.core.threads;

import java.util.concurrent.LinkedBlockingQueue;

public class ThreadQueue
{
	private LinkedBlockingQueue<QueuableThread>	q;
	private int									max;
	private Integer								noT;
	private int									currId;
	private boolean								debugEnabled;
	private boolean								countEnabled;
	private Object								synco;
	private Integer								planned;
	private Integer								finished;

	public ThreadQueue(int max, boolean debugEnabled, boolean countEnabled)
	{
		this.q = new LinkedBlockingQueue<QueuableThread>();
		this.max = max;
		this.noT = 0;
		this.currId = 1;
		this.debugEnabled = debugEnabled;
		this.countEnabled = countEnabled;
		this.synco = new Object();
		this.planned = 0;
		this.finished = 0;
	}

	public ThreadQueue(int max)
	{
		this.q = new LinkedBlockingQueue<QueuableThread>();
		this.max = max;
		this.noT = 0;
		this.currId = 1;
		this.debugEnabled = false;
		this.countEnabled = false;
		this.synco = new Object();
		this.planned = 0;
		this.finished = 0;
	}

	public boolean isDebugEnabled()
	{
		return debugEnabled;
	}

	public void setDebugEnabled(boolean debugEnabled)
	{
		this.debugEnabled = debugEnabled;
	}

	public boolean iscountEnabled()
	{
		return countEnabled;
	}

	public void setcountEnabled(boolean countEnabled)
	{
		this.countEnabled = countEnabled;
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
		synchronized(this.planned)
		{
			this.planned++;
		}
	}

	public void begin()
	{
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
		synchronized(this.finished)
		{
			this.finished++;
			if(this.countEnabled)
				printCount();
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
			while(this.noT > 0)
				this.synco.wait();
		}
	}

	private void launchThreads()
	{
		this.q.size();
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

	public synchronized void printCount()
	{
		String fin = "" + this.finished;
		String plan = "" + this.planned;
		while(fin.length() < plan.length())
			fin = " " + fin;
		String result = fin + "/" + plan;
		for(int i = 0; i < result.length(); i++)
			System.out.print("\b");
		System.out.print(result);
	}
}
