package muskel2.model.help;

import java.io.Serializable;
@Deprecated
public class Match implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	private Team[]	teams;

	public Match(Team... teams)
	{
		super();
		this.teams = teams;
	}
	
	public Team[] getTeams()
	{
		return teams;
	}

	public Team getTeam(int index)
	{
		return teams[index];
	}

	public void rotateTeams()
	{
		Team tmp = teams[0];
		for(int i = 1; i < teams.length; i++)
			teams[i-1] = teams[i];
		teams[teams.length-1] = tmp;
	}
}
