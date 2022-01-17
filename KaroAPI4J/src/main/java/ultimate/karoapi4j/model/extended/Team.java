package ultimate.karoapi4j.model.extended;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.User;

public class Team implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	private String				name;
	private List<User>			players;
	private Map					homeMap;

	public Team(String name, List<User> players)
	{
		this.name = name;
		this.players = new LinkedList<User>(players);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<User> getPlayers()
	{
		return players;
	}

	public void setPlayers(List<User> players)
	{
		this.players = players;
	}

	public Map getHomeMap()
	{
		return homeMap;
	}

	public void setHomeMap(Map homeMap)
	{
		this.homeMap = homeMap;
	}
}
