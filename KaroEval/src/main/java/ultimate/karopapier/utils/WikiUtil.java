package ultimate.karopapier.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoapi4j.model.official.User;
import ultimate.karopapier.utils.Table.Cell;

public abstract class WikiUtil
{
	public static final String		HIGHLIGHT		= "'''";
	public static final String		SORTABLE		= "sortable";
	public static final String		START_CELL		= "|";
	public static final String		START_HEADER	= "!";
	public static final DateFormat	DATE_FORMAT		= new SimpleDateFormat("yyyy.MM.dd HH:mm");

	private WikiUtil()
	{

	}

	public static int[] getDefaultColumnConfig(int columns)
	{
		int[] columnConfig = new int[columns];
		for(int i = 0; i < columns; i++)
			columnConfig[i] = i;
		return columnConfig;
	}

	public static String toString(Table table, String cssClasses)
	{
		return toString(table, cssClasses, "");
	}

	public static String toString(Table table, String cssClasses, String nullValue)
	{
		return toString(table, cssClasses, getDefaultColumnConfig(table.getColumns()), nullValue);
	}

	public static String toString(Table table, String cssClasses, int[] columnConfig)
	{
		return toString(table, cssClasses, columnConfig, "");
	}

	public static String toString(Table table, String cssClasses, int[] columnConfig, String nullValue)
	{
		if(nullValue == null)
			nullValue = "";

		StringBuilder sb = new StringBuilder();
		sb.append("{|class=\"wikitable");
		if(cssClasses != null)
			sb.append(" " + cssClasses);
		sb.append("\"");

		for(int hi = 0; hi < table.getHeaders().size(); hi++)
			toString(sb, table.getHeader(hi), columnConfig, nullValue, true);
		for(int ri = 0; ri < table.getRows().size(); ri++)
			toString(sb, table.getRow(ri), columnConfig, nullValue, false);

		sb.append("|}");

		return sb.toString();
	}

	private static void toString(StringBuilder sb, Cell[] row, int[] columnConfig, String nullValue, boolean isHeader)
	{
		int col;
		for(int ci = 0; ci < columnConfig.length; ci++)
		{
			col = columnConfig[ci];
			if(col >= row.length)
				continue;

			sb.append("\r\n");
			sb.append(isHeader ? START_HEADER : START_CELL);
			if(row[col].colspan > 1)
			{
				sb.append("colspan=" + row[col].colspan);
				ci += (row[col].colspan - 1);
			}
			sb.append("|");

			if(row[col] == null || row[col].value == null)
				sb.append(nullValue);
			else if(row[col].highlight)
				sb.append(highlight(preprocess(row[col].value)));
			else
				sb.append(preprocess(row[col].value));
		}
		sb.append("\r\n|-\r\n");
	}

	public static String toDebugString(Table table, int columnWidth)
	{
		if(columnWidth < 4)
			columnWidth = 4;
		
		StringBuilder sb = new StringBuilder();

		for(int hi = 0; hi < table.getHeaders().size(); hi++)
			toDebugString(sb, table.getHeader(hi), columnWidth, true);
		for(int ri = 0; ri < table.getRows().size(); ri++)
			toDebugString(sb, table.getRow(ri), columnWidth, false);

		return sb.toString();
	}

	private static void toDebugString(StringBuilder sb, Cell[] row, int columnWidth, boolean isHeader)
	{
		for(int col = 0; col < row.length; col++)
		{
			sb.append(isHeader ? START_HEADER : START_CELL);
			sb.append("|");

			if(row[col] == null || row[col].value == null)
				sb.append(fixedLength("<null>", columnWidth));
			else
				sb.append(fixedLength(preprocess(row[col].value).toString(), columnWidth));
		}
		sb.append("|-\r\n");
	}

	public static Object preprocess(Object value)
	{
		if(value instanceof Double)
			return round((double) value);
		else if(value instanceof Player)
			return createLink((Player) value);
		else if(value instanceof User)
			return createLink((User) value);
		else if(value instanceof Date)
			return DATE_FORMAT.format((Date) value);
		return value;
	}

	public static String highlight(Object o)
	{
		return HIGHLIGHT + String.valueOf(o) + HIGHLIGHT;
	}

	public static String createLink(String text, String target)
	{
		return "[[" + text + "|" + target + "]]";
	}

	public static String createLink(Game game)
	{
		return createLink(game, game.getName());
	}

	public static String createLink(Game game, String overwriteTitle)
	{
		return "{{Rennen|" + game.getId() + "|" + overwriteTitle + "}}";
	}

	public static String createLink(PlannedGame game)
	{
		return createLink(game.getGame());
	}

	public static String createLink(PlannedGame game, String overwriteTitle)
	{
		return createLink(game.getGame(), overwriteTitle);
	}

	public static String createLink(Map map, boolean includeName)
	{
		return "{{Karte|" + map.getId() + "}}" + (includeName ? " " + map.getName() : "");
	}

	public static String createLink(User user)
	{
		return createLink(user.getLogin());
	}

	public static String createLink(Player player)
	{
		return createLink(player.getName());
	}

	public static String createLink(String login)
	{
		String tmp;
		if(login.startsWith("Deep"))
			tmp = "[[" + login + "]]";
		else if(login.equals("OleOCrasher"))
			tmp = "[[Benutzer:OleOJumper|OleOCrasher]]";
		else
			tmp = "[[Benutzer:" + login + "|" + login + "]]";
		return tmp;
	}

	public static int countOccurrences(String source, String part)
	{
		return countOccurrences(source, part, true, 0, source.length());
	}

	public static int countOccurrences(String source, String part, int from)
	{
		return countOccurrences(source, part, true, from, source.length());
	}

	public static int countOccurrences(String source, String part, boolean ignoreDuplicates, int from, int to)
	{
		int lastIndex = from;
		int index = from;
		int count = 0;
		while(true)
		{
			index = source.indexOf(part, index);
			if(index == -1 || index >= to)
				break;
			if(!(ignoreDuplicates && countOccurrences(source, "\r\n", false, lastIndex, index) == 1 && lastIndex != from))
				count++;
			lastIndex = index;
			index++;
		}
		return count;
	}

	public static double round(double d)
	{
		if(d == Double.POSITIVE_INFINITY)
			return d;
		else if (d == Double.POSITIVE_INFINITY)
			return d;
		return Math.round(d * 100) / 100.0;
	}
	
	public static String fixedLength(String s, int length)
	{
		if(s.length() > length)
			return s.substring(0, length - 3) + "...";
		else
		{
			StringBuilder sb = new StringBuilder(s);
			while(sb.length() < length)
				sb.append(" ");
			return sb.toString();
		}
	}
}
