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
				
		plan147155(api);

		System.exit(0);
	}
	
	public static void plan147095(KaroAPI api) throws InterruptedException, ExecutionException
	{
		int gid = 147095;

		// update planned moves
		List<Move> plannedMoves = new ArrayList<>();
		// turns
		for(int y = 80; y > 2; y -= 3)
		{
			// turn on the right
			if(y < 77)
			{
				plannedMoves.add(new Move(252, y, 1, 0, null));
				plannedMoves.add(new Move(253, y, 1, 0, null));
				plannedMoves.add(new Move(253, y-1, 0, -1, null));
				plannedMoves.add(new Move(252, y-1, -1, 0, null));
				plannedMoves.add(new Move(252, y, 0, 1, null));
			}
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
	
	public static void plan147155(KaroAPI api) throws InterruptedException, ExecutionException
	{
		int gid = 147155;

		// update planned moves
		List<Move> plannedMoves = new ArrayList<>();
		
		// long 2/2 moves - right side
		for(int y = 4; y < 229; y += 8)
		{
			plannedMoves.add(new Move(282, y, 2, 2, null));
			plannedMoves.add(new Move(280, y-2, -2, -2, null));
			plannedMoves.add(new Move(280, y+2, -2, 2, null));
			plannedMoves.add(new Move(282, y, 2, -2, null));
		}
		// long 2/2 moves - left side
		for(int y = 8; y < 32; y += 8)
		{
			plannedMoves.add(new Move(11, y, -2, 2, null));
			plannedMoves.add(new Move(13, y-2, 2, -2, null));
			plannedMoves.add(new Move(13, y+2, 2, 2, null));
			plannedMoves.add(new Move(11, y, -2, -2, null));
		}		
		for(int y = 32; y < 229; y += 8)
		{
			plannedMoves.add(new Move(2, y, -2, 2, null));
			plannedMoves.add(new Move(4, y-2, 2, -2, null));
			plannedMoves.add(new Move(4, y+2, 2, 2, null));
			plannedMoves.add(new Move(2, y, -2, -2, null));
		}	
		
		// bottom right corner
		plannedMoves.add(new Move(282, 227, 1, 0, null));
		plannedMoves.add(new Move(283, 227, 1, 0, null));
		
		// TODO path to next CP
		
		api.addPlannedMoves(gid, plannedMoves).get();
	}
}
