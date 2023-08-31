package ultimate.karoraupe.rules;

import java.util.Properties;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.Mover;

public class SingleOptionRule extends Rule
{
	public static final String			KEY_SPECIAL_SINGLEOPTION			= Mover.KEY_PREFIX + ".singleoption";
	public static final String			KEY_SPECIAL_SINGLEOPTION_MESSAGE	= KEY_SPECIAL_SINGLEOPTION + ".message";

    public SingleOptionRule()
    {
        this.supportedProperties.put(KEY_SPECIAL_SINGLEOPTION, boolean.class);
        this.supportedProperties.put(KEY_SPECIAL_SINGLEOPTION_MESSAGE, String.class);
    }

    @Override
    public Result evaluate(Game game, Player player, Properties gameConfig)
    {
        if(player.getPossibles().size() == 1 && Boolean.valueOf(gameConfig.getProperty(KEY_SPECIAL_SINGLEOPTION)))
        {
            Move move = player.getPossibles().get(0);
            if(gameConfig.getProperty(KEY_SPECIAL_SINGLEOPTION_MESSAGE) != null && !gameConfig.getProperty(KEY_SPECIAL_SINGLEOPTION_MESSAGE).isEmpty())
                move.setMsg(gameConfig.getProperty(KEY_SPECIAL_SINGLEOPTION_MESSAGE));
            return Result.doMove("Single-Option", move);
        }
        return Result.noResult();
    }
}
