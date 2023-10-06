package ultimate.karoapi4j.model.extended;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.JSONUtil.ToIDArrayConverter;

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
	@JsonSerialize(converter = ToIDArrayConverter.class)
	@JsonDeserialize(converter = User.FromIDArrayToSetConverter.class)
	private Set<User>			members;
	/**
	 * the (optional) home {@link Map}
	 */
	@JsonInclude(value = Include.NON_NULL)
	@JsonSerialize(using = PlaceToRace.Serializer.class)
	@JsonDeserialize(using = PlaceToRace.Deserializer.class)
	private PlaceToRace			homeMap;


	/**
	 * Default constructor
	 */
	public Team()
	{
	}
	
	/**
	 * Create a new team (without home {@link Map})
	 * 
	 * @param name - the team name
	 * @param member - the only members
	 * @see User
	 */
	public Team(String name, User member)
	{
		this(name, member, null);
	}

	/**
	 * Create a new team (with home {@link Map})
	 * 
	 * @param name - the team name
	 * @param member - the only members
	 * @param homeMap - the home {@link Map}
	 * @see User
	 */
	public Team(String name, User member, PlaceToRace homeMap)
	{
		this.name = name;
		this.members = new LinkedHashSet<User>();
		this.members.add(member);
		this.homeMap = homeMap;
	}
	
	/**
	 * Create a new team (without home {@link Map})
	 * 
	 * @param name - the team name
	 * @param members - the list of members
	 * @see User
	 */
	public Team(String name, Collection<User> members)
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
	public Team(String name, Collection<User> members, PlaceToRace homeMap)
	{
		this.name = name;
		this.members = new LinkedHashSet<User>(members);
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
	public Set<User> getMembers()
	{
		return members;
	}

	/**
	 * Set the list of members
	 * 
	 * @param name - the list of members
	 */
	public void setMembers(Set<User> members)
	{
		this.members = members;
	}

	/**
	 * Get the home {@link Map}
	 * 
	 * @return the home {@link Map}
	 */
	public PlaceToRace getHomeMap()
	{
		return homeMap;
	}

	/**
	 * Set the home {@link Map}
	 * 
	 * @param name - the home {@link Map}
	 */
	public void setHomeMap(PlaceToRace homeMap)
	{
		this.homeMap = homeMap;
	}
}
