package ultimate.karopapier.eval;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ultimate.karopapier.eval.model.GameResult;
import ultimate.karopapier.eval.model.PlayerRecord;
import ultimate.karopapier.eval.model.PlayerResult;
import ultimate.karopapier.eval.model.TableRecord;

public class IQEval extends CustomEval
{
	private static final String[]			labels	= new String[] { "Datum", "Spiele", "S", "U", "N", "KaroIQ", "+/-", "IQMax", "IQMin" };

	private Map<String, List<PlayerRecord>>	playerRecords;

	private static double					k		= 2.0;
	private static double					phi		= 0.05;

	public void setConstant(String name, double value)
	{
		if(name.equals("k"))
			k = value;
		else if(name.equals("phi"))
			phi = value;
	}

	@Override
	public void init0()
	{
		this.playerRecords = new TreeMap<String, List<PlayerRecord>>();

		PlayerRecord rec;
		for(String player : players)
		{
			this.playerRecords.put(player, new LinkedList<PlayerRecord>());

			rec = new PlayerRecord(labels, labels[0], player);
			rec.setValue(labels[0], startDate);
			rec.setValue(labels[1], 0);
			rec.setValue(labels[2], 0);
			rec.setValue(labels[3], 0);
			rec.setValue(labels[4], 0);
			rec.setValue(labels[5], 100.0);
			rec.setValue(labels[6], 0.0);
			rec.setValue(labels[7], 100.0);
			rec.setValue(labels[8], 100.0);

			this.playerRecords.get(player).add(rec);
		}
	}

	@Override
	public void addToCalculation(GameResult result)
	{
		Map<String, Double> deltas = new TreeMap<String, Double>();
		List<PlayerResult> results = result.getResults();

		PlayerRecord rec1, rec2;
		PlayerResult res1, res2;
		for(int i1 = 0; i1 < results.size() - 1; i1++)
		{
			for(int i2 = i1 + 1; i2 < results.size(); i2++)
			{
				res1 = results.get(i1);
				res2 = results.get(i2);

				if(res1.isKicked() || res2.isKicked())
					continue;

				rec1 = playerRecords.get(res1.getPlayer()).get(playerRecords.get(res1.getPlayer()).size() - 1);
				rec2 = playerRecords.get(res2.getPlayer()).get(playerRecords.get(res2.getPlayer()).size() - 1);

				// die Rechnung ist darauf ausgelegt, dass Spieler 2 nicht gewonnen haben kann, da
				// er im Ranking hinter Spieler 1 ist...
				boolean draw = (res1.getMoves() == res2.getMoves());
				if(!(res1.getMoves() <= res2.getMoves()))
				{
					// da Didi aber rumgespielt hat kann es anhand der Zugzahl doch auftreten...
					// dann Spieler für folgende Berechnung tauschen...
					
					PlayerResult resTmp = res1;
					res1 = res2;
					res2 = resTmp;
					
					PlayerRecord recTmp = rec1;
					rec1 = rec2;
					rec2 = recTmp;
				}

				double iq1 = (Double) rec1.getValue(labels[5]);
				double iq2 = (Double) rec2.getValue(labels[5]);
				double exp1 = 1 / (Math.exp(-phi * (iq1 - iq2)) + 1);
				double exp2 = 1 / (Math.exp(-phi * (iq2 - iq1)) + 1);
				double delta1 = k * ((draw ? 0.5 : 1) - exp1);
				double delta2 = k * ((draw ? 0.5 : 0) - exp2);

				rec1.setValue(labels[draw ? 3 : 2], ((Integer) rec1.getValue(labels[draw ? 3 : 2])) + 1);

				if(deltas.get(res1.getPlayer()) == null)
					deltas.put(res1.getPlayer(), delta1);
				else
					deltas.put(res1.getPlayer(), deltas.get(res1.getPlayer()) + delta1);

				rec2.setValue(labels[draw ? 3 : 4], ((Integer) rec2.getValue(labels[draw ? 3 : 4])) + 1);

				if(deltas.get(res2.getPlayer()) == null)
					deltas.put(res2.getPlayer(), delta2);
				else
					deltas.put(res2.getPlayer(), deltas.get(res2.getPlayer()) + delta2);
			}
		}

		PlayerRecord rec;
		double delta, iq;
		for(String player : deltas.keySet())
		{
			rec = playerRecords.get(player).get(playerRecords.get(player).size() - 1);

			delta = deltas.get(player);
			delta = delta / Math.sqrt(deltas.size() - 1);

			iq = (Double) rec.getValue(labels[5]);
			iq = iq + delta;

			rec.setValue(labels[0], result.getFinishDate());
			rec.setValue(labels[1], ((Integer) rec.getValue(labels[1])) + 1);
			rec.setValue(labels[5], iq);
			rec.setValue(labels[6], (Double) rec.getValue(labels[6]) + delta);
			if(iq > (Double) rec.getValue(labels[7]))
				rec.setValue(labels[7], iq);
			if(iq < (Double) rec.getValue(labels[8]))
				rec.setValue(labels[8], iq);
		}
	}

	@Override
	public void addInterval(Date date)
	{
		PlayerRecord prOld = null;
		PlayerRecord prNew = null;
		for(List<PlayerRecord> prs : playerRecords.values())
		{
			prOld = prs.get(prs.size() - 1); // last record
			prOld.setValue(labels[0], date); // set interval timestamp

			prNew = prOld.clone(); // clone record for further manipulation
			prNew.setValue(labels[6], 0.0); // reset delta
			prs.add(prNew); // add new record
		}
	}

	@Override
	public Map<String, List<PlayerRecord>> getPlayerRecords()
	{
		return playerRecords;
	}

	@Override
	public List<TableRecord> getTable()
	{
		List<TableRecord> table = new LinkedList<TableRecord>();
		TableRecord tr;
		for(List<PlayerRecord> list : playerRecords.values())
		{
			tr = new TableRecord("KaroIQ", list.get(list.size() - 1));
			table.add(tr);
		}
		Collections.sort(table);
		return table;
	}
}
