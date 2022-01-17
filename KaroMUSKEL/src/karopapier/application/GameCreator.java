package karopapier.application;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import karopapier.model.Game;
import karopapier.model.GameSeries;
import karopapier.model.Karopapier;
import karopapier.model.Map;
import karopapier.model.Player;

public class GameCreator {
	private static final String name = "name=%NAME";
	private static final String nonCriticalChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	private static final String map = "mapid=%MAPID";
	private static final String player = "teilnehmer[%I]=%PID";
	private static final String checkpoints = "checkers=on";
	private static final String zzz = "zzz=%ZZZ";
	private static final String crashs = "crashallowed=%MODE";
	private static final String directionS = "startdirection=%MODE";

	public static String[] tacticalCrashes = {"Taktischer Crash NICHT GEDULDET", "egal", "Taktischer Crash erlaubt"};
	public static int[]    tacticalCrashesI = {2, 0, 1};	
	public static String[] direction = {"egal, beide Richtungen", "Klassisch Karopapier - Weg vom Ziel", "Formula 1 - Erst die Ziellinie queren"};
	public static int[]    directionI = {0, 1, 2};	
	public static String[] checkpointsOption = {"ja", "nein"};
	public static boolean[] checkpointsOptionB = {true, false};
	public static String[] mapTypes = {"Alle", "Normal", "Nacht"};
	public static String[] addPLayersOption = {"Füge keine Spieler hinzu", "Füge Spieler hinzu bis Karten-Maximum", "Füge Spieler hinzu bis maximal..."};
	
	private String server;
	private String newGameURLString;
	
	private KaroThreadQueue urlLoadQ;
	
	public GameCreator() {
		this.server = KaropapierLoader.server;
		this.newGameURLString = KaropapierLoader.newGameURLString;
		this.urlLoadQ = null;
	}
	
	public Karopapier createGameSeries(GameSeries gs, int maxLoadThreads) {
		urlLoadQ = new KaroThreadQueue(maxLoadThreads, gs.getProgressFrame(), gs.getNoG());
		Karopapier karopapier = gs.getKaropapier();		
		
		Game g = null;
		String name = null;
		Map map = null;
		ArrayList<Player> players = null;
		// mögliche Karten
		ArrayList<Map> possMaps = new ArrayList<Map>();
		if(gs.isRandomMap()) {
			int requiredPlayers = Math.max(gs.getKatA().size()+1, gs.getMinPlayers());
			for(Integer i: karopapier.getMaps().keySet()) {
				Map m = karopapier.getMaps().get(i);
				if(	m.getMaxPlayers() >= requiredPlayers) {
					if(gs.getMapType().equals(mapTypes[0])) {
						possMaps.add(m);
					} else if(gs.getMapType().equals(mapTypes[1])) {
						if(!m.isNight())
							possMaps.add(m);
					} else if(gs.getMapType().equals(mapTypes[2])) {
						if(m.isNight())
							possMaps.add(m);
					}
				}
			}
		}				
		// spiele erstellen - START
		for(int i = gs.getCountStart(); i < gs.getCountStart()+gs.getNoG(); i++) {
			g = new Game();
			// rules
			g.setCheckpoints(gs.isChecks());
			g.setZzz(gs.getZzz());
			g.setCrashs(gs.getCrashs());
			g.setDirection(gs.getDirection());
			// name
			name = gs.getName().replace(gs.getNumberPattern(), toMinDigitsString(i, gs.getMinDigits()));
			name = name.replace(gs.getRandomPattern(), generateName(gs.getRandomChars()));
			g.setName(name);
			// players die dabei sein müssen
			players = new ArrayList<Player>();
			players.add(karopapier.getCurrentPlayer());
			for(String key: gs.getKatA().keySet()) {
				players.add(gs.getKatA().get(key));
			}
			// map
			if(gs.isRandomMap()) {
				map = possMaps.get((int)(Math.random()*possMaps.size()));
			} else
				map = gs.getMap();
			g.setMap(map);
			// added players
			int playersLimit = 0;
			if(gs.getAddPlayer().equals(addPLayersOption[0])) {
				playersLimit = 0;
			} else if(gs.getAddPlayer().equals(addPLayersOption[1])) {
				playersLimit = map.getMaxPlayers();
			} else if(gs.getAddPlayer().equals(addPLayersOption[2])) {
				playersLimit = gs.getMaxPlayers();
			}
			ArrayList<Player> katB = new ArrayList<Player>();
			for(String key: gs.getKatB().keySet()) {
				katB.add(gs.getKatB().get(key));
			}
			while(katB.size() > 0) {
				if(players.size() < playersLimit) {
					int a = (int)(Math.random()*katB.size());
					Player p = katB.get(a);
					katB.remove(a);
					// EINLADBARKEIT
					if(p.isInvitable(map.isNight()))
						players.add(p);
				} else
					break;
			}
			ArrayList<Player> katC = new ArrayList<Player>();
			for(String key: gs.getKatC().keySet()) {
				katC.add(gs.getKatC().get(key));
			}
			while(katC.size() > 0) {
				if(players.size() < playersLimit) {
					int a = (int)(Math.random()*katC.size());
					Player p = katC.get(a);
					katC.remove(a);
					// EINLADBARKEIT
					if(p.isInvitable(map.isNight()))
						players.add(p);
				} else
					break;
			}			
			for(Player p: players) {
				// spiele erhöhen
				p.setGamesAct(p.getGamesAct()+1);
			}
			g.setPlayers(players);
			
			// spiel erstellt...
			// thread starten
			launchGame(g);
		}
		// spiele erstellen - ENDE
		
		return karopapier;
	}
	
	public void launchGame(Game game) {
		String urlS = createNewGameURL(game);
		URL url;
		try {
			url = new URL(urlS.substring(0, urlS.indexOf("?")));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		String parameter = urlS.substring(urlS.indexOf("?") + 1);
		URLLoaderThread th = new URLLoaderThread(url, parameter);
		this.urlLoadQ.addThread(th);
	}

	public String createNewGameURL(Game game){
		StringBuilder gameUrl = new StringBuilder();
		
		if(!game.isComplete())
			return null;
		
		gameUrl.append(this.server);
		gameUrl.append((this.newGameURLString.charAt(0) == '/' ? "" : "/") + this.newGameURLString);
		gameUrl.append("?");
		gameUrl.append(name.replace("%NAME", makeNameURLReady(game.getName())));
		gameUrl.append("&");
		gameUrl.append(map.replace("%MAPID", "" + game.getMap().getId()));
		gameUrl.append("&");
		int i = 0;
		for(Player p: game.getPlayers()) {
			gameUrl.append(player.replace("%I", "" + i++).replace("%PID", "" + p.getId()));
			gameUrl.append("&");
		}
		if(game.hasCheckpoints()) {
			gameUrl.append(checkpoints);
			gameUrl.append("&");
		}
		gameUrl.append(zzz.replace("%ZZZ", "" + game.getZzz()));
		gameUrl.append("&");
		gameUrl.append(crashs.replace("%MODE", "" + game.getCrashs()));	
		gameUrl.append("&");
		gameUrl.append(directionS.replace("%MODE", "" + game.getDirection()));		
		
		return gameUrl.toString();
	}
	
	public String makeNameURLReady(String name) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < name.length(); i++) {
			if(nonCriticalChars.indexOf(name.charAt(i)) == -1)
				sb.append(charToASCII(name.charAt(i)));
			else
				sb.append(name.charAt(i));
		}
		return name;
	}
	
	public String charToASCII(char c) {
		return "%" + Integer.toHexString((int)c);
	}
	
	public String toMinDigitsString(int i, int minDigits) {
		String iS = "" + i;
		while(iS.length() < minDigits) {
			iS = "0" + iS;
		}
		return iS;
	}
	
	public String generateName(int numberOfChars) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < numberOfChars; i++) {
			int x = (int) (Math.random()*nonCriticalChars.length());
			sb.append(nonCriticalChars.charAt(x));
		}
		return sb.toString();
	}
}
