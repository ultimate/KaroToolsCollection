package muskel2.model;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;

import muskel2.model.help.Identifiable;

@Deprecated
public class Player implements Serializable, Identifiable
{
	public static final long	serialVersionUID	= 1L;

	public int					id;
	public String				name;
	public boolean				invitableNormal;
	public boolean				invitableNight;
	public int					gamesMax;
	public int					gamesAct;
	public int					gamesActOrPlanned;
	public int					lastVisited;
	public int					activeSince;
	public Color				color;

	public int					league;
	public Map					homeMap;

	@Override
	public int getId()
	{
		return id;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.writeInt(this.id);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		this.id = in.readInt();
	}
}
