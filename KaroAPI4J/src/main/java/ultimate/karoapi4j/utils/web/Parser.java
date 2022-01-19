package ultimate.karoapi4j.utils.web;

import ultimate.karoapi4j.exceptions.ParsingException;

public interface Parser<IN, OUT>
{
	public OUT parse(IN in) throws ParsingException;
}
