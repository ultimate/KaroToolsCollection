package ultimate.karoapi4j.model.extended;

import java.util.Random;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.official.Options;

// TODO
public class Rules implements Cloneable
{
	// TODO check what is necessary and what can be moved to settings
	private int					minZzz;
	private int					maxZzz;
	private EnumGameTC			tc;
	private Boolean				cps;
	private EnumGameDirection	direction;
	private int					gamesPerPlayer;
	private int					numberOfPlayers;

	public Rules()
	{
	}

	public Rules(int minZzz, int maxZzz, EnumGameTC tc, Boolean cps, EnumGameDirection direction)
	{
		this();
		this.minZzz = minZzz;
		this.maxZzz = maxZzz;
		this.tc = tc;
		this.cps = cps;
		this.direction = direction;
	}

	public Rules(int minZzz, int maxZzz, EnumGameTC tc, Boolean cps, EnumGameDirection direction, int gamesPerPlayer, int numberOfPlayers)
	{
		this(minZzz, maxZzz, tc, cps, direction);
		this.gamesPerPlayer = gamesPerPlayer;
		this.numberOfPlayers = numberOfPlayers;
	}

	public int getMinZzz()
	{
		return minZzz;
	}

	public int getMaxZzz()
	{
		return maxZzz;
	}

	public EnumGameTC getTC()
	{
		return tc;
	}

	public Boolean getCPs()
	{
		return cps;
	}

	public EnumGameDirection getDirection()
	{
		return direction;
	}

	public int getGamesPerPlayer()
	{
		return gamesPerPlayer;
	}

	public int getNumberOfPlayers()
	{
		return numberOfPlayers;
	}

	public void setMinZzz(int minZzz)
	{
		this.minZzz = minZzz;
	}

	public void setMaxZzz(int maxZzz)
	{
		this.maxZzz = maxZzz;
	}

	public void setTC(EnumGameTC tc)
	{
		this.tc = tc;
	}

	public void setCPs(Boolean cps)
	{
		this.cps = cps;
	}

	public void setDirection(EnumGameDirection direction)
	{
		this.direction = direction;
	}

	public void setGamesPerPlayer(int gamesPerPlayer)
	{
		this.gamesPerPlayer = gamesPerPlayer;
	}

	public void setNumberOfPlayers(int numberOfPlayers)
	{
		this.numberOfPlayers = numberOfPlayers;
	}

	public Options createOptions(Random random)
	{
		Options options = new Options();

		options.setZzz(random != null ? random.nextInt(maxZzz - minZzz + 1) + minZzz : minZzz);
		options.setCps(cps == null && random != null ? random.nextBoolean() : cps);
		options.setCrashallowed(tc == null  && random != null ? EnumGameTC.getByValue(random.nextInt(EnumGameTC.values().length - 1)) : tc);
		options.setStartdirection(direction == null  && random != null ? EnumGameDirection.getByValue(random.nextInt(EnumGameDirection.values().length - 1)) : direction);

		return options;
	}

	@Override
	public Rules clone()
	{
		return new Rules(minZzz, maxZzz, tc, cps, direction);
	}

	@Override
	public String toString()
	{
		//@formatter:off
		return "Regeln:\n" +
				" -> zzz                  = [" + minZzz + "," + maxZzz + "]\n" + 
				" -> tc                   = " + tc + "\n" + 
				" -> cps                  = " + cps + "\n" +
				" -> direction            = " + direction;
		//@formatter:on
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cps == null) ? 0 : cps.hashCode());
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + maxZzz;
		result = prime * result + minZzz;
		result = prime * result + ((tc == null) ? 0 : tc.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		Rules other = (Rules) obj;
		if(cps == null)
		{
			if(other.cps != null)
				return false;
		}
		else if(!cps.equals(other.cps))
			return false;
		if(tc == null)
		{
			if(other.tc != null)
				return false;
		}
		else if(!tc.equals(other.tc))
			return false;
		if(direction == null)
		{
			if(other.direction != null)
				return false;
		}
		else if(!direction.equals(other.direction))
			return false;
		if(maxZzz != other.maxZzz)
			return false;
		if(minZzz != other.minZzz)
			return false;
		return true;
	}
}
