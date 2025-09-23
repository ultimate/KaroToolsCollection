package ultimate.karoapi4j.model.official;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.base.Identifiable;

/**
 * POJO Move as defined by the {@link KaroAPI}
 * 
 * from https://www.karopapier.de/api/games/44773?mapcode=1&players=1&moves=1
 * "x": 73,
 * "y": 3,
 * "xv": 0,
 * "yv": 0,
 * "t": "2009-07-30 14:26:22",
 * "msg": "-:KIch werde 2 Z&uuml;ge zur&uuml;ckgesetztK:-",
 * "crash": 1
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
@JsonIgnoreProperties({ "num" })
public class Move extends Identifiable
{
	private int		x;
	private int		y;
	private int		xv;
	private int		yv;
	private Date	t;
	@JsonInclude(value = Include.NON_NULL)
	private String	msg;
	@JsonProperty(access = Access.WRITE_ONLY)
	private boolean	crash;

	public Move()
	{
		super();
	}

	public Move(int x, int y, String msg)
	{
		super();
		this.x = x;
		this.y = y;
		this.msg = msg;
	}

	public Move(int x, int y, int xv, int yv, String msg)
	{
		super();
		this.x = x;
		this.y = y;
		this.xv = xv;
		this.yv = yv;
		this.msg = msg;
	}

	public int getX()
	{
		return x;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public int getY()
	{
		return y;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	public int getXv()
	{
		return xv;
	}

	public void setXv(int xv)
	{
		this.xv = xv;
	}

	public int getYv()
	{
		return yv;
	}

	public void setYv(int yv)
	{
		this.yv = yv;
	}

	public Date getT()
	{
		return t;
	}

	public void setT(Date t)
	{
		this.t = t;
	}

	public String getMsg()
	{
		return msg;
	}

	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	public boolean isCrash()
	{
		return crash;
	}

	public void setCrash(boolean crash)
	{
		this.crash = crash;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + xv;
		result = prime * result + yv;
		result = prime * result + ((t == null) ? 0 : t.hashCode());
		result = prime * result + ((msg == null) ? 0 : msg.hashCode());
		result = prime * result + (crash ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(!super.equals(obj))
			return false;
		if(getClass() != obj.getClass())
			return false;
		Move other = (Move) obj;
		if(!equalsVec(other))
			return false;
		if(t == null)
		{
			if(other.t != null)
				return false;
		}
		else if(!t.equals(other.t))
			return false;
		if(msg == null)
		{
			if(other.msg != null)
				return false;
		}
		else if(!msg.equals(other.msg))
			return false;
		return true;
	}

	public boolean equalsVec(Move move)
	{
		if(move == null)
			return false;
		return this.x == move.x && this.y == move.y && this.xv == move.xv && this.yv == move.yv && this.crash == move.crash;
	}

	@Override
	public String toString()
	{
		return "Move [x=" + x + ", y=" + y + ", xv=" + xv + ", yv=" + yv + ", t=" + t + ", msg=" + msg + ", crash=" + crash + "]";
	}
}
