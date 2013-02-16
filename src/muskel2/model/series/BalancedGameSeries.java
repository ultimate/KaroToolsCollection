package muskel2.model.series;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import muskel2.gui.Screen;
import muskel2.gui.screens.MapsAndRulesScreen;
import muskel2.gui.screens.PlayersScreen;
import muskel2.gui.screens.RulesScreen;
import muskel2.gui.screens.SettingsScreen;
import muskel2.gui.screens.SummaryScreen;
import muskel2.model.Game;
import muskel2.model.GameSeries;
import muskel2.model.Map;
import muskel2.model.Player;
import muskel2.model.Rules;
import muskel2.util.PlaceholderFactory;

public class BalancedGameSeries extends GameSeries
{
	private static final long		serialVersionUID	= 1L;

	public static int				MAX_GAMES_PER_PLAYER	= 5;
	public static int				MAX_MAPS				= 5;

	private int						numberOfMaps;

	private HashMap<Integer, Map>	mapList;
	private HashMap<Integer, Rules>	rulesList;

	protected Player[][]			shuffledPlayers;

	public BalancedGameSeries()
	{
		super("gameseries.balanced.titlepatterns");
	}

	@Override
	protected void initSubType()
	{
		if(karopapier.isUnlocked())
		{
			MAX_GAMES_PER_PLAYER = 1000;
			MAX_MAPS = 1000;
		}
		mapList = new HashMap<Integer, Map>();
		rulesList = new HashMap<Integer, Rules>();
	}

	@Override
	public int getMinSupportedPlayersPerMap()
	{
		return 0;
	}

	public int getNumberOfMaps()
	{
		return numberOfMaps;
	}

	public void setNumberOfMaps(int numberOfMaps)
	{
		this.numberOfMaps = numberOfMaps;
	}

	public void setMap(int i, Map map, Rules rules)
	{
		this.mapList.put(i, map);
		this.rulesList.put(i, rules);
	}

	public Map getMap(int i)
	{
		try
		{
			return mapList.get(i);
		}
		catch(IndexOutOfBoundsException e)
		{
			return null;
		}
	}

	public Rules getRules(int i)
	{
		try
		{
			return rulesList.get(i);
		}
		catch(IndexOutOfBoundsException e)
		{
			return null;
		}
	}

	@SuppressWarnings("unused")
	@Override
	protected Screen createScreens()
	{
		Screen s01 = new SettingsScreen(startScreen, karopapier, previousButton, nextButton);
		Screen s02 = new RulesScreen(s01, karopapier, previousButton, nextButton);
		Screen s03 = new PlayersScreen(s02, karopapier, previousButton, nextButton);
		Screen s04 = new MapsAndRulesScreen(s03, karopapier, previousButton, nextButton);
		Screen s05 = new SummaryScreen(s04, karopapier, previousButton, nextButton);
		return s01;
	}

	protected int shufflePlayers(Rules rules)
	{
		List<Player> tmp = new LinkedList<Player>();
		for(int i = 0; i < rules.getGamesPerPlayer(); i++)
		{
			tmp.addAll(this.players);
		}
		Collections.shuffle(tmp, random);
		
		int games = tmp.size() / rules.getNumberOfPlayers();
		if(tmp.size() % rules.getNumberOfPlayers() != 0)
			games++;
		
		this.shuffledPlayers = new Player[games][rules.getNumberOfPlayers()];
		
		int g = 0;
		int p = 0;
		for(Player player: tmp)
		{
			this.shuffledPlayers[g][p++] = player;
			if(p == rules.getNumberOfPlayers())
			{
				p = 0;
				g++;
			}
		}
		
		// TODO noch ausgewogener, so dass jeder gegen jeden gleichhäufig fährt...
		
		// check duplicates
		for(g = 0; g < games; g++)
		{
			for(int p1 = 0; p1 < this.shuffledPlayers[g].length-1; p1++)
			{
				if(this.shuffledPlayers[g][p1] == null)
					continue;
				for(int p2 = p1+1; p2 < this.shuffledPlayers[g].length; p2++)
				{
					if(this.shuffledPlayers[g][p2] == null)
						continue;
					if(this.shuffledPlayers[g][p1].getId() == this.shuffledPlayers[g][p2].getId())
					{
						// duplicate found
						// look for game without this player
						boolean p2Found;
						boolean newp2Found;
						for(int gSearch = 0; gSearch < games; gSearch++)
						{
							if(gSearch == g)
								continue;
							p2Found = false;
							for(int pSearch = 0; pSearch < this.shuffledPlayers[gSearch].length; pSearch++)
							{
								if(this.shuffledPlayers[gSearch][pSearch].getId() == this.shuffledPlayers[g][p2].getId())
								{
									p2Found = true;
									break;
								}
							}
							if(!p2Found)
							{
								boolean changed = false;
								//look for a player not in this game
								for(int pSearch = 0; pSearch < this.shuffledPlayers[gSearch].length; pSearch++)
								{
									newp2Found = true;
									for(int pCompare = 0; pCompare < this.shuffledPlayers[g].length; pCompare++)
									{
										if(pCompare == p2)
											continue;
										if(this.shuffledPlayers[gSearch][pSearch].getId() == this.shuffledPlayers[g][pCompare].getId())
										{
											newp2Found = false;
											break;
										}
									}	
									if(newp2Found)
									{
										// change players
										Player pTmp = this.shuffledPlayers[gSearch][pSearch];
										this.shuffledPlayers[gSearch][pSearch] = this.shuffledPlayers[g][p2];
										this.shuffledPlayers[g][p2] = pTmp;
										changed = true;
										break;
									}									
								}
								if(changed)
									break;
							}
						}
					}
				}				
			}
		}
		
		return games;
	}

	@Override
	protected void planGames0()
	{
		Game game;
		List<Player> gamePlayers;
		Map map;
		Rules rules;
		String name;
		int games;
		int count = 0;
		int dayCount;
		Rules tmpRules;
		for(int i = 0; i < this.numberOfMaps; i++)
		{
			dayCount = 0;

			map = this.mapList.get(i);
			rules = this.rulesList.get(i);
			games = this.shufflePlayers(rules);

			int gamesBefore = this.games.size();
			
			for(int g = 0; g < games; g++)
			{
				gamePlayers = new LinkedList<Player>();

				for(int p = 0; p < this.shuffledPlayers[g].length; p++)
				{
					if(this.shuffledPlayers[g][p] == null)
						break;
					gamePlayers.add(this.shuffledPlayers[g][p]);
				}
				gamePlayers.add(this.creator);

				increasePlannedGames(gamePlayers);

				tmpRules = rules.clone().createRandomValues();
				name = PlaceholderFactory.applyPlaceholders(this.karopapier, title, map, gamePlayers, tmpRules, count, i, dayCount, null, null, -1);

				game = new Game(name, map, gamePlayers, tmpRules);
				this.games.add(game);
				count++;
				dayCount++;
			}
			
			int gamesAfter = this.games.size();
			System.out.println("map #" + i + " games planned:   " + (gamesAfter - gamesBefore));
		}
	}
}
