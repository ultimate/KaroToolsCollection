package ultimate.karoapi4j.enums;

public enum EnumRefreshMode
{
	// formatter:off
	manual,
	onAccess,
	interval_1,
	interval_5,
	interval_10,
	interval_30,
	interval_60;
	// formatter:on
	
	public static EnumRefreshMode forInterval(int interval)
	{		
		for(EnumRefreshMode e: values())
		{
			if(e.getInterval() == interval)
				return e;
		}
		return null;
	}
	
	public int getInterval()
	{
		if(this == manual)
			return Integer.MAX_VALUE;
		if(this == interval_60)
			return 60;
		if(this == interval_30)
			return 30;
		if(this == interval_10)
			return 10;
		if(this == interval_5)
			return 5;
		if(this == interval_1)
			return 1;
		return 0;
	}
	
	public long getInterval_ms()
	{
		return getInterval() * 1000;
	}
	
	public boolean isIntervalBased()
	{
		int interval = getInterval();
		return interval > 0 && interval < Integer.MAX_VALUE;
	}
}
