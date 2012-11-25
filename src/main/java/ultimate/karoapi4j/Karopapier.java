package ultimate.karoapi4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import ultimate.karoapi4j.core.KaropapierLoader;
import ultimate.karoapi4j.enums.EnumRefreshMode;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.sync.BaseRefreshing;
import ultimate.karoapi4j.utils.sync.GenericSynchronizedCollectionMap;
import ultimate.karoapi4j.utils.sync.SynchronizedList;
import ultimate.karoapi4j.utils.web.URLLoader;
import ultimate.karoapi4j.utils.web.urlloaders.CollectionURLLoaderThread;

/**
 * Karopapier Main implementation allowing easy access to all registered users, available maps, all
 * games etc.
 * 
 * @author ultimate
 */
public class Karopapier extends BaseRefreshing<Karopapier>
{
	/**
	 * The KaropapierLoader used to load URL content
	 */
	private KaropapierLoader								loader;

	/**
	 * The URLLoader used to load the users
	 */
	private URLLoader<List<User>>							userLoader;
	/**
	 * The list of users
	 */
	private SynchronizedList<User>							users;

	/**
	 * A Map of the users sorted by name
	 */
	private GenericSynchronizedCollectionMap<String, User>	users_byName;
	/**
	 * A Map of the users sorted by ID
	 */
	private GenericSynchronizedCollectionMap<String, User>	users_byID;

	/**
	 * The URLLoader used to load the maps
	 */
	private URLLoader<List<Map>>							mapLoader;
	/**
	 * The list of maps
	 */
	private SynchronizedList<Map>							maps;

	/**
	 * A Map of the maps sorted by name
	 */
	private GenericSynchronizedCollectionMap<String, Map>	maps_byName;
	/**
	 * A Map of the maps sorted by ID
	 */
	private GenericSynchronizedCollectionMap<String, Map>	maps_byID;

	/**
	 * The user currently logged-in
	 */
	private User											currentUser;

	/**
	 * Initiate Karopapier
	 * 
	 * @param loader - The KaropapierLoader used to load URL content
	 */
	public Karopapier(KaropapierLoader loader)
	{

		try
		{
			userLoader = new CollectionURLLoaderThread<List<User>>(new URL(KaroURLs.USER_LIST));

			users = new SynchronizedList<User>(userLoader, EnumRefreshMode.manual, false);

			users_byName = new GenericSynchronizedCollectionMap.Tree<String, User>(userLoader, EnumRefreshMode.manual, false, "getLogin");
			users_byID = new GenericSynchronizedCollectionMap.Tree<String, User>(userLoader, EnumRefreshMode.manual, false, "getId");

			mapLoader = new CollectionURLLoaderThread<List<Map>>(new URL(KaroURLs.MAP_LIST));
			maps = new SynchronizedList<Map>(mapLoader, EnumRefreshMode.manual, false);

			maps_byName = new GenericSynchronizedCollectionMap.Tree<String, Map>(mapLoader, EnumRefreshMode.manual, false, "getName");
			maps_byID = new GenericSynchronizedCollectionMap.Tree<String, Map>(mapLoader, EnumRefreshMode.manual, false, "getId");
		}
		catch(MalformedURLException e)
		{
			logger.error("This should never happen!", e);
		}
	}

	/**
	 * The list of users
	 * 
	 * @return users
	 */
	public SynchronizedList<User> getUsers()
	{
		return users;
	}

	/**
	 * Get a User by username
	 * 
	 * @param name - the user's name
	 * @return the User
	 */
	public User getUser(String name)
	{
		return users_byName.get(name);
	}

	/**
	 * Get a User by ID
	 * 
	 * @param id - the user's ID
	 * @return the User
	 */
	public User getUser(int id)
	{
		return users_byID.get(id);
	}

	/**
	 * Refresh the user list
	 * 
	 * @see SynchronizedList#refresh()
	 */
	public void refreshUsers()
	{
		users.refresh();
	}

	/**
	 * The list of maps
	 * 
	 * @return maps
	 */
	public SynchronizedList<Map> getMaps()
	{
		return maps;
	}

	/**
	 * Get a Map by mapname
	 * 
	 * @param name - the map's name
	 * @return the Map
	 */
	public Map getMap(String name)
	{
		return maps_byName.get(name);
	}

	/**
	 * Get a Map by ID
	 * 
	 * @param id - the map's ID
	 * @return the Map
	 */
	public Map getMap(int id)
	{
		return maps_byID.get(id);
	}

	/**
	 * Refresh the map list
	 * 
	 * @see SynchronizedList#refresh()
	 */
	public void refreshMaps()
	{
		maps.refresh();
	}

	/**
	 * Perform the login for the given user
	 * 
	 * @param username - the user to log-in
	 * @param password - the password for log-in
	 * @return the User-Object
	 */
	public User login(String username, String password)
	{
		try
		{
			if(loader.login(username, password))
			{
				currentUser = getUser(username);
			}
		}
		catch(IOException e)
		{
			logger.error("User login failed: " + e.getMessage());
		}

		return currentUser;
	}

	/**
	 * The user currently logged-in
	 * 
	 * @return currentUser
	 */
	public User getCurrentUser()
	{
		return currentUser;
	}
}
