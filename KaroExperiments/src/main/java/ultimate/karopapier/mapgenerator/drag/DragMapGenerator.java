package ultimate.karopapier.mapgenerator.drag;

import java.util.Arrays;
import java.util.Random;

import ultimate.karopapier.mapgenerator.MapGeneratorUtil;

public class DragMapGenerator
{
	public static char[][] generate(int players, int length, int checkpoints, double variation, boolean safeStart, boolean allowDeadEnds, boolean spotFinish, boolean forceConnectedFinish, int seed)
	{
		Random random = new Random(seed);
		
		int MAX_TRACK_TRIES = (int) (10 * Math.sqrt(players));
		int SAFE_START_ZONE = (safeStart ? (int) (2 * Math.sqrt(players)) : 2);
		
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
		if(cpDistance < 1)
			cpDistance = 1;
				
		// create track
		char[][] map = new char[players + 2][length + 2];
		boolean[] currentSection = new boolean[players];
		char currentSymbol;
		boolean finishConnected = true;
		double nextCPLine;
		int nextCP;
		int trackTries = MAX_TRACK_TRIES;
		do
		{
			nextCPLine = cpDistance;
			nextCP = 1;
			
			// fill with background TODO use Perlin instead
			for(int y = 0; y < map.length; y++)
				for(int x = 0; x < map[y].length; x++)
					map[y][x] = 'X';
			
			for(int x = 1; x <= length; x++)
			{
				// determine symbol
				if(x == 1)
					currentSymbol = 'S';
				else if(x == finishLine)
					currentSymbol = 'F';
				else if(x >= nextCPLine && x < finishLine)
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
				if(x <= SAFE_START_ZONE)
					Arrays.fill(currentSection, true);
				else
				{
					boolean[] previousSection = currentSection;
					
					boolean deadends = false;
					boolean deadstarts = false;
					int sectionTries = players*2;
					int trackFields = 0;
					do
					{
						currentSection = Arrays.copyOf(previousSection, previousSection.length);
						sectionTries--;
						trackFields = 0;
						for(int p = 0; p < players; p++)
						{
							if(random.nextDouble() < variation)
								currentSection[p] = !previousSection[p];
							if(currentSection[p])
								trackFields++;
						}
						// check impassible // TODO there can be other cases where one track starts and another ends
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
								while(trackEnd < players-1 && currentSection[trackEnd+1])
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
					} while((trackFields == 0 || (!allowDeadEnds && (deadends || deadstarts))) && sectionTries >= 0);
					if(sectionTries == 0)
						System.out.println("warning: max sectionTries reached");
				}
				
				// apply section
				for(int p = 0; p < players; p++)
				{
					if(currentSection[p])
						map[p + 1][x] = currentSymbol;
				}
			}
			
			// check finish connected 
			if(forceConnectedFinish)
			{
				int connectedFinishesFound = 0;
				for(int p = 0; p < players; p++)
				{
					if(map[p+1][finishLine] == 'F' && map[p][finishLine] != 'F')
						connectedFinishesFound++;
				}
				finishConnected = (connectedFinishesFound == 1);
				if(!finishConnected)
				{
					if(trackTries == MAX_TRACK_TRIES)
						System.out.print("finish not connected - retrying.");
					else
						System.out.print(".");
				}
				else if(trackTries < MAX_TRACK_TRIES)
					System.out.println("");
			}
			
			trackTries--;
		} while(forceConnectedFinish && !finishConnected && trackTries >= 0);
		if(trackTries == 0 && !finishConnected)
			System.out.println("warning: max trackTries reached");
		
		return map;
	}
	
	public static void main(String[] args)
	{
		char[][] map;
		
		map = generate(5, 100, 15, 0.0, false, false, true, false, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 100, 15, 0.0, false, false, false, false, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 100, 15, 0.1, false, false, true, false, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 100, 15, 0.1, true, true, true, false, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(20, 100, 15, 0.1, false, false, true, false, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(10, 200, 15, 0.1, false, false, true, false, 1);
		MapGeneratorUtil.printMap(map);
		
		map = generate(10, 200, 15, 0.1, false, false, true, false, 1);
		MapGeneratorUtil.printMap(map);
		
		map = generate(10, 200, 15, 0.1, false, false, true, false, 1);
		MapGeneratorUtil.printMap(map);
		
		map = generate(10, 200, 999, 0.05, false, false, true, false, 1);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 9359, 500, 0.01, false, false, false, true, 9);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 5000, 200, 0.01, false, false, false, true, 2);
		MapGeneratorUtil.printMap(map);
		
		map = generate(10, 1000, 100, 0.02, true, false, false, true, 3);
		MapGeneratorUtil.printMap(map);
	}
}
