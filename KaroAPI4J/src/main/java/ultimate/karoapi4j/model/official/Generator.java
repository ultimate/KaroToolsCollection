package ultimate.karoapi4j.model.official;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.base.Identifiable;
import ultimate.karoapi4j.model.extended.PlaceToRace;

/**
 * POJO Generator as defined by the {@link KaroAPI}.<br/>
 * <br/>
 * Used for lists in the form:<br/>
 * <code>
 * 		[
 *			{
 *              "key": "bagger",
 *              "name": "Bodo BAGGER",
 *              "description": "Bodo Arbeitet Gründlich, Generiert Erfundene Rennstrecken",
 *              "settings": {
 *                  "laenge": 350,
 *                  "dimx": 80,
 *                  "dimy": 50,
 *                  "cpfreq": 10,
 *                  "matschepampe": 0,
 *                  "seed": ""
 *              }
 *          },
 *		    ...
 *		]
 * </code>
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public class Generator extends Identifiable implements PlaceToRace
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger					logger		= LogManager.getLogger(getClass());

	private String										key;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // not needed when serializing
	private String										name;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // not needed when serializing
	private String										description;

	/**
	 * generator specific settings
	 */
	private java.util.Map<String, Object>				settings;

	@JsonIgnore
	private int											uniqueId	= 0;
	@JsonIgnore
	private Generator									source		= null;

	/**
	 * Counters to uniquely identify generator configuration with the same key
	 */
	private static final HashMap<String, AtomicInteger>	ID_COUNTERS	= new HashMap<>();
	/**
	 * Original generators aka "sources" for modified generators by key
	 */
	private static final HashMap<String, Generator>		SOURCES		= new HashMap<>();

	@JsonCreator
	public Generator(@JsonProperty("key") String key)
	{
		super();
		this.key = key;
		synchronized(ID_COUNTERS)
		{
			if(!ID_COUNTERS.containsKey(key))
			{
				ID_COUNTERS.put(key, new AtomicInteger(0));
				this.uniqueId = 0;
				SOURCES.put(key, this);
			}
			else
			{
				this.uniqueId = ID_COUNTERS.get(key).incrementAndGet();
				this.source = SOURCES.get(key);
			}
		}
	}

	public Generator(String key, java.util.Map<String, Object> settings)
	{
		this(key);
		this.settings = settings;
	}

	public Generator(String key, String name, String description, java.util.Map<String, Object> settings)
	{
		this(key, settings);
		this.name = name;
		this.description = description;
	}

	@JsonIgnore
	@Override
	public Integer getId()
	{
		return getUniqueKey().hashCode();
	}

	@JsonIgnore
	@Override
	public void setId(Integer id)
	{
		throw new UnsupportedOperationException("setting id for generator is not allowed");
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
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

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@JsonIgnore
	public String getAuthor()
	{
		String authorKey = KaroAPI.GENERATOR_KEY + "." + this.getKey() + ".author";
		return KaroAPI.getStringProperty(authorKey, "unbekannt");
	}

	public java.util.Map<String, Object> getSettings()
	{
		return settings;
	}

	public void setSettings(java.util.Map<String, Object> settings)
	{
		this.settings = settings;
	}

	public int getUniqueId()
	{
		return uniqueId;
	}

	@JsonIgnore
	public String getUniqueName()
	{
		return name + " (" + (uniqueId == 0 ? "default" : uniqueId) + ")";
	}

	@JsonIgnore
	public String getUniqueKey()
	{
		return key + " (" + (uniqueId == 0 ? "default" : uniqueId) + ")";
	}

	@JsonIgnore
	public boolean isEditable()
	{
		return uniqueId > 0;
	}

	public Generator getSource()
	{
		return source;
	}

	public Generator copy()
	{
		Generator g2 = new Generator(this.key);
		g2.name = new String(this.name);
		g2.description = new String(this.description);
		g2.settings = new HashMap<>(this.settings);
		g2.source = (this.source == null ? this : this.source);
		return g2;
	}

	@JsonIgnore
	@Override
	public boolean isNight()
	{
		String key = "night";
		if(this.settings != null && this.settings.containsKey(key))
		{
			Object value = this.settings.get(key);
			if(value instanceof Boolean)
				return (boolean) value;
			else if(value instanceof String)
				return Boolean.parseBoolean((String) value);
			else if(value instanceof Integer)
				return ((int) value) == 1;
		}
		return false;
	}

	@JsonIgnore
	public int getPlayersMin()
	{
		String playersKey = KaroAPI.GENERATOR_KEY + "." + this.getKey() + ".players.min";
		return KaroAPI.getIntProperty(playersKey, 0);
	}

	@JsonIgnore
	public int getPlayersMax()
	{
		String playersKey = KaroAPI.GENERATOR_KEY + "." + this.getKey() + ".players.max";
		return KaroAPI.getIntProperty(playersKey, 99);
	}

	@JsonIgnore
	@Override
	public int getPlayers()
	{
		String key = "players";
		if(this.settings != null && this.settings.containsKey(key))
		{
			Object value = this.settings.get(key);
			if(value instanceof Integer)
				return (int) value;
			else if(value instanceof String)
			{
				try
				{
					return Integer.parseInt((String) value);
				}
				catch(NumberFormatException e)
				{
					logger.error(e);
				}
			}
		}
		return getPlayersMax();
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(key, settings, uniqueId);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		Generator other = (Generator) obj;
		return Objects.equals(key, other.key) && Objects.equals(settings, other.settings) && uniqueId == other.uniqueId;
	}

	@Override
	public String toString()
	{
		return toSettingsString(true);
		// return "Generator '" + key + "' (" + getPlayers() + " Spieler) mit settings=" + settings;
	}

	public String toSettingsString(boolean deviationsOnly)
	{
		HashMap<String, Object> deviation = new HashMap<>();
		if(source == null)
		{
			if(!deviationsOnly)
				deviation.putAll(this.settings);
		}
		else
		{
			deviation.putAll(this.settings);
			if(deviationsOnly)
			{
				for(String key : source.settings.keySet())
				{
					if(deviation.containsKey(key) && deviation.get(key) != null && deviation.get(key).equals(source.settings.get(key)))
						deviation.remove(key);
				}
			}
		}
		return getUniqueName() + (deviation.size() > 0 ? " " + deviation : "");
	}
}
