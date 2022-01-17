package ultimate.karopapier.muskelx.core.karoaccess;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import muskel2.model.Game;
import muskel2.model.Player;
import ultimate.karopapier.muskelx.model.Karopapier;

public class GameCreator
{
	private static final String	name				= "name=%NAME";
	private static final String	nonCriticalChars	= "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	private static final String	map					= "mapid=%MAPID";
	private static final String	player				= "teilnehmer[%I]=%PID";
	private static final String	checkpoints			= "checkers=on";
	private static final String	zzz					= "zzz=%ZZZ";
	private static final String	crashs				= "crashallowed=%MODE";
	private static final String	directionS			= "startdirection=%MODE";

	private static final String	kick				= "http://www.karopapier.de/kickplayer.php";
	private static final String	kickParam			= "GID=%GID&UID=%UID&sicher=1";

	private String				server;
	private String				newGameURLString;

	private KaroThreadQueue		urlLoadQ;

	private Karopapier			karopapier;

	private Notifyable		notifyable;

	public static final int		maxLoadThreads		= 50;

	public GameCreator(Karopapier karopapier, Notifyable notifyable)
	{
		this.server = KaropapierLoader.server;
		this.newGameURLString = KaropapierLoader.newGameURLString;
		this.urlLoadQ = null;
		this.karopapier = karopapier;
		this.notifyable = notifyable;
	}

	public void createGames(List<Game> games)
	{
		this.urlLoadQ = new KaroThreadQueue(maxLoadThreads, notifyable, true, false, false);

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
		this.urlLoadQ = new KaroThreadQueue(maxLoadThreads, notifyable, false, false, false);

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
				kickPlayer(game, player);
		}
		this.urlLoadQ.begin();
	}

	public void launchGame(Game game)
	{
		String urlS = createNewGameURL(game);
		URL url;
		try
		{
			url = new URL(urlS.substring(0, urlS.indexOf("?")));
		}
		catch(MalformedURLException e)
		{
			e.printStackTrace();
			return;
		}
		String parameter = urlS.substring(urlS.indexOf("?") + 1);
		GameCreatorThread th = new GameCreatorThread(game, url, parameter, "Neues Spiel erstellt", this.karopapier.isInDebugMode());
		this.urlLoadQ.addThread(th);
	}

	public String createNewGameURL(Game game)
	{
		StringBuilder gameUrl = new StringBuilder();

		gameUrl.append(this.server);
		gameUrl.append((this.newGameURLString.charAt(0) == '/' ? "" : "/") + this.newGameURLString);
		gameUrl.append("?");
		gameUrl.append(name.replace("%NAME", makeNameURLReady(game.getName())));
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

	public String makeNameURLReady(String name)
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < name.length(); i++)
		{
			if(nonCriticalChars.indexOf(name.charAt(i)) == -1)
				sb.append(charToASCII(name.charAt(i)));
			else
				sb.append(name.charAt(i));
		}
		return sb.toString();
	}

	public String charToASCII(char c)
	{
		return "%" + Integer.toHexString((int) c);
	}

	public void kickPlayer(Game game, Player player)
	{
		try
		{
			URL url = new URL(kick);
			String parameter = kickParam;
			parameter = parameter.replace("%GID", "" + game.getId());
			parameter = parameter.replace("%UID", "" + player.getId());

			GameCreatorThread th = new GameCreatorThread(game, url, parameter, "Fertig, Du bist draussen...", this.karopapier.isInDebugMode());
			this.urlLoadQ.addThread(th);
		}
		catch(MalformedURLException e)
		{
			e.printStackTrace();
		}
	}
}
