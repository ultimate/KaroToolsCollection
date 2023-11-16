package ultimate.karopapier;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.utils.PropertiesUtil;

public class Fix140310
{
	public static final boolean				ONLY_MOVE_WHEN_IDLE	= true;
	public static final int					FACTOR				= 1000;										// all following numbers in seconds
	public static final int					INTERVAL			= 1;
	public static final int					IDLE_TIME			= 5;
	public static final int					IDLE_DELTA			= 10;
	public static final double				MAX_SPEED			= 1000;
	public static final int					WOLLUST_INTERVAL	= 600;
	public static final int					WOLLUST_TOLERANCE	= 100;
	public static final int					MAX_WOLLUST			= 5000;

	/**
	 * Logger-Instance
	 */
	protected static transient final Logger	logger				= LogManager.getLogger(RandomMover.class);

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		File loginProperties = new File(args[0]);
		System.out.println(loginProperties.getAbsolutePath());
		
		Properties login = PropertiesUtil.loadProperties(loginProperties);
		
		KaroAPI api = new KaroAPI(login.getProperty("karoAPI.user"), login.getProperty("karoAPI.password"));
				
		int gid = 140310;
		Move move;

		int currentX = 244-2-3;
		int currentY = 251+1+0;
		
		System.out.println("moving...");
		move = new Move(currentX-4, currentY+0, -4, 0, "fix");
		api.move(gid, move).get();

		System.out.println("moving...");
		move = new Move(currentX-4-5, currentY-1, -5, -1, "fix");
		api.move(gid, move).get();

		System.exit(0);
	}
}
