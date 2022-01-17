package ultimate.karopapier.eval.model;

import java.util.Date;

public class PlayerRecord extends Record<PlayerRecord, Date>
{
	protected PlayerRecord()
	{
	}

	public PlayerRecord(String[] labels, String sortLabel, String player)
	{
		super(labels, sortLabel, player);
	}
}
