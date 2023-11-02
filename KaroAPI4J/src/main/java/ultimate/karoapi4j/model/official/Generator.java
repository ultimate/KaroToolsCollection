package ultimate.karoapi4j.model.official;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ultimate.karoapi4j.KaroAPI;
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
public class Generator implements PlaceToRace
{
	/**
	 * Logger-Instance
	 */
	protected transient final Logger	logger	= LogManager.getLogger(getClass());

    private String key;    
	@JsonIgnore // not needed when serializing
    private String name;
	@JsonIgnore // not needed when serializing
    private String description;
	/**
	 * generator specific settings
	 */
    private java.util.Map<String, Object> settings;

    public Generator()
    {
        super();
    }

    public Generator(String key, java.util.Map<String, Object> settings)
    {
        this();
        this.key = key;
        this.settings = settings;
    }

    public Generator(String key, String name, String description, java.util.Map<String, Object> settings)
    {
        this(key, settings);
        this.name = name;
        this.description = description;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

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

    public java.util.Map<String, Object> getSettings()
    {
        return settings;
    }

    public void setSettings(java.util.Map<String, Object> settings)
    {
        this.settings = settings;
    }    

    @JsonIgnore
    public boolean isNight()
    {
        String key = "night";
        if(this.settings != null && this.settings.containsKey(key))
        {
            Object nightValue = this.settings.get(key);
            if(nightValue instanceof String)
                return Boolean.parseBoolean((String) nightValue);
            else if(nightValue instanceof Integer)
                return ((int) nightValue) == 1;
        } 
        return false;
    }

    @JsonIgnore
    public int getPlayers()
    {
        String key = "players";
        if(this.settings != null && this.settings.containsKey(key))
        {
            Object nightValue = this.settings.get(key);
            if(nightValue instanceof Integer)
                return (int) nightValue;
            else if(nightValue instanceof String)
            {
                try
                {
                    return Integer.parseInt((String) nightValue);
                }
                catch(NumberFormatException e)
                {
                    logger.error(e);
                }
            }
        }
        return KaroAPI.getIntProperty(KaroAPI.GENERATOR_KEY + "." + this.getKey() + ".players.default");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        //result = prime * result + ((name == null) ? 0 : name.hashCode());
        //result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((settings == null) ? 0 : settings.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Generator other = (Generator) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        /*
        if (name == null) {
            if (other.name != null)
            return false;
        } else if (!name.equals(other.name))
        return false;
        if (description == null) {
            if (other.description != null)
            return false;
        } else if (!description.equals(other.description))
        return false;
        */
        if (settings == null) {
            if (other.settings != null)
                return false;
        } else if (!settings.equals(other.settings))
            return false;
        return true;
    }

	@Override
	public String toString()
	{
		return "Generator '" + key + "': settings=" + settings;
	}
}
