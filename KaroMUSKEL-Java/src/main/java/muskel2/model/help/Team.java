package muskel2.model.help;

import java.io.Serializable;
import java.util.List;

import muskel2.model.Map;
import muskel2.model.Player;
@Deprecated
public class Team implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	public String				name;
	public List<Player>		players;
	public Map					homeMap;
}
