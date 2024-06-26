package ultimate.karopapier;

import java.awt.HeadlessException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karoapi4j.utils.URLLoader;

public class RandomMover
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
		int gid = Integer.parseInt(args[1]);

		Properties login = PropertiesUtil.loadProperties(loginProperties);

		KaroAPI api = new KaroAPI(login.getProperty("karoAPI.user"), login.getProperty("karoAPI.password"));
		KaroAPICache cache = new KaroAPICache(api, login);
		cache.refresh().join();

		User user = api.check().get();
		int uid = user.getId();
		String username = user.getLogin();

		logger.info("loading game: " + gid);

		Random rand = new Random();

		Thread t = new Thread() {
			public void run()
			{
				Point lastP, newP;
				int unchangedCount = 0;

				lastP = MouseInfo.getPointerInfo().getLocation();

				Game game;
				Player player;
				Move move;
				int wollust = 0;
				int movesSinceLastWollustCheck = WOLLUST_TOLERANCE;

				while(true)
				{
					try
					{
						if(movesSinceLastWollustCheck >= WOLLUST_TOLERANCE)
						{
							wollust = getWollust(username);
							movesSinceLastWollustCheck = 0;
						}
						// only move, when wollust is not too high 
						if(wollust < MAX_WOLLUST)
						{
							newP = MouseInfo.getPointerInfo().getLocation();
							// TODO also look for keyboard
							if(newP.equals(lastP))
							{
								if(unchangedCount >= IDLE_TIME || !ONLY_MOVE_WHEN_IDLE)
								{
									game = api.getGameWithDetails(gid).get();
									if(game.isFinished())
									{
										logger.info("wollust=" + wollust + " | game is finished --> exiting");
										return;
									}
									else if(game.getNext() == null || game.getNext().getId() != uid)
									{
										logger.info("wollust=" + wollust + " | idle detected --> it's not my turn...");
										continue;
									}
									else
									{
										player = null;
										for(Player p : game.getPlayers())
										{
											if(p.getId() == uid)
											{
												player = p;
												break;
											}
										}
										if(player == null)
											return;
	
										if(player.getPossibles() == null || player.getPossibles().size() == 0)
										{
											logger.info("wollust=" + wollust + " | idle detected --> crashing...");
											api.refreshAfterCrash(gid).get();
											continue;
										}
	
										player.getPossibles().removeIf(m -> {
											return speed(m) > MAX_SPEED;
										});
	
										move = player.getPossibles().get(rand.nextInt(player.getPossibles().size()));
										logger.info("wollust=" + wollust + " | idle detected --> moving: x=" + move.getX() + " y=" + move.getY() + " xvec=" + move.getXv() + " yvec=" + move.getYv());
										api.move(gid, move);
										
										movesSinceLastWollustCheck++;
										wollust++;
									}
								}
								unchangedCount++;
							}
							else
							{
								if(unchangedCount > 0)
									logger.debug("wollust=" + wollust + " | not idle");
								unchangedCount = 0;
								lastP = newP;
							}
							Thread.sleep(INTERVAL * FACTOR);
						}
						else
						{
							logger.debug("wollust=" + wollust + " | wollust too high --> sleeping for " + WOLLUST_INTERVAL);
							Thread.sleep(WOLLUST_INTERVAL * FACTOR);
							movesSinceLastWollustCheck = WOLLUST_TOLERANCE;
						}
					}
					catch(InterruptedException e)
					{
						e.printStackTrace();
					}
					catch(HeadlessException e)
					{
						e.printStackTrace();
					}
					catch(ExecutionException e)
					{
						e.printStackTrace();
					}
					catch(NullPointerException e)
					{
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
		t.join();
		System.exit(0);
	}

	private static double speed(Move m)
	{
		return Math.sqrt(m.getXv() * m.getXv() + m.getYv() * m.getYv());
	}
	
	private static int getWollust(String username)
	{
		URLLoader loader = new URLLoader("https://www.karopapier.de/addicts");
		String addicts = loader.doGet().get();
		int wollustStart = addicts.indexOf("<th>Züge</th>");
		wollustStart = addicts.indexOf("<td>" + username + "</td>", wollustStart + 1);
		if(wollustStart == -1)
		{
			wollustStart = addicts.indexOf("<td>35</td>", wollustStart + 1);
			wollustStart = addicts.indexOf("<td>", wollustStart + 1);
		}
		wollustStart = addicts.indexOf("<td>", wollustStart + 1) + "<td>".length();
		int wollustEnd = addicts.indexOf("</td>", wollustStart);
		String wollust = addicts.substring(wollustStart, wollustEnd);
		return Integer.parseInt(wollust);
	}
}
