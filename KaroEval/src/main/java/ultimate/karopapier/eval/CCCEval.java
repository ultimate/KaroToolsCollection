package ultimate.karopapier.eval;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import ultimate.karopapier.utils.Table;
import ultimate.karopapier.utils.WikiUtil;

public class CCCEval extends Eval<GameSeries>
{
	protected static final String							GAMES_KEY				= "Balanced";
	protected static final String[]							TABLE_HEAD_MAPS			= new String[] { "Nr.", "Strecke", "Spielerzahl", "ZZZ", "CPs", "Spielzahl" };
	protected static final String[]							TABLE_HEAD_GAME			= new String[] { "Platz", "Spieler", "Grundpunkte", "Crashs", "Züge", "Punkte" };
	protected static final int								BONUS_CHALLENGE			= 10;
	protected static final int								BONUS_FINAL				= 100;
	protected static final int								METRICS_GAME_MAXMOVES	= 0;

	protected final int										TABLE_TABLE_COLUMNS		= 7;

	protected int											cccx;
	// stats & metrics
	protected int											stats_challengesTotal;
	protected int											stats_challengesCreated;
	protected int											stats_gamesPerPlayer;
	protected int											stats_gamesPerPlayerPerChallenge;
	protected int											stats_gamesTotal;
	protected int											stats_gamesCreated;
	protected int											stats_players;
	protected UserStats										totalStats;
	protected UserStats[]									challengeStats;
	protected TreeMap<Integer, UserStats>					userStats;
	protected TreeMap<Integer, UserStats>[]					userChallengeStats;
	protected TreeMap<Integer, UserStats>[][]				userGameStats;
	protected double[][]									challengeMetrics;
	protected double[][][]									gameMetrics;
	// helpers
	protected Integer[]										challengeGames;
	protected Integer[]										challengeOffsets;
	protected List<User>									usersByLogin;
	protected HashMap<Integer, HashMap<Integer, Double>>	pointsPerRank;
	// evaluation
	protected Table[][]										tables;
	protected Table[]										totalTables;
	protected Table											finalTable;
	protected Table											whoOnWho;

	public CCCEval(int cccx)
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
		this.usersByLogin.sort((u1, u2) -> { return u1.getLoginLowerCase().compareTo(u2.getLoginLowerCase()); });
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
		// points
		this.pointsPerRank = new HashMap<>();
		String keyS;
		int number, place, i1, i2;
		double points;
		for(Object key : properties.keySet())
		{
			keyS = (String) key;
			if(keyS.startsWith("points."))
			{
				i1 = keyS.indexOf(".");
				i2 = keyS.indexOf(".", i1 + 1);
				number = Integer.parseInt(keyS.substring(i1 + 1, i2));
				place = Integer.parseInt(keyS.substring(i2 + 1));
				points = Double.parseDouble(properties.getProperty(keyS));
				if(!this.pointsPerRank.containsKey(number))
					this.pointsPerRank.put(number, new HashMap<>());
				this.pointsPerRank.get(number).put(place, points);
			}
		}

		// create the header for the final table
		String[] finalTableHead = new String[this.stats_challengesCreated + 12];
		int col = 0;
		finalTableHead[col++] = "Platz";
		finalTableHead[col++] = "Spieler";
		for(int c = 0; c < this.stats_challengesCreated; c++)
			finalTableHead[col++] = challengeToLink(c, false);
		finalTableHead[col++] = "Grundpunkte (gesamt)";
		finalTableHead[col++] = "Crashs (gesamt)";
		finalTableHead[col++] = "Züge (gesamt)";
		finalTableHead[col++] = "Skalierte Punkte (gesamt)";
		finalTableHead[col++] = "Challenge-Bonus (gesamt)";
		finalTableHead[col++] = "Bonus (Gesamtwertung)";
		finalTableHead[col++] = "Endergebnis";
		finalTableHead[col++] = "Abgeschlossene Rennen";
		finalTableHead[col++] = "Erwartungswert";
		finalTableHead[col++] = "Erwartungswert (alt)";

		// init variables
		this.tables = new Table[stats_challengesTotal][];
		this.totalTables = new Table[stats_challengesTotal];
		this.finalTable = new Table(finalTableHead);
		this.totalStats = new UserStats(0);
		this.userStats = createStatsMap(usersByLogin);
		this.challengeStats = new UserStats[stats_challengesTotal];
		this.userChallengeStats = (TreeMap<Integer, UserStats>[]) new TreeMap[stats_challengesTotal];
		this.userGameStats = (TreeMap<Integer, UserStats>[][]) new TreeMap[stats_challengesTotal][];
		for(int c = 0; c < this.stats_challengesTotal; c++)
		{
			this.tables[c] = new Table[challengeGames[c]];
			this.challengeStats[c] = new UserStats(0);
			this.userChallengeStats[c] = createStatsMap(usersByLogin);
			this.userGameStats[c] = (TreeMap<Integer, UserStats>[]) new TreeMap[challengeGames[c]];
			for(int g = 0; g < challengeGames[c]; g++)
				this.userGameStats[c][g] = createStatsMap(usersByLogin);
		}

		// init the whoOnWho
		Object[] whoOnWhoHead = new Object[this.stats_players + 1];
		this.whoOnWho = new Table(whoOnWhoHead.length);
		this.whoOnWho.addRow(whoOnWhoHead); // don't add this as a header
		col = 1; // leave first column empty
		Object[] row;
		for(User user : usersByLogin)
		{
			if(user == data.getCreator())
				continue;
			whoOnWhoHead[col++] = user;
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
		for(int i = 2; i < 10; i++)
			if(pointsPerRank.containsKey(i))
				logger.info("  pointsPerRank(" + i + ")           = " + pointsPerRank.get(i).values());
	}

	public List<File> evaluate() throws IOException, InterruptedException
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
			detail.append("== Rennergebnisse ==\r\n");

			tableTable = new Table(TABLE_TABLE_COLUMNS);
			for(int g = 0; g < tables[c].length; g++)
			{
				if(g % TABLE_TABLE_COLUMNS == 0)
				{
					if(g != 0)
						tableTable.addRow(row);
					row = new Object[TABLE_TABLE_COLUMNS];
				}
				game = getGame(c, g);
				if(game.getGame() == null)
					continue;
				row[g % TABLE_TABLE_COLUMNS] = "\r\nChallenge " + gameToLink(c, g) + "\r\n" + WikiUtil.toString(tables[c][g], null) + "\r\n";
			}
			tableTable.addRow(row);

			detail.append(WikiUtil.toString(tableTable, null));
			detail.append("\r\n");
			detail.append("== Tabellarische Auswertung ==\r\n");
			detail.append(WikiUtil.toString(totalTables[c], null, "-"));
			detail.append("\r\n");

			filesUpdated.add(writeFile("czzzcc" + cccx + "-wiki-challenge" + (c + 1) + ".txt", detail.toString()));
			detailLinks.append("*" + challengeToLink(c, true) + "\r\n");
		}

		StringBuilder total = new StringBuilder();

		if(!finished)
			total.append(WikiUtil.toString(finalTable, "alignedright", WikiUtil.getDefaultColumnConfig(finalTable.getColumns())));
		else
			total.append(WikiUtil.toString(finalTable, "alignedright", WikiUtil.getDefaultColumnConfig(finalTable.getColumns() - 1))); // without
																																		// expected

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

		stats.append("== Wer gegen wen? ==\r\n");
		stats.append("Eigentlich wollte ich hier noch die ganzen Links zu den Spielen reinschreiben, aber damit kam das Wiki nicht klar! Daher hier nur die Anzahl...\r\n");
		stats.append(WikiUtil.toString(whoOnWho, null));

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

		addFinalBonus(mins, maxs);

		// recalculate maxs to identify the players with the highest bonus
		maxs = getMaxs(userStats.values());

		logger.debug("              min\t| max: ");
		logger.debug("  finished  = " + mins.finished + "\t| " + maxs.finished);
		logger.debug("  left      = " + mins.left + "\t| " + maxs.left);
		logger.debug("  moves     = " + mins.moves + "\t| " + maxs.moves);
		logger.debug("  crashs    = " + mins.crashs + "\t| " + maxs.crashs);
		logger.debug("  basic     = " + mins.basic + "\t| " + maxs.basic);
		logger.debug("  unscaled  = " + mins.unscaled + "\t| " + maxs.unscaled);
		logger.debug("  scaled    = " + WikiUtil.round(mins.scaled) + "\t| " + WikiUtil.round(maxs.scaled));
		logger.debug("  bonus1    = " + mins.bonus1 + "\t| " + maxs.bonus1);
		logger.debug("  bonus2    = " + mins.bonus2 + "\t| " + maxs.bonus2);
		logger.debug("  total     = " + mins.total + "\t| " + maxs.total);

		boolean finished = true;
		UserStats us = null;
		User user;
		List<Entry<Integer, UserStats>> userStatsList = new LinkedList<>(userStats.entrySet());
		
		for(Entry<Integer, UserStats> use : userStatsList)
		{
			us = use.getValue();
			user = karoAPICache.getUser(use.getKey());
			
			us.total = us.scaled + us.bonus1 + us.bonus2; 
			us.totalExpected = us.scaledExpected + us.bonus1 + us.bonus2;			
			
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

			if(rows > 0 && (finished ? us.total == usLast.total : us.totalExpected == usLast.totalExpected) && us.crashs == usLast.crashs && us.basic == usLast.basic && us.moves == usLast.moves)
				rank = " ";
			else
				rank = (rows + 1) + ".";

			row = new Object[finalTable.getColumns()];
			col = 0;
			row[col++] = rank;
			row[col++] = user;
			for(int c = 0; c < stats_challengesCreated; c++)
			{
				row[col] = userChallengeStats[c].get(user.getId()).scaled;

				if(userChallengeStats[c].get(user.getId()).scaled >= 100.0)
					row[col] = WikiUtil.highlight(row[col]);

				if(userChallengeStats[c].get(user.getId()).finished < stats_gamesPerPlayerPerChallenge)
				{
					row[col] = row[col] + "&nbsp;<span style=\"font-size:50%\">(" + (stats_gamesPerPlayerPerChallenge - userChallengeStats[c].get(user.getId()).finished) + ")</span>";
					finished = false;
				}

				col++;
			}
			row[col++] = us.basic;
			row[col++] = us.crashs;
			row[col++] = us.moves;
			row[col++] = us.scaled;
			row[col++] = (us.bonus1 > 0 ? "+" + us.bonus1 : "-");
			row[col++] = (us.bonus2 > 0 ? "+" + us.bonus2 : "-");
			row[col++] = us.total;
			row[col++] = us.finished;
			row[col++] = us.totalExpected;

			finalTable.addRow(row);

			// set highlights
			finalTable.setHighlight(rows, 1, true);
			if(us.basic == maxs.basic)
				finalTable.setHighlight(rows, stats_challengesCreated + 2, true);
			if(us.crashs == maxs.crashs)
				finalTable.setHighlight(rows, stats_challengesCreated + 3, true);
			if(us.moves == mins.moves)
				finalTable.setHighlight(rows, stats_challengesCreated + 4, true);
			if(us.bonus2 > 0)
				finalTable.setHighlight(rows, stats_challengesCreated + 7, true);
			finalTable.setHighlight(rows, stats_challengesCreated + 8, true); // total
			finalTable.setHighlight(rows, stats_challengesCreated + 10, true); // totalExpected

			rows++;
		}

		logger.info("finished=" + finished);

		return finished;
	}

	protected void createTables(int c)
	{
		logger.info("creating tables for challenge #" + (c + 1));

		// init the total table for this challenge
		String[] totalTableHead = new String[challengeGames[c] + 7];
		int col = 0;
		totalTableHead[col++] = "Spieler";
		for(int g = 0; g < challengeGames[c]; g++)
			totalTableHead[col++] = gameToLink(c, g);
		totalTableHead[col++] = "Grundpunkte (gesamt)";
		totalTableHead[col++] = "Crashs (gesamt)";
		totalTableHead[col++] = "Züge (gesamt)";
		totalTableHead[col++] = "Gesamtpunkte (unskaliert)";
		totalTableHead[col++] = "Gesamtpunkte (skaliert)";
		totalTableHead[col++] = "Challenge-Bonus";
		totalTables[c] = new Table(totalTableHead);

		calcMetrics(c);

		for(int g = 0; g < challengeGames[c]; g++)
		{
			calcMetrics(c, g);

			tables[c][g] = createTable(c, g);

			updateWhoOnWho(c, g);
		}

		UserStats mins = getMins(userChallengeStats[c].values());
		UserStats maxs = getMaxs(userChallengeStats[c].values());
		maxs.scaled = 100.0;

		addBonus(c, mins, maxs);

		Object[] row;
		double scaled;
		int rows = 0;
		for(User user : usersByLogin)
		{
			scaled = userChallengeStats[c].get(user.getId()).unscaled * 100 / maxs.unscaled;

			totalStats.scaled += scaled;
			challengeStats[c].scaled += scaled;
			userStats.get(user.getId()).scaled += scaled;
			userChallengeStats[c].get(user.getId()).scaled = scaled;

			row = new Object[totalTables[c].getColumns()];
			col = 0;
			row[col++] = user;
			for(int g = 0; g < challengeGames[c]; g++)
			{
				if(getGame(c, g) == null)
					continue;
				if(getGame(c, g).getPlayers().contains(user))
				{
					for(Object[] tableRow : tables[c][g].getRows())
					{
						if(((Player) tableRow[1]).getName().equals(user.getLogin()))
						{
							row[col++] = tableRow[tables[c][g].getColumns() - 1]; // the points are in the last column
							break;
						}
					}
				}
				else
					row[col++] = null;
			}
			row[col++] = userChallengeStats[c].get(user.getId()).basic;
			row[col++] = userChallengeStats[c].get(user.getId()).crashs;
			row[col++] = userChallengeStats[c].get(user.getId()).moves;
			row[col++] = userChallengeStats[c].get(user.getId()).unscaled;
			row[col++] = userChallengeStats[c].get(user.getId()).scaled;
			if(userChallengeStats[c].get(user.getId()).bonus1 != 0)
				row[col++] = "+" + userChallengeStats[c].get(user.getId()).bonus1;
			else
				row[col++] = "-";
			totalTables[c].addRow(row);

			totalTables[c].setHighlight(rows, 0, true);
			if(userChallengeStats[c].get(user.getId()).basic == maxs.basic)
				totalTables[c].setHighlight(rows, challengeGames[c] + 1, true);
			if(userChallengeStats[c].get(user.getId()).crashs == maxs.crashs)
				totalTables[c].setHighlight(rows, challengeGames[c] + 2, true);
			if(userChallengeStats[c].get(user.getId()).moves == mins.moves)
				totalTables[c].setHighlight(rows, challengeGames[c] + 3, true);
			if(userChallengeStats[c].get(user.getId()).unscaled == maxs.unscaled)
				totalTables[c].setHighlight(rows, challengeGames[c] + 4, true);
			if(userChallengeStats[c].get(user.getId()).scaled == maxs.scaled)
				totalTables[c].setHighlight(rows, challengeGames[c] + 5, true);
			if(userChallengeStats[c].get(user.getId()).bonus1 > 0)
				totalTables[c].setHighlight(rows, challengeGames[c] + 6, true);

			rows++;
		}
	}

	protected Table createTable(int c, int g)
	{
		logger.info("creating tables for challenge #" + (c + 1) + "." + (g + 1));

		Table table = new Table(TABLE_HEAD_GAME);
		Game game = getGame(c, g).getGame();
		if(game == null)
			return table;

		int highestMoveCount = 0;

		List<Player> players = game.getPlayers();
		sortPlayers(players);

		int p = 0;
		for(Player player : players)
		{
			if(player.getName().equals(data.getCreator().getLogin()))
				continue;
			p++;

			if(player.getMoveCount() > highestMoveCount)
				highestMoveCount = player.getMoveCount();

			table.addRow(createRow(c, g, game, player, p));
		}

		return table;
	}

	protected Object[] createRow(int c, int g, Game game, Player player, int position)
	{
		Object[] row = new Object[TABLE_HEAD_GAME.length];
		row[0] = position + ".";
		row[1] = player;

		Integer crashs = null;
		Integer moves = null;
		Double basicPoints = null;
		Double points = null;

		// crashs
		String key = (c + 1) + "." + (g + 1) + "." + player.getName();
		if(properties.containsKey(key))
		{
			String s = properties.getProperty(key);
			logger.info("correcting crash count for " + key + " by " + s);
			crashs = player.getCrashCount() + parseInt(s);
		}
		else
			crashs = player.getCrashCount();

		totalStats.crashs += crashs;
		challengeStats[c].crashs += crashs;
		userStats.get(player.getId()).crashs += crashs;
		userChallengeStats[c].get(player.getId()).crashs += crashs;
		userGameStats[c][g].get(player.getId()).crashs += crashs;

		row[3] = crashs;

		// moves
		if(player.getStatus() == EnumPlayerStatus.ok && player.getRank() == 0)
			moves = player.getMoveCount();
		else if(player.getStatus() == EnumPlayerStatus.ok)
			moves = player.getMoveCount() - 1; // parf ferme
		else
			moves = (int) (this.gameMetrics[c][g][METRICS_GAME_MAXMOVES] + 1);

		totalStats.moves += moves;
		challengeStats[c].moves += moves;
		userStats.get(player.getId()).moves += moves;
		userChallengeStats[c].get(player.getId()).moves += moves;
		userGameStats[c][g].get(player.getId()).moves += moves;

		row[4] = moves;

		if((player.getStatus() == EnumPlayerStatus.ok && player.getRank() > 0) || (player.getStatus() != EnumPlayerStatus.ok))
		{
			basicPoints = calcBasicPoints(game.getPlayers().size() - 1, player.getRank(), moves, crashs);

			totalStats.basic += basicPoints;
			challengeStats[c].basic += basicPoints;
			userStats.get(player.getId()).basic += basicPoints;
			userChallengeStats[c].get(player.getId()).basic += basicPoints;
			userGameStats[c][g].get(player.getId()).basic += basicPoints;

			totalStats.finished++;
			challengeStats[c].finished++;
			userStats.get(player.getId()).finished++;
			userChallengeStats[c].get(player.getId()).finished++;
			userGameStats[c][g].get(player.getId()).finished++;

			if(player.getStatus() != EnumPlayerStatus.ok)
			{
				totalStats.left++;
				challengeStats[c].left++;
				userStats.get(player.getId()).left++;
				userChallengeStats[c].get(player.getId()).left++;
				userGameStats[c][g].get(player.getId()).left++;
			}
		}

		// grundpunkte
		row[2] = basicPoints;

		// points
		if((player.getStatus() == EnumPlayerStatus.ok && player.getRank() > 0) || (player.getStatus() != EnumPlayerStatus.ok))
		{
			points = calcGamePoints(game, player, row);

			totalStats.unscaled += points;
			challengeStats[c].unscaled += points;
			userStats.get(player.getId()).unscaled += points;
			userChallengeStats[c].get(player.getId()).unscaled += points;
			userGameStats[c][g].get(player.getId()).unscaled += points;
		}

		row[5] = points;

		return row;
	}

	protected void sortPlayers(List<Player> players)
	{
		Collections.sort(players, (p1, p2) -> {
			if(p1.getRank() > 0 && p2.getRank() > 0)
			{
				return (int) Math.signum(p1.getRank() - p2.getRank());
			}
			else if(p1.getRank() > 0)
			{
				return -1;
			}
			else if(p2.getRank() > 0)
			{
				return +1;
			}
			else
			{
				return (int) -Math.signum(p1.getMoveCount() - p2.getMoveCount());
			}
		});
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

	protected double calcBasicPoints(int players, int rank, int moves, int crashs)
	{
		if(rank > 0)
			return pointsPerRank.get(players).get(rank);
		else
			return -players;
	}

	protected double calcGamePoints(Game game, Player player, Object[] row)
	{
		if(player.getStatus() == EnumPlayerStatus.ok)
			return (double) row[2] * (int) row[3];
		else
			return -(game.getPlayers().size() - 1);
	}

	protected void addBonus(int c, UserStats mins, UserStats maxs)
	{
		for(User user : usersByLogin)
		{
			if(userChallengeStats[c].get(user.getId()).unscaled == maxs.unscaled)
			{
				totalStats.bonus1 += BONUS_CHALLENGE;
				challengeStats[c].bonus1 += BONUS_CHALLENGE;
				userStats.get(user.getId()).bonus1 += BONUS_CHALLENGE;
				userChallengeStats[c].get(user.getId()).bonus1 += BONUS_CHALLENGE;
			}

			if(userChallengeStats[c].get(user.getId()).crashs == maxs.crashs)
			{
				totalStats.bonus1 += BONUS_CHALLENGE;
				challengeStats[c].bonus1 += BONUS_CHALLENGE;
				userStats.get(user.getId()).bonus1 += BONUS_CHALLENGE;
				userChallengeStats[c].get(user.getId()).bonus1 += BONUS_CHALLENGE;
			}

			if(userChallengeStats[c].get(user.getId()).moves == mins.moves)
			{
				totalStats.bonus1 += BONUS_CHALLENGE;
				challengeStats[c].bonus1 += BONUS_CHALLENGE;
				userStats.get(user.getId()).bonus1 += BONUS_CHALLENGE;
				userChallengeStats[c].get(user.getId()).bonus1 += BONUS_CHALLENGE;
			}
		}
	}

	protected void addFinalBonus(UserStats mins, UserStats maxs)
	{
		for(User user : usersByLogin)
		{
			if(userStats.get(user.getId()).unscaled == maxs.unscaled)
			{
				totalStats.bonus2 += BONUS_FINAL;
				userStats.get(user.getId()).bonus2 += BONUS_FINAL;
			}

			if(userStats.get(user.getId()).crashs == maxs.crashs)
			{
				totalStats.bonus2 += BONUS_FINAL;
				userStats.get(user.getId()).bonus2 += BONUS_FINAL;
			}

			if(userStats.get(user.getId()).moves == mins.moves)
			{
				totalStats.bonus2 += BONUS_FINAL;
				userStats.get(user.getId()).bonus2 += BONUS_FINAL;
			}
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

					logger.debug(user1.getLogin() + " vs. " + user2.getLogin() + "     u=" + u + ", col=" + col);
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
		int players_crashed;
		int players_crashed_allgames = 0;
		int players_finished;
		@SuppressWarnings("unused")
		int players_finished_allgames = 0;

		for(UserStats us : userStats.values())
		{
			// count how many players had a crash (all games)
			if(us.crashs > 0)
				players_crashed_allgames++;
			// count how many players are finished (all games)
			if(us.finished == stats_gamesPerPlayer)
				players_finished_allgames++;
		}

		double avg_crashs;
		double avg_points;
		double expected;
		double expected_max;
		double actual_positive;
		double actual_negative;
		int positive_count, negative_count;
		double player_avg_crashs;
		double player_avg_points;
		UserStats ugs;
		for(int c = 0; c < this.stats_challengesCreated; c++)
		{
			expected_max = 0;

			players_crashed = 0;
			players_finished = 0;
			for(UserStats us : userChallengeStats[c].values())
			{
				// count how many players had a crash (this challenge)
				if(us.crashs > 0)
					players_crashed++;
				// count how many players are finished (this challenge)
				if(us.finished == stats_gamesPerPlayer)
					players_finished++;
			}
			// if there are crashs in this challenge --> use local average, otherwise total
			if(players_crashed != 0)
				avg_crashs = challengeStats[c].crashs / players_crashed;
			else if(players_crashed_allgames != 0)
				avg_crashs = totalStats.crashs / players_crashed_allgames;
			else
				avg_crashs = 0;

			// get the average points = points for the mid rank
			avg_points = pointsPerRank.get(getRules(c).getNumberOfPlayers()).get(getRules(c).getNumberOfPlayers() / 2);

			for(User user : usersByLogin)
			{
				player_avg_crashs = 0;
				player_avg_points = 0;
				actual_positive = 0;
				actual_negative = 0;
				positive_count = 0;
				negative_count = 0;

				for(int g = 0; g < challengeGames[c]; g++)
				{
					ugs = userGameStats[c][g].get(user.getId());
					if(ugs.moves == 0)
						continue;

					if(ugs.unscaled > 0)
					{
						actual_positive += ugs.crashs * ugs.unscaled;
						player_avg_crashs += ugs.crashs;
						player_avg_points += ugs.unscaled;
						positive_count++;
					}
					else
					{
						actual_negative += ugs.unscaled;
						negative_count++;
					}
				}

				if(positive_count > 0)
				{
					player_avg_crashs /= (positive_count + negative_count);
					player_avg_points /= (positive_count + negative_count);
				}

				if(userChallengeStats[c].get(user.getId()).finished == stats_gamesPerPlayerPerChallenge)
				{
					// spieler hat alle Rennen beendet
					expected = actual_positive + actual_negative;
				}
				else if(positive_count > 0)
				{
					// spieler hat ein paar Rennen regulaer beendet (ohne rauswurf)
					// eigene durchschnittliche Punkte und Crashs verwenden
					expected = actual_positive + actual_negative;
					expected += player_avg_crashs * player_avg_points * (stats_gamesPerPlayerPerChallenge - positive_count);
				}
				else
				{
					// spieler hat noch keine Rennen beendet regulaer beendet
					// allgemeine durchschnittliche Punkte und Crashs verwenden
					expected = actual_positive + actual_negative;
					expected += avg_crashs * avg_points * stats_gamesPerPlayerPerChallenge;
				}
//				logger.debug(user.getLogin() + ": unscaledExpected=" + expected);
				userChallengeStats[c].get(user.getId()).unscaledExpected = expected;
				userStats.get(user.getId()).unscaledExpected += expected;

				if(expected > expected_max)
					expected_max = expected;
			}

			for(User user : usersByLogin)
			{
				if(players_finished > 0)
					userChallengeStats[c].get(user.getId()).scaledExpected = userChallengeStats[c].get(user.getId()).unscaledExpected * 100 / expected_max;
				else
					userChallengeStats[c].get(user.getId()).scaledExpected = 50.0;
				logger.debug(user.getLogin() + ": scaledExpected=" + userChallengeStats[c].get(user.getId()).scaledExpected);
				userStats.get(user.getId()).scaledExpected += userChallengeStats[c].get(user.getId()).scaledExpected;
			}
		}
	}

	protected class FinalTableSorter implements Comparator<Entry<Integer, UserStats>>
	{
		private boolean finished;

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
			else if(!finished && o2.getValue().scaledExpected != o1.getValue().scaledExpected)
				return (int) Math.signum(o2.getValue().scaledExpected - o1.getValue().scaledExpected); // mehr
			if(o2.getValue().crashs != o1.getValue().crashs)
				return (int) Math.signum(o2.getValue().crashs - o1.getValue().crashs); // mehr
			if(o2.getValue().basic != o1.getValue().basic)
				return (int) Math.signum(o2.getValue().basic - o1.getValue().basic); // mehr
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
		public double	basic;
		public double	unscaled;
		public double	scaled;
		public int		bonus1;
		public int		bonus2;
		public double	total;
		public double	unscaledExpected;
		public double	scaledExpected;
		public double	totalExpected;

		public UserStats(int init)
		{
			this.finished = init;
			this.left = init;
			this.moves = init;
			this.crashs = init;
			this.basic = init;
			this.unscaled = init;
			this.scaled = init;
			this.bonus1 = init;
			this.bonus2 = init;
			this.total = init;
			this.unscaledExpected = init;
			this.scaledExpected = init;
			this.totalExpected = init;
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
			if(s.basic < min.basic)
				min.basic = s.basic;
			if(s.unscaled < min.unscaled)
				min.unscaled = s.unscaled;
			if(s.scaled < min.scaled)
				min.scaled = s.scaled;
			if(s.bonus1 < min.bonus1)
				min.bonus1 = s.bonus1;
			if(s.bonus2 < min.bonus2)
				min.bonus2 = s.bonus2;
			if(s.total < min.total)
				min.total = s.total;
			if(s.unscaledExpected < min.unscaledExpected)
				min.unscaledExpected = s.unscaledExpected;
			if(s.scaledExpected < min.scaledExpected)
				min.scaledExpected = s.scaledExpected;
			if(s.totalExpected < min.totalExpected)
				min.totalExpected = s.totalExpected;
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
			if(s.basic > max.basic)
				max.basic = s.basic;
			if(s.unscaled > max.unscaled)
				max.unscaled = s.unscaled;
			if(s.scaled > max.scaled)
				max.scaled = s.scaled;
			if(s.bonus1 > max.bonus1)
				max.bonus1 = s.bonus1;
			if(s.bonus2 > max.bonus2)
				max.bonus2 = s.bonus2;
			if(s.total > max.total)
				max.total = s.total;
			if(s.unscaledExpected > max.unscaledExpected)
				max.unscaledExpected = s.unscaledExpected;
			if(s.scaledExpected < max.scaledExpected)
				max.scaledExpected = s.scaledExpected;
			if(s.totalExpected > max.totalExpected)
				max.totalExpected = s.totalExpected;
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

	protected int indexOf(List<User> users, Player player)
	{
		for(int i = 0; i < users.size(); i++)
		{
			if(users.get(i).getLogin().equals(player.getName()))
				return i;
		}
		return -1;
	}
}
