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
		File cronPropertiesFile = null;
		File evalPropertiesFile = null;
		
		System.out.print("loading properties...");
		for(File f: folder.listFiles())
		{
			if(f.getName().equals("cron.properties"))
				cronPropertiesFile = f;
			if(f.getName().endsWith("eval.properties"))
				evalPropertiesFile = f;
		}
		System.out.print(" " + cronPropertiesFile.getAbsolutePath() + " ...");
		Properties cronProperties = PropertiesUtil.loadProperties(cronPropertiesFile);
		Properties evalProperties = PropertiesUtil.loadProperties(evalPropertiesFile);
		System.out.println("OK");

		int execution = Integer.parseInt(cronProperties.getProperty("executions"));
		
		System.out.print("initiating KaroAPI...");
		KaroAPI karoAPI = new KaroAPI(cronProperties.getProperty("karo.username"), cronProperties.getProperty("karo.password"));
		KaroAPICache karoAPICache = new KaroAPICache(karoAPI);
		System.out.println("OK");
		
		System.out.print("loading GameSeries...");
		String gameSeriesFileName = cronProperties.getProperty("gameseries.file");
		GameSeries gs = GameSeriesManager.load(new File(folder, gameSeriesFileName), karoAPICache);
		GameSeriesManager.store(gs, new File(folder, "tmp.json"));
		System.out.println("OK");

		System.out.print("preparing...");
		e.prepare(karoAPICache, gs, evalProperties, folder, execution);
		System.out.println("OK");
		
		System.out.print("evaluating...");
		e.evaluate();
	}
}
