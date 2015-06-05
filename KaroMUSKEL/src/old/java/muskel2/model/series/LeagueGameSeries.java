package muskel2.model.series;


public class LeagueGameSeries extends TeamBasedGameSeries
{
	private static final long	serialVersionUID	= 1L;

	public static int			MAX_TEAMS			= 8;
	public static int			MAX_ROUNDS			= 8;

	public LeagueGameSeries()
	{
		super("gameseries.league.titlepatterns");
	}
}
