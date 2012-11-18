package ultimate.karoapi4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import ultimate.karoapi4j.enums.EnumRefreshMode;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.sync.BaseRefreshing;
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
	 * The URLLoader used to load the users
	 */
	private URLLoader<List<User>>	userLoader;
	/**
	 * The list of users
	 */
	private SynchronizedList<User>	users;
	/**
	 * The URLLoader used to load the maps
	 */
	private URLLoader<List<Map>>	mapLoader;
	/**
	 * The list of maps
	 */
	private SynchronizedList<Map>	maps;

	public Karopapier()
	{
		try
		{
			userLoader = new CollectionURLLoaderThread<List<User>>(new URL(KaroURLs.USER_LIST));
			users = new SynchronizedList<User>(userLoader, EnumRefreshMode.manual, false);

			mapLoader = new CollectionURLLoaderThread<List<Map>>(new URL(KaroURLs.MAP_LIST));
			maps = new SynchronizedList<Map>(mapLoader, EnumRefreshMode.manual, false);
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
	 * Refresh the user list
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
	 * Refresh the map list
	 * @see SynchronizedList#refresh()
	 */
	public void refreshMaps()
	{
		maps.refresh();
	}
}
