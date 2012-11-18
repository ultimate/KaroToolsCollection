package muskel2.model.series;

import java.util.HashMap;

import muskel2.model.GameSeries;
import muskel2.model.Map;
import muskel2.model.Player;
import muskel2.model.Rules;

public class BalancedGameSeries extends GameSeries
{
	private static final long		serialVersionUID	= 1L;

	public static int				MAX_GAMES_PER_PLAYER	= 5;
	public static int				MAX_MAPS				= 5;

	private int						numberOfMaps;

	private HashMap<Integer, Map>	mapList;
	private HashMap<Integer, Rules>	rulesList;

	protected Player[][]			shuffledPlayers;

	public BalancedGameSeries()
	{
		super("gameseries.balanced.titlepatterns");
	}

	@Override
	public int getMinSupportedPlayersPerMap()
	{
		return 0;
	}

	public int getNumberOfMaps()
	{
		return numberOfMaps;
	}

	public void setNumberOfMaps(int numberOfMaps)
	{
		this.numberOfMaps = numberOfMaps;
	}

	public void setMap(int i, Map map, Rules rules)
	{
		this.mapList.put(i, map);
		this.rulesList.put(i, rules);
	}

	public Map getMap(int i)
	{
		try
		{
			return mapList.get(i);
		}
		catch(IndexOutOfBoundsException e)
		{
			return null;
		}
	}

	public Rules getRules(int i)
	{
		try
		{
			return rulesList.get(i);
		}
		catch(IndexOutOfBoundsException e)
		{
			return null;
		}
	}
}
