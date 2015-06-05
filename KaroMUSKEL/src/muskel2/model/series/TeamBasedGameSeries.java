package muskel2.model.series;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import muskel2.gui.Screen;
import muskel2.gui.screens.HomeMapsScreen;
import muskel2.gui.screens.MapsScreen;
import muskel2.gui.screens.PlayersScreen;
import muskel2.gui.screens.RulesScreen;
import muskel2.gui.screens.SettingsScreen;
import muskel2.gui.screens.SummaryScreen;
import muskel2.model.GameSeries;
import muskel2.model.help.Team;

public abstract class TeamBasedGameSeries extends GameSeries
{
	private static final long	serialVersionUID	= 1L;

	protected int				numberOfTeams;
	protected int				minPlayersPerTeam;
	protected int				maxPlayersPerTeam;
	protected int				numberOfGamesPerPair;
	protected boolean			useHomeMaps;
	protected boolean			shuffleTeams;
	protected boolean			autoNameTeams;
	protected boolean			multipleTeams;
	protected boolean			creatorTeam;

	protected List<Team>		teams;
	protected List<Team>		shuffledTeams;

	public TeamBasedGameSeries(String patternKey)
	{
		super(patternKey);
		this.teams = new LinkedList<Team>();
	}

	public int getNumberOfTeams()
	{
		return numberOfTeams;
	}

	public int getMinPlayersPerTeam()
	{
		return minPlayersPerTeam;
	}

	public int getMaxPlayersPerTeam()
	{
		return maxPlayersPerTeam;
	}

	public int getNumberOfGamesPerPair()
	{
		return numberOfGamesPerPair;
	}

	public boolean isUseHomeMaps()
	{
		return useHomeMaps;
	}

	public boolean isShuffleTeams()
	{
		return shuffleTeams;
	}

	public boolean isAutoNameTeams()
	{
		return autoNameTeams;
	}
	
	public boolean isMultipleTeams()
	{
		return multipleTeams;
	}

	public boolean isCreatorTeam()
	{
		return creatorTeam;
	}

	public List<Team> getTeams()
	{
		return teams;
	}

	public void setNumberOfTeams(int numberOfTeams)
	{
		this.numberOfTeams = numberOfTeams;
	}

	public void setMinPlayersPerTeam(int minPlayersPerTeam)
	{
		this.minPlayersPerTeam = minPlayersPerTeam;
	}

	public void setMaxPlayersPerTeam(int maxPlayersPerTeam)
	{
		this.maxPlayersPerTeam = maxPlayersPerTeam;
	}

	public void setNumberOfGamesPerPair(int numberOfGamesPerPair)
	{
		this.numberOfGamesPerPair = numberOfGamesPerPair;
	}

	public void setUseHomeMaps(boolean useHomeMaps)
	{
		this.useHomeMaps = useHomeMaps;
	}

	public void setShuffleTeams(boolean shuffleTeams)
	{
		this.shuffleTeams = shuffleTeams;
	}

	public void setAutoNameTeams(boolean autoNameTeams)
	{
		this.autoNameTeams = autoNameTeams;
	}

	public void setMultipleTeams(boolean multipleTeams)
	{
		this.multipleTeams = multipleTeams;
	}

	public void setCreatorTeam(boolean creatorTeam)
	{
		this.creatorTeam = creatorTeam;
	}

	public void setTeams(List<Team> teams)
	{
		this.teams = teams;
	}

	public int getMinSupportedPlayersPerMap()
	{
		return maxPlayersPerTeam * 2 + 1;
	}
	
	protected void shuffleTeams()
	{
		this.shuffledTeams = new LinkedList<Team>(this.teams);
		if(this.shuffleTeams)
		{
			Collections.shuffle(this.shuffledTeams, random);
		}
	}

	@SuppressWarnings("unused")
	@Override
	public Screen createScreens()
	{
		Screen s01 = new SettingsScreen(startScreen, karopapier, previousButton, nextButton);
		Screen s02 = new RulesScreen(s01, karopapier, previousButton, nextButton);
		Screen s03 = new PlayersScreen(s02, karopapier, previousButton, nextButton);
		Screen s04 = new HomeMapsScreen(s03, karopapier, previousButton, nextButton);
		Screen s05 = new MapsScreen(s04, karopapier, previousButton, nextButton);
		Screen s06 = new SummaryScreen(s05, karopapier, previousButton, nextButton);
		return s01;
	}
}
