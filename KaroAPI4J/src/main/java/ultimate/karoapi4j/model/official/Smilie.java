package ultimate.karoapi4j.model.official;

import ultimate.karoapi4j.KaroAPI;

/**
 * POJO Game as defined by the {@link KaroAPI}.<br/>
 * <br/>
 * Used for lists in the form:<br/>
 * <code>
 * 		[
 *			{
 *              "id": "cool",
 *              "url": "https://www.karopapier.de/bilder/smilies/cool.gif"
 *          },
 *		    ...
 *		]
 * </code>
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public class Smilie
{
    private String id;
    private String url;

    public Smilie()
    {
        super();
    }

    public Smilie(String id, String url)
    {
        super();
        this.id = id;
        this.url = url;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}    
}
