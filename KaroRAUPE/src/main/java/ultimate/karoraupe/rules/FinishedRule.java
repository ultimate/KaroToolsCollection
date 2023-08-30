package ultimate.karoraupe.rules;

import java.util.Properties;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Player;

public class FinishedRule extends Rule
{
    public FinishedRule()
    {
        //this.supportedProperties.put(Mover.KEY_TRIGGER, EnumMoveTrigger.class);
    }

    @Override
    public Boolean evaluate(Game game, Player player, Properties gameConfig)
    {
        if(game.isFinished())
        {
            reason = "game is finished";
            return false;
        }
        return null;
    }
}
