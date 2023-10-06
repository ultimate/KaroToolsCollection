package ultimate.karoapi4j.model.extended;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ultimate.karoapi4j.enums.EnumCreatorParticipation;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.JSONUtil.ToIDArrayConverter;
import ultimate.karoapi4j.utils.JSONUtil.ToIDConverter;
import ultimate.karoapi4j.utils.JSONUtil.ToIDMapConverter;

/**
 * Generic POJO for storing all the informations about a GameSeries.<br>
 * Depending on the {@link EnumGameSeriesType} of the {@link GameSeries} different fields will be used.<br>
 * For complex types predefined containers are provided which can hold the objects either universally or by key:
 * <ul>
 * <li>{@link GameSeries#players} or {@link GameSeries#playersByKey}</li>
 * <li>{@link GameSeries#teams} or {@link GameSeries#teamsByKey}</li>
 * <li>{@link GameSeries#maps} or {@link GameSeries#mapsByKey}</li>
 * <li>{@link GameSeries#rules} or {@link GameSeries#rulesByKey}</li>
 * </li>
 * For simple types, there is a map, which can support any setting: {@link GameSeries#settings}.<br>
 * For Serialization only non default values are serialized, so the JSON won't blow up.
 * 
 * @author ultimate
 */
public class GameSeries
{
	// keys for type specific settings
	// relevant game series types
	// _________________________________________________________________________________________________________________________ACo_Bal_KO__KLC_Lig_Spl_
	// int
	public static final String							NUMBER_OF_GAMES				= "games";				// ______________________X__
	public static final String							NUMBER_OF_MAPS				= "maps";				// ______X__________________
	public static final String							NUMBER_OF_TEAMS				= "teams";				// __X_______X_______X______
	public static final String							NUMBER_OF_TEAMS_PER_MATCH	= "teamsPerMatch";		// __X______________________
	public static final String							NUMBER_OF_GAMES_PER_PAIR	= "gamesPerPair";		// __X_______X_______X______
	public static final String							CURRENT_ROUND				= "round";				// __________X___X__________
	public static final String							CURRENT_REPEAT				= "repeat";				// ______________X__________
	public static final String							MIN_PLAYERS_PER_GAME		= "minPlayersPerGame";	// ______________________X__
	public static final String							MAX_PLAYERS_PER_GAME		= "maxPlayersPerGame";	// ______________________X__
	public static final String							MIN_PLAYERS_PER_TEAM		= "minPlayersPerTeam";	// __X_______X_______X______
	public static final String							MAX_PLAYERS_PER_TEAM		= "maxPlayersPerTeam";	// __X_______X_______X______
	// boolean
	public static final String							USE_HOME_MAPS				= "useHomeMaps";		// __X_______X_______X______
	public static final String							USE_CREATOR_TEAM			= "useCreatorTeam";		// __X_______X_______X______
	public static final String							SHUFFLE_TEAMS				= "shuffleTeams";		// __X_______X_______X______
	public static final String							AUTO_NAME_TEAMS				= "autoNameTeams";		// __X_______X_______X______
	public static final String							ALLOW_MULTIPLE_TEAMS		= "allowMultipleTeams";	// __X_______X_______X______
	public static final String							SMALL_FINAL					= "smallFinal";			// __________X______________
	public static final String							DUMMY_MATCHES				= "dummyMatches";		// __________________X______
	public static final String							V2_TEAM_BASED				= "teamBased";			// __X___X___X___X___X___X__

	// keys for type specific configuration: these are MAX values to prevent misuse (unless config is edited)
	// relevant game series types
	// _________________________________________________________________________________________________________________________ACo_Bal_KO__KLC_Lig_Spl_
	// int
	public static final String							CONF_MAX_TEAMS				= "maxTeams";			// __X_______X_______X______
	public static final String							CONF_TEAM_STEP_SIZE			= "teamStepSize";		// __X_______X_______X______
	public static final String							CONF_MAX_ROUNDS				= "maxRounds";			// __X_______X_______X______
	public static final String							CONF_MAX_GAMES				= "maxGames";			// ______________________X__
	public static final String							CONF_MAX_GAMES_PER_PLAYER	= "maxGamesPerPlayer";	// ______X__________________
	public static final String							CONF_MAX_MAPS				= "maxMaps";			// ______X__________________
	public static final String							CONF_KLC_GROUPS				= "groups";				// ______________X__________
	public static final String							CONF_KLC_LEAGUES			= "leagues";			// ______________X__________
	public static final String							CONF_KLC_FIRST_KO_ROUND		= "firstKORound";		// ______________X__________
	public static final String							CONF_LEAGUE_DUMMY_TEAM		= "dummyTeam";			// __________________X______

	// keys for type specific lists: these are MAX values to prevent misuse (unless config is edited)
	// relevant game series types
	// _________________________________________________________________________________________________________________________ACo_Bal_KO__KLC_Lig_Spl_
	public static final String							KEY_LEAGUE					= "league";				// ______________X__________
	public static final String							KEY_GROUP					= "group";				// ______________X__________
	public static final String							KEY_ROUND					= "roundOf";			// ______________X__________
	public static final String							KEY_REPEAT					= "repeat";				// ______________X__________

	// universal settings
	/**
	 * the type of the gameseries
	 */
	protected EnumGameSeriesType						type;
	/**
	 * the gameseries title (including placeholders)
	 */
	protected String									title;
	@JsonInclude(value = Include.NON_NULL)
	/**
	 * the gameseries tags
	 */
	private Set<String>									tags;
	/**
	 * the creator
	 */
	@JsonSerialize(converter = ToIDConverter.class)
	@JsonDeserialize(converter = User.FromIDConverter.class)
	protected User										creator;

	/**
	 * shall the creator leave all games?
	 */
	@Deprecated
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	protected boolean									creatorGiveUp;
	/**
	 * does the creator participate in the games?
	 */
	@JsonInclude(value = Include.NON_NULL)
	protected EnumCreatorParticipation					creatorParticipation;
	/**
	 * shall the game limits and invitable status be ignored?
	 */
	@JsonInclude(value = Include.NON_DEFAULT)
	protected boolean									ignoreInvitable;

	/**
	 * the list of games (planned & created in a map by key)
	 */
	@JsonInclude(value = Include.NON_EMPTY)
	protected java.util.Map<String, List<PlannedGame>>	games;

	// default lists
	/**
	 * the list of all players participating (optional)
	 */
	@JsonInclude(value = Include.NON_EMPTY)
	@JsonSerialize(converter = ToIDArrayConverter.class)
	@JsonDeserialize(converter = User.FromIDArrayToListConverter.class)
	protected List<User>								players;
	/**
	 * the list of all team participating (optional)
	 */
	@JsonInclude(value = Include.NON_EMPTY)
	protected List<Team>								teams;
	/**
	 * the list of maps used (optional)
	 */
	@JsonInclude(value = Include.NON_EMPTY)
	@JsonSerialize(converter = ToIDArrayConverter.class)
	@JsonDeserialize(converter = Map.FromIDArrayToListConverter.class)
	protected List<Map>									maps;
	/**
	 * the general rules used
	 */
	@JsonInclude(value = Include.NON_NULL)
	protected Rules										rules;

	// parameterized lists
	/**
	 * additional player lists by key (optional)
	 */
	@JsonInclude(value = Include.NON_EMPTY)
	@JsonSerialize(converter = ToIDMapConverter.class)
	@JsonDeserialize(converter = User.FromIDMapToListConverter.class)
	protected java.util.Map<String, List<User>>			playersByKey;
	/**
	 * additional team lists by key (optional)
	 */
	@JsonInclude(value = Include.NON_EMPTY)
	protected java.util.Map<String, List<Team>>			teamsByKey;
	/**
	 * additional map lists by key (optional)
	 */
	@JsonInclude(value = Include.NON_EMPTY)
	@JsonSerialize(converter = ToIDMapConverter.class)
	@JsonDeserialize(converter = Map.FromIDMapToListConverter.class)
	protected java.util.Map<String, List<Map>>			mapsByKey;
	/**
	 * additional rules by key (optional)
	 */
	@JsonInclude(value = Include.NON_EMPTY)
	protected java.util.Map<String, Rules>				rulesByKey;

	/**
	 * type specific settings
	 */
	protected java.util.Map<String, Object>				settings;

	/**
	 * was the gameseries loaded from file?
	 */
	@JsonIgnore
	protected transient boolean							loaded;

	/**
	 * Create a new GameSeries
	 * 
	 * @param type - the type of the gameseries
	 */
	@JsonCreator
	public GameSeries(@JsonProperty("type") EnumGameSeriesType type)
	{
		if(type == null)
			throw new IllegalArgumentException("type must not be null");
		this.type = type;

		this.games = new HashMap<>();

		this.players = new LinkedList<>();
		this.teams = new LinkedList<>();
		this.maps = new LinkedList<>();

		this.playersByKey = new HashMap<>();
		this.teamsByKey = new HashMap<>();
		this.mapsByKey = new HashMap<>();
		this.rulesByKey = new HashMap<>();

		this.settings = new HashMap<>();
	}

	////////////////////////////////////
	// non settable fields / properties
	////////////////////////////////////

	/**
	 * @return the type of the gameseries
	 */
	public EnumGameSeriesType getType()
	{
		return type;
	}

	////////////////////////////////////
	// normal fields
	////////////////////////////////////

	/**
	 * @return the gameseries title (including placeholders)
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @param title - the gameseries title (including placeholders)
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * @return the gameseries tags 
	 */
	public Set<String> getTags()
	{
		return tags;
	}

	/**
	 * @param tags - the gameseries tags
	 */
	public void setTags(Set<String> tags)
	{
		this.tags = tags;
	}

	/**
	 * @return the creator
	 */
	public User getCreator()
	{
		return creator;
	}

	/**
	 * @param creator - the creator
	 */
	public void setCreator(User creator)
	{
		this.creator = creator;
	}

	/**
	 * @return shall the creator leave all games?
	 */
	@Deprecated
	public boolean isCreatorGiveUp()
	{
		return this.creatorParticipation == EnumCreatorParticipation.leave;
	}

	/**
	 * @param creatorGiveUp - shall the creator leave all games?
	 */
	@Deprecated
	public void setCreatorGiveUp(boolean creatorGiveUp)
	{
		if(creatorGiveUp)
			this.creatorParticipation = EnumCreatorParticipation.leave;
		else
			this.creatorParticipation = EnumCreatorParticipation.normal;
	}

	/**
	 * @return does the creator participate in the games?
	 */
	public EnumCreatorParticipation getCreatorParticipation()
	{
		return creatorParticipation;
	}

	/**
	 * @param creatorParticipation - does the creator participate in the games?
	 */
	public void setCreatorParticipation(EnumCreatorParticipation creatorParticipation)
	{
		this.creatorParticipation = creatorParticipation;
	}

	/**
	 * @return shall the game limits and invitable status be ignored?
	 */
	public boolean isIgnoreInvitable()
	{
		return ignoreInvitable;
	}

	/**
	 * @param ignoreInvitable - shall the game limits and invitable status be ignored?
	 */
	public void setIgnoreInvitable(boolean ignoreInvitable)
	{
		this.ignoreInvitable = ignoreInvitable;
	}

	/**
	 * @return the list of games (planned & created in a map by key)
	 */
	public java.util.Map<String, List<PlannedGame>> getGames()
	{
		return games;
	}

	/**
	 * @param games - the list of games (planned & created in a map by key)
	 */
	public void setGames(java.util.Map<String, List<PlannedGame>> games)
	{
		this.games = games;
	}

	/**
	 * @return the list of all players participating (optional)
	 */
	public List<User> getPlayers()
	{
		return players;
	}

	/**
	 * @param players - the list of all players participating (optional)
	 */
	public void setPlayers(List<User> players)
	{
		this.players = players;
	}

	/**
	 * @return the list of all team participating (optional)
	 */
	public List<Team> getTeams()
	{
		return teams;
	}

	/**
	 * @param teams - the list of all team participating (optional)
	 */
	public void setTeams(List<Team> teams)
	{
		this.teams = teams;
	}

	/**
	 * @return the list of maps used (optional)
	 */
	public List<Map> getMaps()
	{
		return maps;
	}

	/**
	 * @param maps - the list of maps used (optional)
	 */
	public void setMaps(List<Map> maps)
	{
		this.maps = maps;
	}

	/**
	 * @return the general rules used
	 */
	public Rules getRules()
	{
		return rules;
	}

	/**
	 * @param rules - the general rules used
	 */
	public void setRules(Rules rules)
	{
		this.rules = rules;
	}

	/**
	 * @return additional player lists by key (optional)
	 */
	public java.util.Map<String, List<User>> getPlayersByKey()
	{
		return playersByKey;
	}

	/**
	 * @param playersByKey - additional player lists by key (optional)
	 */
	public void setPlayersByKey(java.util.Map<String, List<User>> playersByKey)
	{
		this.playersByKey = playersByKey;
	}

	/**
	 * @return additional team lists by key (optional)
	 */
	public java.util.Map<String, List<Team>> getTeamsByKey()
	{
		return teamsByKey;
	}

	/**
	 * @param teamsByKey - additional team lists by key (optional)
	 */
	public void setTeamsByKey(java.util.Map<String, List<Team>> teamsByKey)
	{
		this.teamsByKey = teamsByKey;
	}

	/**
	 * @return additional map lists by key (optional)
	 */
	public java.util.Map<String, List<Map>> getMapsByKey()
	{
		return mapsByKey;
	}

	/**
	 * @param mapsByKey - additional map lists by key (optional)
	 */
	public void setMapsByKey(java.util.Map<String, List<Map>> mapsByKey)
	{
		this.mapsByKey = mapsByKey;
	}

	/**
	 * @return additional rules by key (optional)
	 */
	public java.util.Map<String, Rules> getRulesByKey()
	{
		return rulesByKey;
	}

	/**
	 * @param rulesByKey - additional rules by key (optional)
	 */
	public void setRulesByKey(java.util.Map<String, Rules> rulesByKey)
	{
		this.rulesByKey = rulesByKey;
	}

	/**
	 * @return type specific settings
	 */
	public java.util.Map<String, Object> getSettings()
	{
		return settings;
	}

	/**
	 * @param settings - type specific settings
	 */
	public void setSettings(java.util.Map<String, Object> settings)
	{
		this.settings = settings;
	}

	/**
	 * @param key - the key
	 * @return the value from the type specific settings
	 */
	public Object get(String key)
	{
		return settings.get(key);
	}

	/**
	 * 
	 * @param key - the key
	 * @param value - the value for the type specific settings
	 */
	public void set(String key, Object value)
	{
		settings.put(key, value);
	}

	/**
	 * @param key - remove a value from the type specific settings
	 */
	public void clear(String key)
	{
		settings.remove(key);
	}

	/**
	 * @return was the gameseries loaded from file?
	 */
	public boolean isLoaded()
	{
		return loaded;
	}

	/**
	 * @param loaded - was the gameseries loaded from file?
	 */
	public void setLoaded(boolean loaded)
	{
		this.loaded = loaded;
	}
}
