package ultimate.karoraupe.rules;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoapi4j.utils.ReflectionsUtil;

public class NoPossiblesRule extends Rule
{
	public NoPossiblesRule(KaroAPI api)
	{
		super(api);
		// this.supportedProperties.put(Mover.KEY_TRIGGER, EnumMoveTrigger.class);
	}

	@Override
	public Result evaluate(Game game, Player player, Properties gameConfig)
	{
		if(player.getPossibles() == null || player.getPossibles().size() == 0)
		{
			try
			{
				logger.debug("  GID = " + game.getId() + " --> possibles = 0 --> crashing");
				
				// process crash
				boolean refreshed = api.refreshAfterCrash(game.getId()).get();

				if(!refreshed)
					return Result.dontMove("possibles = 0 and refresh failed");

				// re-load game details
				logger.debug("  GID = " + game.getId() + " --> loading game details (again after crash)");
				Game refreshedGame = api.getGameWithDetails(game.getId()).get();

				// to avoid using the KaroAPICache (which would be overhead) we manually update the
				// game in the same way it would be done in the KaroAPICache
				ReflectionsUtil.copyFields(refreshedGame, game, false);

				if(player.getPossibles() == null || player.getPossibles().size() == 0)
					return Result.dontMove("possibles = 0 and refresh didn't change it");
			}
			catch(ExecutionException | InterruptedException e)
			{
				return Result.dontMove("possibles = 0 and refresh failed");
			}
		}
		return Result.noResult();
	}
}
