package ultimate.karopapier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoapi4j.utils.PropertiesUtil;

public class FixGID
{
	/**
	 * Logger-Instance
	 */
	protected static transient final Logger	logger				= LogManager.getLogger(FixGID.class);

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		File loginProperties = new File(args[0]);
		System.out.println(loginProperties.getAbsolutePath());
		
		Properties login = PropertiesUtil.loadProperties(loginProperties);
		
		KaroAPI api = new KaroAPI(login.getProperty("karoAPI.user"), login.getProperty("karoAPI.password"));
				
		analyze147095(api);

		System.exit(0);
	}
	
	public static void fix147095(KaroAPI api) throws InterruptedException, ExecutionException
	{
		int gid = 147095;
		String playerName = "ultimate";

		Game game = api.getGame(gid, null, true, true).get();

		Player player = null;
		for(Player p: game.getPlayers())
		{
			if(p.getName().equalsIgnoreCase(playerName))
			{
				player = p;
				break;
			}
		}

		if(player == null)
		{
			logger.error("player '" + playerName + "' not found");
			return;
		}

		// update planned moves
		List<Move> plannedMoves = new ArrayList<>();
		// turns
		for(int y = 80; y > 2; y -= 3)
		{
			// turn on the right
			plannedMoves.add(new Move(252, y, 1, 0, null));
			plannedMoves.add(new Move(253, y, 1, 0, null));
			plannedMoves.add(new Move(253, y-1, 0, -1, null));
			plannedMoves.add(new Move(252, y-1, -1, 0, null));
			plannedMoves.add(new Move(252, y, 0, 1, null));
			plannedMoves.add(new Move(251, y, -1, 0, null));
			// turn on the left
			plannedMoves.add(new Move(2, y-1, -1, -1, null));
		}		
		// finish move
		plannedMoves.add(new Move(253, 1, 1, -1, null));
		
		api.addPlannedMoves(gid, plannedMoves).get();
	}
	
	public static void analyze147095(KaroAPI api) throws InterruptedException, ExecutionException
	{
		int gid = 147095;
		String playerName = "ultimate";

		Game game = api.getGame(gid, null, true, true).get();

		Player player = null;
		for(Player p: game.getPlayers())
		{
			if(p.getName().equalsIgnoreCase(playerName))
			{
				player = p;
				break;
			}
		}

		if(player == null)
		{
			logger.error("player '" + playerName + "' not found");
			return;
		}
		
		Move previous = null;
		int duplicates = 0;
		for(Move current: player.getMoves())
		{
			if(current.equalsVec(previous))
				duplicates++;
			previous = current;
		}
		
		System.out.println("duplicate moves found: " + duplicates);
	}
}
