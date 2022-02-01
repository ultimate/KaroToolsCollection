package ultimate.karoapi4j.enums;

import ultimate.karoapi4j.KaroAPI;

/**
 * Game startdirection as defined by the {@link KaroAPI}
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public enum EnumGameDirection
{
	/**
	 * Classic (move away from the finish line)
	 */
	classic,
	/**
	 * F1 (start by passing the finish line)
	 */
	formula1,
	/**
	 * free choice
	 */
	free;

	/**
	 * Get the int value (used in the classic php pages)
	 * 
	 * @return the value
	 */
	public int getValue()
	{
		switch(this)
		{
			case classic:
				return 1;
			case formula1:
				return 2;
			case free:
			default:
				return 0;
		}
	}

	/**
	 * Get the enum from the int value (used in the classic php pages)
	 * 
	 * @param value - the int value
	 * @return the enum
	 */
	public static EnumGameDirection getByValue(int value)
	{
		for(EnumGameDirection e : values())
		{
			if(e.getValue() == value)
				return e;
		}
		return null;
	}
}
