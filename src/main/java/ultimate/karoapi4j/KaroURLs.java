package ultimate.karoapi4j;

/**
 * List of all known KaroAPI-URLs used for initializing Karopapier, Chat, etc.
 * 
 * @author ultimate
 */
public interface KaroURLs
{
	/**
	 * http://reloaded.karopapier.de/api<br>
	 */
	public static final String	API_BASE			= "http://reloaded.karopapier.de/api";

	/**
	 * The placeholder used in some URLs (e.g. User, Game)
	 */
	public static final String	PLACEHOLDER			= "${i}";

	/**
	 * http://reloaded.karopapier.de/api/chat/list.json<br>
	 * http://reloaded.karopapier.de/api/chat/list.json?limit=1<br>
	 */
	public static final String	CHAT_LIST			= API_BASE + "/chat/list.json";

	/**
	 * http://reloaded.karopapier.de/api/chat/users.json<br>
	 */
	public static final String	CHAT_USERS			= API_BASE + "/chat/users.json";

	/**
	 * http://reloaded.karopapier.de/api/game/44773/details.json<br>
	 * http://reloaded.karopapier.de/api/games/44773/details<br>
	 */
	public static final String	GAME_DETAILS		= API_BASE + "/game/" + PLACEHOLDER + "/details.json";

	/**
	 * http://reloaded.karopapier.de/api/games/44773/info<br>
	 * http://reloaded.karopapier.de/api/game/44773/info.json<br>
	 */
	public static final String	GAME_INFO			= API_BASE + "/game/" + PLACEHOLDER + "/info.json";

	/**
	 * http://reloaded.karopapier.de/api/games?user=1<br>
	 * http://reloaded.karopapier.de/api/games?user=1&finished=true<br>
	 * http://reloaded.karopapier.de/api/games?user=1&finished=true&limit=1&offset=300<br>
	 * http://reloaded.karopapier.de/api/games?limit=2&offset=3000<br>
	 */
	public static final String	GAME_LIST			= API_BASE + "/games";

	/**
	 * http://reloaded.karopapier.de/api/map/list.json<br>
	 */
	public static final String	MAP_LIST			= API_BASE + "/map/list.json";
	
	/**
	 * http://reloaded.karopapier.de/api/map/1/vote.json<br>
	 */
	public static final String	MAP_VOTE			= API_BASE + "/map/" + PLACEHOLDER + "/vote.json";

	/**
	 * http://reloaded.karopapier.de/api/user/1/blocker.json<br>
	 */
	public static final String	USER_BLOCKER		= API_BASE + "/user/" + PLACEHOLDER + "/blocker.json";

	/**
	 * http://reloaded.karopapier.de/api/user/blockerlist.json<br>
	 */
	public static final String	USER_BLOCKERLIST	= API_BASE + "/user/blockerlist.json";

	/**
	 * http://reloaded.karopapier.de/api/user/check.json<br>
	 */
	public static final String	USER_CHECK			= API_BASE + "/user/check.json";

	/**
	 * http://reloaded.karopapier.de/api/user/1/dran.json<br>
	 * http://reloaded.karopapier.de/api/user/Botrix/dran.json<br>
	 */
	public static final String	USER_DRAN			= API_BASE + "/user/" + PLACEHOLDER + "/dran.json";

	/**
	 * http://reloaded.karopapier.de/api/user/1/info.json<br>
	 * http://reloaded.karopapier.de/api/user/Botrix/info.json<br>
	 */
	public static final String	USER_INFO			= API_BASE + "/user/" + PLACEHOLDER + "/info.json";

	/**
	 * http://reloaded.karopapier.de/api/user/list.json<br>
	 */
	public static final String	USER_LIST			= API_BASE + "/user/list.json";

	/**
	 * http://reloaded.karopapier.de/api/chat/list.json?limit=1<br>
	 */
	public static final String	PARAMETER_LIMIT		= "limit";

	/**
	 * http://reloaded.karopapier.de/api/games?user=1&finished=true&limit=1&offset=300<br>
	 * http://reloaded.karopapier.de/api/games?limit=2&offset=3000<br>
	 */
	public static final String	PARAMETER_OFFSET	= "offset";

	/**
	 * http://reloaded.karopapier.de/api/games?user=1<br>
	 * http://reloaded.karopapier.de/api/games?user=1&finished=true<br>
	 * http://reloaded.karopapier.de/api/games?user=1&finished=true&limit=1&offset=300<br>
	 */
	public static final String	PARAMETER_USER		= "user";

	/**
	 * http://reloaded.karopapier.de/api/games?user=1&finished=true<br>
	 * http://reloaded.karopapier.de/api/games?user=1&finished=true&limit=1&offset=300<br>
	 */
	public static final String	PARAMETER_FINISHED	= "finished";
}
