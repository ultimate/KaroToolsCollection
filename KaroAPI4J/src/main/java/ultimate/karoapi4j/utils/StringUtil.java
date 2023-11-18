package ultimate.karoapi4j.utils;

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
}
