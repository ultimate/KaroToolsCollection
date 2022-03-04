package ultimate.karoapi4j.exceptions;

import ultimate.karoapi4j.model.extended.GameSeries;

/**
 * Exception to be thrown when something fails with a {@link GameSeries}.
 * 
 * @author ultimate
 */
public class GameSeriesException extends RuntimeException
{
	/**
	 * Default serialVersionUID
	 */
	private static final long	serialVersionUID	= 1L;
	
	/**
	 * argument that specifies the cause
	 */
	private String specification;
	/**
	 * argument that specifies the value
	 */
	private String value;

	/**
	 * @see Exception#Exception()
	 */
	public GameSeriesException()
	{
		super();
	}

	/**
	 * @see Exception#Exception(String, Throwable)
	 */
	public GameSeriesException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @see Exception#Exception(String)
	 * @param specification - an additional argument that specifies the cause
	 */
	public GameSeriesException(String message, String specification)
	{
		super(message);
		this.specification = specification;
	}

	/**
	 * @see Exception#Exception(String)
	 * @param specification - an additional argument that specifies the cause
	 * @param value - an additional argument that specifies the value for the cause
	 */
	public GameSeriesException(String message, String specification, String value)
	{
		super(message);
		this.specification = specification;
		this.value = value;
	}

	/**
	 * @see Exception#Exception(String)
	 */
	public GameSeriesException(String message)
	{
		super(message);
	}

	/**
	 * @see Exception#Exception(Throwable)
	 */
	public GameSeriesException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * get the argument that specifies the cause
	 */
	public String getSpecification()
	{
		return specification;
	}

	/**
	 * argument that specifies the value
	 */
	public String getValue()
	{
		return value;
	}
}
