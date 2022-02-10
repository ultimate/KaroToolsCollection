package muskel2.model;

import java.awt.Image;
import java.io.IOException;
import java.io.Serializable;

import muskel2.model.help.Identifiable;

@Deprecated
public class Map implements Serializable, Identifiable
{
	private static final long	serialVersionUID	= 1L;

	public int					id;
	public String				name;
	public String				creator;
	public boolean				night;
	public int					maxPlayers;
	public Image				image;

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
