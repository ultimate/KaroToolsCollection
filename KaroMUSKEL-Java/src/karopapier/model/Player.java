package karopapier.model;

import java.awt.Color;
import java.io.Serializable;

public class Player implements Serializable{	
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String name;
	private boolean invitableNormal;
	private boolean invitableNight;
	private int gamesMax;
	private int gamesAct;
	private int lastVisited;
	private int activeSince;
	private Color color;
	
	public Player() {
		this.id = -1;
		this.gamesMax = -1;
		this.gamesAct = -1;
		this.lastVisited = -1;
		this.activeSince = -1;
	}

	public Player(int id, String name, boolean invitableNormal,
			boolean invitableNight, int gamesMax, int gamesAct,
			int lastVisited, int activeSince, Color color) {
		super();
		this.id = id;
		this.name = name;
		this.invitableNormal = invitableNormal;
		this.invitableNight = invitableNight;
		this.gamesMax = gamesMax;
		this.gamesAct = gamesAct;
		this.lastVisited = lastVisited;
		this.activeSince = activeSince;
		this.color = color;
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

	public boolean isInvitableNormal() {
		return invitableNormal;
	}

	public void setInvitableNormal(boolean invitableNormal) {
		this.invitableNormal = invitableNormal;
	}

	public boolean isInvitableNight() {
		return invitableNight;
	}

	public void setInvitableNight(boolean invitableNight) {
		this.invitableNight = invitableNight;
	}

	public int getGamesMax() {
		return gamesMax;
	}

	public void setGamesMax(int gamesMax) {
		this.gamesMax = gamesMax;
	}

	public int getGamesAct() {
		return gamesAct;
	}

	public void setGamesAct(int gamesAct) {
		this.gamesAct = gamesAct;
	}

	public int getLastVisited() {
		return lastVisited;
	}

	public void setLastVisited(int lastVisited) {
		this.lastVisited = lastVisited;
	}

	public int getActiveSince() {
		return activeSince;
	}

	public void setActiveSince(int activeSince) {
		this.activeSince = activeSince;
	}
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public String toString() {
		return this.id + " - " + this.name + (this.invitableNight ? "" : " (keine Nachtrennen)" ) + " [ " + this.activeSince + " | " + this.lastVisited + " @ " + this.gamesAct + " / " + this.gamesMax + " ]";
	}
	
	public boolean isInvitable(boolean night) {
		boolean b = true;
		if(this.gamesMax == 0)
			b = true;
		else if(this.gamesAct < 0)
			b = false;
		else
			b = this.gamesAct < this.gamesMax;
		if(night)
			return this.invitableNight && b;
		else
			return this.invitableNormal && b;
	}
}