package muskel2.model.series;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import muskel2.model.GameSeries;
import muskel2.model.Player;

@Deprecated
public class KLCGameSeries extends GameSeries
{
	public static final long			serialVersionUID	= 1L;

	public static int					GROUPS				= 8;
	public static int					LEAGUES				= 4;
	public static int					PLAYERS				= GROUPS * LEAGUES;
	public static int					FIRST_KO_ROUND		= 16;
	public static int					WINNERS_PER_GROUP	= FIRST_KO_ROUND / GROUPS;

	public List<Player>					allPlayers			= new ArrayList<Player>(PLAYERS);
	public List<Player>					playersLeague1		= new ArrayList<Player>(8);
	public List<Player>					playersLeague2		= new ArrayList<Player>(8);
	public List<Player>					playersLeague3		= new ArrayList<Player>(8);
	public List<Player>					playersLeague4		= new ArrayList<Player>(8);
	public List<Player>					playersGroup1		= new ArrayList<Player>(4);
	public List<Player>					playersGroup2		= new ArrayList<Player>(4);
	public List<Player>					playersGroup3		= new ArrayList<Player>(4);
	public List<Player>					playersGroup4		= new ArrayList<Player>(4);
	public List<Player>					playersGroup5		= new ArrayList<Player>(4);
	public List<Player>					playersGroup6		= new ArrayList<Player>(4);
	public List<Player>					playersGroup7		= new ArrayList<Player>(4);
	public List<Player>					playersGroup8		= new ArrayList<Player>(4);
	public List<Player>					playersRoundOf16	= new ArrayList<Player>(16);
	public List<Player>					playersRoundOf8		= new ArrayList<Player>(8);
	public List<Player>					playersRoundOf4		= new ArrayList<Player>(4);
	public List<Player>					playersRoundOf2		= new ArrayList<Player>(2);
	public int							round				= PLAYERS;

	public HashMap<Integer, Integer>	homeMaps			= new HashMap<Integer, Integer>();
}
