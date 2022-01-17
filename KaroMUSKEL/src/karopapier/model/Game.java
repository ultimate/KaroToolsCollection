package karopapier.model;

import java.util.ArrayList;

public class Game {
	public static final int CRASH_DONT_CARE = 0;
	public static final int CRASH_ALLOWED = 1;
	public static final int CRASH_NOT_ALLOWED = 2;

	private String name;
	private ArrayList<Player> players;
	private int zzz;
	private int crashs;
	private boolean checkpoints;
	private int direction;
	private Map map;
	
	public Game() {
		this.name = null;
		this.players = null;
		this.zzz = -1;
		this.crashs = -1;
		this.checkpoints = false;
		this.map = null;
	}

	public Game(String name, int zzz, int crashs, boolean checkpoints, Map map) {
		super();
		this.name = name;
		this.players = new ArrayList<Player>();
		this.zzz = zzz;
		this.crashs = crashs;
		this.checkpoints = checkpoints;
	}

	public Game(String name, ArrayList<Player> players, int zzz, int crashs,
			boolean checkpoints, Map map) {
		super();
		this.name = name;
		this.players = players;
		this.zzz = zzz;
		this.crashs = crashs;
		this.checkpoints = checkpoints;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}

	public void setPlayers(ArrayList<Player> players) {
		this.players = players;
	}

	public int getZzz() {
		return zzz;
	}

	public void setZzz(int zzz) {
		this.zzz = zzz;
	}

	public int getCrashs() {
		return crashs;
	}

	public void setCrashs(int crashs) {
		this.crashs = crashs;
	}
	
	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public boolean hasCheckpoints() {
		return checkpoints;
	}

	public void setCheckpoints(boolean checkpoints) {
		this.checkpoints = checkpoints;
	}
	
	public Map getMap() {
		return map;
	}

	public void setMap(Map map) {
		this.map = map;
	}

	public boolean isComplete() {
		return 	(this.name != null) && 
				(this.players != null) &&
				(this.players.size() != 0) &&
				(this.crashs >= 0) &&
				(this.crashs <= 2) &&
				(this.zzz >= 0) &&
				(this.map != null);
	}
}
