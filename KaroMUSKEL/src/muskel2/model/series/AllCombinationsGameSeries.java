package muskel2.model.series;

import java.util.LinkedList;
import java.util.List;

import muskel2.model.Game;
import muskel2.model.Map;
import muskel2.model.Player;
import muskel2.model.Rules;
import muskel2.model.help.Match;
import muskel2.model.help.Team;
import muskel2.util.LeaguePlanner;
import muskel2.util.PlaceholderFactory;

public class AllCombinationsGameSeries extends TeamBasedGameSeries
{
	private static final long	serialVersionUID	= 1L;

	public static int			MAX_TEAMS			= 8;
	public static int			MAX_ROUNDS			= 8;
	
	protected int numberOfTeamsPerMatch;

	public AllCombinationsGameSeries()
	{
		super("gameseries.allcombinations.titlepatterns");
	}

	public int getNumberOfTeamsPerMatch()
	{
		return numberOfTeamsPerMatch;
	}

	public void setNumberOfTeamsPerMatch(int numberOfTeamsPerMatch)
	{
		this.numberOfTeamsPerMatch = numberOfTeamsPerMatch;
	}

	@Override
	protected void initSubType()
	{
		if(karopapier.isUnlocked())
		{
			MAX_TEAMS = 100;
			MAX_ROUNDS = 100;
		}
	}

	@Override
	protected void planGames0()
	{
		this.shuffleTeams();
		List<Match> matches = LeaguePlanner.createMatchesSpecial(this.shuffledTeams, this.numberOfTeamsPerMatch);
		
		Game game;
		String name;
		Map map;
		List<Player> gamePlayers;
		int count = 0;
		Rules tmpRules;
		for(int round = 1; round <= this.numberOfGamesPerPair; round++)
		{
			for(Match match: matches)
			{		
				map = this.maps.get(random.nextInt(this.maps.size()));
				
				gamePlayers = new LinkedList<Player>();
				gamePlayers.add(this.creator);
				for(Team team: match.getTeams())
				{
					for(Player player: team.getPlayers())
					{
						if(!gamePlayers.contains(player))
							gamePlayers.add(player);
					}
				}

				increasePlannedGames(gamePlayers);

				tmpRules = rules.clone().createRandomValues();
				name = PlaceholderFactory.applyPlaceholders(this.karopapier, title, map, gamePlayers, tmpRules, count, 0, 0, match.getTeams(), -1, -1);
				
				game = new Game(name, map, gamePlayers, tmpRules);
				
				this.games.add(game);
				count++;
			}
		}
	}
}
