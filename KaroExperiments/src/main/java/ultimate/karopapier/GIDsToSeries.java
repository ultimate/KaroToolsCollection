package ultimate.karopapier;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Options;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karomuskel.GameSeriesManager;

public class GIDsToSeries
{
	public static void main(String[] args) throws IOException
	{
		File loginProperties = new File(args[0]);
		int fromGID = Integer.parseInt(args[1]);
		int toGID = Integer.parseInt(args[2]);
		File outputFile = new File(args[3]);
		System.out.println(loginProperties.getAbsolutePath());
		
		Properties login = PropertiesUtil.loadProperties(loginProperties);
		
		KaroAPI api;
		if(login.containsKey("karoAPI.key"))
			api = new KaroAPI(login.getProperty("karoAPI.key"));
		else
			api = new KaroAPI(login.getProperty("karoAPI.user"), login.getProperty("karoAPI.password"));
		
		GameSeries gs = new GameSeries(EnumGameSeriesType.Simple);
		List<PlannedGame> games = new LinkedList<PlannedGame>();
		gs.getGames().put(gs.getType().toString(), games);
				
		Game g;
		Options o;
		Set<User> users;
		PlannedGame pg;
		for(int gid = fromGID; gid <= toGID; gid++)
		{
			System.out.println("processing game " + gid);
			try
			{
				g = api.getGameWithDetails(gid).get();
				o = new Options(g.getZzz(), g.isCps(), g.getStartdirection(), g.getCrashallowed());
				users = new HashSet<>();
				for(Player p: g.getPlayers())
				{
					users.add(new User(p.getId()));
				}
				pg = new PlannedGame(g.getName(), g.getMap(), users, o, g.getTags());
				pg.setGame(g);
	
				games.add(pg);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}	
		
		GameSeriesManager.store(gs, outputFile);
	}
}
