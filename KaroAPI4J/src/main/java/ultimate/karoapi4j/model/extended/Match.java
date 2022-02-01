package ultimate.karoapi4j.model.extended;

/**
 * Simple POJO that represents a match between 2 {@link Team}s
 * 
 * @author ultimate
 */
public class Match
{
	/**
	 * Team 1
	 */
	private Team				team1;
	/**
	 * Team 2
	 */
	private Team				team2;

	/**
	 * Create a new match betwenn 2 {@link Team}s
	 * 
	 * @param team1 - Team 1
	 * @param team2 - Team 2
	 */
	public Match(Team team1, Team team2)
	{
		super();
		this.team1 = team1;
		this.team2 = team2;
	}

	/**
	 * Get Team 1
	 * @return Team 1
	 */
	public Team getTeam1()
	{
		return team1;
	}

	/**
	 * Get Team 2
	 * @return Team 2
	 */
	public Team getTeam2()
	{
		return team2;
	}

	/**
	 * Helper method that swaps both teams.
	 */
	public void swapTeams()
	{
		Team tmp = team1;
		team1 = team2;
		team2 = tmp;
	}
}
