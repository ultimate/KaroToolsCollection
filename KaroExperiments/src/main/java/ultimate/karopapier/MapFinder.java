package ultimate.karopapier;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.utils.PropertiesUtil;

public class MapFinder
{
	/**
	 * Logger-Instance
	 */
	protected static transient final Logger logger = LogManager.getLogger(MapFinder.class);
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		File loginProperties = new File(args[0]);
		int DELAY = 5000;
		
		// initialize karoapi
		Properties login = PropertiesUtil.loadProperties(loginProperties);
		KaroAPI api = new KaroAPI(login.getProperty("karoAPI.user"), login.getProperty("karoAPI.password"));

		logger.info("--------------------------------------------------------");
		
		// go through users and
		logger.info("loading standard maps...");
		List<Map> maps = api.getMaps(true).get();
		logger.info("maps found = " + maps.size());
		logger.info("identifying missing maps...");
		List<Integer> missingMaps = new LinkedList<Integer>();
		int previousMID = 10000;
		for(Map m: maps) {
			if(m.getId() <= previousMID)
				continue;
			if(m.getId() - previousMID > 1) {
				for(int mid = previousMID + 1; mid < m.getId(); mid++)
					missingMaps.add(mid);
			}
			previousMID = m.getId();
		}
		logger.info("maps missing = " + missingMaps.size());
		logger.info(missingMaps);
		logger.info("loading missing maps...");
		int limit = Integer.MAX_VALUE;
		int i = 0;
		for(int mid: missingMaps) {
			maps.add(api.getMap(mid, true).get());
			if(++i % 100 == 0) {
				logger.debug(i + "/" + missingMaps.size());
				// quick break
				Thread.sleep(DELAY);
			}
			if(limit-- <= 0)
				break;
		}
		logger.info("maps in total = " + maps.size());

		logger.info("analyzing & sorting maps...");
		// convert to array to not process map code to often
		Function<Map, Object[]> mapToRow = (m) -> {
			return new Object[] {
				null,
				m.getId(),
				count(m.getCode(), '2') // put the value to look for here
			};
		};
		// sort and index
		int SORT_COL = 2;
		AtomicInteger rank = new AtomicInteger(1);
		Stream<Object[]> rows = maps.stream().map(mapToRow).sorted(
				(r1, r2) -> (int) r2[SORT_COL] - (int) r1[SORT_COL]
		).map(r -> { r[0] = rank.getAndIncrement(); return r; });

		// print result
		Object[] headers = new String[] {
				"Rank ",
				"MID  ", 
				"Count"
		};
		printRow(headers, headers);
		printRow(null, headers);
		rows.forEach(r -> printRow(r, headers));
		
		System.exit(0);
	}
	
	private static void printRow(Object[] row, Object[] headers) {
		if(row != null && row.length != headers.length)
			throw new IllegalArgumentException("row and headers must have same length");
		for(int col = 0; col < headers.length; col++) {
			if(row != null) {
				if(col > 0)
					System.out.print(" | ");
				System.out.print(pad("" + row[col], ("" + headers[col]).length(), true));
			} else {
				if(col > 0)
					System.out.print("-|-");
				for(int h = 0; h < ("" + headers[col]).length(); h++)
					System.out.print("-");
			}
		}
		System.out.println();
	}
	
	
	private static int count(String s, char c) {
		int count = 0;
		for(int i = 0; i < s.length(); i++) {
			if(s.charAt(i) == c)
				count++;
		}
		return count;
	}
	
	private static String pad(String input, int characters, boolean prefix) {
		String s = "" + input;
		while(s.length() < characters) {
			if(prefix) 
				s = " " + s;
			else
				s += " ";
		}
		return s;
	}
}
