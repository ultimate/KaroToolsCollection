package muskel2.model;

import java.io.Serializable;
import java.util.List;

import muskel2.model.help.Identifiable;

@Deprecated
public class Game implements Serializable, Identifiable
{
	private static final long	serialVersionUID	= 1L;

	public Integer				id;
	public String				name;
	public Map					map;
	public List<Player>			players;
	public Rules				rules;

	public boolean				created;
	public boolean				left;

	@Override
	public int getId()
	{
		return id;
	}
}
