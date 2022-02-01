package ultimate.karoapi4j.model.extended;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;

// TODO
public abstract class GameSeries implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	protected String			title;
	protected User				creator;
	protected Rules				rules;
	protected Object			settings;
	protected List<PlannedGame>	plannedGames;
	protected List<Game>		createdGames;
	protected List<User>		players;
	protected List<Map>			maps;

	protected transient boolean	loaded;

	protected static Random		random				= new Random();

	public GameSeries()
	{
		this.plannedGames = new LinkedList<PlannedGame>();
		this.createdGames = new LinkedList<Game>();
		this.players = new LinkedList<User>();
		this.maps = new LinkedList<Map>();
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public User getCreator()
	{
		return creator;
	}

	public void setCreator(User creator)
	{
		this.creator = creator;
	}

	public Rules getRules()
	{
		return rules;
	}

	public void setRules(Rules rules)
	{
		this.rules = rules;
	}

	public Object getSettings()
	{
		return settings;
	}

	public void setSettings(Object settings)
	{
		this.settings = settings;
	}

	public List<PlannedGame> getPlannedGames()
	{
		return plannedGames;
	}

	public void setPlannedGames(List<PlannedGame> plannedGames)
	{
		this.plannedGames = plannedGames;
	}

	public List<Game> getCreatedGames()
	{
		return createdGames;
	}

	public void setCreatedGames(List<Game> createdGames)
	{
		this.createdGames = createdGames;
	}

	public List<User> getPlayers()
	{
		return players;
	}

	public void setPlayers(List<User> players)
	{
		this.players = players;
	}

	public List<Map> getMaps()
	{
		return maps;
	}

	public void setMaps(List<Map> maps)
	{
		this.maps = maps;
	}

	public boolean isLoaded()
	{
		return loaded;
	}

	public void setLoaded(boolean loaded)
	{
		this.loaded = loaded;
	}

	protected void planGame(PlannedGame game)
	{
		this.plannedGames.add(game);
	}

	public final void planGames()
	{
		// TODO spieltagbasiert?!
		System.out.println("Plane Spiele...");
		this.plannedGames.clear();
		this.resetPlannedGames(this.players);
		this.planGames0();
		System.out.println("Spiele geplant: " + this.plannedGames.size());
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
