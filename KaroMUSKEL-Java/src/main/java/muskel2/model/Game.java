package muskel2.model;

import java.io.Serializable;
import java.util.List;

@Deprecated
public class Game implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	public Integer				id;
	public String				name;
	public Map					map;
	public List<Player>			players;
	public Rules				rules;

	public boolean				created;
	public boolean				left;
}
