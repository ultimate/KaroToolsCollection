package ultimate.karoraupe.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.Mover;
import ultimate.karoraupe.rules.Rule.Result;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class TimeoutRuleTest extends KaroRAUPETestcase
{	
	private TimeoutRule rule = new TimeoutRule(karoAPI);

	@Test
	public void test_getLastMoveDate()
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

		long time = 0;

		Game game = new Game();
		game.setStarteddate(new Date(time));
		game.setPlayers(players);

		// prepare		
		// shuffle players
		Date moveDate;
		Player current;
		Move move = null;
		for(int round = 0; round < 10; round++)
		{
			Collections.shuffle(players);

			logger.debug("round = " + round);

			// move each player
			for(int i = 0; i < NUMBER_OF_PLAYERS; i++)
			{
				// increase time
				time++;

				// update current player
				current = players.get(i);

				// add crash once in a while
				if(time % 7 == 0)
				{				
					logger.debug("   t = " + time + " : crashing player " + current.getId());
					move = new Move();
					move.setT(new Date(time));
					move.setCrash(true);
					current.getMoves().add(move);
				}

				// move the player
				logger.debug("   t = " + time + " : moving player " + current.getId());
				move = new Move();
				move.setT(new Date(time));
				current.getMoves().add(move);
				
				moveDate = TimeoutRule.getLastMoveDate(game);
				assertEquals(move.getT(), moveDate);
			}
		}
	}

	public static Stream<Arguments> provideTimes()
	{
		//@formatter:off
	    return Stream.of(
			arguments(60, -120, null),
			arguments(60, -30, false),
			arguments(60, 0, false),
			arguments(60, +30, false),
			arguments(60, +120, false)
	    );
	    //@formatter:on
	}

	@ParameterizedTest
	@MethodSource("provideTimes")
	public void test_evaluate(int timeout, int lastMoveDelta, Boolean expectedResult)
	{
		Game game = new Game();
		game.setStarteddate(new Date(0));
		game.setPlayers(new LinkedList<>());
		game.getPlayers().add(new Player());	
	
		
		Properties gameConfig = new Properties();
		gameConfig.setProperty(Mover.KEY_TIMEOUT, "" + timeout);

		long now = System.currentTimeMillis();
		Result result;

		Move move = new Move();	
		move.setT(new Date(now + lastMoveDelta * Mover.TIME_SCALE));
		game.getPlayers().get(0).setMoves(Arrays.asList(move));

		result = rule.evaluate(game, null, gameConfig);
		assertEquals(expectedResult, result.shallMove());
	}
}
