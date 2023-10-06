package ultimate.karoapi4j.model.official;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.enums.EnumUserMessageType;
import ultimate.karoapi4j.model.base.Identifiable;
import ultimate.karoapi4j.utils.JSONUtil.TimestampConverter;

/**
 * POJO UserMessage as defined by the {@link KaroAPI}
 * 
 * "id": 2111,
 * "user_id": 1411,
 * "user_name": "ultimate",
 * "contact_id": 1,
 * "contact_name": "Didi",
 * "ts": 1607449359,
 * "r": 1,
 * "text": "...",
 * "rxtx": "tx"
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public class UserMessage extends Identifiable
{
	private int				user_id;
	private String			user_name;
	private int				contact_id;
	private String			contact_name;
	@JsonDeserialize(converter = TimestampConverter.class)
	private Date			ts;
	private boolean			r;
	private String			text;
	private EnumUserMessageType	rxtx;

	public int getUser_id()
	{
		return user_id;
	}

	public void setUser_id(int user_id)
	{
		this.user_id = user_id;
	}

	public String getUser_name()
	{
		return user_name;
	}

	public void setUser_name(String user_name)
	{
		this.user_name = user_name;
	}

	public int getContact_id()
	{
		return contact_id;
	}

	public void setContact_id(int contact_id)
	{
		this.contact_id = contact_id;
	}

	public String getContact_name()
	{
		return contact_name;
	}

	public void setContact_name(String contact_name)
	{
		this.contact_name = contact_name;
	}

	public Date getTs()
	{
		return ts;
	}

	public void setTs(Date ts)
	{
		this.ts = ts;
	}

	public boolean isR()
	{
		return r;
	}

	public void setR(boolean r)
	{
		this.r = r;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public EnumUserMessageType getRxtx()
	{
		return rxtx;
	}

	public void setRxtx(EnumUserMessageType rxtx)
	{
		this.rxtx = rxtx;
	}
}
