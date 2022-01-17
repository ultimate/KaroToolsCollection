package ultimate.karoapi4j;

import java.net.MalformedURLException;
import java.net.URL;

import ultimate.karoapi4j.utils.sync.Refreshable;
import ultimate.karoapi4j.utils.web.URLLoader;
import ultimate.karoapi4j.utils.web.urlloaders.StringURLLoaderThread;

public class EnumFinder implements Refreshable<String>
{
	public static final String	url			= "http://reloaded.karopapier.de/api/games";
	public static final String	params		= "limit=100";
	public static final String	enumName	= "tcrash";
	
	private static URLLoader<String> loader;

	public static void main(String[] args) throws MalformedURLException
	{
//		for(int i = 40000; i < 40100; i++)
//		{
//			URLLoader loader = new URLLoaderThread(new URL("http://reloaded.karopapier.de/api/games/" + i + "/info"), "");
			loader = new StringURLLoaderThread(new URL(url));
			System.out.println("loading...");
			loader.load(new EnumFinder());
//		}
	}

	@Override
	public void onRefresh(String refreshed)
	{
		System.out.println("load finished, analyzing...");
		System.out.println(loader.getLoadedContent());
		int index = refreshed.indexOf(enumName, 0);
		int i = 0;
		while(index >= 0)
		{
			System.out.println(i++ + ": " + refreshed.substring(index, refreshed.indexOf(",", index)));
			index = refreshed.indexOf(enumName, index + 1);
		}
		System.out.println("analyzation done");
		System.exit(0);
	}
}
