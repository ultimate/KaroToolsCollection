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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	/**
	 * Logger-Instance
	 */
	private static final Logger	logger	= LoggerFactory.getLogger(Planner.class);
	private static Random		random	= new Random();

	private KaroAPICache		karoAPICache;

	public Planner(KaroAPICache karoAPICache)
	{
		this.karoAPICache = karoAPICache;
	}

	public List<PlannedGame> planSeries(GameSeries gs)
	{
		if(gs == null || gs.getType() == null)
			throw new IllegalArgumentException("gameseries & type must not be null!");

		int numberOfGamesPerPair, numberOfTeamsPerMatch, round, groups, leagues, numberOfGames, maxPlayersPerGame;
		boolean useHomeMaps;

		switch(gs.getType())
		{
			case AllCombinations:
				numberOfGamesPerPair = GameSeriesManager.get(gs, "numberOfGamesPerPair");
				numberOfTeamsPerMatch = GameSeriesManager.get(gs, "numberOfTeamsPerMatch");
				return planSeriesAllCombinations(gs.getTitle(), gs.getTeams(), gs.getMaps(), gs.getRules(), numberOfGamesPerPair, numberOfTeamsPerMatch);
			case Balanced:
				return planSeriesBalanced(gs.getTitle(), null, null, null);
			case KLC:
				round = GameSeriesManager.get(gs, "round");
				groups = GameSeriesManager.get(gs, "groups");
				leagues = GameSeriesManager.get(gs, "leagues");
				return planSeriesKLC(gs.getTitle(), gs.getPlayersByKey(), gs.getMapsByKey(), leagues, groups, gs.getRules(), round);
			case KO:
				return planSeriesKO(gs.getTitle(), gs.getTeams(), null, gs.getRules(), true);
			case League:
				numberOfGamesPerPair = GameSeriesManager.get(gs, "numberOfGamesPerPair");
				useHomeMaps = GameSeriesManager.get(gs, "useHomeMaps");
				return planSeriesLeague(gs.getTitle(), gs.getTeams(), gs.getMaps(), gs.getRules(), useHomeMaps, numberOfGamesPerPair);
			case Simple:
				numberOfGames = GameSeriesManager.get(gs, "numberOfGames");
				maxPlayersPerGame = GameSeriesManager.get(gs, "maxPlayersPerGame");
				return planSeriesSimple(gs.getTitle(), gs.getPlayers(), gs.getMaps(), gs.getRules(), numberOfGames, maxPlayersPerGame);
			default:
				return null;
		}
	}

	public List<PlannedGame> planSeriesAllCombinations(String title, List<Team> teams, List<Map> maps, Rules rules, int numberOfGamesPerPair, int numberOfTeamsPerMatch)
	{
		List<PlannedGame> games = new LinkedList<>();

		// create local copy of the input list
		List<Team> tmp = new LinkedList<>(teams);
		Collections.shuffle(tmp, random);

		List<Match> matches = leagueSpecial(tmp, numberOfTeamsPerMatch);

		PlannedGame game;
		Map map;
		List<User> gamePlayers;
		int count = 0;
		int dayCount;
		HashMap<String, String> placeholderValues = new HashMap<>();

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

	public List<PlannedGame> planSeriesBalanced(String title, List<User> players, java.util.Map<Integer, List<Map>> gameDayMaps, java.util.Map<Integer, Rules> gameDayRules)
	{
		if(!checkKeys(gameDayMaps, gameDayRules))
			throw new IllegalArgumentException("gameDayMaps & gameDayRules must have equals size and keys");

		List<PlannedGame> games = new LinkedList<>();

		PlannedGame game;
		List<User> gamePlayers;
		Map map;
		int count = 0;
		int dayCount;
		HashMap<String, String> placeholderValues = new HashMap<>();

		User[][][] shuffledPlayers = shufflePlayers(players, new ArrayList<>(gameDayRules.values()));

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

				map = gameDayMaps.get(day).get(random.nextInt(gameDayMaps.get(day).size()));

				placeholderValues.put("i", toString(count + 1, 1));
				placeholderValues.put("spieltag", toString(day + 1, 1));
				placeholderValues.put("spieltag.i", toString(dayCount + 1, 1));

				game = planGame(title, map, gamePlayers, gameDayRules.get(day), placeholderValues);

				games.add(game);
				count++;
				dayCount++;
			}

			int gamesAfter = games.size();
			logger.info("map #" + day + " games planned:   " + (gamesAfter - gamesBefore));
		}
		return games;
	}

	// note: lists are modified
	public List<PlannedGame> planSeriesKLC(String title, java.util.Map<String, List<User>> playersByKey, java.util.Map<String, List<Map>> homeMaps, int leagues, int groups, Rules rules, int round)
	{
		int totalPlayers = groups * leagues;

		BiFunction<Team, Team, Team> whoIsHome = (team1, team2) -> {
			int league1 = -1;
			int league2 = -1;
			for(int l = 1; l <= leagues; l++)
			{
				if(playersByKey.get("league_" + l).contains(team1.getMembers().get(0)))
					league1 = l;
				if(playersByKey.get("league_" + l).contains(team2.getMembers().get(0)))
					league2 = l;
			}
			if(league1 == -1 || league2 == -1)
				logger.error("should not happen!");

			if(league1 > league2) // Spieler 0 ist in der niedrigeren Liga (= höhere Liga Nummer)
				return team1;
			else if(league1 < league2) // Spieler 1 ist in der niedrigeren Liga (= höhere Liga Nummer)
				return team2;
			return (random.nextBoolean() ? team1 : team2); // beide sind in der gleichen Liga -> zufall
		};

		if(round == totalPlayers)
			return planGroupphase(title, playersByKey, homeMaps, leagues, groups, whoIsHome, rules, round);
		else
		{
			// create tmp list of teams so we can use planTeamGame
			List<Team> teams = new ArrayList<>(playersByKey.get("round_" + round).size());
			for(User user : playersByKey.get("round_" + round))
				teams.add(new Team(user.getLogin(), Arrays.asList(user), homeMaps.get("" + user.getId()).get(0)));

			return planSeriesKO(title, teams, whoIsHome, rules, true);
		}
	}

	// original listen werden gemischt
	private List<PlannedGame> planGroupphase(String title, java.util.Map<String, List<User>> playersByKey, java.util.Map<String, List<Map>> homeMaps, int leagues, int groups,
			BiFunction<Team, Team, Team> whoIsHome, Rules rules, int round)
	{
		List<PlannedGame> games = new LinkedList<>();

		// Liegen durchmischen
		for(int l = 1; l <= leagues; l++)
			Collections.shuffle(playersByKey.get("league_" + l));

		// Gruppenphase
		for(int g = 1; g <= groups; g++)
		{
			// Bilde Gruppe aus je 1 Spieler pro Liga
			// die Ligen sind 1mal initial durchgemischt, deshalb können wir einfach für Gruppe
			// X jeweils den X-ten Spieler pro Gruppe nehmen
			playersByKey.get("group_" + g).clear();
			for(int l = 1; l <= leagues; l++)
				playersByKey.get("group_" + g).add(playersByKey.get("league_" + l).get(g-1));
			Collections.shuffle(playersByKey.get("group_" + g), random);

			// create a temporarily list of single player teams to be able to use the LeaguePlanner
			List<Team> teamsTmp = new ArrayList<Team>(playersByKey.get("group_" + g).size());
			for(User p : playersByKey.get("group_" + g))
				teamsTmp.add(new Team(p.getLogin(), Arrays.asList(p)));

			List<List<Match>> matches = planLeague(teamsTmp);

			int day = 0;
			PlannedGame game;
			int count = 0;
			int dayCount;
			HashMap<String, String> placeholderValues = new HashMap<>();
			for(List<Match> matchesForDay : matches)
			{
				dayCount = 0;
				for(Match match : matchesForDay)
				{
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

	private PlannedGame planTeamGame(String title, Team team1, Team team2, BiFunction<Team, Team, Team> whoIsHome, Map overwriteMap, Rules rules, java.util.Map<String, String> placeholderValues)
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

	public List<PlannedGame> planSeriesKO(String title, List<Team> teams, BiFunction<Team, Team, Team> whoIsHome, Rules rules, boolean shuffle)
	{
		List<PlannedGame> games = new LinkedList<>();

		// create local copy of the input list
		List<Team> tmp = new ArrayList<>(teams);
		if(shuffle)
			Collections.shuffle(tmp, random);

		int count = 1;
		PlannedGame game;
		Team ti, ti1;
		HashMap<String, String> placeholderValues = new HashMap<>();
		
		for(int i = 0; i < tmp.size(); i = i + 2)
		{
			ti = tmp.get(i);
			ti1 = tmp.get(i + 1);

			placeholderValues.put("i", toString(count + 1, 1));
			// placeholderValues.put("spieltag", toPlaceholderString(day + 1, 1));
			// placeholderValues.put("spieltag.i", toPlaceholderString(dayCount + 1, 1));
			placeholderValues.put("runde", toPlaceholderString(tmp.size(), -1, -1, -1));
			placeholderValues.put("runde.x", toPlaceholderString(tmp.size(), -1, -1, count));

			game = planTeamGame(title, ti, ti1, whoIsHome, null, rules, placeholderValues);

			games.add(game);
			count++;
		}

		return games;
	}

	public List<PlannedGame> planSeriesLeague(String title, List<Team> teams, List<Map> maps, Rules rules, boolean useHomeMaps, int numberOfGamesPerPair)
	{
		List<PlannedGame> games = new LinkedList<>();

		// create local copy of the input list
		List<Team> tmp = new ArrayList<>(teams);
		Collections.shuffle(tmp, random);

		List<List<Match>> matches = planLeague(tmp);

		int day = 0;
		PlannedGame game;
		Map overwriteMap;
		int count = 0;
		int dayCount;
		HashMap<String, String> placeholderValues = new HashMap<>();
		
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

	public List<PlannedGame> planSeriesSimple(String title, List<User> players, List<Map> maps, Rules rules, int numberOfGames, int maxPlayersPerGame)
	{
		List<PlannedGame> games = new LinkedList<>();

		PlannedGame game;
		List<User> gamePlayers;
		List<User> allPlayers;
		User player;
		Map map;
		int count = 0;
		HashMap<String, String> placeholderValues = new HashMap<>();
		
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

			placeholderValues.put("i", toString(count + 1, 1));
			
			game = planGame(title, map, gamePlayers, rules, placeholderValues);
			
			games.add(game);
			count++;
		}
		
		return games;
	}

	public PlannedGame planGame(String title, Map map, List<User> gamePlayers, Rules rules, java.util.Map<String, String> placeholderValues)
	{
		return planGame(title, map, gamePlayers, rules.createOptions(), placeholderValues);
	}

	public PlannedGame planGame(String title, Map map, List<User> gamePlayers, Options options, java.util.Map<String, String> placeholderValues)
	{
		increasePlannedGames(gamePlayers);

		// add default placeholder values
		java.util.Map<String, String> defaultPlaceholderValues = getDefaultPlaceholderValues(map, gamePlayers, options);
		for(Entry<String, String> pv : defaultPlaceholderValues.entrySet())
			placeholderValues.putIfAbsent(pv.getKey(), pv.getValue());

		String name = applyPlaceholders(title, placeholderValues);

		return new PlannedGame(name, map.getId(), toIntArray(gamePlayers), options);
	}

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
		defaultPlaceholderValues.put("regeln.tc", Language.getString("titlepatterns.tc." + options.getCrashallowed()));
		defaultPlaceholderValues.put("regeln.cps", Language.getString("titlepatterns.cps." + options.isCps()));
		defaultPlaceholderValues.put("regeln.richtung", Language.getString("titlepatterns.direction") + options.getStartdirection());

		return defaultPlaceholderValues;
	}

	private static List<List<Match>> planLeague(List<Team> teams)
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

	private static List<Match> leagueSpecial(List<Team> teams, int numberOfTeamsPerMatch)
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

	private static User[][][] shufflePlayers(List<User> players, List<Rules> rules)
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

	private static ShuffleResult shufflePlayers0(List<User> players, List<Rules> rules)
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

	public static void resetPlannedGames(List<User> users)
	{
		for(User user : users)
			user.setPlannedGames(0);
	}

	public static void increasePlannedGames(List<User> users)
	{
		for(User user : users)
			user.setPlannedGames(user.getPlannedGames() + 1);
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

	public static String applyPlaceholders(String title, java.util.Map<String, String> placeholderValues)
	{
		// preparation
		if(placeholderValues.containsKey("i"))
		{
			placeholderValues.putIfAbsent("ii", "0" + placeholderValues.get("i"));
			placeholderValues.putIfAbsent("iii", "00" + placeholderValues.get("i"));
			placeholderValues.putIfAbsent("iiii", "000" + placeholderValues.get("i"));
		}

		String name = title;
		for(Entry<String, String> pv : placeholderValues.entrySet())
			name = name.replace("${" + pv.getKey() + "}", pv.getValue());

		name = name.replace("  ", " ");
		name = name.trim();

		if(name.endsWith(","))
			name = name.substring(0, name.length() - 1);

		return name;
	}

	private static String toString(int x, int minDigits)
	{
		String s = "" + x;
		while(s.length() < minDigits)
			s = "0" + s;
		return s;
	}

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
			tmp.append(Language.getString("titlepatterns.tc." + options.getCrashallowed()));
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
			tmp.append(options.getStartdirection());
		}
		return tmp.toString();
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
