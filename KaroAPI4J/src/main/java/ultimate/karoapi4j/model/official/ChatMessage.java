package ultimate.karoapi4j.model.official;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.base.Identifiable;
import ultimate.karoapi4j.utils.JSONUtil.TimestampConverter;

/**
 * POJO ChatMessage as defined by the {@link KaroAPI}
 * 
 * chat/list.json
 * "id" : "cmdcfac73011f091c38bb3a16f55efd52c",
 * "user" : "Akari",
 * "text" : "eim weiteres tor in 90 sec??",
 * "time" : "22:36"
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public class ChatMessage extends Identifiable
{
	// for id see super class
	// private int id; // see super class
	// Standard JSON Fields
	private String	user;
	private String	text;
	private String	line;
	private String	time;
	private int		lineId;
	private int		uid;
	@JsonDeserialize(converter = TimestampConverter.class)
	private Date	ts;

	public ChatMessage()
	{
		super();
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String getLine()
	{
		return line;
	}

	public void setLine(String line)
	{
		this.line = line;
	}

	public String getTime()
	{
		return time;
	}

	public void setTime(String time)
	{
		this.time = time;
	}

	public int getLineId()
	{
		return lineId;
	}

	public void setLineId(int lineId)
	{
		this.lineId = lineId;
	}

	public int getUid()
	{
		return uid;
	}

	public void setUid(int uid)
	{
		this.uid = uid;
	}

	public Date getTs()
	{
		return ts;
	}

	public void setTs(Date ts)
	{
		this.ts = ts;
	}
}
