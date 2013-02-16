package muskel2.core.threads;


public abstract class QueuableThread extends Thread
{

	protected ThreadQueue	q;
	protected String		message;
	protected int			id;
	protected boolean		debug;

	public QueuableThread()
	{
		super();
		this.message = null;
		this.debug = false;
	}

	public QueuableThread(String message)
	{
		super();
		this.message = message;
		this.debug = false;
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

	public final void start()
	{
		this.id = 0;
		super.start();
	}

	public final void start(int id)
	{
		if(id <= 0)
			throw new IllegalArgumentException("The id must be a positive Integer.");
		this.id = id;
		super.start();
	}

	@Override
	public final void run()
	{
		if(this.isDebugEnabled())
			System.out.println(getStartString());
		innerRun();
		if(this.isDebugEnabled())
			System.out.println(getEndString());
		if(this.q != null)
			this.q.notifyFinished(this);
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
