package ultimate.karoapi4j.exceptions;

import ultimate.karoapi4j.KaroAPI;

/**
 * Exception to be thrown when something fails with the {@link KaroAPI}.
 * 
 * @author ultimate
 */
public class KaroAPIException extends RuntimeException
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
	 * @see Exception#Exception()
	 */
	public KaroAPIException()
	{
		super();
	}

	/**
	 * @see Exception#Exception(String, Throwable)
	 */
	public KaroAPIException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @see Exception#Exception(String)
	 * @param specification - an additional argument that specifies the cause
	 */
	public KaroAPIException(String message, String specification)
	{
		super(message);
		this.specification = specification;
	}

	/**
	 * @see Exception#Exception(String)
	 */
	public KaroAPIException(String message)
	{
		super(message);
	}

	/**
	 * @see Exception#Exception(Throwable)
	 */
	public KaroAPIException(Throwable cause)
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
}
