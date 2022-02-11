package ultimate.karoapi4j.model.official;

import java.awt.Color;
import java.util.Date;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.enums.EnumUserGamesort;
import ultimate.karoapi4j.enums.EnumUserState;
import ultimate.karoapi4j.enums.EnumUserTheme;
import ultimate.karoapi4j.model.base.Identifiable;
import ultimate.karoapi4j.utils.JSONUtil;

/**
 * POJO User as defined by the {@link KaroAPI}
 * 
 * @see <a href="https://www.karopapier.de/api/">https://www.karopapier.de/api/</a>
 * @author ultimate
 */
public class User extends Identifiable
{
	//@formatter:off
	public static class FromIDConverter extends JSONUtil.FromIDConverter<User> { public FromIDConverter() { super(User.class); } };
	public static class FromIDListConverter extends JSONUtil.FromIDListConverter<User> { public FromIDListConverter() { super(User.class); } };
	public static class FromIDMapConverter extends JSONUtil.FromIDMapConverter<User> { public FromIDMapConverter() { super(User.class); } };
	//@formatter:on

	public static final int		INVITABLE_LAST_VISIT_LIMIT	= 3;
	/*
	 * https://www.karopapier.de/api/users/1
	 * "id": 1,
	 * "login": "Didi",
	 * "color": "ffffff",
	 * "lastVisit": 0,
	 * "signup": 7288,
	 * "dran": 8,
	 * "activeGames": 66,
	 * "acceptsDayGames": true,
	 * "acceptsNightGames": true,
	 * "maxGames": 150,
	 * "sound": 10,
	 * "soundfile": "/mp3/quiek.mp3",
	 * "size": 12,
	 * "border": 1,
	 * "desperate": false,
	 * "birthdayToday": false,
	 * "karodayToday": false,
	 * "theme": "karo1",
	 * "bot": false,
	 * "gamesort": "blocktime",
	 * "state": "active",
	 * "superCreator": true,
	 * "uc": 1 // for current user only
	 * "blocked": "6", // for blockers only
	 * "ts": "2022-01-14 12:20:55", // for contacts only
	 */
	// Standard JSON Fields
	// private int id; // see super class
	private String				login;
	private Color				color;
	private int					lastVisit;
	private int					signup;
	private int					dran;
	private int					activeGames;
	private boolean				acceptsDayGames;
	private boolean				acceptsNightGames;
	private int					maxGames;
	private int					sound;
	private String				soundfile;
	private int					size;
	private int					border;
	private boolean				desperate;
	private boolean				birthdayToday;
	private boolean				karodayToday;
	private EnumUserTheme		theme;
	private boolean				bot;
	private EnumUserGamesort	gamesort;
	private EnumUserState		state;
	private boolean				superCreator;
	private Date				ts;
	private boolean				uc;
	private int					blocked;
	// additional Fields (internally used)
	private int					plannedGames;

	public User()
	{
		super();
	}

	public User(Integer id)
	{
		super(id);
	}

	public User(String login)
	{
		this();
		this.login = login;
	}

	public String getLogin()
	{
		return login;
	}

	public void setLogin(String login)
	{
		this.login = login;
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public int getLastVisit()
	{
		return lastVisit;
	}

	public void setLastVisit(int lastVisit)
	{
		this.lastVisit = lastVisit;
	}

	public int getSignup()
	{
		return signup;
	}

	public void setSignup(int signup)
	{
		this.signup = signup;
	}

	public int getDran()
	{
		return dran;
	}

	public void setDran(int dran)
	{
		this.dran = dran;
	}

	public int getActiveGames()
	{
		return activeGames;
	}

	public void setActiveGames(int activeGames)
	{
		this.activeGames = activeGames;
	}

	public boolean isAcceptsDayGames()
	{
		return acceptsDayGames;
	}

	public void setAcceptsDayGames(boolean acceptsDayGames)
	{
		this.acceptsDayGames = acceptsDayGames;
	}

	public boolean isAcceptsNightGames()
	{
		return acceptsNightGames;
	}

	public void setAcceptsNightGames(boolean acceptsNightGames)
	{
		this.acceptsNightGames = acceptsNightGames;
	}

	public int getMaxGames()
	{
		return maxGames;
	}

	public void setMaxGames(int maxGames)
	{
		this.maxGames = maxGames;
	}

	public int getSound()
	{
		return sound;
	}

	public void setSound(int sound)
	{
		this.sound = sound;
	}

	public String getSoundfile()
	{
		return soundfile;
	}

	public void setSoundfile(String soundfile)
	{
		this.soundfile = soundfile;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public int getBorder()
	{
		return border;
	}

	public void setBorder(int border)
	{
		this.border = border;
	}

	public boolean isDesperate()
	{
		return desperate;
	}

	public void setDesperate(boolean desperate)
	{
		this.desperate = desperate;
	}

	public boolean isBirthdayToday()
	{
		return birthdayToday;
	}

	public void setBirthdayToday(boolean birthdayToday)
	{
		this.birthdayToday = birthdayToday;
	}

	public boolean isKarodayToday()
	{
		return karodayToday;
	}

	public void setKarodayToday(boolean karodayToday)
	{
		this.karodayToday = karodayToday;
	}

	public EnumUserTheme getTheme()
	{
		return theme;
	}

	public void setTheme(EnumUserTheme theme)
	{
		this.theme = theme;
	}

	public boolean isBot()
	{
		return bot;
	}

	public void setBot(boolean bot)
	{
		this.bot = bot;
	}

	public EnumUserGamesort getGamesort()
	{
		return gamesort;
	}

	public void setGamesort(EnumUserGamesort gamesort)
	{
		this.gamesort = gamesort;
	}

	public EnumUserState getState()
	{
		return state;
	}

	public void setState(EnumUserState state)
	{
		this.state = state;
	}

	public boolean isSuperCreator()
	{
		return superCreator;
	}

	public void setSuperCreator(boolean superCreator)
	{
		this.superCreator = superCreator;
	}

	public Date getTs()
	{
		return ts;
	}

	public void setTs(Date ts)
	{
		this.ts = ts;
	}

	public boolean isUc()
	{
		return uc;
	}

	public void setUc(boolean uc)
	{
		this.uc = uc;
	}

	public int getBlocked()
	{
		return blocked;
	}

	public void setBlocked(int blocked)
	{
		this.blocked = blocked;
	}

	// additional information

	public int getPlannedGames()
	{
		return plannedGames;
	}

	public void setPlannedGames(int plannedGames)
	{
		this.plannedGames = plannedGames;
	}

	// derived from other information

	public boolean isInvitable(boolean night)
	{
		boolean withinLimit = (activeGames < maxGames || maxGames <= 0) && lastVisit <= INVITABLE_LAST_VISIT_LIMIT;
		return (night ? acceptsNightGames : acceptsDayGames) && withinLimit;
	}
}
