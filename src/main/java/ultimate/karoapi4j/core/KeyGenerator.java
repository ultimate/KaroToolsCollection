package ultimate.karoapi4j.core;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

public class KeyGenerator
{
	public static String createUnlockKey(String username)
	{
		TreeMap<Character, List<Integer>> chars = new TreeMap<Character, List<Integer>>();

		Character curr;
		List<Integer> positions;
		StringBuilder unlockKey = new StringBuilder();
		int count = 0;
		long check = 0;
		int tmp;
		String tmpS;

		for (int i = 0; i < username.length(); i++)
		{
			curr = username.charAt(i);
			if (!chars.containsKey(curr))
			{
				chars.put(curr, new LinkedList<Integer>());
			}
			chars.get(curr).add(i);
		}

		for (Character key : chars.keySet())
		{
			positions = chars.get(key);
			tmp = (int) key.charValue();
			for (Integer position : positions)
			{
				tmpS = Integer.toHexString(tmp + position);
				if (tmpS.length() < 2)
					tmpS = "0" + tmpS;
				unlockKey.append(tmpS.toUpperCase());
				check += position * Math.pow(10, count);
				count++;
			}
		}

		tmpS = Long.toHexString(check);
		if (tmpS.length() % 2 == 1)
			tmpS = "0" + tmpS;
		unlockKey.append(tmpS.toUpperCase());

		return unlockKey.toString();
	}
}
