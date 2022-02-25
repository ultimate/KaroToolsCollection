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

import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.utils.PropertiesUtil;

public class CCCEvalNew implements Eval
{
	/**
	 * Logger-Instance
	 */
	private static transient final Logger	logger			= LogManager.getLogger();

	private static final String				DEFAULT_FOLDER	= "";
	private static final int[]				ALL_COLUMNS;

	static
	{
		ALL_COLUMNS = new int[100];
		for(int i = 0; i < ALL_COLUMNS.length; i++)
			ALL_COLUMNS[i] = i;
	}

	private Properties						gidProperties;

	private int[][]							gids;
	private int[]							races;
	private int[]							numberOfPlayers;
	private int[]							maps;
	private String[]						mapNames;
	private int[]							zzz;
	private boolean[]						cps;
	private String[][]						pages;
	private String[][]						logs;

	private int								stats_racesPerPlayer;
	private int								stats_racesPerPlayerPerChallenge;
	private int								stats_races;
	private int								stats_moves;
	private int								stats_crashs;
	private int								stats_players;

	private Map<Integer, Integer>[]			pointsForMoves;

	private Map<String, List<Double>[]>		real_points;
	private Map<String, List<Integer>[]>	real_crashs;
	private Map<String, List<Integer>[]>	real_crashs_allraces;
	private Map<String, Double[]>			expected_points;
	private Map<String, Double>				expected_total_points;

	private String[][][][]					tables;
	private String[][][]					totalTables;
	private String[][]						finalTable;

	private String[][]						whoOnWho;

	private Properties						p;

	private int								cccx;
	private String							folder;

	private final String[]					mapTableHead			= new String[] { "Nr.", "Strecke", "Spielerzahl", "ZZZ", "CPs", "Spielzahl" };
	// TODO change order (move Grundpunkte after Züge)
	private final String[]					raceTableHead			= new String[] { "Platz", "Spieler", "Grundpunkte", "Crashs", "Züge", "Punkte" };
	private String[]						finalTableHead;
	private String[][]						totalTableHeads;

	private final String					start_playerName		= "&nbsp</TD><TD>";
	private final String					start_moves				= "</TD><TD ALIGN=CENTER>";

	private final String					start_playerName_Log	= ": ";
	private final String					end_playerName_Log		= " -> ";

	private final int						maxCols					= 7;

	private final String					highlight				= "'''";

	public static void main(String[] args) throws Exception
	{

		int cccx = Integer.parseInt(args[0]);

		CCCEvalNew e = new CCCEvalNew(cccx, "..\\CraZZZy Crash Challenge\\CCC" + cccx + "\\");

		// {
		// JFileChooser fileChooser = new JFileChooser();
		// int result = fileChooser.showOpenDialog(null);
		// if(result != JFileChooser.APPROVE_OPTION)
		// return;
		// File file = fileChooser.getSelectedFile();
		//
		// Main.main(new String[] { "-l=debug" });
		// Main.getGui().setVisible(false);
		//
		// FileInputStream fis = new FileInputStream(file);
		// BufferedInputStream bis = new BufferedInputStream(fis);
		// ObjectInputStream ois = new ObjectInputStream(bis);
		//
		// GameSeries gs = (GameSeries) ois.readObject();
		//
		// ois.close();
		// bis.close();
		// fis.close();
		//
		// e.prepare(gs, 20);
		// }
		e.doEvaluation();
	}

	public CCCEvalNew(int cccx, String folder)
	{
		this.cccx = cccx;
		this.folder = folder;
	}

	public String doEvaluation() throws IOException, InterruptedException
	{
		long start;
		logger.info("reading properties (" + cccx + ")... ");
		start = System.currentTimeMillis();
		gidProperties = readProperties(folder + "czzzcc" + cccx + "-gid.properties");
		logger.info("OK (" + (System.currentTimeMillis() - start) + ")");

		logger.info("buffering logs... ");
		start = System.currentTimeMillis();
		readLogs();
		logger.info("OK (" + (System.currentTimeMillis() - start) + ")");

		logger.info("creating tables... ");
		start = System.currentTimeMillis();
		boolean finished = createTables();
		logger.info("OK (" + (System.currentTimeMillis() - start) + ")");

		logger.info("creating WIKI... ");
		start = System.currentTimeMillis();
		String wiki = createWiki(folder + "czzzcc" + cccx + "-schema.txt", finished);
		logger.info("OK (" + (System.currentTimeMillis() - start) + ")");

		// logger.info();
		// logger.info();

		// logger.info(wiki);

		return wiki;
	}

	@Override
	public void prepare(GameSeries gs, int execution)
	{
		String file = folder + "czzzcc" + cccx + "-gid.properties";
		try
		{
			writeProperties(file, gs, execution);
		}
		catch(IOException e)
		{
			System.err.println("could not create " + file);
			e.printStackTrace();
		}
	}

	private String createWiki(String schemaFile, boolean finished) throws IOException
	{
		StringBuilder detail = new StringBuilder();
		StringBuilder detailLinks = new StringBuilder();

		String[][] tableTable;
		String[][] pointsTable;
		String[][] mapTable = new String[tables.length - 1][6];
		for(int c = 1; c < tables.length; c++)
		{
			mapTable[c - 1][0] = challengeToLink(c, true);
			mapTable[c - 1][1] = mapToLink(c, true);
			mapTable[c - 1][2] = "" + numberOfPlayers[c];
			mapTable[c - 1][3] = "" + (zzz[c] != -1 ? zzz[c] : "Random");
			mapTable[c - 1][4] = "" + (cps[c] ? "ja" : "n.V.");
			mapTable[c - 1][5] = "" + races[c];

			detail = new StringBuilder();
			detail.append("= Challenge " + c + " =\n");
			detail.append("Strecke: " + mapToLink(c, false) + "\n");
			detail.append("== Punkteverteilung ==\n");

			pointsTable = new String[2][pointsForMoves[c].size() + 1];
			pointsTable[0][0] = highlight("Züge");
			pointsTable[1][0] = highlight("Grundpunkte");
			int col = 1;
			for(Entry<Integer, Integer> pfm : pointsForMoves[c].entrySet())
			{
				pointsTable[0][col] = "" + pfm.getKey();
				pointsTable[1][col] = "" + pfm.getValue();
				col++;
			}

			detail.append(tableToString(null, pointsTable, null, ALL_COLUMNS));
			detail.append("\n");
			detail.append("== Rennergebnisse ==\n");

			tableTable = new String[tables[c].length / maxCols + 1][maxCols];
			for(int cell = 0; cell < maxCols; cell++)
			{
				tableTable[tableTable.length - 1][cell] = "";
			}

			int row = 0;
			col = 0;
			for(int r = 1; r < tables[c].length; r++)
			{
				tableTable[row][col] = "\nChallenge " + raceToLink(c, r) + "\n" + tableToString(raceTableHead, tables[c][r], null, ALL_COLUMNS) + "\n";
				col++;
				if(col == maxCols)
				{
					col = 0;
					row++;
				}
			}

			detail.append(tableToString(null, tableTable, null, ALL_COLUMNS));
			detail.append("\n");
			detail.append("== Tabellarische Auswertung ==\n");
			detail.append(tableToString(totalTableHeads[c], totalTables[c], null, ALL_COLUMNS));
			detail.append("\n");

			toFile(folder + "czzzcc" + cccx + "-wiki-challenge" + c + ".txt", detail.toString());
			detailLinks.append("*" + challengeToLink(c, true) + "\n");
		}

		StringBuilder total = new StringBuilder();

		if(!finished)
			total.append(tableToString(finalTableHead, finalTable, "alignedright", Arrays.copyOf(ALL_COLUMNS, finalTableHead.length - 1)));
		else
			total.append(tableToString(finalTableHead, finalTable, "alignedright", Arrays.copyOf(ALL_COLUMNS, finalTableHead.length - 3)));

		StringBuilder stats = new StringBuilder();

		stats.append("== Zahlen & Fakten ==\n");
		stats.append("*Rennen insgesamt: '''" + stats_races + "'''\n");
		stats.append("*Teilnehmer: '''" + stats_players + "'''\n");
		stats.append("*Rennen pro Spieler: '''" + stats_racesPerPlayer + "'''\n");
		stats.append("*Rennen pro Spieler pro Challenge: '''" + stats_racesPerPlayerPerChallenge + "'''\n");
		stats.append("*Züge insgesamt: '''" + stats_moves + "'''\n");
		stats.append("*Crashs insgesamt: '''" + stats_crashs + "'''\n");
		stats.append("*Häufigste Begegnung: " + getMaxMinWhoOnWho("max") + "\n");
		stats.append("*Seltenste Begegnung: " + getMaxMinWhoOnWho("min") + "\n");

		stats.append("== Wer gegen wen? ==\n");
		stats.append("Eigentlich wollte ich hier noch die ganzen Links zu den Spielen reinschreiben, aber damit kam das Wiki nicht klar! Daher hier nur die Anzahl...\n");
		stats.append(tableToString(null, whoOnWho, null, ALL_COLUMNS));

		StringBuilder schema = new StringBuilder();

		BufferedReader br = new BufferedReader(new FileReader(schemaFile));
		String tmp;
		while((tmp = br.readLine()) != null)
		{
			schema.append(tmp);
			schema.append("\n");
		}
		br.close();

		String s = schema.toString();
		s = s.replace("${MAPS}", tableToString(mapTableHead, mapTable, null, ALL_COLUMNS));
		s = s.replace("${DETAIL}", detailLinks.toString());
		s = s.replace("${TOTAL}", total.toString());
		s = s.replace("${STATS}", stats.toString());

		toFile(folder + "czzzcc" + cccx + "-wiki-overview.txt", s.toString());

		return s;
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
		finalTableHead[pages.length + 3] = "Züge (gesamt)";
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
			totalTableHead[pages[c].length + 2] = "Züge (gesamt)";
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

	private void initWhoOnWho(List<String> challengePlayers)
	{
		whoOnWho = new String[challengePlayers.size() + 1][challengePlayers.size() + 1];
		whoOnWho[0][0] = "";
		for(int i1 = 1; i1 < whoOnWho.length; i1++)
		{
			whoOnWho[i1][0] = playerToLink(challengePlayers.get(i1 - 1), false);
			whoOnWho[0][i1] = whoOnWho[i1][0];
			whoOnWho[i1][i1] = "-";
			for(int i2 = i1 + 1; i2 < whoOnWho.length; i2++)
			{
				whoOnWho[i1][i2] = "0";
				whoOnWho[i2][i1] = "0";
			}
		}
	}

	private void createWhoOnWho(int c, int r, List<String> challengePlayers)
	{
		List<String> racePlayers = getChallengePlayersFromLog(c, r);
		int ci1, ci2;
		// String tmp;
		for(int i1 = 0; i1 < racePlayers.size(); i1++)
		{
			for(int i2 = i1 + 1; i2 < racePlayers.size(); i2++)
			{
				ci1 = challengePlayers.indexOf(racePlayers.get(i1));
				ci2 = challengePlayers.indexOf(racePlayers.get(i2));
				whoOnWho[ci1 + 1][ci2 + 1] = "" + (intFromString(whoOnWho[ci1 + 1][ci2 + 1]) + 1);
				// if(whoOnWho[ci1 + 1][ci2 + 1].equals("0"))
				// whoOnWho[ci1 + 1][ci2 + 1] = "0 ()";
				// tmp = whoOnWho[ci1 + 1][ci2 + 1].substring(0, whoOnWho[ci1 +
				// 1][ci2 +
				// 1].indexOf(" "));
				// whoOnWho[ci1 + 1][ci2 + 1] = (intFromString(tmp) + 1) +
				// whoOnWho[ci1 + 1][ci2
				// + 1].substring(tmp.length());
				// whoOnWho[ci1 + 1][ci2 + 1] = whoOnWho[ci1 + 1][ci2 +
				// 1].replace(")",
				// raceToLink(c, r) + ")");
				whoOnWho[ci2 + 1][ci1 + 1] = whoOnWho[ci1 + 1][ci2 + 1];
			}
		}
	}

	private String getMaxMinWhoOnWho(String type)
	{
		List<int[]> maxMinList = new LinkedList<int[]>();
		int maxMin = (type.equals("max") ? Integer.MIN_VALUE : Integer.MAX_VALUE);
		int val;
		for(int i1 = 1; i1 < whoOnWho.length; i1++)
		{
			for(int i2 = i1 + 1; i2 < whoOnWho.length; i2++)
			{
				val = intFromString(whoOnWho[i1][i2]);
				if(type.equals("max"))
				{
					if(val > maxMin)
					{
						maxMinList.clear();
						maxMinList.add(new int[] { i1 - 1, i2 - 1 });
						maxMin = val;
					}
					else if(val == maxMin)
					{
						maxMinList.add(new int[] { i1 - 1, i2 - 1 });
						maxMin = val;
					}
				}
				else
				{
					if(val < maxMin)
					{
						maxMinList.clear();
						maxMinList.add(new int[] { i1 - 1, i2 - 1 });
						maxMin = val;
					}
					else if(val == maxMin)
					{
						maxMinList.add(new int[] { i1 - 1, i2 - 1 });
						maxMin = val;
					}
				}
			}
		}
		List<String> challengePlayers = getChallengePlayersFromLog(1);
		StringBuilder ret = new StringBuilder();
		ret.append(maxMin + " mal ");
		for(int i = 0; i < maxMinList.size(); i++)
		{
			if(i > 0 && i < maxMinList.size() - 1)
				ret.append(", ");
			else if(i > 0 && i == maxMinList.size() - 1)
				ret.append(" und ");
			ret.append(playerToLink(challengePlayers.get(maxMinList.get(i)[0]), false));
			ret.append(" '''vs.''' ");
			ret.append(playerToLink(challengePlayers.get(maxMinList.get(i)[1]), false));
		}
		return ret.toString();
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

	private List<String> getChallengePlayersFromLog(int c)
	{
		TreeMap<String, String> players = new TreeMap<String, String>();
		for(int r = 1; r < pages[c].length; r++)
		{
			for(String player : getChallengePlayersFromLog(c, r))
			{
				players.put(player.toLowerCase(), player);
			}
		}
		return new LinkedList<String>(players.values());
	}

	private List<String> getChallengePlayersFromLog(int c, int r)
	{
		List<String> players = new LinkedList<String>();
		int index = 0;
		int end, end1, end2, end3, end4;
		String player;
		String log = getLog(c, r);
		int firstMovePartEnd = log.indexOf(": -----------------------------------");
		if(firstMovePartEnd == -1)
		{
			// logger.info("First round not yet completed: need to look in page instead of
			// log: " + c + "." + r);
			return getChallengePlayers(c, r);
		}
		String firstMovePart = log.substring(log.indexOf("von " + p.getProperty("creator") + " erstellt"), firstMovePartEnd);
		while(true)
		{
			index = firstMovePart.indexOf(start_playerName_Log, index + 1) + start_playerName_Log.length();
			if(index == start_playerName_Log.length() - 1)
				break;
			end1 = firstMovePart.indexOf(end_playerName_Log, index + 1);
			end2 = firstMovePart.indexOf(" wird von ", index + 1);
			end3 = firstMovePart.indexOf(" steigt aus ", index + 1);
			end4 = firstMovePart.indexOf(" CRASHT!!!", index + 1);

			if(end1 == -1)
				end1 = Integer.MAX_VALUE;
			if(end2 == -1)
				end2 = Integer.MAX_VALUE;
			if(end3 == -1)
				end3 = Integer.MAX_VALUE;
			if(end4 == -1)
				end4 = Integer.MAX_VALUE;
			end = Math.min(end1, Math.min(end2, Math.min(end3, end4)));

			player = firstMovePart.substring(index, end);

			if(player.equals(p.getProperty("creator")))
				continue;
			if(players.contains(player))
				continue; // Beim Ausstiegs-Bug kann es zu Doppeleinträgen (Ausstieg + Crash) in der
							// ersten Runde kommen
			players.add(player);
		}
		return players;
	}

	private List<String> getChallengePlayers(int c, int r)
	{
		List<String> players = new LinkedList<String>();
		int index = 0;
		String player;
		String page = getPage(c, r);
		while(true)
		{
			if(page == null)
				logger.info("ERROR: page is null");
			index = page.indexOf(start_playerName, index + 1) + start_playerName.length();
			if(index == start_playerName.length() - 1)
				break;
			player = page.substring(index, page.indexOf(start_moves, index + 1));

			if(player.equals(p.getProperty("creator")))
				continue;
			players.add(player);
		}
		return players;
	}

	private void readLogs() throws MalformedURLException, InterruptedException
	{
		for(int c = 1; c < logs.length; c++)
		{
			for(int r = 1; r < logs[c].length; r++)
			{
				queue.addThread(new GameLoaderThread(c, r, "log"));
			}
		}
		queue.begin();
		queue.waitForFinished();
		// logger.info("all logs loaded");
	}

	private String getPage(int c, int r)
	{
		if(pages[c][r] == null)
		{
			// logger.info("page null: loading: " + c + "." + r);
			try
			{
				queue.addThread(new GameLoaderThread(c, r, "page"));
				queue.begin();
				queue.waitForFinished();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			// logger.info(pages[c][r]);
		}
		return pages[c][r];
	}

	private String getLog(int c, int r)
	{
		if(logs[c][r] == null)
		{
			try
			{
				queue.addThread(new GameLoaderThread(c, r, "log"));
				queue.begin();
				queue.waitForFinished();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return logs[c][r];
	}

	private Properties readProperties(String file) throws IOException
	{
		p = PropertiesUtil.loadProperties(new File(file));

		int amount = intFromString(p.getProperty("challenges"));
		maps = new int[amount + 1];
		mapNames = new String[amount + 1];
		races = new int[amount + 1];
		numberOfPlayers = new int[amount + 1];
		zzz = new int[amount + 1];
		cps = new boolean[amount + 1];
		for(int c = 1; c <= amount; c++)
		{
			maps[c] = intFromString(p.getProperty(c + ".map"));
			mapNames[c] = new String(p.getProperty(c + ".mapName").getBytes("ISO-8859-1"), "UTF-8");
			races[c] = intFromString(p.getProperty(c + ".races"));
			numberOfPlayers[c] = intFromString(p.getProperty(c + ".players"));
			zzz[c] = intFromString(p.getProperty(c + ".zzz"));
			cps[c] = booleanFromString(p.getProperty(c + ".cps"));
		}

		gids = new int[amount + 1][];
		pages = new String[amount + 1][];
		logs = new String[amount + 1][];
		tables = new String[amount + 1][][][];
		totalTables = new String[amount + 1][][];
		totalTableHeads = new String[amount + 1][];

		for(int c = 1; c <= amount; c++)
		{
			gids[c] = new int[races[c] + 1];
			pages[c] = new String[races[c] + 1];
			logs[c] = new String[races[c] + 1];
			tables[c] = new String[races[c] + 1][][];

			for(int r = 1; r <= races[c]; r++)
			{
				gids[c][r] = intFromString(p.getProperty(c + "." + r));
			}
		}

		return p;
	}

	private void writeProperties(String fileName, GameSeries gameSeries, int execution) throws IOException
	{
		if(!(gameSeries instanceof BalancedGameSeries))
			return;

		BalancedGameSeries gs = (BalancedGameSeries) gameSeries;

		Properties p;

		File file = new File(fileName);
		if(file.exists())
		{
			// load points from predefined properties
			p = PropertiesUtil.loadProperties(file);
			// backup file
			file.renameTo(new File(fileName + "." + (execution - 1)));
		}
		else
		{
			p = new Properties();
		}

		p.setProperty("creator", gs.getCreator().getName());
		int numberOfPlayers = gs.getPlayers().size();
		int gamesPerPlayerPerChallenge = gs.getRules(1).getGamesPerPlayer(); // same for all
																				// challenges
		p.setProperty("races.per.player.per.challenge", "" + gamesPerPlayerPerChallenge);

		int c;
		for(c = 1; c <= gs.getNumberOfMaps(); c++)
		{
			int numberOfPlayersPerRace = gs.getRules(c - 1).getNumberOfPlayers();
			int races = gamesPerPlayerPerChallenge * numberOfPlayers / numberOfPlayersPerRace;
			p.setProperty(c + ".map", "" + gs.getMap(c - 1).getId());
			p.setProperty(c + ".mapName", "" + gs.getMap(c - 1).getName());
			p.setProperty(c + ".races", "" + races);
			p.setProperty(c + ".players", "" + numberOfPlayersPerRace);
			p.setProperty(c + ".zzz", "" + (gs.getRules(c - 1).getMinZzz() == gs.getRules(c - 1).getMaxZzz() ? gs.getRules(c - 1).getMinZzz() : -1));
			p.setProperty(c + ".cps", "" + gs.getRules(c - 1).getCheckpointsActivated());

			Game g;
			int racesCreated = 0;
			for(int i = 1; i <= races; i++)
			{
				g = null;
				for(Game gi : gs.getGames())
				{
					if(gi.getName().contains("Challenge " + c + "." + i))
					{
						g = gi;
						break;
					}
				}
				if(g != null && g.isCreated() && g.getId() > 0)
				{
					p.setProperty(c + "." + i, "" + g.getId());
					racesCreated++;
				}
			}
			if(racesCreated != races)
			{
				break;
			}
		}
		int challenges = c - 1;
		logger.info("number of challenges: " + challenges);
		p.setProperty("challenges", "" + challenges);

		PropertiesUtil.storeProperties(file, p, "");
	}

	private String raceToLink(int challenge, int race)
	{
		return "{{Rennen|" + gids[challenge][race] + "|" + challenge + "." + race + "}}";
	}

	private String challengeToLink(int c, boolean text)
	{
		return "[[CraZZZy Crash Challenge " + cccx + " - Detailwertung Challenge " + c + "|" + (text ? "Challenge " : "") + c + "]]";
	}

	private String mapToLink(int challenge, boolean includeName)
	{
		return "{{Karte|" + maps[challenge] + "}}" + (includeName ? " " + mapNames[challenge] : "");
	}

	private String playerToLink(String player, boolean bold)
	{
		String tmp;
		if(player.startsWith("Deep"))
			tmp = "[[" + player + "]]";
		else if(player.equals("OleOCrasher"))
			tmp = "[[Benutzer:OleOJumper|OleOCrasher]]";
		else
			tmp = "[[Benutzer:" + player + "|" + player + "]]";
		if(bold)
			tmp = highlight(tmp);
		return tmp;
	}

	private String tableToString(String[] head, String[][] table, String cssclasses, int[] columns)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{|class=\"wikitable");
		if(cssclasses != null)
			sb.append(" " + cssclasses);
		sb.append("\"\n");

		int col;
		if(head != null)
		{
			sb.append("!");
			for(int i = 0; i < columns.length; i++)
			{
				col = columns[i];
				if(col >= head.length)
					continue;

				if(i > 0)
					sb.append("||");

				sb.append(head[col]);
			}
			sb.append("\n|-\n");
		}
		for(String[] row : table)
		{
			sb.append("|");
			for(int i = 0; i < columns.length; i++)
			{
				col = columns[i];
				if(col >= row.length)
					continue;

				if(i > 0)
					sb.append("||");

				sb.append(row[col]);
			}
			sb.append("\n|-\n");
		}
		sb.append("|}");
		return sb.toString();
	}

	private int countOccurrences(String source, String part)
	{
		return countOccurrences(source, part, true, 0, source.length());
	}

	private int countOccurrences(String source, String part, int from)
	{
		return countOccurrences(source, part, true, from, source.length());
	}

	private int countOccurrences(String source, String part, boolean ignoreDuplicates, int from, int to)
	{
		int lastIndex = from;
		int index = from;
		int count = 0;
		while(true)
		{
			index = source.indexOf(part, index);
			if(index == -1 || index >= to)
				break;
			if(!(ignoreDuplicates && countOccurrences(source, "\n", false, lastIndex, index) == 1 && lastIndex != from))
				count++;
			lastIndex = index;
			index++;
		}
		return count;
	}

	private void toFile(String filename, String content) throws IOException
	{
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filename));

		bos.write(content.getBytes());

		bos.flush();
		bos.close();
	}

	private double round(double d)
	{
		return Math.round(d * 100) / 100.0;
	}

	private int intFromString(String s)
	{
		return Integer.parseInt(s.replace(highlight, ""));
	}

	private boolean booleanFromString(String s)
	{
		return Boolean.valueOf(s.replace(highlight, ""));
	}

	private double doubleFromString(String s)
	{
		return Double.parseDouble(s.replace(highlight, ""));
	}

	private String highlight(String s)
	{
		return highlight + s + highlight;
	}

	private class GameLoaderThread extends URLLoaderThread<String>
	{
		private int		challenge;
		private int		race;
		private String	type;

		public GameLoaderThread(int challenge, int race, String type) throws MalformedURLException
		{
			super(new URL(type.equals("log") ? "http://www.karopapier.de/logs/" + gids[challenge][race] + ".log" : "http://www.karopapier.de/showmap.php"), type.equals("log") ? "GET" : "POST",
					type.equals("log") ? "" : "GID=" + gids[challenge][race], 10000);
			this.challenge = challenge;
			this.race = race;
			this.type = type;
		}

		@Override
		public void innerRun()
		{
			while(result == null || !result.contains("CraZZZy"))
			{
				super.innerRun();
			}
			if(type.equals("log"))
				logs[challenge][race] = result;
			else
				pages[challenge][race] = result;
		}

		@Override
		public String parse(String refreshed)
		{
			return refreshed;
		}
	}

	private class FinalTableRowSorter implements Comparator<String[]>
	{
		private int mainColumnOffset;

		public FinalTableRowSorter(int mainColumnOffset)
		{
			this.mainColumnOffset = mainColumnOffset;
		}

		@Override
		public int compare(String[] o1, String[] o2)
		{
			double val1 = doubleFromString(o1[o1.length + mainColumnOffset]);
			double val2 = doubleFromString(o2[o2.length + mainColumnOffset]);
			if(val2 != val1)
				return (int) Math.signum(val2 - val1);

			// mehr crashs
			int c1 = intFromString(o1[o1.length - 9]);
			int c2 = intFromString(o2[o2.length - 9]);
			if(c2 != c1)
				return (int) Math.signum(c2 - c1);

			// mehr grundpunkte
			double b1 = doubleFromString(o1[o1.length - 10]);
			double b2 = doubleFromString(o2[o2.length - 10]);
			if(b2 != b1)
				return (int) Math.signum(b2 - b1);

			// weniger Züge
			int m1 = intFromString(o1[o1.length - 8]);
			int m2 = intFromString(o2[o2.length - 8]);
			if(m2 != m1)
				return (int) Math.signum(m1 - m2);

			return 0;
		}
	}

	private class TreeMapX<K, V> extends TreeMap<K, V>
	{
		private static final long serialVersionUID = 1L;

		public TreeMapX(List<K> keys, V defaultValue)
		{
			for(K k : keys)
			{
				put(k, defaultValue);
			}
		}
	}

	private class TreeMapX2<K, V> extends TreeMap<K, List<V>[]>
	{
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unchecked")
		public TreeMapX2(List<K> keys)
		{
			List<?>[] array;
			List<V> list;
			for(K k : keys)
			{
				array = new ArrayList<?>[tables.length];
				for(int i = 0; i < array.length; i++)
				{
					list = new ArrayList<V>();
					array[i] = list;
				}
				put(k, (List<V>[]) array);
			}
		}
	}

	private class TreeMapXArray<K, V> extends TreeMap<K, V[]>
	{
		private static final long serialVersionUID = 1L;

		public TreeMapXArray(List<K> keys, V[] array, V defaultValue)
		{
			for(K k : keys)
			{
				V[] arrayClone = array.clone();
				if(defaultValue != null)
					for(int i = 0; i < arrayClone.length; i++)
						arrayClone[i] = defaultValue;
				put(k, arrayClone);
			}
		}
	}
}
