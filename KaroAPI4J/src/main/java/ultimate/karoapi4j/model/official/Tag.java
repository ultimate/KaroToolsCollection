package ultimate.karoapi4j.model.official;

import ultimate.karoapi4j.KaroAPI;

/**
 * POJO Game as defined by the {@link KaroAPI}.<br/>
 * <br/>
 * Used for lists in the form:<br/>
 * <code>
 * 		[
 *          {
 *              "label": "!KaroIQ!"
 *          },
 *          {
 *              "label": "§RE§"
 *          },
 *          {
 *              "label": "CCC"
 *          },
 *          {
 *              "label": "KaroLiga"
 *          },
 *          {
 *              "label": "KLC"
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
}
