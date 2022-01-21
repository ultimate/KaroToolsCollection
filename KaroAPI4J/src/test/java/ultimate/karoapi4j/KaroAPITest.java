package ultimate.karoapi4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.enums.EnumUserGamesort;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.test.KaroAPITestcase;
import ultimate.karoapi4j.utils.CollectionsUtil;
import ultimate.karoapi4j.utils.MethodComparator;

public class KaroAPITest extends KaroAPITestcase
{
	private static final int	VALIDATION_LIMIT	= 10;
	private static final String	TEST_GAMES_NAME		= "KaroAPI4J-Testspiel#";
	private static final int[]	TEST_GAMES_IDS		= new int[] { 132803, 132804, 132805, 132806, 132807 };

	@Test
	public void test_check() throws InterruptedException
	{
		User user = karoAPI.check().doBlocking();

		assertNotNull(user);
		assertEquals(properties.get("karoapi.user"), user.getLogin());
	}

	@Test
	public void test_getUsers() throws InterruptedException
	{
		List<User> users = karoAPI.getUsers().doBlocking();
		logger.debug("loaded users: " + users.size());

		// check global user list

		assertNotNull(users);
		// should be more than just a few users...
		assertTrue(users.size() > 100);
		// should contain the current user
		Predicate<User> findCurrentUser = (user) -> { return user.getLogin().equals(properties.get("karoapi.user")); };
		assertTrue(CollectionsUtil.contains(users, findCurrentUser));

		// check invitable filter

		List<User> invitables = karoAPI.getUsers(null, true, null).doBlocking();
		logger.debug("loaded invitable: " + invitables.size());
		assertNotNull(invitables);
		// all users in this list should be invitable
		Predicate<User> findInvitable = (user) -> { return user.isInvitable(); };
		assertEquals(invitables.size(), CollectionsUtil.count(invitables, findInvitable));
		// the users in invitable should match the invitables from the global user list
		List<User> users_filteredToInvitables = new ArrayList<User>(users);
		users_filteredToInvitables.removeIf(findInvitable.negate());
		compareList(users_filteredToInvitables, invitables, new MethodComparator<>("getId", 1));

		// check desperate filter

		List<User> desperates = karoAPI.getUsers(null, null, true).doBlocking();
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
		List<User> bylogin = karoAPI.getUsers(login, null, null).doBlocking();
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

		List<User> filtered = karoAPI.getUsers(login, true, true).doBlocking();
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
	public void test_getUser() throws InterruptedException
	{
		int id;
		User user;

		id = 1;
		{
			user = karoAPI.getUser(id).doBlocking();
			logger.debug("loaded user: " + user.getId() + " = " + user.getLogin());
			assertEquals(id, user.getId());
			assertEquals("Didi", user.getLogin());
			assertTrue(user.isSuperCreator());
			assertFalse(user.isBot());
		}

		id = 773;
		{
			user = karoAPI.getUser(id).doBlocking();
			logger.debug("loaded user: " + user.getId() + " = " + user.getLogin());
			assertEquals(id, user.getId());
			assertEquals("Botrix", user.getLogin());
			assertFalse(user.isSuperCreator());
			assertTrue(user.isBot());
		}

		id = 2248;
		{
			user = karoAPI.getUser(id).doBlocking();
			logger.debug("loaded user: " + user.getId() + " = " + user.getLogin());
			assertEquals(id, user.getId());
			assertEquals("KaroLigaCupStarter", user.getLogin());
			assertTrue(user.isSuperCreator());
			assertFalse(user.isBot());
		}
	}

	@Test
	public void test_getUserDran() throws InterruptedException
	{
		// get the top-blocker first
		User topBlocker = karoAPI.getBlockers().doBlocking().get(0);

		// now check his dran games
		List<Game> dran = karoAPI.getUserDran(topBlocker.getId()).doBlocking();
		assertNotNull(dran);
		assertTrue(dran.size() > 0);
		for(int i = 0; i < dran.size() && i < VALIDATION_LIMIT; i++)
		{
			Game fullgame = karoAPI.getGame(dran.get(i).getId()).doBlocking();
			assertEquals(topBlocker.getId(), fullgame.getNext().getId());
		}
	}

	@Test
	public void test_getBlockers() throws InterruptedException
	{
		List<User> blockers = karoAPI.getBlockers().doBlocking();
		assertNotNull(blockers);
		assertTrue(blockers.size() > 0);
		
		for(int i = 0; i < blockers.size() && i < VALIDATION_LIMIT; i++)
		{
			assertTrue(blockers.get(i).getDran() > 0);
		}
	}

	@Test
	public void test_getUserBlockers() throws InterruptedException
	{
		int userId = 773; // Botrix (since there should always be someone blocking a bot...
		List<User> blockers = karoAPI.getUserBlockers(userId).doBlocking();
		assertNotNull(blockers);
		assertTrue(blockers.size() > 0);
		
		for(int i = 0; i < blockers.size() && i < VALIDATION_LIMIT; i++)
		{
			assertTrue(blockers.get(i).getBlocked() > 0);
		}
	}

	@Test
	public void test_getGames() throws InterruptedException
	{
		List<Game> games = karoAPI.getGames().doBlocking();

		// for CraZZZy the global can be empty
		assertNotNull(games);
		logger.debug("loaded games: " + games.size());

		// but if we request !mine, then there should be some games
		games = karoAPI.getGames(false, null, null, null, null, null, null, null).doBlocking();
		assertNotNull(games);
		logger.debug("loaded games: " + games.size());
		assertTrue(games.size() > 0);

		// check name filter and sorting for some (finished) test games

		List<Game> games1 = karoAPI.getGames(null, EnumUserGamesort.gid, null, true, TEST_GAMES_NAME, 0, null, null).doBlocking();
		List<Game> games2 = karoAPI.getGames(null, EnumUserGamesort.name, null, true, TEST_GAMES_NAME, 0, null, null).doBlocking();
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
			limited = karoAPI.getGames(null, EnumUserGamesort.name, null, true, null, null, 1, i).doBlocking();
			assertNotNull(limited);
			assertEquals(1, limited.size());
			assertEquals(TEST_GAMES_NAME + (i + 1), limited.get(0).getName());
		}
	}

	@Test
	public void test_getGame() throws InterruptedException
	{
		int id;
		Game game;

		for(int i = 0; i < TEST_GAMES_IDS.length; i++)
		{
			id = TEST_GAMES_IDS[i];
			game = karoAPI.getGame(id).doBlocking();
			logger.debug("loaded game: " + game.getId() + " = " + game.getName());
			assertEquals(id, game.getId());
			assertTrue(game.getName().startsWith(TEST_GAMES_NAME));
			assertEquals("CraZZZy", game.getCreator());
		}

		id = 44773;
		{
			game = karoAPI.getGame(id).doBlocking();
			logger.debug("loaded game: " + game.getId() + " = " + game.getName());
			assertEquals(id, game.getId());
			assertEquals("Runde um Runde nehmen wir jede Ecke und bleiben auf der Strecke!", game.getName());
			assertEquals("Madeleine", game.getCreator());
		}
	}

	@Test
	public void test_getMaps() throws InterruptedException
	{
		List<Map> maps = karoAPI.getMaps().doBlocking();
		logger.debug("loaded maps: " + maps.size());

		// check global map list

		assertNotNull(maps);
		// should be more than just a few maps...
		assertTrue(maps.size() > 100);

		// check mapcode argument

		List<Map> maps_withcode = karoAPI.getMaps(true).doBlocking();
		logger.debug("loaded maps_withcode: " + maps_withcode.size());
		assertNotNull(maps_withcode);
		// all maps in this list should contain a mapcode
		maps_withcode.forEach((map) -> { checkMapCode(map); });
		// the maps in invitable should match the global map list
		compareList(maps, maps_withcode, new MethodComparator<>("getId", 1));
	}

	@Test
	public void test_getMap() throws InterruptedException
	{
		int id;
		Map map;

		id = 1;
		{
			map = karoAPI.getMap(id).doBlocking();
			logger.debug("loaded map: " + map.getId() + " = " + map.getName() + " (by " + map.getAuthor() + ")");
			assertEquals(id, map.getId());
			assertEquals("Die Erste", map.getName());
			assertEquals("Didi", map.getAuthor());
			assertTrue(map.isActive());
			assertFalse(map.isNight());
		}

		id = 25; // inactive
		{
			map = karoAPI.getMap(id).doBlocking();
			logger.debug("loaded map: " + map.getId() + " = " + map.getName() + " (by " + map.getAuthor() + ")");
			assertEquals(id, map.getId());
			assertEquals("Nadelöhr", map.getName());
			assertEquals("Wolfgang Preiß", map.getAuthor());
			assertFalse(map.isActive());
			assertFalse(map.isNight());
		}

		id = 1000; // night
		{
			map = karoAPI.getMap(id).doBlocking();
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
			map = karoAPI.getMap(id, true).doBlocking();
			logger.debug("loaded map: " + map.getId() + " = " + map.getName() + " (by " + map.getAuthor() + ") with code");
			assertEquals(id, map.getId());
			checkMapCode(map);
		}
	}

	@Test
	public void test_getMapImage() throws InterruptedException
	{
		int id = 1;
		BufferedImage image;

		Map map = karoAPI.getMap(id).doBlocking();

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
	public void test_createGameAndMove()
	{
		fail("not implemented");
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
}
