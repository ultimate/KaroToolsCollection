package muskel2.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import ultimate.karoapi4j.model.extended.Match;
import ultimate.karoapi4j.model.extended.Team;

public abstract class LeaguePlanner
{
	public static List<List<Match>> league(List<Team> teams)
	{
		if(teams.size() % 2 == 1)
			throw new IllegalArgumentException("equal number of teams required");
		// oder Team "frei" hinzufügen

		int teamcount = teams.size(); // Anzahl der Teams
		int matchcount = teamcount / 2; // Anzahl der möglichen Spielpaare
		int days = teamcount - 1; // Anzahl der Spieltage pro Runde
		List<List<Match>> matches = new LinkedList<List<Match>>(); // Spielplan
		int gamenr = 0; // Zähler für Spielnummer

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

	public static List<Match> createMatchesSpecial(List<Team> teams, int numberOfTeamsPerMatch)
	{
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

	private static boolean incrementSelectors(int[] s, int max)
	{
		int pointer = s.length-1;
		
		boolean success = false;
		while(pointer >= 0)
		{
			if(s[pointer] < max-(s.length-pointer))
			{
				s[pointer]++;
				for(int p = pointer + 1; p < s.length; p++)
				{
					s[p] = s[p-1]+1;
					if(s[p] > max-(s.length-p))
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

	public static int calculateNumberOfMatches(int teams, int teamsPerMatch)
	{
		BigInteger ret = BigInteger.ONE;
		for(int k = 0; k < teamsPerMatch; k++)
		{
			ret = ret.multiply(BigInteger.valueOf(teams - k)).divide(BigInteger.valueOf(k + 1));
		}
		return ret.intValue();
	}

	private static int countHomeMatches(List<List<Match>> matches, Team t)
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

	public static void main(String[] args)
	{
		int[] s = new int[] {0,1,2,3};
		do
		{
			for(int i = 0; i < s.length; i++)
				System.out.print(s[i] + " ");
			System.out.println();
		}
		while(incrementSelectors(s, 8));
		
		
		
		
		for(int i = 0; i < 1; i++)
		{
			List<Player> list = new LinkedList<Player>();
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

			matches = createMatches(teams);
			check = new boolean[4][4];
			for(List<Match> dayList : matches)
			{
				for(Match match : dayList)
				{
					System.out.print(match.getTeam(0).getName() + "-" + match.getTeam(1).getName() + "\t");
					if(check[Integer.parseInt(match.getTeam(0).getName().substring(1))][Integer.parseInt(match.getTeam(1).getName().substring(1))])
						System.out.print("!");
					check[Integer.parseInt(match.getTeam(0).getName().substring(1))][Integer.parseInt(match.getTeam(1).getName().substring(1))] = true;
					check[Integer.parseInt(match.getTeam(1).getName().substring(1))][Integer.parseInt(match.getTeam(0).getName().substring(1))] = true;
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

			matches = createMatches(teams);
			check = new boolean[6][6];
			for(List<Match> dayList : matches)
			{
				for(Match match : dayList)
				{
					System.out.print(match.getTeam(0).getName() + "-" + match.getTeam(1).getName() + "\t");
					if(check[Integer.parseInt(match.getTeam(0).getName().substring(1))][Integer.parseInt(match.getTeam(1).getName().substring(1))])
						System.out.print("!");
					check[Integer.parseInt(match.getTeam(0).getName().substring(1))][Integer.parseInt(match.getTeam(1).getName().substring(1))] = true;
					check[Integer.parseInt(match.getTeam(1).getName().substring(1))][Integer.parseInt(match.getTeam(0).getName().substring(1))] = true;
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

			matches = createMatches(teams);
			check = new boolean[8][8];
			for(List<Match> dayList : matches)
			{
				for(Match match : dayList)
				{
					System.out.print(match.getTeam(0).getName() + "-" + match.getTeam(1).getName() + "\t");
					if(check[Integer.parseInt(match.getTeam(0).getName().substring(1))][Integer.parseInt(match.getTeam(1).getName().substring(1))])
						System.out.print("!");
					check[Integer.parseInt(match.getTeam(0).getName().substring(1))][Integer.parseInt(match.getTeam(1).getName().substring(1))] = true;
					check[Integer.parseInt(match.getTeam(1).getName().substring(1))][Integer.parseInt(match.getTeam(0).getName().substring(1))] = true;
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

			matches = createMatches(teams);
			check = new boolean[10][10];
			for(List<Match> dayList : matches)
			{
				for(Match match : dayList)
				{
					System.out.print(match.getTeam(0).getName() + "-" + match.getTeam(1).getName() + "\t");
					if(check[Integer.parseInt(match.getTeam(0).getName().substring(1))][Integer.parseInt(match.getTeam(1).getName().substring(1))])
						System.out.print("!");
					check[Integer.parseInt(match.getTeam(0).getName().substring(1))][Integer.parseInt(match.getTeam(1).getName().substring(1))] = true;
					check[Integer.parseInt(match.getTeam(1).getName().substring(1))][Integer.parseInt(match.getTeam(0).getName().substring(1))] = true;
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

			matches = createMatches(teams);
			check = new boolean[12][12];
			for(List<Match> dayList : matches)
			{
				for(Match match : dayList)
				{
					System.out.print(match.getTeam(0).getName() + "-" + match.getTeam(1).getName() + "\t");
					if(check[Integer.parseInt(match.getTeam(0).getName().substring(1))][Integer.parseInt(match.getTeam(1).getName().substring(1))])
						System.out.print("!");
					check[Integer.parseInt(match.getTeam(0).getName().substring(1))][Integer.parseInt(match.getTeam(1).getName().substring(1))] = true;
					check[Integer.parseInt(match.getTeam(1).getName().substring(1))][Integer.parseInt(match.getTeam(0).getName().substring(1))] = true;
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

			matches = createMatches(teams);
			check = new boolean[14][14];
			for(List<Match> dayList : matches)
			{
				for(Match match : dayList)
				{
					System.out.print(match.getTeam(0).getName() + "-" + match.getTeam(1).getName() + "\t");
					if(check[Integer.parseInt(match.getTeam(0).getName().substring(1))][Integer.parseInt(match.getTeam(1).getName().substring(1))])
						System.out.print("!");
					check[Integer.parseInt(match.getTeam(0).getName().substring(1))][Integer.parseInt(match.getTeam(1).getName().substring(1))] = true;
					check[Integer.parseInt(match.getTeam(1).getName().substring(1))][Integer.parseInt(match.getTeam(0).getName().substring(1))] = true;
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

			matches = createMatches(teams);
			check = new boolean[16][16];
			for(List<Match> dayList : matches)
			{
				for(Match match : dayList)
				{
					System.out.print(match.getTeam(0).getName() + "-" + match.getTeam(1).getName() + "\t");
					if(check[Integer.parseInt(match.getTeam(0).getName().substring(1))][Integer.parseInt(match.getTeam(1).getName().substring(1))])
						System.out.print("!");
					check[Integer.parseInt(match.getTeam(0).getName().substring(1))][Integer.parseInt(match.getTeam(1).getName().substring(1))] = true;
					check[Integer.parseInt(match.getTeam(1).getName().substring(1))][Integer.parseInt(match.getTeam(0).getName().substring(1))] = true;
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
