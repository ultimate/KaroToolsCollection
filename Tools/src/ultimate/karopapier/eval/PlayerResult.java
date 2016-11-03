package eval;

import java.util.Date;

public class PlayerResult implements Comparable<PlayerResult>
{
	private String	player;
	private int		moves;
	private int		crashs;
	private int		position;
	private boolean	kicked;
	private Date	finished;

	public PlayerResult(String player, int moves, int crashs, int position, Date finished, boolean kicked)
	{
		super();
		this.player = player;
		this.moves = moves;
		this.crashs = crashs;
		this.position = position;
		this.finished = finished;
		this.kicked = kicked;
	}

	public String getPlayer()
	{
		return player;
	}

	public int getMoves()
	{
		return moves;
	}

	public int getCrashs()
	{
		return crashs;
	}

	public int getPosition()
	{
		return position;
	}

	public boolean isKicked()
	{
		return kicked;
	}

	public Date getFinished()
	{
		return finished;
	}

	@Override
	public int compareTo(PlayerResult o)
	{
		if(this.position - o.position != 0)
			return this.position - o.position; // kleinere pos.
		else
			return o.moves - this.moves; // groessere Zugzahl
	}
}
