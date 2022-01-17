package muskel2.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public abstract class GameSeries implements Serializable
{
	private static final long		serialVersionUID	= 1L;

	protected String				title;
	protected String				patternKey;
	protected Player				creator;
	protected Rules					rules;
	protected List<Game>			games;
	protected List<Player>			players;
	protected List<Map>				maps;
	
	protected static Random			random				= new Random();

	public GameSeries(String patternKey)
	{
		this.games = new LinkedList<Game>();
		this.players = new LinkedList<Player>();
		this.maps = new LinkedList<Map>();
		this.patternKey = patternKey;
	}

	public String getTitle()
	{
		return title;
	}

	public String getPatternKey()
	{
		return patternKey;
	}

	public Player getCreator()
	{
		return creator;
	}

	public Rules getRules()
	{
		return rules;
	}

	public List<Game> getGames()
	{
		return games;
	}

	public List<Player> getPlayers()
	{
		return players;
	}

	public List<Map> getMaps()
	{
		return maps;
	}

	protected void addGame(Game game)
	{
		this.games.add(game);
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setCreator(Player creator)
	{
		this.creator = creator;
	}

	public void setRules(Rules rules)
	{
		this.rules = rules;
	}

	public abstract int getMinSupportedPlayersPerMap();
	
	protected void resetPlannedGames(List<Player> players)
	{
		for(Player player: players)
		{
			player.setGamesActOrPlanned(player.getGamesAct());
		}
	}
	
	protected void increasePlannedGames(List<Player> players)
	{
		for(Player player: players)
		{
			player.setGamesActOrPlanned(player.getGamesActOrPlanned() + 1);
		}
	}
}
