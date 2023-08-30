package ultimate.karoraupe.rules;

import java.util.Properties;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.Mover;
import ultimate.karoraupe.enums.EnumMoveTrigger;

public class EnabledRule extends Rule
{
    public EnabledRule()
    {
        this.supportedProperties.put(Mover.KEY_TRIGGER, EnumMoveTrigger.class);
    }

    @Override
    public Boolean evaluate(Game game, Player player, Properties gameConfig)
    {
        EnumMoveTrigger trigger = EnumMoveTrigger.valueOf(gameConfig.getProperty(Mover.KEY_TRIGGER)).standardize();

        if(trigger == EnumMoveTrigger.never || trigger == EnumMoveTrigger.invalid)
        {
            this.reason = "KaroRAUPE not enabled for this game";
            return false;
        }
        return null;        
    }
}
