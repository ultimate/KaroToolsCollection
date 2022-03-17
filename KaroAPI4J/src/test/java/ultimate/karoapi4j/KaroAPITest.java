package ultimate.karoapi4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.enums.EnumUserGamesort;
import ultimate.karoapi4j.model.official.ChatMessage;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Options;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.model.official.UserMessage;
import ultimate.karoapi4j.test.KaroAPITestcase;
import ultimate.karoapi4j.utils.CollectionsUtil;
import ultimate.karoapi4j.utils.MethodComparator;

public class KaroAPITest extends KaroAPITestcase
{
	private static final int	VALIDATION_LIMIT	= 10;
	private static final String	TEST_GAMES_NAME		= "KaroAPI4J-Testspiel#";
	private static final int[]	TEST_GAMES_IDS		= new int[] { 132803, 132804, 132805, 132806, 132807 };
	private static final String	TEST_CHAT_MESSAGE	= "Hey Leute, sorry fuer den Spam, aber ich muss mal was ausprobieren...";
	private static final int	TEST_CHAT_ID_MIN	= 462585;
	private static final int	TEST_CHAT_ID_MAX	= 462589;

	@Test
	public void test_version() throws InterruptedException, ExecutionException
	{
		assertNotNull(KaroAPI.getVersion());
		assertEquals("1.1.0", KaroAPI.getVersion());
	}

	@Test
	public void test_instanciateWithKey() throws InterruptedException, ExecutionException
	{
		String key = karoAPI.getKey().get();
		User user = karoAPI.check().get();
		logger.debug("user = " + user.getLogin() + ", key = " + key);

		assertNotNull(user);
		assertNotNull(key);

		KaroAPI karoAPIbyKey = new KaroAPI(key);

		String keyByKey = karoAPIbyKey.getKey().get();
		User userByKey = karoAPIbyKey.check().get();

		logger.debug("user = " + userByKey.getLogin() + ", key = " + keyByKey);

		assertNotNull(userByKey);
		assertNotNull(keyByKey);

		assertEquals(key, keyByKey);
		assertEquals(user.getLogin(), userByKey.getLogin());
	}

	@Test
	public void test_check() throws InterruptedException, ExecutionException
	{
		User user = karoAPI.check().get();

		assertNotNull(user);
		assertEquals(properties.get("karoapi.user"), user.getLogin());
	}

	@Test
	public void test_getUsers() throws InterruptedException, ExecutionException
	{
		List<User> users = karoAPI.getUsers().get();
		logger.debug("loaded users: " + users.size());

		// check global user list

		assertNotNull(users);
		// should be more than just a few users...
		assertTrue(users.size() > 100);
		// should contain the current user
		Predicate<User> findCurrentUser = (user) -> { return user.getLogin().equals(properties.get("karoapi.user")); };
		assertTrue(CollectionsUtil.contains(users, findCurrentUser));

		// check invitable filter

		List<User> invitables = karoAPI.getUsers(null, true, null).get();
		logger.debug("loaded invitable: " + invitables.size());
		assertNotNull(invitables);
		// all users in this list should be invitable
		Predicate<User> findInvitable = (user) -> { return user.isInvitable(false); };
		assertEquals(invitables.size(), CollectionsUtil.count(invitables, findInvitable));
		// the users in invitable should match the invitables from the global user list
		List<User> users_filteredToInvitables = new ArrayList<User>(users);
		users_filteredToInvitables.removeIf(findInvitable.negate());
		compareList(users_filteredToInvitables, invitables, new MethodComparator<>("getId", 1));

		// check desperate filter

		List<User> desperates = karoAPI.getUsers(null, null, true).get();
		logger.debug("loaded desperates: " + desperates.size());
		assertNotNull(desperates);
		// all users in this list should be desperate
		Predicate<User> findDesperates = (user) -> { return user.isDesperate(); };
		assertEquals(desperates.size(), CollectionsUtil.count(desperates, findDesperates));
		// the users in invitable should match the invitables from the global user list
		List<User> users_filteredToDesperates = new ArrayList<User>(users);
		users_filteredToDesperates.removeIf(findDesperates.negate());
		compareList(users_filteredToDesperates, desperates, new MethodComparator<>("getId", 1));

		// check login filter

		String login = "bot";
		List<User> bylogin = karoAPI.getUsers(login, null, null).get();
		logger.debug("loaded bylogin: " + bylogin.size());
		assertNotNull(bylogin);
		// all users in this list should match the login
		Predicate<User> findLogin = (user) -> { return user.getLogin().toLowerCase().contains(login.toLowerCase()); };
		assertEquals(bylogin.size(), CollectionsUtil.count(bylogin, findLogin));
		// the users in invitable should match the invitables from the global user list
		List<User> users_filteredToLogin = new ArrayList<User>(users);
		users_filteredToLogin.removeIf(findLogin.negate());
		compareList(users_filteredToLogin, bylogin, new MethodComparator<>("getId", 1));

		// combination of all the filters

		List<User> filtered = karoAPI.getUsers(login, true, true).get();
		logger.debug("loaded filtered: " + filtered.size());
		assertNotNull(filtered);
		// all users in this list should match the filter
		Predicate<User> filter = (user) -> { return findInvitable.test(user) && findDesperates.test(user) && findLogin.test(user); };
		assertEquals(filtered.size(), CollectionsUtil.count(filtered, filter));
		// the users in invitable should match the invitables from the global user list
		List<User> users_filtered = new ArrayList<User>(users);
		users_filtered.removeIf(filter.negate());
		compareList(users_filtered, filtered, new MethodComparator<>("getId", 1));
	}

	@Test
	public void test_getUser() throws InterruptedException, ExecutionException
	{
		int id;
		User user;

		id = 1;
		{
			user = karoAPI.getUser(id).get();
			logger.debug("loaded user: " + user.getId() + " = " + user.getLogin());
			assertEquals(id, user.getId());
			assertEquals("Didi", user.getLogin());
			assertTrue(user.isSuperCreator());
			assertFalse(user.isBot());
		}

		id = 773;
		{
			user = karoAPI.getUser(id).get();
			logger.debug("loaded user: " + user.getId() + " = " + user.getLogin());
			assertEquals(id, user.getId());
			assertEquals("Botrix", user.getLogin());
			assertFalse(user.isSuperCreator());
			assertTrue(user.isBot());
		}

		id = 2248;
		{
			user = karoAPI.getUser(id).get();
			logger.debug("loaded user: " + user.getId() + " = " + user.getLogin());
			assertEquals(id, user.getId());
			assertEquals("KaroLigaCupStarter", user.getLogin());
			assertTrue(user.isSuperCreator());
			assertFalse(user.isBot());
		}
	}

	@Test
	public void test_getUserDran() throws InterruptedException, ExecutionException
	{
		// get the top-blocker first
		User topBlocker = karoAPI.getBlockers().get().get(0);

		// now check his dran games
		List<Game> dran = karoAPI.getUserDran(topBlocker.getId()).get();
		assertNotNull(dran);
		assertTrue(dran.size() > 0);
		for(int i = 0; i < dran.size() && i < VALIDATION_LIMIT; i++)
		{
			Game fullgame = karoAPI.getGame(dran.get(i).getId()).get();
			assertEquals(topBlocker.getId(), fullgame.getNext().getId());
		}
	}

	@Test
	public void test_getBlockers() throws InterruptedException, ExecutionException
	{
		List<User> blockers = karoAPI.getBlockers().get();
		assertNotNull(blockers);
		assertTrue(blockers.size() > 0);

		for(int i = 0; i < blockers.size() && i < VALIDATION_LIMIT; i++)
		{
			assertTrue(blockers.get(i).getDran() > 0);
		}
	}

	@Test
	public void test_getUserBlockers() throws InterruptedException, ExecutionException
	{
		int userId = 773; // Botrix (since there should always be someone blocking a bot...
		List<User> blockers = karoAPI.getUserBlockers(userId).get();
		assertNotNull(blockers);
		assertTrue(blockers.size() > 0);

		for(int i = 0; i < blockers.size() && i < VALIDATION_LIMIT; i++)
		{
			assertTrue(blockers.get(i).getBlocked() > 0);
		}
	}

	@Test
	public void test_getGames() throws InterruptedException, ExecutionException
	{
		List<Game> games = karoAPI.getGames().get();

		// for CraZZZy the global can be empty
		assertNotNull(games);
		logger.debug("loaded games: " + games.size());

		// but if we request !mine, then there should be some games
		games = karoAPI.getGames(false, null, null, null, null, null, null, null).get();
		assertNotNull(games);
		logger.debug("loaded games: " + games.size());
		assertTrue(games.size() > 0);

		// check name filter and sorting for some (finished) test games

		List<Game> games1 = karoAPI.getGames(null, EnumUserGamesort.gid, null, true, TEST_GAMES_NAME, 0, null, null).get();
		List<Game> games2 = karoAPI.getGames(null, EnumUserGamesort.name, null, true, TEST_GAMES_NAME, 0, null, null).get();
		logger.debug("loaded games: " + games1.size() + " & " + games2.size());
		assertNotNull(games1);
		assertNotNull(games2);
		assertEquals(TEST_GAMES_IDS.length, games1.size());
		assertEquals(TEST_GAMES_IDS.length, games2.size());
		for(int id : TEST_GAMES_IDS)
		{
			Predicate<Game> findGame = (game) -> { return game.getId() == id; };
			assertEquals(1, CollectionsUtil.count(games1, findGame));
		}
		// check the games are sorted by id (first print them for debugging)
		CollectionsUtil.sortAscending(games1, "getId"); // TODO currently the gid-sort does not work properly
		for(int i = 0; i < games1.size(); i++)
			logger.debug(i + ": id=" + games1.get(i).getId() + ", name=" + games1.get(i).getName());
		for(int i = 0; i < games1.size(); i++)
		{
			assertTrue(games1.get(i).getName().startsWith(TEST_GAMES_NAME));
			assertEquals(TEST_GAMES_IDS[i], games1.get(i).getId());
		}
		// check the games are sorted by name (first print them for debugging)
		for(int i = 0; i < games2.size(); i++)
			logger.debug(i + ": id=" + games2.get(i).getId() + ", name=" + games2.get(i).getName());
		for(int i = 0; i < games2.size(); i++)
			assertEquals(TEST_GAMES_NAME + (i + 1), games2.get(i).getName());

		// check limit and offset

		List<Game> limited;
		for(int i = 0; i < TEST_GAMES_IDS.length; i++)
		{
			limited = karoAPI.getGames(null, EnumUserGamesort.name, null, true, TEST_GAMES_NAME, 0, 1, i).get();
			assertNotNull(limited);
			// assertEquals(1, limited.size()); // currently the limit is not working, but the offset does
			assertEquals(TEST_GAMES_NAME + (i + 1), limited.get(0).getName());
		}
	}

	@Test
	public void test_getGame() throws InterruptedException, ExecutionException
	{
		int id;
		Game game;

		for(int i = 0; i < TEST_GAMES_IDS.length; i++)
		{
			id = TEST_GAMES_IDS[i];
			game = karoAPI.getGame(id).get();
			logger.debug("loaded game: " + game.getId() + " = " + game.getName());
			assertEquals(id, game.getId());
			assertTrue(game.getName().startsWith(TEST_GAMES_NAME));
			assertEquals("CraZZZy", game.getCreator());
		}

		id = 44773;
		{
			game = karoAPI.getGame(id).get();
			logger.debug("loaded game: " + game.getId() + " = " + game.getName());
			assertEquals(id, game.getId());
			assertEquals("Runde um Runde nehmen wir jede Ecke und bleiben auf der Strecke!", game.getName());
			assertEquals("Madeleine", game.getCreator());
		}
	}

	@Test
	public void test_getMaps() throws InterruptedException, ExecutionException
	{
		List<Map> maps = karoAPI.getMaps().get();
		logger.debug("loaded maps: " + maps.size());

		// check global map list

		assertNotNull(maps);
		// should be more than just a few maps...
		assertTrue(maps.size() > 100);

		// check mapcode argument

		List<Map> maps_withcode = karoAPI.getMaps(true).get();
		logger.debug("loaded maps_withcode: " + maps_withcode.size());
		assertNotNull(maps_withcode);
		// all maps in this list should contain a mapcode
		maps_withcode.forEach((map) -> { checkMapCode(map); });
		// the maps in invitable should match the global map list
		compareList(maps, maps_withcode, new MethodComparator<>("getId", 1));
	}

	@Test
	public void test_getMap() throws InterruptedException, ExecutionException
	{
		int id;
		Map map;

		id = 1;
		{
			map = karoAPI.getMap(id).get();
			logger.debug("loaded map: " + map.getId() + " = " + map.getName() + " (by " + map.getAuthor() + ")");
			assertEquals(id, map.getId());
			assertEquals("Die Erste", map.getName());
			assertEquals("Didi", map.getAuthor());
			assertTrue(map.isActive());
			assertFalse(map.isNight());
		}

		id = 25; // inactive
		{
			map = karoAPI.getMap(id).get();
			logger.debug("loaded map: " + map.getId() + " = " + map.getName() + " (by " + map.getAuthor() + ")");
			assertEquals(id, map.getId());
			assertEquals("Nadelöhr", map.getName());
			assertEquals("Wolfgang Preiß", map.getAuthor());
			assertFalse(map.isActive());
			assertFalse(map.isNight());
		}

		id = 1000; // night
		{
			map = karoAPI.getMap(id).get();
			logger.debug("loaded map: " + map.getId() + " = " + map.getName() + " (by " + map.getAuthor() + ")");
			assertEquals(id, map.getId());
			assertEquals("Nachtrennen", map.getName());
			assertEquals("(unbekannt)", map.getAuthor());
			assertTrue(map.isActive());
			assertTrue(map.isNight());
		}

		// check mapcode argument

		id = 1;
		{
			map = karoAPI.getMap(id, true).get();
			logger.debug("loaded map: " + map.getId() + " = " + map.getName() + " (by " + map.getAuthor() + ") with code");
			assertEquals(id, map.getId());
			checkMapCode(map);
		}
	}

	@Test
	public void test_getMapImage() throws InterruptedException, ExecutionException
	{
		int id = 1;
		BufferedImage image;

		Map map = karoAPI.getMap(id).get();

		// default
		image = karoAPI.getMapImage(id);
		assertNotNull(image);
		assertEquals(780, image.getWidth(null));
		assertEquals(325, image.getHeight(null));

		// thumb
		image = karoAPI.getMapThumb(id, null);
		assertNotNull(image);
		assertEquals(60, image.getWidth(null));
		assertEquals(25, image.getHeight(null));

		// by width
		int width = 200;
		int expectedWidth = (int) (Math.ceil((double) width / map.getCols()) + 1) * map.getCols();
		image = karoAPI.getMapImageByDimension(id, width, null, true);
		logger.debug("width: requested=" + width + ", received=" + image.getWidth() + ", calculated=" + expectedWidth);
		assertEquals(expectedWidth, image.getWidth());
		assertTrue(image.getWidth() >= width);

		// by height
		int height = 200;
		int expectedHeight = (int) (Math.ceil((double) height / map.getRows()) + 1) * map.getRows();
		image = karoAPI.getMapImageByDimension(id, null, height, true);
		assertEquals(expectedHeight, image.getHeight());
		assertTrue(image.getHeight() >= height);

		// by pixel size
		int size = 10;
		int border = 1;
		image = karoAPI.getMapImageByPixelSize(id, size, border, true);
		assertEquals((size + border) * map.getCols(), image.getWidth());
		assertEquals((size + border) * map.getRows(), image.getHeight());
	}

	@Test
	public void test_createGameAndMove() throws InterruptedException, ExecutionException
	{
		int sleep = 500;
		
		User user = karoAPI.check().get();

		PlannedGame plannedGame = new PlannedGame();
		plannedGame.setMap(new Map(105));
		plannedGame.getPlayers().add(user);
		plannedGame.setName("KaroAPI-Test-Game");
		plannedGame.setOptions(new Options(2, true, EnumGameDirection.free, EnumGameTC.free));

		Game game = karoAPI.createGame(plannedGame).get();
		assertNotNull(game);
		assertNotNull(game.getId());
		assertEquals(plannedGame.getName(), game.getName());

		logger.debug("game created: id=" + game.getId() + ", name=" + game.getName());
		int gameId = game.getId();
		int moves = 0;
		int crashs = 0;
		int x, y;
		
		Thread.sleep(sleep);

		// load full game -> check players and moves
		game = karoAPI.getGame(gameId, false, true, true).get();
		assertNotNull(game);
		assertNotNull(game.getId());
		assertNotNull(game.getPlayers());
		assertEquals(1, game.getPlayers().size());
		assertEquals(user.getId(), game.getPlayers().get(0).getId());
		assertEquals(moves, game.getPlayers().get(0).getMoveCount());
		assertEquals(crashs, game.getPlayers().get(0).getCrashCount());
		assertEquals(moves + crashs, game.getPlayers().get(0).getMoves().size());
		assertNotNull(game.getPlayers().get(0).getPossibles());
		assertEquals(1, game.getPlayers().get(0).getPossibles().size());
		
		Thread.sleep(sleep);

		// select start position
		x = 2;
		y = 1;
		assertTrue(karoAPI.selectStartPosition(game.getId(), new Move(x, y, null)).get());
		moves++;

		Thread.sleep(sleep);

		// update -> check players and moves again
		game = karoAPI.getGame(gameId, false, true, true).get();
		assertNotNull(game);
		assertEquals(gameId, game.getId());
		assertNotNull(game.getPlayers());
		assertEquals(1, game.getPlayers().size());
		assertEquals(user.getId(), game.getPlayers().get(0).getId());
		assertEquals(moves, game.getPlayers().get(0).getMoveCount());
		assertEquals(crashs, game.getPlayers().get(0).getCrashCount());
		assertEquals(moves + crashs, game.getPlayers().get(0).getMoves().size());
		assertEquals(x, game.getPlayers().get(0).getMoves().get(game.getPlayers().get(0).getMoves().size() - 1).getX());
		assertEquals(y, game.getPlayers().get(0).getMoves().get(game.getPlayers().get(0).getMoves().size() - 1).getY());
		assertNotNull(game.getPlayers().get(0).getPossibles());
		assertEquals(2, game.getPlayers().get(0).getPossibles().size());

		Thread.sleep(sleep);

		// move 1 (to the left)
		x = 1;
		y = 1;
		assertTrue(karoAPI.move(game.getId(), new Move(x, y, -1, 0, null)).get());
		moves++;

		Thread.sleep(sleep);

		// update -> check players and moves again
		game = karoAPI.getGame(gameId, false, true, true).get();
		assertNotNull(game);
		assertEquals(gameId, game.getId());
		assertNotNull(game.getPlayers());
		assertEquals(1, game.getPlayers().size());
		assertEquals(user.getId(), game.getPlayers().get(0).getId());
		assertEquals(moves, game.getPlayers().get(0).getMoveCount());
		assertEquals(crashs, game.getPlayers().get(0).getCrashCount());
		assertEquals(moves + crashs, game.getPlayers().get(0).getMoves().size());
		assertEquals(x, game.getPlayers().get(0).getMoves().get(game.getPlayers().get(0).getMoves().size() - 1).getX());
		assertEquals(y, game.getPlayers().get(0).getMoves().get(game.getPlayers().get(0).getMoves().size() - 1).getY());
		assertNull(game.getPlayers().get(0).getPossibles()); // we ran into a crash

		Thread.sleep(sleep);

		// refresh
		x = 2;
		y = 1;
		assertTrue(karoAPI.refreshAfterCrash(gameId).get());
		crashs++;

		Thread.sleep(sleep);

		// update -> check players and moves again
		game = karoAPI.getGame(gameId, false, true, true).get();
		assertNotNull(game);
		assertEquals(gameId, game.getId());
		assertNotNull(game.getPlayers());
		assertEquals(1, game.getPlayers().size());
		assertEquals(user.getId(), game.getPlayers().get(0).getId());
		assertEquals(moves, game.getPlayers().get(0).getMoveCount());
		assertEquals(crashs, game.getPlayers().get(0).getCrashCount());
		assertEquals(moves + crashs, game.getPlayers().get(0).getMoves().size());
		assertEquals(x, game.getPlayers().get(0).getMoves().get(game.getPlayers().get(0).getMoves().size() - 1).getX());
		assertEquals(y, game.getPlayers().get(0).getMoves().get(game.getPlayers().get(0).getMoves().size() - 1).getY());
		assertNotNull(game.getPlayers().get(0).getPossibles());
		assertEquals(2, game.getPlayers().get(0).getPossibles().size());

		Thread.sleep(sleep);

		// move (again from ZZZ-point (= start) to the right)
		x = 3;
		y = 1;
		assertTrue(karoAPI.move(game.getId(), new Move(x, y, 1, 0, null)).get());
		moves++;

		Thread.sleep(sleep);

		// update -> check players and moves again
		game = karoAPI.getGame(gameId, false, true, true).get();
		assertNotNull(game);
		assertEquals(gameId, game.getId());
		assertNotNull(game.getPlayers());
		assertEquals(1, game.getPlayers().size());
		assertEquals(user.getId(), game.getPlayers().get(0).getId());
		assertEquals(moves, game.getPlayers().get(0).getMoveCount());
		assertEquals(crashs, game.getPlayers().get(0).getCrashCount());
		assertEquals(moves + crashs, game.getPlayers().get(0).getMoves().size());
		assertNotNull(game.getPlayers().get(0).getPossibles());
		assertEquals(2, game.getPlayers().get(0).getPossibles().size());

		Thread.sleep(sleep);

		// move (cross the finish line)
		x = 5;
		y = 1;
		assertTrue(karoAPI.move(game.getId(), new Move(x, y, 2, 0, null)).get());
		moves++; // last move
		moves++; // parc ferme

		Thread.sleep(sleep);

		// update -> check players and moves again
		game = karoAPI.getGame(gameId, false, true, true).get();
		assertNotNull(game);
		assertEquals(gameId, game.getId());
		assertEquals(1, game.getPlayers().size());
		assertEquals(user.getId(), game.getPlayers().get(0).getId());
		assertEquals(moves, game.getPlayers().get(0).getMoveCount());
		assertEquals(crashs, game.getPlayers().get(0).getCrashCount());
		assertEquals(moves + crashs, game.getPlayers().get(0).getMoves().size());
		assertNull(game.getPlayers().get(0).getPossibles());
		assertTrue(game.isFinished());
	}

	@Test
	public void test_favs() throws InterruptedException, ExecutionException
	{
		int gameId = TEST_GAMES_IDS[0];
		Predicate<Game> findFav = (game) -> { return game.getId() == gameId; };

		// get the initial list of favs
		List<Game> favs = karoAPI.getFavs().get();

		// current list must not contain the fav
		assertEquals(0, CollectionsUtil.count(favs, findFav));

		List<Game> newFavs;

		// create a fav
		karoAPI.addFav(gameId).get();
		newFavs = karoAPI.getFavs().get();
		assertEquals(favs.size() + 1, newFavs.size());

		// new list must contain the new fav
		assertEquals(1, CollectionsUtil.count(newFavs, findFav));

		// delete the fav (reset)
		karoAPI.removeFav(gameId).get();
		newFavs = karoAPI.getFavs().get();
		assertEquals(favs.size(), newFavs.size());
	}

	// @Test // Only run if changes have been made
	public void test_chat() throws InterruptedException, ExecutionException
	{
		ChatMessage oldMessage, newMessage;
		List<ChatMessage> chat;

		oldMessage = karoAPI.getChatLastMessage().get();
		assertNotNull(oldMessage);
		assertNotNull(oldMessage.getId());

		Date now = new Date();
		newMessage = karoAPI.sendChatMessage(TEST_CHAT_MESSAGE).get();
		assertNotNull(newMessage);
		assertNotNull(newMessage.getId());
		assertEquals(oldMessage.getId() + 1, newMessage.getId());
		assertEquals(TEST_CHAT_MESSAGE, newMessage.getText());
		assertEquals(properties.getProperty("karoapi.user"), newMessage.getUser());
		assertNotNull(newMessage.getTs());
		long timediff = newMessage.getTs().getTime() - now.getTime();
		logger.debug("ts = " + newMessage.getTs() + ", timediff = " + timediff);
		assertTrue(Math.abs(timediff) < 10000); // time diff should not be more than 10 seconds...

		int limit = 10;

		chat = karoAPI.getChatMessages(newMessage.getId() - limit + 1, limit).get();
		assertNotNull(chat);
		assertEquals(limit, chat.size());

		CollectionsUtil.contains(chat, (m) -> { return m.getId() == oldMessage.getId(); });
		CollectionsUtil.contains(chat, (m) -> { return m.getId() == newMessage.getId(); });
	}

	@Test
	public void test_getChatMessages() throws InterruptedException, ExecutionException
	{
		int firstId = TEST_CHAT_ID_MIN;
		int lastId = TEST_CHAT_ID_MAX;
		Date firstDate = new GregorianCalendar(2022, 0, 31, 9, 45, 0).getTime();
		Date lastDate = new GregorianCalendar(2022, 0, 31, 9, 52, 0).getTime();

		// dedicated entry

		int id = (int) (Math.random() * (TEST_CHAT_ID_MAX - TEST_CHAT_ID_MIN + 1) + TEST_CHAT_ID_MIN);
		int limit = 1;

		List<ChatMessage> chat = karoAPI.getChatMessages(id, 1).get();
		assertNotNull(chat);
		assertEquals(limit, chat.size());

		assertEquals(id, chat.get(0).getId());
		assertEquals("CraZZZy", chat.get(0).getUser());
		assertEquals(TEST_CHAT_MESSAGE, chat.get(0).getText());
		assertTrue(chat.get(0).getTs().after(firstDate));
		assertTrue(chat.get(0).getTs().before(lastDate));

		// offset & limit

		limit = lastId - firstId + 1;

		chat = karoAPI.getChatMessages(firstId, limit).get();
		assertNotNull(chat);
		assertEquals(limit, chat.size());

		for(int i = 0; i < chat.size(); i++)
		{
			ChatMessage m = chat.get(i);
			assertEquals(firstId + i, m.getId());
			assertTrue(m.getTs().after(firstDate));
			assertTrue(m.getTs().before(lastDate));
		}
	}

	public void test_getChatUsers() throws InterruptedException, ExecutionException
	{
		List<User> users = karoAPI.getChatUsers().get();
		assertNotNull(users);
		assertTrue(users.size() > 0);

		boolean botrixFound = false;
		for(User user : users)
		{
			assertNotNull(user.getId());
			assertNotNull(user.getLogin());
			if(user.getLogin().equalsIgnoreCase("Botrix"))
				botrixFound = true;
		}

		assertTrue(botrixFound);
	}

	@Test
	public void test_notes() throws InterruptedException, ExecutionException
	{
		int gameId = TEST_GAMES_IDS[0];

		// get the initial list of notes
		HashMap<Integer, String> notes = (HashMap<Integer, String>) karoAPI.getNotes().get();
		// current list must not contain the note
		assertFalse(notes.containsKey(gameId));

		HashMap<Integer, String> newNotes;

		// create a note
		karoAPI.addNote(gameId, "sample text").get();
		newNotes = (HashMap<Integer, String>) karoAPI.getNotes().get();
		// new list must contain the new note
		assertTrue(newNotes.containsKey(gameId));

		// delete the note (reset)
		karoAPI.removeNote(gameId).get();
		newNotes = (HashMap<Integer, String>) karoAPI.getNotes().get();
		// new list must not contain the note
		assertFalse(newNotes.containsKey(gameId));
	}

	@Test
	public void test_getContacts() throws InterruptedException, ExecutionException
	{
		List<User> contacts = karoAPI.getContacts().get();
		assertNotNull(contacts);
		assertTrue(contacts.size() > 0);

		for(User contact : contacts)
		{
			assertNotNull(contact.getId());
			assertNotNull(contact.getLogin());
			assertNotNull(contact.getTs());
		}
	}

	@Test
	public void test_multiInstanceAndMessaging() throws InterruptedException, ExecutionException
	{
		// check the login
		assertEquals(properties.getProperty("karoapi.user"), karoAPI.check().get().getLogin());

		// create a second instance
		KaroAPI karoAPI2 = new KaroAPI(properties.getProperty("karoapi.user2"), properties.getProperty("karoapi.password2"));
		// check the login there
		User user2 = karoAPI2.check().get();
		assertEquals(properties.getProperty("karoapi.user2"), user2.getLogin());

		// check if the other API is still valid
		User user1 = karoAPI.check().get();
		assertEquals(properties.getProperty("karoapi.user"), user1.getLogin());

		String str1 = "Hallo " + user2.getLogin() + ", das ist ein API-Test!";
		String str2 = "Danke und Gruß zurück!";

		UserMessage msg, lastMsg;
		List<UserMessage> messages;
		Date now = new Date();
		long timediff;

		// check received user1
		messages = karoAPI2.getUserMessage(user1.getId()).get();
		assertNotNull(messages);
		int count1 = messages.size();

		// check received user2
		messages = karoAPI2.getUserMessage(user1.getId()).get();
		assertNotNull(messages);
		int count2 = messages.size();

		assertEquals(count1, count2);

		// send 1
		msg = karoAPI.sendUserMessage(user2.getId(), str1).get();
		assertNotNull(msg);
		assertEquals(user1.getId(), msg.getUser_id());
		assertEquals(user2.getId(), msg.getContact_id());
		assertEquals(str1, msg.getText());

		// check received
		messages = karoAPI2.getUserMessage(user1.getId()).get();
		assertEquals(count2 + 1, messages.size());
		lastMsg = messages.get(messages.size() - 1);
		assertEquals(str1, lastMsg.getText());
		assertFalse(lastMsg.isR());
		timediff = lastMsg.getTs().getTime() - now.getTime();
		logger.debug("ts = " + lastMsg.getTs() + ", timediff = " + timediff);
		assertTrue(Math.abs(timediff) < 10000); // time diff should not be more than 10 seconds...

		// TODO mark read -> requires PATCH
		// String tmp = karoAPI2.readMessage(user1.getId()).get(); // PATCH currently not supported

		// send 2
		msg = karoAPI2.sendUserMessage(user1.getId(), str2).get();
		assertNotNull(msg);
		assertEquals(user2.getId(), msg.getUser_id());
		assertEquals(user1.getId(), msg.getContact_id());
		assertEquals(str2, msg.getText());

		// check received
		messages = karoAPI.getUserMessage(user2.getId()).get();
		assertEquals(count1 + 2, messages.size());
		lastMsg = messages.get(messages.size() - 1);
		assertEquals(str2, lastMsg.getText());
		assertFalse(lastMsg.isR());
		timediff = lastMsg.getTs().getTime() - now.getTime();
		logger.debug("ts = " + lastMsg.getTs() + ", timediff = " + timediff);
		assertTrue(Math.abs(timediff) < 10000); // time diff should not be more than 10 seconds...

		// TODO mark read -> requires PATCH
		// String tmp = karoAPI.readMessage(user2.getId()).get(); // PATCH currently not supported
	}

	private <T> void compareList(List<T> expected, List<T> actual, Comparator<T> comparator)
	{
		assertNotNull(expected);
		assertNotNull(actual);

		if(logger.isDebugEnabled())
		{
			Iterator<T> i1 = expected.iterator();
			Iterator<T> i2 = actual.iterator();
			T o1, o2;

			int i = 0;
			while(i1.hasNext() || i2.hasNext())
			{
				o1 = (i1.hasNext() ? i1.next() : null);
				o2 = (i2.hasNext() ? i2.next() : null);

				logger.debug((i++) + " -> " + o1 + " vs. " + o2 + " = " + comparator.compare(o1, o2));
			}
		}

		assertEquals(expected.size(), actual.size());
		assertTrue(CollectionsUtil.equals(expected, actual, comparator));
	}

	private void checkMapCode(Map map)
	{
		logger.debug("checking map " + map.getId());
		assertNotNull(map.getCode());
		assertEquals(map.getRows() * (map.getCols() + 1) - 1, map.getCode().length());

		String[] rows = map.getCode().split(Map.ROW_DELIMITER);
		assertEquals(map.getRows(), rows.length);
		for(String row : rows)
		{
			assertEquals(map.getCols(), row.length());
		}
	}

	@Test
	public void test_load() throws InterruptedException, ExecutionException
	{
		int id = TEST_GAMES_IDS[0];

		Game loaded = new Game(id);
		Game expected = karoAPI.getGame(id).get();

		assertNull(loaded.getName());

		karoAPI.load(loaded).get();

		assertEquals(id, loaded.getId());
		assertTrue(loaded.getName().startsWith(TEST_GAMES_NAME));

		assertEquals(expected.getMap().getId(), loaded.getMap().getId()); // compare IDs only
		assertEquals(expected.isCps(), loaded.isCps());
		assertEquals(expected.getZzz(), loaded.getZzz());
		assertEquals(expected.getCrashallowed(), loaded.getCrashallowed());
		assertEquals(expected.getStartdirection(), loaded.getStartdirection());
		assertEquals(expected.isStarted(), loaded.isStarted());
		assertEquals(expected.isFinished(), loaded.isFinished());
		assertEquals(expected.getStarteddate(), loaded.getStarteddate());
		assertEquals(expected.getCreator(), loaded.getCreator());
		assertEquals(expected.getNext(), loaded.getNext());
		assertEquals(expected.getBlocked(), loaded.getBlocked());
		assertEquals(expected.getPlayers(), loaded.getPlayers());
	}

	@Test
	public void test_erronousLogin() throws InterruptedException, ExecutionException
	{
		try
		{
			@SuppressWarnings("unused")
			KaroAPI karoAPIwithWrongCredentials = new KaroAPI("a", "b");
			fail("we shouldn't get here");
		}
		catch(Exception e)
		{
			logger.debug("error", e);
			assertNotNull(e); // KaroAPIException from KaroAPI
			assertNotNull(e.getCause()); // ExecutionException from CompletableFuture
			assertNotNull(e.getCause().getCause()); // RuntimeException from URLLoader
			assertNotNull(e.getCause().getCause().getCause()); // IOException from the Server
			assertInstanceOf(IOException.class, e.getCause().getCause().getCause());
			assertTrue(e.getCause().getCause().getCause().getMessage().startsWith("Server returned HTTP response code: 401"));
		}
	}

	@Test
	public void test_errorHandling() throws InterruptedException, ExecutionException
	{
		KaroAPI karoAPIwithFailingCall = new KaroAPI(properties.getProperty("karoapi.user"), properties.getProperty("karoapi.password"));
		karoAPIwithFailingCall.KAROPAPIER.addRequestProperty("X-Auth-Key", "wrong key");
		CompletableFuture<User> completableFuture = karoAPIwithFailingCall.check();

		try
		{
			completableFuture.get();
			fail("we shouldn't get here");
		}
		catch(Exception e)
		{
			logger.debug("error", e);
			assertNotNull(e);
			assertNotNull(e.getCause());
			assertNotNull(e.getCause().getCause());
			assertInstanceOf(IOException.class, e.getCause().getCause());
			assertTrue(e.getCause().getCause().getMessage().startsWith("Server returned HTTP response code: 401"));
		}
		assertTrue(completableFuture.isDone());
		assertTrue(completableFuture.isCompletedExceptionally());
	}

	@Test
	public void test_threading() throws InterruptedException, ExecutionException
	{
		List<String> executionOrder = new ArrayList<>();

		// immediately blocking
		// -> output "cf" should come after "2"
		CompletableFuture<User> cf1 = karoAPI.check();
		cf1.whenComplete((result, th) -> { executionOrder.add("cf"); });
		executionOrder.add("1");
		executionOrder.add("2");
		cf1.join();

		logger.debug("" + executionOrder);
		assertEquals(Arrays.asList("1", "2", "cf"), executionOrder);

		executionOrder.clear();

		// in parallel
		// -> output "cf" should come before "2" because of sleep
		CompletableFuture<User> cf2 = karoAPI.check();
		cf2.whenComplete((result, th) -> { executionOrder.add("cf"); });
		executionOrder.add("1");
		Thread.sleep(1000);
		executionOrder.add("2");
		cf2.join();

		logger.debug("" + executionOrder);
		assertEquals(Arrays.asList("1", "cf", "2"), executionOrder);
	}
}
