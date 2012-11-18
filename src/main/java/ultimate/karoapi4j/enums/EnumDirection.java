package ultimate.karoapi4j.enums;


public enum EnumDirection
{
	classic,
	free,
	formula1,
	random;
	
	public int getValue()
	{
		if(this.equals(classic))
			return 1;
		if(this.equals(formula1))
			return 2;
		if(this.equals(free))
			return 0;
		return -1;
	}
	
	public static EnumDirection getByValue(int value)
	{
		for(EnumDirection e : values())
		{
			if(e.getValue() == value)
				return e;
		}
		return null;
	}
}
