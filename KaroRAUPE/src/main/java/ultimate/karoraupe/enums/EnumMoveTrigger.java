package ultimate.karoraupe.enums;

import ultimate.karoraupe.Mover;

/**
 * Condition for the {@link Mover}.<br>
 * This enum is designed for maximum input tolerance and therefore has multiple values for the same meaning.<br>
 * You can use {@link EnumMoveTrigger#standardize()} to get the matching standard value.
 * 
 * @author ultimate
 */
public enum EnumMoveTrigger
{
	/**
	 * always
	 */
	always, immer,
	/**
	 * no message
	 */
	nomessage, nomsg, keinbordfunk,
	/**
	 * never
	 */
	never, nie, niemals,
	/**
	 * invalid state
	 */
	invalid;

	/**
	 * Return the matching standard value for an enum value.
	 * 
	 * @return
	 */
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
