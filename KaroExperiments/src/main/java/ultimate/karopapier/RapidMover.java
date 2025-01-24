package ultimate.karopapier;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
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

public class RapidMover
{
	/**
	 * Logger-Instance
	 */
	protected static transient final Logger	logger				= LogManager.getLogger(RapidMover.class);

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		File loginProperties = new File(args[0]);
		int gid = Integer.parseInt(args[1]);
		int xv = ("?".equals(args[2]) ? Integer.MAX_VALUE : Integer.parseInt(args[2]));
		int yv = ("?".equals(args[3]) ? Integer.MAX_VALUE : Integer.parseInt(args[3]));

		Properties login = PropertiesUtil.loadProperties(loginProperties);

		KaroAPI api = new KaroAPI(login.getProperty("karoAPI.user"), login.getProperty("karoAPI.password"));
		KaroAPICache cache = new KaroAPICache(api, login);
		cache.refresh().join();

		User user = api.check().get();
		int uid = user.getId();

		logger.info("loading game: " + gid);

		Thread t = new Thread() {
			public void run()
			{
				Game game;
				Player player;
				Move move;

				while(true)
				{
					try
					{
						game = api.getGameWithDetails(gid).get();
						if(game.isFinished())
						{
							logger.info("game is finished --> exiting");
							return;
						}
						else if(game.getNext() == null || game.getNext().getId() != uid)
						{
							logger.info("it's not my turn... --> exiting");
							continue;
						}
						else
						{
							player = null;
							move = null;
							
							for(Player p : game.getPlayers())
							{
								if(p.getId() == uid)
								{
									player = p;
									break;
								}
							}
							if(player == null)
							{
								logger.info("I don't participate... --> exiting");
								return;
							}

							if(player.getPossibles() == null || player.getPossibles().size() == 0)
							{
								logger.info("crashing...");
								api.refreshAfterCrash(gid).get();
								continue;
							}
							
							if(player.getPossibles().size() == 1)
							{
								move = player.getPossibles().get(0);
							}
							else
							{
								for(Move m: player.getPossibles())
								{
									if((xv == Integer.MAX_VALUE || xv == m.getXv()) && (yv == Integer.MAX_VALUE || yv == m.getYv()))
									{
										move = m;
										break;
									}
								}
							}
							
							if(move == null)
							{
								logger.info("no matching move found...");
								return;
							}
							
							logger.info("moving: x=" + move.getX() + " y=" + move.getY() + " xvec=" + move.getXv() + " yvec=" + move.getYv());
							api.move(gid, move);
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
}
