package ultimate.karoraupe.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.rules.Rule.Result;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class RemuladeRuleTest extends KaroRAUPETestcase
{	
	private RemuladeRule rule = new RemuladeRule(karoAPI);

	public static Stream<Arguments> provideTitlesAndTags()
	{
		//@formatter:off
	    return Stream.of(			
			arguments("§ REmulAde §", null, true),
			arguments("§ REmulAde § some title", null, true),
			arguments("§ REmulAde §some title", null, true),
			arguments("§ remulade §", null, true),
			arguments("§ remulade § some title", null, true),
			arguments("§ remulade §some title", null, true),
			
			arguments("§REmulAde §", null, true),
			arguments("§REmulAde § some title", null, true),
			arguments("§REmulAde §some title", null, true),
			arguments("§remulade §", null, true),
			arguments("§remulade § some title", null, true),
			arguments("§remulade §some title", null, true),

			arguments("§ REmulAde§", null, true),
			arguments("§ REmulAde§ some title", null, true),
			arguments("§ REmulAde§some title", null, true),
			arguments("§ remulade§", null, true),
			arguments("§ remulade§ some title", null, true),
			arguments("§ remulade§some title", null, true),

			arguments("§REmulAde§", null, true),
			arguments("§REmulAde§ some title", null, true),
			arguments("§REmulAde§some title", null, true),
			arguments("§remulade§", null, true),
			arguments("§remulade§ some title", null, true),
			arguments("§remulade§some title", null, true),
			
			arguments("§RAmulAde§", null, false),
			arguments("§RAmulAde§ some title", null, false),
			arguments("§RAmulAde§some title", null, false),
			arguments("§ramulade§", null, false),
			arguments("§ramulade§ some title", null, false),
			arguments("§ramulade§some title", null, false),
			
			arguments("some title § REmulAde §", null, false),
			arguments("some title §REmulAde §", null, false),
			arguments("some title § REmulAde§", null, false),
			arguments("some title §REmulAde§", null, false),
			
			arguments(null, new HashSet<>(Arrays.asList(new String[] {"§RE§"})), true),
			arguments(null, new HashSet<>(Arrays.asList(new String[] {"aaaa", "§RE§"})), true),
			arguments(null, new HashSet<>(Arrays.asList(new String[] {"§RE§", "aaaa"})), true),
			arguments("foo", new HashSet<>(Arrays.asList(new String[] {"§RE§", "aaaa"})), true),
			arguments("§REmulAde§", new HashSet<>(Arrays.asList(new String[] {"§RE§", "aaaa"})), true),

			arguments(null, new HashSet<>(Arrays.asList(new String[] {"§rE§"})), false),
			arguments(null, new HashSet<>(Arrays.asList(new String[] {"aaaa", "§Re§"})), false),
			arguments(null, new HashSet<>(Arrays.asList(new String[] {"§re§", "aaaa"})), false),
			arguments("foo", new HashSet<>(Arrays.asList(new String[] {"§re§", "aaaa"})), false),
			arguments("§REmulAde§", new HashSet<>(Arrays.asList(new String[] {"§re§", "aaaa"})), true)
	    );
	    //@formatter:on
	}

	@ParameterizedTest
	@MethodSource("provideTitlesAndTags")
	public void test_isRemuladeGame_byTitle(String title, Set<String> tags, boolean expectedResult)
	{
		assertEquals(expectedResult, RemuladeRule.isRemuladeGame(title,tags));
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
		Player current, next;
		Move move;
		for(int round = 0; round < 14; round++)
		{
			// get the first player
			previousRE = currentRE;
			currentRE = players.getFirst();

			logger.debug("round = " + round + ", expectedPreviousRE = " + (previousRE != null ? previousRE.getId() : null));

			// move each player
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

	public static Stream<Arguments> provideREInfo()
	{
		Player player = new Player(1);
		Player other = new Player(2);

		//@formatter:off
	    return Stream.of(			
			// 1 player only
			arguments(player, true, 1, 0, player, false), // only repeat if first time in a row
			arguments(player, true, 1, 0, other, true),
			// 3 players
			arguments(player, true, 3, 0, player, false), // only repeat if first time in a row
			arguments(player, true, 3, 1, player, false),
			arguments(player, true, 3, 2, player, false),
			arguments(player, true, 3, 0, other, true),
			arguments(player, true, 3, 1, other, false),
			arguments(player, true, 3, 2, other, false),
			// 5 players
			arguments(player, true, 5, 0, player, true),
			arguments(player, true, 5, 1, player, false),
			arguments(player, true, 5, 2, player, false),
			arguments(player, true, 5, 3, player, false),
			arguments(player, true, 5, 4, player, false),
			arguments(player, true, 5, 0, other, true),
			arguments(player, true, 5, 1, other, false),
			arguments(player, true, 5, 2, other, false),
			arguments(player, true, 5, 3, other, false),
			arguments(player, true, 5, 4, other, false),
			// 7 players
			arguments(player, true, 7, 0, player, true),
			arguments(player, true, 7, 1, player, true), // 2 players need to repeat
			arguments(player, true, 7, 2, player, false),
			arguments(player, true, 7, 3, player, false),
			arguments(player, true, 7, 4, player, false),
			arguments(player, true, 7, 5, player, false),
			arguments(player, true, 7, 6, player, false),
			arguments(player, true, 7, 0, other, true),
			arguments(player, true, 7, 1, other, true), // 2 players need to repeat
			arguments(player, true, 7, 2, other, false),
			arguments(player, true, 7, 3, other, false),
			arguments(player, true, 7, 4, other, false),
			arguments(player, true, 7, 5, other, false),
			arguments(player, true, 7, 6, other, false),
			// 10 players
			arguments(player, true, 10, 0, player, true),
			arguments(player, true, 10, 1, player, true), // 2 players need to repeat
			arguments(player, true, 10, 2, player, false),
			arguments(player, true, 10, 3, player, false),
			arguments(player, true, 10, 4, player, false),
			arguments(player, true, 10, 5, player, false),
			arguments(player, true, 10, 6, player, false),
			arguments(player, true, 10, 7, player, false),
			arguments(player, true, 10, 8, player, false),
			arguments(player, true, 10, 9, player, false),
			arguments(player, true, 10, 0, other, true),
			arguments(player, true, 10, 1, other, true), // 2 players need to repeat
			arguments(player, true, 10, 2, other, false),
			arguments(player, true, 10, 3, other, false),
			arguments(player, true, 10, 4, other, false),
			arguments(player, true, 10, 5, other, false),
			arguments(player, true, 10, 6, other, false),
			arguments(player, true, 10, 7, other, false),
			arguments(player, true, 10, 8, other, false),
			arguments(player, true, 10, 9, other, false),
			// 14 players
			arguments(player, true, 14, 0, player, true),
			arguments(player, true, 14, 1, player, true), // 3 players need to repeat
			arguments(player, true, 14, 2, player, true), // 3 players need to repeat
			arguments(player, true, 14, 3, player, false),
			arguments(player, true, 14, 4, player, false),
			arguments(player, true, 14, 5, player, false),
			arguments(player, true, 14, 6, player, false),
			arguments(player, true, 14, 7, player, false),
			arguments(player, true, 14, 8, player, false),
			arguments(player, true, 14, 9, player, false),
			arguments(player, true, 14, 10, player, false),
			arguments(player, true, 14, 11, player, false),
			arguments(player, true, 14, 12, player, false),
			arguments(player, true, 14, 13, player, false),
			arguments(player, true, 14, 0, other, true),
			arguments(player, true, 14, 1, other, true), // 3 players need to repeat
			arguments(player, true, 14, 2, other, true), // 3 players need to repeat
			arguments(player, true, 14, 3, other, false),
			arguments(player, true, 14, 4, other, false),
			arguments(player, true, 14, 5, other, false),
			arguments(player, true, 14, 6, other, false),
			arguments(player, true, 14, 7, other, false),
			arguments(player, true, 14, 8, other, false),
			arguments(player, true, 14, 9, other, false),
			arguments(player, true, 14, 10, other, false),
			arguments(player, true, 14, 11, other, false),
			arguments(player, true, 14, 12, other, false),
			arguments(player, true, 14, 13, other, false)
	    );
	    //@formatter:on
	}

	@ParameterizedTest
	@MethodSource("provideREInfo")
    public void test_needsToRepeat(Player player, boolean canRepeat, int playersThisRound, int playersAlreadyMoved, Player previousRoundFirstPlayer, boolean expectedResult)
    {
		assertEquals(expectedResult, RemuladeRule.needsToRepeat(player, canRepeat, false, playersThisRound, playersAlreadyMoved, previousRoundFirstPlayer));
    }

	private static Move createMove(int x, int y, int xv, int yv, long time)
	{
		Move m = new Move(x, y, xv, yv, null);
		m.setT(new Date(time));
		return m;
	}

    @Test
    public void test_evaluate()
    {
		Game game = new Game();

		Player player = new Player(1);
		player.setMoves(new ArrayList<>());
		
		Player other = new Player(2);
		other.setMoves(new ArrayList<>());

		// add some moves
		long time = 0;
		// start
		other.getMoves().add(createMove(0, 0, 0, 0, time++));
		player.getMoves().add(createMove(0, 0, 0, 0, time++));
		// round 1
		other.getMoves().add(createMove(0, 1, 0, 1, time++));
		player.getMoves().add(createMove(1, 0, 1, 0, time++));
		// round 2
		other.getMoves().add(createMove(0, 2, 0, 2, time++));
		player.getMoves().add(createMove(3, 0, 2, 0, time++));

		// set motion
		player.setMotion(player.getMoves().get(player.getMoves().size()-1));
		other.setMotion(other.getMoves().get(other.getMoves().size()-1));

		// construct possibles
		player.setPossibles(new ArrayList<>(9));
		other.setPossibles(new ArrayList<>(9));
		int x, y, xv, yv;
		for(int dx = -1; dx <= 1; dx++)
		{
			for(int dy = -1; dy <= 1; dy++)
			{
				xv = player.getMotion().getXv() + dx;
				yv = player.getMotion().getYv() + dy;
				x = player.getMotion().getX() + xv;
				y = player.getMotion().getY() + yv;
				player.getPossibles().add(new Move(x, y, xv, yv, null));

				xv = other.getMotion().getXv() + dx;
				yv = other.getMotion().getYv() + dy;
				x = other.getMotion().getX() + xv;
				y = other.getMotion().getY() + yv;
				other.getPossibles().add(new Move(x, y, xv, yv, null));
			}
		}

		game.setPlayers(Arrays.asList(player, other));

		Properties gameConfig = new Properties();

		Result result;


		// wrong title, no tags
		gameConfig.setProperty(RemuladeRule.KEY_SPECIAL_REMULADE, "true");
		game.setName("foo");
		game.setTags(null);
		result = rule.evaluate(game, player, gameConfig);
		assertNull(result.shallMove());
		assertTrue(result.getReason().contains("not a "));

		// wrong title, wrong tags
		gameConfig.setProperty(RemuladeRule.KEY_SPECIAL_REMULADE, "true");
		game.setName("foo");
		game.setTags(new HashSet<>(Arrays.asList("foo")));
		result = rule.evaluate(game, player, gameConfig);
		assertNull(result.shallMove());
		assertTrue(result.getReason().contains("not a "));

		// not activated
		gameConfig.setProperty(RemuladeRule.KEY_SPECIAL_REMULADE, "false");
		game.setName("foo");
		game.setTags(new HashSet<>(Arrays.asList("§RE§")));
		result = rule.evaluate(game, player, gameConfig);
		assertNull(result.shallMove());
		assertTrue(result.getReason().contains("not activated"));

		// correct title
		gameConfig.setProperty(RemuladeRule.KEY_SPECIAL_REMULADE, "true");
		game.setName("§ REmulAde §");
		game.setTags(null);
		result = rule.evaluate(game, player, gameConfig);
		assertEquals(true, result.shallMove());

		// correct tag
		gameConfig.setProperty(RemuladeRule.KEY_SPECIAL_REMULADE, "true");
		game.setName("foo");
		game.setTags(new HashSet<>(Arrays.asList("§RE§")));
		result = rule.evaluate(game, player, gameConfig);
		assertEquals(true, result.shallMove());
		
		// no need (1 case - all other cases tested seperately)
		player.getPossibles().remove(4); // remove center move, to make canRepeat = false
		gameConfig.setProperty(RemuladeRule.KEY_SPECIAL_REMULADE, "true");
		game.setName("§ REmulAde §");
		game.setTags(new HashSet<>(Arrays.asList("§RE§")));
		result = rule.evaluate(game, player, gameConfig);
		assertNull(result.shallMove());
		assertTrue(result.getReason().contains("no need"));
    }
}
