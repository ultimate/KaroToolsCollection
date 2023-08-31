package ultimate.karoraupe.rules;

import java.util.Properties;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Player;

public class UserTurnRule extends Rule
{
    public UserTurnRule()
    {
        //this.supportedProperties.put(Mover.KEY_TRIGGER, EnumMoveTrigger.class);
    }

    @Override
    public Result evaluate(Game game, Player player, Properties gameConfig)
    {
        if(player == null)
        {
            return Result.dontMove("user not participating in this game");
        }
        else if(game.getNext().getId().intValue() != player.getId().intValue())
        {
            return Result.dontMove("wrong user's turn");
        }
        return Result.noResult();
    }
}
