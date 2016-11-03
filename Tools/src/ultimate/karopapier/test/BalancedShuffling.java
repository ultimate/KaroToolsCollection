package test;

import java.awt.Color;
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
		int totalPlayers = 42;
		int numberOfMaps = 15;
		int gamesPerPlayer = 5;
		
		List<Player> players = new LinkedList<Player>();
		for(char c = 65; c < 65 + totalPlayers; c++)
		{
			players.add(new Player((int) c, "" + c, true, true, 9999, 0, 0, 999, new Color(c, c, c)));
		}

		BalancedShuffling bs = new BalancedShuffling(totalPlayers, numberOfMaps, players);

		List<Rules> rules = new LinkedList<Rules>();
		for(int i = 0; i < numberOfMaps; i++)
		{
			int numberOfPlayers = (i < 5 ? 5 : (i < 10 ? 7 : 10));
			rules.add(new Rules(0, 0, true, true, Direction.egal, false, false, gamesPerPlayer, numberOfPlayers));
		}

		bs.shufflePlayers(rules);
		
		// print games
//		for(int m = 0; m < numberOfMaps; m++)
//		{
//			System.out.println(m);
//			for(int g = 0; g < bs.shuffledPlayers[m].length; g++)
//			{
//				System.out.print((g > 9 ? "" : " ") + g + ": ");
//				for(int p = 0; p < rules.get(m).getNumberOfPlayers(); p++)
//				{
//					if(p >= 0)
//						System.out.print(players.get(bs.shuffledPlayers[m][g][p]).getName() + " ");
//					else
//						System.out.println("  ");
//				}
//				System.out.println("");
//			}
//		}
		
		// print playersGames
//		System.out.println("playersGames");
//		for(int pl = 0; pl < totalPlayers; pl++)
//		{
//			for(int m = 0; m < numberOfMaps; m++)
//			{
//				int count = 0;
//				for(int g = 0; g < bs.shuffledPlayers[m].length; g++)
//				{
//					for(int p = 0; p < rules.get(m).getNumberOfPlayers(); p++)
//					{
//						if(bs.shuffledPlayers[m][g][p] == pl)
//							count++;
//					}
//				}
//				System.out.print(count + " ");
//			}
//			System.out.println("");
//		}
		
		// craete whoOnWho
		int[][][] whoOnWho = new int[numberOfMaps][totalPlayers][totalPlayers];
		int[][] totalWhoOnWho = new int[totalPlayers][totalPlayers];
		for(int pl1 = 0; pl1 < totalPlayers; pl1++)
		{
			for(int pl2 = pl1+1; pl2 < totalPlayers; pl2++)
			{
				for(int m = 0; m < numberOfMaps; m++)
				{
					for(int g = 0; g < bs.shuffledPlayers[m].length; g++)
					{
						boolean p1 = false;
						boolean p2 = false;
						for(int p = 0; p < rules.get(m).getNumberOfPlayers(); p++)
						{
							if(bs.shuffledPlayers[m][g][p] == pl1)
								p1 = true;
							if(bs.shuffledPlayers[m][g][p] == pl2)
								p2 = true;
						}
						if(p1 && p2)
						{
							whoOnWho[m][pl1][pl2]++;
							whoOnWho[m][pl2][pl1]++;
							totalWhoOnWho[pl1][pl2]++;
							totalWhoOnWho[pl2][pl1]++;
						}
					}
				}
			}
		}
		
		// print totalWhoOnWho
		for(int pl1 = 0; pl1 < totalPlayers; pl1++)
		{
			for(int pl2 = 0; pl2 < totalPlayers; pl2++)
			{
				System.out.print((totalWhoOnWho[pl1][pl2] > 9 ? "" : " ") + totalWhoOnWho[pl1][pl2] + " ");	
				//System.out.print("(" + (bs.whoOnWho[pl1][pl2] > 9 ? "" : " ") + bs.whoOnWho[pl1][pl2] + ") ");	
			}
			System.out.println("");
		}
		
		// print whoOnWhos
//		for(int m = 0; m < numberOfMaps; m++)
//		{
//			for(int pl1 = 0; pl1 < totalPlayers; pl1++)
//			{
//				for(int pl2 = 0; pl2 < totalPlayers; pl2++)
//				{
//					System.out.print((whoOnWho[m][pl1][pl2] > 9 ? "" : " ") + whoOnWho[m][pl1][pl2] + " ");	
//				}
//				System.out.println("");
//			}
//			System.out.println("");
//		}
	}

	public List<Player>	players;

	protected int[][][]	shuffledPlayers;
	protected int		numberOfMaps;
	protected int[][] 	whoOnWho;

	public BalancedShuffling(int totalPlayers, int numberOfMaps, List<Player> players)
	{
		super();
		this.players = new LinkedList<Player>();
		this.numberOfMaps = numberOfMaps;
		this.players = players;
	}

	protected void shufflePlayers(List<Rules> rules)
	{			
		// shuffeln kann manchmal fehlschlagen
		// (weil sonst ein spieler doppelt in einem Rennen sein müsste)
		// --> dann versuch es einfach nochmal...
		while(true)
		{
			try
			{
				shufflePlayers0(rules);
				break;
			}
			catch(IllegalArgumentException e)
			{
				continue;
			}
		}
	}
	
	protected void shufflePlayers0(List<Rules> rules)
	{		
		Random rand = new Random();
		int[] playersGames;
		
		this.shuffledPlayers = new int[this.numberOfMaps][][];
		this.whoOnWho = new int[this.players.size()][this.players.size()];
		
		for(int m = 0; m < this.shuffledPlayers.length; m++)
		{
			int playerGames = rules.get(m).getGamesPerPlayer() * this.players.size();
			int games = playerGames / rules.get(m).getNumberOfPlayers();
			if(playerGames % rules.get(m).getNumberOfPlayers() != 0)
				games++;
		
			this.shuffledPlayers[m] = new int[games][rules.get(m).getNumberOfPlayers()];
			
			int g = 0;
			int p = 0;
			
			playersGames = new int[this.players.size()];
			
			while(playerGames > 0)
			{				
				if(p == 0)
				{
					// suche Spieler mit geringster Spielezahl
					int minGames = rules.get(m).getGamesPerPlayer();
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
					for(int row = 0; row < whoOnWho.length; row++)
					{
						if(!potentials.contains(row))
							continue;
						for(int col = row+1; col < whoOnWho.length; col++)
						{
							if(!potentials.contains(col))
								continue;
							
							if(whoOnWho[row][col] == minBattles)
							{
								potentials2a.add(row);
								potentials2b.add(col);
							}
							else if(whoOnWho[row][col] < minBattles)
							{
								potentials2a.clear();
								potentials2b.clear();
								potentials2a.add(row);
								potentials2b.add(col);
								minBattles = whoOnWho[row][col];
							}
						}
					}
					
					int r = rand.nextInt(potentials2a.size());
					
					// add player
					this.shuffledPlayers[m][g][p++] = potentials2a.get(r);
					this.shuffledPlayers[m][g][p++] = potentials2b.get(r);
					
					// update playersGames
					playersGames[potentials2a.get(r)]++;
					playersGames[potentials2b.get(r)]++;				

					// update whoOnWho
					whoOnWho[potentials2a.get(r)][potentials2b.get(r)]++;
					whoOnWho[potentials2b.get(r)][potentials2a.get(r)]++;

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
						rows.add(this.shuffledPlayers[m][g][pl]);
						cols.add(this.shuffledPlayers[m][g][pl]);
					}					
					
					// suche nicht mehr in Frage kommende Spieler (schon max an Spielen)
					// => NICHT zu durchsuchende Spalten
					int maxGames = rules.get(m).getGamesPerPlayer();
					for(int pl = 0; pl < playersGames.length; pl++)
					{
						if(playersGames[pl] == maxGames)
							cols.add(pl);
					}					
					
					// Suche minimale Spaltensumme (seltenster Gegner)
					// (Spaltensumme nur über die Zeilen, cols-Liste ausschließen)
					int minBattles = Integer.MAX_VALUE;
					List<Integer> potentials = new LinkedList<Integer>();
					for(int col = 0; col < whoOnWho.length; col++)
					{
						if(cols.contains(col))
							continue;
						int battles = 0;
						for(int row = 0; row < whoOnWho.length; row++)
						{
							if(!rows.contains(row))
								continue;	
							battles += whoOnWho[row][col]; 	
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
					
					int r = rand.nextInt(potentials.size());

					// add player
					this.shuffledPlayers[m][g][p++] = potentials.get(r);
					
					// update playersGames
					playersGames[potentials.get(r)]++;		
					
					// update whoOnWho
					for(int pl: rows)
					{
						whoOnWho[pl][potentials.get(r)]++;
						whoOnWho[potentials.get(r)][pl]++;
					}

					// update playerGames-Counter
					playerGames--;
				}	
				
				if(p >= rules.get(m).getNumberOfPlayers())
				{
					p = 0;
					g++;
				}
			}
			
			// letztes Rennen auffüllen... (falls nicht voll)
			for(;g < games && p < rules.get(m).getNumberOfPlayers(); p++)
			{
				this.shuffledPlayers[m][g][p++] = -1;
			}
		}
	}
}
