package ultimate.karomuskel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import ultimate.karoapi4j.enums.EnumCreatorParticipation;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.Match;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karoapi4j.model.extended.Team;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.Options;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoapi4j.model.official.User;
import ultimate.karomuskel.Planner.ShuffleResult;
import ultimate.karomuskel.test.KaroMUSKELTestcase;
import ultimate.karomuskel.ui.Language;

public class PlannerTest extends KaroMUSKELTestcase
{
	private Random random;
	
	@BeforeAll
	public void setUpOnce() throws IOException
	{
		super.setUpOnce();

		// needed for the placeholder stuff
		Language.load("de");
		
		// other  
		random = new Random();
	}

	/**
	 * This covers {@link EnumGameSeriesType#League} and {@link EnumGameSeriesType#KLC} (group phase)
	 */
	@ParameterizedTest
	@ValueSource(ints = { 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 })
	public void test_planMatchesLeague(int teamCount)
	{

		// create teams
		List<Team> teams = new LinkedList<Team>();
		for(int i = 1; i <= teamCount; i++)
			teams.add(new Team("T" + i, dummyCache.getUser(10 + i)));

		// test planning
		List<List<Match>> matches;

		// without dummy matches

		logger.debug("league-test for " + teamCount + " teams WITHOUT dummy matches");

		matches = Planner.planMatchesLeague(teams, false);
		checkLeagueMatches(teamCount, teams, matches, false);

		// with dummy matches

		logger.debug("league-test for " + teamCount + " teams WITH dummy matches");

		matches = Planner.planMatchesLeague(teams, true);
		checkLeagueMatches(teamCount, teams, matches, true);
	}

	private void checkLeagueMatches(int teamCount, List<Team> teams, List<List<Match>> matches, boolean dummyMatches)
	{
		assertEquals(teamCount, teams.size());

		int teamCountForCalc = (teamCount % 2 == 0 ? teamCount : teamCount + 1);

		int expectedNumberOfDays = teamCountForCalc - 1;
		int expectedNumberOfMatches = expectedNumberOfDays * teamCountForCalc / 2;
		if(teamCount % 2 == 1 && !dummyMatches)
			expectedNumberOfMatches -= teamCount;

		assertEquals(expectedNumberOfDays, matches.size());

		int[][] check;
		int home, day, matchCount, totalHome, expectedHomePerPlayerMin, expectedHomePerPlayerMax;
		int i1, i2;
		Team dummy = null;

		StringBuilder sb;

		check = new int[teamCountForCalc][teamCountForCalc];
		day = 1;
		matchCount = 0;
		for(List<Match> dayList : matches)
		{
			sb = new StringBuilder();
			sb.append("day " + day++ + "\t");
			for(Match match : dayList)
			{
				matchCount++;
				sb.append(match.getTeam(0).getName() + "-" + match.getTeam(1).getName() + "\t");
				if(dummyMatches && match.getTeam(0).getName().equalsIgnoreCase(GameSeriesManager.getStringConfig(EnumGameSeriesType.League, GameSeries.CONF_LEAGUE_DUMMY_TEAM)))
				{
					dummy = match.getTeam(0);
					i1 = teamCountForCalc;
				}
				else
					i1 = Integer.parseInt(match.getTeam(0).getName().substring(1));
				if(dummyMatches && match.getTeam(1).getName().equalsIgnoreCase(GameSeriesManager.getStringConfig(EnumGameSeriesType.League, GameSeries.CONF_LEAGUE_DUMMY_TEAM)))
				{
					dummy = match.getTeam(1);
					i2 = teamCountForCalc;
				}
				else
					i2 = Integer.parseInt(match.getTeam(1).getName().substring(1));
				check[i1 - 1][i2 - 1]++;
				check[i2 - 1][i1 - 1]++;
			}
			logger.debug(sb.toString());
		}

		assertEquals(expectedNumberOfMatches, matchCount);

		for(i1 = 1; i1 <= teamCountForCalc; i1++)
		{
			for(i2 = 1; i2 <= teamCountForCalc; i2++)
			{
				if(i1 == i2 || (teamCount % 2 == 1 && !dummyMatches && (i1 == teamCountForCalc || i2 == teamCountForCalc)))
					assertEquals(0, check[i1 - 1][i2 - 1], "invalid match count for T" + i1 + " vs. T" + i2 + ": " + check[i1 - 1][i2 - 1]);
				else
					assertEquals(1, check[i1 - 1][i2 - 1], "invalid match count for T" + i1 + " vs. T" + i2 + ": " + check[i1 - 1][i2 - 1]);
			}
		}
		totalHome = 0;
		expectedHomePerPlayerMin = (teamCount - 1) / 2;
		if(teamCount % 2 == 0)
			expectedHomePerPlayerMax = expectedHomePerPlayerMin + 1;
		else
			expectedHomePerPlayerMax = expectedHomePerPlayerMin;
		logger.debug("expecting home count = " + expectedHomePerPlayerMin + " - " + expectedHomePerPlayerMax);
		for(Team t : teams)
		{
			home = Planner.countHomeMatches(matches, t);
			totalHome += home;
			if(teamCount % 2 == 1 && dummyMatches)
				home--;
			logger.debug(t.getName() + ": home=" + home + ", guest=" + (teamCount - 1 - home));
			assertTrue(home >= expectedHomePerPlayerMin && home <= expectedHomePerPlayerMax, "invalid home match count for " + t.getName() + ": " + home);
		}
		if(teamCount % 2 == 1 && dummyMatches)
		{
			Team t = dummy;
			home = Planner.countHomeMatches(matches, t);
			totalHome += home;
			logger.debug(t.getName() + ": home=" + home + ", guest=" + (teamCount - 1 - home));
			assertEquals(0, home, "invalid home match count for " + t.getName() + ": " + home);
		}
		assertEquals(expectedNumberOfMatches, totalHome);
	}

	/**
	 * This covers {@link EnumGameSeriesType#League} and {@link EnumGameSeriesType#KLC} (group phase)
	 */
	@ParameterizedTest
	@ValueSource(ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })
	public void test_planMatchesAllCombinations(int teamCount)
	{
		logger.debug("testing teamCount = " + teamCount);
		// create teams
		List<Team> teams = new LinkedList<Team>();
		for(int i = 1; i <= teamCount; i++)
			teams.add(new Team("T" + i, dummyCache.getUser(10 + i)));

		// test planning
		List<Match> matches;
		int expectedNumberOfMatches;
		for(int numberOfTeamsPerMatch = 1; numberOfTeamsPerMatch <= teamCount; numberOfTeamsPerMatch++)
		{
			expectedNumberOfMatches = Planner.calculateNumberOfMatches(teamCount, numberOfTeamsPerMatch);
			logger.debug("testing teamCount = " + teamCount + ", numberOfTeamsPerMatch = " + numberOfTeamsPerMatch + ", expectedNumberOfMatches = " + expectedNumberOfMatches);
			matches = Planner.planMatchesAllCombinations(teams, numberOfTeamsPerMatch);
			assertNotNull(matches);
			assertEquals(expectedNumberOfMatches, matches.size());

			for(int m1 = 0; m1 < matches.size(); m1++)
			{
				for(int m2 = m1 + 1; m2 < matches.size(); m2++)
				{
					assertFalse(compareTeams(matches.get(m1).getTeams(), matches.get(m2).getTeams()));
				}
			}
		}
	}

	private boolean compareTeams(Team[] teams1, Team[] teams2)
	{
		if(teams1.length != teams2.length)
			return false;

		int identicalTeamsFound;
		for(Team t1 : teams1)
		{
			identicalTeamsFound = 0;
			for(Team t2 : teams2)
			{
				if(t1 == t2)
					identicalTeamsFound++;
			}
			if(identicalTeamsFound != 1)
				return false;
		}
		return true;
	}

	/**
	 * This covers {@link EnumGameSeriesType#Balanced}
	 */
	@Test
	public void test_balancedShufflePlayers()
	{
		int totalPlayers = 30;
		int numberOfMaps = 20;
		int gamesPerPlayer = 6;

		List<User> players = new LinkedList<User>();
		for(int i = 0; i < totalPlayers; i++)
			players.add(dummyCache.getUser(i));

		List<Rules> rules = new LinkedList<Rules>();
		for(int i = 0; i < numberOfMaps; i++)
		{
			// int numberOfPlayers = (i < 6 ? 5 : (i < 12 ? 7 : 10));
			int numberOfPlayers = (i < 6 ? 3 : (i < 12 ? 4 : 5));
			rules.add(new Rules(0, 0, EnumGameTC.free, true, EnumGameDirection.free, gamesPerPlayer, numberOfPlayers));
		}

		ShuffleResult result;

		int tries = 0;
		while(true)
		{
			try
			{
				// shuffeling can fail (see Planner#shufflePlayers)
				tries++;
				result = Planner.balancedShufflePlayers0(players, rules);
				break;
			}
			catch(IllegalArgumentException e)
			{
				continue;
			}
		}
		logger.debug("tries needed = " + tries);
		// assertTrue(tries <= 3); // tries should not exceed 3

		StringBuilder sb = new StringBuilder();

		// print games
		for(int m = 0; m < numberOfMaps; m++)
		{
			logger.debug("map=" + m);
			for(int g = 0; g < result.shuffledUsers[m].length; g++)
			{
				sb = new StringBuilder();
				sb.append((g > 9 ? "" : " ") + g + ": ");
				for(int p = 0; p < rules.get(m).getNumberOfPlayers(); p++)
				{
					if(p > 0)
						sb.append(", ");
					if(result.shuffledUsers[m][g][p] != null)
						sb.append(result.shuffledUsers[m][g][p].getLogin());
				}
				logger.debug(sb.toString());
			}
		}

		// print playersGames
		logger.debug("playersGames");
		for(User player : players)
		{
			sb = new StringBuilder();
			sb.append(player.getLogin() + ": ");
			for(int m = 0; m < numberOfMaps; m++)
			{
				int count = 0;
				for(int g = 0; g < result.shuffledUsers[m].length; g++)
				{
					for(int p = 0; p < rules.get(m).getNumberOfPlayers(); p++)
					{
						if(result.shuffledUsers[m][g][p] == player)
							count++;
					}
				}
				sb.append(count + " ");
				assertEquals(gamesPerPlayer, count, "invalid match count for player " + player.getLogin() + " on map #" + m);
			}
			logger.debug(sb.toString());
		}

	}

	/**
	 * This covers {@link EnumGameSeriesType#KO} and {@link EnumGameSeriesType#KLC} (KO-Phase)
	 */
	@ParameterizedTest
	@ValueSource(ints = { 2, 4, 8, 16, 32 })
	public void test_planSeriesKO(int teamCount)
	{
		// create teams
		List<Team> winners = new LinkedList<Team>();
		for(int i = 1; i <= teamCount; i++)
			winners.add(new Team("W" + i, dummyCache.getUser(10 + i)));
		List<Team> losers = new LinkedList<Team>();
		for(int i = 1; i <= teamCount; i++)
			losers.add(new Team("L" + i, dummyCache.getUser(20 + i)));

		Rules rules = new Rules();

		for(EnumCreatorParticipation creatorParticipation : EnumCreatorParticipation.values())
		{

			for(int numberOfGamesPerPair = 1; numberOfGamesPerPair <= 5; numberOfGamesPerPair++)
			{
				List<PlannedGame> games;
				int pair;
				int expectedNumberOfMatches;

				// winners only
				expectedNumberOfMatches = teamCount / 2 * numberOfGamesPerPair;
				logger.debug("testing teamCount = " + teamCount + ", numberOfGamesPerPair = " + numberOfGamesPerPair + ", expectedNumberOfMatches = " + expectedNumberOfMatches + ", creatorParticipation = " + creatorParticipation);

				games = Planner.planSeriesKO("test", dummyCache.getCurrentUser(), winners, null, new ArrayList<Map>(dummyCache.getMaps()), null, rules, null, creatorParticipation, false, false, numberOfGamesPerPair, 0);

				assertEquals(expectedNumberOfMatches, games.size());
				for(int i = 0; i < games.size(); i++)
				{
					pair = 2 * (i / numberOfGamesPerPair);
					// logger.debug("i = " + i + ", pair = " + pair);
					if(creatorParticipation != EnumCreatorParticipation.not_participating)
					{
						assertEquals(3, games.get(i).getPlayers().size());
						assertTrue(games.get(i).getPlayers().contains(dummyCache.getCurrentUser()));
					}
					else
					{
						assertEquals(2, games.get(i).getPlayers().size());
						assertFalse(games.get(i).getPlayers().contains(dummyCache.getCurrentUser()));
					}
					assertTrue(games.get(i).getPlayers().containsAll(winners.get(pair).getMembers()));
					assertTrue(games.get(i).getPlayers().containsAll(winners.get(pair + 1).getMembers()));
				}

				// with losers
				expectedNumberOfMatches *= 2;
				logger.debug("testing teamCount = " + teamCount + ", numberOfGamesPerPair = " + numberOfGamesPerPair + ", expectedNumberOfMatches = " + expectedNumberOfMatches + ", creatorParticipation = " + creatorParticipation + ", with losers");

				games = Planner.planSeriesKO("test", dummyCache.getCurrentUser(), winners, losers, new ArrayList<Map>(dummyCache.getMaps()), null, rules, null, creatorParticipation, false, false, numberOfGamesPerPair, 0);

				assertEquals(expectedNumberOfMatches, games.size());
				int i = 0;
				for(; i < games.size() / 2; i++)
				{
					pair = 2 * (i / numberOfGamesPerPair);
					// logger.debug("i = " + i + ", pair = " + pair);
					if(creatorParticipation != EnumCreatorParticipation.not_participating)
					{
						assertEquals(3, games.get(i).getPlayers().size());
						assertTrue(games.get(i).getPlayers().contains(dummyCache.getCurrentUser()));
					}
					else
					{
						assertEquals(2, games.get(i).getPlayers().size());
						assertFalse(games.get(i).getPlayers().contains(dummyCache.getCurrentUser()));
					}
					assertTrue(games.get(i).getPlayers().containsAll(winners.get(pair).getMembers()));
					assertTrue(games.get(i).getPlayers().containsAll(winners.get(pair + 1).getMembers()));
				}
				for(; i < games.size(); i++)
				{
					pair = 2 * ((i - games.size() / 2) / numberOfGamesPerPair);
					// logger.debug("i = " + i + ", pair = " + pair);
					if(creatorParticipation != EnumCreatorParticipation.not_participating)
					{
						assertEquals(3, games.get(i).getPlayers().size());
						assertTrue(games.get(i).getPlayers().contains(dummyCache.getCurrentUser()));
					}
					else
					{
						assertEquals(2, games.get(i).getPlayers().size());
						assertFalse(games.get(i).getPlayers().contains(dummyCache.getCurrentUser()));
					}
					assertTrue(games.get(i).getPlayers().containsAll(losers.get(pair).getMembers()));
					assertTrue(games.get(i).getPlayers().containsAll(losers.get(pair + 1).getMembers()));
				}
			}
		}
	}

	/**
	 * This covers {@link EnumGameSeriesType#Simple}
	 */
	@ParameterizedTest
	@ValueSource(ints = { 10, 100, 1000 })
	public void test_planSeriesSimple(int numberOfGames)
	{
		int numberOfPlayersPerGame = 5;
		Rules rules = new Rules();
		List<User> players = new ArrayList<>(dummyCache.getUsers());
		players.remove(dummyCache.getCurrentUser());
		List<PlannedGame> games;

		for(EnumCreatorParticipation creatorParticipation : EnumCreatorParticipation.values())
		{
			games = Planner.planSeriesSimple("test", dummyCache.getCurrentUser(), players, new ArrayList<>(dummyCache.getMaps()), rules, null, creatorParticipation, numberOfGames, numberOfPlayersPerGame);

			assertNotNull(games);
			assertEquals(numberOfGames, games.size());

			for(PlannedGame g : games)
			{
				assertTrue(g.getPlayers().size() <= numberOfPlayersPerGame);

				// check creator
				if(creatorParticipation != EnumCreatorParticipation.not_participating)
					assertTrue(g.getPlayers().contains(dummyCache.getCurrentUser()));
				else
					assertFalse(g.getPlayers().contains(dummyCache.getCurrentUser()));

				for(User p : g.getPlayers())
				{
					// don't check the creator
					if(p == dummyCache.getCurrentUser())
						continue;

					logger.debug("user " + p.getId() + " has games: \tactive=" + p.getActiveGames() + "\tplanned=" + p.getPlannedGames() + "\tmax=" + p.getMaxGames());
					assertTrue(p.getActiveGames() + p.getPlannedGames() <= p.getMaxGames() || p.getMaxGames() == 0);
				}
			}

			Planner.resetPlannedGames(players);
		}
	}
	
	@RepeatedTest(10)
	public void test_planTeamGame()
	{
		int id0, id1, id2;
		
		do
		{
			id0 = random.nextInt(dummyCache.getUsers().size());
			id1 = random.nextInt(dummyCache.getUsers().size());
			id2 = random.nextInt(dummyCache.getUsers().size());
			// must assure that all 3 IDs are different
		}
		while(id0 == id1 || id1 == id2 || id2 == id0);
		
		Team t1 = new Team("T" + id1, dummyCache.getUser(id1));
		Team t2 = new Team("T" + id2, dummyCache.getUser(id2));
		
		PlannedGame game;
		List<User> expectedPlayers;
		
		// home = t1, with creator
		game = Planner.planTeamGame("${teams}", dummyCache.getUser(id0), t1, t2, (ta, tb) -> { return ta; }, dummyCache.getMap(1), new Rules(), null, EnumCreatorParticipation.normal, new HashMap<>());
		assertEquals(t1.getName() + " vs. " + t2.getName(), game.getName());
		expectedPlayers = new ArrayList<>(3);
		expectedPlayers.addAll(t1.getMembers());
		expectedPlayers.addAll(t2.getMembers());
		expectedPlayers.add(dummyCache.getUser(id0));
		assertArrayEquals(expectedPlayers.toArray(), game.getPlayers().toArray());
		
		// home = t2, with creator
		game = Planner.planTeamGame("${teams}", dummyCache.getUser(id0), t1, t2, (ta, tb) -> { return tb; }, dummyCache.getMap(1), new Rules(), null, EnumCreatorParticipation.normal, new HashMap<>());
		assertEquals(t2.getName() + " vs. " + t1.getName(), game.getName());
		expectedPlayers = new ArrayList<>(3);
		expectedPlayers.addAll(t2.getMembers());
		expectedPlayers.addAll(t1.getMembers());
		expectedPlayers.add(dummyCache.getUser(id0));
		assertArrayEquals(expectedPlayers.toArray(), game.getPlayers().toArray());
		
		// home = t1, without creator
		game = Planner.planTeamGame("${teams}", dummyCache.getUser(id0), t1, t2, (ta, tb) -> { return ta; }, dummyCache.getMap(1), new Rules(), null, EnumCreatorParticipation.not_participating, new HashMap<>());
		assertEquals(t1.getName() + " vs. " + t2.getName(), game.getName());
		expectedPlayers = new ArrayList<>(2);
		expectedPlayers.addAll(t1.getMembers());
		expectedPlayers.addAll(t2.getMembers());
		assertArrayEquals(expectedPlayers.toArray(), game.getPlayers().toArray());
		
		// home = t2, without creator
		game = Planner.planTeamGame("${teams}", dummyCache.getUser(id0), t1, t2, (ta, tb) -> { return tb; }, dummyCache.getMap(1), new Rules(), null, EnumCreatorParticipation.not_participating, new HashMap<>());
		assertEquals(t2.getName() + " vs. " + t1.getName(), game.getName());
		expectedPlayers = new ArrayList<>(2);
		expectedPlayers.addAll(t2.getMembers());
		expectedPlayers.addAll(t1.getMembers());
		assertArrayEquals(expectedPlayers.toArray(), game.getPlayers().toArray());
	}

	@Test
	public void test_planGame()
	{
		// just test that all properties are passed to new PlannedGame(...)

		String title = "title";
		
		User creator = dummyCache.getUser(1);
		User participant2 = dummyCache.getUser(2);
		User participant3 = dummyCache.getUser(3);
		Set<User> gamePlayers = new HashSet<>();
		gamePlayers.add(participant2);
		gamePlayers.add(participant3);

		Map map = dummyCache.getMap(1);
		Options options = new Options(123, true, EnumGameDirection.formula1, EnumGameTC.allowed);
		Set<String> tags = new HashSet<>(Arrays.asList("abc", "123"));
		HashMap<String, String> placeholderValues = new HashMap<>();
		placeholderValues.put("foo", "bar");
		
		EnumCreatorParticipation creatorParticipation;
		
		PlannedGame game;

		// creator participating
		creatorParticipation= EnumCreatorParticipation.normal;
		game = Planner.planGame(title, creator, map, new LinkedHashSet<>(gamePlayers), options, tags, creatorParticipation, placeholderValues);

		assertEquals(title, game.getName());
		assertEquals(3, game.getPlayers().size());
		assertTrue(game.getPlayers().contains(creator));
		assertTrue(game.getPlayers().contains(participant2));
		assertTrue(game.getPlayers().contains(participant3));
		assertEquals(map, game.getMap());
		assertEquals(options, game.getOptions());
		assertEquals(tags, game.getTags());
		assertEquals(placeholderValues, game.getPlaceHolderValues());

		// creator leaving
		creatorParticipation= EnumCreatorParticipation.leave;
		game = Planner.planGame(title, creator, map, new LinkedHashSet<>(gamePlayers), options, tags, creatorParticipation, placeholderValues);

		assertEquals(title, game.getName());
		assertEquals(3, game.getPlayers().size());
		assertTrue(game.getPlayers().contains(creator));
		assertTrue(game.getPlayers().contains(participant2));
		assertTrue(game.getPlayers().contains(participant3));
		assertEquals(map, game.getMap());
		assertEquals(options, game.getOptions());
		assertEquals(tags, game.getTags());
		assertEquals(placeholderValues, game.getPlaceHolderValues());

		// creator NOT participating
		creatorParticipation= EnumCreatorParticipation.not_participating;
		game = Planner.planGame(title, creator, map, new LinkedHashSet<>(gamePlayers), options, tags, creatorParticipation, placeholderValues);

		assertEquals(title, game.getName());
		assertEquals(2, game.getPlayers().size());
		assertTrue(game.getPlayers().contains(participant2));
		assertTrue(game.getPlayers().contains(participant3));
		assertEquals(map, game.getMap());
		assertEquals(options, game.getOptions());
		assertEquals(tags, game.getTags());
		assertEquals(placeholderValues, game.getPlaceHolderValues());
	}
}
