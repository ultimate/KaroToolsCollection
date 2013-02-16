package muskel2.model;

import muskel2.util.Language;

public enum Direction
{
	klassisch,
	egal,
	Formula_1,
	random;
	
	public int getValue()
	{
		if(this.equals(klassisch))
			return 1;
		if(this.equals(Formula_1))
			return 2;
		return 0;
	}
	
	public String toString()
	{
		switch(this)
		{
			case klassisch: return Language.getString("option.direction.klassisch");
			case egal: return Language.getString("option.direction.egal");
			case Formula_1: return Language.getString("option.direction.formula1");
			case random: return Language.getString("option.direction.random");
		}
		return null;
	}
}
