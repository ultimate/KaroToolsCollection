package muskel2.model.series;

import muskel2.model.GameSeries;

@Deprecated
public class SimpleGameSeries extends GameSeries
{
	private static final long	serialVersionUID	= 1L;

	public int					numberOfGames;
	public int					minPlayersPerGame;
	public int					maxPlayersPerGame;

	public static int			MAX_GAMES			= 20;
}
