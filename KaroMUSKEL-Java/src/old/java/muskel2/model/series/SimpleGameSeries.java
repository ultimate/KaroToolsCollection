package muskel2.model.series;

import muskel2.model.GameSeries;

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
}
