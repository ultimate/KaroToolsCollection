package ultimate.karoapi4j.model.official;

import java.awt.Color;
import java.util.List;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.enums.EnumPlayerStatus;
import ultimate.karoapi4j.model.base.Identifiable;

/**
 * POJO Player as defined by the {@link KaroAPI}
 * 
 * https://www.karopapier.de/api/games/132000?mapcode=1&players=1&moves=1
 * "id": 1,
 * "name": "Didi",
 * "color": "ffffff",
 * "status": "ok",
 * "moved": false,
 * "rank": 4,
 * "checkedCps": [ 1, 2 ],
 * "moveCount": 157,
 * "crashCount": 1,
 * "moves": [ ... ], // see type Move
 * "motion": { "x": 0, "y": 3, "xv": 0, "yv": 0, "t": "2012-03-05 22:02:14" }, // see type Move
 * "missingCps": ["3", "4" ],
 * "possibles": [ ... ], // see type Move (just without t, msg and crash)
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public class Player extends Identifiable
{
	// private int		id;	// see super class
	private String				name;
	private Color				color;
	private EnumPlayerStatus	status;
	private boolean				moved;
	private int					rank;
	private int[]				checkedCps;
	private int					moveCount;
	private int					crashCount;
	private List<Move>			moves;
	private Move				motion;
	private int[]				missingCps;
	private List<Move>			possibles;

	public Player()
	{
		super();
	}

	public Player(Integer id)
	{
		super(id);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public EnumPlayerStatus getStatus()
	{
		return status;
	}

	public void setStatus(EnumPlayerStatus status)
	{
		this.status = status;
	}

	public boolean isMoved()
	{
		return moved;
	}

	public void setMoved(boolean moved)
	{
		this.moved = moved;
	}

	public int getRank()
	{
		return rank;
	}

	public void setRank(int rank)
	{
		this.rank = rank;
	}

	public int[] getCheckedCps()
	{
		return checkedCps;
	}

	public void setCheckedCps(int[] checkedCps)
	{
		this.checkedCps = checkedCps;
	}

	public int getMoveCount()
	{
		return moveCount;
	}

	public void setMoveCount(int moveCount)
	{
		this.moveCount = moveCount;
	}

	public int getCrashCount()
	{
		return crashCount;
	}

	public void setCrashCount(int crashCount)
	{
		this.crashCount = crashCount;
	}

	public List<Move> getMoves()
	{
		return moves;
	}

	public void setMoves(List<Move> moves)
	{
		this.moves = moves;
	}

	public Move getMotion()
	{
		return motion;
	}

	public void setMotion(Move motion)
	{
		this.motion = motion;
	}

	public int[] getMissingCps()
	{
		return missingCps;
	}

	public void setMissingCps(int[] missingCps)
	{
		this.missingCps = missingCps;
	}

	public List<Move> getPossibles()
	{
		return possibles;
	}

	public void setPossibles(List<Move> possibles)
	{
		this.possibles = possibles;
	}
}
