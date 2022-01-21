package ultimate.karoapi4j.model.official;

import java.util.Date;
import java.util.List;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.extended.Rules;

public class Game
{
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
	private int					id;
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
	// additional Fields
	private boolean				creatorLeft;

	public Game()
	{
		super();
	}

	public Game(int id)
	{
		this();
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
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

	public boolean isCreatorLeft()
	{
		return creatorLeft;
	}

	public void setCreatorLeft(boolean creatorLeft)
	{
		this.creatorLeft = creatorLeft;
	}

	public Game applyRules(Rules rules)
	{
		this.cps = rules.getCPs();
		this.startdirection = rules.getDirection();
		this.crashallowed = rules.getTC();
		this.zzz = rules.getZzz();
		return this;
	}

	@Override
	public String toString()
	{
		return this.getName() + " @" + this.getMap();
	}
}
