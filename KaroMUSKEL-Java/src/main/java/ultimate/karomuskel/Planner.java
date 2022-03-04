package ultimate.karomuskel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.BiFunction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.Match;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karoapi4j.model.extended.Team;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.Options;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karomuskel.ui.Language;

/**
 * The {@link Planner} is the core of the KaroMUSKEL and contains all logic for planning {@link GameSeries}. Planning will not directly create the
 * {@link Game}s, but instead return a list of {@link PlannedGame}s which can be used to create the games using
 * {@link KaroAPI#createGame(PlannedGame)}.<br>
 * Note: other than in the V2 KaroMUSKEL, the KaroMUSKEL does not need to care about threading for creating the games anymore. Instead the
 * {@link KaroAPI} already includes threading and parallel game creation (if configured appropriately) (see
 * {@link KaroAPI#setExecutor(java.util.concurrent.ExecutorService)}). This configuration is automatically performed using the values from the config
 * file if the {@link Launcher} is used to start the KaroMUSKEL including UI.
 * 
 * @see KaroAPI#setExecutor(java.util.concurrent.ExecutorService)
 * @author ultimate
 */
public class Planner
{
	/**
	 * Logger-Instance
	 */
	protected static transient final Logger	logger	= LogManager.getLogger();
	/**
	 * The {@link Random} number generator used to plan {@link GameSeries}
	 */
	private static Random					random	= new Random();

	/**
	 * The {@link KaroAPICache} used to lookup {@link User}s, {@link Map}s, etc.
	 */
	private KaroAPICache					karoAPICache;

	/**
	 * Create a new {@link Planner} with the given {@link KaroAPICache}
	 * 
	 * @param karoAPICache - the {@link KaroAPICache} used to lookup {@link User}s, {@link Map}s, etc.
	 */
	public Planner(KaroAPICache karoAPICache)
	{
		this.karoAPICache = karoAPICache;
	}

	/**
	 * Plan a {@link GameSeries} according to its {@link EnumGameSeriesType} and settings.<br>
	 * Settings will automatically be fetched from the {@link GameSeries} and the plan-function for the respective {@link EnumGameSeriesType} will be
	 * called.
	 * 
	 * @see Planner#planSeriesAllCombinations(String, List, List, Rules, int, int)
	 * @see Planner#planSeriesBalanced(String, List, java.util.Map, java.util.Map)
	 * @see Planner#planSeriesKLC(String, java.util.Map, java.util.Map, int, int, Rules, int)
	 * @see Planner#planSeriesKO(String, List, List, BiFunction, Rules, boolean, boolean)
	 * @see Planner#planSeriesLeague(String, List, List, Rules, boolean, int)
	 * @see Planner#planSeriesSimple(String, List, List, Rules, int, int)l
	 * 
	 * @param gs - the {@link GameSeries}
	 * @return the list of {@link PlannedGame}s
	 */
	public List<PlannedGame> planSeries(GameSeries gs)
	{
		if(gs == null || gs.getType() == null)
			throw new IllegalArgumentException("gameseries & type must not be null!");

		int numberOfGamesPerPair, numberOfTeamsPerMatch, round, groups, leagues, numberOfGames, maxPlayersPerGame;
		boolean useHomeMaps;

		switch(gs.getType())
		{
			case AllCombinations:
				numberOfGamesPerPair = (int) gs.get(GameSeries.NUMBER_OF_GAMES_PER_PAIR);
				numberOfTeamsPerMatch = (int) gs.get(GameSeries.NUMBER_OF_TEAMS_PER_MATCH);
				return planSeriesAllCombinations(gs.getTitle(), gs.getTeams(), gs.getMaps(), gs.getRules(), numberOfGamesPerPair, numberOfTeamsPerMatch);
			case Balanced:
				return planSeriesBalanced(gs.getTitle(), gs.getPlayers(), gs.getMapsByKey(), gs.getRulesByKey());
			case KLC:
				round = (int) gs.get(GameSeries.CURRENT_ROUND);
				groups = GameSeriesManager.getIntConfig(GameSeries.CONF_KLC_GROUPS);
				leagues = GameSeriesManager.getIntConfig(GameSeries.CONF_KLC_LEAGUES);
				return planSeriesKLC(gs.getTitle(), gs.getPlayersByKey(), gs.getMapsByKey(), leagues, groups, gs.getRules(), round);
			case KO:
				useHomeMaps = (boolean) gs.get(GameSeries.USE_HOME_MAPS);
				return planSeriesKO(gs.getTitle(), gs.getTeams(), gs.getMaps(), null, gs.getRules(), useHomeMaps, true);
			case League:
				numberOfGamesPerPair = (int) gs.get(GameSeries.NUMBER_OF_GAMES_PER_PAIR);
				useHomeMaps = (boolean) gs.get(GameSeries.USE_HOME_MAPS);
				return planSeriesLeague(gs.getTitle(), gs.getTeams(), gs.getMaps(), gs.getRules(), useHomeMaps, numberOfGamesPerPair);
			case Simple:
				numberOfGames = (int) gs.get(GameSeries.NUMBER_OF_GAMES);
				maxPlayersPerGame = (int) gs.get(GameSeries.MAX_PLAYERS_PER_GAME);
				return planSeriesSimple(gs.getTitle(), gs.getPlayers(), gs.getMaps(), gs.getRules(), numberOfGames, maxPlayersPerGame);
			default:
				return null;
		}
	}

	/**
	 * Plan the games for an {@link EnumGameSeriesType#AllCombinations} {@link GameSeries}
	 * 
	 * @param title - the title (including placeholders)
	 * @param teams - the list of {@link Team}s
	 * @param maps - the list of {@link Map}s
	 * @param rules - the {@link Rules}
	 * @param numberOfGamesPerPair - the number of games per pair/combination
	 * @param numberOfTeamsPerMatch - the number of {@link Team}s per match
	 * @return the list of {@link PlannedGame}s
	 */
	public List<PlannedGame> planSeriesAllCombinations(String title, List<Team> teams, List<Map> maps, Rules rules, int numberOfGamesPerPair, int numberOfTeamsPerMatch)
	{
		List<PlannedGame> games = new LinkedList<>();

		// create local copy of the input list
		List<Team> tmp = new LinkedList<>(teams);
		Collections.shuffle(tmp, random);

		List<Match> matches = planMatchesAllCombinations(tmp, numberOfTeamsPerMatch);

		PlannedGame game;
		Map map;
		List<User> gamePlayers;
		int count = 0;
		int dayCount;
		HashMap<String, String> placeholderValues;

		for(int round = 0; round < numberOfGamesPerPair; round++)
		{
			dayCount = 0;

			for(Match match : matches)
			{
				map = maps.get(random.nextInt(maps.size()));

				gamePlayers = new LinkedList<User>();
				gamePlayers.add(karoAPICache.getCurrentUser());
				for(Team team : match.getTeams())
				{
					for(User member : team.getMembers())
					{
						if(!gamePlayers.contains(member))
							gamePlayers.add(member);
					}
				}

				placeholderValues = new HashMap<>();
				placeholderValues.put("i", toString(count + 1, 1));
				placeholderValues.put("spieltag", toString(round + 1, 1));
				placeholderValues.put("spieltag.i", toString(dayCount + 1, 1));

				game = planGame(title, map, gamePlayers, rules, null);

				games.add(game);
				count++;
				dayCount++;
			}
		}

		return games;
	}

	/**
	 * Plan the games for a {@link EnumGameSeriesType#Balanced} {@link GameSeries}
	 * 
	 * @param title - the title (including placeholders)
	 * @param players - the list of {@link User}s
	 * @param gameDayMaps - the {@link Map}s used sorted by game day
	 * @param gameDayRules - the {@link Rules} used sorted by game day
	 * @return the list of {@link PlannedGame}s
	 */
	public List<PlannedGame> planSeriesBalanced(String title, List<User> players, java.util.Map<String, List<Map>> gameDayMaps, java.util.Map<String, Rules> gameDayRules)
	{
		if(!checkKeys(gameDayMaps, gameDayRules))
			throw new IllegalArgumentException("gameDayMaps & gameDayRules must have equals size and keys");

		List<PlannedGame> games = new LinkedList<>();

		PlannedGame game;
		List<User> gamePlayers;
		Map map;
		int count = 0;
		int dayCount;
		HashMap<String, String> placeholderValues;

		User[][][] shuffledPlayers = balancedShufflePlayers(players, new ArrayList<>(gameDayRules.values()));

		for(int day = 0; day < gameDayMaps.size(); day++)
		{
			dayCount = 0;

			int gamesBefore = games.size();

			for(int g = 0; g < shuffledPlayers[day].length; g++)
			{
				gamePlayers = new LinkedList<User>();

				for(int p = 0; p < shuffledPlayers[day][g].length; p++)
				{
					if(shuffledPlayers[day][g][p] == null)
						continue;
					gamePlayers.add(shuffledPlayers[day][g][p]);
				}
				gamePlayers.add(karoAPICache.getCurrentUser());

				map = gameDayMaps.get("" + day).get(random.nextInt(gameDayMaps.get("" + day).size()));

				placeholderValues = new HashMap<>();
				placeholderValues.put("i", toString(count + 1, 1));
				placeholderValues.put("spieltag", toString(day + 1, 1));
				placeholderValues.put("spieltag.i", toString(dayCount + 1, 1));

				game = planGame(title, map, gamePlayers, gameDayRules.get("" + day), placeholderValues);

				games.add(game);
				count++;
				dayCount++;
			}

			int gamesAfter = games.size();
			logger.info("map #" + day + " games planned:   " + (gamesAfter - gamesBefore));
		}
		return games;
	}

	/**
	 * Internal logic used by {@link Planner#planSeriesBalanced(String, List, java.util.Map, java.util.Map)} to shuffle the players to have them
	 * equally distributed.<br>
	 * The result is an array of Users with the following indexes used: <code>users[round][match][player]</code>.<br>
	 * 
	 * @param players - the list of {@link User}s
	 * @param rules - the list of {@link Rules} (by round/game day)
	 * @return the shuffled {@link User}s
	 */
	static User[][][] balancedShufflePlayers(List<User> players, List<Rules> rules)
	{
		List<User> tmp = new LinkedList<User>(players);
		Collections.shuffle(tmp);
		// shuffeln kann manchmal fehlschlagen
		// (weil sonst ein spieler doppelt in einem Rennen sein m�sste)
		// --> dann versuch es einfach nochmal...
		while(true)
		{
			try
			{
				ShuffleResult result = balancedShufflePlayers0(tmp, rules);
				if(logger.isDebugEnabled())
					printWhoOnWho(result, false);
				return result.shuffledUsers;
			}
			catch(IllegalArgumentException e)
			{
				continue;
			}
		}
	}

	/**
	 * Internal logic used by {@link Planner#balancedShufflePlayers(List, List)}.<br>
	 * Since shuffling can fail, the shuffle logic is separated into this method, so {@link Planner#balancedShufflePlayers(List, List)} can retry on
	 * failure
	 * more easily.
	 * 
	 * @param players - the list of {@link User}s
	 * @param rules - the list of {@link Rules} (by round/game day)
	 * @return the shuffled {@link User}s as a {@link ShuffleResult}
	 */
	static ShuffleResult balancedShufflePlayers0(List<User> players, List<Rules> rules)
	{
		Random rand = new Random();
		int[] playersGames;

		ShuffleResult result = new ShuffleResult(players.size(), rules.size());

		for(int r = 0; r < rules.size(); r++)
		{
			int playerGames = rules.get(r).getGamesPerPlayer() * players.size();
			int games = playerGames / rules.get(r).getNumberOfPlayers();
			if(playerGames % rules.get(r).getNumberOfPlayers() != 0)
				games++;

			result.shuffledUsers[r] = new User[games][rules.get(r).getNumberOfPlayers()];

			int g = 0;
			int p = 0;

			playersGames = new int[players.size()];

			while(playerGames > 0)
			{
				if(p == 0)
				{
					// suche Spieler mit geringster Spielezahl
					int minGames = rules.get(r).getGamesPerPlayer();
					List<Integer> potentials = new LinkedList<Integer>();
					for(int pl = 0; pl < playersGames.length; pl++)
					{
						if(playersGames[pl] == minGames)
							potentials.add(pl);
						else if(playersGames[pl] < minGames)
						{
							potentials.clear();
							potentials.add(pl);
							minGames = playersGames[pl];
						}
					}

					// wenn nur ein Spieler, dann nimm auch noch Spieler mit 1 Spiel mehr dazu
					int maxGames = minGames;
					while(potentials.size() == 1)
					{
						maxGames++;
						// nimm auch noch spieler mit 1 spiel mehr dazu
						for(int pl = 0; pl < playersGames.length; pl++)
						{
							if(playersGames[pl] == maxGames)
								potentials.add(pl);
						}
					}

					// suche unter diesen Spielern die seltenste Begegnung
					int minBattles = Integer.MAX_VALUE;
					List<Integer> potentials2a = new LinkedList<Integer>();
					List<Integer> potentials2b = new LinkedList<Integer>();
					for(int row = 0; row < result.totalWhoOnWho.length; row++)
					{
						if(!potentials.contains(row))
							continue;
						for(int col = row + 1; col < result.totalWhoOnWho.length; col++)
						{
							if(!potentials.contains(col))
								continue;

							if(result.totalWhoOnWho[row][col] == minBattles)
							{
								potentials2a.add(row);
								potentials2b.add(col);
							}
							else if(result.totalWhoOnWho[row][col] < minBattles)
							{
								potentials2a.clear();
								potentials2b.clear();
								potentials2a.add(row);
								potentials2b.add(col);
								minBattles = result.totalWhoOnWho[row][col];
							}
						}
					}

					int ri = rand.nextInt(potentials2a.size());

					// add player
					result.shuffledUsers[r][g][p++] = players.get(potentials2a.get(ri));
					result.shuffledUsers[r][g][p++] = players.get(potentials2b.get(ri));

					// update playersGames
					playersGames[potentials2a.get(ri)]++;
					playersGames[potentials2b.get(ri)]++;

					// update whoOnWho
					result.whoOnWho[r][potentials2a.get(ri)][potentials2b.get(ri)]++;
					result.whoOnWho[r][potentials2b.get(ri)][potentials2a.get(ri)]++;

					// update totalWhoOnWho
					result.totalWhoOnWho[potentials2a.get(ri)][potentials2b.get(ri)]++;
					result.totalWhoOnWho[potentials2b.get(ri)][potentials2a.get(ri)]++;

					// update playerGames-Counter
					playerGames--;
					playerGames--;
				}
				else
				{
					// spieler die bereits am Rennen teilnehmen
					// => zu durchsuchende Zeilen
					// => NICHT zu durchsuchende Spalten
					List<Integer> rows = new LinkedList<Integer>();
					List<Integer> cols = new LinkedList<Integer>();
					for(int pl = 0; pl < p; pl++)
					{
						rows.add(players.indexOf(result.shuffledUsers[r][g][pl]));
						cols.add(players.indexOf(result.shuffledUsers[r][g][pl]));
					}

					// suche nicht mehr in Frage kommende Spieler (schon max an Spielen)
					// => NICHT zu durchsuchende Spalten
					int maxGames = rules.get(r).getGamesPerPlayer();
					for(int pl = 0; pl < playersGames.length; pl++)
					{
						if(playersGames[pl] == maxGames)
							cols.add(pl);
					}

					// Suche minimale Spaltensumme (seltenster Gegner)
					// (Spaltensumme nur �ber die Zeilen, cols-Liste ausschlie�en)
					int minBattles = Integer.MAX_VALUE;
					List<Integer> potentials = new LinkedList<Integer>();
					for(int col = 0; col < result.totalWhoOnWho.length; col++)
					{
						if(cols.contains(col))
							continue;
						int battles = 0;
						for(int row = 0; row < result.totalWhoOnWho.length; row++)
						{
							if(!rows.contains(row))
								continue;
							battles += result.totalWhoOnWho[row][col];
						}

						if(battles == minBattles)
						{
							potentials.add(col);
						}
						else if(battles < minBattles)
						{
							potentials.clear();
							potentials.add(col);
							minBattles = battles;
						}
					}

					int ri = rand.nextInt(potentials.size());

					// add player
					result.shuffledUsers[r][g][p++] = players.get(potentials.get(ri));

					// update playersGames
					playersGames[potentials.get(ri)]++;

					// update whoOnWho
					for(int pl : rows)
					{
						result.whoOnWho[r][pl][potentials.get(ri)]++;
						result.whoOnWho[r][potentials.get(ri)][pl]++;

						result.totalWhoOnWho[pl][potentials.get(ri)]++;
						result.totalWhoOnWho[potentials.get(ri)][pl]++;
					}

					// update playerGames-Counter
					playerGames--;
				}

				if(p >= rules.get(r).getNumberOfPlayers())
				{
					p = 0;
					g++;
				}
			}

			// letztes Rennen auff�llen... (falls nicht voll)
			for(; g < games && p < rules.get(r).getNumberOfPlayers(); p++)
			{
				result.shuffledUsers[r][g][p] = null;
			}
		}

		return result;
	}

	/**
	 * For debugging: print the combinations (who-on-who) for a balanced gameseries
	 * 
	 * @param result - the {@link ShuffleResult} returned by {@link Planner#balancedShufflePlayers0(List, List)}
	 * @param printDetails - false = print only the total result, true = print results for all game days
	 */
	protected static void printWhoOnWho(ShuffleResult result, boolean printDetails)
	{
		if(!logger.isDebugEnabled())
			return;

		StringBuilder sb = new StringBuilder();
		// print totalWhoOnWho
		for(int pl1 = 0; pl1 < result.totalWhoOnWho.length; pl1++)
		{
			sb.append("\n");
			for(int pl2 = 0; pl2 < result.totalWhoOnWho[pl1].length; pl2++)
			{
				sb.append(toString(result.totalWhoOnWho[pl1][pl2], 2) + " ");
			}
		}

		// print whoOnWhos
		if(printDetails)
		{
			for(int m = 0; m < result.whoOnWho.length; m++)
			{
				for(int pl1 = 0; pl1 < result.whoOnWho[m].length; pl1++)
				{
					sb.append("\n");
					sb = new StringBuilder();
					for(int pl2 = 0; pl2 < result.whoOnWho[m][pl1].length; pl2++)
					{
						sb.append(toString(result.whoOnWho[m][pl1][pl2], 2) + " ");
					}
				}
				logger.debug(sb.toString());
			}
		}
	}

	/**
	 * Internal result returned by {@link Planner#balancedShufflePlayers0(List, List)}.<br>
	 * It contains not only the actual result of shuffled users, but also the counts on the "who-on-who" for validation and debugging.
	 * 
	 * @author ultimate
	 */
	static class ShuffleResult
	{
		/**
		 * The actual result of shuffed {@link Users}
		 */
		User[][][]	shuffledUsers;
		/**
		 * The "who-on-who" statistics for each game day
		 */
		int[][][]	whoOnWho;
		/**
		 * The total "who-on-who" statistics
		 */
		int[][]		totalWhoOnWho;

		/**
		 * Initiate the result and its arrays with the given sizes.
		 * 
		 * @param numberOfUsers - number of {@link User}s
		 * @param numberOfRounds - number of rounds
		 */
		private ShuffleResult(int numberOfUsers, int numberOfRounds)
		{
			this.shuffledUsers = new User[numberOfRounds][][];
			this.totalWhoOnWho = new int[numberOfUsers][numberOfUsers];
			this.whoOnWho = new int[numberOfRounds][numberOfUsers][numberOfUsers];
		}
	}

	/**
	 * Plan the games for a {@link EnumGameSeriesType#KLC} {@link GameSeries}.<br>
	 * Note: the original league lists in <code>playersByKey</code> will be shuffled.<br>
	 * 
	 * @param title - the title (including placeholders)
	 * @param playersByKey - the map of {@link User}s (by leagues)
	 * @param homeMaps - the home {@link Map}s for the {@link User}s
	 * @param leagues - the number of leagues
	 * @param groups - the number of groups
	 * @param rules - the {@link Rules} to use
	 * @param round - the round to plan
	 * @return the list of {@link PlannedGame}s
	 */
	public List<PlannedGame> planSeriesKLC(String title, java.util.Map<String, List<User>> playersByKey, java.util.Map<String, List<Map>> homeMaps, int leagues, int groups, Rules rules, int round)
	{
		int totalPlayers = groups * leagues;

		BiFunction<Team, Team, Team> whoIsHome = (team1, team2) -> {
			int league1 = -1;
			int league2 = -1;
			for(int l = 1; l <= leagues; l++)
			{
				if(playersByKey.get(GameSeries.KEY_LEAGUE + l).contains(team1.getMembers().get(0)))
					league1 = l;
				if(playersByKey.get(GameSeries.KEY_LEAGUE + l).contains(team2.getMembers().get(0)))
					league2 = l;
			}
			if(league1 == -1 || league2 == -1)
				logger.error("should not happen!");

			if(league1 > league2) // Spieler 0 ist in der niedrigeren Liga (= h�here Liga Nummer)
				return team1;
			else if(league1 < league2) // Spieler 1 ist in der niedrigeren Liga (= h�here Liga Nummer)
				return team2;
			return (random.nextBoolean() ? team1 : team2); // beide sind in der gleichen Liga -> zufall
		};

		if(round == totalPlayers)
			return planGroupphase(title, playersByKey, homeMaps, leagues, groups, whoIsHome, rules, round);
		else
		{
			// create tmp list of teams so we can use planTeamGame
			List<Team> teams = new ArrayList<>(playersByKey.get(GameSeries.KEY_ROUND + round).size());
			for(User user : playersByKey.get(GameSeries.KEY_ROUND + round))
				teams.add(new Team(user.getLogin(), Arrays.asList(user), homeMaps.get("" + user.getId()).get(0)));

			return planSeriesKO(title, teams, null, whoIsHome, rules, true, true);
		}
	}

	/**
	 * Internal logic used by {@link Planner#planSeriesKLC(String, java.util.Map, java.util.Map, int, int, Rules, int)} used to plan the group
	 * phase.<br>
	 * It will use the logic of {@link Planner#planMatchesLeague(List)} for each group.<br>
	 * Note: the original league lists in <code>playersByKey</code> will be shuffled.<br>
	 * 
	 * @param title - the title (including placeholders)
	 * @param playersByKey - the map of {@link User}s (by leagues)
	 * @param homeMaps - the home {@link Map}s for the {@link User}s
	 * @param leagues - the number of leagues
	 * @param groups - the number of groups
	 * @param whoIsHome - logic to determine who is the home team
	 * @param rules - the {@link Rules} to use
	 * @param round - the round to plan
	 * @return the list of {@link PlannedGame}s
	 */
	// original listen werden gemischt
	private List<PlannedGame> planGroupphase(String title, java.util.Map<String, List<User>> playersByKey, java.util.Map<String, List<Map>> homeMaps, int leagues, int groups,
			BiFunction<Team, Team, Team> whoIsHome, Rules rules, int round)
	{
		List<PlannedGame> games = new LinkedList<>();

		// Liegen durchmischen
		for(int l = 1; l <= leagues; l++)
			Collections.shuffle(playersByKey.get(GameSeries.KEY_LEAGUE + l));

		// Gruppenphase
		for(int g = 1; g <= groups; g++)
		{
			// Bilde Gruppe aus je 1 Spieler pro Liga
			// die Ligen sind 1mal initial durchgemischt, deshalb k�nnen wir einfach f�r Gruppe
			// X jeweils den X-ten Spieler pro Gruppe nehmen
			playersByKey.get(GameSeries.KEY_GROUP + g).clear();
			for(int l = 1; l <= leagues; l++)
				playersByKey.get(GameSeries.KEY_GROUP + g).add(playersByKey.get(GameSeries.KEY_LEAGUE + l).get(g - 1));
			Collections.shuffle(playersByKey.get(GameSeries.KEY_GROUP + g), random);

			// create a temporarily list of single player teams to be able to use the LeaguePlanner
			List<Team> teamsTmp = new ArrayList<Team>(playersByKey.get(GameSeries.KEY_GROUP + g).size());
			for(User p : playersByKey.get(GameSeries.KEY_GROUP + g))
				teamsTmp.add(new Team(p.getLogin(), Arrays.asList(p)));

			List<List<Match>> matches = planMatchesLeague(teamsTmp);

			int day = 0;
			PlannedGame game;
			int count = 0;
			int dayCount;
			HashMap<String, String> placeholderValues;
			for(List<Match> matchesForDay : matches)
			{
				dayCount = 0;
				for(Match match : matchesForDay)
				{
					placeholderValues = new HashMap<>();
					placeholderValues.put("i", toString(count + 1, 1));
					placeholderValues.put("spieltag", toString(day + 1, 1));
					placeholderValues.put("spieltag.i", toString(dayCount + 1, 1));
					placeholderValues.put("runde", toPlaceholderString(round, g, day, -1));
					placeholderValues.put("runde.x", toPlaceholderString(round, g, day, count));

					game = planTeamGame(title, match.getTeam(0), match.getTeam(1), whoIsHome, null, rules, placeholderValues);

					games.add(game);
					count++;
					dayCount++;
				}
				day++;
			}
		}

		return games;
	}

	/**
	 * Plan the games for a round in a {@link EnumGameSeriesType#KO} {@link GameSeries}.<br>
	 * 
	 * @param title - the title (including placeholders)
	 * @param teams - the teams in this round (pairs will be created ascending (2n) vs. (2n+1)
	 * @param maps - the list of maps to use (only used if !useHomeMaps; if size > 1 a random map will be used)
	 * @param whoIsHome - logic to determine who is the home team
	 * @param rules - the {@link Rules} to use
	 * @param useHomeMaps - use a home {@link Map} or a neutral map from the list
	 * @param shuffle - shuffle the teams before creating the matches (will randomize the KO pairs)
	 * @return the list of {@link PlannedGame}s
	 */
	public List<PlannedGame> planSeriesKO(String title, List<Team> teams, List<Map> maps, BiFunction<Team, Team, Team> whoIsHome, Rules rules, boolean useHomeMaps, boolean shuffle)
	{
		List<PlannedGame> games = new LinkedList<>();

		// create local copy of the input list
		List<Team> tmp = new ArrayList<>(teams);
		if(shuffle)
			Collections.shuffle(tmp, random);

		int count = 1;
		PlannedGame game;
		Team ti, ti1;
		Map overwriteMap = null;
		HashMap<String, String> placeholderValues;

		for(int i = 0; i < tmp.size(); i = i + 2)
		{
			ti = tmp.get(i);
			ti1 = tmp.get(i + 1);

			placeholderValues = new HashMap<>();
			placeholderValues.put("i", toString(count + 1, 1));
			// placeholderValues.put("spieltag", toPlaceholderString(day + 1, 1));
			// placeholderValues.put("spieltag.i", toPlaceholderString(dayCount + 1, 1));
			placeholderValues.put("runde", toPlaceholderString(tmp.size(), -1, -1, -1));
			placeholderValues.put("runde.x", toPlaceholderString(tmp.size(), -1, -1, count));

			if(!useHomeMaps)
				overwriteMap = maps.get(random.nextInt(maps.size()));

			game = planTeamGame(title, ti, ti1, whoIsHome, overwriteMap, rules, placeholderValues);

			games.add(game);
			count++;
		}

		return games;
	}

	/**
	 * Plan the games for a {@link EnumGameSeriesType#League} {@link GameSeries}
	 * 
	 * @param title - the title (including placeholders)
	 * @param teams - the list of {@link Team}s
	 * @param maps - the list of {@link Map}s if useHomeMaps is set to false or if there is an uneven number of games per pair)
	 * @param rules - the {@link Rules} to use
	 * @param useHomeMaps - use a home {@link Map} or a neutral map from the list
	 * @param numberOfGamesPerPair - the number of games per pair (usually this is 2 = one for the first and one for the second half of the season)
	 * @return the list of {@link PlannedGame}s
	 */
	public List<PlannedGame> planSeriesLeague(String title, List<Team> teams, List<Map> maps, Rules rules, boolean useHomeMaps, int numberOfGamesPerPair)
	{
		List<PlannedGame> games = new LinkedList<>();

		// create local copy of the input list
		List<Team> tmp = new ArrayList<>(teams);
		Collections.shuffle(tmp, random);

		List<List<Match>> matches = planMatchesLeague(tmp);

		int day = 0;
		PlannedGame game;
		Map overwriteMap;
		int count = 0;
		int dayCount;
		HashMap<String, String> placeholderValues;

		for(int round = 1; round <= numberOfGamesPerPair; round++)
		{
			for(List<Match> matchesForDay : matches)
			{
				dayCount = 0;
				for(Match match : matchesForDay)
				{
					final int r = round;
					BiFunction<Team, Team, Team> whoIsHome = (team1, team2) -> { return (r % 2 == 0 ? team1 : team2); };

					// use a neutral map if the number of rounds is uneven
					if(useHomeMaps && !((round % 2 == 1) && (round == numberOfGamesPerPair)) && maps != null)
						overwriteMap = maps.get(random.nextInt(maps.size()));
					else
						overwriteMap = null;

					placeholderValues = new HashMap<>();
					placeholderValues.put("i", toString(count + 1, 1));
					placeholderValues.put("spieltag", toString(day + 1, 1));
					placeholderValues.put("spieltag.i", toString(dayCount + 1, 1));

					game = planTeamGame(title, match.getTeam(0), match.getTeam(1), whoIsHome, overwriteMap, rules, placeholderValues);

					games.add(game);
					count++;
					dayCount++;
				}
				day++;
			}
		}

		return games;
	}

	/**
	 * Plan the games for a {@link EnumGameSeriesType#Simple} {@link GameSeries}
	 * 
	 * @param title - the title (including placeholders)
	 * @param players - the list of {@link User}s
	 * @param maps - the list of {@link Map}s to chose from
	 * @param rules - the {@link Rules} to use
	 * @param numberOfGames - the number of games to create
	 * @param maxPlayersPerGame - the max number of {@link User}s per {@link PlannedGame}
	 * @return the list of {@link PlannedGame}s
	 */
	public List<PlannedGame> planSeriesSimple(String title, List<User> players, List<Map> maps, Rules rules, int numberOfGames, int maxPlayersPerGame)
	{
		List<PlannedGame> games = new LinkedList<>();

		PlannedGame game;
		List<User> gamePlayers;
		List<User> allPlayers;
		User player;
		Map map;
		int count = 0;
		HashMap<String, String> placeholderValues;

		for(int i = 0; i < numberOfGames; i++)
		{
			map = maps.get(random.nextInt(maps.size()));

			gamePlayers = new LinkedList<User>();
			gamePlayers.add(karoAPICache.getCurrentUser());

			allPlayers = new LinkedList<User>(players);

			while(gamePlayers.size() < Math.min(maxPlayersPerGame, map.getPlayers()))
			{
				if(allPlayers.size() == 0)
					break;
				player = allPlayers.remove(random.nextInt(allPlayers.size()));
				if(player.isInvitable(map.isNight()))
					gamePlayers.add(player);
			}

			placeholderValues = new HashMap<>();
			placeholderValues.put("i", toString(count + 1, 1));

			game = planGame(title, map, gamePlayers, rules, placeholderValues);

			games.add(game);
			count++;
		}

		return games;
	}

	/**
	 * Plan a team game.<br>
	 * Note: the placeholders in title will filled using the placeholderValues and {@link Planner#applyPlaceholders(String, java.util.Map)}
	 * 
	 * @param title - the title (including placeholders)
	 * @param team1 - the first team
	 * @param team2 - the second team
	 * @param whoIsHome - logic to determine who is the home team, if null, team1 is used
	 * @param overwriteMap - overwrite the map with a neutral one? If null, the home-map is used
	 * @param rules - the {@link Rules} to use
	 * @param placeholderValues - the values for the title placeholders
	 * @return the {@link PlannedGame}
	 */
	public PlannedGame planTeamGame(String title, Team team1, Team team2, BiFunction<Team, Team, Team> whoIsHome, Map overwriteMap, Rules rules, java.util.Map<String, String> placeholderValues)
	{
		Team home, guest;

		if(whoIsHome != null)
		{
			home = whoIsHome.apply(team1, team2);
			guest = (home == team1 ? team2 : team1);
		}
		else
		{
			home = team1;
			guest = team2;
		}

		List<User> gamePlayers = new LinkedList<User>();
		gamePlayers.add(karoAPICache.getCurrentUser());
		for(User player : home.getMembers())
		{
			if(!gamePlayers.contains(player))
				gamePlayers.add(player);
		}
		for(User player : guest.getMembers())
		{
			if(!gamePlayers.contains(player))
				gamePlayers.add(player);
		}

		placeholderValues.put("teams", toPlaceholderString(home, guest));

		return planGame(title, (overwriteMap != null ? overwriteMap : home.getHomeMap()), gamePlayers, rules, placeholderValues);
	}

	/**
	 * Plan a game.<br>
	 * Note: the placeholders in title will filled using the placeholderValues and {@link Planner#applyPlaceholders(String, java.util.Map)}
	 * 
	 * @param title - the title (including placeholders)
	 * @param map - the map to use
	 * @param gamePlayers - the participating {@link User}s
	 * @param rules - the {@link Rules} to use
	 * @param placeholderValues - the values for the title placeholders
	 * @return the {@link PlannedGame}
	 */
	public PlannedGame planGame(String title, Map map, List<User> gamePlayers, Rules rules, java.util.Map<String, String> placeholderValues)
	{
		return planGame(title, map, gamePlayers, rules.createOptions(random), placeholderValues);
	}

	/**
	 * Plan a game.<br>
	 * Note: the placeholders in title will filled using the placeholderValues and {@link Planner#applyPlaceholders(String, java.util.Map)}
	 * 
	 * @param title - the title (including placeholders)
	 * @param map - the map to use
	 * @param gamePlayers - the participating {@link User}s
	 * @param options - the {@link Options} to use
	 * @param placeholderValues - the values for the title placeholders
	 * @return the {@link PlannedGame}
	 */
	public PlannedGame planGame(String title, Map map, List<User> gamePlayers, Options options, java.util.Map<String, String> placeholderValues)
	{
		increasePlannedGames(gamePlayers);

		// add default placeholder values
		java.util.Map<String, String> defaultPlaceholderValues = getDefaultPlaceholderValues(map, gamePlayers, options);
		for(Entry<String, String> pv : defaultPlaceholderValues.entrySet())
			placeholderValues.putIfAbsent(pv.getKey(), pv.getValue());

		String name = applyPlaceholders(title, placeholderValues);

		return new PlannedGame(name, map, gamePlayers, options, placeholderValues);
	}

	/**
	 * Logic used by {@link Planner#planSeriesAllCombinations(String, List, List, Rules, int, int)}
	 * 
	 * @param teams - the list of {@link Team}s
	 * @param numberOfTeamsPerMatch - the number of teams per match
	 * @return the list of {@link Match}es
	 */
	public List<Match> planMatchesAllCombinations(List<Team> teams, int numberOfTeamsPerMatch)
	{
		if(numberOfTeamsPerMatch > teams.size())
			throw new IllegalArgumentException("numberOfTeamsPerMatch must be <= teams.size");

		List<Match> matches = new ArrayList<Match>();

		int[] selectors = new int[numberOfTeamsPerMatch];
		for(int i = 0; i < numberOfTeamsPerMatch; i++)
			selectors[i] = i;

		Team[] matchTeams;
		do
		{
			matchTeams = new Team[numberOfTeamsPerMatch];
			for(int i = 0; i < numberOfTeamsPerMatch; i++)
				matchTeams[i] = teams.get(selectors[i]);
			matches.add(new Match(matchTeams));
		} while(incrementSelectors(selectors, teams.size()));

		return matches;
	}

	/**
	 * Internal helper for {@link Planner#planMatchesAllCombinations(List, int)}
	 * 
	 * @param s
	 * @param max
	 * @return success or not
	 */
	private static boolean incrementSelectors(int[] s, int max)
	{
		int pointer = s.length - 1;

		boolean success = false;
		while(pointer >= 0)
		{
			if(s[pointer] < max - (s.length - pointer))
			{
				s[pointer]++;
				for(int p = pointer + 1; p < s.length; p++)
				{
					s[p] = s[p - 1] + 1;
					if(s[p] > max - (s.length - p))
						return false;
				}
				success = true;
				break;
			}
			else
			{
				pointer--;
			}
		}
		return success;
	}

	/**
	 * Logic used by {@link Planner#planSeriesLeague(String, List, List, Rules, boolean, int)} and
	 * {@link Planner#planGroupphase(String, java.util.Map, java.util.Map, int, int, BiFunction, Rules, int)}
	 * 
	 * @param teams - the list of {@link Team}s
	 * @return the list of {@link Match}es per game day
	 */
	public List<List<Match>> planMatchesLeague(List<Team> teams)
	{
		if(teams.size() % 2 == 1)
			throw new IllegalArgumentException("equal number of teams required");
		// oder Team "frei" hinzuf�gen

		int teamcount = teams.size(); // Anzahl der Teams
		int matchcount = teamcount / 2; // Anzahl der m�glichen Spielpaare
		int days = teamcount - 1; // Anzahl der Spieltage pro Runde
		List<List<Match>> matches = new LinkedList<List<Match>>(); // Spielplan
		int gamenr = 0; // Z�hler f�r Spielnummer

		Team team1, team2;
		List<Match> dayList;
		for(int day = 0; day < days; day++)
		{
			dayList = new LinkedList<Match>();
			teams.add(1, teams.remove(teamcount - 1)); // letztes Team an stelle 1
			for(int matchnr = 0; matchnr < matchcount; matchnr++)
			{
				gamenr++;
				if((gamenr % teamcount != 1) && (gamenr % 2 == 0))
				{
					team1 = teams.get(matchnr);
					team2 = teams.get(teamcount - 1 - matchnr);
				}
				else
				{
					team2 = teams.get(matchnr);
					team1 = teams.get(teamcount - 1 - matchnr);
				}
				dayList.add(new Match(team1, team2));
			}
			matches.add(dayList);
		}

		int maxHomeMatches;
		List<Team> maxHomeTeams;
		int minHomeMatches;
		List<Team> minHomeTeams;
		int homeMatches;
		boolean swapped;
		Team t1, t2;
		Random r = new Random();
		int minSwappable;
		Team minSwappableTeam;
		List<Team> minSwappableList;
		int swappable;
		while(true)
		{
			maxHomeMatches = 0;
			maxHomeTeams = new LinkedList<Team>();
			minHomeMatches = teamcount - 1;
			minHomeTeams = new LinkedList<Team>();
			swapped = false;

			for(Team t : teams)
			{
				homeMatches = countHomeMatches(matches, t);
				if(homeMatches > maxHomeMatches)
				{
					maxHomeMatches = homeMatches;
					maxHomeTeams = new LinkedList<Team>();
					maxHomeTeams.add(t);
				}
				else if(homeMatches == maxHomeMatches)
				{
					maxHomeTeams.add(t);
				}
				if(homeMatches < minHomeMatches)
				{
					minHomeMatches = homeMatches;
					minHomeTeams = new LinkedList<Team>();
					minHomeTeams.add(t);
				}
				else if(homeMatches == minHomeMatches)
				{
					minHomeTeams.add(t);
				}
			}

			if(maxHomeMatches - minHomeMatches < 2)
				break;

			while(maxHomeTeams.size() > 0 && minHomeTeams.size() > 0)
			{
				minSwappable = Math.min(minHomeTeams.size(), maxHomeTeams.size()) + 1;
				minSwappableTeam = null;
				minSwappableList = null;
				for(Team t : maxHomeTeams)
				{
					swappable = 0;
					for(List<Match> ms : matches)
					{
						for(Match m : ms)
						{
							if(m.getTeam(0).equals(t) && minHomeTeams.contains(m.getTeam(1)))
								swappable++;
						}
					}
					if(swappable < minSwappable)
					{
						minSwappable = swappable;
						minSwappableTeam = t;
						minSwappableList = maxHomeTeams;
					}
				}
				for(Team t : minHomeTeams)
				{
					swappable = 0;
					for(List<Match> ms : matches)
					{
						for(Match m : ms)
						{
							if(m.getTeam(1).equals(t) && maxHomeTeams.contains(m.getTeam(0)))
								swappable++;
						}
					}
					if(swappable < minSwappable)
					{
						minSwappable = swappable;
						minSwappableTeam = t;
						minSwappableList = minHomeTeams;
					}
				}
				if(minSwappable == 0)
				{
					minSwappableList.remove(minSwappableTeam);
					continue;
				}
				if(maxHomeTeams.contains(minSwappableTeam))
				{
					t1 = minSwappableTeam;
					t2 = minHomeTeams.get(r.nextInt(minHomeTeams.size()));
				}
				else
				{
					t1 = maxHomeTeams.get(r.nextInt(maxHomeTeams.size()));
					t2 = minSwappableTeam;
				}
				for(List<Match> ms : matches)
				{
					for(Match m : ms)
					{
						if(m.getTeam(0).equals(t1) && m.getTeam(1).equals(t2))
						{
							m.rotateTeams();
							swapped = true;
							maxHomeTeams.remove(t1);
							minHomeTeams.remove(t2);
						}
					}
				}
				if(!swapped && maxHomeTeams.size() == 1 && minHomeTeams.size() == 1)
					break;
			}
		}

		return matches;
	}

	/**
	 * Internal helper for debugging and validating a league
	 * 
	 * @param matches - the list of {@link Match}es
	 * @param t - the {@link Team} to look for
	 * @return the number of home matches for this {@link Team}
	 */
	static int countHomeMatches(List<List<Match>> matches, Team t)
	{
		int home = 0;
		for(List<Match> ms : matches)
		{
			for(Match m : ms)
			{
				if(m.getTeam(0).equals(t))
					home++;
			}
		}
		return home;
	}

	// HELPERS

	/**
	 * Reset the planned games of all {@link User}s in the list
	 * 
	 * @see User#getPlannedGames()
	 * @see User#setPlannedGames(int)
	 * @param users - the list of users
	 */
	public static void resetPlannedGames(List<User> users)
	{
		for(User user : users)
			user.setPlannedGames(0);
	}

	/**
	 * Increase the planned games by 1 for all {@link User}s in the list
	 * 
	 * @see User#getPlannedGames()
	 * @see User#setPlannedGames(int)
	 * @param users - the list of users
	 */
	public static void increasePlannedGames(List<User> users)
	{
		for(User user : users)
			user.setPlannedGames(user.getPlannedGames() + 1);
	}

	/**
	 * Calculate the expected number of {@link Match}es for a {@link EnumGameSeriesType#AllCombinations} {@link GameSeries}.<br>
	 * The result is calculated as the binomial coefficient <code>(n k) = n! / (k! * (n-k)!)</code> which resolves to
	 * <code>n/1 * (n-1)/2 * (n-2)/3 * ...</code> where n = teams and k = teamsPerMatch.
	 * 
	 * @param teams - the total number of {@link Team}s
	 * @param teamsPerMatch - the number of {@link Team}s per {@link Match}
	 * @return the expected number of {@link Match}es
	 */
	public static int calculateNumberOfMatches(int teams, int teamsPerMatch)
	{
		BigInteger ret = BigInteger.ONE;
		for(int k = 0; k < teamsPerMatch; k++)
		{
			ret = ret.multiply(BigInteger.valueOf(teams - k)).divide(BigInteger.valueOf(k + 1));
		}
		return ret.intValue();
	}

	/**
	 * Get a {@link java.util.Map} with the default placeholder values for a {@link PlannedGame} based on the {@link Map}, the {@link User}s and the
	 * {@link Options}.<br>
	 * Additional placeholders can be added depending on the {@link GameSeries}.
	 * 
	 * @param map - the {@link Map}
	 * @param gamePlayers - the list of {@link User}s
	 * @param options - the {@link Options}
	 * @return the placeholder {@link java.util.Map}
	 */
	public java.util.Map<String, String> getDefaultPlaceholderValues(Map map, List<User> gamePlayers, Options options)
	{
		HashMap<String, String> defaultPlaceholderValues = new HashMap<>();

		// karte
		defaultPlaceholderValues.put("karte.id", toString(map.getId(), 1));
		defaultPlaceholderValues.put("karte.name", map.getName());
		defaultPlaceholderValues.put("karte.author", map.getAuthor());

		// spieler
		defaultPlaceholderValues.put("spieler.ersteller", karoAPICache.getCurrentUser().getLogin());
		defaultPlaceholderValues.put("spieler.anzahl", toString(gamePlayers.size(), 1));
		defaultPlaceholderValues.put("spieler.anzahl.x", toString(gamePlayers.size() - 1, 1));
		defaultPlaceholderValues.put("spieler.namen", toPlaceholderString(gamePlayers));
		List<User> playersWithoutCreator = new LinkedList<User>(gamePlayers);
		playersWithoutCreator.remove(karoAPICache.getCurrentUser());
		defaultPlaceholderValues.put("spieler.namen.x", toPlaceholderString(playersWithoutCreator));

		// teams --> set individually
		// runde --> set individually

		// regeln
		defaultPlaceholderValues.put("regeln", toPlaceholderString(options, false));
		defaultPlaceholderValues.put("regeln.x", toPlaceholderString(options, true));
		defaultPlaceholderValues.put("regeln.zzz", Language.getString("titlepatterns.zzz") + options.getZzz());
		defaultPlaceholderValues.put("regeln.tc", Language.getString("titlepatterns.tc") + Language.getString(EnumGameTC.class, options.getCrashallowed()));
		defaultPlaceholderValues.put("regeln.cps", Language.getString("titlepatterns.cps." + options.isCps()));
		defaultPlaceholderValues.put("regeln.richtung", Language.getString("titlepatterns.direction") + Language.getString(EnumGameDirection.class, options.getStartdirection()));

		return defaultPlaceholderValues;
	}

	/**
	 * Apply the given placeholders on the given game title
	 * 
	 * @param title - the title containing the placeholders
	 * @param placeholderValues - the values for the placeholders by key
	 * @return the updated (filled) string
	 */
	public static String applyPlaceholders(String title, java.util.Map<String, String> placeholderValues)
	{
		if(placeholderValues == null)
			return title;

		// preparation
		if(placeholderValues.containsKey("i"))
		{
			placeholderValues.putIfAbsent("ii", "0" + placeholderValues.get("i"));
			placeholderValues.putIfAbsent("iii", "00" + placeholderValues.get("i"));
			placeholderValues.putIfAbsent("iiii", "000" + placeholderValues.get("i"));
		}

		String name = title;
		for(Entry<String, String> pv : placeholderValues.entrySet())
		{
			logger.trace("replacing ${" + pv.getKey() + "} -> " + pv.getValue());
			name = name.replace("${" + pv.getKey() + "}", pv.getValue());
		}

		name = name.replace("  ", " ");
		name = name.trim();

		if(name.endsWith(","))
			name = name.substring(0, name.length() - 1);

		return name;
	}

	/**
	 * Fill a number with leading zeros ('0')
	 * 
	 * @param x - the number
	 * @param minDigits - the minimum number of digits to achieve
	 * @return the number as a string with leading zeros
	 */
	private static String toString(int x, int minDigits)
	{
		String s = "" + x;
		while(s.length() < minDigits)
			s = "0" + s;
		return s;
	}

	/**
	 * Create a string containing the names of all {@link User}s in the list (separated by ',' and 'and')
	 * 
	 * @param players - the list of {@link User}s
	 * @return the string containing the names
	 */
	private static String toPlaceholderString(List<User> players)
	{
		StringBuilder tmp = new StringBuilder();
		for(int p = 0; p < players.size(); p++)
		{
			if((p != 0) && (p != players.size() - 1))
				tmp.append(", ");
			else if((p != 0) && (p == players.size() - 1))
				tmp.append(" " + Language.getString("titlepatterns.and") + " ");
			tmp.append(players.get(p).getLogin());
		}
		return tmp.toString();
	}

	/**
	 * Create a string containing the names of all {@link Team}s in the list (separated by 'vs.')
	 * 
	 * @param teams - the list of {@link Team}s
	 * @return the string containing the names
	 */
	private static String toPlaceholderString(Team... teams)
	{
		StringBuilder tmp = new StringBuilder();
		for(int i = 0; i < teams.length; i++)
		{
			if(i > 0)
				tmp.append(" vs. ");
			tmp.append(teams[i].getName());
		}
		return tmp.toString();
	}

	/**
	 * Create a string representing the current round/game day of a game series.
	 * 
	 * @param round - the round
	 * @param group - the group (or -1 if not applicable)
	 * @param day - the day
	 * @param count - the game count (or -1 if not applicable)
	 * @return the placeholder value string
	 */
	private static String toPlaceholderString(int round, int group, int day, int count)
	{
		StringBuilder tmp = new StringBuilder();
		if(round == 2)
			tmp.append(Language.getString("titlepatterns.final"));
		else if(round == 4)
			tmp.append(Language.getString("titlepatterns.semifinal"));
		else if(round == 8)
			tmp.append(Language.getString("titlepatterns.quarterfinal"));
		else if(group <= 0)
			tmp.append(Language.getString("titlepatterns.roundOf").replace("${i/2}", "" + (round / 2)).replace("${i}", "" + (round)));
		else
			tmp.append(Language.getString("titlepatterns.groupStage").replace("${i}", "" + (group)));

		if(count >= 0)
		{
			tmp.append(",");
			if(round == 2 || round == 4 || round == 8 || group <= 0)
				tmp.append(Language.getString("titlepatterns.match") + " " + count);
			else
				tmp.append(Language.getString("titlepatterns.day") + " " + (day + 1));
		}
		return tmp.toString();
	}

	/**
	 * Create a string for the {@link Options}.<br>
	 * 
	 * @param options - the {@link Options}
	 * @param nonStandardOnly - true = only print those options not equal to the standard/default; false = print all options
	 * @return the placeholder value string
	 */
	private static String toPlaceholderString(Options options, boolean nonStandardOnly)
	{
		StringBuilder tmp = new StringBuilder();
		if(!nonStandardOnly || options.getZzz() != 2)
		{
			tmp.append(Language.getString("titlepatterns.zzz"));
			tmp.append(options.getZzz());
		}
		if(!nonStandardOnly || options.getCrashallowed() != EnumGameTC.forbidden)
		{
			if(!tmp.toString().isEmpty())
				tmp.append(", ");
			tmp.append(Language.getString("titlepatterns.tc"));
			tmp.append(Language.getString(EnumGameTC.class, options.getCrashallowed()));
		}
		if(!nonStandardOnly || !options.isCps())
		{
			if(!tmp.toString().isEmpty())
				tmp.append(", ");
			tmp.append(Language.getString("titlepatterns.cps." + options.isCps()));
		}
		if(!nonStandardOnly || options.getStartdirection() != EnumGameDirection.classic)
		{
			if(!tmp.toString().isEmpty())
				tmp.append(", ");
			tmp.append(Language.getString("titlepatterns.direction"));
			tmp.append(Language.getString(EnumGameDirection.class, options.getStartdirection()));
		}
		return tmp.toString();
	}

	/**
	 * Check that both maps contain exactly the same keys. (as a consistency check)
	 * 
	 * @param <K> - the key type
	 * @param map1 - the first {@link java.util.Map}
	 * @param map2 - the second {@link java.util.Map}
	 * @return true or false
	 */
	private static <K> boolean checkKeys(java.util.Map<K, ?> map1, java.util.Map<K, ?> map2)
	{
		if(map1 == null || map2 == null)
			return false;
		if(map1.size() != map2.size())
			return false;
		for(K key : map1.keySet())
			if(!map2.containsKey(key))
				return false;
		return true;
	}
}
