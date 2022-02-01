package ultimate.karoapi4j.enums;

import ultimate.karoapi4j.KaroAPI;

/**
 * Game TC rule as defined by the {@link KaroAPI}
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public enum EnumGameTC
{
	/**
	 * TC is allowed
	 */
	allowed,
	/**
	 * TC is forbidden
	 */
	forbidden, 
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
			case allowed:
				return 1;
			case forbidden:
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
	public static EnumGameTC getByValue(int value)
	{
		for(EnumGameTC e : values())
		{
			if(e.getValue() == value)
				return e;
		}
		return null;
	}
}
