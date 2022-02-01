package ultimate.karoapi4j.model.extended;

import java.util.LinkedList;
import java.util.List;

import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.User;

/**
 * Simple POJO that defines a team.<br>
 * Each team has
 * <ul>
 * <li>a name</li>
 * <li>a list of members</li>
 * <li>a home map (optional)</li>
 * </ul>
 * 
 * @author ultimate
 */
public class Team
{
	/**
	 * the team name
	 */
	private String				name;
	/**
	 * the list of members
	 * 
	 * @see User
	 */
	private List<User>			members;
	/**
	 * the (optional) home {@link Map}
	 */
	private Map					homeMap;

	/**
	 * Create a new team (without home {@link Map})
	 * 
	 * @param name - the team name
	 * @param members - the list of members
	 * @see User
	 */
	public Team(String name, List<User> members)
	{
		this(name, members, null);
	}

	/**
	 * Create a new team (with home {@link Map})
	 * 
	 * @param name - the team name
	 * @param members - the list of members
	 * @param homeMap - the home {@link Map}
	 * @see User
	 */
	public Team(String name, List<User> members, Map homeMap)
	{
		this.name = name;
		this.members = new LinkedList<User>(members);
		this.homeMap = homeMap;
	}

	/**
	 * Get the team name
	 * 
	 * @return the team name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Set the team name
	 * 
	 * @param name - the new team name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Get the list of members
	 * 
	 * @return the list of members
	 */
	public List<User> getMembers()
	{
		return members;
	}

	/**
	 * Set the list of members
	 * 
	 * @param name - the list of members
	 */
	public void setMembers(List<User> members)
	{
		this.members = members;
	}

	/**
	 * Get the home {@link Map}
	 * 
	 * @return the home {@link Map}
	 */
	public Map getHomeMap()
	{
		return homeMap;
	}

	/**
	 * Set the home {@link Map}
	 * 
	 * @param name - the home {@link Map}
	 */
	public void setHomeMap(Map homeMap)
	{
		this.homeMap = homeMap;
	}
}
