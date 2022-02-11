package ultimate.karomuskel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karomuskel.test.KaroMUSKELTestcase;

@SuppressWarnings("deprecation")
public class GameSeriesManagerTest extends KaroMUSKELTestcase
{
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Test
	public void test_loadV2andConvert_simple() throws ClassNotFoundException, ClassCastException, IOException
	{
		SimpleGameSeries gs2 = (SimpleGameSeries) GameSeriesManager.loadV2(new File("target/test-classes/simple.muskel2"));
		assertNotNull(gs2);
		assertEquals("${spieler.ersteller}s Spieleserie - Spiel ${i} auf Karte ${karte.id}, ${regeln.x}", gs2.title);
		assertEquals(0, gs2.creator.id);
		assertEquals(6, gs2.minPlayersPerGame);
		assertEquals(8, gs2.maxPlayersPerGame);
		assertEquals(2, gs2.rules.minZzz);
		assertEquals(2, gs2.rules.maxZzz);
		assertEquals(false, gs2.rules.crashingAllowed);
		assertEquals(Direction.klassisch, gs2.rules.direction);
		assertEquals(0, gs2.rules.gamesPerPlayer);
		assertEquals(10, gs2.players.size());
		assertEquals(10, gs2.maps.size());
		assertEquals(10, gs2.numberOfGames);
		assertEquals(gs2.numberOfGames, gs2.games.size());
		
		GameSeries gs = GameSeriesManager.convert(gs2, dummyCache);
		assertEquals(gs2.creator.id, gs.getCreator().getId());
		assertEquals(gs2.minPlayersPerGame, gs.get(GameSeries.MIN_PLAYERS_PER_GAME));
		assertEquals(gs2.maxPlayersPerGame, gs.get(GameSeries.MAX_PLAYERS_PER_GAME));
		assertEquals(gs2.rules.minZzz, gs.getRules().getMinZzz());
		assertEquals(gs2.rules.maxZzz, gs.getRules().getMaxZzz());
		if(gs2.rules.crashingAllowed == true)
			assertEquals(EnumGameTC.allowed, gs.getRules().getTC());
		else if(gs2.rules.crashingAllowed == false)
			assertEquals(EnumGameTC.forbidden, gs.getRules().getTC());
		else
			assertNull(gs.getRules().getTC());
		assertEquals(gs2.rules.gamesPerPlayer, gs.getRules().getGamesPerPlayer());
		assertEquals(gs2.players.size(), gs.getPlayers().size());
		assertEquals(gs2.maps.size(), gs.getMaps().size());
		assertEquals(gs2.numberOfGames, gs.get(GameSeries.NUMBER_OF_GAMES));
		assertEquals(gs2.numberOfGames, gs.getGames().size());
	}

	@Test
	public void test_loadV2andConvert_balanced() throws ClassNotFoundException, ClassCastException, IOException
	{
		BalancedGameSeries gs2 = (BalancedGameSeries) GameSeriesManager.loadV2(new File("target/test-classes/balanced.muskel2"));
		assertNotNull(gs2);
		assertEquals("${spieler.ersteller}s Ausgewogene Spieleserie - Spiel ${i} - ${spieler.namen.x} auf Karte ${karte.id}, ${regeln.x}", gs2.title);
		assertEquals(0, gs2.creator.id);
		assertEquals(2, gs2.rules.minZzz);
		assertEquals(2, gs2.rules.maxZzz);
		assertEquals(false, gs2.rules.crashingAllowed);
		assertEquals(Direction.klassisch, gs2.rules.direction);
		assertEquals(0, gs2.rules.gamesPerPlayer);
		assertEquals(10, gs2.players.size());
		assertEquals(0, gs2.maps.size());
		assertEquals(5, gs2.numberOfMaps);
		assertEquals(gs2.numberOfMaps, gs2.mapList.size());
		assertEquals(gs2.numberOfMaps, gs2.rulesList.size());
		int expectedGames = 0;
		for(int i = 0; i < gs2.numberOfMaps; i++)
		{
			assertEquals(i + 1, gs2.mapList.get(i).id);
			assertEquals(i + 1, gs2.rulesList.get(i).gamesPerPlayer);
			assertEquals((i == 0 ? 2 : i + 1), gs2.rulesList.get(i).numberOfPlayers);
			expectedGames += Math.ceil(gs2.rulesList.get(i).gamesPerPlayer * gs2.players.size() / (double) gs2.rulesList.get(i).numberOfPlayers);
		}
		assertEquals(expectedGames, gs2.games.size());
	}

	@Test
	public void test_loadV2andConvert_league() throws ClassNotFoundException, ClassCastException, IOException
	{
		LeagueGameSeries gs2 = (LeagueGameSeries) GameSeriesManager.loadV2(new File("target/test-classes/league.muskel2"));
		assertNotNull(gs2);
		assertEquals("KaroLiga Saison xx - Spieltag ${spieltag} - ${teams} auf Karte ${karte.id}", gs2.title);
		assertEquals(0, gs2.creator.id);
		assertEquals(2, gs2.rules.minZzz);
		assertEquals(2, gs2.rules.maxZzz);
		assertEquals(false, gs2.rules.crashingAllowed);
		assertEquals(Direction.klassisch, gs2.rules.direction);
		assertEquals(0, gs2.rules.gamesPerPlayer);
		assertEquals(0, gs2.players.size());
		assertEquals(0, gs2.maps.size());
		assertEquals(2, gs2.numberOfGamesPerPair);
		assertEquals(8, gs2.numberOfTeams);
		assertEquals(gs2.numberOfTeams, gs2.teams.size());
		int gameDays = (gs2.teams.size() - 1) * gs2.numberOfGamesPerPair;
		int gamesPerDay = gs2.teams.size() / 2;
		int expectedGames = gameDays * gamesPerDay;
		assertEquals(expectedGames, gs2.games.size());
	}

	@Test
	public void test_loadV2andConvert_allcombinations() throws ClassNotFoundException, ClassCastException, IOException
	{
		AllCombinationsGameSeries gs2 = (AllCombinationsGameSeries) GameSeriesManager.loadV2(new File("target/test-classes/allcombinations.muskel2"));
		assertNotNull(gs2);
		assertEquals("${spieler.ersteller}s Jeder-gegen-Jeden-Spieleserie - Spiel ${i} - ${spieler.namen.x} auf Karte ${karte.id}, ${regeln.x}", gs2.title);
		assertEquals(0, gs2.creator.id);
		assertEquals(2, gs2.rules.minZzz);
		assertEquals(2, gs2.rules.maxZzz);
		assertEquals(false, gs2.rules.crashingAllowed);
		assertEquals(Direction.klassisch, gs2.rules.direction);
		assertEquals(0, gs2.rules.gamesPerPlayer);
		assertEquals(0, gs2.players.size());
		assertEquals(9, gs2.maps.size());
		assertEquals(1, gs2.numberOfGamesPerPair);
		assertEquals(8, gs2.numberOfTeams);
		assertEquals(gs2.numberOfTeams, gs2.teams.size());
		int expectedGames = gs2.numberOfTeams * (gs2.numberOfTeams - 1);
		assertEquals(expectedGames, gs2.games.size());
	}

	@Test
	public void test_loadV2andConvert_ko() throws ClassNotFoundException, ClassCastException, IOException
	{
		KOGameSeries gs2 = (KOGameSeries) GameSeriesManager.loadV2(new File("target/test-classes/ko.muskel2"));
		assertNotNull(gs2);
		assertEquals("Karopapier Weltmeisterschaft 20xx - ${runde} - ${teams} auf Karte ${karte.id}", gs2.title);
		assertEquals(0, gs2.creator.id);
		assertEquals(2, gs2.rules.minZzz);
		assertEquals(2, gs2.rules.maxZzz);
		assertEquals(false, gs2.rules.crashingAllowed);
		assertEquals(Direction.klassisch, gs2.rules.direction);
		assertEquals(0, gs2.rules.gamesPerPlayer);
		assertEquals(0, gs2.players.size());
		assertEquals(10, gs2.maps.size());
		assertEquals(1, gs2.numberOfGamesPerPair);
		assertEquals(8, gs2.numberOfTeams);
		assertEquals(gs2.numberOfTeams, gs2.teams.size());
		int expectedGames = gs2.numberOfTeams * gs2.numberOfGamesPerPair / 2;
		assertEquals(expectedGames, gs2.games.size());
	}

	@Test
	public void test_loadV2andConvert_klc() throws ClassNotFoundException, ClassCastException, IOException
	{
		KLCGameSeries gs2 = (KLCGameSeries) GameSeriesManager.loadV2(new File("target/test-classes/klc.muskel2"));
		assertNotNull(gs2);
		assertEquals("KLC Saison xx - ${runde.x} - ${teams} auf Karte ${karte.id}", gs2.title);
		assertEquals(0, gs2.creator.id);
		assertEquals(2, gs2.rules.minZzz);
		assertEquals(2, gs2.rules.maxZzz);
		assertEquals(false, gs2.rules.crashingAllowed);
		assertEquals(Direction.klassisch, gs2.rules.direction);
		assertEquals(0, gs2.rules.gamesPerPlayer);
		assertEquals(0, gs2.players.size());
		assertEquals(0, gs2.maps.size());
		int playersPerLeague = 8;
		assertEquals(playersPerLeague, gs2.playersLeague1.size());
		assertEquals(playersPerLeague, gs2.playersLeague2.size());
		assertEquals(playersPerLeague, gs2.playersLeague3.size());
		assertEquals(playersPerLeague, gs2.playersLeague4.size());
		int playersPerGroup = 4;
		assertEquals(playersPerGroup, gs2.playersGroup1.size());
		assertEquals(playersPerGroup, gs2.playersGroup2.size());
		assertEquals(playersPerGroup, gs2.playersGroup3.size());
		assertEquals(playersPerGroup, gs2.playersGroup4.size());
		assertEquals(playersPerGroup, gs2.playersGroup5.size());
		assertEquals(playersPerGroup, gs2.playersGroup6.size());
		assertEquals(playersPerGroup, gs2.playersGroup7.size());
		assertEquals(playersPerLeague * playersPerGroup, gs2.round);
		int gamesPerGroup = playersPerGroup * (playersPerGroup - 1) / 2;
		int expectedGames = gamesPerGroup * 8;
		assertEquals(expectedGames, gs2.games.size());
	}
}
