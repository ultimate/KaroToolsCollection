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

public class LeagueGameSeries extends TeamBasedGameSeries
{
	private static final long	serialVersionUID	= 1L;

	public static int			MAX_TEAMS			= 8;
	public static int			MAX_ROUNDS			= 8;

	public LeagueGameSeries()
	{
		super("gameseries.league.titlepatterns");
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
		List<List<Match>> matches = LeaguePlanner.createMatches(this.shuffledTeams);
		
		int day = 0;
		Game game;
		String name;
		Map map;
		Team home, guest;
		List<Player> gamePlayers;
		int count = 0;
		int dayCount;
		Rules tmpRules;
		for(int round = 1; round <= this.numberOfGamesPerPair; round++)
		{
			for(List<Match> matchesForDay: matches)
			{
				dayCount = 0;
				for(Match match: matchesForDay)
				{					
					if(round % 2 == 0)
					{
						home = match.getTeam1();
						guest = match.getTeam2();
					}
					else
					{
						home = match.getTeam2();
						guest = match.getTeam1();
					}
					
					if(this.useHomeMaps && !((round % 2 == 1) && (round == this.numberOfGamesPerPair)))
					{
						map = home.getHomeMap();
					}
					else
					{
						map = this.maps.get(random.nextInt(this.maps.size()));						
					}
					
					gamePlayers = new LinkedList<Player>();
					gamePlayers.add(this.creator);
					for(Player player: home.getPlayers())
					{
						if(!gamePlayers.contains(player))
							gamePlayers.add(player);
					}
					for(Player player: guest.getPlayers())
					{
						if(!gamePlayers.contains(player))
							gamePlayers.add(player);
					}

					increasePlannedGames(gamePlayers);

					tmpRules = rules.clone().createRandomValues();
					name = PlaceholderFactory.applyPlaceholders(this.karopapier, title, map, gamePlayers, tmpRules, count, day, dayCount, home, guest, -1);
					
					game = new Game(name, map, gamePlayers, tmpRules);
					
					this.games.add(game);
					count++;
					dayCount++;
				}
				day++;
			}
		}
	}
}
