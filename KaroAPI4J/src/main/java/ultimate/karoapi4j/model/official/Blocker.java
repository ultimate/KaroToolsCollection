package ultimate.karoapi4j.model.official;

public class Blocker
{
	/*
	 * user/1/blocker.json
	 * "id" : "563",
	 * "login" : "Madeleine",
	 * "blocked" : "7"
	 */
	private User	blockedBy;
	private int		blocked;

	public Blocker()
	{
		super();
	}

	public User getBlockedBy()
	{
		return blockedBy;
	}

	public int getBlocked()
	{
		return blocked;
	}

	public void setBlockedBy(User blockedBy)
	{
		this.blockedBy = blockedBy;
	}

	public void setBlocked(int blocked)
	{
		this.blocked = blocked;
	}
}
