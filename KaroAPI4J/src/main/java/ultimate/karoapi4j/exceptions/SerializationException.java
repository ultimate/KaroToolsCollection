package ultimate.karoapi4j.exceptions;

/**
 * Exception to be thrown when serialization fails.
 * @author ultimate
 *
 */
public class SerializationException extends ParsingException
{
	/**
	 * Default serialVersionUID
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * @see Exception#Exception()
	 */
	public SerializationException()
	{
		super();
	}

	/**
	 * @see Exception#Exception(String, Throwable)
	 */
	public SerializationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @see Exception#Exception(String)
	 */
	public SerializationException(String message)
	{
		super(message);
	}

	/**
	 * @see Exception#Exception(Throwable)
	 */
	public SerializationException(Throwable cause)
	{
		super(cause);
	}
}
