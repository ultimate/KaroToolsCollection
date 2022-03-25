package ultimate.karopapier.eval;

import java.io.File;
import java.util.Properties;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karomuskel.GameSeriesManager;

public class EvalRunner
{
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception
	{
		System.out.println("loading eval: " + args[0]);
		Class<Eval<GameSeries>> cls = (Class<Eval<GameSeries>>) Class.forName(args[0]);
		Eval<GameSeries> e = cls.getConstructor().newInstance();
		
		File folder = new File(".");
		File propertiesFile = null;
		
		System.out.print("loading properties...");
		for(File f: folder.listFiles())
		{
			if(f.getName().equals("cron.properties"))
				propertiesFile = f;
		}
		System.out.print(" " + propertiesFile.getAbsolutePath() + " ...");
		Properties properties = PropertiesUtil.loadProperties(propertiesFile);
		System.out.println("OK");

		int execution = Integer.parseInt(properties.getProperty("executions"));
		
		System.out.print("initiating KaroAPI...");
		KaroAPI karoAPI = new KaroAPI(properties.getProperty("karo.username"), properties.getProperty("karo.password"));
		KaroAPICache karoAPICache = new KaroAPICache(karoAPI);
		System.out.println("OK");
		
		System.out.print("loading GameSeries...");
		String gameSeriesFileName = properties.getProperty("gameseries.file");
		GameSeries gs = GameSeriesManager.load(new File(folder, gameSeriesFileName), karoAPICache);
		System.out.println("OK");

		System.out.print("preparing...");
		e.prepare(karoAPICache, gs, properties, folder, execution);
		System.out.println("OK");
		
		System.out.print("evaluating...");
		e.evaluate();
	}
}
