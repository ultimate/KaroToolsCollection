package ultimate.karoapi4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import ultimate.karoapi4j.enums.EnumRefreshMode;
import ultimate.karoapi4j.model.official.ChatMessage;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.URLLoader;
import ultimate.karoapi4j.utils.sync.BaseRefreshing;
import ultimate.karoapi4j.utils.sync.SynchronizedList;
import ultimate.karoapi4j.utils.web.urlloaders.JSONURLLoaderThread;

/**
 * Karopapier Chat implementation allowing easy access to the chat messages and the currently active Chat users
 * 
 * @author ultimate
 */
public class Chat extends BaseRefreshing<Chat>
{
	// TODO add functionality to load history on demand
	// TODO change amount of entries loaded on first load
	// TODO synchronize User-Information with User-List
	
	/**
	 * The URLLoader used to load the history of ChatEntries
	 */
	private URLLoader<List<ChatMessage>>	entryLoader;
	/**
	 * The list/history of ChatEntries
	 */
	private SynchronizedList<ChatMessage>	entries;
	/**
	 * The URLLoader used to load the currently active chat users
	 */
	private URLLoader<List<User>>		userLoader;
	/**
	 * The list of currently active chat users
	 */
	private SynchronizedList<User>		users;

	/**
	 * Construct and initialize a new Chat entity with given update intervals (as int)
	 * 
	 * @see EnumRefreshMode#forInterval(int)
	 * @see Chat#Chat(EnumRefreshMode, EnumRefreshMode, int)
	 * @param entryInterval - the interval for updating the chat entries
	 * @param userInterval - the interval for updating the users
	 * @param entryUpdateLimit - the maximum amount of old chat entries to load on update
	 */
	public Chat(int entryInterval, int userInterval, int entryUpdateLimit)
	{
		this(EnumRefreshMode.forInterval(entryInterval), EnumRefreshMode.forInterval(userInterval), entryUpdateLimit);
	}

	/**
	 * Construct and initialize a new Chat entity with given update intervals (as Enum)
	 * 
	 * @see EnumRefreshMode
	 * @param entryRefreshMode - the interval for updating the chat entries
	 * @param userRefreshMode - the interval for updating the users
	 * @param entryUpdateLimit - the maximum amount of old chat entries to load on update
	 */
	public Chat(EnumRefreshMode entryRefreshMode, EnumRefreshMode userRefreshMode, int entryUpdateLimit)
	{
		if(entryRefreshMode == null)
			throw new IllegalArgumentException("entryRefreshMode must not be null!");
		if(userRefreshMode == null)
			throw new IllegalArgumentException("userRefreshMode must not be null!");
		if(entryUpdateLimit < 1)
			throw new IllegalArgumentException("entryUpdateLimit must be >= 1");

		try
		{
			entryLoader = new JSONURLLoaderThread<List<ChatMessage>>(new URL(KaroURLs.CHAT_LIST), KaroURLs.PARAMETER_LIMIT + "="
					+ entryUpdateLimit);
			entries = new SynchronizedList<ChatMessage>(entryLoader, entryRefreshMode, false);

			userLoader = new JSONURLLoaderThread<List<User>>(new URL(KaroURLs.CHAT_USERS));
			users = new SynchronizedList<User>(userLoader, userRefreshMode, true);
		}
		catch(MalformedURLException e)
		{
			logger.error("This should never happen!", e);
		}
	}

	/**
	 * The list/history of ChatEntries
	 * 
	 * @return the ChatEntries
	 */
	public SynchronizedList<ChatMessage> getEntries()
	{
		return entries;
	}

	/**
	 * The list of currently active chat users
	 * 
	 * @return the Users
	 */
	public SynchronizedList<User> getUsers()
	{
		return users;
	}

	/**
	 * The interval for updating the chat entries
	 * 
	 * @return entryRefreshMode
	 */
	public EnumRefreshMode getEntryRefreshMode()
	{
		return entries.getRefreshMode();
	}

	/**
	 * The interval for updating the users
	 * 
	 * @return userRefreshMode
	 */
	public EnumRefreshMode getUserRefreshMode()
	{
		return users.getRefreshMode();
	}
}
