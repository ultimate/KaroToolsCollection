package ultimate.karopapier.muskelx.model;

import java.util.TreeMap;

import muskel2.model.Map;
import muskel2.model.Player;

public class Karopapier
{
	private TreeMap<Integer, Map>	maps;
	private TreeMap<String, Player>	players;
	private Player					currentPlayer;
	private boolean 				inDebugMode;

	public Karopapier()
	{
		this(new TreeMap<Integer, Map>(), new TreeMap<String, Player>(), null);
	}

	public Karopapier(TreeMap<Integer, Map> maps, TreeMap<String, Player> players, String currentUser)
	{
		super();
		this.maps = maps;
		this.players = players;
		this.currentPlayer = this.players.remove(currentUser.toLowerCase());
	}

	public TreeMap<Integer, Map> getMaps()
	{
		return maps;
	}

	public TreeMap<String, Player> getPlayers()
	{
		return players;
	}

	public Player getCurrentPlayer()
	{
		return currentPlayer;
	}
	
	public boolean isInDebugMode()
	{
		return inDebugMode;
	}

	public void setInDebugMode(boolean inDebugMode)
	{
		this.inDebugMode = inDebugMode;
	}

	public void setCurrentPlayer(Player currentPlayer)
	{
		this.currentPlayer = currentPlayer;
	}
}
