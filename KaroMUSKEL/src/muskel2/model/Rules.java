package muskel2.model;

import java.io.Serializable;
import java.util.Random;

public class Rules implements Cloneable, Serializable
{
	private static final long	serialVersionUID	= 1L;
	
	private int			minZzz;
	private int			maxZzz;
	private Integer 	zzz;
	private Boolean		crashingAllowed;
	private Boolean		checkpointsActivated;
	private Direction	direction;
	private boolean		creatorGiveUp;
	private boolean		ignoreInvitable;
	private int 		gamesPerPlayer;
	private int			numberOfPlayers;
	
	private Random		random;
	
	public Rules(int minZzz, int maxZzz, Boolean crashingAllowed, Boolean checkpointsActivated, Direction direction, boolean creatorGiveUp, boolean ignoreInvitable)
	{
		super();
		this.minZzz = minZzz;
		this.maxZzz = maxZzz;
		this.zzz = null;
		this.crashingAllowed = crashingAllowed;
		this.checkpointsActivated = checkpointsActivated;
		this.direction = direction;
		this.creatorGiveUp = creatorGiveUp;
		this.ignoreInvitable = ignoreInvitable;
		this.random = new Random();
	}
	
	
	
	public Rules(int minZzz, int maxZzz, Boolean crashingAllowed, Boolean checkpointsActivated, Direction direction, boolean creatorGiveUp,
			boolean ignoreInvitable, int gamesPerPlayer, int numberOfPlayers)
	{
		super();
		this.minZzz = minZzz;
		this.maxZzz = maxZzz;
		this.crashingAllowed = crashingAllowed;
		this.checkpointsActivated = checkpointsActivated;
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

	public Boolean getCrashingAllowed()
	{
		return crashingAllowed;
	}

	public Boolean getCheckpointsActivated()
	{
		return checkpointsActivated;
	}

	public Direction getDirection()
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

	public void setCrashingAllowed(Boolean crashingAllowed)
	{
		this.crashingAllowed = crashingAllowed;
	}

	public void setCheckpointsActivated(Boolean checkpointsActivated)
	{
		this.checkpointsActivated = checkpointsActivated;
	}

	public void setDirection(Direction direction)
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
		if(crashingAllowed == null)
			crashingAllowed = random.nextBoolean();
		if(checkpointsActivated == null)
			checkpointsActivated = random.nextBoolean();
		if(direction == null)
		{
			int dir = random.nextInt(Direction.values().length-1);
			for(Direction d: Direction.values())
			{
				if(d.getValue() == dir)
				{
					direction = d;
					break;
				}
			}
		}
		return this;
	}
	
	@Override
	public Rules clone()
	{
		return new Rules(minZzz, maxZzz, crashingAllowed, checkpointsActivated, direction, creatorGiveUp, ignoreInvitable);
	}
	
	@Override
	public String toString()
	{
		return 	"Regeln:\n" +
				" -> zzz                  = [" + minZzz + "," + maxZzz + "]\n" +
				" -> crashingAllowed      = " + crashingAllowed + "\n" +
				" -> checkpointsActivated = " + checkpointsActivated + "\n" +
				" -> direction            = " + direction + "\n" +
				" -> creatorGiveUp        = " + creatorGiveUp + "\n" +
				" -> ignoreInvitable      = " + ignoreInvitable;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((checkpointsActivated == null) ? 0 : checkpointsActivated.hashCode());
		result = prime * result + ((crashingAllowed == null) ? 0 : crashingAllowed.hashCode());
		result = prime * result + (creatorGiveUp ? 1231 : 1237);
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + (ignoreInvitable ? 1231 : 1237);
		result = prime * result + maxZzz;
		result = prime * result + minZzz;
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
		if(checkpointsActivated == null)
		{
			if(other.checkpointsActivated != null)
				return false;
		}
		else if(!checkpointsActivated.equals(other.checkpointsActivated))
			return false;
		if(crashingAllowed == null)
		{
			if(other.crashingAllowed != null)
				return false;
		}
		else if(!crashingAllowed.equals(other.crashingAllowed))
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
