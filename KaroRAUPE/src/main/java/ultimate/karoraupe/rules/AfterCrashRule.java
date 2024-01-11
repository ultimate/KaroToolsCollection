package ultimate.karoraupe.rules;

import java.util.Properties;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.Mover;

public class AfterCrashRule extends Rule
{
	public static final String			KEY_FROMZERO				= Mover.KEY_PREFIX + ".fromzero";

    public AfterCrashRule()
    {
        this.supportedProperties.put(KEY_FROMZERO, boolean.class);
    }

    @Override
    public Result evaluate(Game game, Player player, Properties gameConfig)
    {
        if(player.getMotion() != null && player.getMotion().isCrash())
        {
            if(!Boolean.valueOf(gameConfig.getProperty(KEY_FROMZERO)))
                return Result.dontMove("start from zero");
            else
                return Result.noResult();
        }
        return Result.noResult();
    }
}
