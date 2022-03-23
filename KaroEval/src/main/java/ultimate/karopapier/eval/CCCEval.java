package ultimate.karopapier.eval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karopapier.utils.Table;
import ultimate.karopapier.utils.WikiUtil;

public class CCCEval extends Eval<GameSeries>
{
	protected static final String				GAMES_KEY			= "Balanced";
	protected static final String[]				TABLE_HEAD_MAPS		= new String[] { "Nr.", "Strecke", "Spielerzahl", "ZZZ", "CPs", "Spielzahl" };
	protected static final String[]				TABLE_HEAD_GAME		= new String[] { "Platz", "Spieler", "Grundpunkte", "Crashs", "Züge", "Punkte" };

	protected final int							TABLE_TABLE_COLUMNS	= 7;

	protected int								cccx;
	// stats
	protected int								stats_challengesTotal;
	protected int								stats_challengesCreated;
	protected int								stats_gamesPerPlayer;
	protected int								stats_gamesPerPlayerPerChallenge;
	protected int								stats_gamesTotal;
	protected int								stats_gamesCreated;
	protected int								stats_moves;
	protected int								stats_crashs;
	protected int								stats_players;
	// helpers
	protected Integer[]							challengeGames;
	protected Integer[]							challengeOffsets;
	protected List<User>						usersByLogin;
	// evaluation
	protected Table[][]							tables;
	protected Table[]							totalTables;
	protected Table								finalTable;
	protected Table								whoOnWho;

	// old

	private TreeMap<String, List<Double>[]>		real_points;
	private TreeMap<String, List<Integer>[]>	real_crashs;
	private TreeMap<String, List<Integer>[]>	real_crashs_allgames;
	private TreeMap<String, Double[]>			expected_points;
	private TreeMap<String, Double>				expected_total_points;
	protected String[][]						totalTableHeads;

	private Properties							p;

	public CCCEval(int cccx)
	{
		this.cccx = cccx;
	}

	@Override
	public void prepare(KaroAPICache karoAPICache, GameSeries gameSeries, Properties properties, File folder, int execution)
	{
		super.prepare(karoAPICache, gameSeries, properties, folder, execution);

		if(gameSeries.getType() != EnumGameSeriesType.Balanced)
			return;

		// since all information is stored in the gameseries, we don't need the gid-properties anymore

		// but we init some other values for convenience
		this.stats_challengesTotal = (int) gameSeries.get(GameSeries.NUMBER_OF_MAPS);
		this.stats_players = gameSeries.getPlayers().size();
		this.stats_gamesTotal = gameSeries.getGames().size();
		this.stats_gamesPerPlayerPerChallenge = this.getRules(0).getGamesPerPlayer();
		this.stats_gamesPerPlayer = this.stats_gamesPerPlayerPerChallenge * this.stats_challengesTotal;

		// all games are stored in 1 list - to be able to access them easier we calculate offsets
		this.challengeGames = new Integer[this.stats_challengesTotal];
		this.challengeOffsets = new Integer[this.stats_challengesTotal];
		this.stats_challengesCreated = 0;
		this.stats_gamesCreated = 0;
		boolean allCreated;
		int offset = 0;
		int gamesInThisChallenge;
		List<PlannedGame> games = gameSeries.getGames().get(GAMES_KEY);
		for(int c = 0; c < this.stats_challengesTotal; c++)
		{
			this.challengeOffsets[c] = offset;
			this.challengeGames[c] = this.stats_gamesPerPlayerPerChallenge * this.stats_players / this.getRules(c).getNumberOfPlayers();
			// check if all games in this challenge have been created
			allCreated = true;
			for(int g = 0; g < this.challengeGames[c]; g++)
			{
				if(games.get(offset + g).isCreated())
					this.stats_gamesCreated++;
				else
					allCreated = false;
			}
			if(allCreated)
				this.stats_challengesCreated++;
			offset += this.challengeGames[c];
		}
		
		// some more helpful variables
		usersByLogin = new ArrayList<>(gameSeries.getPlayers());
		Collections.sort(usersByLogin, (u1, u2) -> {return u1.getLoginLowerCase().compareTo(u2.getLoginLowerCase()); });
		
		// create the header for the final table
		String[] finalTableHead = new String[this.stats_challengesCreated + 11];
		int col = 0;
		finalTableHead[col++] = "Platz";
		finalTableHead[col++] = "Spieler";
		for(int c = 0; c < this.stats_challengesCreated; c++)
			finalTableHead[col++] = challengeToLink(c, false);
		finalTableHead[col++] = "Grundpunkte (gesamt)";
		finalTableHead[col++] = "Crashs (gesamt)";
		finalTableHead[col++] = "Z�ge (gesamt)";
		finalTableHead[col++] = "Skalierte Punkte (gesamt)";
		finalTableHead[col++] = "Challenge-Bonus (gesamt)";
		finalTableHead[col++] = "Bonus (Gesamtwertung)";
		finalTableHead[col++] = "Endergebnis";
		finalTableHead[col++] = "Abgeschlossene Rennen";
		finalTableHead[col++] = "Erwartungswert";
		finalTableHead[col++] = "Erwartungswert (alt)";
		
		// init table variables
		this.tables = new Table[stats_challengesTotal][];
		this.totalTables = new Table[stats_challengesTotal];
		this.finalTable = new Table(finalTableHead);

				// create the header for the whoOnWho
		String[] whoOnWhoHead = new String[this.stats_players + 1];
		this.whoOnWho = new Table(whoOnWhoHead);
		col = 1; // leave first column empty
		Object[] row;
		for(User user: usersByLogin)
		{
			whoOnWhoHead[col++] = WikiUtil.createLink(user, false);
			row = new Object[whoOnWhoHead.length];
			row[0] = WikiUtil.createLink(user, false);
			for(int ci = 1; ci < whoOnWhoHead.length; ci++)
				row[ci] = 0;
			this.whoOnWho.addRow(row);
		}

		logger.info("preparation complete:");
		logger.info("  challengesTotal            = " + this.stats_challengesTotal);
		logger.info("  challengesCreated          = " + this.stats_challengesCreated);
		logger.info("  players                    = " + this.stats_players);
		logger.info("  gamesTotal                 = " + this.stats_gamesTotal);
		logger.info("  gamesCreated               = " + this.stats_gamesCreated);
		logger.info("  gamesPerPlayer             = " + this.stats_gamesPerPlayer);
		logger.info("  gamesPerPlayerPerChallenge = " + this.stats_gamesPerPlayerPerChallenge);
		logger.info("  challengeGames             = " + Arrays.asList(this.challengeGames));
		logger.info("  challengeOffsets           = " + Arrays.asList(this.challengeOffsets));
	}

	public int doEvaluation() throws IOException, InterruptedException
	{
		long start;

		// logger.info("buffering games... ");
		// start = System.currentTimeMillis();
		// loadGameDetails(gameSeries);
		// logger.info("OK (" + (System.currentTimeMillis() - start) + ")");

		logger.info("creating tables... ");
		start = System.currentTimeMillis();
		boolean finished = createTables();
		logger.info("OK (" + (System.currentTimeMillis() - start) + ")");

		logger.info("creating WIKI... ");
		start = System.currentTimeMillis();
		String wiki = createWiki(folder + "czzzcc" + cccx + "-schema.txt", finished);
		logger.info("OK (" + (System.currentTimeMillis() - start) + ")");

		if(logger.isDebugEnabled())
		{
			logger.debug("-----------------------------------------------------------------------------");
			logger.debug("-----------------------------------------------------------------------------");
			logger.debug("\n" + wiki);
		}

		// TODO what to return?

		return wiki;
	}

	protected String createWiki(String schemaFile, boolean finished) throws IOException
	{
		StringBuilder detail = new StringBuilder();
		StringBuilder detailLinks = new StringBuilder();

		Table tableTable;
		Table mapTable = new Table(TABLE_HEAD_MAPS);
		Rules rules;
		Object[] row;
		for(int c = 0; c < stats_challengesTotal; c++)
		{
			rules = this.getRules(c);
			row = new Object[TABLE_HEAD_MAPS.length];
			row[0] = challengeToLink(c, true);
			row[1] = mapToLink(c, true);
			row[2] = rules.getNumberOfPlayers();
			row[3] = (rules.getMinZzz() == rules.getMaxZzz() ? rules.getMinZzz() : rules.getMinZzz() + "-" + rules.getMaxZzz());
			row[4] = (rules.getCps() == null ? "Random" : (rules.getCps() ? "ja" : "nein"));
			row[5] = challengeGames[c];
			mapTable.addRow(row);

			detail = new StringBuilder();
			detail.append("= Challenge " + (c + 1) + " =\n");
			detail.append("Strecke: " + mapToLink(c, false) + "\n");
			detail.append("== Rennergebnisse ==\n");

			tableTable = new Table(TABLE_TABLE_COLUMNS);
			for(int g = 0; g < tables[c].length; g++)
			{
				if(g % TABLE_TABLE_COLUMNS == 0)
					row = new Object[TABLE_TABLE_COLUMNS];
				row[g % TABLE_TABLE_COLUMNS] = "\nChallenge " + gameToLink(c, g) + "\n" + WikiUtil.toString(tables[c][g], null) + "\n";
				tableTable.addRow(row);
			}

			detail.append(WikiUtil.toString(tableTable, null));
			detail.append("\n");
			detail.append("== Tabellarische Auswertung ==\n");
			detail.append(WikiUtil.toString(totalTables[c], null));
			detail.append("\n");

			writeFile("czzzcc" + cccx + "-wiki-challenge" + (c + 1) + ".txt", detail.toString());
			detailLinks.append("*" + challengeToLink(c, true) + "\n");
		}

		StringBuilder total = new StringBuilder();

		if(!finished)
			total.append(WikiUtil.toString(finalTable, "alignedright", Arrays.copyOf(ALL_COLUMNS, finalTableHead.length - 1)));
		else
			total.append(WikiUtil.toString(finalTable, "alignedright", Arrays.copyOf(ALL_COLUMNS, finalTableHead.length - 3)));

		StringBuilder stats = new StringBuilder();

		stats.append("== Zahlen & Fakten ==\n");
		stats.append("*Rennen insgesamt: '''" + stats_games + "'''\n");
		stats.append("*Teilnehmer: '''" + stats_players + "'''\n");
		stats.append("*Rennen pro Spieler: '''" + stats_gamesPerPlayer + "'''\n");
		stats.append("*Rennen pro Spieler pro Challenge: '''" + stats_gamesPerPlayerPerChallenge + "'''\n");
		stats.append("*Z�ge insgesamt: '''" + stats_moves + "'''\n");
		stats.append("*Crashs insgesamt: '''" + stats_crashs + "'''\n");
		stats.append("*H�ufigste Begegnung: " + getMaxMinWhoOnWho("max") + "\n");
		stats.append("*Seltenste Begegnung: " + getMaxMinWhoOnWho("min") + "\n");

		stats.append("== Wer gegen wen? ==\n");
		stats.append("Eigentlich wollte ich hier noch die ganzen Links zu den Spielen reinschreiben, aber damit kam das Wiki nicht klar! Daher hier nur die Anzahl...\n");
		stats.append(WikiUtil.toString(null, whoOnWho, null, ALL_COLUMNS));

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
		s = s.replace("${MAPS}", WikiUtil.toString(TABLE_HEAD_MAPS, mapTable, null, ALL_COLUMNS));
		s = s.replace("${DETAIL}", detailLinks.toString());
		s = s.replace("${TOTAL}", total.toString());
		s = s.replace("${STATS}", stats.toString());

		writeFile("czzzcc" + cccx + "-wiki-overview.txt", s.toString());

		return s;
	}

	private boolean createTables()
	{
		String[][] totalTable;
		String[] totalTableHead;
		String player;
		List<String> challengePlayers = this.challenges[c].games[r].getPlayers()FromLog(1);
		stats_players = challengePlayers.size();

		TreeMap<String, Double> sum_pointsUnskaled;
		TreeMap<String, Double> sum_basePoints;
		TreeMap<String, Integer> sum_crashs;
		TreeMap<String, Integer> sum_moves;
		TreeMap<String, Double> total_sum_basePoints = createMap(challengePlayers, 0.0);
		TreeMap<String, Integer> total_sum_crashs = createMap(challengePlayers, (Integer) 0);
		TreeMap<String, Integer> total_sum_moves = createMap(challengePlayers, (Integer) 0);
		TreeMap<String, Double> total_sum_pointsSkaled = createMap(challengePlayers, 0.0);
		TreeMap<String, Integer> total_sum_games = createMap(challengePlayers, (Integer) 0);
		TreeMap<String, Integer> total_sum_bonus = createMap(challengePlayers, (Integer) 0);
		TreeMap<String, Integer> final_sum_bonus = createMap(challengePlayers, (Integer) 0);
		TreeMap<String, Integer[]> total_sum_games_bychallenge = createMap(challengePlayers, new Integer[tables.length], 0);

		real_points = createMap(challengePlayers, this.tables.length);
		real_crashs = createMap(challengePlayers, this.tables.length);
		real_crashs_allgames = createMap(challengePlayers, this.tables.length);

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

			sum_pointsUnskaled = createMap(challengePlayers, 0.0);
			sum_basePoints = createMap(challengePlayers, 0.0);
			sum_crashs = createMap(challengePlayers, (Integer) 0);
			sum_moves = createMap(challengePlayers, (Integer) 0);

			for(String p : challengePlayers)
			{
				totalTable[challengePlayers.indexOf(p)][0] = playerToLink(p, true);
				for(int i = 1; i < totalTable[challengePlayers.indexOf(p)].length; i++)
				{
					totalTable[challengePlayers.indexOf(p)][i] = "-";
				}
			}

			for(int r = 1; r < pages[c].length; r++)
			{
				stats_games++;

				totalTableHead[r] = gameToLink(c, r);
				try
				{
					tables[c][r] = creategameTableFromLog(c, r, totalTable, challengePlayers, sum_pointsUnskaled, sum_basePoints, sum_crashs, sum_moves, total_sum_basePoints, total_sum_crashs,
							total_sum_moves, total_sum_pointsSkaled, total_sum_games, total_sum_bonus, total_sum_games_bychallenge);
				}
				catch(Exception e)
				{
					logger.info("----------------------------------------------");
					logger.info("----------------------------------------------");
					logger.info("----------------------------------------------");
					logger.info("ERROR for game " + c + "." + r);
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

		stats_gamesPerPlayerPerChallenge = intFromString(p.getProperty("games.per.player.per.challenge"));
		stats_gamesPerPlayer = stats_gamesPerPlayerPerChallenge * (pages.length - 1);

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
				// total_sum_games_bychallenge.get(player)[c] + "/" +
				// stats_gamesPerPlayerPerChallenge + ")</span>";
				if(stats_gamesPerPlayerPerChallenge - total_sum_games_bychallenge.get(player)[c] > 0)
				{
					tmp2 = "&nbsp;<span style=\"font-size:50%\">(" + (stats_gamesPerPlayerPerChallenge - total_sum_games_bychallenge.get(player)[c]) + ")</span>";
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
			finalTable[i][tables.length + 8] = "" + total_sum_games.get(player);
		}
		addFinalBonus(challengePlayers, finalTable, total_sum_basePoints, total_sum_crashs, total_sum_moves, final_sum_bonus);
		for(int i = 0; i < challengePlayers.size(); i++)
		{
			player = challengePlayers.get(i);
			total = round(total_sum_pointsSkaled.get(player) + total_sum_bonus.get(player) + final_sum_bonus.get(player));

			expected_old = round(total_sum_pointsSkaled.get(player) / total_sum_games.get(player) * stats_gamesPerPlayer);
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
	
	private void createWhoOnWho(int c, int r, List<String> challengePlayers)
	{
		List<String> gamePlayers = this.challenges[c].games[r].getPlayers()FromLog(c, r);
		int ci1, ci2;
		// String tmp;
		for(int i1 = 0; i1 < gamePlayers.size(); i1++)
		{
			for(int i2 = i1 + 1; i2 < gamePlayers.size(); i2++)
			{
				ci1 = challengePlayers.indexOf(gamePlayers.get(i1));
				ci2 = challengePlayers.indexOf(gamePlayers.get(i2));
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
				// gameToLink(c, r) + ")");
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
		List<String> challengePlayers = this.challenges[c].games[r].getPlayers()FromLog(1);
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

	private String[][] creategameTableFromLog(int c, int r, String[][] totalTable, List<String> challengePlayers, TreeMap<String, Double> sum_pointsUnskaled, TreeMap<String, Double> sum_basePoints,
			TreeMap<String, Integer> sum_crashs, TreeMap<String, Integer> sum_moves, TreeMap<String, Double> total_sum_basePoints, TreeMap<String, Integer> total_sum_crashs,
			TreeMap<String, Integer> total_sum_moves, TreeMap<String, Double> total_sum_pointsSkaled, TreeMap<String, Integer> total_sum_games, TreeMap<String, Integer> total_sum_bonus,
			TreeMap<String, Integer[]> total_sum_games_bychallenge)
	{
		String[][] table = new String[numberOfPlayers[c]][gameTableHead.length];
		int last = numberOfPlayers[c];
		int lastRankWithPoints = last;
		while(intFromString(p.getProperty("points." + numberOfPlayers[c] + "." + lastRankWithPoints)) == 0)
			lastRankWithPoints--;

		final TreeMap<String, Integer> playerMoves = new HashTreeMap<String, Integer>();
		final TreeMap<String, Integer> playerCrashs = new HashTreeMap<String, Integer>();
		final TreeMap<String, Integer> playerPosition = new HashTreeMap<String, Integer>();
		final TreeMap<String, Double> playerPoints = new HashTreeMap<String, Double>();
		int moves;
		int crashs;
		double points;
		int position;
		int crashsAfterLastRankWithoutPointsThrown;
		String log = getLog(c, r);

		List<String> players = this.challenges[c].games[r].getPlayers()FromLog(c, r);

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

			if(cccx == 1 && c == 9 && r == 24 && player.equals("sparrows bruder"))
			{
				crashs -= 15;
			}
			else if(cccx == 1 && c == 6 && r == 22 && player.equals("aristarch"))
			{
				crashs -= 1;
			}
			else if(cccx == 2 && c == 1 && r == 36 && player.equals("ImThinkin"))
			{
				crashs -= 60;
			}
			else if(cccx == 2 && c == 4 && r == 58 && player.equals("KingT"))
			{
				crashs -= 42;
			}
			else if(cccx >= 4 && gidProperties.containsKey(c + "." + r + "." + player))
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
			// if(crashsAfterLastRankWithoutPointsThrown > 0)
			// logger.info(" " + player + ": " + crashsAfterLastRankWithoutPointsThrown);

			position = log.lastIndexOf(player + " ");
			if(position == -1)
			{
				position = last--;
				points = -1;
			}
			else if(log.lastIndexOf(player + " wird von Didi aus dem Spiel geworfen") == position || log.lastIndexOf(player + " wird von KaroMAMA aus dem Spiel geworfen") == position
					|| log.lastIndexOf(player + " steigt aus dem Spiel aus") == position)
			{
				position = players.size();
				points = -players.size();
				moves = maxMoves + 1;
			}
			else if(log.lastIndexOf(player + " CRASHT!!!") == position && log.indexOf(player + " steigt aus dem Spiel aus") >= 0 && log.indexOf(player + " steigt aus dem Spiel aus") < position)
			{
				// Ausstiegs-Bug abfangen: Nach dem Ausstieg wurde gecrasht...
				// dann ist in der Zeile vor dem CRASH der Ausstieg
				position = players.size();
				points = -players.size();
				moves = maxMoves + 1;
			}
			else if(log.lastIndexOf(player + " wird") == position)
			{
				position = position + (player + " wird ").length();
				position = intFromString(log.substring(position, log.indexOf(".", position + 1)));
				if((lastRankWithoutPointsThrownIndex > 0) && (position == lastRankWithPoints) && (crashsAfterLastRankWithoutPointsThrown > 0))
					logger.info("    " + player + ": " + crashsAfterLastRankWithoutPointsThrown + " (" + crashs + ")");
				points = intFromString(p.getProperty("points." + numberOfPlayers[c] + "." + position));
				// points = intFromString(p.getProperty("points." + (numberOfPlayers[c]-thrown) +
				// "." + position));
			}
			else
			{
				position = last--;
				points = -1;
			}

			// store in maps for later use
			playerMoves.put(player, moves);
			playerCrashs.put(player, crashs);
			playerPoints.put(player, points);
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

		// check players for finishing in same round (only CCC6+)
		if(cccx > 5)
		{
			String player1, player2;
			List<String> playersWithSameAmountOfMoves;
			for(int i = 0; i < players.size(); i++)
			{
				player1 = players.get(i);
				moves = playerMoves.get(player1);
				points = playerPoints.get(player1);

				// Player has finished
				if(points != -1)
				{
					playersWithSameAmountOfMoves = new ArrayList<String>(players.size());
					playersWithSameAmountOfMoves.add(player1);

					double totalPoints = points;

					for(int j = i + 1; j < players.size(); j++)
					{
						player2 = players.get(j);
						// Check same amount of moves
						if(playerMoves.get(player2) == moves)
						{
							playersWithSameAmountOfMoves.add(player2);
							totalPoints += playerPoints.get(player2);
						}
					}

					if(playersWithSameAmountOfMoves.size() > 1)
					{
						i += (playersWithSameAmountOfMoves.size() - 1);
						points = totalPoints / playersWithSameAmountOfMoves.size();
						for(String player : playersWithSameAmountOfMoves)
						{
							playerPoints.put(player, points);
						}
						logger.info("  Challenge " + c + "." + r + " > Zuggleichheit (" + moves + " Z�ge, " + points + " Punkte): " + playersWithSameAmountOfMoves);
					}
				}
			}
		}

		// process maps
		for(String player : players)
		{
			moves = playerMoves.get(player);
			crashs = playerCrashs.get(player);
			points = playerPoints.get(player);
			position = playerPosition.get(player);

			if(points != -1)
			{
				real_points.get(player)[c].add(points);
				real_crashs.get(player)[c].add(crashs);
			}
			real_crashs_allgames.get(player)[c].add(crashs);

			fillgameTable(table, totalTable, player, challengePlayers, c, r, position, points, crashs, moves, sum_pointsUnskaled, sum_basePoints, sum_crashs, sum_moves, total_sum_basePoints,
					total_sum_crashs, total_sum_moves, total_sum_pointsSkaled, total_sum_games, total_sum_bonus, total_sum_games_bychallenge);
		}
		return table;
	}

	private String[][] creategameTable(int c, int r, String[][] totalTable, List<String> challengePlayers, TreeMap<String, Double> sum_pointsUnskaled, TreeMap<String, Double> sum_basePoints,
			TreeMap<String, Integer> sum_crashs, TreeMap<String, Integer> sum_moves, TreeMap<String, Double> total_sum_basePoints, TreeMap<String, Integer> total_sum_crashs,
			TreeMap<String, Integer> total_sum_moves, TreeMap<String, Double> total_sum_pointsSkaled, TreeMap<String, Integer> total_sum_games, TreeMap<String, Integer> total_sum_bonus,
			TreeMap<String, Integer[]> total_sum_games_bychallenge)
	{
		String[][] table = new String[numberOfPlayers[c]][gameTableHead.length];
		int last = numberOfPlayers[c];
		int index = 0;

		String player;
		String tmp;
		int moves;
		int crashs;
		int position;
		int points;
		String page = getPage(c, r);

		while(true)
		{
			index = page.indexOf(start_playerName, index + 1) + start_playerName.length();
			if(index == start_playerName.length() - 1)
				break;
			player = page.substring(index, page.indexOf(start_moves, index + 1));

			if(player.equals(p.getProperty("creator")))
				continue;

			index = page.indexOf(start_moves, index + 1) + start_moves.length();
			tmp = page.substring(index, page.indexOf(end_moves, index + 1));
			if(tmp.contains("Crash"))
			{
				moves = intFromString(tmp.substring(0, tmp.indexOf(" ")));
				crashs = intFromString(tmp.substring(tmp.indexOf("+") + 2, tmp.indexOf(" Crash")));
			}
			else if(tmp.contains("X"))
			{
				moves = 0;
				crashs = 0;
			}
			else
			{
				moves = intFromString(tmp);
				crashs = 0;
			}

			index = page.indexOf(end_moves, index + 1) + end_moves.length();
			if(page.charAt(index) == '<')
			{
				index = page.indexOf(">", index + 1) + 1;
			}
			tmp = page.substring(index, page.indexOf("<", index + 1));
			if(tmp.equals("kommt noch"))
				position = -1;
			else if(tmp.equals("rausgeworfen"))
				position = -1;
			else if(tmp.equals("war schon"))
				position = -1;
			else if(tmp.equals("dran"))
				position = -1;
			else if(tmp.startsWith("wurde"))
				position = intFromString(tmp.substring(tmp.indexOf(" ") + 1, tmp.indexOf(".")));
			else
			{
				position = -1;
				logger.info("unbekannter Status: " + tmp);
			}
			if(position < 0)
			{
				position = last--;
				points = -1;
			}
			else
			{
				points = intFromString(p.getProperty("points." + numberOfPlayers[c] + "." + position));
			}

			fillgameTable(table, totalTable, player, challengePlayers, c, r, position, points, crashs, moves, sum_pointsUnskaled, sum_basePoints, sum_crashs, sum_moves, total_sum_basePoints,
					total_sum_crashs, total_sum_moves, total_sum_pointsSkaled, total_sum_games, total_sum_bonus, total_sum_games_bychallenge);
		}
		return table;
	}

	private void fillgameTable(String[][] table, String totalTable[][], String player, List<String> challengePlayers, int c, int r, int position, double points, int crashs, int moves,
			TreeMap<String, Double> sum_pointsUnskaled, TreeMap<String, Double> sum_basePoints, TreeMap<String, Integer> sum_crashs, TreeMap<String, Integer> sum_moves,
			TreeMap<String, Double> total_sum_basePoints, TreeMap<String, Integer> total_sum_crashs, TreeMap<String, Integer> total_sum_moves, TreeMap<String, Double> total_sum_pointsSkaled,
			TreeMap<String, Integer> total_sum_games, TreeMap<String, Integer> total_sum_bonus, TreeMap<String, Integer[]> total_sum_games_bychallenge)
	{

		String gamePoints = (points < 0 ? (points == -1 ? "?" : "" + round(points)) : "" + round(crashs * points));

		while(table[position - 1][0] != null)
		{
			position--;
		}
		table[position - 1][0] = position + ".";
		table[position - 1][1] = playerToLink(player, false);
		table[position - 1][2] = (points == -1 ? "?" : "" + round(points));
		table[position - 1][3] = "" + crashs;
		table[position - 1][4] = "" + moves;
		table[position - 1][5] = gamePoints;

		totalTable[challengePlayers.indexOf(player)][r] = gamePoints;

		sum_pointsUnskaled.put(player, sum_pointsUnskaled.get(player) + (points < 0 ? (points == -1 ? 0 : points) : (crashs * points)));
		if(points != -1)
		{
			sum_basePoints.put(player, sum_basePoints.get(player) + points);
			total_sum_basePoints.put(player, total_sum_basePoints.get(player) + points);
			total_sum_games.put(player, total_sum_games.get(player) + 1);
			total_sum_games_bychallenge.get(player)[c] += 1;
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

	private double addBonus(int c, List<String> challengePlayers, String[][] totalTable, TreeMap<String, Double> sum_pointsUnskaled, TreeMap<String, Double> sum_basePoints,
			TreeMap<String, Integer> sum_crashs, TreeMap<String, Integer> sum_moves, TreeMap<String, Integer> total_sum_bonus)
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

		addBonus(c, index_minMoves, pages[c].length + 2, totalTable, challengePlayers, total_sum_bonus);
		addBonus(c, index_maxCrashs, pages[c].length + 1, totalTable, challengePlayers, total_sum_bonus);
		addBonus(c, index_maxBasePoints, pages[c].length + 0, totalTable, challengePlayers, total_sum_bonus);
		// addBonus(c, index_maxPointsUnskaled, totalTable, challengePlayers,
		// total_sum_bonus);

		return maxPointsUnskaled;
	}

	private void addBonus(int c, List<Integer> indexes, int column, String[][] totalTable, List<String> challengePlayers, TreeMap<String, Integer> total_sum_bonus)
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

	private void addFinalBonus(List<String> challengePlayers, String[][] finalTable, TreeMap<String, Double> total_sum_basePoints, TreeMap<String, Integer> total_sum_crashs,
			TreeMap<String, Integer> total_sum_moves, TreeMap<String, Integer> final_sum_bonus)
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

	private void addFinalBonus(List<Integer> indexes, int column, String[][] finalTable, List<String> challengePlayers, TreeMap<String, Integer> final_sum_bonus)
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
		expected_points = createMap(challengePlayers, new Double[tables.length], 0.0);
		expected_total_points = createMap(challengePlayers, 0.0);

		double crash_count, crash_players;
		double crash_allgames_count, crash_allgames_players;
		double avg_crashs;
		double avg_crashs_allgames;
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
			crash_allgames_count = 0;
			crash_allgames_players = 0;
			for(String player : challengePlayers)
			{
				for(int crashs : real_crashs.get(player)[c])
				{
					crash_count += crashs;
					crash_players++;
				}
				for(int crashs : real_crashs_allgames.get(player)[c])
				{
					crash_allgames_count += crashs;
					crash_allgames_players++;
				}
			}
			avg_crashs_allgames = crash_allgames_count / crash_allgames_players;
			if(crash_players != 0)
				avg_crashs = crash_count / crash_players;
			else
				avg_crashs = avg_crashs_allgames;

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

				if(real_points.get(player)[c].size() == stats_gamesPerPlayerPerChallenge)
				{
					// spieler hat alle Rennen beendet
					expected = actual_positive + actual_negative;
				}
				else if(real_points.get(player)[c].size() - negative_count > 0)
				{
					// spieler hat ein paar Rennen regulaer beendet (ohne rauswurf)
					// eigene durchschnittliche Punkte und Crashs verwenden
					expected = actual_positive + actual_negative;
					expected += player_avg_crashs * player_avg_points * (stats_gamesPerPlayerPerChallenge - real_points.get(player)[c].size());
					// expected = player_avg_crashs * player_avg_points *
					// (stats_gamesPerPlayerPerChallenge - negative_count ) + expected_negative;
				}
				else
				{
					// spieler hat noch keine Rennen beendet regulaer beendet
					// allgemeine durchschnittliche Punkte und Crashs verwenden
					expected = actual_positive + actual_negative;
					expected += avg_crashs * avg_points * (stats_gamesPerPlayerPerChallenge - real_points.get(player)[c].size());
					// expected = avg_crashs * avg_points * stats_gamesPerPlayerPerChallenge;
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

	protected class FinalTableRowSorter implements Comparator<Object[]>
	{
		private int mainColumnOffset;

		public FinalTableRowSorter(int mainColumnOffset)
		{
			this.mainColumnOffset = mainColumnOffset;
		}

		@Override
		public int compare(Object[] o1, Object[] o2)
		{
			// punkte
			double val1 = (double) o1[o1.length + mainColumnOffset];
			double val2 = (double) o2[o2.length + mainColumnOffset];
			if(val2 != val1)
				return (int) Math.signum(val2 - val1);

			// mehr crashs
			int c1 = (int) o1[o1.length - 9];
			int c2 = (int) o2[o2.length - 9];
			if(c2 != c1)
				return (int) Math.signum(c2 - c1);

			// mehr grundpunkte
			double b1 = (double) o1[o1.length - 10];
			double b2 = (double) o2[o2.length - 10];
			if(b2 != b1)
				return (int) Math.signum(b2 - b1);

			// weniger Züge
			int m1 = (int) o1[o1.length - 8];
			int m2 = (int) o2[o2.length - 8];
			if(m2 != m1)
				return (int) Math.signum(m1 - m2);

			return 0;
		}
	}

	protected <K, V> TreeMap<K, V> createMap(List<K> keys, V defaultValue)
	{
		TreeMap<K, V> map = new TreeMap<>();
		for(K k : keys)
			map.put(k, defaultValue);
		return map;
	}

	protected <K, V> TreeMap<K, List<V>[]> createMap(List<K> keys, int arraySize)
	{
		TreeMap<K, List<V>[]> map = new TreeMap<>();
		List<?>[] array;
		List<V> list;
		for(K k : keys)
		{
			array = new ArrayList<?>[arraySize];
			for(int i = 0; i < arraySize; i++)
			{
				list = new ArrayList<V>();
				array[i] = list;
			}
			map.put(k, (List<V>[]) array);
		}
		return map;
	}

	protected <K, V> TreeMap<K, V[]> createMap(List<K> keys, V[] array, V defaultValue)
	{
		TreeMap<K, V[]> map = new TreeMap<>();
		for(K k : keys)
		{
			V[] arrayClone = array.clone();
			if(defaultValue != null)
				for(int i = 0; i < arrayClone.length; i++)
					arrayClone[i] = defaultValue;
			map.put(k, arrayClone);
		}
		return map;
	}

	protected Rules getRules(int challenge)
	{
		return this.gameSeries.getRulesByKey().get("" + challenge);
	}

	protected Map getMap(int challenge)
	{
		return this.gameSeries.getMapsByKey().get("" + challenge).get(0);
	}

	protected PlannedGame getGame(int challenge, int game)
	{
		return this.gameSeries.getGames().get(GAMES_KEY).get(this.challengeOffsets[challenge] + game);
	}

	protected String mapToLink(int challenge, boolean includeName)
	{
		return WikiUtil.createLink(getMap(challenge), includeName);
	}

	protected String gameToLink(int challenge, int game)
	{
		return WikiUtil.createLink(getGame(challenge, game), (challenge + 1) + "." + (game + 1));
	}

	protected String challengeToLink(int challenge, boolean text)
	{
		return WikiUtil.createLink("CraZZZy Crash Challenge " + this.cccx + " - Detailwertung Challenge " + challenge, (text ? "Challenge " : "") + challenge);
	}
}
