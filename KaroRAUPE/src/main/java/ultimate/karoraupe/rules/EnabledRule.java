package ultimate.karoraupe.rules;

import java.util.Properties;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.Mover;
import ultimate.karoraupe.enums.EnumMoveTrigger;

public class EnabledRule extends Rule
{
    private boolean test;
    
    public EnabledRule(KaroAPI api)
    {
    	super(api);
        this.supportedProperties.put(Mover.KEY_TRIGGER, EnumMoveTrigger.class);
    }

    public boolean isTest()
    {
        return test;
    }

    public void setTest(boolean test)
    {
        this.test = test;
    }

    @Override
    public Result evaluate(Game game, Player player, Properties gameConfig)
    {
        EnumMoveTrigger trigger = EnumMoveTrigger.valueOf(gameConfig.getProperty(Mover.KEY_TRIGGER)).standardize();

        if(trigger == EnumMoveTrigger.never)
        {
            return Result.dontMove("KaroRAUPE not enabled for this game");
        }
        else if(trigger == EnumMoveTrigger.invalid)
        {
            return Result.dontMove("KaroRAUPE config invalid for this game");
        }
        else if(!test && trigger == EnumMoveTrigger.test)
        {
            return Result.dontMove("KaroRAUPE not enabled for this game (TEST)");
        }
        return Result.noResult();      
    }
}
