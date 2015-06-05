package muskel2.model.series;


public class KOGameSeries extends TeamBasedGameSeries
{
	private static final long	serialVersionUID	= 1L;

	public static int			MAX_TEAMS			= 16;
	public static int			MAX_ROUNDS			= 3;

	public KOGameSeries()
	{
		super("gameseries.ko.titlepatterns");
	}
}
