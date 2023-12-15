package ultimate.karopapier;

import java.io.File;
import java.io.IOException;
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
				
		int gid = 140311;
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

		// try every move
		for(Move m: player.getPossibles())
		{
			logger.info("moving... " + m);
			logger.info("  -> " + api.move(gid, m).get());
		}

		System.exit(0);
	}
}
