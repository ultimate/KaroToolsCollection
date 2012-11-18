package ultimate.karoapi4j.model.official;

import java.net.URL;
import java.util.Date;
import java.util.List;

import ultimate.karoapi4j.enums.EnumDirection;
import ultimate.karoapi4j.enums.EnumTC;
import ultimate.karoapi4j.model.extended.Rules;

public class Game
{
	/*
	 * "id" : 44773,
	 * "name" : "Runde um Runde nehmen wir jede Ecke und bleiben auf der Strecke!",
	 * "map" : 43,
	 * "cps" : true,
	 * "zzz" : 2,
	 * "tcrash" : "forbidden",
	 * "dir" : "classic",
	 * "started" : true,
	 * "creator" : "Madeleine",
	 * "created" : "2009-02-24 11:05:04",
	 * "finished" : true,
	 * "dranId" : 26,
	 * "dran" : "Dummy nicht einladen",
	 * "blocked" : 116,
	 * "preview" : "http:\/\/www.karopapier.de\/pre\/44773.png",
	 * "location" : "http:\/\/www.karopapier.de\/showmap.php?GID=44773"
	 */
	// Standard JSON Fields
	private int				id;
	private String			name;
	private Map				map;
	private boolean			cps;
	private int				zzz;
	private EnumTC			tcrash;
	private EnumDirection	dir;
	private boolean			started;
	private User			creator;
	private Date			created;
	private boolean			finished;
	private int				dranId;
	private User			dran;
	private int				blocked;
	private URL				preview;
	private URL				location;
	// additional Fields
	private boolean			creatorLeft;
	// Further JSON Fields
	private List<Player>	players;

	public Game()
	{
		super();
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public Map getMap()
	{
		return map;
	}

	public boolean isCps()
	{
		return cps;
	}

	public int getZzz()
	{
		return zzz;
	}

	public EnumTC getTcrash()
	{
		return tcrash;
	}

	public EnumDirection getDir()
	{
		return dir;
	}

	public boolean isStarted()
	{
		return started;
	}

	public User getCreator()
	{
		return creator;
	}

	public Date getCreated()
	{
		return created;
	}

	public boolean isFinished()
	{
		return finished;
	}

	public int getDranId()
	{
		return dranId;
	}

	public User getDran()
	{
		return dran;
	}

	public int getBlocked()
	{
		return blocked;
	}

	public URL getPreview()
	{
		return preview;
	}

	public URL getLocation()
	{
		return location;
	}

	public boolean hasCreatorLeft()
	{
		return creatorLeft;
	}

	public List<Player> getPlayers()
	{
		return players;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setMap(Map map)
	{
		this.map = map;
	}

	public void setCps(boolean cps)
	{
		this.cps = cps;
	}

	public void setZzz(int zzz)
	{
		this.zzz = zzz;
	}

	public void setTcrash(EnumTC tcrash)
	{
		this.tcrash = tcrash;
	}

	public void setDir(EnumDirection dir)
	{
		this.dir = dir;
	}

	public void setStarted(boolean started)
	{
		this.started = started;
	}

	public void setCreator(User creator)
	{
		this.creator = creator;
	}

	public void setCreated(Date created)
	{
		this.created = created;
	}

	public void setFinished(boolean finished)
	{
		this.finished = finished;
	}

	public void setDranId(int dranId)
	{
		this.dranId = dranId;
	}

	public void setDran(User dran)
	{
		this.dran = dran;
	}

	public void setBlocked(int blocked)
	{
		this.blocked = blocked;
	}

	public void setPreview(URL preview)
	{
		this.preview = preview;
	}

	public void setLocation(URL location)
	{
		this.location = location;
	}

	public void setCreatorLeft(boolean creatorLeft)
	{
		this.creatorLeft = creatorLeft;
	}

	public void setPlayers(List<Player> players)
	{
		this.players = players;
	}
	
	public Game applyRules(Rules rules)
	{
		this.cps = rules.getCPs();
		this.dir = rules.getDirection();
		this.tcrash = rules.getTC();
		this.zzz = rules.getZzz();
		return this;
	}

	@Override
	public String toString()
	{
		return this.getName() + " @" + this.getMap();
	}
}
