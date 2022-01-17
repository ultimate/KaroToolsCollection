package karopapier.model;

import java.awt.Image;
import java.awt.Point;
import java.io.Serializable;

public class Map implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String name;
	private boolean night;
	private int maxPlayers;
	private transient Image image;
	private Point bestFirstMove;
	
	public Map() {
		
	}

	public Map(int id, String name, boolean night, int maxPlayers, Image image) {
		super();
		this.id = id;
		this.name = name;
		this.night = night;
		this.maxPlayers = maxPlayers;
		this.image = image;
		this.bestFirstMove = null;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isNight() {
		return night;
	}

	public void setNight(boolean night) {
		this.night = night;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}
	
	public Point getBestFirstMove() {
		return bestFirstMove;
	}

	public void setBestFirstMove(Point bestFirstMove) {
		this.bestFirstMove = bestFirstMove;
	}

	public String toString() {
		return "Karte " + this.id + ": " + this.name + " (" + this.maxPlayers + " Spieler) " + (this.night ? "'Nacht'" : "'Tag'");
	}
}
