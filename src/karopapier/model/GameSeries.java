package karopapier.model;

import java.util.TreeMap;

import karopapier.gui.ProgressFrame;

public class GameSeries {
	
	private ProgressFrame progress;
	private Karopapier karopapier;
	private String name;
	private int noG;
	private String numberPattern;
	private int countStart;
	private int minDigits;
	private String randomPattern;
	private int randomChars;
	private int crashs;
	private int direction;
	private boolean checks;
	private int zzz;
	private Map map;
	private boolean randomMap;
	private String mapType;
	private String addPlayer;
	private int minPlayers;
	private int maxPlayers;
	private TreeMap<String, Player> katA;
	private TreeMap<String, Player> katB;
	private TreeMap<String, Player> katC;
	
	public GameSeries() {
		super();
	}

	public String getName() {
		return name;
	}

	public int getNoG() {
		return noG;
	}

	public String getNumberPattern() {
		return numberPattern;
	}

	public int getCountStart() {
		return countStart;
	}

	public int getMinDigits() {
		return minDigits;
	}

	public String getRandomPattern() {
		return randomPattern;
	}

	public int getRandomChars() {
		return randomChars;
	}

	public int getCrashs() {
		return crashs;
	}
	
	public int getDirection() {
		return direction;
	}

	public boolean isChecks() {
		return checks;
	}

	public int getZzz() {
		return zzz;
	}

	public Map getMap() {
		return map;
	}

	public boolean isRandomMap() {
		return randomMap;
	}

	public String getMapType() {
		return mapType;
	}

	public String getAddPlayer() {
		return addPlayer;
	}

	public int getMinPlayers() {
		return minPlayers;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public TreeMap<String, Player> getKatA() {
		return katA;
	}

	public TreeMap<String, Player> getKatB() {
		return katB;
	}

	public TreeMap<String, Player> getKatC() {
		return katC;
	}

	public Karopapier getKaropapier() {
		return karopapier;
	}		

	public ProgressFrame getProgressFrame() {
		return progress;
	}

	public void setProgressFrame(ProgressFrame progressFrame) {
		this.progress = progressFrame;
	}

	public void setKaropapier(Karopapier karopapier) {
		this.karopapier = karopapier;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNoG(int noG) {
		this.noG = noG;
	}

	public void setNumberPattern(String numberPattern) {
		this.numberPattern = numberPattern;
	}

	public void setCountStart(int countStart) {
		this.countStart = countStart;
	}

	public void setMinDigits(int minDigits) {
		this.minDigits = minDigits;
	}

	public void setRandomPattern(String randomPattern) {
		this.randomPattern = randomPattern;
	}

	public void setRandomChars(int randomChars) {
		this.randomChars = randomChars;
	}

	public void setCrashs(int crashs) {
		this.crashs = crashs;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public void setChecks(boolean checks) {
		this.checks = checks;
	}

	public void setZzz(int zzz) {
		this.zzz = zzz;
	}

	public void setMap(Map map) {
		this.map = map;
	}

	public void setRandomMap(boolean randomMap) {
		this.randomMap = randomMap;
	}

	public void setMapType(String mapType) {
		this.mapType = mapType;
	}

	public void setAddPlayer(String addPlayer) {
		this.addPlayer = addPlayer;
	}

	public void setMinPlayers(int minPlayers) {
		this.minPlayers = minPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public void setKatA(TreeMap<String, Player> katA) {
		this.katA = katA;
	}

	public void setKatB(TreeMap<String, Player> katB) {
		this.katB = katB;
	}

	public void setKatC(TreeMap<String, Player> katC) {
		this.katC = katC;
	}
}
