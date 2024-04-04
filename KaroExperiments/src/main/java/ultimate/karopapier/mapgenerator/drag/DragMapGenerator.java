package ultimate.karopapier.mapgenerator.drag;

import java.util.Arrays;
import java.util.Random;

import ultimate.karopapier.mapgenerator.MapGeneratorUtil;

public class DragMapGenerator
{
	public static char[][] generate(int players, int length, int checkpoints, double variation, int variationLines, boolean allowDeadEnds, boolean spotFinish, int seed)
	{
		Random random = new Random(seed);
		
		// check size
		int size = (players + 2) * (length + 3) - 1;
		int maxLength = 65536 / (players + 2) - 3;
		System.out.println("players = " + players + ", length = " + length + ", maxLength = " + maxLength + ", size = " + size);
		if(size > 65535)
			throw new IllegalArgumentException("Maximale Länge überschritten. Bei " + players + " Spielern liegt diese bei " + maxLength);
		
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
				int tries = 5;
				do
				{
					currentSection = Arrays.copyOf(previousSection, previousSection.length);
					tries--;
					if(variationLines > 0)
					{
						int lineToVary;
						for(int i = 0; i < variationLines; i++)
						{
							if(random.nextDouble() < variation)
							{
								lineToVary = random.nextInt(players);
								// check if already varied
								if(currentSection[lineToVary] != previousSection[lineToVary])
									continue;
								currentSection[lineToVary] = !previousSection[lineToVary];
							}
						}
					}
					else
					{
						for(int p = 0; p < players; p++)
						{
							if(random.nextDouble() < variation)
								currentSection[p] = !previousSection[p];
						}
					}
					if(!allowDeadEnds)
					{
						// check for dead ends & starts
						deadends = false;
						deadstarts = false;
						for(int p = 0; p < players; p++)
						{
							// continuous track
							if(previousSection[p] && currentSection[p])
								continue;
							// continuous grass
							if(!previousSection[p] && !currentSection[p])
								continue;
							// TODO multiple tracks ending at the same moment
							// previous track between two grass fields
							if(previousSection[p] && (p == 0 || !previousSection[p-1]) && (p == players-1 || !previousSection[p+1]))
							{
								// all 3 successors are grass
								if(!currentSection[p] && (p == 0 || !currentSection[p-1]) && (p == players-1 || !currentSection[p+1]))
									deadends = true;
							}
							// current track between two grass fields
							if(currentSection[p] && (p == 0 || !currentSection[p-1]) && (p == players-1 || !currentSection[p+1]))
							{
								// all 3 predecessors are grass
								if(!previousSection[p] && (p == 0 || !previousSection[p-1]) && (p == players-1 || !previousSection[p+1]))
									deadstarts = true;
							}
						}
						System.out.println("x = " + x + ", deadends = " + deadends + ", deadstarts = " + deadstarts);
					}
				} while(!allowDeadEnds && (deadends || deadstarts) && tries >= 0);
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
		
		map = generate(5, 100, 15, 0.0, 0, false, true, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 100, 15, 0.0, 0, false, false, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 100, 15, 0.1, 1, false, true, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 100, 15, 0.1, 1, true, true, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(20, 100, 15, 0.1, 2, false, true, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(10, 200, 15, 0.1, 2, false, true, 1);
		MapGeneratorUtil.printMap(map);
		
		map = generate(10, 200, 15, 0.1, 1, false, true, 1);
		MapGeneratorUtil.printMap(map);
		
		map = generate(10, 200, 15, 0.1, 0, false, true, 1);
		MapGeneratorUtil.printMap(map);
		
		map = generate(10, 200, 15, 0.05, 0, false, true, 1);
		MapGeneratorUtil.printMap(map);
	}
}
