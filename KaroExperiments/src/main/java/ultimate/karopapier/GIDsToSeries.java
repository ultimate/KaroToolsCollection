package ultimate.karopapier;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karomuskel.GameSeriesManager;

public class GIDsToSeries
{
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		File loginProperties = new File(args[0]);
		String gidRange = args[1];
		File outputFile = new File(args[2]);
		System.out.println(loginProperties.getAbsolutePath());
		
		Properties login = PropertiesUtil.loadProperties(loginProperties);
		
		KaroAPI api;
		if(login.containsKey("karoAPI.key"))
			api = new KaroAPI(login.getProperty("karoAPI.key"));
		else
			api = new KaroAPI(login.getProperty("karoAPI.user"), login.getProperty("karoAPI.password"));
		
		Properties cacheProperties = new Properties();
		cacheProperties.setProperty("karoAPI.images", "false");
		KaroAPICache karoAPICache = new KaroAPICache(api, cacheProperties);
		karoAPICache.refresh().get();
		
		GameSeries gs = GameSeriesManager.loadFromGIDs(EnumGameSeriesType.Simple, gidRange, karoAPICache);
		
		GameSeriesManager.store(gs, outputFile);
	}
}
