package ultimate.karoapi4j.utils.threads;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public abstract class QueuableThread<T> extends Thread
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger	logger	= LogManager.getLogger(getClass() + "[" + this.getName() + "]");

	protected ThreadQueue				q;
	protected boolean					debug;
	protected CountDownLatch			latch;
	protected Consumer<T>				consumer;
	protected Exception					exception;

	public QueuableThread()
	{
		this(Long.toHexString(System.currentTimeMillis()));
	}

	public QueuableThread(String name)
	{
		super(name);
		this.debug = false;
		this.latch = new CountDownLatch(1);
	}

	public boolean isDebugEnabled()
	{
		return debug;
	}

	public void setDebugEnabled(boolean debug)
	{
		this.debug = debug;
	}

	public final void setThreadQueue(ThreadQueue q)
	{
		if(q == null)
			throw new IllegalArgumentException("The ThreadQueue must not be null.");
		this.q = q;
	}

	public T doBlocking() throws InterruptedException
	{
		doAsync(null);
		waitForFinished();
		return getResult();
	}

	public void doAsync(Consumer<T> consumer)
	{
		this.consumer = consumer;
		if(this.q != null)
			this.q.addThread(this);
		else
			super.start();
	}

	@Override
	public final void run()
	{
		logger.debug("QueuableThread started");
		innerRun();
		logger.debug("QueuableThread finished");
		if(this.q != null)
			this.q.notifyFinished(this);
		this.latch.countDown();
		if(this.consumer != null)
			consumer.accept(getResult());
	}

	public boolean isFinished()
	{
		return latch.getCount() == 0;
	}

	public void waitForFinished() throws InterruptedException
	{
		latch.await();
	}

	public boolean hasException()
	{
		return this.exception != null;
	}

	public Exception getException()
	{
		return this.exception;
	}

	public abstract void innerRun();

	public abstract T getResult();
}
