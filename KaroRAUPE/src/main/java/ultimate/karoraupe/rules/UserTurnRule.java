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
    public Boolean evaluate(Game game, Player player, Properties gameConfig)
    {
        if(player == null)
        {
            reason = "user not participating in this game";
            return false;
        }
        else if(game.getNext().getId() != player.getId())
        {
            reason = "wrong user's turn";
            return false;
        }
        return null;
    }
}
