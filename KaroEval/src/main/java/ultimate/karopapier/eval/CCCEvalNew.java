package ultimate.karopapier.eval;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.enums.EnumPlayerStatus;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.CollectionsUtil;
import ultimate.karopapier.utils.Table;
import ultimate.karopapier.utils.WikiUtil;

public class CCCEvalNew extends Eval<GameSeries>
{
	protected static final String				GAMES_KEY				= "Balanced";
	protected static final String[]				TABLE_HEAD_MAPS			= new String[] { "Nr.", "Strecke", "Spielerzahl", "ZZZ", "CPs", "Spielzahl" };
	protected static final int					METRICS_GAME_MAXMOVES	= 0;

	protected static final String				STATUS_RACING			= "&#127950;";																	// race car
	protected static final String				STATUS_FINISHED			= "&#127937;";																	// race flag
	protected static final String				STATUS_LEFT				= "&#128128;";																	// skull
	protected static final String				STATUS_FORBIDDEN		= "&#128683;";																	// forbidden

	// TODO read from file
	protected static final int					CLUSTER_SIZE			= 6;
	protected static final double				MAX_CHALLENGE_POINTS	= 5;
	protected static final boolean				APPLY_SQRT				= true;
	protected static final boolean				CAP_NEGATIVE			= true;

	protected int								cccx;
	// stats & metrics
	protected int								stats_challengesTotal;
	protected int								stats_challengesCreated;
	protected int								stats_gamesPerPlayer;
	protected int								stats_gamesPerPlayerPerChallenge;
	protected int								stats_gamesTotal;
	protected int								stats_gamesCreated;
	protected int								stats_players;
	protected UserStats							totalStats;
	protected UserStats[]						challengeStats;
	protected TreeMap<Integer, UserStats>		userStats;
	protected TreeMap<Integer, UserStats>[]		userChallengeStats;
	protected TreeMap<Integer, UserStats>[][]	userGameStats;
	protected double[][]						challengeMetrics;
	protected double[][][]						gameMetrics;
	// helpers
	protected Integer[]							challengeGames;
	protected Integer[]							challengeOffsets;
	protected List<User>						usersByLogin;
	// evaluation
	protected Table[]							totalTables;
	protected Table								finalTable;
	protected Table								whoOnWho;

	public CCCEvalNew(int cccx)
	{
		this.cccx = cccx;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void prepare(KaroAPICache karoAPICache, GameSeries gameSeries, Properties properties, File folder, int execution)
	{
		super.prepare(karoAPICache, gameSeries, properties, folder, execution);

		if(gameSeries.getType() != EnumGameSeriesType.Balanced)
			return;

		// since all information is stored in the gameseries or the cron-properties, we don't need the gid-properties any more
		// but we init some other values for convenience and later usage

		// stats
		this.stats_challengesTotal = (int) gameSeries.get(GameSeries.NUMBER_OF_MAPS);
		this.stats_players = gameSeries.getPlayers().size();
		this.stats_gamesTotal = gameSeries.getGames().get(GAMES_KEY).size();
		this.stats_gamesPerPlayerPerChallenge = this.getRules(0).getGamesPerPlayer();
		this.stats_gamesPerPlayer = this.stats_gamesPerPlayerPerChallenge * this.stats_challengesTotal;
		// users
		this.usersByLogin = gameSeries.getPlayers();
		this.usersByLogin.sort((u1, u2) -> {
			return u1.getLoginLowerCase().compareTo(u2.getLoginLowerCase());
		});
		// all games are stored in 1 list - to be able to access them easier we calculate offsets
		this.challengeGames = new Integer[this.stats_challengesTotal];
		this.challengeOffsets = new Integer[this.stats_challengesTotal];
		this.challengeMetrics = new double[this.stats_challengesTotal][];
		this.gameMetrics = new double[this.stats_challengesTotal][][];
		this.stats_challengesCreated = 0;
		this.stats_gamesCreated = 0;
		boolean allCreated;
		int offset = 0;
		List<PlannedGame> games = gameSeries.getGames().get(GAMES_KEY);
		for(int c = 0; c < this.stats_challengesTotal; c++)
		{
			this.challengeOffsets[c] = offset;
			this.challengeGames[c] = this.stats_gamesPerPlayerPerChallenge * this.stats_players / this.getRules(c).getNumberOfPlayers();
			this.gameMetrics[c] = new double[this.challengeGames[c]][];
			// check if all games in this challenge have been created
			allCreated = true;
			for(int g = 0; g < this.challengeGames[c]; g++)
			{
				if(games.get(offset + g).getGame() != null)
					this.stats_gamesCreated++;
				else
					allCreated = false;
			}
			if(allCreated)
				this.stats_challengesCreated++;
			offset += this.challengeGames[c];
		}

		// create the header for the final table
		String[] finalTableHead = new String[this.stats_challengesCreated + 7];
		int col = 0;
		finalTableHead[col++] = "Platz";
		finalTableHead[col++] = "Spieler";
		for(int c = 0; c < this.stats_challengesCreated; c++)
			finalTableHead[col++] = challengeToLink(c, false);
		finalTableHead[col++] = "Züge (gesamt)";
		finalTableHead[col++] = "Crashs (gesamt)";
		finalTableHead[col++] = "Endergebnis";
		finalTableHead[col++] = "Abgeschlossene Rennen";
		finalTableHead[col++] = "Erwartungswert";

		// init variables
		this.totalTables = new Table[stats_challengesTotal];
		this.finalTable = new Table(finalTableHead);
		this.totalStats = new UserStats(0);
		this.userStats = createStatsMap(usersByLogin);
		this.challengeStats = new UserStats[stats_challengesTotal];
		this.userChallengeStats = (TreeMap<Integer, UserStats>[]) new TreeMap[stats_challengesTotal];
		this.userGameStats = (TreeMap<Integer, UserStats>[][]) new TreeMap[stats_challengesTotal][];
		for(int c = 0; c < this.stats_challengesTotal; c++)
		{
			this.challengeStats[c] = new UserStats(0);
			this.userChallengeStats[c] = createStatsMap(usersByLogin);
			this.userGameStats[c] = (TreeMap<Integer, UserStats>[]) new TreeMap[challengeGames[c]];
			for(int g = 0; g < challengeGames[c]; g++)
				this.userGameStats[c][g] = createStatsMap(usersByLogin);
		}

		// init the whoOnWho
		Object[] whoOnWhoHead = new Object[this.stats_players + 1];
		this.whoOnWho = new Table(whoOnWhoHead.length);
		this.whoOnWho.addRow(whoOnWhoHead); // don't add this as a header, but as the first row
		col = 1; // leave first column empty
		Object[] row;
		for(User user : usersByLogin)
		{
			if(user == data.getCreator())
				continue;
			this.whoOnWho.setValue(0, col++, user);
			row = new Object[whoOnWhoHead.length];
			row[0] = user;
			for(int ci = 1; ci < whoOnWhoHead.length; ci++)
			{
				if(ci == this.whoOnWho.getRows().size())
					row[ci] = "-";
				else
					row[ci] = 0;
			}
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

	public List<File> evaluate() throws IOException, InterruptedException
	{
		long start;

		logger.info("creating tables... ");
		start = System.currentTimeMillis();
		boolean finished = createTables();
		logger.info("OK (" + (System.currentTimeMillis() - start) + ")");

		logger.info("reading schema... ");
		start = System.currentTimeMillis();
		String schema = readFile("czzzcc" + cccx + "-schema.txt");
		logger.info("OK (" + (System.currentTimeMillis() - start) + ")");

		logger.info("creating WIKI... ");
		start = System.currentTimeMillis();
		List<File> filesUpdated = createWiki(schema, finished);
		logger.info("OK (" + (System.currentTimeMillis() - start) + ")");

		if(logger.isDebugEnabled())
		{
			String wiki = readFile("czzzcc" + cccx + "-wiki-overview.txt");
			logger.debug("-----------------------------------------------------------------------------");
			logger.debug("-----------------------------------------------------------------------------");
			logger.debug("\r\n" + wiki);
		}

		return filesUpdated;
	}

	protected List<File> createWiki(String schema, boolean finished) throws IOException
	{
		List<File> filesUpdated = new LinkedList<>();

		StringBuilder detail = new StringBuilder();
		StringBuilder detailLinks = new StringBuilder();

		Table tableTable;
		Table mapTable = new Table(TABLE_HEAD_MAPS);
		Rules rules;
		Object[] row;
		PlannedGame game;
		for(int c = 0; c < stats_challengesCreated; c++)
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
			detail.append("= Challenge " + (c + 1) + " =\r\n");
			detail.append("Strecke: " + mapToLink(c, false) + "\r\n");

			// detail.append("== Rennergebnisse ==\r\n");
			// tableTable = new Table(TABLE_TABLE_COLUMNS);
			// for(int g = 0; g < tables[c].length; g++)
			// {
			// if(g % TABLE_TABLE_COLUMNS == 0)
			// {
			// if(g != 0)
			// tableTable.addRow(row);
			// row = new Object[TABLE_TABLE_COLUMNS];
			// }
			// game = getGame(c, g);
			// if(game.getGame() == null)
			// continue;
			// row[g % TABLE_TABLE_COLUMNS] = "\r\nChallenge " + gameToLink(c, g) + "\r\n" + WikiUtil.toString(tables[c][g], null) + "\r\n";
			// }
			// tableTable.addRow(row);
			// detail.append(WikiUtil.toString(tableTable, null));
			// detail.append("\r\n");

			detail.append("== Tabellarische Auswertung ==\r\n");
			detail.append(WikiUtil.toString(totalTables[c], "alignedright sortable", ""));
			detail.append("\r\n");

			filesUpdated.add(writeFile("czzzcc" + cccx + "-wiki-challenge" + (c + 1) + ".txt", detail.toString()));
			detailLinks.append("*" + challengeToLink(c, true) + "\r\n");
		}

		StringBuilder total = new StringBuilder();

		if(finished) // without "Abgeschlossene Rennen" & "Erwartungswert"
			total.append(WikiUtil.toString(finalTable, "alignedright sortable", WikiUtil.getDefaultColumnConfig(finalTable.getColumns() - 2)));
		else
			total.append(WikiUtil.toString(finalTable, "alignedright sortable", WikiUtil.getDefaultColumnConfig(finalTable.getColumns())));
		//

		StringBuilder stats = new StringBuilder();

		stats.append("== Zahlen & Fakten ==\r\n");
		stats.append("*Rennen insgesamt: '''" + stats_gamesTotal + "'''\r\n");
		stats.append("*Teilnehmer: '''" + stats_players + "'''\r\n");
		stats.append("*Rennen pro Spieler: '''" + stats_gamesPerPlayer + "'''\r\n");
		stats.append("*Rennen pro Spieler pro Challenge: '''" + stats_gamesPerPlayerPerChallenge + "'''\r\n");
		stats.append("*Züge insgesamt: '''" + totalStats.moves + "'''\r\n");
		stats.append("*Crashs insgesamt: '''" + totalStats.crashs + "'''\r\n");
		stats.append("*Häufigste Begegnung: " + getMaxMinWhoOnWho("max") + "\r\n");
		stats.append("*Seltenste Begegnung: " + getMaxMinWhoOnWho("min") + "\r\n");

		/*
		 * stats.append("== Wer gegen wen? ==\r\n");
		 * stats.append("Eigentlich wollte ich hier noch die ganzen Links zu den Spielen reinschreiben, aber damit kam das Wiki nicht klar! Daher hier nur die Anzahl...\r\n");
		 * stats.append(WikiUtil.toString(whoOnWho, null));
		 */

		String s = new String(schema);
		s = s.replace("${MAPS}", WikiUtil.toString(mapTable, null));
		s = s.replace("${DETAIL}", detailLinks.toString());
		s = s.replace("${TOTAL}", total.toString());
		s = s.replace("${STATS}", stats.toString());

		filesUpdated.add(writeFile("czzzcc" + cccx + "-wiki-overview.txt", s.toString()));

		return filesUpdated;
	}

	protected boolean createTables()
	{
		for(int c = 0; c < this.stats_challengesCreated; c++)
			createTables(c);

		calculateExpected();

		UserStats mins = getMins(userStats.values());
		UserStats maxs = getMaxs(userStats.values());

		logger.debug("              min\t| max: ");
		logger.debug("  finished  = " + mins.finished + "\t| " + maxs.finished);
		logger.debug("  left      = " + mins.left + "\t| " + maxs.left);
		logger.debug("  moves     = " + mins.moves + "\t| " + maxs.moves);
		logger.debug("  crashs    = " + mins.crashs + "\t| " + maxs.crashs);
		logger.debug("  total     = " + mins.total + "\t| " + maxs.total);
		logger.debug("  expected  = " + mins.expected + "\t| " + maxs.expected);

		boolean finished = true;
		UserStats us = null;
		User user;
		List<Entry<Integer, UserStats>> userStatsList = new LinkedList<>(userStats.entrySet());

		for(Entry<Integer, UserStats> use : userStatsList)
		{
			us = use.getValue();
			user = karoAPICache.getUser(use.getKey());

			for(int c = 0; c < stats_challengesCreated; c++)
			{
				if(userChallengeStats[c].get(user.getId()).finished < stats_gamesPerPlayerPerChallenge)
					finished = false;
			}
		}

		Collections.sort(userStatsList, new FinalTableSorter(finished));

		Object[] row;
		int col;
		String rank;
		int rows = 0;
		UserStats usLast;
		for(Entry<Integer, UserStats> use : userStatsList)
		{
			usLast = us;
			us = use.getValue();
			user = karoAPICache.getUser(use.getKey());

			if(rows > 0 && (finished ? us.total == usLast.total : us.expected == usLast.expected) && us.crashs == usLast.crashs && us.moves == usLast.moves)
				rank = " ";
			else
				rank = (rows + 1) + ".";

			row = new Object[finalTable.getColumns()];
			col = 0;
			row[col++] = rank;
			row[col++] = user;
			for(int c = 0; c < stats_challengesCreated; c++)
			{
				row[col] = userChallengeStats[c].get(user.getId()).total;

				if(userChallengeStats[c].get(user.getId()).total >= MAX_CHALLENGE_POINTS)
					row[col] = WikiUtil.highlight(WikiUtil.preprocess(row[col]));

				if(userChallengeStats[c].get(user.getId()).finished < stats_gamesPerPlayerPerChallenge)
				{
					row[col] = WikiUtil.preprocess(row[col]) + "&nbsp;<span style=\"font-size:50%\">(" + (stats_gamesPerPlayerPerChallenge - userChallengeStats[c].get(user.getId()).finished) + ")</span>";
					finished = false;
				}

				col++;
			}
			row[col++] = us.moves;
			row[col++] = us.crashs;
			row[col++] = us.total;
			row[col++] = us.finished;
			row[col++] = us.expected;

			finalTable.addRow(row);

			// set highlights
			finalTable.setHighlight(rows, 1, true);
			finalTable.setHighlight(rows, stats_challengesCreated + 2, us.moves == mins.moves); // moves
			finalTable.setHighlight(rows, stats_challengesCreated + 3, us.crashs == maxs.crashs); // crashs
			finalTable.setHighlight(rows, stats_challengesCreated + 4, true); // total
			finalTable.setHighlight(rows, stats_challengesCreated + 5, false); // finished
			finalTable.setHighlight(rows, stats_challengesCreated + 6, true); // expected

			rows++;
		}

		logger.info("finished=" + finished);

		return finished;
	}

	protected void createTables(int c)
	{
		int COLS_PER_RACE = 3;
		logger.info("creating tables for challenge #" + (c + 1));

		// init the total table for this challenge
		String[] totalTableHead1 = new String[challengeGames[c] * COLS_PER_RACE + 6];
		String[] totalTableHead2 = new String[challengeGames[c] * COLS_PER_RACE + 6];
		int col = 0;

		totalTableHead2[col++] = "Spieler";

		for(int g = 0; g < challengeGames[c]; g++)
		{
			totalTableHead1[col] = gameToLink(c, g);
			totalTableHead2[col] = "Z";
			col++;
			totalTableHead1[col] = "";
			totalTableHead2[col] = "C";
			col++;
			totalTableHead1[col] = "";
			totalTableHead2[col] = "S";
			col++;
		}

		totalTableHead1[col] = "Züge";
		totalTableHead2[col++] = "Gesamt";
		totalTableHead2[col++] = "Punkte";

		totalTableHead1[col] = "Crashs";
		totalTableHead2[col++] = "Gesamt";
		totalTableHead2[col++] = "Punkte";

		totalTableHead2[col++] = "Gesamtergebnis";

		totalTables[c] = new Table(totalTableHead1, totalTableHead2);

		for(int g = 0; g < challengeGames[c]; g++)
			totalTables[c].getHeaders().get(0)[g * COLS_PER_RACE + 1].colspan = COLS_PER_RACE;
		totalTables[c].getHeaders().get(0)[challengeGames[c] * COLS_PER_RACE + 1].colspan = 2;
		totalTables[c].getHeaders().get(0)[challengeGames[c] * COLS_PER_RACE + 3].colspan = 2;

		calcMetrics(c);

		for(int g = 0; g < challengeGames[c]; g++)
		{
			calcMetrics(c, g);
			updateWhoOnWho(c, g);
		}

		Object[] row;
		Player player;
		int moves, crashs;
		String status;
		boolean finished, left, forbidden;
		for(User user : usersByLogin)
		{
			status = STATUS_RACING;

			row = new Object[totalTables[c].getColumns()];
			col = 0;
			row[col++] = user;

			for(int g = 0; g < challengeGames[c]; g++)
			{
				finished = false;
				left = false;
				forbidden = false;

				Game game = getGame(c, g).getGame();
				player = null;
				for(Player p : game.getPlayers())
				{
					if(p.getName().equals(user.getLogin()))
					{
						player = p;
						break;
					}
				}
				if(player != null)
				{
					crashs = getCrashs(c, g, game, player);

					if((player.getStatus() == EnumPlayerStatus.ok && player.getRank() > 0) || (player.getStatus() != EnumPlayerStatus.ok))
					{
						finished = true;

						if(player.getStatus() != EnumPlayerStatus.ok)
						{
							left = true;
							status = STATUS_LEFT;
						}
						else if(crashs == 0)
						{
							forbidden = true;
							status = STATUS_FORBIDDEN;
						}
						else
						{
							status = STATUS_FINISHED;
						}
					}

					moves = getMoves(c, g, game, player, forbidden);

					row[col++] = moves;
					row[col++] = crashs;
					row[col++] = status;

					userGameStats[c][g].get(user.getId()).moves = moves;
					userGameStats[c][g].get(user.getId()).crashs = crashs;
					userGameStats[c][g].get(user.getId()).finished += (finished ? 1 : 0);
					userGameStats[c][g].get(user.getId()).left += (left ? 1 : 0);

					userChallengeStats[c].get(user.getId()).moves += moves;
					userChallengeStats[c].get(user.getId()).crashs += crashs;
					userChallengeStats[c].get(user.getId()).finished += (finished ? 1 : 0);
					userChallengeStats[c].get(user.getId()).left += (left ? 1 : 0);

					userStats.get(user.getId()).moves += moves;
					userStats.get(user.getId()).crashs += crashs;
					userStats.get(user.getId()).finished += (finished ? 1 : 0);
					userStats.get(user.getId()).left += (left ? 1 : 0);
				}
				else
				{
					row[col++] = "-";
					row[col++] = "-";
					row[col++] = "-";
				}
			}

			row[col++] = userChallengeStats[c].get(user.getId()).moves;
			row[col++] = "?"; // movesPoints
			row[col++] = userChallengeStats[c].get(user.getId()).crashs;
			row[col++] = "?"; // crashPoints
			row[col++] = "?"; // totalPoints

			totalTables[c].addRow(row);
		}

		// apply points by moves
		assignPoints(totalTables[c], challengeGames[c] * COLS_PER_RACE + 1, challengeGames[c] * COLS_PER_RACE + 2, CollectionsUtil.ASCENDING);
		// apply points by crashs
		assignPoints(totalTables[c], challengeGames[c] * COLS_PER_RACE + 3, challengeGames[c] * COLS_PER_RACE + 4, CollectionsUtil.DESCENDING);
		// calculate product
		int movesPoints, crashPoints;
		double totalPoints;
		User user;
		for(int r = 0; r < totalTables[c].getRows().size(); r++)
		{
			user = (User) totalTables[c].getValue(r, 0);
			movesPoints = (int) totalTables[c].getValue(r, challengeGames[c] * COLS_PER_RACE + 2);
			crashPoints = (int) totalTables[c].getValue(r, challengeGames[c] * COLS_PER_RACE + 4);

			totalPoints = calculatePoints(movesPoints, crashPoints, 0);

			totalTables[c].setValue(r, totalTables[c].getColumns() - 1, totalPoints);

			userChallengeStats[c].get(user.getId()).total = totalPoints;
			userStats.get(user.getId()).total += totalPoints;
		}
		// resort by name (was resorted in assignPoints)
		totalTables[c].sort(0, (Comparator<User>) (m1, m2) -> {
			return m1.getLoginLowerCase().compareTo(m2.getLoginLowerCase());
		});
		// // sort by total points
		// totalTables[c].sort(totalTables[c].getColumns() - 1, (Comparator<Double>) (m1, m2) -> {
		// return (int) Math.signum(m2 - m1);
		// });
	}

	protected void assignPoints(Table table, int valueColumn, int pointColumn, int sortMode)
	{
		// sort
		if(sortMode == CollectionsUtil.DESCENDING)
			table.sort(valueColumn, (Comparator<Integer>) (m1, m2) -> {
				return m2 - m1;
			});
		else
			table.sort(valueColumn, (Comparator<Integer>) (m1, m2) -> {
				return m1 - m2;
			});

		// apply points / clustering
		int rank = stats_players;
		int maxPoints = (int) Math.ceil(rank / (double) CLUSTER_SIZE);
		int currentValue, previousValue = 0;
		int currentPoints, previousPoints = 0;
		for(int r = 0; r < table.getRows().size(); r++)
		{
			currentValue = (int) table.getValue(r, valueColumn);
			currentPoints = (int) Math.ceil(rank / (double) CLUSTER_SIZE);

			if(currentValue == previousValue)
				currentPoints = previousPoints;

			table.setValue(r, pointColumn, currentPoints);

			if(currentPoints == maxPoints)
			{
				// add colors / highlight
				table.setHighlight(r, valueColumn, true);
				table.setHighlight(r, pointColumn, true);
			}

			previousValue = currentValue;
			previousPoints = currentPoints;
			rank--;
		}
	}

	protected int getCrashs(int c, int g, Game game, Player player)
	{
		// crashs --> need to count them, because of possible duplicates
		int crashs = 0;
		for(int i = 0; i < player.getMoves().size(); i++)
		{
			if(!player.getMoves().get(i).isCrash()) // not a crash
				continue;
			if(player.getMoves().get(i - 1).isCrash() && player.getMoves().get(i).getT().equals(player.getMoves().get(i - 1).getT())) // doppel-crash
				continue;
			crashs++;
		}
		String key = (c + 1) + "." + (g + 1) + "." + player.getName();
		if(properties.containsKey(key))
		{
			String s = properties.getProperty(key);
			logger.info("correcting crash count " + crashs + " for " + key + " by " + s);
			crashs += parseInt(s);
		}
		return crashs;
	}

	protected int getMoves(int c, int g, Game game, Player player, boolean forbidden)
	{
		// moves
		int moves = 0;
		if(player.getStatus() == EnumPlayerStatus.ok && player.getRank() == 0)
			moves = player.getMoveCount(); // still racing
		else if(player.getStatus() == EnumPlayerStatus.ok)
			moves = player.getMoveCount() - 1; // parc ferme
		else
			moves = (int) (this.gameMetrics[c][g][METRICS_GAME_MAXMOVES] + 1); // kicked or left
		return moves;
	}

	protected double calculatePoints(double movesPoints, double crashPoints, double offset)
	{
		double points = movesPoints * crashPoints + offset;
		if(APPLY_SQRT)
			points = Math.sqrt(points);
		if(CAP_NEGATIVE && points < 0)
			return 0;
		return points;
	}

	protected void calcMetrics(int c)
	{
		// for inheritence
		this.challengeMetrics[c] = new double[0];
	}

	protected void calcMetrics(int c, int g)
	{
		// for inheritence
		this.gameMetrics[c][g] = new double[1];

		int moves;
		for(Player p : getGame(c, g).getGame().getPlayers())
		{
			moves = p.getMoveCount();
			if(p.getRank() > 0)
				moves--; // parf ferme

			if(moves > this.gameMetrics[c][g][METRICS_GAME_MAXMOVES])
				this.gameMetrics[c][g][METRICS_GAME_MAXMOVES] = moves;
		}
	}

	protected void updateWhoOnWho(int c, int g)
	{
		PlannedGame game = getGame(c, g);
		if(game.getGame() == null)
			return;

		int value;
		int u = 1;
		User user2;
		for(User user1 : usersByLogin)
		{
			if(game.getPlayers().contains(user1))
			{
				for(int col = 1; col < u; col++)
				{
					user2 = (User) whoOnWho.getValue(0, col);
					if(!game.getPlayers().contains(user2))
						continue;

					// logger.debug(user1.getLogin() + " vs. " + user2.getLogin() + " u=" + u + ", col=" + col);
					value = ((int) whoOnWho.getValue(u, col)) + 1;
					whoOnWho.setValue(u, col, value);
					whoOnWho.setValue(col, u, value);
				}
			}
			u++;
		}
	}

	protected String getMaxMinWhoOnWho(String type)
	{
		List<Object[]> maxMinList = new LinkedList<Object[]>();
		int maxMin = (type.equals("max") ? Integer.MIN_VALUE : Integer.MAX_VALUE);
		int val;
		Object[] pair;
		for(int ci = 1; ci < whoOnWho.getColumns(); ci++)
		{
			for(int ri = ci + 1; ri < whoOnWho.getRows().size(); ri++)
			{
				val = (int) whoOnWho.getValue(ri, ci);
				pair = new Object[] { whoOnWho.getValue(0, ci), whoOnWho.getValue(ri, 0) };
				if(type.equals("max"))
				{
					if(val > maxMin)
					{
						maxMinList.clear();
						maxMinList.add(pair);
						maxMin = val;
					}
					else if(val == maxMin)
					{
						maxMinList.add(pair);
						maxMin = val;
					}
				}
				else
				{
					if(val < maxMin)
					{
						maxMinList.clear();
						maxMinList.add(pair);
						maxMin = val;
					}
					else if(val == maxMin)
					{
						maxMinList.add(pair);
						maxMin = val;
					}
				}
			}
		}
		StringBuilder ret = new StringBuilder();
		ret.append(maxMin + " mal ");
		for(int i = 0; i < maxMinList.size(); i++)
		{
			if(i > 0 && i < maxMinList.size() - 1)
				ret.append(", ");
			else if(i > 0 && i == maxMinList.size() - 1)
				ret.append(" und ");
			ret.append(WikiUtil.createLink((User) maxMinList.get(i)[0]));
			ret.append(" '''vs.''' ");
			ret.append(WikiUtil.createLink((User) maxMinList.get(i)[1]));
		}
		return ret.toString();
	}

	protected void calculateExpected()
	{
		double maxPoints = (int) Math.ceil(stats_players / (double) CLUSTER_SIZE);
		double midPoints = (maxPoints - 1) / 2.0 + 1;

		double playerAvgMovesInFinishedGames, playerAvgCrashsInFinishedGames;
		double expectedMovesPoints, expectedCrashPoints;
		double relativeMovesPosition, relativeCrashPosition, expected;

		UserStats ugs, mins, maxs;
		List<UserStats> allFinishedStats;

		for(int c = 0; c < this.stats_challengesCreated; c++)
		{
			logger.debug("calculating expected for challenge #" + (c + 1));

			// calculate what the best and worst player needed to finish a race
			allFinishedStats = new LinkedList<>();
			for(int g = 0; g < challengeGames[c]; g++)
				allFinishedStats.addAll(userGameStats[c][g].values());
			allFinishedStats.removeIf(us -> {
				return us.finished == 0;
			});
			mins = getMins(allFinishedStats);
			maxs = getMaxs(allFinishedStats);

			logger.debug(" - moves:   min=" + mins.moves + ", max=" + maxs.moves);
			logger.debug(" - crashes: min=" + mins.crashs + ", max=" + maxs.crashs);

			for(User user : usersByLogin)
			{
				if(userChallengeStats[c].get(user.getId()).finished == 0)
				{
					logger.debug(" - " + user.getLogin() + "\t finished=0");
					expectedMovesPoints = midPoints;
					expectedCrashPoints = midPoints;
				}
				else
				{
					playerAvgMovesInFinishedGames = 0;
					playerAvgCrashsInFinishedGames = 0;

					for(int g = 0; g < challengeGames[c]; g++)
					{
						ugs = userGameStats[c][g].get(user.getId());

						if(ugs.finished == 1)
						{
							playerAvgMovesInFinishedGames += ugs.moves;
							playerAvgCrashsInFinishedGames += ugs.crashs;
						}
					}

					playerAvgMovesInFinishedGames /= (double) userChallengeStats[c].get(user.getId()).finished;
					playerAvgCrashsInFinishedGames /= (double) userChallengeStats[c].get(user.getId()).finished;

					// moves
					relativeMovesPosition = 1 - (playerAvgMovesInFinishedGames - mins.moves) / (maxs.moves - mins.moves);
					expectedMovesPoints = relativeMovesPosition * (maxPoints - 1) + 1;
					// crashs
					relativeCrashPosition = (playerAvgCrashsInFinishedGames - mins.crashs) / (maxs.crashs - mins.crashs);
					expectedCrashPoints = relativeCrashPosition * (maxPoints - 1) + 1;

					logger.debug(
							" - " + user.getLogin() + "\t finished=" + userChallengeStats[c].get(user.getId()).finished + "\tavgMoves= " + WikiUtil.round(playerAvgMovesInFinishedGames) + "\t-> expectedPoints=" + WikiUtil.round(expectedMovesPoints));
					logger.debug(
							" - " + user.getLogin() + "\t finished=" + userChallengeStats[c].get(user.getId()).finished + "\tavgCrashs=" + WikiUtil.round(playerAvgCrashsInFinishedGames) + "\t-> expectedPoints=" + WikiUtil.round(expectedCrashPoints));
				}

				expected = calculatePoints(expectedMovesPoints, expectedCrashPoints, 0);

				userChallengeStats[c].get(user.getId()).expected = expected;
				userStats.get(user.getId()).expected += expected;
			}
		}
	}

	protected class FinalTableSorter implements Comparator<Entry<Integer, UserStats>>
	{
		protected boolean finished;

		public FinalTableSorter(boolean finished)
		{
			super();
			this.finished = finished;
		}

		@Override
		public int compare(Entry<Integer, UserStats> o1, Entry<Integer, UserStats> o2)
		{
			if(finished && o2.getValue().total != o1.getValue().total)
				return (int) Math.signum(o2.getValue().total - o1.getValue().total); // mehr
			else if(!finished && o2.getValue().expected != o1.getValue().expected)
				return (int) Math.signum(o2.getValue().expected - o1.getValue().expected); // mehr
			if(o2.getValue().crashs != o1.getValue().crashs)
				return (int) Math.signum(o2.getValue().crashs - o1.getValue().crashs); // mehr
			if(o2.getValue().moves != o1.getValue().moves)
				return (int) Math.signum(o1.getValue().moves - o2.getValue().moves); // weniger!
			return 0;
		}
	}

	protected class UserStats
	{
		public int		finished;
		public int		left;
		public int		moves;
		public int		crashs;
		public double	total;
		public double	expected;

		public UserStats(int init)
		{
			this.finished = init;
			this.left = init;
			this.moves = init;
			this.crashs = init;
			this.total = init;
			this.expected = init;
		}
	}

	protected TreeMap<Integer, UserStats> createStatsMap(Collection<User> users)
	{
		TreeMap<Integer, UserStats> map = new TreeMap<>();
		for(User u : users)
			map.put(u.getId(), new UserStats(0));
		return map;
	}

	protected UserStats getMins(Collection<UserStats> stats)
	{
		UserStats min = new UserStats(Integer.MAX_VALUE);
		for(UserStats s : stats)
		{
			if(s.finished < min.finished)
				min.finished = s.finished;
			if(s.left < min.left)
				min.left = s.left;
			if(s.moves < min.moves)
				min.moves = s.moves;
			if(s.crashs < min.crashs)
				min.crashs = s.crashs;
			if(s.total < min.total)
				min.total = s.total;
			if(s.expected < min.expected)
				min.expected = s.expected;
		}
		return min;
	}

	protected UserStats getMaxs(Collection<UserStats> stats)
	{
		UserStats max = new UserStats(0);
		for(UserStats s : stats)
		{
			if(s.finished > max.finished)
				max.finished = s.finished;
			if(s.left > max.left)
				max.left = s.left;
			if(s.moves > max.moves)
				max.moves = s.moves;
			if(s.crashs > max.crashs)
				max.crashs = s.crashs;
			if(s.total > max.total)
				max.total = s.total;
			if(s.expected > max.expected)
				max.expected = s.expected;
		}
		return max;
	}

	protected Rules getRules(int challenge)
	{
		return this.data.getRulesByKey().get("" + challenge);
	}

	protected Map getMap(int challenge)
	{
		return this.data.getMapsByKey().get("" + challenge).get(0);
	}

	protected PlannedGame getGame(int challenge, int game)
	{
		return this.data.getGames().get(GAMES_KEY).get(this.challengeOffsets[challenge] + game);
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
		return WikiUtil.createLink("CraZZZy Crash Challenge " + this.cccx + " - Detailwertung Challenge " + (challenge + 1), (text ? "Challenge " : "") + (challenge + 1));
	}

	protected int parseInt(String s)
	{
		try
		{
			return Integer.parseInt(s);
		}
		catch(Exception e)
		{
			logger.error(e);
			return 0;
		}
	}
}
