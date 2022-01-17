package ultimate.karopapier.util;

import java.io.IOException;
import java.net.URL;
import java.util.Random;

import muskel2.core.karoaccess.KaropapierLoader;

public class RandomMover
{
	public static void main(String[] args) throws IOException, InterruptedException
	{
		String user = args[0];
		String pw = args[1];
		int gid = Integer.parseInt(args[2]);
		long sleep = 1000;
		Random rand = new Random();
		if(KaropapierLoader.login(user, pw))
		{
			System.out.println("logged in as " + user);
			
			@SuppressWarnings("unused")
			String page;
			int x = 0;
			int y = 0;
			int r;
			do
			{
				r = rand.nextInt(3)-1;
				if(x == 0)
				{
					if(r != 0)
					{
						x = r;
						y = 0;
					}
				}
				else if(y == 0)
				{
					if(r != 0)
					{
						y = r;
						x = 0;
					}
				}
				page = KaropapierLoader.readPage(new URL("http://www.karopapier.de/move.php"), "GID=" + gid + "&xvec=" + x + "&yvec=" + y);
				System.out.println("moved: x=" + x + " y=" + y);
				Thread.sleep(sleep);
			}
			while(true);
		}
		else
		{
			System.out.println("could not log in...");
		}
	}
}
