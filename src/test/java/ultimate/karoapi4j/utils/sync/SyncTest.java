package ultimate.karoapi4j.utils.sync;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import ultimate.karoapi4j.enums.EnumRefreshMode;
import ultimate.karoapi4j.model.official.ChatEntry;
import ultimate.karoapi4j.utils.JSONUtil;
import ultimate.karoapi4j.utils.web.URLLoader;
import ultimate.karoapi4j.utils.web.urlloaders.JSONURLLoaderThread;
import ultimate.karoapi4j.utils.web.urlloaders.StringURLLoaderThread;

public class SyncTest
{
	public static void main(String[] args) throws MalformedURLException, InterruptedException
	{
		String s = 	"[\n" + 
		    	 	"{\n" + 
		    		"	\"id\" : \"cmdcfac73011f091c38bb3a16f55efd52c\",\n" + 
		    		"	\"user\" : \"Akari\",\n" + 
		    		"	\"text\" : \"eim weiteres tor in 90 sec??\",\n" + 
		    		"	\"time\" : \"22:36\"\n" + 
		    		"},\n" + 
		    		"{\n" + 
		    		"	\"id\" : \"cmb944206db5e7179df3e2d8a0be7158f5\",\n" + 
		    		"	\"user\" : \"ImThinkin\",\n" + 
		    		"	\"text\" : \"oh ioch w\u00e4r sowas von daf\u00fcr\",\n" + 
		    		"	\"time\" : \"22:36\"\n" + 
		    		"} ]";
		URLLoader<String> stringLoader = new StringURLLoaderThread(new URL("http://reloaded.karopapier.de/api/chat/list.json"));
		stringLoader.load(new JsonRefreshable());
		
		URLLoader<List<ChatEntry>> listLoader = new JSONURLLoaderThread<List<ChatEntry>>(new URL("http://reloaded.karopapier.de/api/chat/list.json"));
		final SynchronizedList<ChatEntry> list = new SynchronizedList<ChatEntry>(listLoader, EnumRefreshMode.interval_1, false);
		
		Thread t = new Thread()
		{
			public void run()
			{
				while(true)
				{
					try
					{
						Thread.sleep(2000);
					}
					catch(InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("50 " + list.size());
					System.out.println("51 " + list.toString().replaceAll("},", "},\n"));
				}
			}
		};
		
		t.start();
	}
	
	private static class SysoRefreshable implements Refreshable<String>
	{

		@Override
		public void onRefresh(String refreshed)
		{
			System.out.println("65 " + refreshed);
		}
		
	}
	
	private static class JsonRefreshable implements Refreshable<String>
	{
		private Object o;
		
		@Override
		public void onRefresh(String refreshed)
		{
			o = JSONUtil.deserialize(refreshed);
			System.out.println("78 " + o.toString().replaceAll("},", "},\n"));
		}
		
	}
}
