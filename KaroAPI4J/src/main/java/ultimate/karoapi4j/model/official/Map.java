package ultimate.karoapi4j.model.official;

import java.awt.Image;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.base.Identifiable;
import ultimate.karoapi4j.model.extended.PlaceToRace;
import ultimate.karoapi4j.utils.JSONUtil;

/**
 * POJO Map as defined by the {@link KaroAPI}
 *  
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
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public class Map extends Identifiable implements PlaceToRace
{
	//@formatter:off
	public static class FromIDConverter extends JSONUtil.FromIDConverter<Map> { public FromIDConverter() { super(Map.class); } };
	public static class FromIDArrayToListConverter extends JSONUtil.FromIDArrayToListConverter<Map> { public FromIDArrayToListConverter() { super(Map.class); } };
	public static class FromIDMapToListConverter extends JSONUtil.FromIDMapToListConverter<Map> { public FromIDMapToListConverter() { super(Map.class); } };
	//@formatter:on
	/**
	 * the row delimeter used in the map code
	 */
	public static final String	ROW_DELIMITER	= "\n";
	// Standard JSON Fields
	// private int id; // see super class
	private String				name;
	private String				author;
	private int					cols;
	private int					rows;
	private double				rating;
	private int					players;
	private int[]				cps;
	private boolean				active;
	private boolean				night;
	private int					record;
	private String				code;
	// additional Fields (internally used)
	private Image				image;
	private Image				thumb;

	public Map()
	{
		super();
	}

	public Map(Integer id)
	{
		super(id);
	}

	@Override
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

	@Override
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

	@Override
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

	// additional information

	public Image getImage()
	{
		return image;
	}

	public void setImage(Image image)
	{
		this.image = image;
	}

	public Image getThumb()
	{
		return thumb;
	}

	public void setThumb(Image thumb)
	{
		this.thumb = thumb;
	}

	public String getLabel()
	{
		return this.name + " (#" + this.getId() + ")";
	}

	@Override
	public String toString()
	{
		return "Karte " + this.getId() + ": " + this.name + " (" + this.players + " Spieler) von '" + author + "' " + (this.night ? "'Nacht'" : "'Tag'");
	}
}
