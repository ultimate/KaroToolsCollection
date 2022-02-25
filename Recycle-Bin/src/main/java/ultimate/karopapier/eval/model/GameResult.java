package ultimate.karopapier.eval.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import ultimate.karopapier.eval.CustomEval;

public class GameResult
{
	private int				gid;
	private String			name;
	private Date			finishDate;
	private List<PlayerResult>	results;

	public GameResult(int gid, String name, Date finishDate, List<PlayerResult> results)
	{
		super();
		this.gid = gid;
		this.name = name;
		this.finishDate = finishDate;
		Collections.sort(results);
		this.results = results;
	}

	public int getGid()
	{
		return gid;
	}

	public String getName()
	{
		return name;
	}

	public Date getFinishDate()
	{
		return finishDate;
	}

	public List<PlayerResult> getResults()
	{
		return results;
	}

	public boolean isFinished()
	{
		return finishDate != null;
	}

	@Override
	public String toString()
	{
		String title = "GameResult: \"" + name + "\" [" + gid + "]\n";
		StringBuilder[] sbs = new StringBuilder[this.results.size() + 1];

		for(int i = 0; i < sbs.length; i++)
			sbs[i] = new StringBuilder();

		sbs[0].append("Pos.");
		for(int i = 1; i < sbs.length; i++)
			sbs[i].append(results.get(i - 1).getPosition());
		CustomEval.fillStringBuilders(sbs);

		sbs[0].append("Player");
		for(int i = 1; i < sbs.length; i++)
			sbs[i].append(results.get(i - 1).getPlayer());
		CustomEval.fillStringBuilders(sbs);

		sbs[0].append("Moves");
		for(int i = 1; i < sbs.length; i++)
			sbs[i].append(results.get(i - 1).getMoves());
		CustomEval.fillStringBuilders(sbs);

		sbs[0].append("Crashs");
		for(int i = 1; i < sbs.length; i++)
			sbs[i].append(results.get(i - 1).getCrashs());
		CustomEval.fillStringBuilders(sbs);

		sbs[0].append("Finished");
		for(int i = 1; i < sbs.length; i++)
			sbs[i].append(results.get(i - 1).getFinished() == null ? "-" : CustomEval.format(results.get(i - 1).getFinished()));
		CustomEval.fillStringBuilders(sbs);

		StringBuilder all = new StringBuilder();
		all.append(title);
		for(int i = 0; i < sbs.length; i++)
		{
			all.append(sbs[i]);
			all.append("\n");
		}

		return all.toString();
	}

	public static class DateComparator implements Comparator<GameResult>
	{
		@Override
		public int compare(GameResult o1, GameResult o2)
		{
			return o1.getFinishDate().compareTo(o2.getFinishDate());
		}
	}

	public static class GIDComparator implements Comparator<GameResult>
	{
		@Override
		public int compare(GameResult o1, GameResult o2)
		{
			return o1.getGid() - o2.getGid();
		}
	}
}