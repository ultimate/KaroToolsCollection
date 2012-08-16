package ultimate.karoapi4j.utils.threads;


public abstract class QueuableThread extends Thread
{

	protected ThreadQueue	q;
	protected String		message;
	protected int			id;
	protected boolean		debug;

	protected boolean				running;

	public QueuableThread()
	{
		this(null);
	}

	public QueuableThread(String message)
	{
		super();
		this.message = message;
		this.debug = false;
		this.running = true;
		super.start();
	}

	public boolean isDebugEnabled()
	{
		return debug;
	}

	public void setDebugEnabled(boolean debug)
	{
		this.debug = debug;
	}

	public final String getMessage()
	{
		return message;
	}

	public final void setThreadQueue(ThreadQueue q)
	{
		if(q == null)
			throw new IllegalArgumentException("The ThreadQueue must not be null.");
		this.q = q;
	}

	@Override
	public void start()
	{
		this.start(0);
	}

	public final void start(int id)
	{
		if(id < 0)
			throw new IllegalArgumentException("The id must be a positive Integer.");
		this.id = id;
		synchronized(this)
		{
			this.notify();
		}
	}

	public void terminate()
	{
		this.running = false;
	}

	@Override
	public final void run()
	{
		while(true)
		{
			synchronized(this)
			{
				try
				{
					this.wait();
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			if(!running)
				break;
			
			if(this.isDebugEnabled())
				System.out.println(getStartString());
			innerRun();
			if(this.isDebugEnabled())
				System.out.println(getEndString());
			if(this.q != null)
				this.q.notifyFinished(this);
		}
	}

	private final String getStartString()
	{
		return "QueuableThread " + getIdString() + " started" + (this.message == null ? "" : ": " + this.message);
	}

	private final String getEndString()
	{
		return "QueuableThread " + getIdString() + " finished";
	}

	private final String getIdString()
	{
		String idS = "" + this.id;
		while(idS.length() < minDigits)
		{
			idS = "0" + idS;
		}
		return idS;
	}

	public abstract void innerRun();

	private static int	minDigits	= 5;

	public static final void setMinIdDigits(int minDigits)
	{
		QueuableThread.minDigits = minDigits;
	}

	public static final int getMinIdDigits()
	{
		return QueuableThread.minDigits;
	}
	
}
