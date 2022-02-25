package muskel2.model;

import java.io.Serializable;
import java.util.Random;

@Deprecated
public class Rules implements Cloneable, Serializable
{
	public static final long	serialVersionUID	= 1L;

	public int					minZzz;
	public int					maxZzz;
	public Integer				zzz;
	public Boolean				crashingAllowed;
	public Boolean				checkpointsActivated;
	public Direction			direction;
	public boolean				creatorGiveUp;
	public boolean				ignoreInvitable;
	public int					gamesPerPlayer;
	public int					numberOfPlayers;

	public Random				random;
}
