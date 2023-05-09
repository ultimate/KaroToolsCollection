package ultimate.karoapi4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Options;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.test.KaroAPITestcase;

@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
public class KaroAPICacheTest extends KaroAPITestcase
{
	@Test
	@Order(1)
	public void test_init()
	{
		assertNotNull(karoAPICache);

		assertNotNull(karoAPICache.getUsers());
		assertTrue(karoAPICache.getUsers().size() > 0);

		int uid = 1;
		assertNotNull(karoAPICache.getUsersById().get(uid));
		assertEquals("Didi", karoAPICache.getUsersById().get(uid).getLogin());

		assertNotNull(karoAPICache.getMaps());
		assertTrue(karoAPICache.getMaps().size() > 0);

		int mid = 1;
		assertNotNull(karoAPICache.getMapsById().get(mid));
		assertEquals("Die Erste", karoAPICache.getMapsById().get(mid).getName());

		assertNotNull(karoAPICache.getGames());
		assertTrue(karoAPICache.getGames().size() == 0); // no games preloaded
	}

	@Test
	@Order(2)
	public void test_currentUser()
	{
		assertNotNull(karoAPICache.getCurrentUser());
		assertTrue(karoAPICache.getCurrentUser() == karoAPICache.getUser(karoAPICache.getCurrentUser().getId()));
	}

	@Test
	@Order(3)
	public void test_loading()
	{
		int uid = 9; // this user is inactive

		// user should not be preloaded
		assertNull(karoAPICache.getUsersById().get(uid));
		// but direct access should trigger loading
		assertNotNull(karoAPICache.getUser(uid));
		assertEquals("Jones Villeneuve", karoAPICache.getUser(uid).getLogin());
		// now the user should be cached
		assertNotNull(karoAPICache.getUsersById().get(uid));
	}

	@Test
	@Order(4)
	public void test_refreshing() throws InterruptedException, ExecutionException
	{
		// check the number of games this user has
		int games = karoAPICache.getCurrentUser().getActiveGames();

		// now create a game
		PlannedGame plannedGame = new PlannedGame();
		plannedGame.setMap(new Map(105));
		plannedGame.getPlayers().add(karoAPICache.getCurrentUser());
		plannedGame.setName("KaroAPI-Test-Game");
		plannedGame.setOptions(new Options(2, true, EnumGameDirection.free, EnumGameTC.free));
		Game game = karoAPI.createGame(plannedGame).get();
		logger.debug("game created: id=" + game.getId() + ", name=" + game.getName());

		// still the current user should not have the same amount of games
		assertEquals(games, karoAPICache.getCurrentUser().getActiveGames());

		// now refresh
		User previousObject = karoAPICache.getCurrentUser();
		User refreshedObject = karoAPICache.refresh(karoAPICache.getCurrentUser()).get();
		// check that it is still the same entity
		assertTrue(previousObject == refreshedObject);

		// now the current user should have 1 game more
		assertEquals(games + 1, karoAPICache.getCurrentUser().getActiveGames());

		// finish the game for clean up, for each step refresh and check the game
		//@formatter:off
		assertEquals(0, karoAPICache.refresh(game).get().getPlayers().get(0).getMoveCount()); 	// refresh to also get the players & moves
		assertTrue(karoAPI.selectStartPosition(game.getId(), new Move(2, 1, null)).get()); 		// select start position
		assertEquals(1, karoAPICache.refresh(game).get().getPlayers().get(0).getMoveCount()); 	// refresh & check
		assertTrue(karoAPI.move(game.getId(), new Move(3, 1, 1, 0, null)).get()); 				// move 1
		assertEquals(2, karoAPICache.refresh(game).get().getPlayers().get(0).getMoveCount());	// refresh & check
		assertTrue(karoAPI.move(game.getId(), new Move(5, 1, 2, 0, null)).get());				// move 2
		assertEquals(4, karoAPICache.refresh(game).get().getPlayers().get(0).getMoveCount());	// refresh & check
		//@formatter:on

		// check the games again - should be back to were it was before
		karoAPICache.refresh(karoAPICache.getCurrentUser()).get();
		assertEquals(games, karoAPICache.getCurrentUser().getActiveGames());
	}

	@Test
	@Order(5)
	public void test_refreshingNewGame() throws InterruptedException, ExecutionException
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

		Thread.sleep(sleep);

		// the result should not be null, but does not contain all fields
		assertFalse(karoAPICache.contains(game));
		assertNotNull(game);
		assertNotNull(game.getId());
		assertNull(game.getPlayers());

		// load full game -> via refresh
		karoAPICache.refresh(game).join();
		assertTrue(karoAPICache.contains(game));
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

		boolean left = karoAPI.leaveGame(gameId).get();
		assertTrue(left);
	}

	@Test
	@Order(6)
	public void test_refreshingOldGame() throws InterruptedException, ExecutionException
	{
		int id = 136424;

		Game game = new Game(id);
		
		assertNotNull(game.getId());
		assertEquals(id, game.getId());
		assertNull(game.getName());
		assertFalse(game.isStarted());
		assertFalse(game.isFinished());

		// load game -> via refresh
		karoAPICache.refresh(game).join();
		
		assertNotNull(game.getId());
		assertEquals(id, game.getId());
		assertNotNull(game.getName());
		assertEquals("CraZZZy Crash Challenge 6 - Challenge 1.4 - Karte 10059 | 3er Challenge | ZZZ=25", game.getName());
		assertTrue(game.isStarted());
		assertTrue(game.isFinished());
	}

	@Test
	@Order(7)
	public void test_imageCaching() throws InterruptedException, ExecutionException
	{
		logger.info("image cache=" + karoAPICache.getCacheFolder().getAbsolutePath());

		assertTrue(karoAPICache.isLoadImages());

		File img;
		File thumb;
		for(Map m : karoAPICache.getMaps())
		{
			logger.debug("checking images for map #" + m.getId());

			assertNotNull(m.getImage());
			assertNotNull(m.getThumb());

			img = new File(karoAPICache.getCacheFolder(), m.getId() + "." + KaroAPICache.IMAGE_TYPE);
			thumb = new File(karoAPICache.getCacheFolder(), m.getId() + "_thumb." + KaroAPICache.IMAGE_TYPE);

			assertTrue(img.exists());
			assertTrue(thumb.exists());
		}
	}

	@Test
	@Order(8)
	public void test_cachingAndUncaching() throws InterruptedException, ExecutionException
	{
		int uderId = 1;
		String userName = "Didi";
		String newName = "NotDidi";

		User user = karoAPICache.getUser(uderId);

		// check initial state
		assertTrue(karoAPICache.contains(user));
		assertTrue(user == karoAPICache.getUser(1));
		assertEquals(userName, user.getLogin());

		// remove from cache
		karoAPICache.uncache(User.class, uderId);

		// check again
		assertFalse(karoAPICache.contains(user));
		assertNotNull(karoAPICache.getUser(1)); // user is automatically reloaded
		assertFalse(user == karoAPICache.getUser(1));
		assertEquals(userName, user.getLogin());

		// rename user and replace user in cache (with error)
		user.setLogin(newName);
		try
		{
			karoAPICache.cache(user);
			fail("expected exception");
		}
		catch(IllegalArgumentException e)
		{
			assertNotNull(e);
		}

		// replace user in cache (valid)
		karoAPICache.uncache(User.class, uderId);
		karoAPICache.cache(user);

		// check again
		assertTrue(karoAPICache.contains(user));
		assertTrue(user == karoAPICache.getUser(1));
		assertEquals(newName, user.getLogin());

		// refresh the user
		karoAPICache.refresh(user).join();

		// check again
		assertTrue(karoAPICache.contains(user));
		assertTrue(user == karoAPICache.getUser(1));
		assertEquals(userName, user.getLogin());
	}
}
