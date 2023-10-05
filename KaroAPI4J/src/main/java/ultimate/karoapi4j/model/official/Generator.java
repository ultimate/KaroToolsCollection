package ultimate.karoapi4j.model.official;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ultimate.karoapi4j.KaroAPI;

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
public class Generator
{
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
}
