package muskel2.model.series;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import muskel2.gui.Screen;
import muskel2.gui.screens.GroupWinnersScreen;
import muskel2.gui.screens.HomeMapsScreen;
import muskel2.gui.screens.KOWinnersScreen;
import muskel2.gui.screens.PlayersScreen;
import muskel2.gui.screens.RulesScreen;
import muskel2.gui.screens.SettingsScreen;
import muskel2.gui.screens.SummaryScreen;
import muskel2.model.Game;
import muskel2.model.GameSeries;
import muskel2.model.Karopapier;
import muskel2.model.Map;
import muskel2.model.Player;
import muskel2.model.Rules;
import muskel2.model.help.Match;
import muskel2.model.help.Team;
import muskel2.util.LeaguePlanner;
import muskel2.util.PlaceholderFactory;

public class KLCGameSeries extends GameSeries
{
	private static final long			serialVersionUID	= 1L;

	public static int					GROUPS				= 8;
	public static int					LEAGUES				= 4;
	public static int					PLAYERS				= GROUPS * LEAGUES;
	public static int					FIRST_KO_ROUND		= 16;
	public static int					WINNERS_PER_GROUP	= FIRST_KO_ROUND / GROUPS;

	private List<Player>				allPlayers			= new ArrayList<Player>(PLAYERS);
	private List<Player>				playersLeague1		= new ArrayList<Player>(8);
	private List<Player>				playersLeague2		= new ArrayList<Player>(8);
	private List<Player>				playersLeague3		= new ArrayList<Player>(8);
	private List<Player>				playersLeague4		= new ArrayList<Player>(8);
	private List<Player>				playersGroup1		= new ArrayList<Player>(4);
	private List<Player>				playersGroup2		= new ArrayList<Player>(4);
	private List<Player>				playersGroup3		= new ArrayList<Player>(4);
	private List<Player>				playersGroup4		= new ArrayList<Player>(4);
	private List<Player>				playersGroup5		= new ArrayList<Player>(4);
	private List<Player>				playersGroup6		= new ArrayList<Player>(4);
	private List<Player>				playersGroup7		= new ArrayList<Player>(4);
	private List<Player>				playersGroup8		= new ArrayList<Player>(4);
	private List<Player>				playersRoundOf16	= new ArrayList<Player>(16);
	private List<Player>				playersRoundOf8		= new ArrayList<Player>(8);
	private List<Player>				playersRoundOf4		= new ArrayList<Player>(4);
	private List<Player>				playersRoundOf2		= new ArrayList<Player>(2);
	private int							round				= PLAYERS;

	private HashMap<Integer, Integer>	homeMaps			= new HashMap<Integer, Integer>();

	public KLCGameSeries()
	{
		super("gameseries.klc.defaultTitle");
	}

	@Override
	protected void initSubType()
	{
	}

	@SuppressWarnings("unused")
	@Override
	protected Screen createScreens()
	{
		Screen s01 = new SettingsScreen(startScreen, karopapier, previousButton, nextButton);
		Screen s02 = new RulesScreen(s01, karopapier, previousButton, nextButton);
		Screen s03 = new PlayersScreen(s02, karopapier, previousButton, nextButton);
		Screen s04 = new HomeMapsScreen(s03, karopapier, previousButton, nextButton);
		s04.setNextKey("screen.homemaps.nextskip");
		Screen s05 = new SummaryScreen(s04, karopapier, previousButton, nextButton);
		return s01;
	}

	@Override
	public int getMinSupportedPlayersPerMap()
	{
		// 2 players + creator
		return 3;
	}

	@SuppressWarnings("unused")
	@Override
	protected Screen createScreensOnLoad()
	{
		Screen s01 = super.createScreensOnLoad();
		s01.setNextKey("screen.summary.nextko");
		if(round == PLAYERS)
		{
			Screen s02 = new GroupWinnersScreen(s01, karopapier, previousButton, nextButton);
			Screen s03 = new SummaryScreen(s02, karopapier, previousButton, nextButton);
		}
		else if(round > 2)
		{
			Screen s02 = new KOWinnersScreen(s01, karopapier, previousButton, nextButton);
			Screen s03 = new SummaryScreen(s02, karopapier, previousButton, nextButton);
		}
		round = round / 2;
		
		// Restore HomeMaps
		System.out.println("restoring homemaps");
		for(Entry<Integer, Integer> e: this.homeMaps.entrySet())
		{
			for(Player p: this.getKaropapier().getPlayers().values())
			{
				if(p.getId() == e.getKey())
					p.setHomeMap(this.getKaropapier().getMaps().get(e.getValue()));
			}
		}
		
		return s01;
	}

	@Override
	protected void planGames0()
	{
		// HomeMaps für Serialization sichern
		this.homeMaps.clear();
		for(Player p: this.allPlayers)
			this.homeMaps.put(p.getId(), p.getHomeMap().getId());
		
		if(this.round == PLAYERS)
		{
			// Liegen durchmischen
			for(int l = 1; l <= LEAGUES; l++)
				Collections.shuffle(this.getPlayersLeagueX(l));

			// Gruppenphase
			for(int g = 1; g <= GROUPS; g++)
			{
				// Bilde Gruppe aus je 1 Spieler pro Liga
				// die Ligen sind 1mal initial durchgemischt, deshalb können wir einfach für Gruppe
				// X jeweils den X-ten Spieler pro Gruppe nehmen
				this.getPlayersGroupX(g).clear();
				this.getPlayersGroupX(g).add(this.getPlayersLeagueX(1).get(g - 1));
				this.getPlayersGroupX(g).add(this.getPlayersLeagueX(2).get(g - 1));
				this.getPlayersGroupX(g).add(this.getPlayersLeagueX(3).get(g - 1));
				this.getPlayersGroupX(g).add(this.getPlayersLeagueX(4).get(g - 1));
				Collections.shuffle(this.getPlayersGroupX(g), random);

				// create a temporarily list of teams to be able to use the LeaguePlanner
				List<Team> teamsTmp = new ArrayList<Team>(this.getPlayersGroupX(g).size());
				for(Player p : this.getPlayersGroupX(g))
				{
					teamsTmp.add(new Team(p.getName(), Arrays.asList(p)));
				}
				List<List<Match>> matches = LeaguePlanner.createMatches(teamsTmp);

				int day = 0;
				Game game;
				String name;
				Map map;
				Player home, guest;
				List<Player> gamePlayers;
				int count = 0;
				int dayCount;
				Rules tmpRules;
				for(List<Match> matchesForDay : matches)
				{
					dayCount = 0;
					for(Match match : matchesForDay)
					{
						if(match.getTeam(0).getPlayers().get(0).getLeague() > match.getTeam(1).getPlayers().get(0).getLeague())
						{
							// Spieler 0 ist in der niedrigeren Liga (= höhere Liga Nummer)
							home = match.getTeam(0).getPlayers().get(0);
							guest = match.getTeam(1).getPlayers().get(0);
						}
						else if(match.getTeam(0).getPlayers().get(0).getLeague() < match.getTeam(1).getPlayers().get(0).getLeague())
						{
							// Spieler 1 ist in der niedrigeren Liga (= höhere Liga Nummer)
							guest = match.getTeam(0).getPlayers().get(0);
							home = match.getTeam(1).getPlayers().get(0);
						}
						else
						{
							// Spieler 0 und 1 sind in der gleichen Liga
							if(random.nextBoolean())
							{
								home = match.getTeam(0).getPlayers().get(0);
								guest = match.getTeam(1).getPlayers().get(0);
							}
							else
							{
								guest = match.getTeam(0).getPlayers().get(0);
								home = match.getTeam(1).getPlayers().get(0);
							}
						}

						map = home.getHomeMap();

						gamePlayers = new LinkedList<Player>();
						gamePlayers.add(this.creator);
						gamePlayers.add(home);
						gamePlayers.add(guest);

						increasePlannedGames(gamePlayers);

						tmpRules = rules.clone().createRandomValues();
						name = PlaceholderFactory.applyPlaceholders(this.karopapier, title, map, gamePlayers, tmpRules, count, day, dayCount, match.getTeams(), this.round, g);

						game = new Game(name, map, gamePlayers, tmpRules);

						this.games.add(game);
						count++;
						dayCount++;
					}
					day++;
				}
			}
		}
		else
		{
			// KO-Phase
			Collections.shuffle(this.getPlayersRoundOfX(this.round), random);

			int count = 1;
			Game game;
			String name;
			Map map;
			Player home, guest;
			List<Player> gamePlayers;
			Rules tmpRules;
			Team tmpTeamHome, tmpTeamGuest;
			for(int i = 0; i < this.round; i = i + 2)
			{
				if(this.getPlayersRoundOfX(this.round).get(i).getLeague() > this.getPlayersRoundOfX(this.round).get(i + 1).getLeague())
				{
					// Spieler i ist in der niedrigeren Liga (= höhere Liga Nummer)
					home = this.getPlayersRoundOfX(this.round).get(i);
					guest = this.getPlayersRoundOfX(this.round).get(i + 1);
				}
				else if(this.getPlayersRoundOfX(this.round).get(i).getLeague() < this.getPlayersRoundOfX(this.round).get(i + 1).getLeague())
				{
					// Spieler i+1 ist in der niedrigeren Liga (= höhere Liga Nummer)
					guest = this.getPlayersRoundOfX(this.round).get(i);
					home = this.getPlayersRoundOfX(this.round).get(i + 1);
				}
				else
				{
					// Spieler i und i+1 sind in der gleichen Liga
					if(random.nextBoolean())
					{
						home = this.getPlayersRoundOfX(this.round).get(i);
						guest = this.getPlayersRoundOfX(this.round).get(i + 1);
					}
					else
					{
						guest = this.getPlayersRoundOfX(this.round).get(i);
						home = this.getPlayersRoundOfX(this.round).get(i + 1);
					}
				}

				map = home.getHomeMap();

				gamePlayers = new LinkedList<Player>();
				gamePlayers.add(this.creator);
				gamePlayers.add(home);
				gamePlayers.add(guest);

				increasePlannedGames(gamePlayers);

				tmpRules = rules.clone().createRandomValues();
				tmpTeamHome = new Team(home.getName(), Arrays.asList(home));
				tmpTeamGuest = new Team(guest.getName(), Arrays.asList(guest));
				name = PlaceholderFactory.applyPlaceholders(this.karopapier, title, map, gamePlayers, tmpRules, count, -1, -1, new Team[] { tmpTeamHome, tmpTeamGuest }, this.round, -1);

				game = new Game(name, map, gamePlayers, tmpRules);

				this.games.add(game);
				count++;
			}
		}
	}

	public List<Player> getPlayersLeagueX(int league)
	{
		switch(league)
		{
			case 1:
				return playersLeague1;
			case 2:
				return playersLeague2;
			case 3:
				return playersLeague3;
			case 4:
				return playersLeague4;
		}
		return null;
	}

	public List<Player> getPlayersGroupX(int group)
	{
		switch(group)
		{
			case 1:
				return playersGroup1;
			case 2:
				return playersGroup2;
			case 3:
				return playersGroup3;
			case 4:
				return playersGroup4;
			case 5:
				return playersGroup5;
			case 6:
				return playersGroup6;
			case 7:
				return playersGroup7;
			case 8:
				return playersGroup8;
		}
		return null;
	}

	public List<Player> getPlayersRoundOfX(int round)
	{
		switch(round)
		{
			case 16:
				return playersRoundOf16;
			case 8:
				return playersRoundOf8;
			case 4:
				return playersRoundOf4;
			case 2:
				return playersRoundOf2;
			case 32:
				return allPlayers;
		}
		return null;
	}

	public List<Player> getAllPlayers()
	{
		return allPlayers;
	}

	public int getRound()
	{
		return round;
	}
}
