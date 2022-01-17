package karopapier.application;

import java.awt.Color;
import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.ImageIcon;

import karopapier.model.Karopapier;
import karopapier.model.Map;
import karopapier.model.Player;

public class KaropapierLoader {
	
	private static CookieHandler ch;
	private static String currentUser;
	
	public static String server = "http://www.karopapier.de";
	
	public static String newGameURLString = "newgame.php";
	public static String loginURLString = "anmelden.php";
	public static String playerURLString = "users.php";
	
	private static String loginPattern = "ID=%ID&PWD=%PWD";
	

	public static Karopapier initiateKaropapier() throws IOException{
		URL newGameURL = new URL(server + (newGameURLString.charAt(0) == '/' ? "" : "/") + newGameURLString);
		URL playerURL = new URL(server + (newGameURLString.charAt(0) == '/' ? "" : "/") + playerURLString);
		System.out.println("Initiating content from newGamePage " + newGameURL.toString());
		System.out.println("                     and playerPage " + playerURL.toString());
		
		System.out.print("Downloading newGamePage...");
		String newGamePage = readPage(newGameURL, "");
		newGamePage = readPage(newGameURL, "usershow=all"); // wegen redirect...
		System.out.println(" OK!");
		System.out.print("Downloading playerPage...");
		String playerPage = readPage(playerURL, "");
		System.out.println(" OK!");
		
		System.out.print("Processing Player information from playerPage...");
		ArrayList<Player> playersFromPlayersPage = processPlayerPage(playerPage);
		System.out.println(" Done!");
		
		System.out.print("Processing Map and Player information from newGamePage...");
		Karopapier karopapier = processNewGamePage(server, newGamePage, /*playersFromLoginPage,*/ playersFromPlayersPage);	
		System.out.println(" Done!");
		
		return karopapier;
	}
	
	public static ArrayList<Player> processLoginPage(String loginPage) {
		ArrayList<Player> players = new ArrayList<Player>();
		
		int currentIndex = 0;
		while(true) {
			int start = loginPage.indexOf("<OPTION VALUE=\"", currentIndex);
			if(start == -1)
				break;
			start = start + "<OPTION VALUE=\"".length();
			int end = loginPage.indexOf("\">", start);
			if(end == -1)
				break;
			currentIndex = end;
			
			String playerName = loginPage.substring(start, end);
			if(playerName == null)
				break;
			
			Player currentPlayer = new Player();
			currentPlayer.setName(playerName);
			players.add(currentPlayer);
		}
		
		return players;
	}
	
	public static ArrayList<Player> processPlayerPage(String playerPage) {
		ArrayList<Player> players = new ArrayList<Player>();
		
		int currentIndex = 0;
		while(true) {
			int start = playerPage.indexOf("userinfo.php?about=", currentIndex);
			if(start == -1)
				break;
			start = start + "userinfo.php?about=".length();
			int end = playerPage.indexOf("BGCOLOR=", start);
			if(end == -1)
				break;
			end = end + "BGCOLOR=".length() + 6;
			currentIndex = end;
			
			String playerS = playerPage.substring(start, end);
			if(playerS == null)
				break;
			
			String idS = playerS.substring(0, playerS.indexOf(">"));
			String name = playerS.substring(playerS.indexOf("<B>")+"<B>".length(), playerS.indexOf("</B>"));
			String colorS = "#" + playerS.substring(playerS.indexOf("BGCOLOR=")+"BGCOLOR=".length());
			
			Player currentPlayer = new Player();
			currentPlayer.setId(Integer.parseInt(idS));
			currentPlayer.setName(name);
			currentPlayer.setColor(Color.decode(colorS));
			
			players.add(currentPlayer);
		}
		
		return players;
	}
	
	public static Karopapier processNewGamePage(String server, String newGamePage, /*ArrayList<Player> playersFromLoginPage, */ArrayList<Player> playersFromPlayerPage) throws MalformedURLException{
		Karopapier karopapier = new Karopapier();
		
		int currentIndex;

		// maps lesen
		TreeMap<Integer, Map> maps = new TreeMap<Integer, Map>();
		String firstSelect = newGamePage.substring(newGamePage.indexOf("<SELECT NAME=mapid"), newGamePage.indexOf("</SELECT>", newGamePage.indexOf("<SELECT NAME=mapid")) + "</SELECT>".length());
		currentIndex = 0;
		while(true) {
			int start = firstSelect.indexOf("<OPTION VALUE=", currentIndex);
			if(start == -1)
				break;
			start = start + "<OPTION VALUE=".length();
			int end = firstSelect.indexOf("<", start);
			if(end == -1) {
				break;
			}
			currentIndex = end;
			
			String mapS = firstSelect.substring(start, end);
			if(mapS == null)
				break;
			
			String mapId = mapS.substring(0, mapS.indexOf(">")); 
			String mapName = mapS.substring(mapS.indexOf(mapId + "", (mapId + "").length()) + (mapId + "").length(), mapS.indexOf("("));
			if(mapName.equals(" "))
				mapName = "Karte " + mapId;
			else {
				mapName = mapName.substring(mapName.indexOf(":") + 1).trim();
			}
			String maxPlayers = mapS.substring(mapS.indexOf("(") + 1, mapS.indexOf(" Spieler"));
			
			Map currentMap = new Map();
			currentMap.setId(Integer.parseInt(mapId));
			currentMap.setName(mapName);
			currentMap.setMaxPlayers(Integer.parseInt(maxPlayers));
			if(currentMap.getId() < 1000) {
				currentMap.setImage(new ImageIcon(new URL(server + "/previews/" + mapId + ".png")).getImage());
				currentMap.setNight(false);
			} else
				currentMap.setNight(true);
			maps.put(currentMap.getId(), currentMap);
		}
		
		//Spezial Map added!!!
		Map specialMap = new Map(144, "Jodys großer Knoten",false, 10, null);
		maps.put(specialMap.getId(), specialMap);

		// player lesen
		TreeMap<String, Player> players = new TreeMap<String, Player>();
		String secondSelect = newGamePage.substring(newGamePage.indexOf("<SELECT NAME=teilnehmer"), newGamePage.indexOf("</SELECT>", newGamePage.indexOf("<SELECT NAME=teilnehmer")) + "</SELECT>".length());
		currentIndex = 0;
		while(true) {
			int start = secondSelect.indexOf("<OPTION VALUE=", currentIndex);
			if(start == -1)
				break;
			start = start + "<OPTION VALUE=".length();
			int end = secondSelect.indexOf("<", start);
			if(end == -1) {
				break;
			}
			currentIndex = end;
			
			String playerS = secondSelect.substring(start, end);
			if(playerS == null)
				break;
			
			String plId = playerS.substring(0, playerS.indexOf(">")); 
			String plName;
			try {
				plName = playerS.substring(playerS.indexOf(plId + "", (plId + "").length()) + (plId + "").length()+2, playerS.indexOf("[")).trim();
			} catch(StringIndexOutOfBoundsException e) {
				// trenner für mehr als 3 Tage weg...
				continue;
			}
			if(plName.indexOf("(BOT): ") != -1) 
				plName = plName.substring(plName.indexOf("(BOT): ") + "(BOT): ".length());
			boolean nightB = true;
			if(plName.indexOf("(keine Nachtrennen)") != -1) {
				plName = plName.substring(0, plName.lastIndexOf("("));
				nightB = false;
			}
			String activeSinceS = playerS.substring(playerS.lastIndexOf("[")+1, playerS.lastIndexOf("|"));
			String lastVisitedS = playerS.substring(playerS.lastIndexOf("|")+1, playerS.lastIndexOf(" @ "));
			String gamesActS = playerS.substring(playerS.lastIndexOf(" @ ")+3, playerS.lastIndexOf("/"));
			String gamesMaxS = playerS.substring(playerS.lastIndexOf("/")+1, playerS.lastIndexOf("]"));
			
			Player currentPlayer = new Player();
			currentPlayer.setId(Integer.parseInt(plId));
			currentPlayer.setName(plName);
			currentPlayer.setActiveSince(Integer.parseInt(activeSinceS));
			currentPlayer.setLastVisited(Integer.parseInt(lastVisitedS));
			try
			{
				currentPlayer.setGamesAct(Integer.parseInt(gamesActS));
			}
			catch(NumberFormatException e)
			{
				currentPlayer.setGamesAct(0);
			}
			try
			{
				currentPlayer.setGamesMax((gamesMaxS.equals("&#8734;") ? 0 : Integer.parseInt(gamesMaxS)));
			}
			catch(NumberFormatException e)
			{
				System.out.println(plName);
				throw e;
			}
			currentPlayer.setInvitableNormal(true);
			currentPlayer.setInvitableNight(nightB);
			players.put(currentPlayer.getName().toLowerCase(), currentPlayer);
		}
		
		// player seiten player einarbeiten
		for(Player p: playersFromPlayerPage) {
			if(!players.containsKey(p.getName().toLowerCase())){
				players.put(p.getName().toLowerCase(), p);
			} else {
				players.get(p.getName().toLowerCase()).setId(p.getId());
				players.get(p.getName().toLowerCase()).setColor(p.getColor());
			}
		}
		
		karopapier.setMaps(maps);
		karopapier.setPlayers(players);
		karopapier.setCurrentUser(currentUser);

		return karopapier;
	}
	
	public static String readPage(URL url, String parameter) throws IOException {
		StringBuilder site = new StringBuilder();
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	  	connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setAllowUserInteraction(false);
		connection.setRequestMethod("POST");
		
		PrintWriter out = new PrintWriter(connection.getOutputStream());
		out.print(parameter);
		out.close();
		
		connection.connect();
		InputStream is = connection.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(is);
		int curr = bis.read();
		while(curr != -1) {
			site.append((char)curr);
			curr = bis.read();
		}
		bis.close();
		is.close();
		
		return site.toString();
	}
	
	public static Point loadMapsFirstMove(int index){
		File in = new File("maps-save/map-" + index + ".move");
		if(!in.exists())
			return null;
		StringBuilder inS = new StringBuilder();
		try {
			FileInputStream fis = new FileInputStream(in);
			BufferedInputStream bis = new BufferedInputStream(fis);
			
			int curr = bis.read();
			while(curr != -1) {
				inS.append((char)curr);
				curr = bis.read();
			}
			
			bis.close();
			fis.close();
		} catch(IOException e) {
			return null;
		}
		try {
			int x = Integer.parseInt(inS.substring(0, inS.indexOf("|")));
			int y = Integer.parseInt(inS.substring(inS.indexOf("|")+1));
			return new Point(x,y);
		} catch(NumberFormatException e) {
			return null;
		}
	}
	
	public static void saveMapsFirstMove(Map m) throws IOException {
		if(m == null || m.getBestFirstMove() == null)
			return;
		File out = new File("maps-save/map-" + m.getId() + ".move");
		if(!out.getParentFile().exists())
			out.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(out);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		
		String outS = (int)m.getBestFirstMove().getX() + "|" + (int)m.getBestFirstMove().getY();
		for(int i = 0; i < outS.length(); i++)
			bos.write(outS.charAt(i));

		bos.flush();
		fos.flush();
		bos.close();
		fos.close();
	}
	
	public static ArrayList<String> getGameURLsForPlayer(String playerName) {
		ArrayList<String> gameURLs = new ArrayList<String>();
		// TODO
		return gameURLs;
	}
	
	public static boolean login(String username, String password) throws IOException{		
		currentUser = username;
		ch = new SimpleCookieHandler();
		CookieHandler.setDefault(ch);
		
		URL url = new URL (server + (loginURLString.charAt(0) == '/' ? "" : "/") + loginURLString);
		String page = readPage(url, loginPattern.replace("%ID", username).replace("%PWD", password));
		
		return page.contains("Login erfolgreich");
	}
}
