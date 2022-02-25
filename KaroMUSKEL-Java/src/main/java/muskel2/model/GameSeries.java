package muskel2.model;

import java.io.Serializable;
import java.util.List;

@Deprecated
public abstract class GameSeries implements Serializable
{
	public static final long	serialVersionUID	= 2L;

	public String				title;
	public String				defaultTitleKey;
	@Deprecated
	public String				patternKey;
	public Player				creator;
	public Rules				rules;
	public List<Game>			games;
	public List<Player>			players;
	public List<Map>			maps;
}
