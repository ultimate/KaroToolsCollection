package muskel2.model.series;

import java.util.List;

import muskel2.model.GameSeries;
import muskel2.model.help.Team;

@Deprecated
public abstract class TeamBasedGameSeries extends GameSeries
{
	public static final long	serialVersionUID	= 1L;

	public int					numberOfTeams;
	public int					minPlayersPerTeam;
	public int					maxPlayersPerTeam;
	public int					numberOfGamesPerPair;
	public boolean				useHomeMaps;
	public boolean				shuffleTeams;
	public boolean				autoNameTeams;
	public boolean				multipleTeams;
	public boolean				creatorTeam;

	public List<Team>			teams;
	public List<Team>			shuffledTeams;
}
