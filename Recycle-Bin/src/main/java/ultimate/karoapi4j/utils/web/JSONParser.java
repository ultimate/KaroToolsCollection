package ultimate.karoapi4j.utils.web;

import com.fasterxml.jackson.core.type.TypeReference;

import ultimate.karoapi4j.utils.JSONUtil;

@Deprecated
public class JSONParser<E> implements Parser<String, E>
{
	/**
	 * A TypeReference for JSON-Deserialization
	 */
	protected TypeReference<E> typeRef = new TypeReference<E>() {};

	public JSONParser(TypeReference<E> typeRef)
	{
		super();
		this.typeRef = typeRef;
	}

	@Override
	public E parse(String in)
	{
		return JSONUtil.deserialize(in, typeRef);
	}
}
