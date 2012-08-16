package ultimate.karoapi4j.model;

import java.util.Date;

public class Move
{
	/*
	 * "x" : "73",
	 * "y" : "3",
	 * "xv" : "0",
	 * "yv" : "0",
	 * "c" : true,
	 * "t" : "2009-07-30 14:26:22",
	 * "msg" : "-:KIch werde 2 Z&uuml;ge zur&uuml;ckgesetztK:-"
	 */
	private int		x;
	private int		y;
	private int		xv;
	private int		yv;
	private boolean	c;
	private Date	t;
	private String	msg;

	public Move()
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

	public boolean isC()
	{
		return c;
	}

	public Date getT()
	{
		return t;
	}

	public String getMsg()
	{
		return msg;
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

	public void setC(boolean c)
	{
		this.c = c;
	}

	public void setT(Date t)
	{
		this.t = t;
	}

	public void setMsg(String msg)
	{
		this.msg = msg;
	}
}
