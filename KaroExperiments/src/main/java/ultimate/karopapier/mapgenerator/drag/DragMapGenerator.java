package ultimate.karopapier.mapgenerator.drag;

import java.util.Arrays;

import ultimate.karoapi4j.utils.Kezzer;
import ultimate.karopapier.mapgenerator.MapGeneratorUtil;

public class DragMapGenerator
{
	public static char[][] generate(int players, int length, int checkpoints, double variation, int direction, boolean allowDeadEnds, boolean shutdownArea, boolean forceConnectedFinish, int seed)
	{
		Kezzer random = new Kezzer(seed);
		
		int MAX_TRACK_TRIES = (int) (10 * Math.sqrt(players));
		int START_ZONE = (int) Math.ceil(Math.log(players));
		int FINISH_ZONE = (int) Math.sqrt(length);
		
		// check size
		int height;
		int width;
		int maxLength;
		switch(direction)
		{
			case 90:
			case 270:
				height = players + 2;
				width = length + 2;
				maxLength = 65536 / height - 3;
				break;
			case 0:
			case 180:
				height = length + 2;
				width = players + 2;
				maxLength = 65536 / (width + 1) - 2;
				break;
			default:
				throw new IllegalArgumentException("Richtung muss 0, 90, 180 oder 270 Grad sein!");					
		}
		int size = height * (width + 1) - 1;
		System.out.println("players = " + players + ", length = " + length + ", direction = " + direction + ", maxLength = " + maxLength + ", size = " + size+ ", shutdownArea = " + shutdownArea);
		if(size > 65535 || length > maxLength)
			throw new IllegalArgumentException("Maximale Länge überschritten. Bei " + players + " Spielern und Richtung = " + direction + " liegt diese bei " + maxLength);
		
		System.out.println("checkpoints = " + checkpoints + ", variation = " + variation + ", allowDeadEnds = " + allowDeadEnds + ", seed = " + seed);
		
		int finishLine = length;
		if(shutdownArea)
			finishLine -= FINISH_ZONE;
		double cpDistance = finishLine / (double) (checkpoints + 1);
		if(cpDistance < 1)
			cpDistance = 1;
				
		// create track
		char[][] map = new char[height][width];
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
			
			for(int cursor = 1; cursor <= length; cursor++)
			{
				// determine symbol
				if(cursor == 1)
					currentSymbol = 'S';
				else if(cursor == finishLine)
					currentSymbol = 'F';
				else if(cursor >= nextCPLine && cursor < finishLine)
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
				if(cursor <= START_ZONE)
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
							if(random.rnd() < variation)
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
					{
						if(direction == 90)
							map[p + 1][cursor] = currentSymbol;
						else if(direction == 270)
							map[p + 1][width - 1 - cursor] = currentSymbol;
						else if(direction == 180)
							map[cursor][p + 1] = currentSymbol;
						else if(direction == 0)
							map[height - 1 - cursor][p + 1] = currentSymbol;
					}
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
		
		map = generate(5, 100, 15, 0.0, 90, false, false, false, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 100, 15, 0.0, 270, false, false, false, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 100, 15, 0.0, 0, false, false, false, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 100, 15, 0.0, 180, false, false, false, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 100, 15, 0.0, 90, false, true, false, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 100, 15, 0.1, 90, false, false, false, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 100, 15, 0.1, 90, true, false, false, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(20, 100, 15, 0.1, 90, false, false, false, 0);
		MapGeneratorUtil.printMap(map);
		
		map = generate(10, 200, 15, 0.1, 90, false, false, false, 1);
		MapGeneratorUtil.printMap(map);
		
		map = generate(10, 200, 15, 0.1, 90, false, false, false, 1);
		MapGeneratorUtil.printMap(map);
		
		map = generate(10, 200, 15, 0.1, 90, false, false, false, 1);
		MapGeneratorUtil.printMap(map);
		
		map = generate(10, 200, 999, 0.05, 90, false, false, false, 1);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 9359, 500, 0.01, 90, false, true, true, 9);
		MapGeneratorUtil.printMap(map);
		
		map = generate(5, 5000, 200, 0.01, 90, false, true, true, 2);
		MapGeneratorUtil.printMap(map);
		
		map = generate(10, 1000, 100, 0.02, 90, false, true, true, 3);
		MapGeneratorUtil.printMap(map);
	}
}
