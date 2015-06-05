package muskel2.model;

import java.awt.Color;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.Map.Entry;

import muskel2.Main;

public class Player implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	private int					id;
	private String				name;
	private boolean				invitableNormal;
	private boolean				invitableNight;
	private int					gamesMax;
	private int					gamesAct;
	private int					gamesActOrPlanned;
	private int					lastVisited;
	private int					activeSince;
	private Color				color;

	public Player()
	{
		this.id = -1;
		this.gamesMax = -1;
		this.gamesAct = -1;
		this.gamesActOrPlanned = -1;
		this.lastVisited = -1;
		this.activeSince = -1;
	}

	public Player(int id, String name, boolean invitableNormal, boolean invitableNight, int gamesMax, int gamesAct, int lastVisited, int activeSince,
			Color color)
	{
		super();
		this.id = id;
		this.name = name;
		this.invitableNormal = invitableNormal;
		this.invitableNight = invitableNight;
		this.gamesMax = gamesMax;
		this.gamesAct = gamesAct;
		this.gamesActOrPlanned = gamesAct;
		this.lastVisited = lastVisited;
		this.activeSince = activeSince;
		this.color = color;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}
	
	public String getNameLowerCase()
	{
		return name.toLowerCase();
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isInvitableNormal()
	{
		return invitableNormal;
	}

	public void setInvitableNormal(boolean invitableNormal)
	{
		this.invitableNormal = invitableNormal;
	}

	public boolean isInvitableNight()
	{
		return invitableNight;
	}

	public void setInvitableNight(boolean invitableNight)
	{
		this.invitableNight = invitableNight;
	}

	public int getGamesMax()
	{
		return gamesMax;
	}

	public void setGamesMax(int gamesMax)
	{
		this.gamesMax = gamesMax;
	}

	public int getGamesAct()
	{
		return gamesAct;
	}

	public void setGamesAct(int gamesAct)
	{
		this.gamesAct = gamesAct;
	}

	public int getGamesActOrPlanned()
	{
		return gamesActOrPlanned;
	}

	public void setGamesActOrPlanned(int gamesActOrPlanned)
	{
		this.gamesActOrPlanned = gamesActOrPlanned;
	}

	public int getLastVisited()
	{
		return lastVisited;
	}

	public void setLastVisited(int lastVisited)
	{
		this.lastVisited = lastVisited;
	}

	public int getActiveSince()
	{
		return activeSince;
	}

	public void setActiveSince(int activeSince)
	{
		this.activeSince = activeSince;
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public String toString()
	{
		return this.id + " - " + this.name + (this.invitableNight ? "" : " (keine Nachtrennen)") + " [ " + this.activeSince + " | "
				+ this.lastVisited + " @ " + this.gamesActOrPlanned + " / " + this.gamesMax + " ]";
	}

	public boolean isInvitable(boolean night)
	{
		boolean b = true;
		if(this.gamesMax == 0)
			b = true;
		else if(this.gamesAct < 0)
			b = false;
		else
			b = Math.max(this.gamesAct, this.gamesActOrPlanned) < this.gamesMax;
		if(night)
			return this.invitableNight && b;
		else
			return this.invitableNormal && b;
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.writeInt(this.id);
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		this.id = in.readInt();
		Player original = null;
		String key = null;
		if(Main.getKaropapier().getCurrentPlayer().getId() == this.id)
		{
			original = Main.getKaropapier().getCurrentPlayer();
			Main.getKaropapier().setCurrentPlayer(this);
		}
		else
		{
			for(Entry<String, Player> player: Main.getKaropapier().getPlayers().entrySet())
			{
				if(player.getValue().getId() == this.id)
				{
					key = player.getKey();
					original = player.getValue();
					break;
				}
			}
			if(original == null)
				throw new NotSerializableException("could not deserialize player with id: " + this.id);
			Main.getKaropapier().getPlayers().put(key, this);
		}
		this.name = original.name;
		this.invitableNormal = original.invitableNormal;
		this.invitableNight = original.invitableNight;
		this.gamesMax = original.gamesMax;
		this.gamesAct = original.gamesAct;
		this.gamesActOrPlanned = original.gamesActOrPlanned;
		this.lastVisited = original.lastVisited;
		this.activeSince = original.activeSince;
		this.color = original.color;		
	}
}
