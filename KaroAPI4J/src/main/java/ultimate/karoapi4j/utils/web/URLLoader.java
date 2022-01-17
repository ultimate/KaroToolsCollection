package ultimate.karoapi4j.utils.web;

import ultimate.karoapi4j.utils.sync.Loader;

/**
 * Extension of Loader for URLLoaders.<br>
 * URLLoaders usually load Strings from the given URL, so transforming this String to the required
 * loaded Type is necessary via {@link URLLoader#parse(String)}
 * 
 * @author ultimate
 * @param <T> - the type of content to load
 */
public interface URLLoader<T> extends Loader<T>
{
	/**
	 * Parse the given String (loaded from the URL) to the required Type
	 * 
	 * @param refreshed - the (new) content to parse
	 * @return the content in the required Type
	 */
	public T parse(String refreshed);
}
