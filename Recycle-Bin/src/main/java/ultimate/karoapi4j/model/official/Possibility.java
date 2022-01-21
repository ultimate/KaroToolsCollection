package ultimate.karoapi4j.model.official;

@Deprecated
public class Possibility
{
	/*
	 * from https://www.karopapier.de/api/games/132000?mapcode=1&players=1&moves=1
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
