package ultimate.karoapi4j.model.extended;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.JSONUtil.ToIDConverter;
import ultimate.karoapi4j.utils.JSONUtil.ToIDListConverter;

// TODO
public class GameSeries
{
	// type specific settings
	// relevant game series types __________________________________________________________________ACo_Bal_KO__KLC_Lig_Spl_
	// int
	public static final String				NUMBER_OF_GAMES				= "maps";				// ______________________X__
	public static final String				NUMBER_OF_MAPS				= "maps";				// ______X__________________
	public static final String				NUMBER_OF_TEAMS				= "teams";				// __X_______X_______X______
	public static final String				NUMBER_OF_TEAMS_PER_MATCH	= "teamsPerMatch";		// __X______________________
	public static final String				NUMBER_OF_ROUND				= "round";				// __________X___X__________
	public static final String				NUMBER_OF_GROUPS			= "groups";				// ______________X__________
	public static final String				NUMBER_OF_LEAGUES			= "leagues";			// ______________X__________
	public static final String				NUMBER_OF_GAMES_PER_PAIR	= "gamesPerPair";		// __X_______X_______X______
	public static final String				MIN_PLAYERS_PER_GAME		= "minPlayersPerGame";	// ______________________X__
	public static final String				MAX_PLAYERS_PER_GAME		= "maxPlayersPerGame";	// ______________________X__
	public static final String				MIN_PLAYERS_PER_TEAM		= "minPlayersPerTeam";	// __X_______X_______X______
	public static final String				MAX_PLAYERS_PER_TEAM		= "maxPlayersPerTeam";	// __X_______X_______X______
	// boolean
	public static final String				USE_HOME_MAPS				= "useHomeMaps";		// __X_______X_______X______
	public static final String				USE_CREATOR_TEAM			= "useCreatorTeam";		// __X_______X_______X______
	public static final String				SHUFFLE_TEAMS				= "shuffleTeams";		// __X_______X_______X______
	public static final String				AUTO_NAME_TEAMS				= "autoNameTeams";		// __X_______X_______X______
	public static final String				ALLOW_MULTIPLE_TEAMS		= "allowMultipleTeams";	// __X_______X_______X______

	// universal settings
	protected EnumGameSeriesType			type;
	@JsonInclude(value = Include.NON_DEFAULT)
	protected boolean						teamBased;
	protected String						title;
	@JsonSerialize(converter = ToIDConverter.class)
	@JsonDeserialize(converter = User.FromIDConverter.class)
	protected User							creator;

	// TODO
	protected boolean						creatorGiveUp;
	protected boolean						ignoreInvitable;

	// games (planned & created in one list)
	@JsonInclude(value = Include.NON_EMPTY)
	protected List<PlannedGame>				games;

	// default lists
	@JsonInclude(value = Include.NON_EMPTY)
	@JsonSerialize(converter = ToIDListConverter.class)
	@JsonDeserialize(converter = User.FromIDListConverter.class)
	protected List<User>					players;
	@JsonInclude(value = Include.NON_EMPTY)
	protected List<Team>					teams;
	@JsonInclude(value = Include.NON_EMPTY)
	@JsonSerialize(converter = ToIDListConverter.class)
	@JsonDeserialize(converter = Map.FromIDListConverter.class)
	protected List<Map>						maps;
	@JsonInclude(value = Include.NON_NULL)
	protected Rules							rules;

	// parameterized lists
	@JsonInclude(value = Include.NON_EMPTY)
	protected HashMap<String, List<User>>	playersByKey;										// TODO IDs only
	@JsonInclude(value = Include.NON_EMPTY)
	protected HashMap<String, List<Team>>	teamsByKey;
	@JsonInclude(value = Include.NON_EMPTY)
	protected HashMap<String, List<Map>>	mapsByKey;											// TODO IDs only
	@JsonInclude(value = Include.NON_EMPTY)
	protected HashMap<String, Rules>		rulesByKey;

	// type specific settings
	protected java.util.Map<String, Object>	settings;

	protected transient boolean				loaded;

	protected static Random					random						= new Random();

	public GameSeries()
	{
		this.settings = new HashMap<>();
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

	public boolean isCreatorGiveUp()
	{
		return creatorGiveUp;
	}

	public void setCreatorGiveUp(boolean creatorGiveUp)
	{
		this.creatorGiveUp = creatorGiveUp;
	}

	public boolean isIgnoreInvitable()
	{
		return ignoreInvitable;
	}

	public void setIgnoreInvitable(boolean ignoreInvitable)
	{
		this.ignoreInvitable = ignoreInvitable;
	}

	public List<PlannedGame> getGames()
	{
		return games;
	}

	public void setGames(List<PlannedGame> games)
	{
		this.games = games;
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

	public Object get(String key)
	{
		return settings.get(key);
	}

	public void set(String key, Object value)
	{
		settings.put(key, value);
	}

	public void clear(String key)
	{
		settings.remove(key);
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
		this.games.add(game);
	}
}
