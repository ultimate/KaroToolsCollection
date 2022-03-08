package ultimate.karopapier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Move;
import ultimate.karoapi4j.model.official.Player;

public class RandomMover
{
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		String user = args[0];
		String pw = args[1];
		int gid = Integer.parseInt(args[2]);
		int maxSpeed = Integer.parseInt(args[3]);

		KaroAPI api = new KaroAPI(user, pw);
		int uid = api.check().get().getId();

		long sleep = 1000;
		Random rand = new Random();

		Game game;
		Player player;
		Move move;
		List<Move> validMoves = new ArrayList<>(9);

		do
		{
			game = api.getGame(gid).get();
			if(game.getNext() == null || game.getNext().getId() != uid)
				return;

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
				api.refreshAfterCrash(gid);

			validMoves.clear();
			for(Move m : player.getPossibles())
			{
				if(speed(m) <= maxSpeed)
					validMoves.add(m);
			}
			if(validMoves.size() == 0)
				validMoves.addAll(player.getPossibles());

			move = validMoves.get(rand.nextInt(validMoves.size()));
			System.out.println("moving: x=" + move.getX() + " y=" + move.getY() + " xvec=" + move.getXv() + " yvec=" + move.getYv());
			api.move(gid, move);
			Thread.sleep(sleep);
		} while(true);
	}

	private static double speed(Move m)
	{
		return Math.sqrt(m.getXv() * m.getXv() + m.getYv() * m.getYv());
	}
}
