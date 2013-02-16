package muskel2.model.series;

import java.util.LinkedList;
import java.util.List;

import muskel2.gui.Screen;
import muskel2.gui.screens.KOWinnersScreen;
import muskel2.gui.screens.SummaryScreen;
import muskel2.model.Game;
import muskel2.model.Map;
import muskel2.model.Player;
import muskel2.model.Rules;
import muskel2.model.help.Team;
import muskel2.util.PlaceholderFactory;

public class KOGameSeries extends TeamBasedGameSeries
{
	private static final long	serialVersionUID	= 1L;

	public static int			MAX_TEAMS			= 16;
	public static int			MAX_ROUNDS			= 3;

	public KOGameSeries()
	{
		super("gameseries.ko.titlepatterns");
	}

	@Override
	protected void initSubType()
	{
		if(karopapier.isUnlocked())
		{
			MAX_TEAMS = 256;
			MAX_ROUNDS = 100;
		}
	}

	@SuppressWarnings("unused")
	@Override
	protected Screen createScreensOnLoad()
	{
		Screen s01 = super.createScreensOnLoad();
		s01.setNextKey("screen.summary.nextko");
		if(this.teams.size() > 2)
		{
			Screen s02 = new KOWinnersScreen(s01, karopapier, previousButton, nextButton);
			Screen s03 = new SummaryScreen(s02, karopapier, previousButton, nextButton);
		}
		return s01;
	}

	@Override
	protected void planGames0()
	{
		this.shuffleTeams();

		int count = 1;
		Game game;
		String name;
		Map map;
		Team home, guest;
		List<Player> gamePlayers;
		Rules tmpRules;
		for(int i = 0; i < this.numberOfTeams; i = i + 2)
		{
			for(int round = 1; round <= this.numberOfGamesPerPair; round++)
			{
				if(round % 2 == 1)
				{
					home = this.shuffledTeams.get(i);
					guest = this.shuffledTeams.get(i + 1);
				}
				else
				{
					guest = this.shuffledTeams.get(i);
					home = this.shuffledTeams.get(i + 1);
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
				name = PlaceholderFactory.applyPlaceholders(this.karopapier, title, map, gamePlayers, tmpRules, count, -1, -1, home, guest, shuffledTeams.size());
				
				game = new Game(name, map, gamePlayers, tmpRules);

				this.games.add(game);
				count++;
			}
		}
	}
}
