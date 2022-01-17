package ultimate.karoapi4j.model.official;

import java.util.Date;

public class ChatEntry
{
	/*
	 * chat/list.json
	 * "id" : "cmdcfac73011f091c38bb3a16f55efd52c",
	 * "user" : "Akari",
	 * "text" : "eim weiteres tor in 90 sec??",
	 * "time" : "22:36"
	 */
	// Standard JSON Fields
	private String	id;
	private User	user;
	private String	text;
	private Date	time;
	
	public ChatEntry()
	{
		super();
	}

	public String getId()
	{
		return id;
	}

	public User getUser()
	{
		return user;
	}

	public String getText()
	{
		return text;
	}

	public Date getTime()
	{
		return time;
	}

	public void setId(String id)
	{
		this.id = id;
	}
	
	public void setUser(User user)
	{
		this.user = user;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public void setTime(Date time)
	{
		this.time = time;
	}
}
