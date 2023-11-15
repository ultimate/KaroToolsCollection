package ultimate.karoapi4j.exceptions;

/**
 * Exception to be thrown when parsing fails.
 * 
 * @author ultimate
 */
public class ParsingException extends RuntimeException
{
	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @see Exception#Exception()
	 */
	public ParsingException()
	{
		super();
	}

	/**
	 * @see Exception#Exception(String, Throwable)
	 */
	public ParsingException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @see Exception#Exception(String)
	 */
	public ParsingException(String message)
	{
		super(message);
	}

	/**
	 * @see Exception#Exception(Throwable)
	 */
	public ParsingException(Throwable cause)
	{
		super(cause);
	}
}
