package muskel2.core.karoaccess;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import muskel2.core.web.SimpleCookieHandler;
import muskel2.gui.help.MapRenderer;
import muskel2.model.Game;
import muskel2.model.Karopapier;
import muskel2.model.Map;
import muskel2.model.Player;
import muskel2.util.HtmlUtil;

public class KaropapierLoader
{
	private static CookieHandler	ch;
	private static String			currentUser;

	public static final String		server					= "http://www.karopapier.de";
	public static final String		serverReloaded			= "http://reloaded.karopapier.de";

	public static final String		newGameURLString		= "newgame.php";
	private static final String		newGameURL2String		= "creategame.php";

	private static final String		loginURLString			= "anmelden.php";

	private static final String		playerURLString			= "users.php";

	private static final String		loginPattern			= "ID=%ID&PWD=%PWD";

	private static final String		gameListString			= "showgames.php";
	private static final String		gameListParams			= "limit=%I&nurmeine=1";

	private static BufferedImage	whiteImage;
	private static BufferedImage	blackImage;
	private static final int		standardImageLoadSize	= 100;

	static
	{
		whiteImage = new BufferedImage(MapRenderer.imageWidth, MapRenderer.imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2dw = whiteImage.createGraphics();
		g2dw.setColor(Color.white);
		g2dw.fillRect(0, 0, MapRenderer.imageWidth, MapRenderer.imageHeight);

		blackImage = new BufferedImage(MapRenderer.imageWidth, MapRenderer.imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2db = blackImage.createGraphics();
		g2db.setColor(Color.black);
		g2db.fillRect(0, 0, MapRenderer.imageWidth, MapRenderer.imageHeight);
	}

	public static Karopapier initiateKaropapier() throws IOException
	{
		URL newGameURL = new URL(server + (newGameURLString.charAt(0) == '/' ? "" : "/") + newGameURLString);

		URL newGameURL2 = new URL(server + (newGameURL2String.charAt(0) == '/' ? "" : "/") + newGameURL2String);

		URL playerURL = new URL(server + (newGameURLString.charAt(0) == '/' ? "" : "/") + playerURLString);

		System.out.println("Initiating content from newGamePage " + newGameURL.toString());
		System.out.println("                   and newGamePage2 " + newGameURL2.toString());
		System.out.println("                     and playerPage " + playerURL.toString());

		System.out.print("Downloading newGamePage...");
		String newGamePage = readPage(newGameURL, "");
		newGamePage = readPage(newGameURL, "usershow=all"); // wegen redirect...
		System.out.println(" OK!");

		System.out.print("Downloading newGamePage2...");
		String newGamePage2 = readPage(newGameURL2, "ChangeMap=Ändern"); // für
																			// Karteninfos
		System.out.println(" OK!");

		System.out.print("Downloading playerPage...");
		String playerPage = readPage(playerURL, "");
		System.out.println(" OK!");

		System.out.print("Processing Player information from playerPage...");
		List<Player> playersFromPlayersPage = processPlayerPage(playerPage);
		System.out.println(" Done!");

		System.out.print("Processing Map and Player information from newGamePage & newGamePage2...");
		Karopapier karopapier = processNewGamePages(server, newGamePage, newGamePage2, playersFromPlayersPage);
		System.out.println(" Done!");

		return karopapier;
	}

	public static ArrayList<Player> processLoginPage(String loginPage)
	{
		ArrayList<Player> players = new ArrayList<Player>();

		int currentIndex = 0;
		while (true)
		{
			int start = loginPage.indexOf("<OPTION VALUE=\"", currentIndex);
			if (start == -1)
				break;
			start = start + "<OPTION VALUE=\"".length();
			int end = loginPage.indexOf("\">", start);
			if (end == -1)
				break;
			currentIndex = end;

			String playerName = loginPage.substring(start, end);
			if (playerName == null)
				break;

			Player currentPlayer = new Player();
			currentPlayer.setName(playerName);
			players.add(currentPlayer);
		}

		return players;
	}

	public static List<Player> processPlayerPage(String playerPage)
	{
		List<Player> players = new LinkedList<Player>();

		int currentIndex = 0;
		while (true)
		{
			int start = playerPage.indexOf("userinfo.php?about=", currentIndex);
			if (start == -1)
				break;
			start = start + "userinfo.php?about=".length();
			int end = playerPage.indexOf("BGCOLOR=", start);
			end = playerPage.indexOf(">", end);
			if (end == -1)
				break;
//			end = end + "BGCOLOR=".length() + 6;
			currentIndex = end;

			String playerS = playerPage.substring(start, end);
			if (playerS == null)
				break;

			String idS = playerS.substring(0, playerS.indexOf(">"));
			String name = playerS.substring(playerS.indexOf("<B>") + "<B>".length(), playerS.indexOf("</B>"));
			String colorS = "#" + playerS.substring(playerS.indexOf("BGCOLOR=") + "BGCOLOR=".length());

			Player currentPlayer = new Player();
			currentPlayer.setId(Integer.parseInt(idS));
			currentPlayer.setName(name);
			currentPlayer.setColor(Color.decode(colorS));

			players.add(currentPlayer);
		}

		return players;
	}

	public static Karopapier processNewGamePages(String server, String newGamePage, String newGamePage2, List<Player> playersFromPlayerPage) throws MalformedURLException, IOException
	{
		int currentIndex;

		// Initialisierung
		TreeMap<Integer, Map> maps = new TreeMap<Integer, Map>();
		TreeMap<String, Player> players = new TreeMap<String, Player>();
		boolean unlocked = KaropapierLoader.checkUnlockFile(currentUser);

		// maps lesen aus newGamePage
		String firstSelect = newGamePage.substring(newGamePage.indexOf("<SELECT NAME=mapid"), newGamePage.indexOf("</SELECT>", newGamePage.indexOf("<SELECT NAME=mapid")) + "</SELECT>".length());
		String mapS = null, mapId, mapName, maxPlayers, creator;
		currentIndex = 0;
		while (true)
		{
			try
			{
				int start = firstSelect.indexOf("<OPTION VALUE=", currentIndex);
				if (start == -1)
					break;
				start = start + "<OPTION VALUE=".length();
				int end = firstSelect.indexOf("<", start);
				if (end == -1)
				{
					break;
				}
				currentIndex = end;
	
				mapS = firstSelect.substring(start, end);
				if (mapS == null)
					break;
	
				mapId = mapS.substring(0, mapS.indexOf(">"));
				mapName = mapS.substring(mapS.indexOf(mapId + "", (mapId + "").length()) + (mapId + "").length(), mapS.indexOf("("));
				if (mapName.equals(" "))
					mapName = "Karte " + mapId;
				else
				{
					mapName = mapName.substring(mapName.indexOf(":") + 1).trim();
				}
				mapName = HtmlUtil.fixHtml(mapName);
				maxPlayers = mapS.substring(mapS.indexOf("(") + 1, mapS.indexOf(" Spieler"));
	
				Map currentMap = new Map();
				currentMap.setId(Integer.parseInt(mapId));
				currentMap.setName(mapName);
				currentMap.setMaxPlayers(Integer.parseInt(maxPlayers));
				currentMap.setNight(currentMap.getId() < 1000 ? false : true);
				currentMap.setImage(readImage(currentMap.getId(), currentMap.isNight(), false));
				maps.put(currentMap.getId(), currentMap);
			}
			catch(Exception e)
			{
				System.out.println("could not parse map '" + mapS + "'");
			}
		}

		// kartenersteller lesen aus newGamePage2
		currentIndex = 0;
		while (true)
		{
			int start = newGamePage2.indexOf("return overlib('", currentIndex);
			if (start == -1)
				break;
			start = start + "return overlib('".length();
			int end = newGamePage2.indexOf(" Spieler: <IMG SRC", start);
			if (end == -1)
			{
				break;
			}
			currentIndex = end;

			String tooltip = newGamePage2.substring(start, end);
			if (tooltip == null)
				break;

			mapId = tooltip.substring(0, tooltip.indexOf(" "));
			creator = tooltip.substring(tooltip.indexOf("&quot; von ") + "&quot; von ".length(), tooltip.indexOf(", ", tooltip.indexOf("&quot; von ") + "&quot; von ".length()));

			if(maps.get(Integer.parseInt(mapId)) != null)
				maps.get(Integer.parseInt(mapId)).setCreator(creator);
		}

		if (unlocked)
		{
			// Spezial Map added!!!
			addSpecialMaps(maps);
		}

		// player lesen aus newGamePage
		String secondSelect = newGamePage.substring(newGamePage.indexOf("<SELECT NAME=teilnehmer"),
				newGamePage.indexOf("</SELECT>", newGamePage.indexOf("<SELECT NAME=teilnehmer")) + "</SELECT>".length());
		currentIndex = 0;
		while (true)
		{
			int start = secondSelect.indexOf("<OPTION VALUE=", currentIndex);
			if (start == -1)
				break;
			start = start + "<OPTION VALUE=".length();
			int end = secondSelect.indexOf("<", start);
			if (end == -1)
			{
				break;
			}
			currentIndex = end;

			String playerS = secondSelect.substring(start, end);
			if (playerS == null)
				break;

			String plId = playerS.substring(0, playerS.indexOf(">"));
			String plName;
			try
			{
				plName = playerS.substring(playerS.indexOf(plId + "", (plId + "").length()) + (plId + "").length() + 2, playerS.indexOf("[")).trim();
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// trenner für mehr als 3 Tage weg...
				continue;
			}
			if (plName.indexOf("(BOT): ") != -1)
				plName = plName.substring(plName.indexOf("(BOT): ") + "(BOT): ".length());
			boolean nightB = true;
			if (plName.indexOf("(keine Nachtrennen)") != -1)
			{
				plName = plName.substring(0, plName.lastIndexOf("("));
				nightB = false;
			}
			String activeSinceS = playerS.substring(playerS.lastIndexOf("[") + 1, playerS.lastIndexOf("|"));
			String lastVisitedS = playerS.substring(playerS.lastIndexOf("|") + 1, playerS.lastIndexOf(" @ "));
			String gamesActS = playerS.substring(playerS.lastIndexOf(" @ ") + 3, playerS.lastIndexOf("/"));
			String gamesMaxS = playerS.substring(playerS.lastIndexOf("/") + 1, playerS.lastIndexOf("]"));

			Player currentPlayer = new Player();
			currentPlayer.setId(Integer.parseInt(plId));
			currentPlayer.setName(plName);
			currentPlayer.setActiveSince(Integer.parseInt(activeSinceS));
			currentPlayer.setLastVisited(Integer.parseInt(lastVisitedS));
			try
			{
				currentPlayer.setGamesAct(Integer.parseInt(gamesActS));
				currentPlayer.setGamesActOrPlanned(Integer.parseInt(gamesActS));
			}
			catch (NumberFormatException e)
			{
				currentPlayer.setGamesAct(0);
			}
			try
			{
				currentPlayer.setGamesMax((gamesMaxS.equals("&#8734;") ? 0 : Integer.parseInt(gamesMaxS)));
			}
			catch (NumberFormatException e)
			{
				System.out.println(plName);
				throw e;
			}
			currentPlayer.setInvitableNormal(true);
			currentPlayer.setInvitableNight(nightB);
			players.put(currentPlayer.getName().toLowerCase(), currentPlayer);
		}

		// player seiten player einarbeiten
		for (Player p : playersFromPlayerPage)
		{
			if (!players.containsKey(p.getName().toLowerCase()))
			{
				players.put(p.getName().toLowerCase(), p);
			}
			else
			{
				players.get(p.getName().toLowerCase()).setId(p.getId());
				players.get(p.getName().toLowerCase()).setColor(p.getColor());
			}
		}

		return new Karopapier(maps, players, currentUser, unlocked);
	}

	public static void addSpecialMaps(TreeMap<Integer, Map> maps)
	{
		maps.put(25, new Map(25, HtmlUtil.fixHtml("Nadelöhr"), maps.get(37).getCreator(), false, 4, readImage(25, false, true)));
		maps.put(30, new Map(30, HtmlUtil.fixHtml("Karo-F (alt)"), maps.get(43).getCreator(), false, 10, readImage(30, false, true)));
		maps.put(31, new Map(31, HtmlUtil.fixHtml("Nadelöhr II (alt)"), maps.get(37).getCreator(), false, 10, readImage(31, false, true)));
		maps.put(32, new Map(32, HtmlUtil.fixHtml("Flugplatz (alt)"), maps.get(34).getCreator(), false, 20, readImage(32, false, true)));
		maps.put(39, new Map(39, HtmlUtil.fixHtml("Frischer Fisch"), maps.get(41).getCreator(), false, 6, readImage(39, false, true)));
		maps.put(47, new Map(47, HtmlUtil.fixHtml("Klapperschlange (alt)"), maps.get(54).getCreator(), false, 8, readImage(47, false, true)));
		maps.put(48, new Map(48, HtmlUtil.fixHtml("Die Checkpointteststrecke"), maps.get(1).getCreator(), false, 5, readImage(48, false, true)));
		maps.put(52, new Map(52, HtmlUtil.fixHtml("Dubya (groß)"), maps.get(57).getCreator(), false, 18, readImage(52, false, true)));
		maps.put(81, new Map(81, HtmlUtil.fixHtml("Checkpointtester"), maps.get(1).getCreator(), false, 3, readImage(81, false, true)));
		maps.put(100, new Map(100, HtmlUtil.fixHtml("Das Haus vom Nikolaus (alt)"), maps.get(109).getCreator(), false, 5, readImage(100, false, true)));
		maps.put(106, new Map(106, HtmlUtil.fixHtml("Für Annie :wavey: (alt)"), maps.get(108).getCreator(), false, 9, readImage(106, false, true)));
		maps.put(113, new Map(113, HtmlUtil.fixHtml("Knubbelbubbeldings (alt)"), maps.get(121).getCreator(), false, 4, readImage(113, false, true)));
		maps.put(115, new Map(115, HtmlUtil.fixHtml("lol (alt)"), maps.get(118).getCreator(), false, 10, readImage(115, false, true)));
		maps.put(144, new Map(144, HtmlUtil.fixHtml("Jodys großer Knoten"), maps.get(143).getCreator(), false, 10, readImage(144, false, true)));
		maps.put(177, new Map(177, HtmlUtil.fixHtml("City Race (alt)"), maps.get(178).getCreator(), false, 8, readImage(177, false, true)));
		maps.put(186, new Map(177, HtmlUtil.fixHtml("Yun2 (alt)"), maps.get(187).getCreator(), false, 7, readImage(186, false, true)));
	}

	public static Image readImage(int mapId, boolean night, boolean special)
	{
		Image image = night ? blackImage : whiteImage;
		if (!night)
		{
			try
			{
				image = ImageIO.read(new URL(serverReloaded + "/map/" + mapId + ".png?width=" + standardImageLoadSize + "&border=0"));
			}
			catch (IOException e1)
			{
				try
				{
					image = ImageIO.read(new URL(server + "/previews/" + mapId + ".png"));
				}
				catch (IOException e2)
				{
					try
					{
						image = ImageIO.read(new URL(server + "/oo/viewmap.php?MID=" + mapId + "&SIZE=8&BORDER=0"));
					}
					catch (IOException e3)
					{
						special = true;
					}
				}
			}
		}
		if(special || night)
			return specialImage(image);
		return image;
	}

	private static Image specialImage(Image image)
	{
		BufferedImage image2 = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2d = image2.createGraphics();
		g2d.drawImage(image, 0, 0, null);
		g2d.setColor(Color.red);
		int size = (int) Math.min(image2.getWidth() * 0.7F, image2.getHeight() * 0.7F);
		g2d.setStroke(new BasicStroke(size / 7));
		g2d.drawOval((image2.getWidth() - size) / 2, (image2.getHeight() - size) / 2, size, size);
		int delta = (int) (size / 2 * 0.707F);
		g2d.drawLine(image2.getWidth() / 2 - delta, image2.getHeight() / 2 + delta, image2.getWidth() / 2 + delta, image2.getHeight() / 2 - delta);
		return image2;
	}

	public static String readPage(URL url, String parameter) throws IOException
	{
		StringBuilder site = new StringBuilder();

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setAllowUserInteraction(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		PrintWriter out = new PrintWriter(connection.getOutputStream());
		out.print(encodeParameters(parameter));
		out.close();

		connection.connect();
		InputStream is = connection.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(is);
		int curr = bis.read();
		while (curr != -1)
		{
			site.append((char) curr);
			curr = bis.read();
		}
		bis.close();
		is.close();

		return site.toString();
	}

	public static boolean login(String username, String password) throws IOException
	{
		currentUser = username;
		ch = new SimpleCookieHandler();
		CookieHandler.setDefault(ch);

		URL url = new URL(server + (loginURLString.charAt(0) == '/' ? "" : "/") + loginURLString);
		String page = readPage(url, loginPattern.replace("%ID", username).replace("%PWD", password));

		boolean success = page.contains("Login erfolgreich");

		return success;
	}

	public static void findIds(List<Game> gameList) throws IOException
	{
		URL gameListURL = new URL(server + (gameListString.charAt(0) == '/' ? "" : "/") + gameListString);
		String gameListPage;

		System.out.println("Suche nach Spiel-IDs...");

		int count = 0;
		boolean gameFound = true;
		while (gameFound)
		{
			gameFound = false;

			while (true)
			{
				try
				{
					gameListPage = readPage(gameListURL, gameListParams.replace("%I", "" + count));
					break;
				}
				catch (Exception e)
				{

				}
			}

			int currentIndex = 0;
			while (true)
			{
				int start = gameListPage.indexOf("<A HREF=showmap.php?", currentIndex) + "<A HREF=showmap.php?".length();
				start = gameListPage.indexOf("<A HREF=showmap.php?", start);
				if (start == -1)
					break;
				start = start + "<A HREF=showmap.php?".length();
				int start2 = gameListPage.indexOf(">", start) + 1;
				int end = gameListPage.indexOf("<", start2);
				if (end == -1)
					break;
				currentIndex = end;

				String name = gameListPage.substring(start2, end).trim();
				if (name.isEmpty())
					break;
				count++;
				gameFound = true;

				int id = Integer.parseInt(gameListPage.substring(start + "GID=".length(), gameListPage.indexOf("&", start)));
				System.out.println(" --> " + name + " (" + id + ")");

				for (Game game : gameList)
				{
					if (game.getName().equals(name))
					{
						if (game.getId() == null || game.getId() < id)
							game.setId(id);
					}
				}
			}
		}
	}

	public static String createUnlockKey(String username)
	{
		TreeMap<Character, List<Integer>> chars = new TreeMap<Character, List<Integer>>();

		Character curr;
		List<Integer> positions;
		StringBuilder unlockKey = new StringBuilder();
		int count = 0;
		long check = 0;
		int tmp;
		String tmpS;

		for (int i = 0; i < username.length(); i++)
		{
			curr = username.charAt(i);
			if (!chars.containsKey(curr))
			{
				chars.put(curr, new LinkedList<Integer>());
			}
			chars.get(curr).add(i);
		}

		for (Character key : chars.keySet())
		{
			positions = chars.get(key);
			tmp = (int) key.charValue();
			for (Integer position : positions)
			{
				tmpS = Integer.toHexString(tmp + position);
				if (tmpS.length() < 2)
					tmpS = "0" + tmpS;
				unlockKey.append(tmpS.toUpperCase());
				check += position * Math.pow(10, count);
				count++;
			}
		}

		tmpS = Long.toHexString(check);
		if (tmpS.length() % 2 == 1)
			tmpS = "0" + tmpS;
		unlockKey.append(tmpS.toUpperCase());

		return unlockKey.toString();
	}

	public static boolean checkUnlockFile(String username)
	{
		String unlockKey = createUnlockKey(username);
		return new File(unlockKey).exists();
	}

	public static void createUnlockFile(String username) throws IOException
	{
		String unlockKey = createUnlockKey(username);
		File file = new File(unlockKey);

		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos);

		for (int i = 0; i < username.length(); i++)
		{
			bos.write(username.charAt(i));
		}

		bos.flush();
		fos.flush();

		bos.close();
		fos.close();
	}
	
	public static String encodeParameters(String s)
	{
		StringBuffer sb = new StringBuffer();

		char c;
		for(int i = 0; i < s.length(); i++)
		{
			c = s.charAt(i);
			if(c != '&' && (c < 0x30 || c > 0x7F))
				sb.append('%').append(Integer.toHexString(c).toUpperCase());
			else
				sb.append(c);
		}
		return sb.toString();
	}
}
