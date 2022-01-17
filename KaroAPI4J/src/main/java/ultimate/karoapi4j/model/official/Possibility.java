package ultimate.karoapi4j.model.official;

public class Possibility
{
	/*
	 * "x" : 78,
	 * "y" : 33,
	 * "xv" : -1,
	 * "yv" : -5
	 */
	private int	x;
	private int	y;
	private int	xv;
	private int	yv;

	public Possibility()
	{
		super();
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public int getXv()
	{
		return xv;
	}

	public int getYv()
	{
		return yv;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	public void setXv(int xv)
	{
		this.xv = xv;
	}

	public void setYv(int yv)
	{
		this.yv = yv;
	}
}
