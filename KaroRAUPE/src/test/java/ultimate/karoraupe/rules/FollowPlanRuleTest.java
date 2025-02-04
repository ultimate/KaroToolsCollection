package ultimate.karoraupe.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.ArrayList;
import java.util.List;
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
import ultimate.karoraupe.rules.FollowPlanRule.PlannedMoveWithPredecessor;
import ultimate.karoraupe.rules.Rule.Result;
import ultimate.karoraupe.test.KaroRAUPETestcase;

public class FollowPlanRuleTest extends KaroRAUPETestcase
{	
	private FollowPlanRule rule = new FollowPlanRule(karoAPI);

	private static void move(List<Move> moves, int xv, int yv)
	{
		Move lastMove = moves.get(moves.size() - 1);

		// check xv / yv for correctness
		if(Math.abs(lastMove.getXv() - xv) > 2)
			throw new IllegalArgumentException("xv=" + xv + " not possible from lastMove xv=" + lastMove.getXv());
		if(Math.abs(lastMove.getYv() - yv) > 2)
			throw new IllegalArgumentException("yv=" + yv + " not possible from lastMove yv=" + lastMove.getYv());

		int x = lastMove.getX() + xv;
		int y = lastMove.getY() + yv;
		Move newMove = new Move(x, y, xv, yv, null);
		moves.add(newMove);
	}

	private static List<Move> createPlannedMovesList()
	{
		List<Move> plannedMoves = new ArrayList<>();

		plannedMoves.add(new Move(10,10, null)); // start
		move(plannedMoves, 1,0); // 1
		move(plannedMoves, 2,0); // 2
		move(plannedMoves, 2,0); // 3
		move(plannedMoves, 2,0); // 4
		move(plannedMoves, 2,1); // 5
		move(plannedMoves, 1,2); // 6
		move(plannedMoves, 0,2); // 7
		move(plannedMoves, -1,2); // 8
		move(plannedMoves, -2,1); // 9
		move(plannedMoves, -2,0); // 10
		move(plannedMoves, -2,-1); // 11
		move(plannedMoves, -1,-2); // 12
		move(plannedMoves, 0,-2); // 13
		move(plannedMoves, 1,-2); // 14
		move(plannedMoves, 2,-1); // 15
		move(plannedMoves, 2,0); // 16 - should be STRICT the same as move 4		
		move(plannedMoves, 1, 0); // 17	
		move(plannedMoves, 1, 1); // 18 - should end up at the same point as move 5
		move(plannedMoves, 0, 2); // 19
		move(plannedMoves, 0, 3); // 20

		return plannedMoves;
	}

	private static List<Move> calcPossibles(Move current)
	{		
		List<Move> possibles = new ArrayList<>(9);
		int x, y, xv, yv;
		for(int dx = -1; dx <= 1; dx++)
		{
			for(int dy = -1; dy <= 1; dy++)
			{
				xv = current.getXv() + dx;
				yv = current.getYv() + dy;
				x = current.getX() + xv;
				y = current.getY() + yv;
				possibles.add(new Move(x, y, xv, yv, null));
			}
		}
		return possibles;
	}

	public static Stream<Arguments> provideMotionAndResult()
	{
		List<Move> plannedMoves = createPlannedMovesList();
		Move moveSomewhereElse = new Move(100, 100, 1, 1, null);
		Move moveNotStrict = new Move(15,10, 1, 0, null);

		//@formatter:off
	    return Stream.of(
			// strict, plannedPossibles_strict=0
			arguments(moveSomewhereElse, plannedMoves, true, null, "nothing to choose from"),
			// strict, plannedPossibles_strict=1
			arguments(plannedMoves.get(3), plannedMoves, true, true, "Planned-Strict"),
			arguments(plannedMoves.get(5), plannedMoves, true, true, "Planned-Strict"),
			arguments(plannedMoves.get(18), plannedMoves, true, true, "Planned-Strict"),
			// strict, plannedPossibles_strict>1
			arguments(plannedMoves.get(4), plannedMoves, true, null, "can't decide"),

			// not strict, plannedPossibles=0
			arguments(moveSomewhereElse, plannedMoves, false, null, "nothing to choose from"),
			// not strict, plannedPossibles=1
			arguments(plannedMoves.get(5), plannedMoves, false, true, "Planned"),
			// not strict, plannedPossibles>1 && plannedPossibles_strict=0
			arguments(moveNotStrict, plannedMoves, false, null, "can't decide"),
			// not strict, plannedPossibles>1 && plannedPossibles_strict=1
			arguments(plannedMoves.get(18), plannedMoves, false, true, "Planned-Strict"),
			// not strict, plannedPossibles>1 && plannedPossibles_strict>1
			arguments(plannedMoves.get(4), plannedMoves, false, null, "can't decide")
	    );
	    //@formatter:on
	}

	@ParameterizedTest
	@MethodSource("provideMotionAndResult")
	public void test_evaluate(Move motion, List<Move> plannedMoves, boolean strict, Boolean expectedResult, String expectedReason)
	{
		Game game = new Game();
		game.setPlannedMoves(plannedMoves);

		Player player = new Player();
		player.setMotion(motion);
		player.setPossibles(calcPossibles(motion));

		Properties gameConfig = new Properties();
		gameConfig.setProperty(Mover.KEY_STRICT, "" + strict);
		
		logger.info("checking plan for move " + motion);

		Result result = rule.evaluate(game, player, gameConfig);

		// this is for debugging only
		List<Move> possibles = calcPossibles(motion);			
		logger.info("  possibles               = " + possibles.size());
        List<PlannedMoveWithPredecessor> plannedPossibles = FollowPlanRule.findPlannedPossibles(player.getMotion(), player.getPossibles(), game.getPlannedMoves());	
		logger.info("  plannedPossibles        = " + plannedPossibles.size());					
        List<PlannedMoveWithPredecessor> plannedPossibles_strict = new ArrayList<>(plannedPossibles);
        plannedPossibles_strict.removeIf(pmwp -> {return !pmwp.strict;});
		logger.info("  plannedPossibles_strict = " + plannedPossibles_strict.size());
		
		// actual checks
		logger.info("  result.shallMove        = " + result.shallMove());
		assertEquals(expectedResult, result.shallMove());
		logger.info("  result.reason           = " + result.getReason());
		assertTrue(expectedReason == null || result.getReason().endsWith(expectedReason));
	}

	@Test
	public void test_findPlannedPossibles()
	{
		List<Move> plannedMoves = createPlannedMovesList();

		Move currentMove;
		List<Move> possibles;
		List<PlannedMoveWithPredecessor> plannedPossibles;

		for(int i = 1; i < plannedMoves.size(); i++)
		{
			logger.info("checking plan for move #" + i);

			currentMove = plannedMoves.get(i);
			logger.info("  current          = " + currentMove);

			possibles = calcPossibles(currentMove);			
			logger.info("  possibles        = " + possibles);
			
			plannedPossibles = FollowPlanRule.findPlannedPossibles(currentMove, possibles, plannedMoves);
			logger.info("  plannedPossibles = " + plannedPossibles);

			assertNotNull(plannedPossibles);
			
			if(i == plannedMoves.size() - 1)
			{
				// end of list
				assertEquals(0, plannedPossibles.size());
			}
			else if(i == 3 || i == 15)
			{
				// lines overlap each other with the same possibles
				assertEquals(2, plannedPossibles.size());
				
				PlannedMoveWithPredecessor plannedMove1 = plannedPossibles.get(0);
				PlannedMoveWithPredecessor plannedMove2 = plannedPossibles.get(1);
				
				assertFalse(plannedMove1.predecessor.equalsVec(plannedMove2.predecessor));
				assertTrue(plannedMove1.predecessor.equals(currentMove) || plannedMove2.predecessor.equals(currentMove));
				
				assertTrue(plannedMove1.plannedMove.equalsVec(plannedMove2.plannedMove));
				assertEquals(plannedMoves.get(4), plannedMove1.plannedMove);
				assertEquals(plannedMoves.get(16), plannedMove1.plannedMove);
			}
			else if(i == 4 || i == 16)
			{
				// lines overlap each other with the same possibles
				assertEquals(2, plannedPossibles.size());
				
				PlannedMoveWithPredecessor plannedMove1 = plannedPossibles.get(0);
				PlannedMoveWithPredecessor plannedMove2 = plannedPossibles.get(1);
				
				assertTrue(plannedMove1.predecessor.equalsVec(currentMove));
				assertTrue(plannedMove2.predecessor.equalsVec(currentMove));
				
				assertFalse(plannedMove1.plannedMove.equalsVec(plannedMove2.plannedMove));
				assertEquals(plannedMoves.get(5), plannedMove1.plannedMove);
				assertEquals(plannedMoves.get(17), plannedMove2.plannedMove);
			}
			else if(i == 5 || i == 18)
			{
				// lines are touching each other only shared possibles in 
				if(i == 5) // from move 5, only 1 move is possible
				{
					assertEquals(1, plannedPossibles.size());

					PlannedMoveWithPredecessor plannedMove1 = plannedPossibles.get(0);

					assertEquals(plannedMoves.get(5), plannedMove1.predecessor);

					assertEquals(plannedMoves.get(6), plannedMove1.plannedMove);
				}
				else if(i == 18) // from move 8, 2 moves are possible
				{
					assertEquals(2, plannedPossibles.size());

					PlannedMoveWithPredecessor plannedMove1 = plannedPossibles.get(0);
					PlannedMoveWithPredecessor plannedMove2 = plannedPossibles.get(1);

					assertEquals(plannedMoves.get(5), plannedMove1.predecessor);
					assertEquals(plannedMoves.get(18), plannedMove2.predecessor);

					assertEquals(plannedMoves.get(6), plannedMove1.plannedMove);
					assertEquals(plannedMoves.get(19), plannedMove2.plannedMove);
				}
			}
			else
			{
				assertEquals(1, plannedPossibles.size());
				// check predecessor
				PlannedMoveWithPredecessor plannedMove = plannedPossibles.get(0);
				assertTrue(plannedMove.predecessor.equalsVec(currentMove));
				assertTrue(plannedMove.plannedMove.equalsVec(plannedMoves.get(i+1)));
			}
		}
	}
}
