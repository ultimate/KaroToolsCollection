package ultimate.karoapi4j.model.official;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameStatus;
import ultimate.karoapi4j.model.extended.PlaceToRace;
import ultimate.karoapi4j.utils.JSONUtil;
import ultimate.karoapi4j.utils.JSONUtil.ToIDArrayConverter;
import ultimate.karoapi4j.utils.JSONUtil.ToIDConverter;

/**
 * POJO PlannedGame (or game that shall be created) as defined by the {@link KaroAPI}
 * from https://www.karopapier.de/api/example/game/new
 * "name": "Neues Spiel",
 * "map": 105,
 * "players": [ 2241 ],
 * "options": { .. } // see options
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
@JsonFilter(value = JSONUtil.FILTER_UNOFFICIAL)
public class PlannedGame
{
	private String							name;
	@JsonSerialize(using = PlaceToRace.Serializer.class)
	@JsonDeserialize(using = PlaceToRace.Deserializer.class)
	private PlaceToRace						map;
	@JsonSerialize(converter = ToIDArrayConverter.class)
	@JsonDeserialize(converter = User.FromIDArrayToSetConverter.class)
	private Set<User>						players;
	private Options							options;
	@JsonInclude(value = Include.NON_NULL)
	private Set<String>						tags;
	@JsonInclude(value = Include.NON_NULL)
	@JsonSerialize(converter = ToIDConverter.class)
	@JsonDeserialize(converter = Game.FromIDConverter.class)
	private Game							game;

	// additional properties
	@JsonInclude(value = Include.NON_DEFAULT)
	private boolean							created;
	@JsonInclude(value = Include.NON_DEFAULT)
	private boolean							left;
	@JsonInclude(value = Include.NON_NULL)
	@JsonFilter(value = JSONUtil.FILTER_UNOFFICIAL)
	private String							home;
	@JsonInclude(value = Include.NON_NULL)
	@JsonFilter(value = JSONUtil.FILTER_UNOFFICIAL)
	private String							guest;

	// additional temporary properties (not serialized at all)
	@JsonIgnore
	private java.util.Map<String, String>	placeHolderValues;

	// additional temporary properties (not serialized at all) for the KaroMUSKEL or useful for game creation
	@JsonIgnore
	private EnumGameStatus					status;
	@JsonIgnore
	private Throwable						exception;

	public PlannedGame()
	{
		this.created = false;
		this.left = false;
		this.players = new LinkedHashSet<>();
	}

	public PlannedGame(String name, PlaceToRace map, Set<User> players, Options options, Set<String> tags)
	{
		this();
		this.name = name;
		this.map = map;
		this.players = new LinkedHashSet<>(players);
		this.options = options;
		
		if(tags != null)
			this.tags = new LinkedHashSet<>(tags);
		else
			this.tags = new LinkedHashSet<>();
	}

	public PlannedGame(String name, PlaceToRace map, Set<User> players, Options options, Set<String> tags, java.util.Map<String, String> placeHolderValues)
	{
		this(name, map, players, options, tags);
		this.placeHolderValues = placeHolderValues;
	}
	
	public PlannedGame(Game g, KaroAPICache karoAPICache)
	{
		this();
		
		this.name = g.getName();
		this.map = g.getMap();
		
		this.players = new LinkedHashSet<>();
		for(Player p: g.getPlayers())
			this.players.add(karoAPICache.getUser(p.getId()));
		
		this.options = new Options(g.getZzz(), g.isCps(), g.getStartdirection(), g.getCrashallowed());
		
		if(g.getTags() != null)
			this.tags = new LinkedHashSet<>(g.getTags());
		else
			this.tags = new LinkedHashSet<>();
		
		this.game = g;
		this.created = true;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public PlaceToRace getMap()
	{
		return map;
	}

	public void setMap(PlaceToRace map)
	{
		this.map = map;
	}

	public Set<User> getPlayers()
	{
		return players;
	}

	public void setPlayers(Set<User> players)
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

	public Set<String> getTags()
	{
		return tags;
	}

	public void setTags(Set<String> tags)
	{
		this.tags = tags;
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

	public String getHome()
	{
		return home;
	}

	public void setHome(String home)
	{
		this.home = home;
	}

	public String getGuest()
	{
		return guest;
	}

	public void setGuest(String guest)
	{
		this.guest = guest;
	}

	public java.util.Map<String, String> getPlaceHolderValues()
	{
		return placeHolderValues;
	}

	public void setPlaceHolderValues(java.util.Map<String, String> placeHolderValues)
	{
		this.placeHolderValues = placeHolderValues;
	}

	public EnumGameStatus getStatus()
	{
		return status;
	}

	public void setStatus(EnumGameStatus status)
	{
		this.status = status;
	}

	public Throwable getException()
	{
		return exception;
	}

	public void setException(Throwable exception)
	{
		this.exception = exception;
	}
	
	@JsonIgnore
	public boolean hasException()
	{
		return this.exception != null;
	}
	
	@JsonIgnore
	public void clearException()
	{
		this.exception = null;
	}
}
