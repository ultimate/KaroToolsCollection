package muskel2.model;

import java.io.Serializable;
import java.util.List;

public class Game implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	private static int			newIdCounter		= -1;

	private Integer				id;
	private String				name;
	private Map					map;
	private List<Player>		players;
	private Rules				rules;

	private boolean				created;
	private boolean				left;

	public Game(String name, Map map, List<Player> players, Rules rules)
	{
		super();
		this.name = name;
		this.map = map;
		this.players = players;
		this.rules = rules;
		
		this.created = false;
		this.left = false;
		
		this.id = newIdCounter--;
	}

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
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

	public List<Player> getPlayers()
	{
		return players;
	}

	public Rules getRules()
	{
		return rules;
	}

	public boolean isCreated()
	{
		return created;
	}

	public boolean isLeft()
	{
		return left;
	}

	public void setCreated(boolean created)
	{
		this.created = created;
	}

	public void setLeft(boolean left)
	{
		this.left = left;
	}

	@Override
	public String toString()
	{
		return this.getName() + " @" + this.getMap();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (created ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (left ? 1231 : 1237);
		result = prime * result + ((map == null) ? 0 : map.getId());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((rules == null) ? 0 : rules.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		Game other = (Game) obj;
		if(created != other.created)
			return false;
		if(id == null)
		{
			if(other.id != null)
				return false;
		}
		else if(!id.equals(other.id))
			return false;
		if(left != other.left)
			return false;
		if(map == null)
		{
			if(other.map != null)
				return false;
		}
		else if(map.getId() != other.map.getId())
			return false;
		if(name == null)
		{
			if(other.name != null)
				return false;
		}
		else if(!name.equals(other.name))
			return false;
		if(rules == null)
		{
			if(other.rules != null)
				return false;
		}
		else if(!rules.equals(other.rules))
			return false;
		return true;
	}
}
