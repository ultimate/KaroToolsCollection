package muskel2.core.karoaccess;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import muskel2.gui.screens.SummaryScreen;
import muskel2.model.Direction;
import muskel2.model.Game;
import muskel2.model.Karopapier;
import muskel2.model.Map;
import muskel2.model.Player;
import muskel2.model.Rules;
import muskel2.util.RequestLogger;

public class GameCreator
{
	private static final String		name				= "name=%NAME";
//	private static final String		nonCriticalChars	= "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	private static final String		map					= "mapid=%MAPID";
	private static final String		player				= "teilnehmer[%I]=%PID";
	private static final String		checkpoints			= "checkers=on";
	private static final String		zzz					= "zzz=%ZZZ";
	private static final String		crashs				= "crashallowed=%MODE";
	private static final String		directionS			= "startdirection=%MODE";

	private static final String		kick				= "http://www.karopapier.de/kickplayer.php";
	private static final String		kickParam			= "GID=%GID&UID=%UID&sicher=1";

	private static final DateFormat	dateFormat			= new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
	
	private static final String 	SUCCESS_CREATE		= "Neues Spiel erstellt<br /> <a href=\"showmap.php?GID=";
	private static final String 	SUCCESS_LEAVE		= "Fertig, Du bist draussen...";

	private String					server;
	private String					newGameURLString;

	private KaroThreadQueue			urlLoadQ;

	private Karopapier				karopapier;

	private SummaryScreen			summaryScreen;

	public static int				MAX_LOAD_THREADS	= 10;

	public GameCreator(Karopapier karopapier, SummaryScreen summaryScreen)
	{
		this.server = KaropapierLoader.server;
		this.newGameURLString = KaropapierLoader.newGameURLString;
		this.urlLoadQ = null;
		this.karopapier = karopapier;
		this.summaryScreen = summaryScreen;
	}

	public void createGames(List<Game> games)
	{
		RequestLogger logger = null;
		try
		{
			logger = new RequestLogger(dateFormat.format(new Date()) + " create.log");
		}
		catch(FileNotFoundException e)
		{
			System.out.println("could not create RequestLogger:");
			e.printStackTrace();
		}
		
		this.urlLoadQ = new KaroThreadQueue(MAX_LOAD_THREADS, summaryScreen, true, false, false, logger);

		for(Game game : games)
		{
			launchGame(game);
			for(Player player : game.getPlayers())
			{
				player.setGamesAct(player.getGamesAct() + 1);
			}
		}
		this.urlLoadQ.begin();
	}

	public void leaveGames(List<Game> games, Player player)
	{
		RequestLogger logger = null;
		try
		{
			logger = new RequestLogger(dateFormat.format(new Date()) + " leave.log");
		}
		catch(FileNotFoundException e)
		{
			System.out.println("could not create RequestLogger:");
			e.printStackTrace();
		}
		
		this.urlLoadQ = new KaroThreadQueue(MAX_LOAD_THREADS, summaryScreen, false, false, false, logger);

		try
		{
			KaropapierLoader.findIds(games);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return;
		}

		for(Game game : games)
		{
			if(game.isCreated())
			{
				if(game.getId() > 0)
					kickPlayer(game, player);
				else
					System.out.println("Warning: Could not leave game because of unknown ID: " + game.getName() + " (" + game.getId() + ")");
			}
		}
		this.urlLoadQ.begin();
	}

	public void launchGame(Game game)
	{
		String urlS;
		URL url;
		try
		{
			urlS = createNewGameURL(game);
			url = new URL(urlS.substring(0, urlS.indexOf("?")));
		}
		catch(MalformedURLException e)
		{
			e.printStackTrace();
			return;
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return;
		}
		String parameter = urlS.substring(urlS.indexOf("?") + 1);
		GameCreatorThread th = new GameCreatorThread(game, url, parameter, SUCCESS_CREATE, this.karopapier.isInDebugMode());
		this.urlLoadQ.addThread(th);
	}

	public void waitForFinished() throws InterruptedException
	{
		this.urlLoadQ.waitForFinished();
	}

	public String createNewGameURL(Game game) throws UnsupportedEncodingException
	{
		StringBuilder gameUrl = new StringBuilder();

		gameUrl.append(this.server);
		gameUrl.append((this.newGameURLString.charAt(0) == '/' ? "" : "/") + this.newGameURLString);
		gameUrl.append("?");
		gameUrl.append(name.replace("%NAME", URLEncoder.encode(game.getName(), "UTF-8")));
//		gameUrl.append(name.replace("%NAME", makeNameURLReady(game.getName())));
		gameUrl.append("&");
		gameUrl.append(map.replace("%MAPID", "" + game.getMap().getId()));
		gameUrl.append("&");
		int i = 0;
		for(Player p : game.getPlayers())
		{
			gameUrl.append(player.replace("%I", "" + i++).replace("%PID", "" + p.getId()));
			gameUrl.append("&");
		}
		if(game.getRules().getCheckpointsActivated())
		{
			gameUrl.append(checkpoints);
			gameUrl.append("&");
		}
		gameUrl.append(zzz.replace("%ZZZ", "" + game.getRules().getZzz()));
		gameUrl.append("&");
		gameUrl.append(crashs.replace("%MODE", game.getRules().getCrashingAllowed() ? "1" : "2"));
		gameUrl.append("&");
		gameUrl.append(directionS.replace("%MODE", "" + game.getRules().getDirection().getValue()));

		return gameUrl.toString();
	}

//	private String makeNameURLReady(String name)
//	{
//		StringBuilder sb = new StringBuilder();
//		for(int i = 0; i < name.length(); i++)
//		{
//			if(nonCriticalChars.indexOf(name.charAt(i)) == -1)
//				sb.append(charToASCII(name.charAt(i)));
//			else
//				sb.append(name.charAt(i));
//		}
//		return sb.toString();
//	}
//
//	private String charToASCII(char c)
//	{
//		return "%" + Integer.toHexString((int) c);
//	}

	public void kickPlayer(Game game, Player player)
	{
		try
		{
			URL url = new URL(kick);
			String parameter = kickParam;
			parameter = parameter.replace("%GID", "" + game.getId());
			parameter = parameter.replace("%UID", "" + player.getId());

			GameCreatorThread th = new GameCreatorThread(game, url, parameter, SUCCESS_LEAVE, this.karopapier.isInDebugMode());
			this.urlLoadQ.addThread(th);
		}
		catch(MalformedURLException e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws UnsupportedEncodingException
	{
		GameCreator gc = new GameCreator(null, null);
		Rules r = new Rules(1, 1, true, true, Direction.egal, true, true);
		r.createRandomValues();
		Map m = new Map(111, "x", "x", true, 1, null);
		List<Player> ps = Arrays.asList(new Player[] { new Player(222, "x", true, true, 999, 99, 999, 999, null) });
		Game g = new Game("test & nix", m, ps, r);
		System.out.println(gc.createNewGameURL(g));
	}
}
