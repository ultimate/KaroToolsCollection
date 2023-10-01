package ultimate.karoapi4j.model.extended;

/**
 * Simple POJO that represents a match between 2 {@link Team}s
 * 
 * @author ultimate
 */
public class Match
{
	/**
	 * the teams
	 */
	private Team[] teams;

	/**
	 * Create a new match betwenn n {@link Team}s
	 * 
	 * @param teams
	 */
	public Match(Team... teams)
	{
		super();
		this.teams = teams;
	}

	/**
	 * Get all teams
	 * 
	 * @return teams
	 */
	public Team[] getTeams()
	{
		return teams;
	}

	/**
	 * Get a teams
	 * 
	 * @param index
	 * @return team
	 */
	public Team getTeam(int index)
	{
		return teams[index];
	}

	/**
	 * Helper method that rotates the teams.<br>
	 * (Each team is moved one index forward and the first team is put to the end.)
	 */
	public void rotateTeams()
	{
		Team tmp = teams[0];
		for(int i = 1; i < teams.length; i++)
			teams[i - 1] = teams[i];
		teams[teams.length - 1] = tmp;
	}
}