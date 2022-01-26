package ultimate.karoapi4j.utils;

import ultimate.karoapi4j.exceptions.ParsingException;
//TODO javadoc
public interface Parser<IN, OUT>
{
	public OUT parse(IN in) throws ParsingException;
}
