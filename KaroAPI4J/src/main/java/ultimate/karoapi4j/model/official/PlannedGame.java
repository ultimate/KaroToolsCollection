package ultimate.karoapi4j.model.official;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.utils.JSONUtil.ToIDConverter;
import ultimate.karoapi4j.utils.JSONUtil.ToIDListConverter;

/**
 * POJO PlannedGame (or game that shall be created) as defined by the {@link KaroAPI}
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public class PlannedGame
{
	/*
	 * from https://www.karopapier.de/api/example/game/new
	 * "name": "Neues Spiel",
	 * "map": 105,
	 * "players": [ 2241 ],
	 * "options": { .. } // see options
	 */
	private String		name;
	@JsonSerialize(converter = ToIDConverter.class)
	@JsonDeserialize(converter = Map.FromIDConverter.class)
	private Map			map;
	@JsonSerialize(converter = ToIDListConverter.class)
	@JsonDeserialize(converter = User.FromIDListConverter.class)
	private List<User>	players	= new LinkedList<>();
	private Options		options;
	@JsonInclude(value = Include.NON_NULL)
	@JsonSerialize(converter = ToIDConverter.class)
	@JsonDeserialize(converter = Game.FromIDConverter.class)
	private Game		game;

	// additional properties
	@JsonInclude(value = Include.NON_DEFAULT)
	private boolean		created;
	@JsonInclude(value = Include.NON_DEFAULT)
	private boolean		left;

	public PlannedGame()
	{
		this.created = false;
		this.left = false;
	}

	public PlannedGame(String name, Map map, List<User> players, Options options)
	{
		this();
		this.name = name;
		this.map = map;
		this.players = players;
		this.options = options;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Map getMap()
	{
		return map;
	}

	public void setMap(Map map)
	{
		this.map = map;
	}

	public List<User> getPlayers()
	{
		return players;
	}

	public void setPlayers(List<User> players)
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

	public Game getGame()
	{
		return game;
	}

	public void setGame(Game game)
	{
		this.game = game;
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
