package ultimate.karopapier.ccc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;

import com.fasterxml.jackson.core.type.TypeReference;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumCreatorParticipation;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karoapi4j.model.official.Generator;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.JSONUtil;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.Planner;
import ultimate.karomuskel.ui.Language;

public class CCCGeneratorPlanner
{
	private static final int GAME_DAYS_VIRTUALLY = 25;
	private static final int GAMES_PER_PLAYER_PER_VIRTUAL_GAME_DAY = 6;
	private static final int GAMES_PER_PLAYER_PER_REAL_GAME_DAY = 1;
	private static final int VARIATIONS_PER_GAME_DAY = GAMES_PER_PLAYER_PER_VIRTUAL_GAME_DAY / GAMES_PER_PLAYER_PER_REAL_GAME_DAY;
	private static final int CHALLENGES = GAME_DAYS_VIRTUALLY * VARIATIONS_PER_GAME_DAY;
	private static final String RANGE_DELIM = "-";
	private static final String GENERATOR_PLACEHOLDER = "%G%";
	private static final String CHALLENGE_NAME = "7 - Challenge ";

	/*
		src/main/resources/login.properties
		"../CraZZZy Crash Challenge/CCC7/czzzcc7-generator-settings.csv"
		"../CraZZZy Crash Challenge/CCC7/czzzcc7-participants.txt"
		src/main/resources/czzzcc7.json
	 */
	public static void main(String[] args) throws IOException {
		try {
			File loginProperties = new File(args[0]);
			File challenges = new File(args[1]);
			File participants = new File(args[2]);
			File outputFile = new File(args[3]);
			
			// initialize karoapi
			Properties login = PropertiesUtil.loadProperties(loginProperties);
			KaroAPI api = new KaroAPI(login.getProperty("karoAPI.user"), login.getProperty("karoAPI.password"));
			KaroAPICache cache = new KaroAPICache(api, login);
			cache.refresh().join();		
			
			// create instance 
			String title = "CraZZZy Crash Challenge 7 - Challenge ${spieltag}.${spieltag.i} - " + GENERATOR_PLACEHOLDER + " | ${spieler.anzahl.x}er Challenge | ${regeln.zzz}";
			CCCGeneratorPlanner p = new CCCGeneratorPlanner(cache, title, "Crash^7");
			
			// read participants
			try(BufferedReader br = new BufferedReader(new FileReader(participants))) {
				String line;
				while((line = br.readLine()) != null) {
					User user = cache.getUser(line);
					if(user != null) {
						p.getGameSeries().getPlayers().add(user);
						System.out.println("- user '" + line + "' added");
					} else {
						System.out.println("- user '" + line + "' not found");
					}
				}
			}
			
			List<ChallengeConfig> configs = new ArrayList<>();
			
			// read challenges & create game days
			try(BufferedReader br = new BufferedReader(new FileReader(challenges))) {
				String line;
				while((line = br.readLine()) != null) {
					ChallengeConfig challengeConfig = parseLine(line);
					p.createGameDaysFromConfig(challengeConfig, VARIATIONS_PER_GAME_DAY, GAMES_PER_PLAYER_PER_REAL_GAME_DAY);
					configs.add(challengeConfig);
				}
			}
			
			// get the result
			GameSeries gs = p.getGameSeries();
			
			// initialize KaroMUSKEL (what's needed for planning)
			Language.load("de");
			
			// plan the games
			Planner planner = new Planner();
			List<PlannedGame> plannedGames = planner.planSeries(gs);
			
			// add the generator variation to the names
			System.out.println("modifying game names...");
			Map<String, Integer> generatorUsages = new HashMap<String, Integer>();
			for(int i = 0; i < GAME_DAYS_VIRTUALLY; i++) {
				int realChallenge = (i+1);
				ChallengeConfig config = configs.get(i);
				int generatorUsage = generatorUsages.getOrDefault(config.generatorKey, 0);
				
				int challengeStart = i * VARIATIONS_PER_GAME_DAY + 1;
				int challengeEnd = i * VARIATIONS_PER_GAME_DAY + VARIATIONS_PER_GAME_DAY;
				
				String replacement;
				if("u7".equals(config.generatorKey)) {
					Generator tmp = JSONUtil.deserialize(config.generatorSerialization, new TypeReference<Generator>() {});
					String mid = (String) tmp.getSettings().get("mid");
					replacement = config.generatorKey + " Map " + mid;
				}
				else
					replacement = config.generatorKey + " Variante " + (generatorUsage + 1);
				System.out.println("- challenge #" + challengeStart + "-" + challengeEnd + " -> replacing " + GENERATOR_PLACEHOLDER + " -> " + replacement);
				
				for(int c = challengeStart, ci = 1; c <= challengeEnd; c++, ci++) {
					int cf = c;
					int cif = ci;
					plannedGames.stream().filter(pg -> pg.getName().contains(CHALLENGE_NAME + cf)).forEach(pg -> {
						String newName = pg.getName()
//								.replace(CHALLENGE_NAME + cf, "Challenge " + realChallenge + "." + cif)
								.replace(GENERATOR_PLACEHOLDER, replacement);
						pg.setName(newName);
					});
				}

				generatorUsages.put(config.generatorKey, generatorUsage+1);
			}
			
			gs.getGames().put("Balanced", plannedGames);
			
			GameSeriesManager.store(gs, outputFile);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
	
	public static class ChallengeConfig {
		int challenge;
		String generatorKey;
		String generatorSerialization;
		int zzzMin;
		int zzzMax;
		boolean cps;
		int players;		
	}
	
	private static ChallengeConfig parseLine(String line) {
		String[] parts = line.split("\t");
		ChallengeConfig cc = new ChallengeConfig();
		cc.challenge 				= Integer.parseInt(parts[0]);
		cc.generatorKey 			= parts[1];
		cc.generatorSerialization	= "{\"key\":\"" + cc.generatorKey + "\",\"settings\": " + parts[2] + "}";;
		int[] zzzRange 				= parseIntRange(parts[3]);
		cc.zzzMin 					= zzzRange[0];
		cc.zzzMax 					= zzzRange[1];
		cc.cps 						= "ja".equalsIgnoreCase(parts[4]);
		//cc.direction 				= EnumGameDirection.valueOf(parts[5]); // always free
		//cc.estimatedMoves 		= Integer.parseInt(parts[6]); // irrelevant
		cc.players 					= Integer.parseInt(parts[7]);
		return cc;
	}
	
	private static int[] parseIntRange(String range) {
		int part1 = Integer.parseInt(range.substring(0, range.indexOf(RANGE_DELIM)));
		int part2 = Integer.parseInt(range.substring(range.indexOf(RANGE_DELIM) + 1));
		return new int[] {part1, part2};
	}
	
	private static double[] parseDoubleRange(String range) {
		double part1 = Double.parseDouble(range.substring(0, range.indexOf(RANGE_DELIM)));
		double part2 = Double.parseDouble(range.substring(range.indexOf(RANGE_DELIM) + 1));
		return new double[] {part1, part2};
	}
	
	private KaroAPICache cache;
	private GameSeries gs;
	private Random rnd;
	private int challengeCounter = 0;
	
	public CCCGeneratorPlanner(KaroAPICache cache, String title, String seed) {
		this.cache = cache;
		
		// create empty gameseries
		gs = new GameSeries(EnumGameSeriesType.Balanced);
		// general settings (from SettingsSc
		gs.setTitle(title);
		gs.getTags().add("CCC");
		gs.setSeed(seed);
		gs.setCreator(this.cache.getUser("CraZZZy"));
		gs.setCreatorParticipation(EnumCreatorParticipation.not_participating);
		gs.setIgnoreInvitable(true);
		gs.set(GameSeries.NUMBER_OF_MAPS, CHALLENGES);
		gs.set(GameSeries.V2_TEAM_BASED, false);
		
		rnd = new Random(seed.hashCode());
	}

	public GameSeries getGameSeries() {
		return gs;
	}
	
	public void createGameDaysFromConfig(ChallengeConfig config, int variations, int gamesPerPlayerPerRealGameDay) {
		String key = config.generatorKey.toLowerCase();
		Generator base = cache.getGenerators().stream().filter(g -> key.equalsIgnoreCase(g.getKey())).findFirst().get();
		for(int i = 0; i < variations; i++) {
			System.out.println("challenge #" + (challengeCounter + 1) + " -> on generator: " + base);
			
			// load generator settings
			Generator tmp = JSONUtil.deserialize(config.generatorSerialization, new TypeReference<Generator>() {});
			cleanUpSettings(tmp);
			randomize(tmp, rnd);
			// apply them to the generator
			Generator g = base.copy();
			for(Entry<String, Object> entry: tmp.getSettings().entrySet()) {
				g.getSettings().put(entry.getKey(), entry.getValue());
			}
			// set this as a map
			gs.getMapsByKey().put("" + challengeCounter, Arrays.asList(g));
			System.out.println("- " + g.toSettingsString(true));
			
			// randomize zzz but make it fixed for the challenge
			int zzz = rnd.nextInt(config.zzzMax - config.zzzMin + 1) + config.zzzMin;
			
			// set rules
			Rules rules = new Rules();
			rules.setCps(config.cps);
			rules.setCrashallowed(EnumGameTC.allowed);
			rules.setGamesPerPlayer(gamesPerPlayerPerRealGameDay);
			rules.setMaxZzz(zzz);
			rules.setMinZzz(zzz);
			rules.setNumberOfPlayers(config.players);
			rules.setStartdirection(EnumGameDirection.free);
			gs.getRulesByKey().put("" + challengeCounter, rules);
			System.out.println("- ZZZ: " + rules.getMinZzz() + "-" + rules.getMaxZzz());
			
			// increase counter
			challengeCounter++;
		}
	}
	
	private static void cleanUpSettings(Generator g) {
		// remove unnecessary setting
		g.getSettings().remove("generator");
		// parse numbers where possible
		for(Entry<String, Object> entry: g.getSettings().entrySet()) {
			try {
				int val = Integer.parseInt(entry.getValue().toString());
				g.getSettings().put(entry.getKey(), val);
			}
			catch(Exception e) {
				try {
					double val = Double.parseDouble(entry.getValue().toString());
					g.getSettings().put(entry.getKey(), val);
				}
				catch(Exception e2) {
				}
			}
		}
	}
	
	private static void randomize(Generator g, Random rnd) {
		for(Entry<String, Object> entry: g.getSettings().entrySet()) {
			if(entry.getValue() instanceof String && ((String) entry.getValue()).contains(RANGE_DELIM)) {
				String val = (String) entry.getValue();
				System.out.print("- randomizing " + g.getKey() + "." + entry.getKey() + "=" + val + " -> ");
				if(((String) entry.getValue()).contains(".")) {
					double[] range = parseDoubleRange(val);
					int lower = (int) Math.round(range[0] * 10);
					int upper = (int) Math.round(range[1] * 10);
					double newVal = rnd.nextInt(upper - lower)/10.0 + range[0];
					g.getSettings().put(entry.getKey(), val);
					System.out.println(newVal);
				} else {
					int[] range = parseIntRange(val);
					int newVal = rnd.nextInt(range[1] - range[0] + 1) + range[0];
					g.getSettings().put(entry.getKey(), val);
					System.out.println(newVal);
				}
			}
		}
		g.getSettings().put("seed", rnd.nextInt());
	}
}
