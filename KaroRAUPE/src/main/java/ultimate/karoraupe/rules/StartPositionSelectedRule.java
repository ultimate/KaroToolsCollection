package ultimate.karoraupe.rules;

import java.util.Properties;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Player;

public class StartPositionSelectedRule extends Rule
{
    public StartPositionSelectedRule()
    {
        //this.supportedProperties.put(Mover.KEY_TRIGGER, EnumMoveTrigger.class);
    }

    @Override
    public Boolean evaluate(Game game, Player player, Properties gameConfig)
    {
        if(player.getMotion() == null)
        {
            reason = "no start position selected yet";
            return false;
        }
        return null;
    }
}
