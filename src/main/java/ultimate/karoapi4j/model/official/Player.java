package ultimate.karoapi4j.model.official;

import java.util.List;

import ultimate.karoapi4j.enums.EnumStatus;

public class Player
{
	/*
	 * "id" : 1,
	 * "name" : "Didi",
	 * "color" : "FFFFFF",
	 * "dran" : false,
	 * "moved" : false,
	 * "position" : 4,
	 * "status" : "ok",
	 * "moveCount" : 157,
	 * "crashCount" : 1,
	 * "missingCps" : [],
	 * "lastmove" : {
	 * "x" : 0,
	 * "y" : 3,
	 * "xv" : 0,
	 * "yv" : 0,
	 * "c" : 0,
	 * "t" : "2012-03-05 22:02:14"
	 * },
	 * "possibles" : [...],
	 * "moves" : [...]
	 */
	// Standard JSON Fields
	private User				user;
	private boolean				dran;
	private boolean				moved;
	private int					position;
	private EnumStatus			status;
	private int					moveCount;
	private int					crashCount;
	private String[]			missingCps; // type?
	// Further JSON Fields
	private Move				lastMove;
	private List<Possibility>	possibles;
	private List<Move>			moves;

	public Player()
	{
		super();
	}

	public User getUser()
	{
		return user;
	}

	public boolean isDran()
	{
		return dran;
	}

	public boolean isMoved()
	{
		return moved;
	}

	public int getPosition()
	{
		return position;
	}

	public EnumStatus getStatus()
	{
		return status;
	}

	public int getMoveCount()
	{
		return moveCount;
	}

	public int getCrashCount()
	{
		return crashCount;
	}

	public String[] getMissingCps()
	{
		return missingCps;
	}

	public Move getLastMove()
	{
		return lastMove;
	}

	public List<Possibility> getPossibles()
	{
		return possibles;
	}

	public List<Move> getMoves()
	{
		return moves;
	}

	public void setUser(User user)
	{
		this.user = user;
	}

	public void setDran(boolean dran)
	{
		this.dran = dran;
	}

	public void setMoved(boolean moved)
	{
		this.moved = moved;
	}

	public void setPosition(int position)
	{
		this.position = position;
	}

	public void setStatus(EnumStatus status)
	{
		this.status = status;
	}

	public void setMoveCount(int moveCount)
	{
		this.moveCount = moveCount;
	}

	public void setCrashCount(int crashCount)
	{
		this.crashCount = crashCount;
	}

	public void setMissingCps(String[] missingCps)
	{
		this.missingCps = missingCps;
	}

	public void setLastMove(Move lastMove)
	{
		this.lastMove = lastMove;
	}

	public void setPossibles(List<Possibility> possibles)
	{
		this.possibles = possibles;
	}

	public void setMoves(List<Move> moves)
	{
		this.moves = moves;
	}
}
