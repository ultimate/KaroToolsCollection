package ultimate.karoapi4j.model.official;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.base.Identifiable;

/**
 * POJO PlannedGame (or game that shall be created) as defined by the {@link KaroAPI}
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public class PlannedGame extends Identifiable
{
	/*
	 * from https://www.karopapier.de/api/example/game/new
	 * "name": "Neues Spiel",
	 * "map": 105,
	 * "players": [ 2241 ],
	 * "options": { .. } // see options
	 */
	private String	name;
	private int		map;
	private int[]	players;
	private Options	options;

	// additional properties
	@JsonInclude(value = Include.NON_DEFAULT)
	private boolean	created;
	@JsonInclude(value = Include.NON_DEFAULT)
	private boolean	left;

	public PlannedGame()
	{

	}
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getMap()
	{
		return map;
	}

	public void setMap(int map)
	{
		this.map = map;
	}

	public int[] getPlayers()
	{
		return players;
	}

	public void setPlayers(int[] players)
	{
		this.players = players;
	}

	public Options getOptions()
	{
		return options;
	}

	public void setOptions(Options options)
	{
		this.options = options;
	}

	public boolean isCreated()
	{
		return created;
	}

	public void setCreated(boolean created)
	{
		this.created = created;
	}

	public boolean isLeft()
	{
		return left;
	}

	public void setLeft(boolean left)
	{
		this.left = left;
	}
}
