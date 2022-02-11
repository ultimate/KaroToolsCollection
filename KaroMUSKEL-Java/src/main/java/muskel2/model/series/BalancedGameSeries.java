package muskel2.model.series;

import java.util.HashMap;

import muskel2.model.GameSeries;
import muskel2.model.Map;
import muskel2.model.Player;
import muskel2.model.Rules;

@Deprecated
public class BalancedGameSeries extends GameSeries
{
	private static final long		serialVersionUID		= 1L;

	public static int				MAX_GAMES_PER_PLAYER	= 5;
	public static int				MAX_MAPS				= 5;

	public int						numberOfMaps;

	public HashMap<Integer, Map>	mapList;
	public HashMap<Integer, Rules>	rulesList;

	public Player[][]				shuffledPlayers;
}
