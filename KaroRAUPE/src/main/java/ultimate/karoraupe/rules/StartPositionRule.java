package ultimate.karoraupe.rules;

import java.util.Properties;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.Mover;

public class StartPositionRule extends Rule
{
	public static final String KEY_STARTCHECK = Mover.KEY_PREFIX + ".startcheck";

	public StartPositionRule(KaroAPI api)
	{
		super(api);
		this.supportedProperties.put(KEY_STARTCHECK, boolean.class);
	}

	@Override
	public Result evaluate(Game game, Player player, Properties gameConfig)
	{
		if(Boolean.valueOf(gameConfig.getProperty(KEY_STARTCHECK)) && player.getMotion() == null)
		{
			return Result.dontMove("no start position selected yet");
		}
		return Result.noResult();
	}
}
