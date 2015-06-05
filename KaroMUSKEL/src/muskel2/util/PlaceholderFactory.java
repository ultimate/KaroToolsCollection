package muskel2.util;

import java.util.LinkedList;
import java.util.List;

import muskel2.model.Direction;
import muskel2.model.Karopapier;
import muskel2.model.Map;
import muskel2.model.Player;
import muskel2.model.Rules;
import muskel2.model.help.Team;

public abstract class PlaceholderFactory
{
	public static String applyPlaceholders(Karopapier karopapier, String title, Map map, List<Player> gamePlayers, Rules rules, int count, int day, int dayCount, Team home, Team guest, int teams)
	{
		String name = title;
		StringBuilder tmp;

		// zaehlung
		name = name.replace("${i}", "" + (count + 1));
		if(day >= 0)
		{
			name = name.replace("${spieltag}", "" + (day + 1));
			name = name.replace("${spieltag.i}", "" + (dayCount + 1));
		}
		
		// karte
		name = name.replace("${karte.id}", "" + map.getId());
		name = name.replace("${karte.name}", map.getName());
		
		// spieler
		name = name.replace("${spieler.ersteller}", karopapier.getCurrentPlayer().getName());
		name = name.replace("${spieler.anzahl}", "" + gamePlayers.size());
		name = name.replace("${spieler.anzahl.x}", "" + (gamePlayers.size()-1));
		if(name.contains("${spieler.namen}"))
		{
			tmp = new StringBuilder();
			for(int p = 0; p < gamePlayers.size(); p++)
			{
				if((p != 0) && (p != gamePlayers.size() - 1))
					tmp.append(", ");
				else if((p != 0) && (p == gamePlayers.size() - 1))
					tmp.append(" " + Language.getString("titlepatterns.and") + " ");
				tmp.append(gamePlayers.get(p).getName());
			}
			name = name.replace("${spieler.namen}", tmp.toString());
		}
		if(name.contains("${spieler.namen.x}"))
		{
			tmp = new StringBuilder();
			List<Player> tmpList = new LinkedList<Player>(gamePlayers);
			tmpList.remove(karopapier.getCurrentPlayer());
			for(int p = 0; p < tmpList.size(); p++)
			{
				if((p != 0) && (p != tmpList.size() - 1))
					tmp.append(", ");
				else if((p != 0) && (p == tmpList.size() - 1))
					tmp.append(" " + Language.getString("titlepatterns.and") + " ");
				tmp.append(tmpList.get(p).getName());
			}
			name = name.replace("${spieler.namen.x}", tmp.toString());
		}
		
		// teams
		if(home != null)
			name = name.replace("${team.heim}", "" + home.getName());
		if(home != null)
			name = name.replace("${team.gast}", "" + guest.getName());
		
		// runde
		if(teams > 0)
		{
			if(name.contains("${runde}"))
			{
				if(teams == 2)
					name = name.replace("${runde}", Language.getString("titlepatterns.final"));
				else if(teams == 4)
					name = name.replace("${runde}", Language.getString("titlepatterns.semifinal"));
				else if(teams == 8)
					name = name.replace("${runde}", Language.getString("titlepatterns.quarterfinal"));
				else
				{
					name = name.replace("${runde}", Language.getString("titlepatterns.roundOf").replace("${i/2}", "" + (teams/2)).replace("${i}", "" + (teams)));
					
				}
			}
		}
		
		// regeln
		if(name.contains("${regeln}"))
		{
			tmp = new StringBuilder();
			tmp.append(Language.getString("titlepatterns.zzz"));
			tmp.append(rules.getZzz());
			tmp.append(", ");
			tmp.append(Language.getString("titlepatterns.tc." + rules.getCrashingAllowed()));
			tmp.append(", ");
			tmp.append(Language.getString("titlepatterns.cps." + rules.getCheckpointsActivated()));
			tmp.append(", ");
			tmp.append(Language.getString("titlepatterns.direction"));
			tmp.append(rules.getDirection());
			name = name.replace("${regeln}", tmp);
		}
		if(name.contains("${regeln.x}"))
		{
			tmp = new StringBuilder();
			if(rules.getZzz() != 2)
			{
				tmp.append(Language.getString("titlepatterns.zzz"));
				tmp.append(rules.getZzz());
			}
			if(rules.getCrashingAllowed())
			{
				if(!tmp.toString().isEmpty())
					tmp.append(", ");
				tmp.append(Language.getString("titlepatterns.tc." + rules.getCrashingAllowed()));
			}
			if(!rules.getCheckpointsActivated())
			{
				if(!tmp.toString().isEmpty())
					tmp.append(", ");
				tmp.append(Language.getString("titlepatterns.cps." + rules.getCheckpointsActivated()));
			}
			if(!rules.getDirection().equals(Direction.klassisch))
			{
				if(!tmp.toString().isEmpty())
					tmp.append(", ");
				tmp.append(Language.getString("titlepatterns.direction"));
				tmp.append(rules.getDirection());
			}
			name = name.replace("${regeln.x}", tmp);
		}
		name = name.replace("${regeln.zzz}", Language.getString("titlepatterns.zzz") + rules.getZzz());
		name = name.replace("${regeln.tc}",  Language.getString("titlepatterns.tc." + rules.getCrashingAllowed()));
		name = name.replace("${regeln.cps}", Language.getString("titlepatterns.cps." + rules.getCheckpointsActivated()));
		name = name.replace("${regeln.richtung}", Language.getString("titlepatterns.direction") + rules.getDirection());

		return name;
	}
}
