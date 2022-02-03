package muskel2.util;

import java.util.LinkedList;
import java.util.List;

import muskel2.model.Direction;
import muskel2.model.Game;
import muskel2.model.Karopapier;
import muskel2.model.Map;
import muskel2.model.Player;
import muskel2.model.Rules;
import muskel2.model.help.Team;
import ultimate.karomuskel.utils.Language;

public abstract class PlaceholderFactory
{
	public static String applyPlaceholders(Karopapier karopapier, String title, Map map, List<Player> gamePlayers, Rules rules, int count, int day, int dayCount, Team[] teams, int round, int group)
	{
		String name = title;
		StringBuilder tmp;

		// zaehlung
		name = name.replace("${i}", "" + (count + 1));
		name = name.replace("${ii}", toString(count + 1, 2));
		name = name.replace("${iii}", toString(count + 1, 3));
		name = name.replace("${iiii}", toString(count + 1, 4));
		if(day >= 0)
		{
			name = name.replace("${spieltag}", "" + (day + 1));
			name = name.replace("${spieltag.i}", "" + (dayCount + 1));
		}
		else
		{
			name = name.replace("${spieltag}", "");
			name = name.replace("${spieltag.i}", "");
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
		if(teams != null)
		{
			tmp = new StringBuilder();
			
			for(int i = 0; i < teams.length; i++)
			{
				if(i > 0)
					tmp.append(" vs. ");
				tmp.append(teams[i].getName());
			}
			
			name = name.replace("${teams}", tmp.toString());
		}
		
		// runde
		if(round > 0)
		{
			if(name.contains("${runde}"))
			{
				if(round == 2)
					name = name.replace("${runde}", Language.getString("titlepatterns.final"));
				else if(round == 4)
					name = name.replace("${runde}", Language.getString("titlepatterns.semifinal"));
				else if(round == 8)
					name = name.replace("${runde}", Language.getString("titlepatterns.quarterfinal"));
				else if(group <= 0)
					name = name.replace("${runde}", Language.getString("titlepatterns.roundOf").replace("${i/2}", "" + (round/2)).replace("${i}", "" + (round)));
				else 
					name = name.replace("${runde}", Language.getString("titlepatterns.groupStage").replace("${i}", "" + (group)));
			}
			if(name.contains("${runde.x}"))
			{
				if(round == 2)
					name = name.replace("${runde.x}", Language.getString("titlepatterns.final") + ", " + Language.getString("titlepatterns.match") + " " + count);
				else if(round == 4)
					name = name.replace("${runde.x}", Language.getString("titlepatterns.semifinal") + ", " + Language.getString("titlepatterns.match") + " " + count);
				else if(round == 8)
					name = name.replace("${runde.x}", Language.getString("titlepatterns.quarterfinal") + ", " + Language.getString("titlepatterns.match") + " " + count);
				else if(group <= 0)
					name = name.replace("${runde.x}", Language.getString("titlepatterns.roundOf").replace("${i/2}", "" + (round/2)).replace("${i}", "" + (round)) + ", " + Language.getString("titlepatterns.match") + " " + count);
				else 
					name = name.replace("${runde.x}", Language.getString("titlepatterns.groupStage").replace("${i}", "" + (group)) + ", " + Language.getString("titlepatterns.day") + " " + (day+1) );
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

		name = name.replace("  ", "");
		name = name.trim();
		if(name.endsWith(","))
			name = name.substring(0, name.length() -1);
		
		return name;
	}
	
	/**
	 * Shortcut for {@link PlaceholderFactory#applyPlaceholders(Karopapier, String, Map, List, Rules, int, int, int, Team, Team, int)}
	 * Note: some Placeholders might not work here!
	 * @param karopapier
	 * @param title
	 * @param game
	 * @param count
	 * @return
	 */
	public static String applyPlaceholders(Karopapier karopapier, String title, Game game, int count)
	{
		return applyPlaceholders(karopapier, title, game.getMap(), game.getPlayers(), game.getRules(), count, 0, 0, null, 0, 0);
	}
	
	private static String toString(int x, int digits)
	{
		String s = "" + x;
		while(s.length() < digits)
			s = "0" + s;
		return s;
	}
}
