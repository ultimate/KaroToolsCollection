package ultimate.karoapi4j.model.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Base class for all model classes that have an Integer id
 * 
 * @author ultimate
 */
public class Identifiable
{
	/**
	 * the id
	 */
	@JsonInclude(value = Include.NON_NULL)
	private Integer id;

	/**
	 * Default constructor
	 */
	public Identifiable()
	{
		super();
	}

	/**
	 * Constructor with id
	 * 
	 * @param id
	 */
	public Identifiable(Integer id)
	{
		super();
		this.id = id;
	}

	/**
	 * Get the id
	 * 
	 * @return the id
	 */
	public Integer getId()
	{
		return id;
	}

	/**
	 * Set the id
	 * 
	 * @param id - the id
	 */
	public void setId(Integer id)
	{
		this.id = id;
	}
}
