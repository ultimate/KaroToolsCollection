package ultimate.karoapi4j.model.official;

import java.util.List;

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
 *		        "moves": [ ... ]
 *		    },
 *		    {
 *		        "id": 89538,
 *		        "moves": [ ... ]
 *		    },
 *		    ...
 *		]
 * </code>
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public class MovesListEntry extends Identifiable
{
	private List<Move> moves;
	
	public MovesListEntry()
	{
		super();
	}

	public MovesListEntry(Integer id, List<Move> moves)
	{
		super(id);
		this.moves = moves;
	}

	public List<Move> getMoves()
	{
		return moves;
	}

	public void setMoves(List<Move> moves)
	{
		this.moves = moves;
	}
}
