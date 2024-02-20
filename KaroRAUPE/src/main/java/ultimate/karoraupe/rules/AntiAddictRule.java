package ultimate.karoraupe.rules;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.extended.AddictInfo;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoraupe.Mover;

public class AntiAddictRule extends Rule
{
	public static final String										KEY_SPECIAL_ANTIADDICT				= Mover.KEY_PREFIX + ".antiaddict";
	public static final String										KEY_SPECIAL_ANTIADDICT_MAXWOLLUST	= KEY_SPECIAL_ANTIADDICT + ".maxwollust";
	public static final String										KEY_SPECIAL_ANTIADDICT_MAXMOVES		= KEY_SPECIAL_ANTIADDICT + ".maxmoves";

	/**
	 * Addicts loaded
	 */
	private CompletableFuture<java.util.Map<String, AddictInfo>>	addicts;
	private boolean errorLogged = false;

	public AntiAddictRule(KaroAPI api)
	{
		super(api);
		this.supportedProperties.put(KEY_SPECIAL_ANTIADDICT, boolean.class);
		this.supportedProperties.put(KEY_SPECIAL_ANTIADDICT_MAXWOLLUST, int.class);
		this.supportedProperties.put(KEY_SPECIAL_ANTIADDICT_MAXMOVES, int.class);

		this.addicts = api.getAddicts();
	}

	@Override
	public Result evaluate(Game game, Player player, Properties gameConfig)
	{
		if(Boolean.valueOf(gameConfig.getProperty(KEY_SPECIAL_ANTIADDICT)))
		{
			AddictInfo ai;
			try
			{
				ai = this.addicts.get().get(player.getName());

				int maxWollust = Integer.parseInt(gameConfig.getProperty(KEY_SPECIAL_ANTIADDICT_MAXWOLLUST, "-1"));
				if(maxWollust > 0 && ai.getWollust() > maxWollust)
					return Result.dontMove("Max WOLLUST of " + maxWollust + " exceeded: " + ai.getWollust());

				int maxMoves = Integer.parseInt(gameConfig.getProperty(KEY_SPECIAL_ANTIADDICT_MAXMOVES, "-1"));
				if(maxMoves > 0 && ai.getMovesTotal() > maxMoves)
					return Result.dontMove("Max moves of " + maxMoves + " exceeded: " + ai.getMovesTotal());
			}
			catch(InterruptedException | ExecutionException e)
			{
				if(!this.errorLogged)
				{
					logger.error("could not load addict info", e);
					this.errorLogged = true;
				}
			}
		}
		return Result.noResult();
	}
}
