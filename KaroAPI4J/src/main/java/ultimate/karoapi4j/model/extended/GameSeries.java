package ultimate.karoapi4j.model.extended;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;

// TODO
public class GameSeries
{
	// universal settings
	protected EnumGameSeriesType			type;
	protected boolean						teamBased;
	protected String						title;
	protected User							creator;

	// games (planned & created)
	protected List<PlannedGame>				plannedGames;
	protected List<Game>					createdGames; // TODO IDs only

	// default lists
	protected List<User>					players; // TODO IDs only
	protected List<Team>					teams; // TODO IDs only
	protected List<Map>						maps; // TODO IDs only
	protected Rules							rules;

	// parameterized lists
	protected HashMap<String, List<User>>	playersByKey; // TODO IDs only
	protected HashMap<String, List<Team>>	teamsByKey; // TODO IDs only
	protected HashMap<String, List<Map>>	mapsByKey; // TODO IDs only
	protected HashMap<String, Rules>		rulesByKey;

	// type specific settings
	protected java.util.Map<String, Object>	settings;

	protected transient boolean				loaded;

	protected static Random					random	= new Random();

	public GameSeries()
	{
	}
	
	public GameSeries(EnumGameSeriesType type, boolean teamBased)
	{
		this();
		this.type = type;
		this.teamBased = teamBased;
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
}
