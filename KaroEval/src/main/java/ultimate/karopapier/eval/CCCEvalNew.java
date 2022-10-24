package ultimate.karopapier.eval;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumPlayerStatus;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.CollectionsUtil;
import ultimate.karopapier.utils.Table;
import ultimate.karopapier.utils.WikiUtil;

public class CCCEvalNew extends CCCEval
{
	protected static final String	STATUS_RACING			= "&#127950;";			// race car
	protected static final String	STATUS_FINISHED			= "&#127937;";			// race flag
	protected static final String	STATUS_LEFT				= "&#128128;";			// skull
	protected static final String	STATUS_FORBIDDEN		= "&#128683;";			// forbidden

	protected static final String	PROPERTY_CLUSTER_SIZE	= "points.clusterSize";
	protected static final String	PROPERTY_CALC_MODE		= "points.calcMode";
	protected static final String	PROPERTY_CAP_NEGATIVE	= "points.capNegative";

	// read from file
	protected int					CLUSTER_SIZE;
	protected String				CALC_MODE;
	protected boolean				CAP_NEGATIVE;

	// calculated
	protected double				stats_challengePointsMax;
	protected double				stats_challengePointsMid;
	protected double				stats_challengePointsMin;

	public CCCEvalNew(int cccx)
	{
		super(cccx, false);
	}

	@Override
	public void prepare(KaroAPICache karoAPICache, GameSeries gameSeries, Properties properties, File folder, int execution)
	{
		super.prepare(karoAPICache, gameSeries, properties, folder, execution);

		// points & calculation settings (read from properties)
		this.CLUSTER_SIZE = Integer.valueOf(properties.getProperty(PROPERTY_CLUSTER_SIZE));
		this.CALC_MODE = properties.getProperty(PROPERTY_CALC_MODE);
		this.CAP_NEGATIVE = Boolean.valueOf(properties.getProperty(PROPERTY_CAP_NEGATIVE));

		this.stats_challengePointsMax = (int) Math.ceil(this.stats_players / (double) this.CLUSTER_SIZE);
		this.stats_challengePointsMid = (this.stats_challengePointsMax - this.stats_challengePointsMin) / 2.0 + this.stats_challengePointsMin;
		this.stats_challengePointsMin = 1;

		this.stats_challengePointsMax = calculatePoints(this.stats_challengePointsMax, this.stats_challengePointsMax);
		this.stats_challengePointsMid = calculatePoints(this.stats_challengePointsMid, this.stats_challengePointsMid);
		this.stats_challengePointsMin = calculatePoints(this.stats_challengePointsMin, this.stats_challengePointsMin);

		logger.info("  " + PROPERTY_CLUSTER_SIZE + "         = " + this.CLUSTER_SIZE);
		logger.info("  " + PROPERTY_CALC_MODE + "            = " + this.CALC_MODE);
		logger.info("  " + PROPERTY_CAP_NEGATIVE + "         = " + this.CAP_NEGATIVE);
		logger.info("  challengePointsMax         = " + this.stats_challengePointsMax);
		logger.info("  challengePointsMid         = " + this.stats_challengePointsMid);
		logger.info("  challengePointsMin         = " + this.stats_challengePointsMin);
	}

	@Override
	protected String[] createFinalTableHead()
	{
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
		return finalTableHead;
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
		logger.debug("  expected  = " + mins.totalExpected + "\t| " + maxs.totalExpected);

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

			if(rows > 0 && (finished ? us.total == usLast.total : us.totalExpected == usLast.totalExpected) && us.crashs == usLast.crashs && us.moves == usLast.moves)
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

				if(userChallengeStats[c].get(user.getId()).total >= stats_challengePointsMax)
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
			row[col++] = us.totalExpected;

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

					challengeStats[c].moves += moves;
					challengeStats[c].crashs += crashs;

					totalStats.moves += moves;
					totalStats.crashs += crashs;
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
		
		int offset = challengeGames[c] * COLS_PER_RACE;
		assignPointsAndSort(totalTables[c], c, offset + 1, offset + 2, offset + 3, offset + 4, totalTables[c].getColumns() - 1, false);
	}

	protected void assignPointsAndSort(Table table, int c, int movesColumn, int movesPointsColumn, int crashsColumn, int crashPointsColumn, int resultColumn, boolean isExpected)
	{
		// apply points by moves
		assignPoints(table, movesColumn, movesPointsColumn, CollectionsUtil.ASCENDING);
		// apply points by crashs
		assignPoints(table, crashsColumn, crashPointsColumn, CollectionsUtil.DESCENDING);
		// calculate product
		int movesPoints, crashPoints;
		double totalPoints;
		User user;
		for(int r = 0; r < table.getRows().size(); r++)
		{
			user = (User) table.getValue(r, 0);
			movesPoints = (int) table.getValue(r, movesPointsColumn);
			crashPoints = (int) table.getValue(r, crashPointsColumn);

			totalPoints = calculatePoints(movesPoints, crashPoints);

			table.setValue(r, resultColumn, totalPoints);

			if(!isExpected)
			{
				userChallengeStats[c].get(user.getId()).total = totalPoints;
				userStats.get(user.getId()).total += totalPoints;
			}
			else
			{
				userChallengeStats[c].get(user.getId()).totalExpected = totalPoints;
				userStats.get(user.getId()).totalExpected += totalPoints;
			}
		}
		// resort by name (was resorted in assignPoints)
		// users can sort the table by their need since it is "sortable", but a fixed order by name, makes it easier to identify changes
		table.sort(0, (Comparator<User>) (m1, m2) -> {
			return m1.getLoginLowerCase().compareTo(m2.getLoginLowerCase());
		});
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

	protected double calculatePoints(double movesPoints, double crashPoints)
	{
		double points = 0;

		switch(CALC_MODE)
		{
			case "multiply":
				points = movesPoints * crashPoints;
				break;

			case "add":
				points = movesPoints + crashPoints;
				break;

			case "sqrt":
				points = Math.sqrt(movesPoints * crashPoints);
				break;
		}
		if(CAP_NEGATIVE && points < 0)
			return 0;
		return points;
	}

	protected void calculateExpected()
	{

		int actualMoves, actualCrashs;
		int expectedMoves, expectedCrashs;
		int avgMoves, avgCrashs;

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

			Table tmpTable = new Table(new String[] { "User", "Finished Games", "Moves Actual", "Moves Expected", "Points", "Crashs Actual", "Crashs Expected", "Points", "Erwartungswert", "Aktuelle Punkte" });

			if(challengeStats[c].finished > 0)
			{
				avgMoves = 0;
				avgCrashs = 0;

				for(int g = 0; g < challengeGames[c]; g++)
				{
					for(UserStats ugs2 : userGameStats[c][g].values())
					{
						if(ugs2.finished == 1)
						{
							avgMoves += ugs2.moves;
							avgCrashs += ugs2.crashs;
						}
					}
				}

				avgMoves /= challengeStats[c].finished;
				avgCrashs /= challengeStats[c].finished;
			}
			else
			{
				avgMoves = 100;
				avgCrashs = 100;
			}

			for(User user : usersByLogin)
			{
				if(userChallengeStats[c].get(user.getId()).finished == 0)
				{
					actualMoves = 0;
					actualCrashs = 0;

					expectedMoves = avgMoves;
					expectedCrashs = avgCrashs;
				}
				else
				{
					actualMoves = 0;
					actualCrashs = 0;

					for(int g = 0; g < challengeGames[c]; g++)
					{
						ugs = userGameStats[c][g].get(user.getId());

						if(ugs.finished == 1)
						{
							actualMoves += ugs.moves;
							actualCrashs += ugs.crashs;
						}
					}

					expectedMoves = actualMoves * stats_gamesPerPlayerPerChallenge / userChallengeStats[c].get(user.getId()).finished;
					expectedCrashs = actualCrashs * stats_gamesPerPlayerPerChallenge / userChallengeStats[c].get(user.getId()).finished;

				}
				tmpTable.addRow(user, userChallengeStats[c].get(user.getId()).finished, actualMoves, expectedMoves, null, actualCrashs, expectedCrashs, null, null, userChallengeStats[c].get(user.getId()).total);
			}

			assignPointsAndSort(tmpTable, c, 3, 4, 6, 7, tmpTable.getColumns() - 2, true);

			logger.debug("\n" + WikiUtil.toDebugString(tmpTable, 20));
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
			else if(!finished && o2.getValue().totalExpected != o1.getValue().totalExpected)
				return (int) Math.signum(o2.getValue().totalExpected - o1.getValue().totalExpected); // mehr
			if(o2.getValue().crashs != o1.getValue().crashs)
				return (int) Math.signum(o2.getValue().crashs - o1.getValue().crashs); // mehr
			if(o2.getValue().moves != o1.getValue().moves)
				return (int) Math.signum(o1.getValue().moves - o2.getValue().moves); // weniger!
			return 0;
		}
	}
}
