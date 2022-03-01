package ultimate.karoapi4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		// check that this user has no game
		assertEquals(0, karoAPICache.getCurrentUser().getActiveGames());

		// now create a game
		PlannedGame plannedGame = new PlannedGame();
		plannedGame.setMap(new Map(105));
		plannedGame.getPlayers().add(karoAPICache.getCurrentUser());
		plannedGame.setName("KaroAPI-Test-Game");
		plannedGame.setOptions(new Options(2, true, EnumGameDirection.free, EnumGameTC.free));
		Game game = karoAPI.createGame(plannedGame).get();
		logger.debug("game created: id=" + game.getId() + ", name=" + game.getName());

		// still the current user should not have a game
		assertEquals(0, karoAPICache.getCurrentUser().getActiveGames());

		// now refresh
		User previousObject = karoAPICache.getCurrentUser();
		User refreshedObject = karoAPICache.refresh(karoAPICache.getCurrentUser()).get();
		// check that it is still the same entity
		assertTrue(previousObject == refreshedObject);

		// now the current user should have a game
		assertEquals(1, karoAPICache.getCurrentUser().getActiveGames());

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

		// check the games again
		karoAPICache.refresh(karoAPICache.getCurrentUser()).get();
		assertEquals(0, karoAPICache.getCurrentUser().getActiveGames());
	}

	@Test
	@Order(5)
	public void test_imageCaching() throws InterruptedException, ExecutionException
	{
		logger.info("image cache=" + karoAPICache.getCacheFolder().getAbsolutePath());

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
}
