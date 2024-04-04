package ultimate.karopapier.mapgenerator.drag;

import java.util.Arrays;
import java.util.Random;

import ultimate.karopapier.mapgenerator.MapGeneratorUtil;

public class DragMapGenerator
{
	public static char[][] generate(int players, int length, int checkpoints, double variation, boolean allowDeadEnds, boolean spotFinish, int seed)
	{
		Random random = new Random(seed);
		
		// check size
		int size = (players + 2) * (length + 3) - 1;
		int maxLength = 65536 / (players + 2) - 3;
		System.out.println("players = " + players + ", length = " + length + ", maxLength = " + maxLength + ", size = " + size+ ", spotFinish = " + spotFinish);
		if(size > 65535)
			throw new IllegalArgumentException("Maximale Länge überschritten. Bei " + players + " Spielern liegt diese bei " + maxLength);
		
		System.out.println("checkpoints = " + checkpoints + ", variation = " + variation + ", allowDeadEnds = " + allowDeadEnds + ", seed = " + seed);
		
		int finishLine = length;
		if(!spotFinish)
			finishLine -= Math.sqrt(length);
		double cpDistance = finishLine / (double) (checkpoints + 1);
				
		char[][] map = new char[players + 2][length + 2];
		// fill with background TODO use Perlin instead
		for(int y = 0; y < map.length; y++)
			for(int x = 0; x < map[y].length; x++)
				map[y][x] = 'X';
		
		// create track
		boolean[] currentSection = new boolean[players];
		char currentSymbol;
		double nextCPLine = cpDistance;
		int nextCP = 1;
		for(int x = 1; x <= length; x++)
		{
			// determine symbol
			if(x == 1)
				currentSymbol = 'S';
			else if(x == finishLine)
				currentSymbol = 'F';
			else if(x == (int) nextCPLine && x < finishLine)
			{
				currentSymbol = (char) ('0' + nextCP);
				nextCPLine += cpDistance;
				nextCP++;
				if(nextCP > 9)
					nextCP = 1;
			}
			else
				currentSymbol = 'O';
			
			// determine how the section looks like
			if(x <= 2)
				Arrays.fill(currentSection, true);
			else
			{
				boolean[] previousSection = currentSection;
				
				boolean deadends = false;
				boolean deadstarts = false;
				int tries = players*2;
				int trackFields = 0;
				do
				{
					currentSection = Arrays.copyOf(previousSection, previousSection.length);
					tries--;
					trackFields = 0;
					for(int p = 0; p < players; p++)
					{
						if(random.nextDouble() < variation)
							currentSection[p] = !previousSection[p];
						if(currentSection[p])
							trackFields++;
					}
					// check impassible
					if(trackFields == 0)
						continue;
					if(!allowDeadEnds)
					{
						// check for dead ends & starts
						deadends = false;
						for(int trackStart = 0, trackEnd = 0; trackStart < players; trackStart = ++trackEnd)
						{
							if(!previousSection[trackStart])
								continue;
							// identify the current track width
							while(trackEnd < players-1 && previousSection[trackEnd+1])
								trackEnd++;
							// check successors
							boolean hasSuccessor = false;
							// TODO optionally allow diagonal
							for(int t = trackStart; t <= trackEnd; t++)
							{
								if(currentSection[t])
									hasSuccessor = true;
							}
							if(!hasSuccessor)
							{
								deadends = true;
								break;
							}
						}
						deadstarts = false;
						for(int trackStart = 0, trackEnd = 0; trackStart < players; trackStart = ++trackEnd)
						{
							if(!currentSection[trackStart])
								continue;
							// identify the current track width
							while(trackEnd < players-1 && previousSection[trackEnd+1])
								trackEnd++;
							// check successors
							boolean hasPredecessor = false;
							// TODO optionally allow diagonal
							for(int t = trackStart; t <= trackEnd; t++)
							{
								if(previousSection[t])
									hasPredecessor = true;
							}
							if(!hasPredecessor)
							{
								deadstarts = true;
								break;
							}
						}
//						System.out.println("x = " + x + ", deadends = " + deadends + ", deadstarts = " + deadstarts);
					}
				} while((trackFields == 0 || !allowDeadEnds && (deadends || deadstarts)) && tries >= 0);
				if(tries == 0)
					System.out.println("warning: max tries reached");
			}
			
			// apply section
			for(int p = 0; p < players; p++)
			{
				if(currentSection[p])
					map[p + 1][x] = currentSymbol;
			}
		}
		
		return map;
	}
	
	public static void main(String[] args)
	{
		char[][] map;
		
		map = generate(5, 100, 15, 0.0, false, true, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 100, 15, 0.0, false, false, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 100, 15, 0.1, false, true, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 100, 15, 0.1, true, true, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(20, 100, 15, 0.1, false, true, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(10, 200, 15, 0.1, false, true, 1);
		MapGeneratorUtil.printMap(map);
		
		map = generate(10, 200, 15, 0.1, false, true, 1);
		MapGeneratorUtil.printMap(map);
		
		map = generate(10, 200, 15, 0.1, false, true, 1);
		MapGeneratorUtil.printMap(map);
		
		map = generate(10, 200, 15, 0.05, false, true, 1);
		MapGeneratorUtil.printMap(map);
	}
}
