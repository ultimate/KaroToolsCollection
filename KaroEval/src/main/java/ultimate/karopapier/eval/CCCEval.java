package ultimate.karopapier.eval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.Supplier;

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
	// stats
	protected int											stats_challengesTotal;
	protected int											stats_challengesCreated;
	protected int											stats_gamesPerPlayer;
	protected int											stats_gamesPerPlayerPerChallenge;
	protected int											stats_gamesTotal;
	protected int											stats_gamesCreated;
	protected int											stats_moves;
	protected int											stats_crashs;
	protected int											stats_players;
	// helpers
	protected Integer[]										challengeGames;
	protected Integer[]										challengeOffsets;
	protected List<User>									usersByLogin;
	protected HashMap<Integer, HashMap<Integer, Integer>>	pointsPerRank;
	// evaluation
	protected Table[][]										tables;
	protected Table[]										totalTables;
	protected Table											finalTable;
	protected Table											whoOnWho;
	// user stats & metrics
	protected TreeMap<Integer, UserStats>					userStats;
	protected TreeMap<Integer, UserStats>[]					userChallengeStats;
	protected double[][]									challengeMetrics;
	protected double[][][]									gameMetrics;

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

		// since all information is stored in the gameseries or the cron-properties, we don't need the gid-properties any more
		// but we init some other values for convenience and later usage

		// stats
		this.stats_challengesTotal = (int) gameSeries.get(GameSeries.NUMBER_OF_MAPS);
		this.stats_players = gameSeries.getPlayers().size();
		this.stats_gamesTotal = gameSeries.getGames().size();
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
		int gamesInThisChallenge;
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
				if(games.get(offset + g).isCreated())
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
		int number, place, points, i1, i2;
		for(Object key : properties.keySet())
		{
			keyS = (String) key;
			if(keyS.startsWith("points."))
			{
				i1 = keyS.indexOf(".");
				i2 = keyS.indexOf(".", i1 + 1);
				number = Integer.parseInt(keyS.substring(i1 + 1, i2));
				place = Integer.parseInt(keyS.substring(i2));
				points = Integer.parseInt(properties.getProperty(keyS));
				if(!this.pointsPerRank.containsKey(number))
					this.pointsPerRank.put(number, new HashMap<>());
				this.pointsPerRank.get(number).put(place, points);
			}
		}

		// create the header for the final table
		String[] finalTableHead = new String[this.stats_challengesCreated + 11];
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
		this.userStats = createStatsMap(usersByLogin);
		this.userChallengeStats = new TreeMap[stats_challengesTotal];
		for(int c = 0; c < this.stats_challengesTotal; c++)
			this.userChallengeStats[c] = createStatsMap(usersByLogin);

		// init the whoOnWho
		String[] whoOnWhoHead = new String[this.stats_players + 1];
		this.whoOnWho = new Table(whoOnWhoHead);
		col = 1; // leave first column empty
		Object[] row;
		for(User user : usersByLogin)
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
			detail.append(WikiUtil.toString(totalTables[c], null, "-"));
			detail.append("\n");

			writeFile("czzzcc" + cccx + "-wiki-challenge" + (c + 1) + ".txt", detail.toString());
			detailLinks.append("*" + challengeToLink(c, true) + "\n");
		}

		StringBuilder total = new StringBuilder();

		if(!finished)
			total.append(WikiUtil.toString(finalTable, "alignedright", WikiUtil.getDefaultColumnConfig(finalTable.getColumns())));
		else
			total.append(WikiUtil.toString(finalTable, "alignedright", WikiUtil.getDefaultColumnConfig(finalTable.getColumns() - 2)));

		StringBuilder stats = new StringBuilder();

		stats.append("== Zahlen & Fakten ==\n");
		stats.append("*Rennen insgesamt: '''" + stats_gamesTotal + "'''\n");
		stats.append("*Teilnehmer: '''" + stats_players + "'''\n");
		stats.append("*Rennen pro Spieler: '''" + stats_gamesPerPlayer + "'''\n");
		stats.append("*Rennen pro Spieler pro Challenge: '''" + stats_gamesPerPlayerPerChallenge + "'''\n");
		stats.append("*Z�ge insgesamt: '''" + stats_moves + "'''\n");
		stats.append("*Crashs insgesamt: '''" + stats_crashs + "'''\n");
		stats.append("*H�ufigste Begegnung: " + getMaxMinWhoOnWho("max") + "\n");
		stats.append("*Seltenste Begegnung: " + getMaxMinWhoOnWho("min") + "\n");

		stats.append("== Wer gegen wen? ==\n");
		stats.append("Eigentlich wollte ich hier noch die ganzen Links zu den Spielen reinschreiben, aber damit kam das Wiki nicht klar! Daher hier nur die Anzahl...\n");
		stats.append(WikiUtil.toString(whoOnWho, null));

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
		s = s.replace("${MAPS}", WikiUtil.toString(mapTable, null));
		s = s.replace("${DETAIL}", detailLinks.toString());
		s = s.replace("${TOTAL}", total.toString());
		s = s.replace("${STATS}", stats.toString());

		writeFile("czzzcc" + cccx + "-wiki-overview.txt", s.toString());

		return s;
	}

	protected boolean createTables()
	{
		String[] totalTableHead;
		Object[] row;
		int col;
		UserStats mins, maxs;

		for(int c = 0; c < this.stats_challengesCreated; c++)
		{
			// init the total table for this challenge
			totalTableHead = new String[challengeGames[c] + 6];
			col = 0;
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
				tables[c][g] = createTable(c, g);

				createWhoOnWho(c, r, challengePlayers);
			}

			mins = getMins(userChallengeStats[c].values());
			maxs = getMaxs(userChallengeStats[c].values());

			addBonus(c, mins, maxs);

			double scaled;
			for(User user : usersByLogin)
			{
				scaled = userChallengeStats[c].get(user.getId()).unscaled * 100 / maxs.unscaled;
				userChallengeStats[c].get(user.getId()).scaled = scaled;
				userStats.get(user.getId()).scaled += scaled;

				row = new Object[totalTables[c].getColumns()];
				col = 0;
				row[col++] = WikiUtil.createLink(user, true);
				for(int g = 0; g < challengeGames[c]; g++)
				{
					if(getGame(c, g) == null)
						continue;
					if(getGame(c, g).getPlayers().contains(user))
					{
						for(Object[] tableRow : tables[c][g].getRows())
						{
							if(((String) tableRow[1]).contains(user.getLogin()))
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
				row[col++] = userChallengeStats[c].get(user.getId()).bonus1;
				totalTables[c].addRow(row);
			}
		}

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

	protected void createTables(int c)
	{

	}

	protected Table createTable(int c, int g)
	{
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
			if(player.getName().equals(gameSeries.getCreator().getLogin()))
				continue;
			p++;

			if(player.getMoveCount() > highestMoveCount)
				highestMoveCount = player.getMoveCount();

			tables[c][g].addRow(createRow(c, g, game, player, p));
		}

		return table;
	}

	protected Object[] createRow(int c, int g, Game game, Player player, int position)
	{
		Object[] row = new Object[TABLE_HEAD_GAME.length];
		row[0] = position + ".";
		row[1] = WikiUtil.createLink(player, false);

		Integer crashs = null;
		Integer moves = null;
		Integer basicPoints = null;
		Double points = null;

		if(properties.containsKey(c + "." + g + "." + player.getName()))
		{
			String s = properties.getProperty(c + "." + g + "." + player);
			logger.info("correcting crash count for " + player.getName() + " @ " + c + "." + g + " by " + s);
			crashs = player.getCrashCount() - parseInt(s);
		}
		else
		{
			crashs = player.getCrashCount();
		}
		if(player.getStatus() == EnumPlayerStatus.ok)
		{
			moves = player.getMoveCount(); // moves
			if(player.getRank() > 0)
			{
				basicPoints = pointsPerRank.get(game.getPlayers().size()).get(position);
				points = calcGamePoints(game, player, row);
			}
		}
		else
		{
			basicPoints = -game.getPlayers().size();
			points = calcGamePoints(game, player, row);
			moves = (int) (this.gameMetrics[c][g][METRICS_GAME_MAXMOVES] + 1);
		}

		row[2] = basicPoints; // grundpunkte
		if(basicPoints != null)
		{
			userStats.get(player.getId()).basic += basicPoints;
			userChallengeStats[c].get(player.getId()).basic += basicPoints;
		}

		row[3] = crashs; // crashs
		if(crashs != null)
		{
			userStats.get(player.getId()).crashs += crashs;
			userChallengeStats[c].get(player.getId()).crashs += crashs;
		}

		row[4] = moves; // moves
		if(moves != null)
		{
			userStats.get(player.getId()).moves += moves;
			userChallengeStats[c].get(player.getId()).moves += moves;
		}

		row[5] = points; // points
		if(points != null)
		{
			userStats.get(player.getId()).unscaled += points;
			userChallengeStats[c].get(player.getId()).unscaled += points;
		}

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

		for(Player p : getGame(c, g).getGame().getPlayers())
		{
			if(p.getMoveCount() > this.gameMetrics[c][g][METRICS_GAME_MAXMOVES])
				this.gameMetrics[c][g][METRICS_GAME_MAXMOVES] = p.getMoveCount();
		}
	}

	protected double calcGamePoints(Game game, Player player, Object[] row)
	{
		if(player.getStatus() == EnumPlayerStatus.ok)
			return (int) row[2] * (int) row[3];
		else
			return -(game.getPlayers().size() - 1);
	}

	protected void addBonus(int c, UserStats mins, UserStats maxs)
	{
		int u = 0;
		for(User user : usersByLogin)
		{
			if(userChallengeStats[c].get(user.getId()).unscaled == maxs.unscaled)
			{
				userChallengeStats[c].get(user.getId()).bonus1 += BONUS_CHALLENGE;
				userStats.get(user.getId()).bonus1 += BONUS_CHALLENGE;
			}

			if(userChallengeStats[c].get(user.getId()).crashs == maxs.crashs)
			{
				userChallengeStats[c].get(user.getId()).bonus1 += BONUS_CHALLENGE;
				userStats.get(user.getId()).bonus1 += BONUS_CHALLENGE;
			}

			if(userChallengeStats[c].get(user.getId()).moves == mins.moves)
			{
				userChallengeStats[c].get(user.getId()).bonus1 += BONUS_CHALLENGE;
				userStats.get(user.getId()).bonus1 += BONUS_CHALLENGE;
			}
		}
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
				whoOnWho.[ci1 + 1][ci2 + 1] = whoOnWho[ci1 + 1][ci2 + 1] + 1;
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

	private void calculateExpected(List<String> challengePlayers)
	{
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

			avg_points = intFromString(p.getProperty("points." + getRules(c).getNumberOfPlayers() + "." + (int) (getRules(c).getNumberOfPlayers() / 2)));

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

	protected void sortFinalTable(boolean finished)
	{
		if(!finished)
			Collections.sort(finalTable.getRows(), new FinalTableRowSorter(-2)); // sort by Erwartungswert
		else
			Collections.sort(finalTable.getRows(), new FinalTableRowSorter(-4)); // sort by Endergebnis
	}

	protected class UserStats
	{
		public int		moves;
		public int		crashs;
		public int		basic;
		public double	unscaled;
		public double	scaled;
		public int		bonus1;
		public int		bonus2;
		public double	total;
		public double	expected;

		public UserStats(int init)
		{
			this.moves = init;
			this.crashs = init;
			this.basic = init;
			this.unscaled = init;
			this.scaled = init;
			this.bonus1 = init;
			this.bonus2 = init;
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
			if(s.expected < min.expected)
				min.expected = s.expected;
		}
		return min;
	}

	protected UserStats getMaxs(Collection<UserStats> stats)
	{
		UserStats min = new UserStats(0);
		for(UserStats s : stats)
		{
			if(s.moves > min.moves)
				min.moves = s.moves;
			if(s.crashs > min.crashs)
				min.crashs = s.crashs;
			if(s.basic > min.basic)
				min.basic = s.basic;
			if(s.unscaled > min.unscaled)
				min.unscaled = s.unscaled;
			if(s.scaled > min.scaled)
				min.scaled = s.scaled;
			if(s.bonus1 > min.bonus1)
				min.bonus = s.bonus;
			if(s.bonus2 > min.bonus2)
				min.bonus2 = s.bonus2;
			if(s.total > min.total)
				min.total = s.total;
			if(s.expected > min.expected)
				min.expected = s.expected;
		}
		return min;
	}

	// protected <K, V> TreeMap<K, List<V>[]> createMap(Collection<K> keys, int arraySize)
	// {
	// TreeMap<K, List<V>[]> map = new TreeMap<>();
	// List<?>[] array;
	// List<V> list;
	// for(K k : keys)
	// {
	// array = new ArrayList<?>[arraySize];
	// for(int i = 0; i < arraySize; i++)
	// {
	// list = new ArrayList<V>();
	// array[i] = list;
	// }
	// map.put(k, (List<V>[]) array);
	// }
	// return map;
	// }
	//
	// protected <K, V> TreeMap<K, V[]> createMap(Collection<K> keys, V[] array, V defaultValue)
	// {
	// TreeMap<K, V[]> map = new TreeMap<>();
	// for(K k : keys)
	// {
	// V[] arrayClone = array.clone();
	// if(defaultValue != null)
	// for(int i = 0; i < arrayClone.length; i++)
	// arrayClone[i] = defaultValue;
	// map.put(k, arrayClone);
	// }
	// return map;
	// }

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
