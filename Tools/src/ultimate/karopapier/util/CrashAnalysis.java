package ultimate.karopapier.util;

import java.util.LinkedList;
import java.util.List;

public abstract class CrashAnalysis
{
	public static void main(String[] args)
	{		
	}
	
	public static void printMoveList(List<Move> moves)
	{
		for(Move move: moves)
		{
			System.out.println(move);
		}
	}
	
	public static void printMoveListShort(List<Move> moves)
	{
		for(Move move: moves)
		{
			if(move.crash)
				System.out.print("CRASH ");
			System.out.print(move.x_pos + ", ");
		}
		System.out.println("");
	}

	public static List<Move> generateTestCase(int[] movesBetweenCrashs, int aussetzen)
	{
		int id = 0;
		int counter = 0;
		List<Move> moves = new LinkedList<Move>();
		moves.add(new Move(id++, 0, 0, 0, 0, false)); // start
		for(int m : movesBetweenCrashs)
		{
			for(int i = 0; i < m; i++)
			{
				counter++;
				moves.add(new Move(id++, counter, counter, 1, 1, false)); // zug
			}
			moves.add(crashMove(moves, aussetzen)); // crash
			counter = moves.get(moves.size()-1).x_pos;
		}
		return moves;
	}
	
	public static Move crashMove(List<Move> moves, int aussetzen)
	{
		List<Move> res = new LinkedList<Move>(moves);
		int maxmoves = res.size(); // anzahl aller züge
		if(aussetzen > maxmoves)
		{
//			System.out.println("Steht nur zum Testen hier -- zzz > zuege");
			aussetzen = maxmoves - 1;
		}
//		System.out.println("Steht nur zum Testen hier -- Aussetzen: " + aussetzen);
		// int oberaussetzen = aussetzen;
		int walker = 0;
		int i = aussetzen;
		Move row = null;
		int sum;
		while(i >= 0)
		{
//			System.out.println("Steht nur zum Testen hier -- " + i + " : " + aussetzen);
			row = res.remove(res.size() - 1); // $row=mysql_fetch_array($res);
//			System.out.println("Steht nur zum Testen hier -- " + row.x_pos + ":" + row.y_pos);
			if(row.crash == true)
			{
				sum = aussetzen + walker + i;
//				System.out.println("Zeig: " + sum + " >= " + maxmoves);
				if(sum >= maxmoves)
					i = maxmoves - walker - 1;
				else
					i = i + aussetzen;
				// oberaussetzen = oberaussetzen + aussetzen;
			}
			i--;
			walker++;
//			System.out.println("Count: " + walker);
		}
		return new Move(maxmoves-1, row.x_pos, row.y_pos, 0, 0, true);
	}

	private static class Move
	{
		private int		id;
		private int		x_pos;
		private int		y_pos;
		private int		x_vec;
		private int		y_vec;
		private boolean	crash;

		public Move(int id, int xPos, int yPos, int xVec, int yVec, boolean crash)
		{
			this.id = id;
			x_pos = xPos;
			y_pos = yPos;
			x_vec = xVec;
			y_vec = yVec;
			this.crash = crash;
		}

		@Override
		public String toString()
		{
			return "Move [id=" + id + ", x_pos=" + x_pos + ", y_pos=" + y_pos + ", x_vec=" + x_vec + ", y_vec=" + y_vec + ", crash=" + crash + "]";
		}
	}
}
