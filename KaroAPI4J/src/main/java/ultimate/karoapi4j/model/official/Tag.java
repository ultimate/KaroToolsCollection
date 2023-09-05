package ultimate.karoapi4j.model.official;

import ultimate.karoapi4j.KaroAPI;

/**
 * POJO Game as defined by the {@link KaroAPI}.<br/>
 * <br/>
 * Used for lists in the form:<br/>
 * <code>
 * 		[
 *          {
 *              "label": "!KaroIQ!",
 *              "description": "Spiele für https://wiki.karopapier.de/KaroIQ"
 *          },
 *          {
 *              "label": "§RE§",
 *              "description": "Spiele mit der Spezialregel https://wiki.karopapier.de/Einladeraum#Rundenerster_wiederholt_letzten_Zug"
 *          },
 *          {
 *              "label": "CCC",
 *              "description": "Spiele aus der https://wiki.karopapier.de/CraZZZy_Crash_Challenge"
 *          },
 *          {
 *              "label": "KaroLiga",
 *              "description": "Spiele aus der https://wiki.karopapier.de/KaroLiga"
 *          },
 *          {
 *              "label": "KLC",
 *              "description": "Spiele aus dem https://wiki.karopapier.de/KaroLigaCup"
 *          }
 *      ]
 * </code>
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public class Tag
{
    private String label;
    private String description;

    public Tag()
    {
        super();
    }

    public Tag(String label)
    {
        super();
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }    
}
