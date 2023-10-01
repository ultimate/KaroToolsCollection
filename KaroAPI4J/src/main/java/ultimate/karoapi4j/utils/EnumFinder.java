package ultimate.karoapi4j.utils;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is a helper class that scans the raw content from the karo API for enum values. The output can then be used to add new values to the enum
 * definitions in ultimate.karoapi4j.enums.<br>
 * Note: see EnumFinderTest for the list of enums and for checking their completeness
 * 
 * @see EnumFinder#findEnums(String, String)
 *
 * @author ultimate
 */
public class EnumFinder
{
	/**
	 * Logger-Instance
	 */
	protected static transient final Logger logger = LogManager.getLogger(EnumFinder.class);
	/**
	 * The quotations mark around json identifiers
	 */
	public static final String				QUOT	= "\"";

	/**
	 * This method scans the raw content from a karo API call for the given key. As soon as the key is found it will look for the value contained in
	 * the quotation marks after the colon and add this to the result {@link Set}.<br>
	 * For example for the following array and the key <code>"state"</code> the method will return the {@link Set}
	 * <code>["active", "inactive"]</code>:
	 * <code><pre>
	 * [
	 *   { // object 1
	 *      "id": 1,
	 *      "state": "active",
	 *   },
	 *   { // object 2
	 *      "id": 2,
	 *      "state": "inactive",
	 *   }
	 * ]
	 * </pre></code>
	 * 
	 * @param raw - the raw content from the karo API
	 * @param key - the key to scan for
	 * @return the {@link Set} of found values
	 */
	public static Set<String> findEnums(String raw, String key)
	{
		Set<String> values = new HashSet<>();
		String searchString = QUOT + key + QUOT;
		int index = 0;
		int start, end;

		do
		{
			index = raw.indexOf(searchString, index);
			if(index >= 0)
			{
				start = raw.indexOf(QUOT, index + searchString.length() + 1) + QUOT.length();
				end = raw.indexOf(QUOT, start + QUOT.length());
				values.add(raw.substring(start, end));
				index = end;
			}
		} while(index >= 0);
		return values;
	}
}
