package muskel2.core.exceptions;

public class GameSeriesException extends RuntimeException
{
	private static final long	serialVersionUID	= 1L;
	
	private String specification;

	public GameSeriesException()
	{
		super();
	}

	public GameSeriesException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public GameSeriesException(String message, String specification)
	{
		super(message);
		this.specification = specification;
	}

	public GameSeriesException(String message)
	{
		super(message);
	}

	public GameSeriesException(Throwable cause)
	{
		super(cause);
	}

	public String getSpecification()
	{
		return specification;
	}
}
