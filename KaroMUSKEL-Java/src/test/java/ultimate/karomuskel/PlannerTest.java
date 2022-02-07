package ultimate.karomuskel;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

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
}
