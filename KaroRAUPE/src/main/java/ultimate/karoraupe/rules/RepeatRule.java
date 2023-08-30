package ultimate.karoraupe.rules;

import java.util.Properties;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.Mover;

public class RepeatRule extends Rule
{
	public static final String			KEY_SPECIAL_REPEAT					= Mover.KEY_PREFIX + ".repeat";
	public static final String			KEY_SPECIAL_REPEAT_MOVES			= KEY_SPECIAL_REPEAT + ".moves";
	public static final String			KEY_SPECIAL_REPEAT_MESSAGE			= KEY_SPECIAL_REPEAT + ".message";


    public RepeatRule()
    {
        this.supportedProperties.put(KEY_SPECIAL_REPEAT, boolean.class);
        this.supportedProperties.put(KEY_SPECIAL_REPEAT_MOVES, int.class);
        this.supportedProperties.put(KEY_SPECIAL_REPEAT_MESSAGE, String.class);
    }

    @Override
    public Boolean evaluate(Game game, Player player, Properties gameConfig)
    {
        if(Boolean.valueOf(gameConfig.getProperty(KEY_SPECIAL_REPEAT)))
        {            
            int moves = Integer.parseInt(gameConfig.getProperty(KEY_SPECIAL_REPEAT_MOVES, "1"));
            int index = player.getMoves().size() - moves;
            if(index > 0 && !player.getMoves().get(index).isCrash())
            {
                move = player.getMoves().get(index);
                move.setX(player.getMotion().getX() + move.getXv()); // overwrite
                move.setY(player.getMotion().getY() + move.getYv()); // overwrite
                move.setMsg(""); // clear
                reason = "Repeat move n-" + moves + ": xv=" + move.getXv() + ", yv=" + move.getYv();
                if(gameConfig.getProperty(KEY_SPECIAL_REPEAT_MESSAGE) != null && !gameConfig.getProperty(KEY_SPECIAL_REPEAT_MESSAGE).isEmpty())
                    move.setMsg(gameConfig.getProperty(KEY_SPECIAL_REPEAT_MESSAGE));
            }
            else if(index > 0 && player.getMoves().get(index).isCrash())
            {
                reason = "Repeat move n-" + moves + " not possible: was a crash";
                return false;
            }
            else
            {
                reason = "Repeat move n-" + moves + " not possible: not enough moves: " + player.getMoveCount();
                return false;
            }
        }
        return null;
    }
}
