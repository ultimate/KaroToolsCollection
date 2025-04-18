package ultimate.karoapi4j.model.official;

import java.util.Map;

import ultimate.karoapi4j.KaroAPI;

/**
 * POJO MapVote as defined by the {@link KaroAPI}
 * 
 * map/1/vote.json
 * "votes": {
 * "3": 4,
 * "4": 6,
 * "5": 9
 * },
 * "avg": 4.2631578947368,
 * "total": 19,
 * "myvote": 0
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public class MapVote
{
	// Standard JSON Fields
	private Map<String, Integer>	votes;
	private double					avg;
	private int						total;
	private int						myvote;

	public Map<String, Integer> getVotes()
	{
		return votes;
	}

	public double getAvg()
	{
		return avg;
	}

	public int getTotal()
	{
		return total;
	}

	public int getMyvote()
	{
		return myvote;
	}

	public void setVotes(Map<String, Integer> votes)
	{
		this.votes = votes;
	}

	public void setAvg(double avg)
	{
		this.avg = avg;
	}

	public void setTotal(int total)
	{
		this.total = total;
	}

	public void setMyvote(int myvote)
	{
		this.myvote = myvote;
	}
}
