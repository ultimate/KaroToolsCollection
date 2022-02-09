package muskel2.model;

import java.awt.Image;
import java.io.IOException;
import java.io.Serializable;
@Deprecated
public class Map implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	public int					id;
	public String				name;
	public String				creator;
	public boolean				night;
	public int					maxPlayers;
	public Image				image;

	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.writeInt(this.id);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		this.id = in.readInt();
	}
}
