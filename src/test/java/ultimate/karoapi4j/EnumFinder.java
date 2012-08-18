package ultimate.karoapi4j;

import java.net.MalformedURLException;
import java.net.URL;

import ultimate.karoapi4j.utils.sync.Refreshable;
import ultimate.karoapi4j.utils.web.URLLoader;
import ultimate.karoapi4j.utils.web.URLLoaderThread;

public class EnumFinder implements Refreshable<String>
{
	public static final String	url			= "http://reloaded.karopapier.de/api/games";
	public static final String	params		= "limit=100";
	public static final String	enumName	= "status";

	public static void main(String[] args) throws MalformedURLException
	{
//		for(int i = 40000; i < 40100; i++)
//		{
//			URLLoader loader = new URLLoaderThread(new URL("http://reloaded.karopapier.de/api/games/" + i + "/info"), "");
			URLLoader loader = new URLLoaderThread(new URL(url), "");
			System.out.println("loading...");
			loader.loadURL(new EnumFinder());
//		}
	}

	@Override
	public void onRefresh(String refreshed)
	{
		System.out.println("load finished, analyzing...");
		int index = refreshed.indexOf(enumName, 0);
		while(index >= 0)
		{
			System.out.println(refreshed.substring(index, refreshed.indexOf(",", index)));
			index = refreshed.indexOf(enumName, index + 1);
		}
	}
}
