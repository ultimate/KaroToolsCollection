package ultimate.karomuskel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.extended.Match;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karoapi4j.model.extended.Team;
import ultimate.karoapi4j.model.official.User;
import ultimate.karomuskel.Planner.ShuffleResult;
import ultimate.karomuskel.test.KaroMUSKELTestcase;

public class PlannerTest extends KaroMUSKELTestcase
{
	protected Planner planner;
	// TODO Test

	@BeforeAll
	public void setUpOnce() throws IOException
	{
		super.setUpOnce();
		planner = new Planner(dummyCache);
	}

	@ParameterizedTest
	@ValueSource(ints = { 4, 6, 8, 10, 12, 14, 16 })
	public void test_planMatchesLeague(int teamCount)
	{
		logger.debug("league-test for " + teamCount + " teams");

		int expectedNumberOfDays = (teamCount - 1);
		int expectedNumberOfMatches = expectedNumberOfDays * teamCount/2;

		List<User> list = new LinkedList<User>();
		List<Team> teams = new LinkedList<Team>();
		List<List<Match>> matches;
		int[][] check;
		int home;

		for(int i = 0; i < teamCount; i++)
		{
			teams.add(new Team("T" + i, list));
		}

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
				check[Integer.parseInt(match.getTeam(0).getName().substring(1))][Integer.parseInt(match.getTeam(1).getName().substring(1))]++;
				check[Integer.parseInt(match.getTeam(1).getName().substring(1))][Integer.parseInt(match.getTeam(0).getName().substring(1))]++;
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

	@Test
	public void test_shufflePlayers()
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
//				int numberOfPlayers = (i < 6 ? 5 : (i < 12 ? 7 : 10));
			int numberOfPlayers = (i < 6 ? 3 : (i < 12 ? 4 : 5));
			rules.add(new Rules(0, 0, EnumGameTC.free, true, EnumGameDirection.free, gamesPerPlayer, numberOfPlayers));
		}

		ShuffleResult result;

		while(true)
		{
			try
			{
				// shuffeling can fail (see Planner#shufflePlayers)
				result = Planner.shufflePlayers0(players, rules);
				break;
			}
			catch(IllegalArgumentException e)
			{
				continue;
			}
		}

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
}
