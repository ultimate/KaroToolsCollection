package ultimate.karoapi4j.model.official;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.base.Identifiable;
import ultimate.karoapi4j.utils.JSONUtil;

/**
 * POJO Game as defined by the {@link KaroAPI}
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
@JsonInclude()
public class Game extends Identifiable
{
	//@formatter:off
	public static class FromIDConverter extends JSONUtil.FromIDConverter<Game> { public FromIDConverter() { super(Game.class); } };
	public static class FromIDListConverter extends JSONUtil.FromIDListConverter<Game> { public FromIDListConverter() { super(Game.class); } };
	//@formatter:on
	
	/*
	 * https://www.karopapier.de/api/games/44773?mapcode=1&players=1&moves=1
	 * "id": 132000,
	 * "name": "Paket !KaroIQ!",
	 * "map": { }, // see type Map
	 * "cps": true,
	 * "zzz": 2,
	 * "crashallowed": "forbidden",
	 * "startdirection": "classic",
	 * "started": true,
	 * "finished": false,
	 * "starteddate": "2021-10-16 06:59:17",
	 * "creator": "KaBotte",
	 * "next": { "id": 1641, "name": "ImThinkin" }, // reduced user
	 * "blocked": 1,
	 * "players": [ ] // see type Player
	 */
	// for id see super class
	// private int		id;	// see super class
	@JsonProperty
	private String				name;
	private Map					map;
	private boolean				cps;
	private int					zzz;
	private EnumGameTC			crashallowed;
	private EnumGameDirection	startdirection;
	private boolean				started;
	private boolean				finished;
	private Date				starteddate;
	private String				creator;
	private User				next;
	private int					blocked;
	private List<Player>		players;

	public Game()
	{
		super();
	}

	public Game(Integer id)
	{
		super(id);
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

	public boolean isCps()
	{
		return cps;
	}

	public void setCps(boolean cps)
	{
		this.cps = cps;
	}

	public int getZzz()
	{
		return zzz;
	}

	public void setZzz(int zzz)
	{
		this.zzz = zzz;
	}

	public EnumGameTC getCrashallowed()
	{
		return crashallowed;
	}

	public void setCrashallowed(EnumGameTC crashallowed)
	{
		this.crashallowed = crashallowed;
	}

	public EnumGameDirection getStartdirection()
	{
		return startdirection;
	}

	public void setStartdirection(EnumGameDirection startdirection)
	{
		this.startdirection = startdirection;
	}

	public boolean isStarted()
	{
		return started;
	}

	public void setStarted(boolean started)
	{
		this.started = started;
	}

	public boolean isFinished()
	{
		return finished;
	}

	public void setFinished(boolean finished)
	{
		this.finished = finished;
	}

	public Date getStarteddate()
	{
		return starteddate;
	}

	public void setStarteddate(Date starteddate)
	{
		this.starteddate = starteddate;
	}

	public String getCreator()
	{
		return creator;
	}

	public void setCreator(String creator)
	{
		this.creator = creator;
	}

	public User getNext()
	{
		return next;
	}

	public void setNext(User next)
	{
		this.next = next;
	}

	public int getBlocked()
	{
		return blocked;
	}

	public void setBlocked(int blocked)
	{
		this.blocked = blocked;
	}

	public List<Player> getPlayers()
	{
		return players;
	}

	public void setPlayers(List<Player> players)
	{
		this.players = players;
	}

	@Override
	public String toString()
	{
		return this.getName() + " @" + this.getMap();
	}
}
