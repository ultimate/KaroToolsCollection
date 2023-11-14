package ultimate.karomuskel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.Options;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karomuskel.test.KaroMUSKELTestcase;

public class CreatorTest extends KaroMUSKELTestcase
{
	@Test
	@Order(5)
	public void test_createAndLeaveGame() throws InterruptedException, ExecutionException
	{
		int sleep = 500;
		Creator creator = new Creator(karoAPICache);
		
		User user = karoAPI.check().get();

		PlannedGame plannedGame = new PlannedGame();
		plannedGame.setMap(new Map(105));
		plannedGame.getPlayers().add(user);
		plannedGame.setName("KaroAPI-Test-Game");
		plannedGame.setOptions(new Options(2, true, EnumGameDirection.free, EnumGameTC.free));

		// check the initial state
		assertNull(plannedGame.getGame());
		
		// create game using the creator
		creator.createGames(Arrays.asList(plannedGame), null).join();

		Thread.sleep(sleep);
		
		// check the result
		assertNotNull(plannedGame.getGame());
		assertNotNull(plannedGame.getGame().getId());
		assertTrue(plannedGame.isCreated());
		assertFalse(plannedGame.isLeft());
		assertTrue(karoAPICache.contains(plannedGame.getGame()));
		assertTrue(plannedGame.getGame() == karoAPICache.getGame(plannedGame.getGame().getId()));
		
		// check the players
		assertNull(plannedGame.getGame().getPlayers());
		
		// refresh to load the players
		karoAPICache.refresh(plannedGame.getGame()).join();
		
		// check the players again
		assertNotNull(plannedGame.getGame().getPlayers());
		
		// leave game using the creator
		creator.leaveGames(Arrays.asList(plannedGame), null).join();

		Thread.sleep(sleep);
		
		// check the result
		assertNotNull(plannedGame.getGame());
		assertNotNull(plannedGame.getGame().getId());
		assertTrue(plannedGame.isCreated());
		assertTrue(plannedGame.isLeft());
		assertTrue(karoAPICache.contains(plannedGame.getGame()));
		assertTrue(plannedGame.getGame() == karoAPICache.getGame(plannedGame.getGame().getId()));
	}
}
