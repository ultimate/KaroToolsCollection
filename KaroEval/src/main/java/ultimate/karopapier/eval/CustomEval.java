package ultimate.karopapier.eval;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karoapi4j.utils.threads.ThreadQueue;
import ultimate.karoapi4j.utils.web.urlloaders.StringURLLoaderThread;
import ultimate.karopapier.eval.model.GameResult;
import ultimate.karopapier.eval.model.PlayerRecord;
import ultimate.karopapier.eval.model.PlayerResult;
import ultimate.karopapier.eval.model.TableRecord;

public abstract class CustomEval
{
	/**
	 * Logger-Instance
	 */
	private static transient final Logger	logger			= LogManager.getLogger();
	
	private static List<GameResult>				allResults				= new LinkedList<GameResult>();
	private static List<GameResult>				filteredResults			= new LinkedList<GameResult>();

	private static Map<String, Double>			constants				= new TreeMap<String, Double>();
	private static boolean						cache					= true;
	private static String						cacheFolder				= "cache";
	private static int							gidFirst				= 1;
	private static int							gidMax					= 1000000;
	private static int							gidLast					= gidMax;
	private static int							threads					= 50;
	private static String						calcOrder				= "gid";
	private static int							calcInterval			= 100;
	private static String						titleRegExp				= null;
	private static Class<? extends CustomEval>	evalClass				= IQEval.class;

	private static ThreadQueue					queue;

	private static final SimpleDateFormat		dateFormat				= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final String					calcOrder_gid			= "gid";
	private static final String					calcOrder_date			= "date";
	private static final String					start_playerName_Log	= ": ";
	private static final String					end_playerName_Log		= " -> ";
	private static final String					url_gameList			= "http://www.karopapier.de/showgames.php";
	private static final String					url_log					= "http://www.karopapier.de/logs/";

	public static void main(String[] args) throws Exception
	{
		long start;

		logger.info("Initialisierung...                         ");
		start = System.currentTimeMillis();
		init(args);
		logger.info(" OK (" + ((System.currentTimeMillis() - start) / 1000.0) + "s)");

		logger.info("Ermittle höchste GID...              ");
		start = System.currentTimeMillis();
		loadMaxGID();
		logger.info(format(gidMax, 6));
		logger.info(" OK (" + ((System.currentTimeMillis() - start) / 1000.0) + "s)");

		logger.info("Lade log-Dateien...                        ");
		start = System.currentTimeMillis();
		loadLogs();
		logger.info(" OK (" + ((System.currentTimeMillis() - start) / 1000.0) + "s)");

		logger.info("Sortiere Spiele...                         ");
		start = System.currentTimeMillis();
		sortResults();
		logger.info(" OK (" + ((System.currentTimeMillis() - start) / 1000.0) + "s)");

		logger.info("Auswertung (alle Spiele)...                ");
		start = System.currentTimeMillis();
		evaluate(allResults, "result_all");
		logger.info(" OK (" + ((System.currentTimeMillis() - start) / 1000.0) + "s)");

		logger.info("Auswertung (gefilterte Spiele)...          ");
		start = System.currentTimeMillis();
		evaluate(filteredResults, "result_filtered");
		logger.info(" OK (" + ((System.currentTimeMillis() - start) / 1000.0) + "s)");
	}

	private static void init(String[] args) throws IOException, ClassNotFoundException
	{
		String properties = "iq.properties";
		if(args.length == 0 || args.length > 1)
		{
			logger.info("one single argument is required: path to your properties file (further arguments will be ignored)");
			logger.info("exampe: java -cp iq.jar eval.CustomEval \"C:\\Karo\\iq.properties\"");
		}
		readProperties(properties);

		queue = new ThreadQueue(threads, false, true);
	}

	@SuppressWarnings("unchecked")
	private static void readProperties(String file) throws IOException, ClassNotFoundException
	{
		Properties p = PropertiesUtil.loadProperties(new File(file));

		if(p.getProperty("cache.enabled") != null)
			cache = p.getProperty("cache.enabled").equalsIgnoreCase("true");
		if(p.getProperty("cache.folder") != null)
			cacheFolder = p.getProperty("cache.folder");
		if(p.getProperty("gid.first") != null)
			gidFirst = Integer.parseInt(p.getProperty("gid.first"));
		if(p.getProperty("gid.last") != null)
			gidLast = Integer.parseInt(p.getProperty("gid.last"));
		if(p.getProperty("threads") != null)
			threads = Integer.parseInt(p.getProperty("threads"));
		if(p.getProperty("calculation.order") != null)
			calcOrder = p.getProperty("calculation.order");
		if(p.getProperty("calculation.interval") != null)
			calcInterval = Integer.parseInt(p.getProperty("calculation.interval"));
		if(p.getProperty("calculation.class") != null)
			evalClass = (Class<? extends CustomEval>) Class.forName(p.getProperty("calculation.class"));
		if(p.getProperty("calculation.title.regExp") != null && !p.getProperty("calculation.title.regExp").isEmpty())
			titleRegExp = p.getProperty("calculation.title.regExp");
		
		Double constant;
		for(Object key: p.keySet())
		{
			if(((String) key).startsWith("calculation.class.constant."))
			{
				constant = Double.parseDouble(p.getProperty((String) key));
				constants.put(((String) key).substring("calculation.class.constant.".length()), constant);
			}
		}
	}

	private static void loadMaxGID() throws MalformedURLException, InterruptedException
	{
		int upperBound = gidMax;
		int lowerBound = 0;
		int limit;
		int start, end;
		String page;
		int pageSize = 25;
		StringURLLoaderThread t;
		URL url = new URL(url_gameList);
		String params = "finished_limit=10000000&games_order=gid&limit=";
		String gidS = "showmap.php?GID=";
		String andS = "&";
		String pageEnd = "Brought to you by Didi";
		while(Math.abs(upperBound - lowerBound) > pageSize)
		{
			limit = (upperBound + lowerBound) / 2;
			t = new StringURLLoaderThread(url, params + limit);
			t.start();
			t.join();
			page = t.getLoadedContent();
			page = page.substring(0, page.indexOf(pageEnd)); // chat-nachricht ausschliessen...
			start = page.lastIndexOf(gidS);
			end = page.indexOf(andS, start);
			if(start != -1)
			{
				start += gidS.length();
				gidMax = Integer.parseInt(page.substring(start, end));
			}
			if(countOccurrences(page, gidS, true) == pageSize)
			{
				lowerBound = limit;
			}
			else
			{
				upperBound = limit;
			}
		}
	}

	private static void loadLogs() throws MalformedURLException, InterruptedException
	{
		if(gidMax < gidLast)
			gidLast = gidMax;
		int added = 0;
		for(int gid = gidFirst; gid <= gidLast; gid++)
		{
			queue.addThread(new ResultThread(gid));
			queue.printCount();
			added++;
			if(added >= 2 * queue.getMax())
				queue.begin();
		}
		if(added < 2 * queue.getMax())
			queue.begin();
		queue.waitForFinished();
	}

	private static void evaluate(List<GameResult> results, String listName) throws InstantiationException, IllegalAccessException
	{
		logger.info("");

		CustomEval eval = evalClass.newInstance();
		long start;

		logger.info("  Ermittle beteiligte Spieler              ");
		start = System.currentTimeMillis();
		eval.init(results);
		logger.info(" OK (" + ((System.currentTimeMillis() - start) / 1000.0) + "s)");

		logger.info("  Berechne Wertung                         ");
		start = System.currentTimeMillis();
		eval.calculate(results);
		logger.info(" OK (" + ((System.currentTimeMillis() - start) / 1000.0) + "s)");

		logger.info("  Schreibe Spielerdateien                  ");
		start = System.currentTimeMillis();
		writeRecords(listName, eval.getPlayerRecords());
		logger.info(" OK (" + ((System.currentTimeMillis() - start) / 1000.0) + "s)");

		logger.info("  Schreibe Tabelle                         ");
		start = System.currentTimeMillis();
		writeTable(listName, eval.getTable());
		logger.info(" OK (" + ((System.currentTimeMillis() - start) / 1000.0) + "s)");

		logger.info("                                           ");
	}

	private static void writeRecords(String folder, Map<String, List<PlayerRecord>> playerRecords)
	{
		File folderFile = new File(folder);
		folderFile.mkdir();

		File file;
		StringBuilder sb;
		String valueString;
		int done = 0;
		for(Entry<String, List<PlayerRecord>> e : playerRecords.entrySet())
		{
			file = new File(folderFile, e.getKey() + ".csv");
			sb = new StringBuilder();
			for(String s : e.getValue().get(0).getLabels())
			{
				sb.append(s);
				sb.append(";");
			}
			sb.append("\n");
			for(PlayerRecord r : e.getValue())
			{
				for(Object value : r.getValues())
				{
					if(value instanceof Double)
						valueString = "" + Math.round(((Double) value) * 100.0) / 100.0;
					else if(value instanceof Date)
						valueString = format((Date) value);
					else
						valueString = value.toString();
					sb.append(valueString);
					sb.append(";");
				}
				sb.append("\n");
			}
			writeFile(file, sb.toString(), true);
			printCount(++done, playerRecords.size());
		}
	}

	private static void writeTable(String folder, List<TableRecord> list)
	{
		File folderFile = new File(folder);
		folderFile.mkdir();

		File file = new File(folderFile, "_table.csv");
		StringBuilder sb;
		String valueString;
		sb = new StringBuilder();
		sb.append("Spieler;");
		for(String s : list.get(0).getLabels())
		{
			sb.append(s);
			sb.append(";");
		}
		sb.append("\n");
		for(TableRecord r : list)
		{
			sb.append(r.getPlayer());
			sb.append(";");
			for(Object value : r.getValues())
			{
				if(value instanceof Double)
					valueString = "" + Math.round(((Double) value) * 100.0) / 100.0;
				else if(value instanceof Date)
					valueString = format((Date) value);
				else
					valueString = value.toString();
				sb.append(valueString);
				sb.append(";");
			}
			sb.append("\n");
		}
		writeFile(file, sb.toString(), true);
	}

	private static GameResult parseGame(int gid, String log)
	{
		try
		{
			List<String> players = getPlayersFromLog(log);
			GameResult result = createResultFromLog(gid, log, players);
			return result;
		}
		catch(Exception e)
		{
			logger.info("Error parsing game gid=" + gid);
			e.printStackTrace();
			return new GameResult(gid, "ERROR", null, null);
		}
	}

	private static String readCache(int gid)
	{
		File file = cacheFile(gid);
		if(!file.exists())
			return null;
		try
		{
			StringBuilder sb = new StringBuilder();
			InputStream is = new BufferedInputStream(new FileInputStream(file));
			int c;
			while((c = is.read()) != -1)
			{
				sb.append((char) c);
			}
			is.close();
			return sb.toString();
		}
		catch(Exception e)
		{
			return null;
		}
	}

	private static void writeCache(int gid, String log)
	{
		writeFile(cacheFile(gid), log, false);
	}

	private static void writeFile(File file, String content, boolean overwrite)
	{
		if(file.exists() && !overwrite)
			return;
		if(!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		try
		{
			OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
			for(int i = 0; i < content.length(); i++)
				os.write(content.charAt(i));
			os.flush();
			os.close();
		}
		catch(Exception e)
		{
			return;
		}
	}

	private static File cacheFile(int gid)
	{
		return new File(cacheFolder + "/" + gid + ".log");
	}

	private static String format(int i, int spaces)
	{
		String s = "" + i;
		while(s.length() < spaces)
			s = " " + s;
		return s;
	}

	private static Date lastMove(String log, String player)
	{
		int end = log.lastIndexOf(player) - 2;
		int start = end - dateFormat.toPattern().length();
		return parseDate(log.substring(start, end));
	}

	public static synchronized Date parseDate(String s)
	{
		try
		{
			return dateFormat.parse(s);
		}
		catch(ParseException e)
		{
			return null;
		}
	}

	public static synchronized String format(Date date)
	{
		return dateFormat.format(date);
	}

	private static int countOccurrences(String source, String part, boolean ignoreDuplicates)
	{
		return countOccurrences(source, part, ignoreDuplicates, 0, source.length());
	}

	private static int countOccurrences(String source, String part, boolean ignoreDuplicates, int from, int to)
	{
		int lastIndex = from;
		int index = from;
		int count = 0;
		while(true)
		{
			index = source.indexOf(part, index);
			if(index == -1 || index >= to)
				break;
			if(!(ignoreDuplicates && countOccurrences(source, "\n", false, lastIndex, index) == 1 && lastIndex != from))
				count++;
			lastIndex = index;
			index++;
		}
		return count;
	}

	private static List<String> getPlayersFromLog(String log)
	{
		List<String> players = new LinkedList<String>();
		int index = 0;
		String player;
		int firstMovePartEnd = log.indexOf(": -----------------------------------");
		if(firstMovePartEnd == -1)
			firstMovePartEnd = log.length();
		String firstMovePart = log.substring(log.indexOf("erstellt"), firstMovePartEnd);
		while(true)
		{
			index = firstMovePart.indexOf(start_playerName_Log, index + 1) + start_playerName_Log.length();
			if(index == start_playerName_Log.length() - 1)
				break;
			if(firstMovePart.indexOf(end_playerName_Log, index + 1) > 0)
				player = firstMovePart.substring(index, firstMovePart.indexOf(end_playerName_Log, index + 1));
			else
				player = firstMovePart.substring(index);

			if(player.contains(" wird von Didi aus dem Spiel geworfen"))
				player = player.substring(0, player.indexOf(" wird von Didi aus dem Spiel geworfen"));
			if(player.contains(" wird von KaroMAMA aus dem Spiel geworfen"))
				player = player.substring(0, player.indexOf(" wird von KaroMAMA aus dem Spiel geworfen"));
			if(player.contains(" steigt aus dem Spiel aus"))
				player = player.substring(0, player.indexOf(" steigt aus dem Spiel aus"));

			players.add(player);
		}
		return players;
	}

	private static GameResult createResultFromLog(int gid, String log, List<String> players)
	{
		List<PlayerResult> results = new LinkedList<PlayerResult>();
		int last = players.size();

		String firstLine = log;
		if(log.indexOf("\n") > 0)
			firstLine = log.substring(0, log.indexOf("\n"));
		String name = firstLine.substring(27, firstLine.lastIndexOf(" auf Map "));

		int moves;
		int crashs;
		int position;
		boolean kicked;
		Date finished;
		Date gameFinished = null;

		// int maxMoves = 0;
		// for(String player : players)
		// {
		// moves = countOccurrences(log, player + end_playerName_Log);
		// if(moves > maxMoves)
		// maxMoves = moves;
		// }

		int thrown = 0;
		List<Integer> thrownIndexes = new ArrayList<Integer>(players.size());
		for(String player : players)
		{
			position = log.lastIndexOf(player);
			if(position == -1)
			{
			}
			else if(log.lastIndexOf(player + " wird von Didi aus dem Spiel geworfen") == position
					|| log.lastIndexOf(player + " wird von KaroMAMA aus dem Spiel geworfen") == position
					|| log.lastIndexOf(player + " steigt aus dem Spiel aus") == position)
			{
				thrown++;
				thrownIndexes.add(position);
			}
		}
		last = last - thrown;

		for(String player : players)
		{
			moves = countOccurrences(log, player + " -> ", true);
			if(moves != countOccurrences(log, player + " -> ", false))
				logger.info(" " + gid + " " + player);
			moves = countOccurrences(log, player + " -> ", false);
			if(moves < 0)
				moves = 0;
			crashs = countOccurrences(log, player + " CRASHT!!!", true);

			position = log.lastIndexOf(player);
			if(position == -1)
			{
				position = last--;
				kicked = false;
				finished = null;
			}
			else if(log.lastIndexOf(player + " wird von Didi aus dem Spiel geworfen") == position
					|| log.lastIndexOf(player + " wird von KaroMAMA aus dem Spiel geworfen") == position
					|| log.lastIndexOf(player + " steigt aus dem Spiel aus") == position)
			{
				position = players.size();
				// moves = maxMoves + 1;
				kicked = true;
				finished = lastMove(log, player);
			}
			else if(log.lastIndexOf(player + " wird") == position)
			{
				position = position + (player + " wird ").length();
				position = Integer.parseInt(log.substring(position, log.indexOf(".", position + 1)));
				kicked = false;
				finished = lastMove(log, player);
			}
			else
			{
				position = last--;
				kicked = false;
				finished = null;
			}

			if(finished != null && (gameFinished == null || finished.after(gameFinished)))
				gameFinished = finished;
			results.add(new PlayerResult(player, moves, crashs, position, finished, kicked));
		}
		
		Collections.sort(results);

		boolean allPlayersFinished = true;
		for(PlayerResult r : results)
		{
			allPlayersFinished = allPlayersFinished && (r.getFinished() != null);
		}

		return new GameResult(gid, name, (allPlayersFinished ? gameFinished : null), results);
	}

	public static void fillStringBuilders(StringBuilder[] sbs)
	{
		int maxLength = 0;
		for(StringBuilder sb : sbs)
		{
			if(sb.length() > maxLength)
				maxLength = sb.length();
		}
		maxLength += 2;

		for(StringBuilder sb : sbs)
		{
			while(sb.length() < maxLength)
				sb.append(" ");
		}
	}

	private static void sortResults()
	{
		if(calcOrder.equals(calcOrder_gid))
		{
			Collections.sort(allResults, new GameResult.GIDComparator());
			Collections.sort(filteredResults, new GameResult.GIDComparator());
		}
		else if(calcOrder.equals(calcOrder_date))
		{
			Collections.sort(allResults, new GameResult.DateComparator());
			Collections.sort(filteredResults, new GameResult.DateComparator());
		}
	}

	private static void printCount(int finished, int planned)
	{
		String fin = "" + finished;
		String plan = "" + planned;
		while(fin.length() < plan.length())
			fin = " " + fin;
		String result = fin + "/" + plan;
		for(int i = 0; i < result.length(); i++)
			logger.info("\b");
		logger.info(result);
	}

	protected List<String>	players;
	protected Date			startDate;

	public CustomEval()
	{
	}

	public final void init(List<GameResult> results)
	{
		for(Entry<String, Double> constant: constants.entrySet())
		{
			this.setConstant(constant.getKey(), constant.getValue());
		}
		
		players = new LinkedList<String>();
		int done = 0;
		printCount(done, results.size());
		for(GameResult gr : results)
		{
			for(PlayerResult pr : gr.getResults())
			{
				if(!players.contains(pr.getPlayer()))
					players.add(pr.getPlayer());
			}
			printCount(++done, results.size());
			if(done == 1)
			{
				startDate = gr.getFinishDate();
			}
		}
		init0();
	}

	public abstract void init0();

	public abstract void setConstant(String name, double value);

	public final void calculate(List<GameResult> results)
	{
		int done = 0;
		long intervalLength = calcInterval * 24 * 60 * 60 * 1000;
		long intervals = 0;
		long offset = 0;
		printCount(done, results.size());
		for(GameResult gr : results)
		{
			if(calcOrder.equals(calcOrder_gid))
			{
				if(done % calcInterval == 0)
				{
					this.addInterval(gr.getFinishDate());
					intervals++;
				}
			}
			else if(calcOrder.equals(calcOrder_date))
			{
				if(done == 0)
				{
					String offsetDateS = format(gr.getFinishDate());
					Date offsetDate = parseDate(offsetDateS.substring(0, 10) + " 00:00:00");
					offset = offsetDate.getTime();
					this.addInterval(offsetDate);
				}
				if(gr.getFinishDate().getTime() >= (intervalLength * (intervals + 1) + offset))
				{
					this.addInterval(new Date(intervalLength * (intervals + 1) + offset));
					intervals++;
				}
			}

			this.addToCalculation(gr);
			printCount(++done, results.size());
		}
	}

	public abstract void addToCalculation(GameResult result);

	public abstract void addInterval(Date date);

	public abstract Map<String, List<PlayerRecord>> getPlayerRecords();

	public abstract List<TableRecord> getTable();

	private static class ResultThread extends StringURLLoaderThread
	{
		private int	gid;

		public ResultThread(int gid) throws MalformedURLException
		{
			super(new URL(url_log + gid + ".log"), "GET", "", 10000);
			this.gid = gid;
		}

		@Override
		public void innerRun()
		{
			if(cache)
				result = readCache(gid);
			boolean fromCache = (result != null);
			while(result == null || !(result.contains("erstellt") || result.contains("<HTML>")))
			{
				super.innerRun();
			}
			GameResult gameResult = null;
			if(!result.contains("<HTML>"))
			{
				gameResult = parseGame(gid, result);
				if(gameResult.isFinished())
				{
					allResults.add(gameResult);
					if(titleRegExp != null && gameResult.getName().matches(titleRegExp))
						filteredResults.add(gameResult);
				}
			}
			if(cache && !fromCache && (gameResult.isFinished() || result.contains("<HTML>")))
				writeCache(gid, result);
		}
	}
}
