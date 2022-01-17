package muskel2.model.help;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import muskel2.model.Map;
import muskel2.model.Player;

public class Team implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	private String				name;
	private List<Player>		players;
	private Map					homeMap;

	public Team(String name, List<Player> players)
	{
		this.name = name;
		this.players = new LinkedList<Player>(players);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<Player> getPlayers()
	{
		return players;
	}

	public void setPlayers(List<Player> players)
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
