package ultimate.karopapier.eval.impl;

import java.io.File;
import java.util.Map.Entry;
import java.util.Properties;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.PlaceToRace;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karopapier.eval.CCCEvalNew;
import ultimate.karopapier.utils.WikiUtil;

public class CCC7Eval extends CCCEvalNew
{
	public CCC7Eval()
	{
		super(7);
	}

	@Override
	public void prepare(KaroAPICache karoAPICache, GameSeries gameSeries, Properties properties, File folder, int execution)
	{
		// overwrite map number for better evaluation
		gameSeries.set(GameSeries.NUMBER_OF_MAPS, 25);
		for(Entry<String, Rules> rules : gameSeries.getRulesByKey().entrySet()) {
			rules.getValue().setGamesPerPlayer(6);
		}
		super.prepare(karoAPICache, gameSeries, properties, folder, execution);
	}

	protected Rules getRules(int challenge)
	{
		return this.data.getRulesByKey().get("" + (challenge / 5));
	}

	protected PlaceToRace getMap(int challenge)
	{
		return this.data.getMapsByKey().get("" + (challenge / 5)).get(0);
	}
	
	protected String gameToLink(int challenge, int game)
	{
		return WikiUtil.createLink(getGame(challenge, game), (challenge + 1) + "." + ((game + 1) / 5 + 1) + "." + ((game + 1) % 5) );
	}
}
