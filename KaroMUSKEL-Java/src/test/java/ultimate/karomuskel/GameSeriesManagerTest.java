package ultimate.karomuskel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import muskel2.model.Direction;
import muskel2.model.series.AllCombinationsGameSeries;
import muskel2.model.series.BalancedGameSeries;
import muskel2.model.series.KLCGameSeries;
import muskel2.model.series.KOGameSeries;
import muskel2.model.series.LeagueGameSeries;
import muskel2.model.series.SimpleGameSeries;

@SuppressWarnings("deprecation")
public class GameSeriesManagerTest
{
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Test
	public void test_loadV2andConvert_simple() throws ClassNotFoundException, ClassCastException, IOException
	{
		SimpleGameSeries gs = (SimpleGameSeries) GameSeriesManager.loadV2(new File("target/test-classes/simple.muskel2"));
		assertNotNull(gs);
		assertEquals("${spieler.ersteller}s Spieleserie - Spiel ${i} auf Karte ${karte.id}, ${regeln.x}", gs.title);
		assertEquals(0, gs.creator.id);
		assertEquals(6, gs.minPlayersPerGame);
		assertEquals(8, gs.maxPlayersPerGame);
		assertEquals(2, gs.rules.minZzz);
		assertEquals(2, gs.rules.maxZzz);
		assertEquals(false, gs.rules.crashingAllowed);
		assertEquals(Direction.klassisch, gs.rules.direction);
		assertEquals(0, gs.rules.gamesPerPlayer);
		assertEquals(10, gs.players.size());
		assertEquals(10, gs.maps.size());
		assertEquals(10, gs.numberOfGames);
		assertEquals(gs.numberOfGames, gs.games.size());
	}

	@Test
	public void test_loadV2andConvert_balanced() throws ClassNotFoundException, ClassCastException, IOException
	{
		BalancedGameSeries gs = (BalancedGameSeries) GameSeriesManager.loadV2(new File("target/test-classes/balanced.muskel2"));
		assertNotNull(gs);
		assertEquals("${spieler.ersteller}s Ausgewogene Spieleserie - Spiel ${i} - ${spieler.namen.x} auf Karte ${karte.id}, ${regeln.x}", gs.title);
		assertEquals(0, gs.creator.id);
		assertEquals(2, gs.rules.minZzz);
		assertEquals(2, gs.rules.maxZzz);
		assertEquals(false, gs.rules.crashingAllowed);
		assertEquals(Direction.klassisch, gs.rules.direction);
		assertEquals(0, gs.rules.gamesPerPlayer);
		assertEquals(10, gs.players.size());
		assertEquals(0, gs.maps.size());
		assertEquals(5, gs.numberOfMaps);
		assertEquals(gs.numberOfMaps, gs.mapList.size());
		assertEquals(gs.numberOfMaps, gs.rulesList.size());
		int expectedGames = 0;
		for(int i = 0; i < gs.numberOfMaps; i++)
		{
			assertEquals(i + 1, gs.mapList.get(i).id);
			assertEquals(i + 1, gs.rulesList.get(i).gamesPerPlayer);
			assertEquals((i == 0 ? 2 : i + 1), gs.rulesList.get(i).numberOfPlayers);
			expectedGames += Math.ceil(gs.rulesList.get(i).gamesPerPlayer * gs.players.size() / (double) gs.rulesList.get(i).numberOfPlayers);
		}
		assertEquals(expectedGames, gs.games.size());
	}

	@Test
	public void test_loadV2andConvert_league() throws ClassNotFoundException, ClassCastException, IOException
	{
		LeagueGameSeries gs = (LeagueGameSeries) GameSeriesManager.loadV2(new File("target/test-classes/league.muskel2"));
		assertNotNull(gs);
		assertEquals("KaroLiga Saison xx - Spieltag ${spieltag} - ${teams} auf Karte ${karte.id}", gs.title);
		assertEquals(0, gs.creator.id);
		assertEquals(2, gs.rules.minZzz);
		assertEquals(2, gs.rules.maxZzz);
		assertEquals(false, gs.rules.crashingAllowed);
		assertEquals(Direction.klassisch, gs.rules.direction);
		assertEquals(0, gs.rules.gamesPerPlayer);
		assertEquals(0, gs.players.size());
		assertEquals(0, gs.maps.size());
		assertEquals(2, gs.numberOfGamesPerPair);
		assertEquals(8, gs.numberOfTeams);
		assertEquals(gs.numberOfTeams, gs.teams.size());
		int gameDays = (gs.teams.size() - 1) * gs.numberOfGamesPerPair;
		int gamesPerDay = gs.teams.size() / 2;
		int expectedGames = gameDays * gamesPerDay;
		assertEquals(expectedGames, gs.games.size());
	}

	@Test
	public void test_loadV2andConvert_allcombinations() throws ClassNotFoundException, ClassCastException, IOException
	{
		AllCombinationsGameSeries gs = (AllCombinationsGameSeries) GameSeriesManager.loadV2(new File("target/test-classes/allcombinations.muskel2"));
		assertNotNull(gs);
		assertEquals("${spieler.ersteller}s Jeder-gegen-Jeden-Spieleserie - Spiel ${i} - ${spieler.namen.x} auf Karte ${karte.id}, ${regeln.x}", gs.title);
		assertEquals(0, gs.creator.id);
		assertEquals(2, gs.rules.minZzz);
		assertEquals(2, gs.rules.maxZzz);
		assertEquals(false, gs.rules.crashingAllowed);
		assertEquals(Direction.klassisch, gs.rules.direction);
		assertEquals(0, gs.rules.gamesPerPlayer);
		assertEquals(0, gs.players.size());
		assertEquals(9, gs.maps.size());
		assertEquals(1, gs.numberOfGamesPerPair);
		assertEquals(8, gs.numberOfTeams);
		assertEquals(gs.numberOfTeams, gs.teams.size());
		int expectedGames = gs.numberOfTeams * (gs.numberOfTeams - 1);
		assertEquals(expectedGames, gs.games.size());
	}

	@Test
	public void test_loadV2andConvert_ko() throws ClassNotFoundException, ClassCastException, IOException
	{
		KOGameSeries gs = (KOGameSeries) GameSeriesManager.loadV2(new File("target/test-classes/ko.muskel2"));
		assertNotNull(gs);
		assertEquals("Karopapier Weltmeisterschaft 20xx - ${runde} - ${teams} auf Karte ${karte.id}", gs.title);
		assertEquals(0, gs.creator.id);
		assertEquals(2, gs.rules.minZzz);
		assertEquals(2, gs.rules.maxZzz);
		assertEquals(false, gs.rules.crashingAllowed);
		assertEquals(Direction.klassisch, gs.rules.direction);
		assertEquals(0, gs.rules.gamesPerPlayer);
		assertEquals(0, gs.players.size());
		assertEquals(10, gs.maps.size());
		assertEquals(1, gs.numberOfGamesPerPair);
		assertEquals(8, gs.numberOfTeams);
		assertEquals(gs.numberOfTeams, gs.teams.size());
		int expectedGames = gs.numberOfTeams * gs.numberOfGamesPerPair / 2;
		assertEquals(expectedGames, gs.games.size());
	}

	@Test
	public void test_loadV2andConvert_klc() throws ClassNotFoundException, ClassCastException, IOException
	{
		KLCGameSeries gs = (KLCGameSeries) GameSeriesManager.loadV2(new File("target/test-classes/klc.muskel2"));
		assertNotNull(gs);
		assertEquals("KLC Saison xx - ${runde.x} - ${teams} auf Karte ${karte.id}", gs.title);
		assertEquals(0, gs.creator.id);
		assertEquals(2, gs.rules.minZzz);
		assertEquals(2, gs.rules.maxZzz);
		assertEquals(false, gs.rules.crashingAllowed);
		assertEquals(Direction.klassisch, gs.rules.direction);
		assertEquals(0, gs.rules.gamesPerPlayer);
		assertEquals(0, gs.players.size());
		assertEquals(0, gs.maps.size());
		int playersPerLeague = 8;
		assertEquals(playersPerLeague, gs.playersLeague1.size());
		assertEquals(playersPerLeague, gs.playersLeague2.size());
		assertEquals(playersPerLeague, gs.playersLeague3.size());
		assertEquals(playersPerLeague, gs.playersLeague4.size());
		int playersPerGroup = 4;
		assertEquals(playersPerGroup, gs.playersGroup1.size());
		assertEquals(playersPerGroup, gs.playersGroup2.size());
		assertEquals(playersPerGroup, gs.playersGroup3.size());
		assertEquals(playersPerGroup, gs.playersGroup4.size());
		assertEquals(playersPerGroup, gs.playersGroup5.size());
		assertEquals(playersPerGroup, gs.playersGroup6.size());
		assertEquals(playersPerGroup, gs.playersGroup7.size());
		assertEquals(playersPerLeague * playersPerGroup, gs.round);
		int gamesPerGroup = playersPerGroup * (playersPerGroup - 1) / 2;
		int expectedGames = gamesPerGroup * 8;
		assertEquals(expectedGames, gs.games.size());
	}
}
