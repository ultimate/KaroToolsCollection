package ultimate.karopapier.ccc;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.official.Generator;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karomuskel.GameSeriesManager;

public class CCCGeneratorSettingsChecker
{
	/*
		src/main/resources/login.properties
		../CraZZZy Crash Challenge/CCC7/czzzcc7.json
	 */
	public static void main(String[] args) throws IOException {
		try {
			File loginProperties = new File(args[0]);
			File gameSeriesFile = new File(args[1]);
			
			int minPlayers = 5;
			
			// initialize karoapi
			Properties login = PropertiesUtil.loadProperties(loginProperties);
			KaroAPI api = new KaroAPI(login.getProperty("karoAPI.user"), login.getProperty("karoAPI.password"));
			KaroAPICache cache = new KaroAPICache(api, login);
			cache.refresh().join();		
			
			GameSeries gs = GameSeriesManager.load(gameSeriesFile, cache);
			
			Set<Long> generatorsChecked = new HashSet<>();
			Random rnd = new Random();
			
			int i = 0;
			for(PlannedGame pg: gs.getGames().get("Balanced")) {
				if(pg.getMap() instanceof Generator) {
					
					Generator g = (Generator) pg.getMap();
					long hash = Objects.hash(g.getKey(), g.getSettings());
					
					if(generatorsChecked.contains(hash))
						continue;
					if(pg.isCreated())
						continue;
					
					System.out.println("analyzing #" + (i++) + ": " + pg.getName() + ": " + g.toSettingsString(false));
					
//					if(i > 12*6) {
						boolean ok = false;
						boolean first = true;
						do
						{
							if(!first)
								g.getSettings().put("seed", rnd.nextInt());
								
							first = false;
							
							System.out.print("seed = " + g.getSettings().get("seed" ));
							try
							{
								Map map = api.generateMap(g).get();
		
								ok = map.getPlayers() >= minPlayers;
								System.out.println(" \t-> players = " + map.getPlayers() + " -> " + (ok ? "OK" : "TOO SMALL!"));
							} catch(Exception e) {
								System.out.println(" \t-> ERROR!");
							}
						} while(!ok);
//					}
					generatorsChecked.add(hash);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
}
