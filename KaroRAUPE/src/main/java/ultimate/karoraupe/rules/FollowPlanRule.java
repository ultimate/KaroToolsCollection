package ultimate.karoraupe.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.Mover;

public class FollowPlanRule extends Rule
{

    public FollowPlanRule(KaroAPI api)
    {
    	super(api);
        this.supportedProperties.put(Mover.KEY_MESSAGE, String.class);
        this.supportedProperties.put(Mover.KEY_STRICT, boolean.class);
    }

    @Override
    public Result evaluate(Game game, Player player, Properties gameConfig)
    {
        List<PlannedMoveWithPredecessor> plannedPossibles = findPlannedPossibles(player.getMotion(), player.getPossibles(), game.getPlannedMoves());
					
        // create separate list with only planned moves which have the current move as a predessor
        List<PlannedMoveWithPredecessor> plannedPossibles_strict = new ArrayList<>(plannedPossibles);
        plannedPossibles_strict.removeIf(pmwp -> {return !pmwp.strict;});
        
        logger.debug("  GID = " + game.getId() + " --> " + plannedPossibles);

        Move move;
        String reason;

        boolean strict = Boolean.valueOf(gameConfig.getProperty(Mover.KEY_STRICT));
        if(strict)
        {					
            if(plannedPossibles_strict.size() == 0)
            {
                return Result.noResult("possibles = " + player.getPossibles().size() + ", matches all = " + plannedPossibles.size() + ", strict = " + plannedPossibles_strict.size() + ", strict = " + strict + " --> nothing to choose from");
            }
            else if(plannedPossibles_strict.size() > 1)
            {
                return Result.noResult("possibles = " + player.getPossibles().size() + ", matches all = " + plannedPossibles.size() + ", strict = " + plannedPossibles_strict.size() + ", strict = " + strict + " --> can't decide");
            }
            else
            {	
                reason = "Planned-Strict";				
                move = plannedPossibles_strict.get(0).plannedMove;
            }
        }
        else
        {
            if(plannedPossibles.size() == 0)
            {
                return Result.noResult("possibles = " + player.getPossibles().size() + ", matches all = " + plannedPossibles.size() + ", strict = " + plannedPossibles_strict.size() + ", strict = " + strict + " --> nothing to choose from");
            }
            else if(plannedPossibles.size() > 1)
            {
                if(plannedPossibles_strict.size() != 1)
                {
                    return Result.noResult("possibles = " + player.getPossibles().size() + ", matches all = " + plannedPossibles.size() + ", strict = " + plannedPossibles_strict.size() + ", strict = " + strict + " --> can't decide");
                }
                else
                {
                    // prefer the only strict possibility
                    reason = "Planned-Strict";
                    move = plannedPossibles_strict.get(0).plannedMove;
                }
            }
            else 
            {
                reason = "Planned";
                move = plannedPossibles.get(0).plannedMove;
            }
        }

        if(gameConfig.getProperty(Mover.KEY_MESSAGE) != null && !gameConfig.getProperty(Mover.KEY_MESSAGE).isEmpty())
            move.setMsg(gameConfig.getProperty(Mover.KEY_MESSAGE));

        return Result.doMove(reason, move);
    }

	public static class PlannedMoveWithPredecessor
	{
		Move plannedMove;
		Move predecessor;
		boolean strict;

		public PlannedMoveWithPredecessor(Move plannedMove, Move predecessor, boolean strict)
		{
			this.plannedMove = plannedMove;
			this.predecessor = predecessor;
			this.strict = strict;
		}

		public String toString()
		{
			return (predecessor == null ? null : predecessor.toString()) + "->" + plannedMove.toString() + " (strict=" + strict + ")";
		}
	}

	/**
	 * Match the possibles against the planned moves.<br>
	 * A move will only be selected, if the current move is in the planned moves.
	 * 
	 * @param possibles
	 * @param plannedMoves
	 * @return
	 */
	public static List<PlannedMoveWithPredecessor> findPlannedPossibles(Move currentMove, List<Move> possibles, List<Move> plannedMoves)
	{
		List<PlannedMoveWithPredecessor> matches = new ArrayList<>();

		if(plannedMoves != null)
		{
			for(int i = 0; i < plannedMoves.size(); i++)
			{	
				Move predecessor = (i == 0 ? null : plannedMoves.get(i-1));
				Move plannedMove = plannedMoves.get(i);		
				// look if the planned moves contain the current move
				for(Move possible : possibles)
				{
                    if(plannedMove.equalsVec(possible))
					{
						boolean strict = predecessor == null || currentMove.equalsVec(predecessor);
						matches.add(new PlannedMoveWithPredecessor(plannedMove, predecessor, strict));
						break;
					}
				}
			}
		}
		return matches;
	}
}
