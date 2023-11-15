package ultimate.karoapi4j.enums;

import ultimate.karoapi4j.KaroAPI;

/**
 * The game sort order as defined by the {@link KaroAPI}
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public enum EnumUserGamesort
{
	/**
	 * sort by game-id
	 */
	gid,
	/**
	 * sort by name
	 */
	name,
	/**
	 * sort by map-id
	 */
	mapid,
	/**
	 * sort by block-time
	 */
	blocktime,
	/**
	 * sort by block-time (inverted)
	 */
	blocktime2,
}
