package ultimate.karoraupe.rules;

import java.util.Properties;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Player;

public class AfterCrashRule extends Rule
{
    public AfterCrashRule()
    {
        //this.supportedProperties.put("key", class);
    }

    @Override
    public Boolean evaluate(Game game, Player player, Properties gameConfig)
    {
        if(player.getMotion() != null && player.getMotion().getXv() == 0 && player.getMotion().getYv() == 0)
        {
            reason = "restart after crash";
            return false;
        }
        return null;
    }
}
