package ultimate.karoapi4j.model.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class Identifiable
{
	@JsonInclude(value = Include.NON_NULL)
	private Integer id;

	public Identifiable()
	{
		super();
	}

	public Identifiable(Integer id)
	{
		super();
		this.id = id;
	}

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}
}
