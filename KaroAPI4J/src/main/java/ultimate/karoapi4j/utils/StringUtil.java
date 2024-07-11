package ultimate.karoapi4j.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public abstract class StringUtil
{
	private StringUtil()
	{
	}

	/**
	 * Fill a number with leading zeros ('0')
	 * 
	 * @param x - the number
	 * @param minDigits - the minimum number of digits to achieve
	 * @return the number as a string with leading zeros
	 */
	public static String toString(int x, int minDigits)
	{
		String s = "" + x;
		while(s.length() < minDigits)
			s = "0" + s;
		return s;
	}

	/**
	 * Limit a string to a given fixed length by replacing the overhead with "..." if needed.
	 * 
	 * @param s
	 * @param length
	 * @return
	 */
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

	/**
	 * Process a html section into a list of items that are represented by the same tag.
	 * E.g. process a table row
	 * <code>&lt;tr&gt;&lt;td&gt;1&lt;/td&gt;&lt;td&gt;2&lt;/td&gt;&lt;td&gt;3&lt;/td&gt;&lt;/tr&gt;</code>
	 * with tag <code>td</code> to <code>["1", "2", "3"]</code>
	 * 
	 * @param <T>
	 * @param section
	 * @param tagToLookFor
	 * @param parser
	 * @return
	 */
	public static <T> List<T> processHTMLSection(String section, String tagToLookFor, Function<String, T> parser)
	{
		String START_TAG = "<" + tagToLookFor + "";
		String END_TAG = "</" + tagToLookFor + ">";

		List<T> items = new LinkedList<T>();

		int itemStart = section.indexOf(START_TAG);

		if(itemStart < 0)
			return items;

		int itemEnd = 0;

		do
		{
			itemStart = section.indexOf(">", itemStart) + 1;
			itemEnd = section.indexOf(END_TAG, itemStart);
			items.add(parser.apply(section.substring(itemStart, itemEnd)));
			itemStart = section.indexOf(START_TAG, itemEnd);
		} while(itemStart >= 0);

		return items;
	}

	/**
	 * Trim tags from a html snippet and just keep the remaining text
	 * 
	 * @param value
	 * @return
	 */
	public static String trimTags(String value)
	{
		int index = 0;
		for(; index < value.length(); index++)
		{
			if(value.charAt(index) == '<')
				value = value.replace(value.substring(index, value.indexOf('>', index) + 1), "");
		}
		return value.trim();
	}

	/**
	 * Trim text from a text snippet and just keep the first occurrence that is a number
	 * 
	 * @param value
	 * @return
	 */
	public static String trimToNumber(String value)
	{
		value = trimTags(value);

		String allowedChars = "0123456789.";

		int start = 0;
		for(; start < value.length() && allowedChars.indexOf(value.charAt(start)) < 0; start++)
			;

		if(start >= value.length())
			return "0";

		int end = start + 1;
		for(; end < value.length() && allowedChars.indexOf(value.charAt(end)) >= 0; end++)
			;

		return value.substring(start, end);
	}

	/**
	 * Parse a range string to an array of numbers.
	 * 
	 * @param ranges - list of ranges in the format "1-5,7,9,13-18,20"
	 * @return the numbers defined by the range string
	 */
	public static int[] parseRanges(String ranges)
	{
		List<Integer> numbers = new LinkedList<>();

		// parse all the ranges within the string
		String[] rs = ranges.split(",");
		for(String r : rs)
		{
			try
			{
				if(r.contains("-"))
				{
					String[] startEnd = r.split("-");
					if(startEnd.length != 2)
						throw new IllegalArgumentException("cannot parse ranges: '" + ranges + "'");
					int start = Integer.parseInt(startEnd[0].trim());
					int end = Integer.parseInt(startEnd[1].trim());
					for(int n = start; n <= end; n++)
						numbers.add(n);
				}
				else
				{
					numbers.add(Integer.parseInt(r.trim()));
				}
			}
			catch(NumberFormatException e)
			{
				throw new IllegalArgumentException("cannot parse ranges: '" + ranges + "'", e);
			}
		}

		// convert to array
		int[] arr = new int[numbers.size()];
		int i = 0;
		for(Integer n : numbers)
			arr[i++] = n;
		
		return arr;
	}
}
