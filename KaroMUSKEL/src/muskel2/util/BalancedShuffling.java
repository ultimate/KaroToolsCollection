package muskel2.util;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import muskel2.model.Direction;
import muskel2.model.Player;
import muskel2.model.Rules;

public class BalancedShuffling
{
	public static void main(String[] args)
	{
		int totalPlayers = 30;
		int numberOfMaps = 20;
		int gamesPerPlayer = 6;

		List<Player> players = new LinkedList<Player>();
		for(char c = 65; c < 65 + totalPlayers; c++)
		{
			players.add(new Player((int) c, "" + c, true, true, 9999, 0, 0, 999, new Color(c, c, c)));
		}

		List<Rules> rules = new LinkedList<Rules>();
		for(int i = 0; i < numberOfMaps; i++)
		{
//			int numberOfPlayers = (i < 6 ? 5 : (i < 12 ? 7 : 10));
			int numberOfPlayers = (i < 6 ? 3 : (i < 12 ? 4 : 5));
			rules.add(new Rules(0, 0, true, true, Direction.egal, false, false, gamesPerPlayer, numberOfPlayers));
		}

		Player[][][] shuffledPlayers = shufflePlayers(players, rules);

		// print games
		for(int m = 0; m < numberOfMaps; m++)
		{
			System.out.println(m);
			for(int g = 0; g < shuffledPlayers[m].length; g++)
			{
				System.out.print((g > 9 ? "" : " ") + g + ": ");
				for(int p = 0; p < rules.get(m).getNumberOfPlayers(); p++)
				{
					if(p >= 0)
					{
						if(shuffledPlayers[m][g][p] != null)
							System.out.print(shuffledPlayers[m][g][p].getName() + " ");
					}
					else
						System.out.println(" ");
				}
				System.out.println("");
			}
		}

		// print playersGames
		System.out.println("playersGames");
		for(int pl = 0; pl < totalPlayers; pl++)
		{
			for(int m = 0; m < numberOfMaps; m++)
			{
				int count = 0;
				for(int g = 0; g < shuffledPlayers[m].length; g++)
				{
					for(int p = 0; p < rules.get(m).getNumberOfPlayers(); p++)
					{
						if(shuffledPlayers[m][g][p] == players.get(pl))
							count++;
					}
				}
				System.out.print(count + " ");
			}
			System.out.println("");
		}

	}

	public static Player[][][] shufflePlayers(List<Player> players, List<Rules> rules)
	{
		List<Player> tmp = new LinkedList<Player>(players);
		Collections.shuffle(tmp);
		// shuffeln kann manchmal fehlschlagen
		// (weil sonst ein spieler doppelt in einem Rennen sein müsste)
		// --> dann versuch es einfach nochmal...
		while(true)
		{
			try
			{
				ShuffleResult result = shufflePlayers0(tmp, rules);
				printWhoOnWho(result);
				return result.shuffledPlayers;
			}
			catch(IllegalArgumentException e)
			{
				continue;
			}
		}
	}

	protected static ShuffleResult shufflePlayers0(List<Player> players, List<Rules> rules)
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

			result.shuffledPlayers[r] = new Player[games][rules.get(r).getNumberOfPlayers()];

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
					result.shuffledPlayers[r][g][p++] = players.get(potentials2a.get(ri));
					result.shuffledPlayers[r][g][p++] = players.get(potentials2b.get(ri));

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
						rows.add(players.indexOf(result.shuffledPlayers[r][g][pl]));
						cols.add(players.indexOf(result.shuffledPlayers[r][g][pl]));
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
					result.shuffledPlayers[r][g][p++] = players.get(potentials.get(ri));

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
				result.shuffledPlayers[r][g][p] = null;
			}
		}

		return result;
	}

	protected static void printWhoOnWho(ShuffleResult result)
	{
		// print totalWhoOnWho
		for(int pl1 = 0; pl1 < result.totalWhoOnWho.length; pl1++)
		{
			for(int pl2 = 0; pl2 < result.totalWhoOnWho[pl1].length; pl2++)
			{
				System.out.print((result.totalWhoOnWho[pl1][pl2] > 9 ? "" : " ") + result.totalWhoOnWho[pl1][pl2] + " ");
				// System.out.print("(" + (bs.whoOnWho[pl1][pl2] > 9 ? "" : " ") +
				// bs.whoOnWho[pl1][pl2] + ") ");
			}
			System.out.println("");
		}

		// print whoOnWhos
		// for(int m = 0; m < result.whoOnWho.length; m++)
		// {
		// for(int pl1 = 0; pl1 < result.whoOnWho[m].length; pl1++)
		// {
		// for(int pl2 = 0; pl2 < result.whoOnWho[m][pl1].length; pl2++)
		// {
		// System.out.print((result.whoOnWho[m][pl1][pl2] > 9 ? "" : " ") +
		// result.whoOnWho[m][pl1][pl2] + " ");
		// }
		// System.out.println("");
		// }
		// System.out.println("");
		// }
	}

	private static class ShuffleResult
	{
		private Player[][][]	shuffledPlayers;
		private int[][][]	whoOnWho;
		private int[][]		totalWhoOnWho;

		private ShuffleResult(int numberOfPlayers, int numberOfRounds)
		{
			this.shuffledPlayers = new Player[numberOfRounds][][];
			this.totalWhoOnWho = new int[numberOfPlayers][numberOfPlayers];
			this.whoOnWho = new int[numberOfRounds][numberOfPlayers][numberOfPlayers];
		}
	}
}
