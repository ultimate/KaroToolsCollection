package ultimate.karopapier.eval;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.enums.EnumPlayerStatus;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karopapier.eval.CCCEval.UserStats;

public class CCCEvalNew extends CCCEval
{
	public CCCEvalNew(int cccx)
	{
		super(cccx);
	}
	
	@Override
	protected Object[] createRow(int c, int g, Game game, Player player, int position)
	{
		// TODO Auto-generated method stub
		return super.createRow(c, g, game, player, position);
	}

	@Override
	protected void calcMetrics(int c)
	{
		// TODO Auto-generated method stub
		super.calcMetrics(c);
	}

	@Override
	protected void calcMetrics(int c, int g)
	{
		// TODO Auto-generated method stub
		super.calcMetrics(c, g);
	}

	@Override
	protected double calcBasicPoints(int players, EnumPlayerStatus status, int rank, int moves, int crashs)
	{
		// TODO Auto-generated method stub
		return super.calcBasicPoints(players, status, rank, moves, crashs);
	}

	@Override
	protected double calcGamePoints(Game game, Player player, Object[] row)
	{
		// TODO Auto-generated method stub
		return super.calcGamePoints(game, player, row);
	}

	@Override
	protected void addBonus(int c, UserStats mins, UserStats maxs)
	{
		// TODO Auto-generated method stub
		super.addBonus(c, mins, maxs);
	}

	@Override
	protected void addFinalBonus(UserStats mins, UserStats maxs)
	{
		// TODO Auto-generated method stub
		super.addFinalBonus(mins, maxs);
	}

	@Override
	protected void calculateExpected()
	{
		// TODO Auto-generated method stub
		super.calculateExpected();
	}

	@SuppressWarnings("unchecked")
	private boolean createTables()
	{
		String[][] totalTable;
		String[] totalTableHead;
		String player;
		List<String> challengePlayers = getChallengePlayersFromLog(1);
		stats_players = challengePlayers.size();

		Map<String, Double> sum_pointsUnskaled;
		Map<String, Double> sum_basePoints;
		Map<String, Integer> sum_crashs;
		Map<String, Integer> sum_moves;
		Map<String, Double> total_sum_basePoints = new TreeMapX<String, Double>(challengePlayers, 0.0);
		Map<String, Integer> total_sum_crashs = new TreeMapX<String, Integer>(challengePlayers, 0);
		Map<String, Integer> total_sum_moves = new TreeMapX<String, Integer>(challengePlayers, 0);
		Map<String, Double> total_sum_pointsSkaled = new TreeMapX<String, Double>(challengePlayers, 0.0);
		Map<String, Integer> total_sum_races = new TreeMapX<String, Integer>(challengePlayers, 0);
		Map<String, Integer> total_sum_bonus = new TreeMapX<String, Integer>(challengePlayers, 0);
		Map<String, Integer> final_sum_bonus = new TreeMapX<String, Integer>(challengePlayers, 0);
		Map<String, Integer[]> total_sum_races_bychallenge = new TreeMapXArray<String, Integer>(challengePlayers, new Integer[tables.length], 0);

		real_points = new TreeMapX2<String, Double>(challengePlayers);
		real_crashs = new TreeMapX2<String, Integer>(challengePlayers);
		real_crashs_allraces = new TreeMapX2<String, Integer>(challengePlayers);
		pointsForMoves = (Map<Integer, Integer>[]) new Map<?, ?>[pages.length + 1];

		initWhoOnWho(challengePlayers);

		finalTableHead = new String[pages.length + 11];
		finalTableHead[0] = "Platz";
		finalTableHead[1] = "Spieler";
		finalTableHead[pages.length + 1] = "Grundpunkte (gesamt)";
		finalTableHead[pages.length + 2] = "Crashs (gesamt)";
		finalTableHead[pages.length + 3] = "Z�ge (gesamt)";
		finalTableHead[pages.length + 4] = "Skalierte Punkte (gesamt)";
		finalTableHead[pages.length + 5] = "Challenge-Bonus (gesamt)";
		finalTableHead[pages.length + 6] = "Bonus (Gesamtwertung)";
		finalTableHead[pages.length + 7] = "Endergebnis";
		finalTableHead[pages.length + 8] = "Abgeschlossene Rennen";
		finalTableHead[pages.length + 9] = "Erwartungswert";
		finalTableHead[pages.length + 10] = "Erwartungswert (alt)";
		finalTable = new String[challengePlayers.size()][finalTableHead.length];

		for(int c = 1; c < pages.length; c++)
		{
			totalTableHead = new String[pages[c].length + 6];
			totalTableHead[0] = "Spieler";
			totalTableHead[pages[c].length + 0] = "Grundpunkte (gesamt)";
			totalTableHead[pages[c].length + 1] = "Crashs (gesamt)";
			totalTableHead[pages[c].length + 2] = "Z�ge (gesamt)";
			totalTableHead[pages[c].length + 3] = "Gesamtpunkte (unskaliert)";
			totalTableHead[pages[c].length + 4] = "Gesamtpunkte (skaliert)";
			totalTableHead[pages[c].length + 5] = "Challenge-Bonus";

			finalTableHead[c + 1] = challengeToLink(c, false);

			totalTable = new String[challengePlayers.size()][totalTableHead.length];

			sum_pointsUnskaled = new TreeMapX<String, Double>(challengePlayers, 0.0);
			sum_basePoints = new TreeMapX<String, Double>(challengePlayers, 0.0);
			sum_crashs = new TreeMapX<String, Integer>(challengePlayers, 0);
			sum_moves = new TreeMapX<String, Integer>(challengePlayers, 0);

			for(String p : challengePlayers)
			{
				totalTable[challengePlayers.indexOf(p)][0] = playerToLink(p, true);
				for(int i = 1; i < totalTable[challengePlayers.indexOf(p)].length; i++)
				{
					totalTable[challengePlayers.indexOf(p)][i] = "-";
				}
			}

			final Map<?, ?>[] playerMoves = new Map<?, ?>[pages[c].length + 1];
			final Map<?, ?>[] playerCrashs = new Map<?, ?>[pages[c].length + 1];
			final Map<?, ?>[] playerPosition = new Map<?, ?>[pages[c].length + 1];
			final Map<?, ?>[] playerFinished = new Map<?, ?>[pages[c].length + 1];

			// create table Part 1
			for(int r = 1; r < pages[c].length; r++)
			{
				stats_races++;

				totalTableHead[r] = raceToLink(c, r);

				playerMoves[r] = new HashMap<String, Integer>();
				playerCrashs[r] = new HashMap<String, Integer>();
				playerPosition[r] = new HashMap<String, Integer>();
				playerFinished[r] = new HashMap<String, Boolean>();

				try
				{
					tables[c][r] = createRaceTableFromLog_Part1(c, r, (Map<String, Integer>) playerMoves[r], (Map<String, Integer>) playerCrashs[r], (Map<String, Integer>) playerPosition[r],
							(Map<String, Boolean>) playerFinished[r]);
				}
				catch(Exception e)
				{
					logger.info("----------------------------------------------");
					logger.info("----------------------------------------------");
					logger.info("----------------------------------------------");
					logger.info("ERROR for race " + c + "." + r);
					logger.info(getLog(c, r));
					logger.info("----------------------------------------------");
					logger.info("----------------------------------------------");
					logger.info("----------------------------------------------");
					throw new RuntimeException(e);
				}
			}

			// determine points for moves
			pointsForMoves[c] = new TreeMap<Integer, Integer>();
			int minMoves = Integer.MAX_VALUE;
			int maxMoves = Integer.MIN_VALUE;
			int totalMoves = 0;
			int divisor = 0;
			int avgMoves;

			// calculate min and avg moves
			// only consider finished players (if there are some)
			boolean someoneFinished = false;
			for(int r = 1; r < pages[c].length; r++)
			{
				for(Boolean finished : ((Map<String, Boolean>) playerFinished[r]).values())
				{
					if(finished)
					{
						someoneFinished = true;
						break;
					}
				}
				if(someoneFinished)
					break;
			}
			for(int r = 1; r < pages[c].length; r++)
			{
				for(Entry<String, Integer> e : ((Map<String, Integer>) playerMoves[r]).entrySet())
				{
					// only use finished players for minMoves
					if(((Map<String, Boolean>) playerFinished[r]).get(e.getKey()) && e.getValue() < minMoves)
						minMoves = e.getValue();
					// use all players for maxMoves
					if(e.getValue() > maxMoves)
						maxMoves = e.getValue();
					totalMoves += e.getValue();
					divisor++;
				}
			}
			if(someoneFinished)
			{
				// calculate avg if there are finished players
				avgMoves = totalMoves / divisor;
			}
			else
			{
				// if no one is finished the true min and avg will still increase
				// hence use the current max for that
				minMoves = maxMoves;
				avgMoves = maxMoves;
			}

			// assign points for moves
			int points = (avgMoves - minMoves) * 2;
			pointsForMoves[c].put(minMoves, points + 1);
			points--;
			for(int moves = minMoves + 1; moves <= maxMoves; moves++)
			{
				pointsForMoves[c].put(moves, points);
				if(points > 0)
					points--;
			}

			// create table Part 2
			for(int r = 1; r < pages[c].length; r++)
			{
				try
				{
					createRaceTableFromLog_Part2(tables[c][r], c, r, totalTable, challengePlayers, sum_pointsUnskaled, sum_basePoints, sum_crashs, sum_moves, total_sum_basePoints, total_sum_crashs,
							total_sum_moves, total_sum_pointsSkaled, total_sum_races, total_sum_bonus, total_sum_races_bychallenge, (Map<String, Integer>) playerMoves[r],
							(Map<String, Integer>) playerCrashs[r], (Map<String, Integer>) playerPosition[r], (Map<String, Boolean>) playerFinished[r], pointsForMoves[c]);
				}
				catch(Exception e)
				{
					logger.info("----------------------------------------------");
					logger.info("----------------------------------------------");
					logger.info("----------------------------------------------");
					logger.info("ERROR for race " + c + "." + r);
					logger.info(getLog(c, r));
					logger.info("----------------------------------------------");
					logger.info("----------------------------------------------");
					logger.info("----------------------------------------------");
					throw new RuntimeException(e);
				}
				createWhoOnWho(c, r, challengePlayers);
			}

			double maxPointsUnskaled = addBonus(c, challengePlayers, totalTable, sum_pointsUnskaled, sum_basePoints, sum_crashs, sum_moves, total_sum_bonus);
			if(maxPointsUnskaled == 0.0)
				maxPointsUnskaled = 100;

			double skaledPoints;
			for(int i = 0; i < challengePlayers.size(); i++)
			{
				if(!totalTable[i][pages[c].length + 3].equals("?") && !totalTable[i][pages[c].length + 3].equals("-"))
				{
					skaledPoints = round(doubleFromString(totalTable[i][pages[c].length + 3]) * 100 / maxPointsUnskaled);
					totalTable[i][pages[c].length + 4] = "" + skaledPoints;
					if(totalTable[i][pages[c].length + 4].equals("100.0"))
					{
						totalTable[i][pages[c].length + 3] = highlight(totalTable[i][pages[c].length + 3]);
						totalTable[i][pages[c].length + 4] = highlight(totalTable[i][pages[c].length + 4]);
					}
					total_sum_pointsSkaled.put(challengePlayers.get(i), total_sum_pointsSkaled.get(challengePlayers.get(i)) + skaledPoints);
				}
			}

			totalTableHeads[c] = totalTableHead;
			totalTables[c] = totalTable;
		}

		stats_racesPerPlayerPerChallenge =

				intFromString(p.getProperty("races.per.player.per.challenge"));
		stats_racesPerPlayer = stats_racesPerPlayerPerChallenge * (pages.length - 1);

		calculateExpected(challengePlayers);

		double expected_bonus, expected_old, total;
		String tmp, tmp2;
		boolean finished = true;
		for(int i = 0; i < challengePlayers.size(); i++)
		{
			player = challengePlayers.get(i);
			finalTable[i][1] = playerToLink(player, true);
			for(int c = 1; c < tables.length; c++)
			{
				tmp = totalTables[c][i][totalTables[c][i].length - 2];
				// tmp2 = "&nbsp;<span style=\"font-size:50%\">(" +
				// total_sum_races_bychallenge.get(player)[c] + "/" +
				// stats_racesPerPlayerPerChallenge + ")</span>";
				if(stats_racesPerPlayerPerChallenge - total_sum_races_bychallenge.get(player)[c] > 0)
				{
					tmp2 = "&nbsp;<span style=\"font-size:50%\">(" + (stats_racesPerPlayerPerChallenge - total_sum_races_bychallenge.get(player)[c]) + ")</span>";
					finished = false;
				}
				else
					tmp2 = "";
				if(tmp.equals("100.0"))
					finalTable[i][c + 1] = highlight(tmp) + tmp2;
				else
					finalTable[i][c + 1] = tmp + tmp2;
			}
			finalTable[i][tables.length + 1] = "" + round(total_sum_basePoints.get(player));
			finalTable[i][tables.length + 2] = "" + total_sum_crashs.get(player);
			stats_crashs += total_sum_crashs.get(player);
			finalTable[i][tables.length + 3] = "" + total_sum_moves.get(player);
			stats_moves += total_sum_moves.get(player);
			finalTable[i][tables.length + 4] = "" + round(total_sum_pointsSkaled.get(player));
			finalTable[i][tables.length + 5] = "+" + total_sum_bonus.get(player);
			finalTable[i][tables.length + 6] = "-";
			finalTable[i][tables.length + 8] = "" + total_sum_races.get(player);
		}
		addFinalBonus(challengePlayers, finalTable, total_sum_basePoints, total_sum_crashs, total_sum_moves, final_sum_bonus);
		for(int i = 0; i < challengePlayers.size(); i++)
		{
			player = challengePlayers.get(i);
			total = round(total_sum_pointsSkaled.get(player) + total_sum_bonus.get(player) + final_sum_bonus.get(player));

			expected_old = round(total_sum_pointsSkaled.get(player) / total_sum_races.get(player) * stats_racesPerPlayer);
			expected_bonus = total_sum_bonus.get(player) + final_sum_bonus.get(player);

			finalTable[i][tables.length + 7] = highlight("" + total);
			finalTable[i][tables.length + 9] = highlight("" + round(round(expected_total_points.get(player) + expected_bonus)));
			finalTable[i][tables.length + 10] = ("" + round(round(expected_old + expected_bonus)));
		}

		logger.info("finished=" + finished);

		sortFinalTable(finished);

		for(int i = 0; i < finalTable.length; i++)
		{
			if(i > 0 && finalTable[i][finalTable[i].length - 1].equals(finalTable[i - 1][finalTable[i].length - 1])
					&& finalTable[i][finalTable[i].length - 7].equals(finalTable[i - 1][finalTable[i].length - 7])
					&& finalTable[i][finalTable[i].length - 8].equals(finalTable[i - 1][finalTable[i].length - 8])
					&& finalTable[i][finalTable[i].length - 6].equals(finalTable[i - 1][finalTable[i].length - 6]))
			{
				finalTable[i][0] = " ";
			}
			else
			{
				finalTable[i][0] = (i + 1) + ".";
			}
		}

		return finished;
	}

	private String[][] createRaceTableFromLog_Part1(int c, int r, Map<String, Integer> playerMoves, Map<String, Integer> playerCrashs, final Map<String, Integer> playerPosition,
			Map<String, Boolean> playerFinished)
	{
		String[][] table = new String[numberOfPlayers[c]][raceTableHead.length];
		int last = numberOfPlayers[c];
		int lastRankWithPoints = last;
		while(intFromString(p.getProperty("points." + numberOfPlayers[c] + "." + lastRankWithPoints)) == 0)
			lastRankWithPoints--;

		int moves;
		int crashs;
		int position;
		boolean finished;
		int crashsAfterLastRankWithoutPointsThrown;
		String log = getLog(c, r);

		List<String> players = getChallengePlayersFromLog(c, r);

		int maxMoves = 0;
		for(String player : players)
		{
			moves = countOccurrences(log, player + " -> ");
			if(moves > maxMoves)
				maxMoves = moves;
		}

		int thrown = 0;
		List<Integer> thrownIndexes = new ArrayList<Integer>(numberOfPlayers[c]);
		int lastRankWithoutPointsThrownIndex = -1;
		for(String player : players)
		{
			position = log.lastIndexOf(player);
			if(position == -1)
			{
			}
			else if(log.lastIndexOf(player + " wird von Didi aus dem Spiel geworfen") == position || log.lastIndexOf(player + " wird von KaroMAMA aus dem Spiel geworfen") == position
					|| log.lastIndexOf(player + " steigt aus dem Spiel aus") == position)
			{
				thrown++;
				thrownIndexes.add(position);
			}
		}
		last = last - thrown;
		if((last >= lastRankWithPoints) && (thrownIndexes.size() >= (numberOfPlayers[c] - lastRankWithPoints)))
		{
			// mehr oder genauso viele rausschmisse, wie 0-Raenge
			Integer[] indexes = thrownIndexes.toArray(new Integer[thrownIndexes.size()]);
			Arrays.sort(indexes);
			lastRankWithoutPointsThrownIndex = indexes[numberOfPlayers[c] - lastRankWithPoints - 1];
		}
		if(lastRankWithoutPointsThrownIndex > 0)
			logger.info("  Challenge " + c + "." + r);

		for(String player : players)
		{
			moves = countOccurrences(log, player + " -> ");
			if(moves < 0)
				moves = 0;
			crashs = countOccurrences(log, player + " CRASHT!!!");
			crashsAfterLastRankWithoutPointsThrown = countOccurrences(log, player + " CRASHT!!!", lastRankWithoutPointsThrownIndex);

			if(gidProperties.containsKey(c + "." + r + "." + player))
			{
				logger.info("    correcting crash count for " + player + " @ " + c + "." + r);
				try
				{
					crashs += intFromString(gidProperties.getProperty(c + "." + r + "." + player));
					logger.info(" >> OK");
				}
				catch(NumberFormatException e)
				{
					logger.info(" >> ERROR: " + e.getMessage());
				}
			}

			// get Position anyway + fix moves
			position = log.lastIndexOf(player);
			if(position == -1)
			{
				position = last--;
				finished = false;
			}
			else if(log.lastIndexOf(player + " wird von Didi aus dem Spiel geworfen") == position || log.lastIndexOf(player + " wird von KaroMAMA aus dem Spiel geworfen") == position
					|| log.lastIndexOf(player + " steigt aus dem Spiel aus") == position)
			{
				position = players.size();
				moves = maxMoves + 1;
				finished = false;
			}
			else if(log.lastIndexOf(player + " CRASHT!!!") == position && log.indexOf(player + " steigt aus dem Spiel aus") >= 0 && log.indexOf(player + " steigt aus dem Spiel aus") < position)
			{
				// Ausstiegs-Bug abfangen: Nach dem Ausstieg wurde gecrasht...
				// dann ist in der Zeile vor dem CRASH der Ausstieg
				position = players.size();
				moves = maxMoves + 1;
				finished = false;
			}
			else if(log.lastIndexOf(player + " wird") == position)
			{
				position = position + (player + " wird ").length();
				position = intFromString(log.substring(position, log.indexOf(".", position + 1)));
				if((lastRankWithoutPointsThrownIndex > 0) && (position == lastRankWithPoints) && (crashsAfterLastRankWithoutPointsThrown > 0))
					logger.info("    " + player + ": " + crashsAfterLastRankWithoutPointsThrown + " (" + crashs + ")");
				finished = true;
			}
			else
			{
				position = last--;
				finished = false;
			}

			// store in maps for later use
			playerFinished.put(player, finished);
			playerMoves.put(player, moves);
			playerCrashs.put(player, crashs);
			playerPosition.put(player, position);
		}

		// sort players by position
		// logger.info("before: " + players);
		players.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2)
			{
				return playerPosition.get(o1) - playerPosition.get(o2);
			}
		});
		// logger.info("after: " + players);

		return table;
	}

	private void createRaceTableFromLog_Part2(String[][] table, int c, int r, String[][] totalTable, List<String> challengePlayers, Map<String, Double> sum_pointsUnskaled,
			Map<String, Double> sum_basePoints, Map<String, Integer> sum_crashs, Map<String, Integer> sum_moves, Map<String, Double> total_sum_basePoints, Map<String, Integer> total_sum_crashs,
			Map<String, Integer> total_sum_moves, Map<String, Double> total_sum_pointsSkaled, Map<String, Integer> total_sum_races, Map<String, Integer> total_sum_bonus,
			Map<String, Integer[]> total_sum_races_bychallenge, Map<String, Integer> playerMoves, Map<String, Integer> playerCrashs, Map<String, Integer> playerPosition,
			Map<String, Boolean> playerFinished, Map<Integer, Integer> pointsForMoves)
	{
		int moves;
		int crashs;
		double points;
		int position;

		List<String> players = getChallengePlayersFromLog(c, r);

		// process maps
		for(String player : players)
		{
			moves = playerMoves.get(player);
			crashs = playerCrashs.get(player);
			position = playerPosition.get(player);
			points = pointsForMoves.get(moves) * (intFromString(p.getProperty("points." + numberOfPlayers[c] + "." + position)) + 1);

			// Ausstiege/Rausschmisse immer 0 Punkte!
			if(!playerFinished.get(player))
				points = 0;

			if(points != -1)
			{
				real_points.get(player)[c].add(points);
				real_crashs.get(player)[c].add(crashs);
			}
			real_crashs_allraces.get(player)[c].add(crashs);

			fillRaceTable(table, totalTable, player, challengePlayers, c, r, position, points, crashs, moves, sum_pointsUnskaled, sum_basePoints, sum_crashs, sum_moves, total_sum_basePoints,
					total_sum_crashs, total_sum_moves, total_sum_pointsSkaled, total_sum_races, total_sum_bonus, total_sum_races_bychallenge);
		}
	}

	private void fillRaceTable(String[][] table, String totalTable[][], String player, List<String> challengePlayers, int c, int r, int position, double points, int crashs, int moves,
			Map<String, Double> sum_pointsUnskaled, Map<String, Double> sum_basePoints, Map<String, Integer> sum_crashs, Map<String, Integer> sum_moves, Map<String, Double> total_sum_basePoints,
			Map<String, Integer> total_sum_crashs, Map<String, Integer> total_sum_moves, Map<String, Double> total_sum_pointsSkaled, Map<String, Integer> total_sum_races,
			Map<String, Integer> total_sum_bonus, Map<String, Integer[]> total_sum_races_bychallenge)
	{

		String racePoints = (points < 0 ? (points == -1 ? "?" : "" + round(points)) : "" + round(crashs * points));

		while(table[position - 1][0] != null)
		{
			position--;
		}
		table[position - 1][0] = position + ".";
		table[position - 1][1] = playerToLink(player, false);
		table[position - 1][2] = (points == -1 ? "?" : "" + round(points));
		table[position - 1][3] = "" + crashs;
		table[position - 1][4] = "" + moves;
		table[position - 1][5] = racePoints;

		totalTable[challengePlayers.indexOf(player)][r] = racePoints;

		sum_pointsUnskaled.put(player, sum_pointsUnskaled.get(player) + (points < 0 ? (points == -1 ? 0 : points) : (crashs * points)));
		if(points != -1)
		{
			sum_basePoints.put(player, sum_basePoints.get(player) + points);
			total_sum_basePoints.put(player, total_sum_basePoints.get(player) + points);
			total_sum_races.put(player, total_sum_races.get(player) + 1);
			total_sum_races_bychallenge.get(player)[c] += 1;
		}
		sum_crashs.put(player, sum_crashs.get(player) + crashs);
		total_sum_crashs.put(player, total_sum_crashs.get(player) + crashs);
		sum_moves.put(player, sum_moves.get(player) + moves);
		total_sum_moves.put(player, total_sum_moves.get(player) + moves);
	}

	private void sortFinalTable(boolean finished)
	{
		if(!finished)
			Arrays.sort(finalTable, new FinalTableRowSorter(-2)); // sort by Erwartungswert
		else
			Arrays.sort(finalTable, new FinalTableRowSorter(-4)); // sort by Endergebnis
	}

	private double addBonus(int c, List<String> challengePlayers, String[][] totalTable, Map<String, Double> sum_pointsUnskaled, Map<String, Double> sum_basePoints, Map<String, Integer> sum_crashs,
			Map<String, Integer> sum_moves, Map<String, Integer> total_sum_bonus)
	{
		String player;

		int minMoves = Integer.MAX_VALUE;
		int maxCrashs = -1;
		double maxBasePoints = -1;
		double maxPointsUnskaled = -1;
		List<Integer> index_minMoves = new ArrayList<Integer>();
		List<Integer> index_maxCrashs = new ArrayList<Integer>();
		List<Integer> index_maxBasePoints = new ArrayList<Integer>();
		List<Integer> index_maxPointsUnskaled = new ArrayList<Integer>();
		for(int i = 0; i < challengePlayers.size(); i++)
		{
			player = challengePlayers.get(i);
			totalTable[i][pages[c].length + 3] = "" + round(sum_pointsUnskaled.get(player));
			if(sum_pointsUnskaled.get(player) > maxPointsUnskaled)
			{
				maxPointsUnskaled = sum_pointsUnskaled.get(player);
				index_maxPointsUnskaled.clear();
				index_maxPointsUnskaled.add(i);
			}
			else if(sum_pointsUnskaled.get(player) == maxPointsUnskaled)
			{
				maxPointsUnskaled = sum_pointsUnskaled.get(player);
				index_maxPointsUnskaled.add(i);
			}
			totalTable[i][pages[c].length + 0] = "" + round(sum_basePoints.get(player));
			if(sum_basePoints.get(player) > maxBasePoints)
			{
				maxBasePoints = sum_basePoints.get(player);
				index_maxBasePoints.clear();
				index_maxBasePoints.add(i);
			}
			else if(sum_basePoints.get(player) == maxBasePoints)
			{
				maxBasePoints = sum_basePoints.get(player);
				index_maxBasePoints.add(i);
			}
			totalTable[i][pages[c].length + 1] = "" + sum_crashs.get(player);
			if(sum_crashs.get(player) > maxCrashs)
			{
				maxCrashs = sum_crashs.get(player);
				index_maxCrashs.clear();
				index_maxCrashs.add(i);
			}
			else if(sum_crashs.get(player) == maxCrashs)
			{
				maxCrashs = sum_crashs.get(player);
				index_maxCrashs.add(i);
			}
			totalTable[i][pages[c].length + 2] = "" + sum_moves.get(player);
			if(sum_moves.get(player) < minMoves)
			{
				minMoves = sum_moves.get(player);
				index_minMoves.clear();
				index_minMoves.add(i);
			}
			else if(sum_moves.get(player) == minMoves)
			{
				minMoves = sum_moves.get(player);
				index_minMoves.add(i);
			}
		}

		// addBonus(c, index_minMoves, pages[c].length + 2, totalTable, challengePlayers, total_sum_bonus);
		addBonus(c, index_maxCrashs, pages[c].length + 1, totalTable, challengePlayers, total_sum_bonus);
		// addBonus(c, index_maxBasePoints, pages[c].length + 0, totalTable, challengePlayers, total_sum_bonus);
		// addBonus(c, index_maxPointsUnskaled, totalTable, challengePlayers,
		// total_sum_bonus);

		return maxPointsUnskaled;
	}

	private void addBonus(int c, List<Integer> indexes, int column, String[][] totalTable, List<String> challengePlayers, Map<String, Integer> total_sum_bonus)
	{
		for(int i : indexes)
		{
			if(totalTable[i][pages[c].length + 5].equals("?") || totalTable[i][pages[c].length + 5].equals("-"))
			{
				totalTable[i][pages[c].length + 5] = highlight("+10");
				total_sum_bonus.put(challengePlayers.get(i), total_sum_bonus.get(challengePlayers.get(i)) + 10);
			}
			else
			{
				for(int b = 10; b <= 100; b = b + 10)
				{
					if(totalTable[i][pages[c].length + 5].equals(highlight("+" + b)))
					{
						totalTable[i][pages[c].length + 5] = highlight("+" + (b + 10));
						total_sum_bonus.put(challengePlayers.get(i), total_sum_bonus.get(challengePlayers.get(i)) + 10);
						break;
					}
				}
			}
			totalTable[i][column] = highlight(totalTable[i][column]);
		}
	}

	private void addFinalBonus(List<String> challengePlayers, String[][] finalTable, Map<String, Double> total_sum_basePoints, Map<String, Integer> total_sum_crashs,
			Map<String, Integer> total_sum_moves, Map<String, Integer> final_sum_bonus)
	{
		String player;

		int minMoves = Integer.MAX_VALUE;
		int maxCrashs = -1;
		double maxBasePoints = -1;
		List<Integer> index_minMoves = new ArrayList<Integer>();
		List<Integer> index_maxCrashs = new ArrayList<Integer>();
		List<Integer> index_maxBasePoints = new ArrayList<Integer>();
		for(int i = 0; i < challengePlayers.size(); i++)
		{
			player = challengePlayers.get(i);
			if(total_sum_basePoints.get(player) > maxBasePoints)
			{
				maxBasePoints = total_sum_basePoints.get(player);
				index_maxBasePoints.clear();
				index_maxBasePoints.add(i);
			}
			else if(total_sum_basePoints.get(player) == maxBasePoints)
			{
				maxBasePoints = total_sum_basePoints.get(player);
				index_maxBasePoints.add(i);
			}
			if(total_sum_crashs.get(player) > maxCrashs)
			{
				maxCrashs = total_sum_crashs.get(player);
				index_maxCrashs.clear();
				index_maxCrashs.add(i);
			}
			else if(total_sum_crashs.get(player) == maxCrashs)
			{
				maxCrashs = total_sum_crashs.get(player);
				index_maxCrashs.add(i);
			}
			if(total_sum_moves.get(player) < minMoves)
			{
				minMoves = total_sum_moves.get(player);
				index_minMoves.clear();
				index_minMoves.add(i);
			}
			else if(total_sum_moves.get(player) == minMoves)
			{
				minMoves = total_sum_moves.get(player);
				index_minMoves.add(i);
			}
		}

		addFinalBonus(index_minMoves, pages.length + 3, finalTable, challengePlayers, final_sum_bonus);
		addFinalBonus(index_maxCrashs, pages.length + 2, finalTable, challengePlayers, final_sum_bonus);
		addFinalBonus(index_maxBasePoints, pages.length + 1, finalTable, challengePlayers, final_sum_bonus);
	}

	private void addFinalBonus(List<Integer> indexes, int column, String[][] finalTable, List<String> challengePlayers, Map<String, Integer> final_sum_bonus)
	{
		for(int i : indexes)
		{
			if(finalTable[i][pages.length + 6].equals("?") || finalTable[i][pages.length + 6].equals("-"))
			{
				finalTable[i][pages.length + 6] = highlight("+100");
				final_sum_bonus.put(challengePlayers.get(i), final_sum_bonus.get(challengePlayers.get(i)) + 100);
			}
			else
			{
				for(int b = 100; b <= 1000; b = b + 100)
				{
					if(finalTable[i][pages.length + 6].equals(highlight("+" + b)))
					{
						finalTable[i][pages.length + 6] = highlight("+" + (b + 100));
						final_sum_bonus.put(challengePlayers.get(i), final_sum_bonus.get(challengePlayers.get(i)) + 100);
						break;
					}
				}
			}

			// fett machen
			finalTable[i][column] = highlight(finalTable[i][column]);
		}
	}

	private void calculateExpected(List<String> challengePlayers)
	{
		expected_points = new TreeMapXArray<String, Double>(challengePlayers, new Double[tables.length], 0.0);
		expected_total_points = new TreeMapX<String, Double>(challengePlayers, 0.0);

		double crash_count, crash_players;
		double crash_allraces_count, crash_allraces_players;
		double avg_crashs;
		double avg_crashs_allraces;
		double avg_points;
		double expected;
		double expected_max;
		double actual_positive;
		double actual_negative;
		int negative_count;
		double player_avg_crashs;
		double player_avg_points;
		int total_players_finished;
		for(int c = 1; c < tables.length; c++)
		{
			total_players_finished = 0;
			expected_max = 0;
			crash_count = 0;
			crash_players = 0;
			crash_allraces_count = 0;
			crash_allraces_players = 0;
			for(String player : challengePlayers)
			{
				for(int crashs : real_crashs.get(player)[c])
				{
					crash_count += crashs;
					crash_players++;
				}
				for(int crashs : real_crashs_allraces.get(player)[c])
				{
					crash_allraces_count += crashs;
					crash_allraces_players++;
				}
			}
			avg_crashs_allraces = crash_allraces_count / crash_allraces_players;
			if(crash_players != 0)
				avg_crashs = crash_count / crash_players;
			else
				avg_crashs = avg_crashs_allraces;

			avg_points = intFromString(p.getProperty("points." + numberOfPlayers[c] + "." + (int) (numberOfPlayers[c] / 2)));

			for(String player : challengePlayers)
			{
				player_avg_crashs = 0;
				player_avg_points = 0;
				actual_positive = 0;
				actual_negative = 0;
				negative_count = 0;

				for(int i = 0; i < real_points.get(player)[c].size(); i++)
				{
					if(real_points.get(player)[c].get(i) > 0)
					{
						actual_positive += real_crashs.get(player)[c].get(i) * real_points.get(player)[c].get(i);
						player_avg_crashs += real_crashs.get(player)[c].get(i);
						player_avg_points += real_points.get(player)[c].get(i);
						total_players_finished++;
					}
					else
					{
						actual_negative += real_points.get(player)[c].get(i);
						negative_count++;
					}
				}

				if(real_points.get(player)[c].size() - negative_count > 0)
				{
					player_avg_crashs /= real_points.get(player)[c].size();
					player_avg_points /= real_points.get(player)[c].size();
				}
				else
				{
					player_avg_points = 0;
				}

				if(real_points.get(player)[c].size() == stats_racesPerPlayerPerChallenge)
				{
					// spieler hat alle Rennen beendet
					expected = actual_positive + actual_negative;
				}
				else if(real_points.get(player)[c].size() - negative_count > 0)
				{
					// spieler hat ein paar Rennen regulaer beendet (ohne rauswurf)
					// eigene durchschnittliche Punkte und Crashs verwenden
					expected = actual_positive + actual_negative;
					expected += player_avg_crashs * player_avg_points * (stats_racesPerPlayerPerChallenge - real_points.get(player)[c].size());
					// expected = player_avg_crashs * player_avg_points *
					// (stats_racesPerPlayerPerChallenge - negative_count ) + expected_negative;
				}
				else
				{
					// spieler hat noch keine Rennen beendet regulaer beendet
					// allgemeine durchschnittliche Punkte und Crashs verwenden
					expected = actual_positive + actual_negative;
					expected += avg_crashs * avg_points * (stats_racesPerPlayerPerChallenge - real_points.get(player)[c].size());
					// expected = avg_crashs * avg_points * stats_racesPerPlayerPerChallenge;
				}
				expected_points.get(player)[c] = expected;
				if(expected > expected_max)
					expected_max = expected;
			}

			for(String player : challengePlayers)
			{
				if(total_players_finished != 0 || expected_points.get(player)[c] < 0)
					expected_points.get(player)[c] = round(expected_points.get(player)[c] / expected_max * 100);
				else
					expected_points.get(player)[c] = 50.0;
				expected_total_points.put(player, expected_total_points.get(player) + expected_points.get(player)[c]);
			}
		}
	}
}
