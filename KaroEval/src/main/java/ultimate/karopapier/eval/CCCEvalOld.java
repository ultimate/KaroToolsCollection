package ultimate.karopapier.eval;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import ultimate.karopapier.utils.Table;
import ultimate.karopapier.utils.Table.Cell;
import ultimate.karopapier.utils.WikiUtil;

public class CCCEvalOld extends CCCEval
{
	protected static final String[]							TABLE_HEAD_GAME	= new String[] { "Platz", "Spieler", "Grundpunkte", "Crashs", "Züge", "Punkte" };
	protected static final int								BONUS_CHALLENGE	= 10;
	protected static final int								BONUS_FINAL		= 100;

	// helpers
	protected HashMap<Integer, HashMap<Integer, Double>>	pointsPerRank;

	public CCCEvalOld(int cccx)
	{
		super(cccx, true, true);
	}

	@Override
	public void prepare(KaroAPICache karoAPICache, GameSeries gameSeries, Properties properties, File folder, int execution)
	{
		super.prepare(karoAPICache, gameSeries, properties, folder, execution);

		// points & calculation settings (read from properties)
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
				if(i2 < 0) // only process "points.x.y"
					continue;
				number = Integer.parseInt(keyS.substring(i1 + 1, i2));
				place = Integer.parseInt(keyS.substring(i2 + 1));
				points = Double.parseDouble(properties.getProperty(keyS));
				if(!this.pointsPerRank.containsKey(number))
					this.pointsPerRank.put(number, new HashMap<>());
				this.pointsPerRank.get(number).put(place, points);
			}
		}
		for(int i = 2; i < 10; i++)
			if(this.pointsPerRank.containsKey(i))
				logger.info("  pointsPerRank(" + i + ")           = " + pointsPerRank.get(i).values());
	}

	@Override
	protected String[] createFinalTableHead()
	{
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
		return finalTableHead;
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
					row[col] = WikiUtil.highlight(WikiUtil.preprocess(row[col]));

				if(userChallengeStats[c].get(user.getId()).finished < stats_gamesPerPlayerPerChallenge)
				{
					row[col] = WikiUtil.preprocess(row[col]) + "&nbsp;<span style=\"font-size:50%\">(" + (stats_gamesPerPlayerPerChallenge - userChallengeStats[c].get(user.getId()).finished) + ")</span>";
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
					for(Cell[] tableRow : tables[c][g].getRows())
					{
						if(((Player) tableRow[1].value).getName().equals(user.getLogin()))
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
		logger.debug("creating tables for challenge #" + (c + 1) + "." + (g + 1));

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

		// crashs --> need to count them, because of possible duplicates
		crashs = 0;
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
			moves = player.getMoveCount() - 1; // parc ferme
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
			basicPoints = calcBasicPoints(game.getPlayers().size() - 1, player.getStatus(), player.getRank(), moves, crashs);

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

	protected double calcBasicPoints(int players, EnumPlayerStatus status, int rank, int moves, int crashs)
	{
		if(status == EnumPlayerStatus.ok && rank > 0)
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
			if(userChallengeStats[c].get(user.getId()).basic == maxs.basic)
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
			if(userStats.get(user.getId()).basic == maxs.basic)
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

	protected void calculateExpected()
	{
		UserStats ugs;
		for(int c = 0; c < this.stats_challengesCreated; c++)
		{
			logger.debug("calculating expected for challenge #" + (c + 1));

			double crash_finished_count = 0;
			double crash_finished_players = 0;
			double crash_allraces_count = 0;
			double crash_allraces_players = 0;
			double total_finished_players = 0;

			for(User user : usersByLogin)
			{
				for(int g = 0; g < challengeGames[c]; g++)
				{
					ugs = userGameStats[c][g].get(user.getId());

					if(ugs.crashs > 0)
					{
						// count how often players have crashed in this challenge (in all games)
						crash_allraces_count += ugs.crashs;
						crash_allraces_players++;

						// count how often players have crashed in this challenge (in finished games only)
						if(ugs.basic > 0)
						{
							crash_finished_count += ugs.crashs;
							crash_finished_players++;
						}
					}
					// count how many players have finished a game
					if(ugs.basic > 0)
					{
						total_finished_players++;
					}
				}
			}
			// if there are finished games in this challenge --> use crash average for those
			double avg_crashs = 0;
			if(crash_finished_players != 0)
				avg_crashs = crash_finished_count / crash_finished_players;
			// otherwise use the crash average for all games in this challenge
			else if(crash_allraces_players != 0)
				avg_crashs = crash_allraces_count / crash_allraces_players;

			// get the average points = points for the mid rank
			double avg_points = pointsPerRank.get(getRules(c).getNumberOfPlayers()).get(getRules(c).getNumberOfPlayers() / 2);

			logger.debug("  crash_finished_count   = " + crash_finished_count);
			logger.debug("  crash_finished_players = " + crash_finished_players);
			logger.debug("  crash_allraces_count   = " + crash_allraces_count);
			logger.debug("  crash_allraces_players = " + crash_allraces_players);
			logger.debug("  avg_crashs             = " + avg_crashs);
			logger.debug("  avg_points             = " + avg_points);

			double expected_max = 0;

			for(User user : usersByLogin)
			{
				double player_avg_crashs = 0;
				double player_avg_points = 0;
				double actual_positive = 0;
				double actual_negative = 0;
				int positive_count = 0;
				int negative_count = 0;
				double expected;

				for(int g = 0; g < challengeGames[c]; g++)
				{
					ugs = userGameStats[c][g].get(user.getId());
					if(ugs.moves == 0)
						continue;

					if(ugs.unscaled > 0)
					{
						actual_positive += ugs.unscaled;
						player_avg_crashs += ugs.crashs;
						player_avg_points += ugs.basic;
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
				userChallengeStats[c].get(user.getId()).unscaledExpected = expected;
				userStats.get(user.getId()).unscaledExpected += expected;

				if(expected > expected_max)
					expected_max = expected;
			}

			for(User user : usersByLogin)
			{
				if(total_finished_players > 0)
					userChallengeStats[c].get(user.getId()).scaledExpected = userChallengeStats[c].get(user.getId()).unscaledExpected * 100 / expected_max;
				else
					userChallengeStats[c].get(user.getId()).scaledExpected = 50.0;

				userStats.get(user.getId()).scaledExpected += userChallengeStats[c].get(user.getId()).scaledExpected;

				logger.trace("  " + user.getLogin() + "\t: unscaled = " + WikiUtil.round(userChallengeStats[c].get(user.getId()).unscaled) + "\t -> expected = " + WikiUtil.round(userChallengeStats[c].get(user.getId()).unscaledExpected)
						+ "\t -> delta = " + WikiUtil.round(userChallengeStats[c].get(user.getId()).unscaledExpected - userChallengeStats[c].get(user.getId()).unscaled));
				logger.trace("  " + user.getLogin() + "\t: scaled   = " + WikiUtil.round(userChallengeStats[c].get(user.getId()).scaled) + "\t -> expected = " + WikiUtil.round(userChallengeStats[c].get(user.getId()).scaledExpected) + "\t -> delta = "
						+ WikiUtil.round(userChallengeStats[c].get(user.getId()).scaledExpected - userChallengeStats[c].get(user.getId()).scaled));
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
}
