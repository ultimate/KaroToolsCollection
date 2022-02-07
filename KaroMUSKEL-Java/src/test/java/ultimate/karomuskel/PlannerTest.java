package ultimate.karomuskel;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import muskel2.model.Direction;
import muskel2.model.Player;
import muskel2.model.Rules;
import ultimate.karoapi4j.model.extended.Match;
import ultimate.karoapi4j.model.extended.Team;
import ultimate.karoapi4j.model.official.User;

public class PlannerTest
{
	@Test
	public void test_league()
	{
		for(int i = 0; i < 1; i++)
		{
			List<User> list = new LinkedList<User>();
			List<Team> teams = new LinkedList<Team>();
			List<List<Match>> matches;
			boolean[][] check;
			int home;

			Team team0 = new Team("T0", list);
			Team team1 = new Team("T1", list);
			Team team2 = new Team("T2", list);
			Team team3 = new Team("T3", list);
			Team team4 = new Team("T4", list);
			Team team5 = new Team("T5", list);
			Team team6 = new Team("T6", list);
			Team team7 = new Team("T7", list);
			Team team8 = new Team("T8", list);
			Team team9 = new Team("T9", list);
			Team team10 = new Team("T10", list);
			Team team11 = new Team("T11", list);
			Team team12 = new Team("T12", list);
			Team team13 = new Team("T13", list);
			Team team14 = new Team("T14", list);
			Team team15 = new Team("T15", list);

			teams.add(team0);
			teams.add(team1);
			teams.add(team2);
			teams.add(team3);

			matches = planLeagueMatches(teams);
			check = new boolean[4][4];
			for(List<Match> dayList : matches)
			{
				for(Match match : dayList)
				{
					System.out.print(match.getTeam1().getName() + "-" + match.getTeam2().getName() + "\t");
					if(check[Integer.parseInt(match.getTeam1().getName().substring(1))][Integer.parseInt(match.getTeam2().getName().substring(1))])
						System.out.print("!");
					check[Integer.parseInt(match.getTeam1().getName().substring(1))][Integer.parseInt(match.getTeam2().getName().substring(1))] = true;
					check[Integer.parseInt(match.getTeam2().getName().substring(1))][Integer.parseInt(match.getTeam1().getName().substring(1))] = true;
				}
				System.out.println("");
			}
			System.out.println("");
			for(boolean[] bs : check)
			{
				for(boolean b : bs)
				{
					System.out.print(b ? "1" : "0");
				}
				System.out.println("");
			}
			System.out.println("");
			for(Team t : teams)
			{
				home = countHomeMatches(matches, t);
				System.out.println(t.getName() + ":\t H: " + home + "\t A:" + (teams.size() - 1 - home));
			}
			System.out.println("");

			teams.add(team4);
			teams.add(team5);

			matches = planLeagueMatches(teams);
			check = new boolean[6][6];
			for(List<Match> dayList : matches)
			{
				for(Match match : dayList)
				{
					System.out.print(match.getTeam1().getName() + "-" + match.getTeam2().getName() + "\t");
					if(check[Integer.parseInt(match.getTeam1().getName().substring(1))][Integer.parseInt(match.getTeam2().getName().substring(1))])
						System.out.print("!");
					check[Integer.parseInt(match.getTeam1().getName().substring(1))][Integer.parseInt(match.getTeam2().getName().substring(1))] = true;
					check[Integer.parseInt(match.getTeam2().getName().substring(1))][Integer.parseInt(match.getTeam1().getName().substring(1))] = true;
				}
				System.out.println("");
			}
			System.out.println("");
			for(boolean[] bs : check)
			{
				for(boolean b : bs)
				{
					System.out.print(b ? "1" : "0");
				}
				System.out.println("");
			}
			System.out.println("");
			for(Team t : teams)
			{
				home = countHomeMatches(matches, t);
				System.out.println(t.getName() + ":\t H: " + home + "\t A:" + (teams.size() - 1 - home));
			}
			System.out.println("");

			teams.add(team6);
			teams.add(team7);

			matches = planLeagueMatches(teams);
			check = new boolean[8][8];
			for(List<Match> dayList : matches)
			{
				for(Match match : dayList)
				{
					System.out.print(match.getTeam1().getName() + "-" + match.getTeam2().getName() + "\t");
					if(check[Integer.parseInt(match.getTeam1().getName().substring(1))][Integer.parseInt(match.getTeam2().getName().substring(1))])
						System.out.print("!");
					check[Integer.parseInt(match.getTeam1().getName().substring(1))][Integer.parseInt(match.getTeam2().getName().substring(1))] = true;
					check[Integer.parseInt(match.getTeam2().getName().substring(1))][Integer.parseInt(match.getTeam1().getName().substring(1))] = true;
				}
				System.out.println("");
			}
			System.out.println("");
			for(boolean[] bs : check)
			{
				for(boolean b : bs)
				{
					System.out.print(b ? "1" : "0");
				}
				System.out.println("");
			}
			System.out.println("");
			for(Team t : teams)
			{
				home = countHomeMatches(matches, t);
				System.out.println(t.getName() + ":\t H: " + home + "\t A:" + (teams.size() - 1 - home));
			}
			System.out.println("");

			teams.add(team8);
			teams.add(team9);

			matches = planLeagueMatches(teams);
			check = new boolean[10][10];
			for(List<Match> dayList : matches)
			{
				for(Match match : dayList)
				{
					System.out.print(match.getTeam1().getName() + "-" + match.getTeam2().getName() + "\t");
					if(check[Integer.parseInt(match.getTeam1().getName().substring(1))][Integer.parseInt(match.getTeam2().getName().substring(1))])
						System.out.print("!");
					check[Integer.parseInt(match.getTeam1().getName().substring(1))][Integer.parseInt(match.getTeam2().getName().substring(1))] = true;
					check[Integer.parseInt(match.getTeam2().getName().substring(1))][Integer.parseInt(match.getTeam1().getName().substring(1))] = true;
				}
				System.out.println("");
			}
			System.out.println("");
			for(boolean[] bs : check)
			{
				for(boolean b : bs)
				{
					System.out.print(b ? "1" : "0");
				}
				System.out.println("");
			}
			System.out.println("");
			for(Team t : teams)
			{
				home = countHomeMatches(matches, t);
				System.out.println(t.getName() + ":\t H: " + home + "\t A:" + (teams.size() - 1 - home));
			}
			System.out.println("");

			teams.add(team10);
			teams.add(team11);

			matches = planLeagueMatches(teams);
			check = new boolean[12][12];
			for(List<Match> dayList : matches)
			{
				for(Match match : dayList)
				{
					System.out.print(match.getTeam1().getName() + "-" + match.getTeam2().getName() + "\t");
					if(check[Integer.parseInt(match.getTeam1().getName().substring(1))][Integer.parseInt(match.getTeam2().getName().substring(1))])
						System.out.print("!");
					check[Integer.parseInt(match.getTeam1().getName().substring(1))][Integer.parseInt(match.getTeam2().getName().substring(1))] = true;
					check[Integer.parseInt(match.getTeam2().getName().substring(1))][Integer.parseInt(match.getTeam1().getName().substring(1))] = true;
				}
				System.out.println("");
			}
			System.out.println("");
			for(boolean[] bs : check)
			{
				for(boolean b : bs)
				{
					System.out.print(b ? "1" : "0");
				}
				System.out.println("");
			}
			System.out.println("");
			for(Team t : teams)
			{
				home = countHomeMatches(matches, t);
				System.out.println(t.getName() + ":\t H: " + home + "\t A:" + (teams.size() - 1 - home));
			}
			System.out.println("");

			teams.add(team12);
			teams.add(team13);

			matches = planLeagueMatches(teams);
			check = new boolean[14][14];
			for(List<Match> dayList : matches)
			{
				for(Match match : dayList)
				{
					System.out.print(match.getTeam1().getName() + "-" + match.getTeam2().getName() + "\t");
					if(check[Integer.parseInt(match.getTeam1().getName().substring(1))][Integer.parseInt(match.getTeam2().getName().substring(1))])
						System.out.print("!");
					check[Integer.parseInt(match.getTeam1().getName().substring(1))][Integer.parseInt(match.getTeam2().getName().substring(1))] = true;
					check[Integer.parseInt(match.getTeam2().getName().substring(1))][Integer.parseInt(match.getTeam1().getName().substring(1))] = true;
				}
				System.out.println("");
			}
			System.out.println("");
			for(boolean[] bs : check)
			{
				for(boolean b : bs)
				{
					System.out.print(b ? "1" : "0");
				}
				System.out.println("");
			}
			System.out.println("");
			for(Team t : teams)
			{
				home = countHomeMatches(matches, t);
				System.out.println(t.getName() + ":\t H: " + home + "\t A:" + (teams.size() - 1 - home));
			}
			System.out.println("");

			teams.add(team14);
			teams.add(team15);

			matches = planLeagueMatches(teams);
			check = new boolean[16][16];
			for(List<Match> dayList : matches)
			{
				for(Match match : dayList)
				{
					System.out.print(match.getTeam1().getName() + "-" + match.getTeam2().getName() + "\t");
					if(check[Integer.parseInt(match.getTeam1().getName().substring(1))][Integer.parseInt(match.getTeam2().getName().substring(1))])
						System.out.print("!");
					check[Integer.parseInt(match.getTeam1().getName().substring(1))][Integer.parseInt(match.getTeam2().getName().substring(1))] = true;
					check[Integer.parseInt(match.getTeam2().getName().substring(1))][Integer.parseInt(match.getTeam1().getName().substring(1))] = true;
				}
				System.out.println("");
			}
			System.out.println("");
			for(boolean[] bs : check)
			{
				for(boolean b : bs)
				{
					System.out.print(b ? "1" : "0");
				}
				System.out.println("");
			}
			System.out.println("");
			for(Team t : teams)
			{
				home = countHomeMatches(matches, t);
				System.out.println(t.getName() + ":\t H: " + home + "\t A:" + (teams.size() - 1 - home));
			}
			System.out.println("");
		}
	}
	
	@Test
	public void test_shuffle()
	{
			int totalPlayers = 30;
			int numberOfMaps = 20;
			int gamesPerPlayer = 6;

			List<Player> players = new LinkedList<Player>();
			for(char c = 65; c < 65 + totalPlayers; c++)
			{
				players.add(new Player((int) c, "" + c, true, true, 9999, 0, 0, 999, new Color(c, c, c), 0, null));
			}

			List<Rules> rules = new LinkedList<Rules>();
			for(int i = 0; i < numberOfMaps; i++)
			{
//				int numberOfPlayers = (i < 6 ? 5 : (i < 12 ? 7 : 10));
				int numberOfPlayers = (i < 6 ? 3 : (i < 12 ? 4 : 5));
				rules.add(new Rules(0, 0, true, true, Direction.egal, false, false, gamesPerPlayer, numberOfPlayers));
			}

			Player[][][] shuffledPlayers = shufflePlayers(players, rules);

			// print games
			for(int m = 0; m < numberOfMaps; m++)
			{
				System.out.println(m);
				for(int g = 0; g < shuffledPlayers[m].length; g++)
				{
					System.out.print((g > 9 ? "" : " ") + g + ": ");
					for(int p = 0; p < rules.get(m).getNumberOfPlayers(); p++)
					{
						if(p >= 0)
						{
							if(shuffledPlayers[m][g][p] != null)
								System.out.print(shuffledPlayers[m][g][p].getName() + " ");
						}
						else
							System.out.println(" ");
					}
					System.out.println("");
				}
			}

			// print playersGames
			System.out.println("playersGames");
			for(int pl = 0; pl < totalPlayers; pl++)
			{
				for(int m = 0; m < numberOfMaps; m++)
				{
					int count = 0;
					for(int g = 0; g < shuffledPlayers[m].length; g++)
					{
						for(int p = 0; p < rules.get(m).getNumberOfPlayers(); p++)
						{
							if(shuffledPlayers[m][g][p] == players.get(pl))
								count++;
						}
					}
					System.out.print(count + " ");
				}
				System.out.println("");
			}

	}
}
