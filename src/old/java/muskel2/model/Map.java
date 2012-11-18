package muskel2.model;

import java.awt.Image;
import java.io.IOException;
import java.io.Serializable;

public class Map implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	private int					id;
	private String				name;
	private String				creator;
	private boolean				night;
	private int					maxPlayers;
	private Image				image;

	public Map()
	{

	}

	public Map(int id, String name, String creator, boolean night, int maxPlayers, Image image)
	{
		super();
		this.id = id;
		this.name = name;
		this.creator = creator;
		this.night = night;
		this.maxPlayers = maxPlayers;
		this.image = image;
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getCreator()
	{
		return creator;
	}

	public boolean isNight()
	{
		return night;
	}

	public int getMaxPlayers()
	{
		return maxPlayers;
	}

	public Image getImage()
	{
		return image;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setCreator(String creator)
	{
		this.creator = creator;
	}

	public void setNight(boolean night)
	{
		this.night = night;
	}

	public void setMaxPlayers(int maxPlayers)
	{
		this.maxPlayers = maxPlayers;
	}

	public void setImage(Image image)
	{
		this.image = image;
	}

	public String getLabel()
	{
		return this.name + " (#" + this.id + ")";
	}

	@Override
	public String toString()
	{
		return "Karte " + this.id + ": " + this.name + " (" + this.maxPlayers + " Spieler) von '" + creator + "' "
				+ (this.night ? "'Nacht'" : "'Tag'");
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		throw new IOException("Saving of Muskel-2-Model-Classes not supported any more!");
		// out.writeInt(this.id);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		this.id = in.readInt();
	}
}
