package ultimate.karomuskel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.base.Identifiable;
import ultimate.karoapi4j.model.extended.Match;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karoapi4j.model.extended.Team;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.Options;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karomuskel.ui.Language;

public class Planner
{
	private static Random	random	= new Random();

	private KaroAPICache	karoAPICache;

	public Planner(KaroAPICache karoAPICache)
	{
		this.karoAPICache = karoAPICache;
	}

	public List<PlannedGame> allCombinations(List<Team> teams, List<Map> maps, String title, Rules rules, int numberOfGamesPerPair, int numberOfTeamsPerMatch)
	{
		// create local copy of the input list
		List<Team> tmp = new LinkedList<>(teams);
		Collections.shuffle(tmp, random);

		List<Match> matches = leagueSpecial(tmp, numberOfTeamsPerMatch);
		List<PlannedGame> games = new LinkedList<>();

		PlannedGame game;
		String name;
		Map map;
		List<User> players;
		int count = 0;
		Options options;
		for(int round = 1; round <= numberOfGamesPerPair; round++)
		{
			for(Match match : matches)
			{
				map = maps.get(random.nextInt(maps.size()));

				players = new LinkedList<User>();
				players.add(karoAPICache.getCurrentUser());
				for(Team team : match.getTeams())
				{
					for(User member : team.getMembers())
					{
						if(!players.contains(member))
							players.add(member);
					}
				}

				increasePlannedGames(players);

				options = rules.clone().createOptions();
				name = applyPlaceholders(title, map, players, options, count, 0, 0, match.getTeams(), -1, -1);

				game = new PlannedGame(name, map.getId(), toIntArray(players), options);

				games.add(game);
				count++;
			}
		}

		return games;
	}

	// OLD

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

	public static int calculateNumberOfMatches(int teams, int teamsPerMatch)
	{
		BigInteger ret = BigInteger.ONE;
		for(int k = 0; k < teamsPerMatch; k++)
		{
			ret = ret.multiply(BigInteger.valueOf(teams - k)).divide(BigInteger.valueOf(k + 1));
		}
		return ret.intValue();
	}

	public static List<Match> leagueSpecial(List<Team> teams, int numberOfTeamsPerMatch)
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

	protected static void resetPlannedGames(List<User> users)
	{
		for(User user : users)
			user.setPlannedGames(0);
	}

	protected static void increasePlannedGames(List<User> users)
	{
		for(User user : users)
			user.setPlannedGames(user.getPlannedGames() + 1);
	}

	public String applyPlaceholders(String title, Map map, List<User> players, Options options, int count, int day, int dayCount, Team[] teams, int round, int group)
	{
		String name = title;
		StringBuilder tmp;

		// zaehlung
		name = name.replace("${i}", "" + (count + 1));
		name = name.replace("${ii}", toString(count + 1, 2));
		name = name.replace("${iii}", toString(count + 1, 3));
		name = name.replace("${iiii}", toString(count + 1, 4));
		if(day >= 0)
		{
			name = name.replace("${spieltag}", "" + (day + 1));
			name = name.replace("${spieltag.i}", "" + (dayCount + 1));
		}
		else
		{
			name = name.replace("${spieltag}", "");
			name = name.replace("${spieltag.i}", "");
		}

		// karte
		name = name.replace("${karte.id}", "" + map.getId());
		name = name.replace("${karte.name}", map.getName());

		// spieler
		name = name.replace("${spieler.ersteller}", karoAPICache.getCurrentUser().getLogin());
		name = name.replace("${spieler.anzahl}", "" + players.size());
		name = name.replace("${spieler.anzahl.x}", "" + (players.size() - 1));
		if(name.contains("${spieler.namen}"))
		{
			tmp = new StringBuilder();
			for(int p = 0; p < players.size(); p++)
			{
				if((p != 0) && (p != players.size() - 1))
					tmp.append(", ");
				else if((p != 0) && (p == players.size() - 1))
					tmp.append(" " + Language.getString("titlepatterns.and") + " ");
				tmp.append(players.get(p).getLogin());
			}
			name = name.replace("${spieler.namen}", tmp.toString());
		}
		if(name.contains("${spieler.namen.x}"))
		{
			tmp = new StringBuilder();
			List<User> tmpList = new LinkedList<User>(players);
			tmpList.remove(karoAPICache.getCurrentUser());
			for(int p = 0; p < tmpList.size(); p++)
			{
				if((p != 0) && (p != tmpList.size() - 1))
					tmp.append(", ");
				else if((p != 0) && (p == tmpList.size() - 1))
					tmp.append(" " + Language.getString("titlepatterns.and") + " ");
				tmp.append(tmpList.get(p).getLogin());
			}
			name = name.replace("${spieler.namen.x}", tmp.toString());
		}

		// teams
		if(teams != null)
		{
			tmp = new StringBuilder();

			for(int i = 0; i < teams.length; i++)
			{
				if(i > 0)
					tmp.append(" vs. ");
				tmp.append(teams[i].getName());
			}

			name = name.replace("${teams}", tmp.toString());
		}

		// runde
		if(round > 0)
		{
			if(name.contains("${runde}"))
			{
				if(round == 2)
					name = name.replace("${runde}", Language.getString("titlepatterns.final"));
				else if(round == 4)
					name = name.replace("${runde}", Language.getString("titlepatterns.semifinal"));
				else if(round == 8)
					name = name.replace("${runde}", Language.getString("titlepatterns.quarterfinal"));
				else if(group <= 0)
					name = name.replace("${runde}", Language.getString("titlepatterns.roundOf").replace("${i/2}", "" + (round / 2)).replace("${i}", "" + (round)));
				else
					name = name.replace("${runde}", Language.getString("titlepatterns.groupStage").replace("${i}", "" + (group)));
			}
			if(name.contains("${runde.x}"))
			{
				if(round == 2)
					name = name.replace("${runde.x}", Language.getString("titlepatterns.final") + ", " + Language.getString("titlepatterns.match") + " " + count);
				else if(round == 4)
					name = name.replace("${runde.x}", Language.getString("titlepatterns.semifinal") + ", " + Language.getString("titlepatterns.match") + " " + count);
				else if(round == 8)
					name = name.replace("${runde.x}", Language.getString("titlepatterns.quarterfinal") + ", " + Language.getString("titlepatterns.match") + " " + count);
				else if(group <= 0)
					name = name.replace("${runde.x}", Language.getString("titlepatterns.roundOf").replace("${i/2}", "" + (round / 2)).replace("${i}", "" + (round)) + ", "
							+ Language.getString("titlepatterns.match") + " " + count);
				else
					name = name.replace("${runde.x}", Language.getString("titlepatterns.groupStage").replace("${i}", "" + (group)) + ", " + Language.getString("titlepatterns.day") + " " + (day + 1));
			}
		}

		// regeln
		if(name.contains("${regeln}"))
		{
			tmp = new StringBuilder();
			tmp.append(Language.getString("titlepatterns.zzz"));
			tmp.append(options.getZzz());
			tmp.append(", ");
			tmp.append(Language.getString("titlepatterns.tc." + options.getCrashallowed()));
			tmp.append(", ");
			tmp.append(Language.getString("titlepatterns.cps." + options.isCps()));
			tmp.append(", ");
			tmp.append(Language.getString("titlepatterns.direction"));
			tmp.append(options.getStartdirection());
			name = name.replace("${regeln}", tmp);
		}
		if(name.contains("${regeln.x}"))
		{
			tmp = new StringBuilder();
			if(options.getZzz() != 2)
			{
				tmp.append(Language.getString("titlepatterns.zzz"));
				tmp.append(options.getZzz());
			}
			if(options.getCrashallowed() != EnumGameTC.forbidden)
			{
				if(!tmp.toString().isEmpty())
					tmp.append(", ");
				tmp.append(Language.getString("titlepatterns.tc." + options.getCrashallowed()));
			}
			if(!options.isCps())
			{
				if(!tmp.toString().isEmpty())
					tmp.append(", ");
				tmp.append(Language.getString("titlepatterns.cps." + options.isCps()));
			}
			if(options.getStartdirection() != EnumGameDirection.classic)
			{
				if(!tmp.toString().isEmpty())
					tmp.append(", ");
				tmp.append(Language.getString("titlepatterns.direction"));
				tmp.append(options.getStartdirection());
			}
			name = name.replace("${regeln.x}", tmp);
		}
		name = name.replace("${regeln.zzz}", Language.getString("titlepatterns.zzz") + options.getZzz());
		name = name.replace("${regeln.tc}", Language.getString("titlepatterns.tc." + options.getCrashallowed()));
		name = name.replace("${regeln.cps}", Language.getString("titlepatterns.cps." + options.isCps()));
		name = name.replace("${regeln.richtung}", Language.getString("titlepatterns.direction") + options.getStartdirection());

		name = name.replace("  ", "");
		name = name.trim();
		if(name.endsWith(","))
			name = name.substring(0, name.length() - 1);

		return name;
	}

	/**
	 * Shortcut for {@link PlaceholderFactory#applyPlaceholders(karoAPICache, String, Map, List, Rules, int, int, int, Team, Team, int)}
	 * Note: some Placeholders might not work here!
	 * 
	 * @param karoAPICache
	 * @param title
	 * @param game
	 * @param count
	 * @return
	 */
	public String applyPlaceholders(String title, PlannedGame game, int count)
	{
		Map map = this.karoAPICache.getMap(game.getMap());
		List<User> players = new ArrayList<>(game.getPlayers().length);
		for(int pid : game.getPlayers())
			players.add(this.karoAPICache.getUser(pid));
		return applyPlaceholders(title, map, players, game.getOptions(), count, 0, 0, null, 0, 0);
	}

	// helpers

	private static String toString(int x, int digits)
	{
		String s = "" + x;
		while(s.length() < digits)
			s = "0" + s;
		return s;
	}

	private static int[] toIntArray(List<? extends Identifiable> list)
	{
		int[] array = new int[list.size()];
		int cursor = 0;
		for(Identifiable i : list)
			array[cursor++] = i.getId();
		return array;
	}
}
