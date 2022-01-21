package ultimate.karoapi4j.model.official;

import java.awt.Image;

public class Map
{
	public static final String ROW_DELIMITER = "\n";
	/*
	 * https://www.karopapier.de/api/maps/1?mapcode=true
	 * "id": 1,
	 * "name": "Die Erste",
	 * "author": "Didi",
	 * "cols": 60,
	 * "rows": 25,
	 * "rating": 3.9487,
	 * "players": 5,
	 * "cps": ["1","2","3","4","5","6","7"],
	 * "active": true,
	 * "night": 0,
	 * "record": 3,
	 * "code": "PXXX...XXXXXXX"
	 */
	// Standard JSON Fields
	private int		id;
	private String	name;
	private String	author;
	private int		cols;
	private int		rows;
	private double	rating;
	private int		players;
	private int[]	cps;
	private boolean	active;
	private boolean	night;
	private int		record;
	private String	code;
	// additional Fields
	private Image	image;
	private Image	preview;

	public Map()
	{
		super();
	}

	public Map(int id)
	{
		this();
		this.id = id;
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

	public void setName(String name)
	{
		this.name = name;
	}

	public String getAuthor()
	{
		return author;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

	public int getCols()
	{
		return cols;
	}

	public void setCols(int cols)
	{
		this.cols = cols;
	}

	public int getRows()
	{
		return rows;
	}

	public void setRows(int rows)
	{
		this.rows = rows;
	}

	public double getRating()
	{
		return rating;
	}

	public void setRating(double rating)
	{
		this.rating = rating;
	}

	public int getPlayers()
	{
		return players;
	}

	public void setPlayers(int players)
	{
		this.players = players;
	}

	public int[] getCps()
	{
		return cps;
	}

	public void setCps(int[] cps)
	{
		this.cps = cps;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public boolean isNight()
	{
		return night;
	}

	public void setNight(boolean night)
	{
		this.night = night;
	}

	public int getRecord()
	{
		return record;
	}

	public void setRecord(int record)
	{
		this.record = record;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public Image getImage()
	{
		return image;
	}

	public void setImage(Image image)
	{
		this.image = image;
	}

	public Image getPreview()
	{
		return preview;
	}

	public void setPreview(Image preview)
	{
		this.preview = preview;
	}

	public String getLabel()
	{
		return this.name + " (#" + this.id + ")";
	}

	@Override
	public String toString()
	{
		return "Karte " + this.id + ": " + this.name + " (" + this.players + " Spieler) von '" + author + "' " + (this.night ? "'Nacht'" : "'Tag'");
	}

	// TODO hasCP
	// TODO CPs as int[]
}
