package ultimate.karopapier.ccc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumCreatorParticipation;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karomuskel.GameSeriesManager;

public class CCCGeneratorPlanner
{
	public static final int GAME_DAYS_VIRTUALLY = 25;
	public static final int GAMES_PER_PLAYER_PER_GAME_DAY = 6;
	public static final int VARIATIONS_PER_GAME_DAY = GAMES_PER_PLAYER_PER_GAME_DAY;
	public static final int CHALLENGES = GAME_DAYS_VIRTUALLY * GAMES_PER_PLAYER_PER_GAME_DAY;
	
	public static void main(String[] args) {
		
		File loginProperties = new File(args[0]);
		
		// initialize karoapi
		Properties login = PropertiesUtil.loadProperties(loginProperties);
		KaroAPI api = new KaroAPI(login.getProperty("karoAPI.user"), login.getProperty("karoAPI.password"));
		KaroAPICache cache = new KaroAPICache(api, login);
		cache.refresh().join();		
		
		// create empty gameseries
		GameSeries gs = new GameSeries(EnumGameSeriesType.Balanced);
		// general settings (from SettingsSc
		gs.setTitle("CraZZZy Crash Challenge 7 - Challenge ${spieltag}.${spieltag.i} - Karte ${karte.id} | ${spieler.anzahl.x}er Challenge | ${regeln.ZZZ}");
		gs.getTags().add("CCC");
		gs.setSeed("Crash^7");
		gs.setCreator(cache.getUser("CraZZZy"));
		gs.setCreatorParticipation(EnumCreatorParticipation.not_participating);
		gs.setIgnoreInvitable(true);
		gs.set(GameSeries.NUMBER_OF_MAPS, CHALLENGES);
		gs.set(GameSeries.V2_TEAM_BASED, false);

		// aps
		gs.setMapsByKey(null);
		
		
		
		GameSeriesManager.store(gs);
	}
	
	public void createGameDay(GameSeries gs, ) {
		gs.
	}
}
