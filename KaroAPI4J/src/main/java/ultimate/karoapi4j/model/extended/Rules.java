package ultimate.karoapi4j.model.extended;

import java.util.Random;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.official.Options;

// TODO
public class Rules implements Cloneable
{
	// TODO check what is necessary and what can be moved to settings
	private int					minZzz;
	private int					maxZzz;
	private EnumGameTC			crashallowed;
	private Boolean				cps;
	private EnumGameDirection	startdirection;
	@JsonInclude(value = Include.NON_DEFAULT)
	private int					gamesPerPlayer;
	@JsonInclude(value = Include.NON_DEFAULT)
	private int					numberOfPlayers;

	public Rules()
	{
	}

	public Rules(int minZzz, int maxZzz, EnumGameTC crashallowed, Boolean cps, EnumGameDirection startdirection)
	{
		this();
		this.minZzz = minZzz;
		this.maxZzz = maxZzz;
		this.crashallowed = crashallowed;
		this.cps = cps;
		this.startdirection = startdirection;
	}

	public Rules(int minZzz, int maxZzz, EnumGameTC crashallowed, Boolean cps, EnumGameDirection startdirection, int gamesPerPlayer, int numberOfPlayers)
	{
		this(minZzz, maxZzz, crashallowed, cps, startdirection);
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

	public EnumGameTC getCrashallowed()
	{
		return crashallowed;
	}

	public Boolean getCPs()
	{
		return cps;
	}

	public EnumGameDirection getStartdirection()
	{
		return startdirection;
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

	public void setCrashallowed(EnumGameTC crashallowed)
	{
		this.crashallowed = crashallowed;
	}

	public void setCPs(Boolean cps)
	{
		this.cps = cps;
	}

	public void setStartdirection(EnumGameDirection startdirection)
	{
		this.startdirection = startdirection;
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
		options.setCrashallowed(crashallowed == null && random != null ? EnumGameTC.getByValue(random.nextInt(EnumGameTC.values().length - 1)) : crashallowed);
		options.setStartdirection(startdirection == null && random != null ? EnumGameDirection.getByValue(random.nextInt(EnumGameDirection.values().length - 1)) : startdirection);

		return options;
	}

	@Override
	public Rules clone()
	{
		return new Rules(minZzz, maxZzz, crashallowed, cps, startdirection);
	}

	@Override
	public String toString()
	{
		//@formatter:off
		return "Regeln:\n" +
				" -> zzz          = [" + minZzz + "," + maxZzz + "]\n" + 
				" -> crashallowed = " + crashallowed + "\n" + 
				" -> cps          = " + cps + "\n" +
				" -> startdirection    = " + startdirection;
		//@formatter:on
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cps == null) ? 0 : cps.hashCode());
		result = prime * result + ((startdirection == null) ? 0 : startdirection.hashCode());
		result = prime * result + maxZzz;
		result = prime * result + minZzz;
		result = prime * result + ((crashallowed == null) ? 0 : crashallowed.hashCode());
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
		if(crashallowed == null)
		{
			if(other.crashallowed != null)
				return false;
		}
		else if(!crashallowed.equals(other.crashallowed))
			return false;
		if(startdirection == null)
		{
			if(other.startdirection != null)
				return false;
		}
		else if(!startdirection.equals(other.startdirection))
			return false;
		if(maxZzz != other.maxZzz)
			return false;
		if(minZzz != other.minZzz)
			return false;
		return true;
	}
}
