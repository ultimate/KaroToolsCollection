package muskel2.model;

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
}
