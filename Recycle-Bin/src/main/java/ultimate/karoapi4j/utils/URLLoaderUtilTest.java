package ultimate.karoapi4j.utils;

import java.net.URL;

import ultimate.karoapi4j.model.official.User;

public class URLLoaderUtilTest
{
// no real test yet...
	public static void main(String[] args) throws Exception
	{
		URL url = new URL("http://reloaded.karopapier.de/api/user/1/info.json");
		
		System.out.println(URLLoaderUtil.load(url));
		
		System.out.println(URLLoaderUtil.load(url, User.class));
	}
}
