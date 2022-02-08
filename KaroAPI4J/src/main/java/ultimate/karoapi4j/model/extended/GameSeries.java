package ultimate.karoapi4j.model.extended;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;

// TODO
public abstract class GameSeries
{
	// universal settings
	protected EnumGameSeriesType			type;
	protected boolean						teamBased;
	protected String						title;
	protected User							creator;

	// games (planned & created)
	protected List<PlannedGame>				plannedGames;
	protected List<Game>					createdGames;

	// default lists
	protected List<User>					players;
	protected List<Team>					teams;
	protected List<Map>						maps;
	protected Rules							rules;

	// parameterized lists
	protected HashMap<String, List<User>>	playersByKey;
	protected HashMap<String, List<Team>>	teamsByKey;
	protected HashMap<String, List<Map>>	mapsByKey;
	protected HashMap<String, Rules>		rulesByKey;

	// type specific settings
	protected java.util.Map<String, Object>	settings;

	protected transient boolean				loaded;

	protected static Random					random	= new Random();

	public GameSeries(EnumGameSeriesType type, boolean teamBased)
	{
		this.type = type;
		this.teamBased = teamBased;

		this.plannedGames = new LinkedList<PlannedGame>();
		this.createdGames = new LinkedList<Game>();

		this.players = new LinkedList<User>();
		this.teams = new LinkedList<Team>();

		this.maps = new LinkedList<Map>();
	}

	public EnumGameSeriesType getType()
	{
		return type;
	}

	public boolean isTeamBased()
	{
		return teamBased;
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

	public List<Team> getTeams()
	{
		return teams;
	}

	public void setTeams(List<Team> teams)
	{
		this.teams = teams;
	}

	public List<Map> getMaps()
	{
		return maps;
	}

	public void setMaps(List<Map> maps)
	{
		this.maps = maps;
	}

	public Rules getRules()
	{
		return rules;
	}

	public void setRules(Rules rules)
	{
		this.rules = rules;
	}

	public HashMap<String, List<User>> getPlayersByKey()
	{
		return playersByKey;
	}

	public void setPlayersByKey(HashMap<String, List<User>> playersByKey)
	{
		this.playersByKey = playersByKey;
	}

	public HashMap<String, List<Team>> getTeamsByKey()
	{
		return teamsByKey;
	}

	public void setTeamsByKey(HashMap<String, List<Team>> teamsByKey)
	{
		this.teamsByKey = teamsByKey;
	}

	public HashMap<String, List<Map>> getMapsByKey()
	{
		return mapsByKey;
	}

	public void setMapsByKey(HashMap<String, List<Map>> mapsByKey)
	{
		this.mapsByKey = mapsByKey;
	}

	public HashMap<String, Rules> getRulesByKey()
	{
		return rulesByKey;
	}

	public void setRulesByKey(HashMap<String, Rules> rulesByKey)
	{
		this.rulesByKey = rulesByKey;
	}

	public java.util.Map<String, Object> getSettings()
	{
		return settings;
	}

	public void setSettings(java.util.Map<String, Object> settings)
	{
		this.settings = settings;
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
