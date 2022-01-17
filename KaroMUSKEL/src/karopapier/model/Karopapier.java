package karopapier.model;

import java.util.TreeMap;

public class Karopapier {

	private TreeMap<Integer, Map> maps;
	private TreeMap<String, Player> players;
	private String currentUser;
	private Player currentPlayer;
	
	public Karopapier(TreeMap<Integer, Map> maps,
			TreeMap<String, Player> players, String currentUser) {
		super();
		this.maps = maps;
		this.players = players;
		this.currentUser = currentUser;
	}

	public Karopapier() {
		super();
		this.maps = new TreeMap<Integer, Map>();
		this.players = new TreeMap<String, Player>();
		this.currentUser = "";
	}

	public TreeMap<Integer, Map> getMaps() {
		return maps;
	}

	public TreeMap<String, Player> getPlayers() {
		return players;
	}
	
	public String getCurrentUser() {
		return currentUser;
	}

	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	public void setMaps(TreeMap<Integer, Map> maps) {
		this.maps = maps;
	}

	public void setPlayers(TreeMap<String, Player> players) {
		this.players = players;
	}

	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
		this.currentPlayer = this.players.remove(currentUser.toLowerCase());
	}
}
