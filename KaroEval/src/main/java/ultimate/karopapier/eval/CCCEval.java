package ultimate.karopapier.eval;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoapi4j.model.official.User;
import ultimate.karopapier.utils.Table;
import ultimate.karopapier.utils.WikiUtil;

public abstract class CCCEval extends Eval<GameSeries>
{
	protected static final String				GAMES_KEY				= "Balanced";
	protected static final String[]				TABLE_HEAD_MAPS			= new String[] { "Nr.", "Strecke", "Spielerzahl", "ZZZ", "CPs", "Spielzahl", "Startdatum", "Laufende Spiele", "Abgeschlossene Spiele", "Letzter Zug", "Züge insgesamt", "Ø Züge p.S.p.R.", "Crashs insgesamt", "Ø Crashs p.S.p.R." };
	protected static final int					METRICS_GAME_MAXMOVES	= 0;
	protected final int							TABLE_TABLE_COLUMNS		= 7;

	protected int								cccx;
	protected boolean							includeTableTables;
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
	protected Table[][]							tables;
	protected Table[]							totalTables;
	protected Table								finalTable;
	protected String[]							finalTableHead;
	protected Table								whoOnWho;

	public CCCEval(int cccx, boolean includeTableTables)
	{
		this.cccx = cccx;
		this.includeTableTables = includeTableTables;
	}

	@Override
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
		List<File> filesUpdated = createWiki(schema, finished, includeTableTables);
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

		// get custom header
		this.finalTableHead = createFinalTableHead();

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
		this.whoOnWho.addRow(whoOnWhoHead); // don't add this as a header, but as the first row
		int col = 1; // leave first column empty
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

	protected abstract String[] createFinalTableHead();

	protected List<File> createWiki(String schema, boolean finished, boolean includeTableTables) throws IOException
	{
		List<File> filesUpdated = new LinkedList<>();

		StringBuilder detail = new StringBuilder();
		StringBuilder detailLinks = new StringBuilder();

		Table tableTable;
		Table mapTable = new Table(TABLE_HEAD_MAPS);
		Rules rules;
		Object[] row;
		int col;
		PlannedGame game;
		
		for(int c = 0; c < stats_challengesCreated; c++)
		{
			rules = this.getRules(c);
			row = new Object[TABLE_HEAD_MAPS.length];
			col = 0;
			// Nr.
			row[col++] = challengeToLink(c, true);
			// Strecke
			row[col++] = mapToLink(c, true);
			// Spielerzahl
			row[col++] = rules.getNumberOfPlayers();
			// ZZZ
			row[col++] = (rules.getMinZzz() == rules.getMaxZzz() ? rules.getMinZzz() : rules.getMinZzz() + "-" + rules.getMaxZzz());
			// CPs
			row[col++] = (rules.getCps() == null ? "Random" : (rules.getCps() ? "ja" : "nein"));
			// Spielzahl
			row[col++] = challengeGames[c];
			// Startdatum
			row[col++] = challengeStats[c].startedDate;
			// Laufende Spiele
			row[col++] = challengeGames[c] - challengeStats[c].finished;
			// Abgeschlossene Spiele
			row[col++] = challengeStats[c].finished;
			// Letzter Zug
			row[col++] = challengeStats[c].finishedDate;
			// Züge insgesamt
			row[col++] = challengeStats[c].moves;
			// Ø Züge p.S.p.R.
			row[col++] = challengeStats[c].moves / (double) (stats_gamesPerPlayerPerChallenge * stats_players);
			// Crashs insgesamt
			row[col++] = challengeStats[c].crashs;
			// Ø Crashs p.S.p.R.
			row[col++] = challengeStats[c].crashs / (double) (stats_gamesPerPlayerPerChallenge * stats_players);
			mapTable.addRow(row);

			detail = new StringBuilder();
			detail.append("= Challenge " + (c + 1) + " =\r\n");
			detail.append("Strecke: " + mapToLink(c, false) + "\r\n");

			if(includeTableTables)
			{
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
			}

			detail.append("== Tabellarische Auswertung ==\r\n");
			detail.append(WikiUtil.toString(totalTables[c], "alignedright sortable", ""));
			detail.append("\r\n");

			filesUpdated.add(writeFile("czzzcc" + cccx + "-wiki-challenge" + (c + 1) + ".txt", detail.toString()));
			detailLinks.append("*" + challengeToLink(c, true) + "\r\n");
		}

		// add total row to the Map table
		row = new Object[TABLE_HEAD_MAPS.length];
		col = 0;
		// Nr.
		row[col++] = "Gesamt-Statistik";
		// Strecke
		row[col++] = "";
		// Spielerzahl
		row[col++] = "";
		// ZZZ
		row[col++] = "";
		// CPs
		row[col++] = "";
		// Spielzahl
		row[col++] = stats_gamesTotal;
		// Startdatum
		row[col++] = totalStats.startedDate;
		// Laufende Spiele
		row[col++] = totalStats.count - totalStats.finished;
		// Abgeschlossene Spiele
		row[col++] = totalStats.finished;
		// Letzter Zug
		row[col++] = totalStats.finishedDate;
		// Züge insgesamt
		row[col++] = totalStats.moves;
		// Ø Züge p.S.p.R.
		row[col++] = totalStats.moves / (double) (stats_gamesPerPlayerPerChallenge * stats_players * stats_challengesTotal);
		// Crashs insgesamt
		row[col++] = totalStats.crashs;
		// Ø Crashs p.S.p.R.
		row[col++] = totalStats.crashs / (double) (stats_gamesPerPlayerPerChallenge * stats_players * stats_challengesTotal);
		mapTable.addRow(row);
		mapTable.getRow(mapTable.getRows().size() - 1)[0].colspan = 5;

		StringBuilder total = new StringBuilder();

		if(finished) // without "Abgeschlossene Rennen" & "Erwartungswert"
			total.append(WikiUtil.toString(finalTable, "alignedright sortable", WikiUtil.getDefaultColumnConfig(finalTable.getColumns() - 2)));
		else
			total.append(WikiUtil.toString(finalTable, "alignedright sortable", WikiUtil.getDefaultColumnConfig(finalTable.getColumns())));
		
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

		StringBuilder whoOnWhoString = new StringBuilder();
		
		whoOnWhoString.append(WikiUtil.toString(whoOnWho, null));

		String s = new String(schema);
		s = s.replace("${MAPS}", WikiUtil.toString(mapTable, "alignedright sortable"));
		s = s.replace("${DETAIL}", detailLinks.toString());
		s = s.replace("${TOTAL}", total.toString());
		s = s.replace("${STATS}", stats.toString());
		s = s.replace("${WHOONWHO}", whoOnWhoString.toString());

		filesUpdated.add(writeFile("czzzcc" + cccx + "-wiki-overview.txt", s.toString()));

		return filesUpdated;
	}

	protected abstract boolean createTables();

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
		
		totalStats.count++;
		
		if(getGame(c,g).getGame().isFinished())
		{
			challengeStats[c].finished++;
			totalStats.finished++;
		}
		
		if(challengeStats[c].startedDate == null || challengeStats[c].startedDate.getTime() == 0 || (getGame(c,g).getGame().getStarteddate() != null && getGame(c,g).getGame().getStarteddate().before(challengeStats[c].startedDate)))
			challengeStats[c].startedDate = getGame(c,g).getGame().getStarteddate();
		
		if(totalStats.startedDate == null || totalStats.startedDate.getTime() == 0 || (getGame(c,g).getGame().getStarteddate() != null && getGame(c,g).getGame().getStarteddate().before(challengeStats[c].startedDate)))
			totalStats.startedDate = getGame(c,g).getGame().getStarteddate();
		
		if(challengeStats[c].finishedDate == null || challengeStats[c].finishedDate.getTime() == 0 || (getGame(c,g).getGame().getFinisheddate() != null && getGame(c,g).getGame().getFinisheddate().before(challengeStats[c].finishedDate)))
			challengeStats[c].finishedDate = getGame(c,g).getGame().getFinisheddate();
		
		if(totalStats.finishedDate == null || totalStats.finishedDate.getTime() == 0 || (getGame(c,g).getGame().getFinisheddate() != null && getGame(c,g).getGame().getFinisheddate().before(challengeStats[c].finishedDate)))
			totalStats.finishedDate = getGame(c,g).getGame().getFinisheddate();
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

	protected static class UserStats
	{
		public int 		count;
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
		public Date		startedDate;
		public Date		finishedDate;

		public UserStats(int init)
		{
			this.count = init;
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
			this.startedDate = new Date(init == Integer.MAX_VALUE ? Long.MAX_VALUE : init);
			this.finishedDate = new Date(init == Integer.MAX_VALUE ? Long.MAX_VALUE : init);
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
			if(s.count < min.count)
				min.count = s.count;
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
			if(s.startedDate.before(min.startedDate))
				min.startedDate = s.startedDate;
			if(s.finishedDate.before(min.finishedDate))
				min.finishedDate = s.finishedDate;
		}
		return min;
	}

	protected UserStats getMaxs(Collection<UserStats> stats)
	{
		UserStats max = new UserStats(0);
		for(UserStats s : stats)
		{
			if(s.count > max.count)
				max.count = s.count;
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
			if(s.startedDate.after(max.startedDate))
				max.startedDate = s.startedDate;
			if(s.finishedDate.after(max.finishedDate))
				max.finishedDate = s.finishedDate;
		}
		return max;
	}
}
