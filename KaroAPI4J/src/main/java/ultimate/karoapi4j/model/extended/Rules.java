package ultimate.karoapi4j.model.extended;

import java.io.Serializable;
import java.util.Random;

import ultimate.karoapi4j.enums.EnumDirection;
import ultimate.karoapi4j.enums.EnumTC;

public class Rules implements Cloneable, Serializable
{
	private static final long	serialVersionUID	= 1L;

	private int					minZzz;
	private int					maxZzz;
	private Integer				zzz;
	private EnumTC				tc;
	private Boolean				cps;
	private EnumDirection		direction;
	private boolean				creatorGiveUp;
	private boolean				ignoreInvitable;
	private int					gamesPerPlayer;
	private int					numberOfPlayers;

	private Random				random;

	public Rules(int minZzz, int maxZzz, EnumTC tc, Boolean cps, EnumDirection direction, boolean creatorGiveUp, boolean ignoreInvitable)
	{
		super();
		this.minZzz = minZzz;
		this.maxZzz = maxZzz;
		this.zzz = null;
		this.tc = tc;
		this.cps = cps;
		this.direction = direction;
		this.creatorGiveUp = creatorGiveUp;
		this.ignoreInvitable = ignoreInvitable;
		this.random = new Random();
	}

	public Rules(int minZzz, int maxZzz, EnumTC tc, Boolean cps, EnumDirection direction, boolean creatorGiveUp, boolean ignoreInvitable,
			int gamesPerPlayer, int numberOfPlayers)
	{
		super();
		this.minZzz = minZzz;
		this.maxZzz = maxZzz;
		this.tc = tc;
		this.cps = cps;
		this.direction = direction;
		this.creatorGiveUp = creatorGiveUp;
		this.ignoreInvitable = ignoreInvitable;
		this.gamesPerPlayer = gamesPerPlayer;
		this.numberOfPlayers = numberOfPlayers;
		this.random = new Random();
	}

	public int getZzz()
	{
		return zzz;
	}

	public int getMinZzz()
	{
		return minZzz;
	}

	public int getMaxZzz()
	{
		return maxZzz;
	}

	public EnumTC getTC()
	{
		return tc;
	}

	public Boolean getCPs()
	{
		return cps;
	}

	public EnumDirection getDirection()
	{
		return direction;
	}

	public boolean isCreatorGiveUp()
	{
		return creatorGiveUp;
	}

	public boolean isIgnoreInvitable()
	{
		return ignoreInvitable;
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

	public void setZzz(int zzz)
	{
		this.minZzz = zzz;
		this.maxZzz = zzz;
	}

	public void setTC(EnumTC tc)
	{
		this.tc = tc;
	}

	public void setCPs(Boolean cps)
	{
		this.cps = cps;
	}

	public void setDirection(EnumDirection direction)
	{
		this.direction = direction;
	}

	public void setCreatorGiveUp(boolean creatorGiveUp)
	{
		this.creatorGiveUp = creatorGiveUp;
	}

	public void setIgnoreInvitable(boolean ignoreInvitable)
	{
		this.ignoreInvitable = ignoreInvitable;
	}

	public void setGamesPerPlayer(int gamesPerPlayer)
	{
		this.gamesPerPlayer = gamesPerPlayer;
	}

	public void setNumberOfPlayers(int numberOfPlayers)
	{
		this.numberOfPlayers = numberOfPlayers;
	}

	public Rules createRandomValues()
	{
		if(zzz == null)
			zzz = random.nextInt(maxZzz - minZzz + 1) + minZzz;
		if(tc == null || tc == EnumTC.random)
			tc = EnumTC.getByValue(random.nextInt(EnumTC.values().length - 1));
		if(cps == null)
			cps = random.nextBoolean();
		if(direction == null || direction == EnumDirection.random)
			direction = EnumDirection.getByValue(random.nextInt(EnumDirection.values().length - 1));
		return this;
	}

	@Override
	public Rules clone()
	{
		return new Rules(minZzz, maxZzz, tc, cps, direction, creatorGiveUp, ignoreInvitable);
	}

	@Override
	public String toString()
	{
		return "Regeln:\n" + " -> zzz                  = [" + minZzz + "," + maxZzz + "]\n" + " -> tc                   = " + tc + "\n"
				+ " -> cps                  = " + cps + "\n" + " -> direction            = " + direction + "\n" + " -> creatorGiveUp        = "
				+ creatorGiveUp + "\n" + " -> ignoreInvitable      = " + ignoreInvitable;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cps == null) ? 0 : cps.hashCode());
		result = prime * result + (creatorGiveUp ? 1231 : 1237);
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + (ignoreInvitable ? 1231 : 1237);
		result = prime * result + maxZzz;
		result = prime * result + minZzz;
		result = prime * result + ((tc == null) ? 0 : tc.hashCode());
		result = prime * result + ((zzz == null) ? 0 : zzz.hashCode());
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
		if(creatorGiveUp != other.creatorGiveUp)
			return false;
		if(direction == null)
		{
			if(other.direction != null)
				return false;
		}
		else if(!direction.equals(other.direction))
			return false;
		if(ignoreInvitable != other.ignoreInvitable)
			return false;
		if(maxZzz != other.maxZzz)
			return false;
		if(minZzz != other.minZzz)
			return false;
		if(zzz == null)
		{
			if(other.zzz != null)
				return false;
		}
		else if(!zzz.equals(other.zzz))
			return false;
		return true;
	}
}
