package muskel2.model.series;

import java.util.LinkedList;
import java.util.List;

import muskel2.gui.Screen;
import muskel2.gui.screens.MapsScreen;
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

public class SimpleGameSeries extends GameSeries
{
	private static final long	serialVersionUID	= 1L;
	
	private int				numberOfGames;
	private int				minPlayersPerGame;
	private int				maxPlayersPerGame;
	
	public static int MAX_GAMES = 20;

	public SimpleGameSeries()
	{
		super("gameseries.simple.titlepatterns");
	}

	@Override
	protected void initSubType()
	{
		if(karopapier.isUnlocked())
			MAX_GAMES = 1000;
	}

	public int getNumberOfGames()
	{
		return numberOfGames;
	}

	public int getMinPlayersPerGame()
	{
		return minPlayersPerGame;
	}

	@Override
	public int getMinSupportedPlayersPerMap()
	{
		return getMinPlayersPerGame();
	}

	public int getMaxPlayersPerGame()
	{
		return maxPlayersPerGame;
	}

	public void setNumberOfGames(int numberOfGames)
	{
		this.numberOfGames = numberOfGames;
	}

	public void setMinPlayersPerGame(int minPlayersPerGame)
	{
		this.minPlayersPerGame = minPlayersPerGame;
	} 
	
	public void setMaxPlayersPerGame(int maxPlayersPerGame)
	{
		this.maxPlayersPerGame = maxPlayersPerGame;
	}
	
	@Override
	protected void planGames0()
	{
		Game game;
		List<Player> gamePlayers;
		List<Player> allPlayers;
		Player player;
		Map map;
		String name;
		int count = 0;
		Rules tmpRules;
		for(int i = 0; i < this.numberOfGames; i++)
		{
			map = this.maps.get(random.nextInt(this.maps.size()));
			
			gamePlayers = new LinkedList<Player>();
			gamePlayers.add(this.creator);
			
			allPlayers = new LinkedList<Player>(this.players);
			
			while(gamePlayers.size() < Math.min(this.maxPlayersPerGame, map.getMaxPlayers()))
			{
				if(allPlayers.size() == 0)
					break;
				player = allPlayers.remove(random.nextInt(allPlayers.size()));
				if(this.rules.isIgnoreInvitable() || player.isInvitable(map.isNight()))
				{
					gamePlayers.add(player);
				}
			}
			
			increasePlannedGames(gamePlayers);

			tmpRules = rules.clone().createRandomValues();
			name = PlaceholderFactory.applyPlaceholders(this.karopapier, title, map, gamePlayers, tmpRules, count, -1, -1, null, null, -1);
			
			game = new Game(name, map, gamePlayers, tmpRules);
			this.games.add(game);
			count++;
		}
	}

	@SuppressWarnings("unused")
	@Override
	public Screen createScreens()
	{
		Screen s01 = new SettingsScreen(startScreen, karopapier, previousButton, nextButton);
		Screen s02 = new RulesScreen(s01, karopapier, previousButton, nextButton);
		Screen s03 = new PlayersScreen(s02, karopapier, previousButton, nextButton);
		Screen s04 = new MapsScreen(s03, karopapier, previousButton, nextButton);
		Screen s05 = new SummaryScreen(s04, karopapier, previousButton, nextButton);
		return s01;
	}
}
