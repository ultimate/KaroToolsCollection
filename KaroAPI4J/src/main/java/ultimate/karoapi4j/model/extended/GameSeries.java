package ultimate.karoapi4j.model.extended;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.User;

public abstract class GameSeries implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	protected String			title;
	protected User				creator;
	protected Rules				rules;
	protected List<Game>		games;
	protected List<User>		players;
	protected List<Map>			maps;

	protected transient boolean	loaded;

	protected static Random		random				= new Random();

	public GameSeries()
	{
		this.games = new LinkedList<Game>();
		this.players = new LinkedList<User>();
		this.maps = new LinkedList<Map>();
	}

	public String getTitle()
	{
		return title;
	}

	public User getCreator()
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

	public List<User> getPlayers()
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

	public boolean isLoaded()
	{
		return loaded;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setCreator(User creator)
	{
		this.creator = creator;
	}

	public void setRules(Rules rules)
	{
		this.rules = rules;
	}

	public void setLoaded(boolean loaded)
	{
		this.loaded = loaded;
	}

	public final void planGames()
	{
		// TODO spieltagbasiert?!
		System.out.println("Plane Spiele...");
		this.games.clear();
		this.resetPlannedGames(this.players);
		this.planGames0();
		System.out.println("Spiele geplant: " + this.games.size());
	}

	protected abstract void planGames0();

	public abstract int getMinSupportedPlayersPerMap();

	protected void resetPlannedGames(List<User> players)
	{
		for(User player : players)
		{
			player.setPlannedGames(0);
		}
	}

	protected void increasePlannedGames(List<User> players)
	{
		for(User player : players)
		{
			player.setPlannedGames(player.getPlannedGames() + 1);
		}
	}
}
