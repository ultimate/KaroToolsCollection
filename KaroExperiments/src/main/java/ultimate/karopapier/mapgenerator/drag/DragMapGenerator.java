package ultimate.karopapier.mapgenerator.drag;

import java.util.Arrays;

import ultimate.karoapi4j.utils.Kezzer;
import ultimate.karopapier.mapgenerator.MapGeneratorUtil;

public class DragMapGenerator
{
	private static final boolean	FORCE_CONNECTED_FINISH	= true;
	private static final boolean	ALLOW_DEAD_ENDS			= false;
	private static final int		triesSectionMax			= 100;
	private static final int		triesTrackMax			= 100;
	private static final int		MAX_SIZE				= 65535;

	public static char[][] generate(int players, int length, int checkpoints, double variation, int direction, String seed)
	{
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
				maxLength = (MAX_SIZE + 1) / height - 3;
				break;
			case 0:
			case 180:
				height = length + 2;
				width = players + 2;
				maxLength = (MAX_SIZE + 1) / (width + 1) - 2;
				break;
			default:
				throw new IllegalArgumentException("Richtung muss 0, 90, 180 oder 270 Grad sein!");
		}
		int size = height * (width + 1) - 1;
		System.out.println(
				"players = " + players + ", length = " + length + ", direction = " + direction + ", maxLength = " + maxLength + ", size = " + size);
		if(size > MAX_SIZE || length > maxLength)
			throw new IllegalArgumentException(
					"Maximale Länge überschritten. Bei " + players + " Spielern und Richtung = " + direction + " liegt diese bei " + maxLength);

		System.out.println("checkpoints = " + checkpoints + ", variation = " + variation + ", seed = " + seed);

		Kezzer random = new Kezzer(seed);
		
		int startZone = (int) Math.ceil(Math.log(players));
		int finishZone = (int) Math.sqrt(length);
		int finishLine = length - finishZone;
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
		int triesTrack = Math.min((int) (10 * Math.sqrt(players)), triesTrackMax);
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
				if(cursor <= startZone)
					Arrays.fill(currentSection, true);
				else
				{
					boolean[] previousSection = currentSection;

					boolean deadends = false;
					boolean deadstarts = false;
					int sectionTries = Math.min(triesSectionMax, players * 2);
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
						// check impassible
						if(trackFields == 0)
							continue;
						if(!ALLOW_DEAD_ENDS)
						{
							// check for dead ends & starts
							deadends = hasDeadEnds(currentSection, previousSection);
							deadstarts = hasDeadStarts(currentSection, previousSection);
						}
					} while((trackFields == 0 || (!ALLOW_DEAD_ENDS && (deadends || deadstarts))) && sectionTries >= 0);
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
			if(FORCE_CONNECTED_FINISH)
			{
				int connectedFinishesFound = 0;
				char f1 = 'X', f2 = 'X';
				for(int p = 0; p < players; p++)
				{
					if(direction == 90)
					{
						f1 = map[p][finishLine];
						f2 = map[p + 1][finishLine];
					}
					else if(direction == 270)
					{
						f1 = map[p][width - 1 - finishLine];
						f2 = map[p + 1][width - 1 - finishLine];
					}
					else if(direction == 180)
					{
						f1 = map[finishLine][p];
						f2 = map[finishLine][p + 1];
					}
					else if(direction == 0)
					{
						f1 = map[height - 1 - finishLine][p];
						f2 = map[height - 1 - finishLine][p + 1];
					}

					if(f2 == 'F' && f1 != 'F')
						connectedFinishesFound++;
				}
				finishConnected = (connectedFinishesFound == 1);
				// if(!finishConnected)
				// {
				// if(triesTrack == triesTrackInit)
				// System.out.print("finish not connected - retrying.");
				// else
				// System.out.print(".");
				// }
				// else if(triesTrack < triesTrackInit)
				// System.out.println("");
			}

			triesTrack--;
		} while(FORCE_CONNECTED_FINISH && !finishConnected && triesTrack >= 0);
		// if(triesTrack == 0 && !finishConnected)
		// System.out.println("warning: max trackTries reached");

		return map;
	}
	
	private static boolean hasDeadEnds(boolean[] currentSection, boolean[] previousSection)
	{
		for(int trackStart = 0, trackEnd = 0; trackStart < previousSection.length; trackStart = ++trackEnd)
		{
			if(!previousSection[trackStart])
				continue;
			// identify the current track width
			while(trackEnd < previousSection.length - 1 && previousSection[trackEnd + 1])
				trackEnd++;
			// check successors
			boolean hasSuccessor = false;
			for(int t = trackStart; t <= trackEnd; t++)
			{
				if(currentSection[t])
					hasSuccessor = true;
			}
			if(!hasSuccessor)
				return true;
		}
		return false;
	}
	
	private static boolean hasDeadStarts(boolean[] currentSection, boolean[] previousSection)
	{
		for(int trackStart = 0, trackEnd = 0; trackStart < currentSection.length; trackStart = ++trackEnd)
		{
			if(!currentSection[trackStart])
				continue;
			// identify the current track width
			while(trackEnd < currentSection.length - 1 && currentSection[trackEnd + 1])
				trackEnd++;
			// check successors
			boolean hasPredecessor = false;
			for(int t = trackStart; t <= trackEnd; t++)
			{
				if(previousSection[t])
					hasPredecessor = true;
			}
			if(!hasPredecessor)
				return true;
		}
		return false;
	}

	public static void main(String[] args)
	{
		char[][] map;

		// default
		map = generate(5, 500, 9, 0.01, 90, "test");
		MapGeneratorUtil.printMap(map);

		// parameterized 01
		map = generate(5, 100, 15, 0.0, 270, "0");
		MapGeneratorUtil.printMap(map);

		// parameterized 02
		map = generate(5, 100, 15, 0.0, 0, "1");
		MapGeneratorUtil.printMap(map);

		// parameterized 03
		map = generate(5, 100, 15, 0.0, 180, "2");
		MapGeneratorUtil.printMap(map);

		// parameterized 04
		map = generate(10, 200, 999, 0.05, 90, "3");
		MapGeneratorUtil.printMap(map);

		// map = generate(5, 100, 15, 0.0, 90, 0);
		// MapGeneratorUtil.printMap(map);
		//
		// map = generate(5, 100, 15, 0.1, 90, 0);
		// MapGeneratorUtil.printMap(map);
		//
		// map = generate(5, 100, 15, 0.1, 90, 0);
		// MapGeneratorUtil.printMap(map);
		//
		// map = generate(20, 100, 15, 0.1, 90, 0);
		// MapGeneratorUtil.printMap(map);
		//
		// map = generate(10, 200, 15, 0.1, 90, 1);
		// MapGeneratorUtil.printMap(map);
		//
		// map = generate(10, 200, 15, 0.1, 90, 1);
		// MapGeneratorUtil.printMap(map);
		//
		// map = generate(10, 200, 15, 0.1, 90, 1);
		// MapGeneratorUtil.printMap(map);
		//
		//
		// map = generate(5, 9359, 500, 0.01, 90, 9);
		// MapGeneratorUtil.printMap(map);
		//
		// map = generate(5, 5000, 200, 0.01, 90, 2);
		// MapGeneratorUtil.printMap(map);
		//
		// map = generate(10, 1000, 100, 0.02, 90, 3);
		// MapGeneratorUtil.printMap(map);
	}
}
