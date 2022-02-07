package ultimate.karomuskel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import muskel2.model.Game;
import muskel2.model.Player;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.base.Identifiable;
import ultimate.karoapi4j.model.extended.GameSeries;
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

	public List<PlannedGame> plan(GameSeries gs)
	{
		if(gs == null || gs.getType() == null)
			throw new IllegalArgumentException("gameseries & type must not be null!");

		switch(gs.getType())
		{
			case AllCombinations:
				return planAllCombinations(gs.getTitle(), gs.getTeams(), gs.getMaps(), gs.getRules(), 0, 0);
			case Balanced:
				return planBalanced(gs.getTitle(), null, null, null);
			case KLC:
				return planKLC(gs.getTitle());
			case KO:
				return planKO(gs.getTitle());
			case League:
				return planLeague(gs.getTitle());
			case Simple:
				return planSimple(gs.getTitle());
			default:
				return null;
		}
	}

	public List<PlannedGame> planAllCombinations(String title, List<Team> teams, List<Map> maps, Rules rules, int numberOfGamesPerPair, int numberOfTeamsPerMatch)
	{
		List<PlannedGame> games = new LinkedList<>();

		// create local copy of the input list
		List<Team> tmp = new LinkedList<>(teams);
		Collections.shuffle(tmp, random);

		List<Match> matches = leagueSpecial(tmp, numberOfTeamsPerMatch);

		PlannedGame game;
		String name;
		Map map;
		List<User> gamePlayers;
		int count = 0;
		Options options;
		for(int round = 1; round <= numberOfGamesPerPair; round++)
		{
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

				// TODO separate
				increasePlannedGames(gamePlayers);
				options = rules.createOptions();
				name = applyPlaceholders(title, map, gamePlayers, options, count, 0, 0, match.getTeams(), -1, -1);
				game = new PlannedGame(name, map.getId(), toIntArray(gamePlayers), options);

				games.add(game);
				count++;
			}
		}

		return games;
	}

	public List<PlannedGame> planBalanced(String title, List<User> players, java.util.Map<Integer, List<Map>> gameDayMaps, java.util.Map<Integer, Rules> gameDayRules)
	{
		if(!checkKeys(gameDayMaps, gameDayRules))
			throw new IllegalArgumentException("gameDayMaps & gameDayRules must have equals size and keys");

		List<PlannedGame> games = new LinkedList<>();

		PlannedGame game;
		List<User> gamePlayers;
		Map map;
		Options options;
		String name;
		int count = 0;
		int dayCount;

		User[][][] shuffledPlayers = shufflePlayers(players, new ArrayList<>(gameDayRules.values()));

		for(int i = 0; i < gameDayMaps.size(); i++)
		{
			dayCount = 0;

			int gamesBefore = games.size();

			for(int g = 0; g < shuffledPlayers[i].length; g++)
			{
				gamePlayers = new LinkedList<User>();

				for(int p = 0; p < shuffledPlayers[i][g].length; p++)
				{
					if(shuffledPlayers[i][g][p] == null)
						continue;
					gamePlayers.add(shuffledPlayers[i][g][p]);
				}
				gamePlayers.add(karoAPICache.getCurrentUser());

				// TODO separate
				increasePlannedGames(gamePlayers);
				options = gameDayRules.get(i).createOptions();
				map = gameDayMaps.get(i).get(random.nextInt(gameDayMaps.get(i).size()));
				name = applyPlaceholders(title, map, gamePlayers, options, count, i, dayCount, null, -1, -1);
				game = new PlannedGame(name, map.getId(), toIntArray(gamePlayers), options);
				
				games.add(game);
				count++;
				dayCount++;
			}

			int gamesAfter = games.size();
			System.out.println("map #" + i + " games planned:   " + (gamesAfter - gamesBefore));
		}
		return games;
	}

	public List<PlannedGame> planKLC(String title, int round, int groups, int leagues)
	{	
		// HomeMaps für Serialization sichern
//		homeMaps.clear();
//		for(Player p : allPlayers)
//			homeMaps.put(p.getId(), p.getHomeMap().getId());

		int players = groups * leagues;
		
		if(round == players)
			return planKLCGroupphase(title, round);
		else
			return planKLCKOPhase(title, round);
	}

	public List<PlannedGame> planKLCGroupphase(String title, int round)
	{
		List<PlannedGame> games = new LinkedList<>();

		// Liegen durchmischen
		for(int l = 1; l <= LEAGUES; l++)
			Collections.shuffle(getPlayersLeagueX(l));

		// Gruppenphase
		for(int g = 1; g <= GROUPS; g++)
		{
			// Bilde Gruppe aus je 1 Spieler pro Liga
			// die Ligen sind 1mal initial durchgemischt, deshalb können wir einfach für Gruppe
			// X jeweils den X-ten Spieler pro Gruppe nehmen
			getPlayersGroupX(g).clear();
			getPlayersGroupX(g).add(getPlayersLeagueX(1).get(g - 1));
			getPlayersGroupX(g).add(getPlayersLeagueX(2).get(g - 1));
			getPlayersGroupX(g).add(getPlayersLeagueX(3).get(g - 1));
			getPlayersGroupX(g).add(getPlayersLeagueX(4).get(g - 1));
			Collections.shuffle(getPlayersGroupX(g), random);

			// create a temporarily list of teams to be able to use the LeaguePlanner
			List<Team> teamsTmp = new ArrayList<Team>(getPlayersGroupX(g).size());
			for(Player p : getPlayersGroupX(g))
			{
				teamsTmp.add(new Team(p.getName(), Arrays.asList(p)));
			}
			List<List<Match>> matches = LeaguePlanner.createMatches(teamsTmp);

			int day = 0;
			Game game;
			String name;
			Map map;
			Player home, guest;
			List<Player> gamePlayers;
			int count = 0;
			int dayCount;
			Rules tmpRules;
			for(List<Match> matchesForDay : matches)
			{
				dayCount = 0;
				for(Match match : matchesForDay)
				{
					if(match.getTeam(0).getPlayers().get(0).getLeague() > match.getTeam(1).getPlayers().get(0).getLeague())
					{
						// Spieler 0 ist in der niedrigeren Liga (= höhere Liga Nummer)
						home = match.getTeam(0).getPlayers().get(0);
						guest = match.getTeam(1).getPlayers().get(0);
					}
					else if(match.getTeam(0).getPlayers().get(0).getLeague() < match.getTeam(1).getPlayers().get(0).getLeague())
					{
						// Spieler 1 ist in der niedrigeren Liga (= höhere Liga Nummer)
						guest = match.getTeam(0).getPlayers().get(0);
						home = match.getTeam(1).getPlayers().get(0);
					}
					else
					{
						// Spieler 0 und 1 sind in der gleichen Liga
						if(random.nextBoolean())
						{
							home = match.getTeam(0).getPlayers().get(0);
							guest = match.getTeam(1).getPlayers().get(0);
						}
						else
						{
							guest = match.getTeam(0).getPlayers().get(0);
							home = match.getTeam(1).getPlayers().get(0);
						}
					}

					map = home.getHomeMap();

					gamePlayers = new LinkedList<Player>();
					gamePlayers.add(creator);
					gamePlayers.add(home);
					gamePlayers.add(guest);

					// TODO separate
					increasePlannedGames(gamePlayers);
					tmpRules = rules.clone().createRandomValues();
					name = PlaceholderFactory.applyPlaceholders(karopapier, title, map, gamePlayers, tmpRules, count, day, dayCount, match.getTeams(), round, g);
					game = new Game(name, map, gamePlayers, tmpRules);

					games.add(game);
					count++;
					dayCount++;
				}
				day++;
			}
		}

		return games;
	}

	public List<PlannedGame> planKLCKOPhase(String title, int round)
	{
		List<PlannedGame> games = new LinkedList<>();

		// KO-Phase
		Collections.shuffle(getPlayersRoundOfX(round), random);

		int count = 1;
		Game game;
		String name;
		Map map;
		Player home, guest;
		List<Player> gamePlayers;
		Rules tmpRules;
		Team tmpTeamHome, tmpTeamGuest;
		for(int i = 0; i < round; i = i + 2)
		{
			if(getPlayersRoundOfX(round).get(i).getLeague() > getPlayersRoundOfX(round).get(i + 1).getLeague())
			{
				// Spieler i ist in der niedrigeren Liga (= höhere Liga Nummer)
				home = getPlayersRoundOfX(round).get(i);
				guest = getPlayersRoundOfX(round).get(i + 1);
			}
			else if(getPlayersRoundOfX(round).get(i).getLeague() < getPlayersRoundOfX(round).get(i + 1).getLeague())
			{
				// Spieler i+1 ist in der niedrigeren Liga (= höhere Liga Nummer)
				guest = getPlayersRoundOfX(round).get(i);
				home = getPlayersRoundOfX(round).get(i + 1);
			}
			else
			{
				// Spieler i und i+1 sind in der gleichen Liga
				if(random.nextBoolean())
				{
					home = getPlayersRoundOfX(round).get(i);
					guest = getPlayersRoundOfX(round).get(i + 1);
				}
				else
				{
					guest = getPlayersRoundOfX(round).get(i);
					home = getPlayersRoundOfX(round).get(i + 1);
				}
			}

			map = home.getHomeMap();

			gamePlayers = new LinkedList<Player>();
			gamePlayers.add(creator);
			gamePlayers.add(home);
			gamePlayers.add(guest);

			increasePlannedGames(gamePlayers);

			tmpRules = rules.clone().createRandomValues();
			tmpTeamHome = new Team(home.getName(), Arrays.asList(home));
			tmpTeamGuest = new Team(guest.getName(), Arrays.asList(guest));
			name = PlaceholderFactory.applyPlaceholders(karopapier, title, map, gamePlayers, tmpRules, count, -1, -1, new Team[] { tmpTeamHome, tmpTeamGuest }, round, -1);

			game = new Game(name, map, gamePlayers, tmpRules);

			games.add(game);
			count++;
		}

		return games;
	}

	public List<PlannedGame> planKO(String title)
	{
		List<PlannedGame> games = new LinkedList<>();

		return games;
	}

	public List<PlannedGame> planLeague(String title)
	{
		List<PlannedGame> games = new LinkedList<>();

		return games;
	}

	public List<PlannedGame> planSimple(String title)
	{
		List<PlannedGame> games = new LinkedList<>();

		return games;
	}

	public static List<List<Match>> planLeague(List<Team> teams)
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

	public static User[][][] shufflePlayers(List<User> players, List<Rules> rules)
	{
		List<User> tmp = new LinkedList<User>(players);
		Collections.shuffle(tmp);
		// shuffeln kann manchmal fehlschlagen
		// (weil sonst ein spieler doppelt in einem Rennen sein müsste)
		// --> dann versuch es einfach nochmal...
		while(true)
		{
			try
			{
				ShuffleResult result = shufflePlayers0(tmp, rules);
				printWhoOnWho(result, false);
				return result.shuffledUsers;
			}
			catch(IllegalArgumentException e)
			{
				continue;
			}
		}
	}

	protected static ShuffleResult shufflePlayers0(List<User> players, List<Rules> rules)
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
					// (Spaltensumme nur über die Zeilen, cols-Liste ausschließen)
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

			// letztes Rennen auffüllen... (falls nicht voll)
			for(; g < games && p < rules.get(r).getNumberOfPlayers(); p++)
			{
				result.shuffledUsers[r][g][p] = null;
			}
		}

		return result;
	}

	protected static void printWhoOnWho(ShuffleResult result, boolean printDetails)
	{
		// print totalWhoOnWho
		for(int pl1 = 0; pl1 < result.totalWhoOnWho.length; pl1++)
		{
			for(int pl2 = 0; pl2 < result.totalWhoOnWho[pl1].length; pl2++)
			{
				System.out.print(toString(result.totalWhoOnWho[pl1][pl2], 2) + " ");
			}
			System.out.println("");
		}

		// print whoOnWhos
		if(printDetails)
		{
			for(int m = 0; m < result.whoOnWho.length; m++)
			{
				for(int pl1 = 0; pl1 < result.whoOnWho[m].length; pl1++)
				{
					for(int pl2 = 0; pl2 < result.whoOnWho[m][pl1].length; pl2++)
					{
						System.out.print(toString(result.whoOnWho[m][pl1][pl2], 2) + " ");
					}
					System.out.println("");
				}
				System.out.println("");
			}
		}
	}

	private static class ShuffleResult
	{
		private User[][][]	shuffledUsers;
		private int[][][]	whoOnWho;
		private int[][]		totalWhoOnWho;

		private ShuffleResult(int numberOfUsers, int numberOfRounds)
		{
			this.shuffledUsers = new User[numberOfRounds][][];
			this.totalWhoOnWho = new int[numberOfUsers][numberOfUsers];
			this.whoOnWho = new int[numberOfRounds][numberOfUsers][numberOfUsers];
		}
	}

	// HELPERS

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
