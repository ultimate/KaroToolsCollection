package ultimate.karoraupe.enums;

import ultimate.karoraupe.Mover;

/**
 * Condition for the {@link Mover}
 * 
 * @author ultimate
 */
public enum EnumMoveTrigger
{
	/**
	 * always
	 */
	always,
	immer,
	/**
	 * no message
	 */
	nomessage,
	nomsg,
	keinbordfunk,
	/**
	 * never
	 */
	never,
	nie,
	niemals,
	/**
	 * invalid state
	 */
	invalid;
	
	public EnumMoveTrigger standardize()
	{
		switch(this)
		{
			case always:
			case immer:
				return always;
				
			case nomessage:
			case nomsg:
			case keinbordfunk:
				return nomessage;
				
			case never:
			case nie:
			case niemals:
				return never;
				
			case invalid:
			default:
				return invalid;
		}
	}
}
