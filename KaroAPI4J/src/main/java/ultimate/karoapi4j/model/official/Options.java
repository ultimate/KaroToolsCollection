package ultimate.karoapi4j.model.official;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;

/**
 * POJO Options as defined by the {@link KaroAPI}
 * 
 * From game creation
 * "zzz": 2,
 * "cps": true,
 * "startdirection": "classic",
 * "crashallowed": "forbidden"
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public class Options
{
	private int					zzz;
	private boolean				cps;
	private EnumGameDirection	startdirection;
	private EnumGameTC			crashallowed;

	public Options()
	{

	}

	public Options(int zzz, boolean cps, EnumGameDirection startdirection, EnumGameTC crashallowed)
	{
		super();
		this.zzz = zzz;
		this.cps = cps;
		this.startdirection = startdirection;
		this.crashallowed = crashallowed;
	}

	public int getZzz()
	{
		return zzz;
	}

	public void setZzz(int zzz)
	{
		this.zzz = zzz;
	}

	public boolean isCps()
	{
		return cps;
	}

	public void setCps(boolean cps)
	{
		this.cps = cps;
	}

	public EnumGameDirection getStartdirection()
	{
		return startdirection;
	}

	public void setStartdirection(EnumGameDirection startdirection)
	{
		this.startdirection = startdirection;
	}

	public EnumGameTC getCrashallowed()
	{
		return crashallowed;
	}

	public void setCrashallowed(EnumGameTC crashallowed)
	{
		this.crashallowed = crashallowed;
	}
}
