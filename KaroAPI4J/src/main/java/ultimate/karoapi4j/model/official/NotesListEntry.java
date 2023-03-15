package ultimate.karoapi4j.model.official;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.base.Identifiable;

/**
 * POJO Game as defined by the {@link KaroAPI}.<br/>
 * <br/>
 * Used for lists in the form:<br/>
 * <code>
 * 		[
 *			{
 *		        "id": 89529,
 *		        "text": "..."
 *		    },
 *		    {
 *		        "id": 89538,
 *		        "text": "..."
 *		    },
 *		    ...
 *		]
 * </code>
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public class NotesListEntry extends Identifiable
{
	private String text;
	
	public NotesListEntry()
	{
		super();
	}

	public NotesListEntry(Integer id, String text)
	{
		super(id);
		this.text = text;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}
}
