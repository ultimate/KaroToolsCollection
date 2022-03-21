package ultimate.karopapier.utils;

import ultimate.karoapi4j.model.official.Game;
import ultimate.karoapi4j.model.official.Map;
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

	public static String toWikiString(Table table, String cssClasses)
	{
		return toWikiString(table, cssClasses, getDefaultColumnConfig(table.getColumns()));
	}

	public static String toWikiString(Table table, String cssClasses, int[] columnConfig)
	{
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
		for(Object[] row : table.getRows())
		{
			sb.append("|");
			for(int i = 0; i < columnConfig.length; i++)
			{
				col = columnConfig[i];
				if(col >= row.length)
					continue;

				if(i > 0)
					sb.append("||");

				sb.append(row[col]);
			}
			sb.append("\n|-\n");
		}
		sb.append("|}");

		return sb.toString();
	}

	public static String highlight(String s)
	{
		return HIGHLIGHT + s + HIGHLIGHT;
	}

	public static String gameToLink(Game game)
	{
		return "{{Rennen|" + game.getId() + "|" + game.getName() + "}}";
	}

	public static String mapToLink(Map map, boolean includeName)
	{
		return "{{Karte|" + map.getId() + "}}" + (includeName ? " " + map.getName() : "");
	}

	public static String playerToLink(User user, boolean bold)
	{
		String tmp;
		if(user.getLogin().startsWith("Deep"))
			tmp = "[[" + user.getLogin() + "]]";
		else if(user.getLogin().equals("OleOCrasher"))
			tmp = "[[Benutzer:OleOJumper|OleOCrasher]]";
		else
			tmp = "[[Benutzer:" + user.getLogin() + "|" + user.getLogin() + "]]";
		if(bold)
			tmp = highlight(tmp);
		return tmp;
	}
}
