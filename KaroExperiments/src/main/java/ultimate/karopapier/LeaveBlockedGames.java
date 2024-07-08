package ultimate.karopapier;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.enums.EnumUserGamesort;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.PropertiesUtil;

public class LeaveBlockedGames
{
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		File loginProperties = new File(args[0]);
		String name = args[1];
		int dran = Integer.parseInt(args[2]);
		int limit = Integer.parseInt(args[3]);

		System.out.println(loginProperties.getAbsolutePath());
		
		Properties login = PropertiesUtil.loadProperties(loginProperties);
		
		KaroAPI api;
		if(login.containsKey("karoAPI.key"))
			api = new KaroAPI(login.getProperty("karoAPI.key"));
		else
			api = new KaroAPI(login.getProperty("karoAPI.user"), login.getProperty("karoAPI.password"));
		
		User user = api.check().get();
		
		System.out.println("user logged in: " + user.getLogin() + " (id=" + user.getId() + ")");
		System.out.println("leaving games with title '" + name + "' and dran >= " + dran);
		
		List<Game> games = api.getGames(true, EnumUserGamesort.gid2, null, false, name, true, limit, null).get();
		
		games.forEach(g -> {
			try
			{
				System.out.print("gid = " + g.getId() + ", name = " + g.getName() + ", dran = " + (g.getNext() != null ? g.getNext().getId() : "?") + " / " + g.getBlocked());
				if((int) g.getNext().getId() != (int) user.getId())
				{
					System.out.println(" --> wrong user's turn");
					return;
				}					
				if(g.getBlocked() < dran)
				{
					System.out.println(" --> not blocked long enough");
					return;
				}
				System.out.print(" --> leaving");
				
//				System.out.println(" --> " + api.leaveGame(g.getId()).get());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		});
		
		System.exit(0);
	}
}
