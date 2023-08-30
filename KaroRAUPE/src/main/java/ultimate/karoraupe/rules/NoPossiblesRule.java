package ultimate.karoraupe.rules;

import java.util.Properties;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Player;

public class NoPossiblesRule extends Rule
{
    public NoPossiblesRule()
    {
        //this.supportedProperties.put(Mover.KEY_TRIGGER, EnumMoveTrigger.class);
    }

    @Override
    public Boolean evaluate(Game game, Player player, Properties gameConfig)
    {
        if(player.getPossibles() == null || player.getPossibles().size() == 0)
        {
            reason = "possibles = 0 --> can't move";
            return false;
        }
        return null;
    }
}
