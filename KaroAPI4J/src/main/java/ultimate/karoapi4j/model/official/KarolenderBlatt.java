package ultimate.karoapi4j.model.official;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.utils.JSONUtil;

/**
 * POJO KarolenderBlatt as defined by the {@link KaroAPI}.<br/>
 * <br/>
 * Used for lists in the form:<br/>
 * <code>
 * 		[
 *			{
 *              "posted": "2014-09-04",
 *              "line": "Heute vor 11 Jahren begegnet quabla zum ersten mal der Bordfunkfliege. Zitat &quot;Jetzt dacht ich grad Didi haette ne neue Spielerei, weil neben dem kleinen Strichmaennchen da im Feld &quot;Karopapier-Bordfunk für alle!!!!&quot; eine genauso grosse Fliege rumflitzte. Die war ab er echt. Scheiss RL!&quot;"
 *          },
 *		    ...
 *		]
 * </code>
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public class KarolenderBlatt
{
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = JSONUtil.DATE_FORMAT)
	private Date posted;
    private String line;
	
	public KarolenderBlatt()
	{
		super();
	}

	public KarolenderBlatt(Date posted, String line)
	{
		super();
		this.posted = posted;
        this.line = line;
	}

	public Date getPosted()
	{
		return posted;
	}

	public void setPosted(Date posted)
	{
		this.posted = posted;
	}

	public String getLine()
	{
		return line;
	}

	public void setLine(String line)
	{
		this.line = line;
	}
}
