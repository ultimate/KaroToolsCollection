package ultimate.karoapi4j.model.official;

import java.util.Date;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

import ultimate.karoapi4j.model.base.Identifiable;

public class ChatMessage extends Identifiable
{
	public static final int DATE_FACTOR = 1000;

	public static class TimestampConverter implements Converter<Long, Date>
	{
		@Override
		public Date convert(Long value)
		{
			return new Date(value * DATE_FACTOR);
		}

		@Override
		public JavaType getInputType(TypeFactory typeFactory)
		{
			return typeFactory.constructType(Long.class);
		}

		@Override
		public JavaType getOutputType(TypeFactory typeFactory)
		{
			return typeFactory.constructType(Date.class);
		}
	}

	/*
	 * chat/list.json
	 * "id" : "cmdcfac73011f091c38bb3a16f55efd52c",
	 * "user" : "Akari",
	 * "text" : "eim weiteres tor in 90 sec??",
	 * "time" : "22:36"
	 */
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