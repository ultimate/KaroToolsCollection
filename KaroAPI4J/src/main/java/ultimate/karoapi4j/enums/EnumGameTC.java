package ultimate.karoapi4j.enums;

public enum EnumGameTC
{
	allowed,
	forbidden,
	free,
	random;
	
	public int getValue()
	{
		if(this.equals(allowed))
			return 1;
		if(this.equals(forbidden))
			return 2;
		if(this.equals(free))
			return 0;
		return -1;
	}
	
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
