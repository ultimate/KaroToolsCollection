package karopapier.application;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimpleCookieHandler extends CookieHandler
{
	private List<Cookie>	cache	= new LinkedList<Cookie>();

	@Override
	public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException
	{
		List<String> setCookieList = responseHeaders.get("Set-Cookie");
		if(setCookieList != null)
		{
			for(String item : setCookieList)
			{
				Cookie cookie = new Cookie(uri, item);
				// Remove cookie if it already exists - New one will replace
				for(Cookie existingCookie : cache)
				{
					if(/* (cookie.getURI().equals(existingCookie.getURI())) && */(cookie.getName().equals(existingCookie.getName())))
					{
						cache.remove(existingCookie);
						break;
					}
				}
				cache.add(cookie);
			}
		}
	}

	@Override
	public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException
	{
		// Retrieve all the cookies for matching URI
		// Put in list
		List<String> list = new ArrayList<String>();
		List<Cookie> temp = new LinkedList<Cookie>(cache);
		for(Cookie cookie : temp)
		{
			// Remove cookies that have expired
			if(cookie.hasExpired())
			{
				cache.remove(cookie);
			}
			else if(cookie.matches(uri))
			{
				list.add(cookie.toString());
			}
		}
		// Map to return
		Map<String, List<String>> cookieMap = new HashMap<String, List<String>>(requestHeaders);
		// Convert StringBuilder to List, store in map
		if(list.size() > 0)
		{
			Collections.sort(list);
			cookieMap.put("Cookie", list);
		}
		return Collections.unmodifiableMap(cookieMap);
	}
}
