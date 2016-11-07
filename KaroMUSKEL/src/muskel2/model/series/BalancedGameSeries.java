package muskel2.model.series;

import java.util.ArrayList;
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
import muskel2.util.BalancedShuffling;
import muskel2.util.PlaceholderFactory;

public class BalancedGameSeries extends GameSeries
{
	private static final long	serialVersionUID		= 1L;

	public static int			MAX_GAMES_PER_PLAYER	= 5;
	public static int			MAX_MAPS				= 5;

	private int					numberOfMaps;

	private HashMap<Integer, Map>	mapList;
	private HashMap<Integer, Rules>	rulesList;

	protected Player[][]		shuffledPlayers;

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
		
		List<Rules> tmpRulesList = new ArrayList<Rules>(numberOfMaps);
		for(int i = 0; i < numberOfMaps; i++)
			tmpRulesList.add(this.rulesList.get(i));
		
		Player[][][] shuffledPlayers = BalancedShuffling.shufflePlayers(this.players, tmpRulesList);

		for(int i = 0; i < this.numberOfMaps; i++)
		{
			dayCount = 0;

			map = this.mapList.get(i);
			rules = this.rulesList.get(i);
			games = shuffledPlayers[i].length;
			
			int gamesBefore = this.games.size();

			for(int g = 0; g < games; g++)
			{
				gamePlayers = new LinkedList<Player>();

				for(int p = 0; p < shuffledPlayers[i][g].length; p++)
				{
					if(shuffledPlayers[i][g][p] == null)
						continue;
					gamePlayers.add(shuffledPlayers[i][g][p]);
				}
				gamePlayers.add(this.creator);

				increasePlannedGames(gamePlayers);

				tmpRules = rules.clone().createRandomValues();
				name = PlaceholderFactory.applyPlaceholders(this.karopapier, title, map, gamePlayers, tmpRules, count, i, dayCount, null, -1);

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
