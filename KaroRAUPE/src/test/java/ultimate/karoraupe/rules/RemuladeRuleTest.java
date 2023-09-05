package ultimate.karoraupe.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class RemuladeRuleTest extends KaroRAUPETestcase
{	
	@Test
	public void test_isRemuladeGame_byTitle()
	{
		assertTrue(RemuladeRule.isRemuladeGame("§ REmulAde §", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ REmulAde § some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ REmulAde §some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ remulade §", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ remulade § some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ remulade §some title", null));
		
		assertTrue(RemuladeRule.isRemuladeGame("§REmulAde §", null));
		assertTrue(RemuladeRule.isRemuladeGame("§REmulAde § some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§REmulAde §some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§remulade §", null));
		assertTrue(RemuladeRule.isRemuladeGame("§remulade § some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§remulade §some title", null));

		assertTrue(RemuladeRule.isRemuladeGame("§ REmulAde§", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ REmulAde§ some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ REmulAde§some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ remulade§", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ remulade§ some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§ remulade§some title", null));

		assertTrue(RemuladeRule.isRemuladeGame("§REmulAde§", null));
		assertTrue(RemuladeRule.isRemuladeGame("§REmulAde§ some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§REmulAde§some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§remulade§", null));
		assertTrue(RemuladeRule.isRemuladeGame("§remulade§ some title", null));
		assertTrue(RemuladeRule.isRemuladeGame("§remulade§some title", null));
		
		assertFalse(RemuladeRule.isRemuladeGame("§RAmulAde§", null));
		assertFalse(RemuladeRule.isRemuladeGame("§RAmulAde§ some title", null));
		assertFalse(RemuladeRule.isRemuladeGame("§RAmulAde§some title", null));
		assertFalse(RemuladeRule.isRemuladeGame("§ramulade§", null));
		assertFalse(RemuladeRule.isRemuladeGame("§ramulade§ some title", null));
		assertFalse(RemuladeRule.isRemuladeGame("§ramulade§some title", null));
		
		assertFalse(RemuladeRule.isRemuladeGame("some title § REmulAde §", null));
		assertFalse(RemuladeRule.isRemuladeGame("some title §REmulAde §", null));
		assertFalse(RemuladeRule.isRemuladeGame("some title § REmulAde§", null));
		assertFalse(RemuladeRule.isRemuladeGame("some title §REmulAde§", null));
	}

	@Test
	public void test_isRemuladeGame_byTag()
	{		
		assertTrue(RemuladeRule.isRemuladeGame(null, new HashSet<>(Arrays.asList(new String[] {"§RE§"}))));
		assertTrue(RemuladeRule.isRemuladeGame(null, new HashSet<>(Arrays.asList(new String[] {"aaaa", "§RE§"}))));
		assertTrue(RemuladeRule.isRemuladeGame(null, new HashSet<>(Arrays.asList(new String[] {"§RE§", "aaaa"}))));

		assertFalse(RemuladeRule.isRemuladeGame(null, new HashSet<>(Arrays.asList(new String[] {"§rE§"}))));
		assertFalse(RemuladeRule.isRemuladeGame(null, new HashSet<>(Arrays.asList(new String[] {"aaaa", "§Re§"}))));
		assertFalse(RemuladeRule.isRemuladeGame(null, new HashSet<>(Arrays.asList(new String[] {"§re§", "aaaa"}))));
	}

	@Test
	public void test_countPlayersThisRound_countPlayersAlreadyMoved()
	{
		LinkedList<Player> players = new LinkedList<>();
		Game game = new Game();
		game.setPlayers(players);

		Player newPlayer;
		for(int i = 1; i <= 4; i++)
		{
			newPlayer = new Player();
			// increase number of players
			players.add(newPlayer);

			// case player finished last round
			newPlayer.setMoved(false);
			newPlayer.setRank(1);
			newPlayer.setPossibles(null);

			assertEquals(i-1, RemuladeRule.countPlayersThisRound(game));
			assertEquals(i-1, RemuladeRule.countPlayersAlreadyMoved(game));

			// case yet to move
			newPlayer.setMoved(false);
			newPlayer.setRank(0);
			newPlayer.setPossibles(null);

			assertEquals(i, RemuladeRule.countPlayersThisRound(game));
			assertEquals(i-1, RemuladeRule.countPlayersAlreadyMoved(game));

			// case player's turn
			newPlayer.setMoved(true);
			newPlayer.setRank(0);
			newPlayer.setPossibles(Arrays.asList(new Move()));

			assertEquals(i, RemuladeRule.countPlayersThisRound(game));
			assertEquals(i-1, RemuladeRule.countPlayersAlreadyMoved(game));

			// case player already moved
			newPlayer.setMoved(true);
			newPlayer.setRank(0);
			newPlayer.setPossibles(null);

			assertEquals(i, RemuladeRule.countPlayersThisRound(game));
			assertEquals(i, RemuladeRule.countPlayersAlreadyMoved(game));

			// we'll leave the player moved in the end for the next loop
		}
	}

	@Test
    public void test_findPreviousRoundFirstPlayer()
    {	
		int NUMBER_OF_PLAYERS = 4;

		// create a game with 4 players
		LinkedList<Player> players = new LinkedList<>();
		Player newPlayer;
		for(int p = 0; p < NUMBER_OF_PLAYERS; p++)
		{
			newPlayer = new Player(p);		
			newPlayer.setMoves(new LinkedList<>());
			players.add(newPlayer);			
		}

		Game game = new Game();
		game.setPlayers(players);

		long time = 0;

		// prepare		
		// shuffle players
		Collections.shuffle(players);
		// mark first player to move
		players.getFirst().setMoved(true);

		// play some rounds
		Player previousRE, currentRE = null;
		for(int round = 0; round < 10; round++)
		{
			// get the first player
			previousRE = currentRE;
			currentRE = players.getFirst();

			logger.debug("round = " + round + ", expectedPreviousRE = " + (previousRE != null ? previousRE.getId() : null));

			// move each player
			Player current, next;
			Move move;
			for(int i = 0; i < NUMBER_OF_PLAYERS; i++)
			{
				// increase time
				time++;

				// update current player
				current = players.get(i);

				// make sure we already marked the player to move
				assertTrue(current.isMoved());				

				// check result
				assertEquals(previousRE, RemuladeRule.findPreviousRoundFirstPlayer(game));

				// add crash once in a while
				if(time % 7 == 0)
				{				
					logger.debug("  crashing player " + current.getId());
					move = new Move();
					move.setT(new Date(time));
					move.setCrash(true);
					current.getMoves().add(move);
				}

				// move the player
				logger.debug("  moving player " + current.getId());
				move = new Move();
				move.setT(new Date(time));
				current.setPossibles(null);
				current.getMoves().add(move);

				// find and mark the next player
				if(i < NUMBER_OF_PLAYERS - 1)
				{
					next = players.get(i+1);
				}
				else
				{					
					// shuffle players
					Collections.shuffle(players);
					// reset player status (it's a new round)
					players.forEach(p -> {
						p.setMoved(false);
					});
					// get first player
					next = players.get(0);					
				}
				next.setMoved(true);
				next.setPossibles(Arrays.asList(new Move())); // add some possibles
			}
		}
    }

    @Test
    public void test_isREProtected()
    {
		Random r = new Random();

		int playerX = r.nextInt(100);
		int playerY = r.nextInt(100);
		int playerXv = r.nextInt(20) - 10;
		int playerYv = r.nextInt(20) - 10;

		int repeatX = playerX + playerXv;
		int repeatY = playerY + playerYv;

		Player player = new Player(0);
		player.setMotion(new Move(playerX, playerY, playerXv, playerYv, null));

		Game game = new Game();
		game.setPlayers(new LinkedList<>());
		game.getPlayers().add(player);
		
		logger.debug("player vectors: ");
		game.getPlayers().forEach(p -> { logger.debug("  player #" + p.getId() + ": " + p.getMotion()); });

		// make sure player is not protecting himself
		assertFalse(RemuladeRule.isREProtected(game, player));

		// add a couple of players
		Player newPlayer;
		for(int i = 1; i <= 5; i++)
		{
			newPlayer = new Player(i);
			game.getPlayers().add(newPlayer);

			// make sure the newPlayer is at the repeat position
			newPlayer.setMotion(new Move(repeatX, repeatY, r.nextInt(20) - 10, r.nextInt(20) - 10, null));

			logger.debug("player vectors: ");
			game.getPlayers().forEach(p -> { logger.debug("  player #" + p.getId() + ": " + p.getMotion()); });

			assertTrue(RemuladeRule.isREProtected(game, player));

			// now make sure the player has a different vector
			do
			{
				newPlayer.setMotion(new Move(r.nextInt(100), r.nextInt(100), r.nextInt(20) - 10, r.nextInt(20) - 10, null));
			} while(newPlayer.getMotion().getX() == repeatX && newPlayer.getMotion().getY() == repeatY);

			logger.debug("player vectors: ");
			game.getPlayers().forEach(p -> { logger.debug("  player #" + p.getId() + ": " + p.getMotion()); });

			assertFalse(RemuladeRule.isREProtected(game, player));
		}
    }

    @Test
    public void test_canRepeat()
    {        
		Random r = new Random();
		
		int playerX = r.nextInt(100);
		int playerY = r.nextInt(100);
		int playerXv = r.nextInt(20) - 10;
		int playerYv = r.nextInt(20) - 10;
		
		Player player = new Player(0);
		player.setMotion(new Move(playerX, playerY, playerXv, playerYv, null));
		
		int x, y, xv, yv;
		for(int dx = -1; dx <= 1; dx++)
		{
			for(int dy = -1; dy <= 1; dy++)
			{
				xv = playerXv + dx;
				yv = playerYv + dy;
				x = playerX + xv;
				y = playerY + yv;
				player.setPossibles(Arrays.asList(new Move(x, y, xv, yv, null)));

				if(dx == 0 && dy == 0)
					assertTrue(RemuladeRule.canRepeat(player));
				else
					assertFalse(RemuladeRule.canRepeat(player));
			}
		}
    }

    @Test
    public void test_needsToRepeat()
    {

    }
}
