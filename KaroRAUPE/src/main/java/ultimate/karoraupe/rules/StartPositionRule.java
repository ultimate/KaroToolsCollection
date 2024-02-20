package ultimate.karoraupe.rules;

import java.util.Properties;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Player;

public class StartPositionRule extends Rule
{
    public StartPositionRule(KaroAPI api)
    {
    	super(api);
        //this.supportedProperties.put(Mover.KEY_TRIGGER, EnumMoveTrigger.class);
    }

    @Override
    public Result evaluate(Game game, Player player, Properties gameConfig)
    {
        if(player.getMotion() == null)
        {
            return Result.dontMove("no start position selected yet");
        }
        return Result.noResult();
    }
}
