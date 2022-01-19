package ultimate.karoapi4j.utils.threads;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadQueue
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger	logger	= LoggerFactory.getLogger(getClass());
	
	private LinkedBlockingQueue<QueuableThread<?>>	q;
	private int										max;
	private AtomicInteger							noT;
	private boolean									debugEnabled;
	private Object									synco;
	private AtomicInteger							planned;
	private AtomicInteger							finished;

	public ThreadQueue(int max, boolean debugEnabled, boolean countEnabled)
	{
		this.q = new LinkedBlockingQueue<QueuableThread<?>>();
		this.max = max;
		this.noT = new AtomicInteger(0);
		this.debugEnabled = debugEnabled;
		this.synco = new Object();
		this.planned = new AtomicInteger(0);
		this.finished = new AtomicInteger(0);
	}

	public ThreadQueue(int max)
	{
		this(max, false, false);
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

	public void addThread(QueuableThread<?> th)
	{
		if(th == null)
			throw new IllegalArgumentException("The Thread must not be null!");
		th.setThreadQueue(this);
		th.setDebugEnabled(this.debugEnabled);
		this.q.add(th);
//		synchronized(this.planned)
//		{
			this.planned.incrementAndGet();
//		}
		logger.debug("progress = " + getProgress());
	}

	public void begin()
	{
		launchThreads();
	}

	public int getRemainingThreads()
	{
		return this.q.size();
	}

	public int getRunningThreads()
	{
		return this.noT.get();
	}

	public void notifyFinished(QueuableThread<?> th)
	{
		if(th == null)
			throw new IllegalArgumentException("The Thread must not be null!");
//		synchronized(this.noT)
//		{
			this.noT.incrementAndGet();
//		}
//		synchronized(this.finished)
//		{
			this.finished.incrementAndGet();
			logger.debug("progress = " + getProgress());
//		}
		synchronized(this.synco)
		{
			this.synco.notifyAll();
		}
		launchThreads();
	}

	public void waitForFinished() throws InterruptedException
	{
		synchronized(this.synco)
		{
			while(this.noT.get() > 0 || this.q.size() > 0)
				this.synco.wait();
		}
	}

	protected void launchThreads()
	{
		while(this.noT.get() < this.max)
		{
			QueuableThread<?> th = this.q.poll();
			if(th == null)
				break;
			launchThread(th);
		}
	}

	protected void launchThread(QueuableThread<?> th)
	{
		th.start();
//		synchronized(this.noT)
//		{
			this.noT.incrementAndGet();
//		}
	}

	public void setMax(int max)
	{
		this.max = max;
	}

	public String getProgress()
	{
		String fin = "" + this.finished.get();
		String plan = "" + this.planned.get();
		while(fin.length() < plan.length())
			fin = " " + fin;
		return fin + " / " + plan;
	}
}
