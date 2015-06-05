package muskel2.model.help;

import java.io.Serializable;

public class Match implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	private Team				team1;
	private Team				team2;

	public Match(Team team1, Team team2)
	{
		super();
		this.team1 = team1;
		this.team2 = team2;
	}

	public Team getTeam1()
	{
		return team1;
	}

	public Team getTeam2()
	{
		return team2;
	}
	
	public void swapTeams()
	{
		Team tmp = team1;
		team1 = team2;
		team2 = tmp;
	}
}
