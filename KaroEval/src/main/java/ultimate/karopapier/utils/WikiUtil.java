package ultimate.karopapier.utils;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.Player;
import ultimate.karoapi4j.model.official.User;

public abstract class WikiUtil
{
	public static final String HIGHLIGHT = "'''";

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
		sb.append("\"\n");

		int col;
		if(table.getHeader() != null)
		{
			sb.append("!");
			for(int i = 0; i < columnConfig.length; i++)
			{
				col = columnConfig[i];
				if(col >= table.getHeader().length)
					continue;

				if(i > 0)
					sb.append("||");

				sb.append(table.getHeader()[col]);
			}
			sb.append("\n|-\n");
		}
		for(int ri = 0; ri < table.getRows().size(); ri++)
		{
			Object[] row = table.getRow(ri);
			sb.append("|");
			for(int ci = 0; ci < columnConfig.length; ci++)
			{
				col = columnConfig[ci];
				if(col >= row.length)
					continue;

				if(ci > 0)
					sb.append("||");

				if(row[col] == null)
					sb.append(nullValue);
				else
				{
					Object value = row[col];
					if(value instanceof Double)
						value = round((double) value);
					if(table.isHighlight(ri, col))
						sb.append(highlight(value));
					else
						sb.append(value);
				}
			}
			sb.append("\n|-\n");
		}
		sb.append("|}");

		return sb.toString();
	}

	public static String highlight(Object o)
	{
		return HIGHLIGHT + String.valueOf(o) + HIGHLIGHT;
	}

	public static String createLink(String text, String target)
	{
		return "[[" + target + "|" + text + "]]";
	}

	public static String createLink(Game game)
	{
		return createLink(game, game.getName());
	}

	public static String createLink(Game game, String overwriteTitle)
	{
		return "{{Rennen|" + game.getId() + "|" + game.getName() + "}}";
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

	public static String createLink(User user, boolean bold)
	{
		return createLink(user.getLogin(), bold);
	}

	public static String createLink(Player player, boolean bold)
	{
		return createLink(player.getName(), bold);
	}

	private static String createLink(String login, boolean bold)
	{
		String tmp;
		if(login.startsWith("Deep"))
			tmp = "[[" + login + "]]";
		else if(login.equals("OleOCrasher"))
			tmp = "[[Benutzer:OleOJumper|OleOCrasher]]";
		else
			tmp = "[[Benutzer:" + login + "|" + login + "]]";
		if(bold)
			tmp = highlight(tmp);
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
			if(!(ignoreDuplicates && countOccurrences(source, "\n", false, lastIndex, index) == 1 && lastIndex != from))
				count++;
			lastIndex = index;
			index++;
		}
		return count;
	}

	private static double round(double d)
	{
		return Math.round(d * 100) / 100.0;
	}
}
