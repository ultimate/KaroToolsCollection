package ultimate.karomuskel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import muskel2.model.Direction;
import muskel2.model.series.AllCombinationsGameSeries;
import muskel2.model.series.BalancedGameSeries;
import muskel2.model.series.KLCGameSeries;
import muskel2.model.series.KOGameSeries;
import muskel2.model.series.LeagueGameSeries;
import muskel2.model.series.SimpleGameSeries;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.base.Identifiable;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karoapi4j.model.official.User;
import ultimate.karomuskel.test.KaroMUSKELTestcase;

@SuppressWarnings("deprecation")
public class GameSeriesManagerTest extends KaroMUSKELTestcase
{
	@Test
	public void test_getConfig()
	{
		assertEquals("de", GameSeriesManager.getStringConfig("language"));
		assertEquals(10, GameSeriesManager.getIntConfig("karoAPI.maxThreads"));

		assertEquals(16, GameSeriesManager.getIntConfig(new GameSeries(EnumGameSeriesType.KO), GameSeries.CONF_MAX_TEAMS));
		assertEquals(5, GameSeriesManager.getIntConfig(new GameSeries(EnumGameSeriesType.KO), GameSeries.CONF_MAX_ROUNDS));

		assertEquals(8, GameSeriesManager.getIntConfig(new GameSeries(EnumGameSeriesType.League), GameSeries.CONF_MAX_TEAMS));
		assertEquals(8, GameSeriesManager.getIntConfig(new GameSeries(EnumGameSeriesType.League), GameSeries.CONF_MAX_ROUNDS));
	}

	@Test
	public void test_storeAndLoad() throws IOException
	{
		User creator = dummyCache.getCurrentUser();

		int uid1 = 11;
		int uid2 = 13;
		int mid0 = 8;
		int mid1 = 12;

		int minP = 6;
		int maxP = 8;
		int num = 10;
		int minZzz = 2;
		int maxZzz = 2;

		GameSeries gs = new GameSeries(EnumGameSeriesType.Simple);
		gs.setTitle("test series {i}");
		gs.setCreator(creator);
		gs.set(GameSeries.MIN_PLAYERS_PER_GAME, minP);
		gs.set(GameSeries.MAX_PLAYERS_PER_GAME, maxP);
		gs.set(GameSeries.NUMBER_OF_GAMES, num);
		gs.setRules(new Rules(minZzz, maxZzz, EnumGameTC.allowed, true, EnumGameDirection.formula1));
		gs.setPlayers(Arrays.asList(creator, dummyCache.getUser(uid1), dummyCache.getUser(uid2)));
		gs.setMaps(Arrays.asList(dummyCache.getMap(mid0), dummyCache.getMap(mid1)));

		File file = new File("target/test-classes/test" + System.currentTimeMillis() + ".json");
		assertFalse(file.exists());

		GameSeriesManager.store(gs, file);

		assertTrue(file.exists());

		GameSeries loaded = GameSeriesManager.load(file, dummyCache);

		assertNotNull(loaded);

		assertTrue(loaded.isLoaded());
		assertEquals(EnumGameSeriesType.Simple, loaded.getType());
		assertEquals(gs.getTitle(), loaded.getTitle());
		assertEquals(creator, loaded.getCreator());
		assertEquals(minP, gs.get(GameSeries.MIN_PLAYERS_PER_GAME));
		assertEquals(maxP, gs.get(GameSeries.MAX_PLAYERS_PER_GAME));
		assertEquals(num, gs.get(GameSeries.NUMBER_OF_GAMES));
		assertNotNull(loaded.getRules());
		assertEquals(minZzz, loaded.getRules().getMinZzz());
		assertEquals(maxZzz, loaded.getRules().getMaxZzz());
		assertEquals(EnumGameTC.allowed, loaded.getRules().getCrashallowed());
		assertEquals(true, loaded.getRules().getCps());
		assertEquals(EnumGameDirection.formula1, loaded.getRules().getStartdirection());
		assertNotNull(loaded.getPlayers());
		assertEquals(3, loaded.getPlayers().size());
		assertEquals(creator, loaded.getPlayers().get(0));
		assertEquals(dummyCache.getUser(uid1), loaded.getPlayers().get(1));
		assertEquals(dummyCache.getUser(uid2), loaded.getPlayers().get(2));
		assertNotNull(loaded.getMaps());
		assertEquals(2, loaded.getMaps().size());
		assertEquals(dummyCache.getMap(mid0), loaded.getMaps().get(0));
		assertEquals(dummyCache.getMap(mid1), loaded.getMaps().get(1));
	}

	@ParameterizedTest
	@ValueSource(strings = { "target/test-classes/simple.muskel2" })
	public void test_loadV2andConvert_simple(String filename) throws ClassNotFoundException, ClassCastException, IOException
	{
		SimpleGameSeries gs2 = (SimpleGameSeries) GameSeriesManager.loadV2(new File(filename));
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
		assertEquals(EnumGameSeriesType.Simple, gs.getType());
		assertFalse(GameSeriesManager.isTeamBased(gs));
		assertEquals(gs2.title, gs.getTitle());
		assertEquals(gs2.creator.id, gs.getCreator().getId());
		assertEquals(gs2.minPlayersPerGame, gs.get(GameSeries.MIN_PLAYERS_PER_GAME));
		assertEquals(gs2.maxPlayersPerGame, gs.get(GameSeries.MAX_PLAYERS_PER_GAME));
		assertEquals(gs2.rules.minZzz, gs.getRules().getMinZzz());
		assertEquals(gs2.rules.maxZzz, gs.getRules().getMaxZzz());
		if(gs2.rules.crashingAllowed == true)
			assertEquals(EnumGameTC.allowed, gs.getRules().getCrashallowed());
		else if(gs2.rules.crashingAllowed == false)
			assertEquals(EnumGameTC.forbidden, gs.getRules().getCrashallowed());
		else
			assertNull(gs.getRules().getCrashallowed());
		if(gs2.rules.direction == Direction.klassisch)
			assertEquals(EnumGameDirection.classic, gs.getRules().getStartdirection());
		else if(gs2.rules.direction == Direction.Formula_1)
			assertEquals(EnumGameDirection.formula1, gs.getRules().getStartdirection());
		else
			assertNull(gs.getRules().getStartdirection());
		assertEquals(gs2.rules.gamesPerPlayer, gs.getRules().getGamesPerPlayer());
		assertEquals(gs2.players.size(), gs.getPlayers().size());
		assertEquals(gs2.maps.size(), gs.getMaps().size());
		assertEquals(gs2.numberOfGames, gs.get(GameSeries.NUMBER_OF_GAMES));
		assertTrue(gs.getGames().containsKey(gs.getType().toString()));
		assertEquals(gs2.numberOfGames, gs.getGames().get(gs.getType().toString()).size());
	}

	@ParameterizedTest
	@ValueSource(strings = { "target/test-classes/balanced.muskel2" })
	public void test_loadV2andConvert_balanced(String filename) throws ClassNotFoundException, ClassCastException, IOException
	{
		BalancedGameSeries gs2 = (BalancedGameSeries) GameSeriesManager.loadV2(new File(filename));
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
			expectedGames += Planner.calculateNumberOfGames(gs2.players.size(), gs2.rulesList.get(i).gamesPerPlayer, gs2.rulesList.get(i).numberOfPlayers);
		}
		assertEquals(expectedGames, gs2.games.size());

		GameSeries gs = GameSeriesManager.convert(gs2, dummyCache);
		assertEquals(EnumGameSeriesType.Balanced, gs.getType());
		assertFalse(GameSeriesManager.isTeamBased(gs));
		assertEquals(gs2.title, gs.getTitle());
		assertEquals(gs2.creator.id, gs.getCreator().getId());
		assertEquals(gs2.numberOfMaps, gs.get(GameSeries.NUMBER_OF_MAPS));
		assertEquals(gs2.numberOfMaps, gs.getMapsByKey().size());
		compareMapWithEntities(gs.getMapsByKey(), gs2.mapList);
		assertEquals(gs2.numberOfMaps, gs.getRulesByKey().size());
		assertEquals(gs2.rules.minZzz, gs.getRules().getMinZzz());
		assertEquals(gs2.rules.maxZzz, gs.getRules().getMaxZzz());
		if(gs2.rules.crashingAllowed == true)
			assertEquals(EnumGameTC.allowed, gs.getRules().getCrashallowed());
		else if(gs2.rules.crashingAllowed == false)
			assertEquals(EnumGameTC.forbidden, gs.getRules().getCrashallowed());
		else
			assertNull(gs.getRules().getCrashallowed());
		if(gs2.rules.direction == Direction.klassisch)
			assertEquals(EnumGameDirection.classic, gs.getRules().getStartdirection());
		else if(gs2.rules.direction == Direction.Formula_1)
			assertEquals(EnumGameDirection.formula1, gs.getRules().getStartdirection());
		else
			assertNull(gs.getRules().getStartdirection());
		assertEquals(gs2.rules.gamesPerPlayer, gs.getRules().getGamesPerPlayer());
		assertEquals(gs2.players.size(), gs.getPlayers().size());
		assertEquals(gs2.maps.size(), gs.getMaps().size());
		assertTrue(gs.getGames().containsKey(gs.getType().toString()));
		assertEquals(expectedGames, gs.getGames().get(gs.getType().toString()).size());
	}

	@ParameterizedTest
	@ValueSource(strings = { "target/test-classes/league.muskel2" })
	public void test_loadV2andConvert_league(String filename) throws ClassNotFoundException, ClassCastException, IOException
	{
		LeagueGameSeries gs2 = (LeagueGameSeries) GameSeriesManager.loadV2(new File(filename));
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

		GameSeries gs = GameSeriesManager.convert(gs2, dummyCache);
		assertEquals(EnumGameSeriesType.League, gs.getType());
		assertTrue(GameSeriesManager.isTeamBased(gs));
		assertEquals(gs2.title, gs.getTitle());
		assertEquals(gs2.creator.id, gs.getCreator().getId());
		assertEquals(gs2.autoNameTeams, gs.get(GameSeries.AUTO_NAME_TEAMS));
		assertEquals(gs2.creatorTeam, gs.get(GameSeries.USE_CREATOR_TEAM));
		assertEquals(gs2.maxPlayersPerTeam, gs.get(GameSeries.MAX_PLAYERS_PER_TEAM));
		assertEquals(gs2.minPlayersPerTeam, gs.get(GameSeries.MIN_PLAYERS_PER_TEAM));
		assertEquals(gs2.multipleTeams, gs.get(GameSeries.ALLOW_MULTIPLE_TEAMS));
		assertEquals(gs2.numberOfGamesPerPair, gs.get(GameSeries.NUMBER_OF_GAMES_PER_PAIR));
		assertEquals(gs2.shuffleTeams, gs.get(GameSeries.SHUFFLE_TEAMS));
		assertEquals(gs2.useHomeMaps, gs.get(GameSeries.USE_HOME_MAPS));
		assertEquals(gs2.numberOfTeams, gs.get(GameSeries.NUMBER_OF_TEAMS));
		assertEquals(gs2.numberOfTeams, gs.getTeams().size());
		assertEquals(gs2.rules.minZzz, gs.getRules().getMinZzz());
		assertEquals(gs2.rules.maxZzz, gs.getRules().getMaxZzz());
		if(gs2.rules.crashingAllowed == true)
			assertEquals(EnumGameTC.allowed, gs.getRules().getCrashallowed());
		else if(gs2.rules.crashingAllowed == false)
			assertEquals(EnumGameTC.forbidden, gs.getRules().getCrashallowed());
		else
			assertNull(gs.getRules().getCrashallowed());
		if(gs2.rules.direction == Direction.klassisch)
			assertEquals(EnumGameDirection.classic, gs.getRules().getStartdirection());
		else if(gs2.rules.direction == Direction.Formula_1)
			assertEquals(EnumGameDirection.formula1, gs.getRules().getStartdirection());
		else
			assertNull(gs.getRules().getStartdirection());
		assertEquals(0, gs.getRules().getGamesPerPlayer());
		assertEquals(gs2.players.size(), gs.getPlayers().size());
		assertEquals(gs2.maps.size(), gs.getMaps().size());
		assertEquals(gs2.numberOfTeams, gs.getTeamsByKey().get("shuffled").size());
		assertTrue(gs.getGames().containsKey(gs.getType().toString()));
		assertEquals(expectedGames, gs.getGames().get(gs.getType().toString()).size());
	}

	@ParameterizedTest
	@ValueSource(strings = { "target/test-classes/allcombinations.muskel2" })
	public void test_loadV2andConvert_allcombinations(String filename) throws ClassNotFoundException, ClassCastException, IOException
	{
		AllCombinationsGameSeries gs2 = (AllCombinationsGameSeries) GameSeriesManager.loadV2(new File(filename));
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
		assertEquals(3, gs2.numberOfTeamsPerMatch);
		assertEquals(8, gs2.numberOfTeams);
		assertEquals(gs2.numberOfTeams, gs2.teams.size());
		int expectedGames = gs2.numberOfTeams * (gs2.numberOfTeams - 1);
		assertEquals(expectedGames, gs2.games.size());

		GameSeries gs = GameSeriesManager.convert(gs2, dummyCache);
		assertEquals(EnumGameSeriesType.AllCombinations, gs.getType());
		assertTrue(GameSeriesManager.isTeamBased(gs));
		assertEquals(gs2.title, gs.getTitle());
		assertEquals(gs2.creator.id, gs.getCreator().getId());
		assertEquals(gs2.autoNameTeams, gs.get(GameSeries.AUTO_NAME_TEAMS));
		assertEquals(gs2.creatorTeam, gs.get(GameSeries.USE_CREATOR_TEAM));
		assertEquals(gs2.maxPlayersPerTeam, gs.get(GameSeries.MAX_PLAYERS_PER_TEAM));
		assertEquals(gs2.minPlayersPerTeam, gs.get(GameSeries.MIN_PLAYERS_PER_TEAM));
		assertEquals(gs2.multipleTeams, gs.get(GameSeries.ALLOW_MULTIPLE_TEAMS));
		assertEquals(gs2.numberOfGamesPerPair, gs.get(GameSeries.NUMBER_OF_GAMES_PER_PAIR));
		assertEquals(gs2.shuffleTeams, gs.get(GameSeries.SHUFFLE_TEAMS));
		assertEquals(gs2.useHomeMaps, gs.get(GameSeries.USE_HOME_MAPS));
		assertEquals(gs2.numberOfTeamsPerMatch, gs.get(GameSeries.NUMBER_OF_TEAMS_PER_MATCH));
		assertEquals(gs2.numberOfTeams, gs.get(GameSeries.NUMBER_OF_TEAMS));
		assertEquals(gs2.numberOfTeams, gs.getTeams().size());
		assertEquals(gs2.rules.minZzz, gs.getRules().getMinZzz());
		assertEquals(gs2.rules.maxZzz, gs.getRules().getMaxZzz());
		if(gs2.rules.crashingAllowed == true)
			assertEquals(EnumGameTC.allowed, gs.getRules().getCrashallowed());
		else if(gs2.rules.crashingAllowed == false)
			assertEquals(EnumGameTC.forbidden, gs.getRules().getCrashallowed());
		else
			assertNull(gs.getRules().getCrashallowed());
		if(gs2.rules.direction == Direction.klassisch)
			assertEquals(EnumGameDirection.classic, gs.getRules().getStartdirection());
		else if(gs2.rules.direction == Direction.Formula_1)
			assertEquals(EnumGameDirection.formula1, gs.getRules().getStartdirection());
		else
			assertNull(gs.getRules().getStartdirection());
		assertEquals(0, gs.getRules().getGamesPerPlayer());
		assertEquals(gs2.players.size(), gs.getPlayers().size());
		assertEquals(gs2.maps.size(), gs.getMaps().size());
		assertEquals(gs2.numberOfTeams, gs.getTeamsByKey().get("shuffled").size());
		assertTrue(gs.getGames().containsKey(gs.getType().toString()));
		assertEquals(expectedGames, gs.getGames().get(gs.getType().toString()).size());
	}

	@ParameterizedTest
	@ValueSource(strings = { "target/test-classes/ko.muskel2" })
	public void test_loadV2andConvert_ko(String filename) throws ClassNotFoundException, ClassCastException, IOException
	{
		KOGameSeries gs2 = (KOGameSeries) GameSeriesManager.loadV2(new File(filename));
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

		GameSeries gs = GameSeriesManager.convert(gs2, dummyCache);
		assertEquals(EnumGameSeriesType.KO, gs.getType());
		assertTrue(GameSeriesManager.isTeamBased(gs));
		assertEquals(gs2.title, gs.getTitle());
		assertEquals(gs2.creator.id, gs.getCreator().getId());
		assertEquals(gs2.autoNameTeams, gs.get(GameSeries.AUTO_NAME_TEAMS));
		assertEquals(gs2.creatorTeam, gs.get(GameSeries.USE_CREATOR_TEAM));
		assertEquals(gs2.maxPlayersPerTeam, gs.get(GameSeries.MAX_PLAYERS_PER_TEAM));
		assertEquals(gs2.minPlayersPerTeam, gs.get(GameSeries.MIN_PLAYERS_PER_TEAM));
		assertEquals(gs2.multipleTeams, gs.get(GameSeries.ALLOW_MULTIPLE_TEAMS));
		assertEquals(gs2.numberOfGamesPerPair, gs.get(GameSeries.NUMBER_OF_GAMES_PER_PAIR));
		assertEquals(gs2.shuffleTeams, gs.get(GameSeries.SHUFFLE_TEAMS));
		assertEquals(gs2.useHomeMaps, gs.get(GameSeries.USE_HOME_MAPS));
		assertEquals(gs2.numberOfTeams, gs.get(GameSeries.NUMBER_OF_TEAMS));
		assertEquals(gs2.numberOfTeams, gs.get(GameSeries.CURRENT_ROUND));
		assertEquals(gs2.numberOfTeams, gs.getTeams().size());
		assertEquals(gs2.rules.minZzz, gs.getRules().getMinZzz());
		assertEquals(gs2.rules.maxZzz, gs.getRules().getMaxZzz());
		if(gs2.rules.crashingAllowed == true)
			assertEquals(EnumGameTC.allowed, gs.getRules().getCrashallowed());
		else if(gs2.rules.crashingAllowed == false)
			assertEquals(EnumGameTC.forbidden, gs.getRules().getCrashallowed());
		else
			assertNull(gs.getRules().getCrashallowed());
		if(gs2.rules.direction == Direction.klassisch)
			assertEquals(EnumGameDirection.classic, gs.getRules().getStartdirection());
		else if(gs2.rules.direction == Direction.Formula_1)
			assertEquals(EnumGameDirection.formula1, gs.getRules().getStartdirection());
		else
			assertNull(gs.getRules().getStartdirection());
		assertEquals(0, gs.getRules().getGamesPerPlayer());
		assertEquals(gs2.players.size(), gs.getPlayers().size());
		assertEquals(gs2.maps.size(), gs.getMaps().size());
		assertEquals(gs2.numberOfTeams, gs.getTeamsByKey().get("shuffled").size());
		String gamesKey = gs.getType().toString() + "." + GameSeries.KEY_ROUND + gs2.numberOfTeams;
		assertTrue(gs.getGames().containsKey(gamesKey));
		assertEquals(expectedGames, gs.getGames().get(gamesKey).size());
	}

	public static Stream<Arguments> provideKLCRounds()
	{
		//@formatter:off
	    return Stream.of(
	        arguments("target/test-classes/klc-32.muskel2", 32),
	        arguments("target/test-classes/klc-16.muskel2", 16),
	        arguments("target/test-classes/klc-8.muskel2", 	8),
	        arguments("target/test-classes/klc-4.muskel2", 	4),
	        arguments("target/test-classes/klc-2.muskel2", 	2)
	    );
	    //@formatter:on
	}

	@ParameterizedTest
	@MethodSource("provideKLCRounds")
	public void test_loadV2andConvert_klc(String filename, int round) throws ClassNotFoundException, ClassCastException, IOException
	{
		KLCGameSeries gs2 = (KLCGameSeries) GameSeriesManager.loadV2(new File(filename));
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
		assertEquals(playersPerLeague * KLCGameSeries.LEAGUES, gs2.homeMaps.size());
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
		assertEquals(round, gs2.round);
		int gamesPerGroup = playersPerGroup * (playersPerGroup - 1) / 2;
		int expectedGames;
		String gamesKey;
		if(round == 32)
		{
			gamesKey = EnumGameSeriesType.KLC.toString() + "." + GameSeries.KEY_GROUP + "phase";
			expectedGames = gamesPerGroup * 8;
		}
		else
		{
			gamesKey = EnumGameSeriesType.KLC.toString() + "." + GameSeries.KEY_ROUND + round;
			expectedGames = round / 2;
		}
		assertEquals(expectedGames, gs2.games.size());
		assertEquals(round <= 16 ? 16 : 0, gs2.playersRoundOf16.size());
		assertEquals(round <= 8 ? 8 : 0, gs2.playersRoundOf8.size());
		assertEquals(round <= 4 ? 4 : 0, gs2.playersRoundOf4.size());
		assertEquals(round <= 2 ? 2 : 0, gs2.playersRoundOf2.size());

		GameSeries gs = GameSeriesManager.convert(gs2, dummyCache);
		assertEquals(EnumGameSeriesType.KLC, gs.getType());
		assertFalse(GameSeriesManager.isTeamBased(gs));
		assertEquals(gs2.title, gs.getTitle());
		assertEquals(gs2.creator.id, gs.getCreator().getId());
		assertEquals(gs2.round, gs.get(GameSeries.CURRENT_ROUND));
		assertEquals(KLCGameSeries.LEAGUES, GameSeriesManager.getIntConfig(gs, GameSeries.CONF_KLC_LEAGUES));
		assertEquals(KLCGameSeries.GROUPS, GameSeriesManager.getIntConfig(gs, GameSeries.CONF_KLC_GROUPS));
		assertEquals(gs2.homeMaps.size(), gs.getMapsByKey().size());
		compareMapWithIDs(gs.getMapsByKey(), gs2.homeMaps);
		assertEquals(gs2.rules.minZzz, gs.getRules().getMinZzz());
		assertEquals(gs2.rules.maxZzz, gs.getRules().getMaxZzz());
		if(gs2.rules.crashingAllowed == true)
			assertEquals(EnumGameTC.allowed, gs.getRules().getCrashallowed());
		else if(gs2.rules.crashingAllowed == false)
			assertEquals(EnumGameTC.forbidden, gs.getRules().getCrashallowed());
		else
			assertNull(gs.getRules().getCrashallowed());
		if(gs2.rules.direction == Direction.klassisch)
			assertEquals(EnumGameDirection.classic, gs.getRules().getStartdirection());
		else if(gs2.rules.direction == Direction.Formula_1)
			assertEquals(EnumGameDirection.formula1, gs.getRules().getStartdirection());
		else
			assertNull(gs.getRules().getStartdirection());
		assertEquals(gs2.rules.gamesPerPlayer, gs.getRules().getGamesPerPlayer());
		assertEquals(gs2.allPlayers.size(), gs.getPlayers().size());
		assertEquals(gs2.maps.size(), gs.getMaps().size());
		compareLists(gs.getPlayersByKey().get("league1"), gs2.playersLeague1);
		compareLists(gs.getPlayersByKey().get("league2"), gs2.playersLeague2);
		compareLists(gs.getPlayersByKey().get("league3"), gs2.playersLeague3);
		compareLists(gs.getPlayersByKey().get("league4"), gs2.playersLeague4);
		compareLists(gs.getPlayersByKey().get("group1"), gs2.playersGroup1);
		compareLists(gs.getPlayersByKey().get("group2"), gs2.playersGroup2);
		compareLists(gs.getPlayersByKey().get("group3"), gs2.playersGroup3);
		compareLists(gs.getPlayersByKey().get("group4"), gs2.playersGroup4);
		compareLists(gs.getPlayersByKey().get("group5"), gs2.playersGroup5);
		compareLists(gs.getPlayersByKey().get("group6"), gs2.playersGroup6);
		compareLists(gs.getPlayersByKey().get("group7"), gs2.playersGroup7);
		compareLists(gs.getPlayersByKey().get("group8"), gs2.playersGroup8);
		compareLists(gs.getPlayersByKey().get("roundOf16"), gs2.playersRoundOf16);
		compareLists(gs.getPlayersByKey().get("roundOf8"), gs2.playersRoundOf8);
		compareLists(gs.getPlayersByKey().get("roundOf4"), gs2.playersRoundOf4);
		compareLists(gs.getPlayersByKey().get("roundOf2"), gs2.playersRoundOf2);
		assertTrue(gs.getGames().containsKey(gamesKey));
		assertEquals(expectedGames, gs.getGames().get(gamesKey).size());
	}

	@ParameterizedTest
	@ValueSource(strings = { "../CraZZZy Crash Challenge/CCC3/czzzcc3.muskel" })
	public void test_loadV2_withwrongSerialVersionUID(String filename) throws ClassNotFoundException, ClassCastException, IOException, ExecutionException, InterruptedException, URISyntaxException
	{
		BalancedGameSeries gs2 = (BalancedGameSeries) GameSeriesManager.loadV2(new File(filename));
		assertNotNull(gs2);
		assertEquals("CraZZZy Crash Challenge 3 - Challenge ${spieltag}.${spieltag.i} - Karte ${karte.id} | ${spieler.anzahl.x}er Challenge | ${regeln.zzz}", gs2.title);
		assertEquals(2241, gs2.creator.id); // CraZZZy
		assertEquals(2, gs2.rules.minZzz); // not used, since rules are defined by game day
		assertEquals(2, gs2.rules.maxZzz); // not used, since rules are defined by game day
		assertEquals(false, gs2.rules.crashingAllowed); // not used, since rules are defined by game day
		assertEquals(Direction.klassisch, gs2.rules.direction); // not used, since rules are defined by game day
		assertEquals(0, gs2.rules.gamesPerPlayer); // not used, since rules are defined by game day
		assertEquals(28, gs2.players.size());
		assertEquals(0, gs2.maps.size());
		assertEquals(20, gs2.numberOfMaps);
		assertEquals(gs2.numberOfMaps, gs2.mapList.size());
		assertEquals(gs2.numberOfMaps, gs2.rulesList.size());
		int expectedGames = 0;
		expectedGames += checkCCC3(gs2, 1, 197, 6, 6, 1, true);
		expectedGames += checkCCC3(gs2, 2, 228, 6, 6, 6, true);
		expectedGames += checkCCC3(gs2, 3, 221, 6, 6, 8, true);
		expectedGames += checkCCC3(gs2, 4, 223, 6, 6, 7, true);
		expectedGames += checkCCC3(gs2, 5, 199, 6, 3, 1, false);
		expectedGames += checkCCC3(gs2, 6, 205, 6, 3, 6, true);
		expectedGames += checkCCC3(gs2, 7, 207, 6, 7, 3, true);
		expectedGames += checkCCC3(gs2, 8, 206, 6, 6, 4, true);
		expectedGames += checkCCC3(gs2, 9, 227, 6, 7, 7, true);
		expectedGames += checkCCC3(gs2, 10, 208, 6, 7, 9, true);
		expectedGames += checkCCC3(gs2, 11, 225, 6, 7, 7, true);
		expectedGames += checkCCC3(gs2, 12, 148, 6, 4, 12, true);
		expectedGames += checkCCC3(gs2, 13, 204, 6, 4, 4, true);
		expectedGames += checkCCC3(gs2, 14, 209, 6, 7, 8, true);
		expectedGames += checkCCC3(gs2, 15, 198, 6, 4, 6, true);
		expectedGames += checkCCC3(gs2, 16, 215, 6, 3, 4, true);
		expectedGames += checkCCC3(gs2, 17, 203, 6, 3, 5, true);
		expectedGames += checkCCC3(gs2, 18, 195, 6, 4, 3, true);
		expectedGames += checkCCC3(gs2, 19, 193, 6, 4, 5, true);
		expectedGames += checkCCC3(gs2, 20, 217, 6, 3, 6, true);

		assertEquals(expectedGames, gs2.games.size());
	}

	private int checkCCC3(BalancedGameSeries gs2, int i, int mapId, int gamesPerPlayer, int numberOfPlayers, int zzz, boolean cps)
	{
		assertEquals(mapId, gs2.mapList.get(i - 1).id);
		assertEquals(gamesPerPlayer, gs2.rulesList.get(i - 1).gamesPerPlayer);
		assertEquals(numberOfPlayers, gs2.rulesList.get(i - 1).numberOfPlayers);
		assertEquals(zzz, gs2.rulesList.get(i - 1).minZzz);
		assertEquals(zzz, gs2.rulesList.get(i - 1).maxZzz);
		assertEquals(cps, gs2.rulesList.get(i - 1).checkpointsActivated);
		assertEquals(Direction.egal, gs2.rulesList.get(i - 1).direction);
		return Planner.calculateNumberOfGames(gs2.players.size(), gamesPerPlayer, numberOfPlayers);
	}

	private <T extends Identifiable, T2 extends muskel2.model.help.Identifiable> void compareLists(List<T> converted, List<T2> original)
	{
		assertEquals(original.size(), converted.size());
		for(int i = 0; i < original.size(); i++)
		{
			assertEquals(original.get(i).getId(), converted.get(i).getId());
		}
	}

	private <T extends Identifiable, T2 extends muskel2.model.help.Identifiable> void compareMapWithEntities(java.util.Map<String, List<T>> byKey, java.util.Map<Integer, T2> original)
	{
		assertEquals(original.size(), byKey.size());
		String key;
		for(Entry<Integer, T2> t2 : original.entrySet())
		{
			key = "" + t2.getKey();
			assertEquals(1, byKey.get(key).size());
			assertEquals(t2.getValue().getId(), byKey.get(key).get(0).getId());
		}
	}

	private <T extends Identifiable> void compareMapWithIDs(java.util.Map<String, List<T>> byKey, java.util.Map<Integer, Integer> original)
	{
		assertEquals(original.size(), byKey.size());
		String key;
		for(Entry<Integer, Integer> t2 : original.entrySet())
		{
			key = "" + t2.getKey();
			assertEquals(1, byKey.get(key).size());
			assertEquals(t2.getValue(), byKey.get(key).get(0).getId());
		}
	}
}
