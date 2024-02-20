package ultimate.karoraupe.rules;

import java.util.Properties;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.Mover;

public class RepeatRule extends Rule
{
	public static final String			KEY_SPECIAL_REPEAT					= Mover.KEY_PREFIX + ".repeat";
	public static final String			KEY_SPECIAL_REPEAT_MOVES			= KEY_SPECIAL_REPEAT + ".moves";
	public static final String			KEY_SPECIAL_REPEAT_MESSAGE			= KEY_SPECIAL_REPEAT + ".message";


    public RepeatRule(KaroAPI api)
    {
    	super(api);
        this.supportedProperties.put(KEY_SPECIAL_REPEAT, boolean.class);
        this.supportedProperties.put(KEY_SPECIAL_REPEAT_MOVES, int.class);
        this.supportedProperties.put(KEY_SPECIAL_REPEAT_MESSAGE, String.class);
    }

    @Override
    public Result evaluate(Game game, Player player, Properties gameConfig)
    {
        if(Boolean.valueOf(gameConfig.getProperty(KEY_SPECIAL_REPEAT)))
        {            
            int moves = Integer.parseInt(gameConfig.getProperty(KEY_SPECIAL_REPEAT_MOVES, "1"));
            if(moves <= 0)
            {                
                return Result.dontMove("Repeat move n-" + moves + " not possible: invalid value");
            }
            int index = player.getMoves().size() - moves;
            logger.debug("number of moves = " + player.getMoves().size() + ", repeat = " + moves + ", index = " + index + ", move = " + (index >= 0 && index < player.getMoves().size() ? player.getMoves().get(index) : null));
            if(index > 0 && !player.getMoves().get(index).isCrash())
            {
                Move move = player.getMoves().get(index);
                move.setX(player.getMotion().getX() + move.getXv()); // overwrite
                move.setY(player.getMotion().getY() + move.getYv()); // overwrite

                boolean moveIsPossible = false;
                for(Move possible: player.getPossibles())
                {
                    if(possible.equalsVec(move))
                    {
                        moveIsPossible = true;
                        break;
                    }
                }
                if(!moveIsPossible)
                {
                    return Result.dontMove("Repeat move n-" + moves + " not possible: move not in possibles");
                }

                move.setMsg(""); // clear
                if(gameConfig.getProperty(KEY_SPECIAL_REPEAT_MESSAGE) != null && !gameConfig.getProperty(KEY_SPECIAL_REPEAT_MESSAGE).isEmpty())
                    move.setMsg(gameConfig.getProperty(KEY_SPECIAL_REPEAT_MESSAGE));
                
                return Result.doMove("Repeat move n-" + moves + ": xv=" + move.getXv() + ", yv=" + move.getYv(), move);
            }
            else if(index > 0 && player.getMoves().get(index).isCrash())
            {
                return Result.dontMove("Repeat move n-" + moves + " not possible: was a crash");
            }
            else
            {
                return Result.dontMove("Repeat move n-" + moves + " not possible: not enough moves: " + player.getMoveCount());
            }
        }
        return Result.noResult();
    }
}
