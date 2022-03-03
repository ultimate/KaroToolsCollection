package ultimate.karomuskel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.extended.Match;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karoapi4j.model.extended.Team;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karomuskel.Planner.ShuffleResult;
import ultimate.karomuskel.test.KaroMUSKELTestcase;
import ultimate.karomuskel.ui.Language;

public class PlannerTest extends KaroMUSKELTestcase
{
	protected Planner planner;

	@BeforeAll
	public void setUpOnce() throws IOException
	{
		super.setUpOnce();
		planner = new Planner(dummyCache);

		// needed for the placeholder stuff
		Language.load("de");
	}

	/**
	 * This covers {@link EnumGameSeriesType#League} and {@link EnumGameSeriesType#KLC} (group phase)
	 */
	@ParameterizedTest
	@ValueSource(ints = { 4, 6, 8, 10, 12, 14, 16 })
	public void test_planMatchesLeague(int teamCount)
	{
		logger.debug("league-test for " + teamCount + " teams");

		int expectedNumberOfDays = (teamCount - 1);
		int expectedNumberOfMatches = expectedNumberOfDays * teamCount / 2;

		// create teams
		List<Team> teams = new LinkedList<Team>();
		for(int i = 1; i <= teamCount; i++)
			teams.add(new Team("T" + i, Arrays.asList(dummyCache.getUser(10 + i))));

		// test planning
		List<List<Match>> matches;
		int[][] check;
		int home;

		StringBuilder sb;
		matches = planner.planMatchesLeague(teams);

		assertEquals(expectedNumberOfDays, matches.size());

		check = new int[teamCount][teamCount];
		int day = 1;
		int matchCount = 0;
		for(List<Match> dayList : matches)
		{
			sb = new StringBuilder();
			sb.append("day " + day++ + "\t");
			for(Match match : dayList)
			{
				matchCount++;
				sb.append(match.getTeam(0).getName() + "-" + match.getTeam(1).getName() + "\t");
				check[Integer.parseInt(match.getTeam(0).getName().substring(1))-1][Integer.parseInt(match.getTeam(1).getName().substring(1))-1]++;
				check[Integer.parseInt(match.getTeam(1).getName().substring(1))-1][Integer.parseInt(match.getTeam(0).getName().substring(1))-1]++;
			}
			logger.debug(sb.toString());
		}

		assertEquals(expectedNumberOfMatches, matchCount);

		for(int i1 = 0; i1 < teamCount; i1++)
		{
			for(int i2 = 0; i2 < teamCount; i2++)
			{
				if(i1 == i2)
					assertEquals(0, check[i1][i2], "invalid match count for T" + i1 + " vs. T" + i2 + ": " + check[i1][i2]);
				else
					assertEquals(1, check[i1][i2], "invalid match count for T" + i1 + " vs. T" + i2 + ": " + check[i1][i2]);
			}
		}
		int totalHome = 0;
		int expectedHomePerPlayer = (teamCount - 1) / 2;
		logger.debug("expecting home counr = " + expectedHomePerPlayer + " or " + (expectedHomePerPlayer + 1));
		for(Team t : teams)
		{
			home = Planner.countHomeMatches(matches, t);
			totalHome += home;
			logger.debug(t.getName() + ": home=" + home + ", guest=" + (teamCount - 1 - home));
			assertTrue(home == expectedHomePerPlayer || home == (expectedHomePerPlayer + 1), "invalid home match count for " + t.getName() + ": " + home);
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
			teams.add(new Team("T" + i, Arrays.asList(dummyCache.getUser(10 + i))));

		// test planning
		List<Match> matches;
		int expectedNumberOfMatches;
		for(int numberOfTeamsPerMatch = 1; numberOfTeamsPerMatch <= teamCount; numberOfTeamsPerMatch++)
		{
			expectedNumberOfMatches = Planner.calculateNumberOfMatches(teamCount, numberOfTeamsPerMatch);
			logger.debug("testing teamCount = " + teamCount + ", numberOfTeamsPerMatch = " + numberOfTeamsPerMatch + ", expectedNumberOfMatches = " + expectedNumberOfMatches);
			matches = planner.planMatchesAllCombinations(teams, numberOfTeamsPerMatch);
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
		List<Team> teams = new LinkedList<Team>();
		for(int i = 1; i <= teamCount; i++)
			teams.add(new Team("T" + i, Arrays.asList(dummyCache.getUser(10 + i))));

		Rules rules = new Rules();

		List<PlannedGame> games = planner.planSeriesKO("test", teams, new ArrayList<>(dummyCache.getMaps()), null, rules, false, false);

		assertEquals(teamCount / 2, games.size());
		for(int i = 0; i < games.size(); i++)
		{
			assertEquals(3, games.get(i).getPlayers().size());
			assertTrue(games.get(i).getPlayers().contains(dummyCache.getCurrentUser()));
			assertTrue(games.get(i).getPlayers().containsAll(teams.get(2*i).getMembers()));
			assertTrue(games.get(i).getPlayers().containsAll(teams.get(2*i+1).getMembers()));
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
		List<PlannedGame> games = planner.planSeriesSimple("test", players, new ArrayList<>(dummyCache.getMaps()), rules, numberOfGames, numberOfPlayersPerGame);

		assertNotNull(games);
		assertEquals(numberOfGames, games.size());

		for(PlannedGame g : games)
		{
			assertTrue(g.getPlayers().size() <= numberOfPlayersPerGame);
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
