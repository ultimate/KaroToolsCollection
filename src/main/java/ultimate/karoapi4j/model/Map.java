package ultimate.karoapi4j.model;

import java.awt.Image;

public class Map
{
	/*
	 * map/list.json
	 * "id" : 1,
	 * "name" : "Die Erste",
	 * "author" : "Didi",
	 * "cols" : 60,
	 * "rows" : 25,
	 * "mapcode" :
	 * "PXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\r\nPXXXXOOOOOOOFSOOOOOOOOOOOOOOOOOOOOOO1OOOOOOOOOOOOOOOXXXXXXXX\r\nPXXOOOOOOOOOFSOOOOOOOOOOOOOOOOOOOOO1O1OOOOOOOOOOOOOOOOOXXXXX\r\nPXOOOOOOOOOOFSOOOOOOOOOOOOOOOOOOOO1O1O1OOOOOOOOOOOOOOOOOOXXX\r\nPXOOOOOOOOOOFSOOOOOOOOOOOOOOOOOOO1O1O1O1OOOOOOOOOOOOOOOOOOOX\r\nXOOOOOOOOOOOFSOOOOOOOOOOOOOOOOOO1O1O1O1O1OOOOOOOOOOOOOOOOOOX\r\nXXOOOOOOOOXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOOX\r\nXOOOOOOOXXXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOOXXXXXXXOOOOOOOOX\r\nXOOOOOOXXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOOOOOXXXXXXOOOOOOOXX\r\nX77777XXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOOOOOOOOXXXOOOOOOOOXX\r\nXOOOOOXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOXXOOOOOOO33XXX2O2O2O2XX\r\nXOOOOOOXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOXXXXXOO3333OXX2222222XX\r\nXOOOOOOOOXXXXXXXXXXXXXXXXXXXXOOOOOOOXXXXXXXX333OOOOOOOOOOXXX\r\nXOOOOOOOOXXXXXXXXXXXXXXXXXXXXX444444XXXXXXXXXOOOOOOOOOOOOXXX\r\nXXOOOOOOOOOXXXXXXXXXXXXXXXXXXXXOOOOOOXXXXXXXXOOOOOOOOOOOXXXX\r\nXXXOOOOOOO666XXXXXXXXXXXXXXXXXXXOOOOOOOXXXXXXXXOOOOOOOOXXXXX\r\nXXXOOOOOO66OOOOOOOOOOXXXXXXXXXXXXOOOOOOOXXXXXXXXXXXXXXXXXXXX\r\nXXXOOOOOO6OOOOOOOOOOOOOOXXXXXXXXOOOOOOOOXXXXXXXXXXXXXXXXXXXX\r\nXXXXXXXXX6OOOOOOOOOOOOOOOOXXXOOOOOOOOOOOXXXXXXXXXXXXXXXXXXXX\r\nXXXXXXXXXXXXXXXXOOOOOOOOOO555OOOOOOOOOOXXXXXXXXXXXXXXXXXXXXX\r\nXXXXXXXXXXXXXXXXXXXOOOOOOO555OOOOOOOOXXXXXXXXXXXXXXXXXXXXXXX\r\nXXXXXXXXXXXXXXXXXXXXXOOOOO555OOOOOOXXXXXXXXXXXXXXXXXXXXXXXXX\r\nXXXXXXXXXXXXXXXXXXXXXXXXXO555OOOXXXXXXXXXXXXXXXXXXXXXXXXXXXX\r\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\r\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
	 * ,
	 * "cps" : [ "1", "2", "3", "4", "5", "6", "7" ]
	 */
	// Standard JSON Fields
	private int			id;
	private String		name;
	private String		author;
	private int			cols;
	private int			rows;
	private String		mapcode;
	private String[]	cps;
	// additional Fields
	private Image		image;
	private Image		preview;

	public Map()
	{
		super();
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getAuthor()
	{
		return author;
	}

	public int getCols()
	{
		return cols;
	}

	public int getRows()
	{
		return rows;
	}

	public String getMapcode()
	{
		return mapcode;
	}

	public String[] getCps()
	{
		return cps;
	}

	public Image getImage()
	{
		return image;
	}

	public Image getPreview()
	{
		return preview;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

	public void setCols(int cols)
	{
		this.cols = cols;
	}

	public void setRows(int rows)
	{
		this.rows = rows;
	}

	public void setMapcode(String mapcode)
	{
		this.mapcode = mapcode;
	}

	public void setCps(String[] cps)
	{
		this.cps = cps;
	}

	public void setImage(Image image)
	{
		this.image = image;
	}

	public void setPreview(Image preview)
	{
		this.preview = preview;
	}
	
	// TODO hasCP
	// TODO CPs as int[]
}
