package ultimate.karoapi4j.model.extended;

import java.util.Random;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.official.Options;

/**
 * Simple POJO that defines a set that can be randomized during game creation by using {@link Rules#createOptions(Random)}.<br>
 * The rules support the following {@link Options} for randomization:<br>
 * <ul>
 * <li>ZZZ = in the range of minZzz to maxZzz (both inclusive)</li>
 * <li>crashallowed = randomized if the value is null, or preset otherwise</li>
 * <li>startdirection = randomized if the value is null, or preset otherwise</li>
 * <li>cps = randomized if the value is null, or preset otherwise</li>
 * </ul>
 * ... and the following additional properties for series with multiple game days:
 * <ul>
 * <li>gamesPerPlayer (used for {@link EnumGameSeriesType#Balanced})</li>
 * <li>numberOfPlayers (used for {@link EnumGameSeriesType#Balanced})</li>
 * </ul>
 * 
 * @author ultimate
 */
public class Rules implements Cloneable
{
	/**
	 * the minimum ZZZ to use
	 */
	private int					minZzz;
	/**
	 * the maximum ZZZ to use
	 */
	private int					maxZzz;
	/**
	 * the crash rules (or null for randomization)
	 */
	private EnumGameTC			crashallowed;
	/**
	 * cps active or not (or null for randomization)
	 */
	private Boolean				cps;
	/**
	 * the start direction (or null for randomization)
	 */
	private EnumGameDirection	startdirection;
	/**
	 * the number of games per player (used for {@link EnumGameSeriesType#Balanced})
	 */
	@JsonInclude(value = Include.NON_DEFAULT)
	private int					gamesPerPlayer;
	/**
	 * the number of players (used for {@link EnumGameSeriesType#Balanced})
	 */
	@JsonInclude(value = Include.NON_DEFAULT)
	private int					numberOfPlayers;

	/**
	 * Default constructor
	 */
	public Rules()
	{
	}

	/**
	 * @param minZzz - the minimum ZZZ to use
	 * @param maxZzz - the maximum ZZZ to use
	 * @param crashallowed - the crash rules (null for randomization)
	 * @param cps - cps active or not (or null for randomization)
	 * @param startdirection - the start direction (or null for randomization)
	 */
	public Rules(int minZzz, int maxZzz, EnumGameTC crashallowed, Boolean cps, EnumGameDirection startdirection)
	{
		this();
		this.minZzz = minZzz;
		this.maxZzz = maxZzz;
		this.crashallowed = crashallowed;
		this.cps = cps;
		this.startdirection = startdirection;
	}

	/**
	 * @param minZzz - the minimum ZZZ to use
	 * @param maxZzz - the maximum ZZZ to use
	 * @param crashallowed - the crash rules (or null for randomization)
	 * @param cps - cps active or not (or null for randomization)
	 * @param startdirection - the start direction (or null for randomization)
	 * @param gamesPerPlayer - the number of games per player (used for {@link EnumGameSeriesType#Balanced})
	 * @param numberOfPlayers - the number of players (used for {@link EnumGameSeriesType#Balanced})
	 */
	public Rules(int minZzz, int maxZzz, EnumGameTC crashallowed, Boolean cps, EnumGameDirection startdirection, int gamesPerPlayer, int numberOfPlayers)
	{
		this(minZzz, maxZzz, crashallowed, cps, startdirection);
		this.gamesPerPlayer = gamesPerPlayer;
		this.numberOfPlayers = numberOfPlayers;
	}

	/**
	 * the minimum ZZZ to use
	 * 
	 * @return minZzz
	 */
	public int getMinZzz()
	{
		return minZzz;
	}

	/**
	 * @param minZzz - the minimum ZZZ to use
	 */
	public void setMinZzz(int minZzz)
	{
		this.minZzz = minZzz;
	}

	/**
	 * the maximum ZZZ to use
	 * 
	 * @return maxZzz
	 */
	public int getMaxZzz()
	{
		return maxZzz;
	}

	/**
	 * @param maxZzz - the maximum ZZZ to use
	 */
	public void setMaxZzz(int maxZzz)
	{
		this.maxZzz = maxZzz;
	}

	/**
	 * the crash rules (or null for randomization)
	 * 
	 * @return crashallowed
	 */
	public EnumGameTC getCrashallowed()
	{
		return crashallowed;
	}

	/**
	 * @param crashallowed - the crash rules (or null for randomization)
	 */
	public void setCrashallowed(EnumGameTC crashallowed)
	{
		this.crashallowed = crashallowed;
	}

	/**
	 * cps active or not (or null for randomization)
	 * 
	 * @return cps
	 */
	public Boolean getCps()
	{
		return cps;
	}

	/**
	 * @param cps - cps active or not (or null for randomization)
	 */
	public void setCps(Boolean cps)
	{
		this.cps = cps;
	}

	/**
	 * the start direction (or null for randomization)
	 * 
	 * @return startdirection
	 */
	public EnumGameDirection getStartdirection()
	{
		return startdirection;
	}

	/**
	 * @param startdirection - the start direction (or null for randomization)
	 */
	public void setStartdirection(EnumGameDirection startdirection)
	{
		this.startdirection = startdirection;
	}

	/**
	 * the number of games per player (used for {@link EnumGameSeriesType#Balanced})
	 * 
	 * @return gamesPerPlayer
	 */
	public int getGamesPerPlayer()
	{
		return gamesPerPlayer;
	}

	/**
	 * @param gamesPerPlayer - the number of games per player (used for {@link EnumGameSeriesType#Balanced})
	 */
	public void setGamesPerPlayer(int gamesPerPlayer)
	{
		this.gamesPerPlayer = gamesPerPlayer;
	}

	/**
	 * the number of players (used for {@link EnumGameSeriesType#Balanced})
	 * 
	 * @return numberOfPlayers
	 */
	public int getNumberOfPlayers()
	{
		return numberOfPlayers;
	}

	/**
	 * @param numberOfPlayers - the number of players (used for {@link EnumGameSeriesType#Balanced})
	 */
	public void setNumberOfPlayers(int numberOfPlayers)
	{
		this.numberOfPlayers = numberOfPlayers;
	}

	/**
	 * Create a new {@link Options} object that can be used to create a game using the {@link KaroAPI}.<br>
	 * Values will be randomized within the given bounds:
	 * <ul>
	 * <li>ZZZ = in the range of minZzz to maxZzz (both inclusive)</li>
	 * <li>crashallowed = randomized if the value is null, or preset otherwise</li>
	 * <li>startdirection = randomized if the value is null, or preset otherwise</li>
	 * <li>cps = randomized if the value is null, or preset otherwise</li>
	 * </ul>
	 * 
	 * @param random - the random value generator
	 * @return the new {@link Options}
	 */
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
