package ultimate.karoapi4j.model.official;

import java.awt.Color;
import java.util.List;

import ultimate.karoapi4j.enums.EnumUserGamesort;
import ultimate.karoapi4j.enums.EnumUserState;
import ultimate.karoapi4j.enums.EnumUserTheme;

public class User
{
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
	 * "uc": 1 // for user check only
	 */
	// Standard JSON Fields
	private int					id;
	private String				login;
	private Color				color;
	private int					lastVisit;
	private int					signup;
	private int					dran;
	private int					activeGames;
	private boolean				acceptDayGames;
	private boolean				acceptNightGames;
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
	private boolean				uc;
	private int					blocked;
	// additional Fields
	private int					plannedGames;
	private List<Game>			games;

	public User()
	{
		super();
	}

	public User(int id)
	{
		this();
		this.id = id;
	}

	public User(String login)
	{
		this();
		this.login = login;
	}

	public int getId()
	{
		return id;
	}

	public String getLogin()
	{
		return login;
	}

	public Color getColor()
	{
		return color;
	}

	public int getLastVisit()
	{
		return lastVisit;
	}

	public int getSignup()
	{
		return signup;
	}

	public int getDran()
	{
		return dran;
	}

	public int getActiveGames()
	{
		return activeGames;
	}

	public int getMaxGames()
	{
		return maxGames;
	}

	public int getSound()
	{
		return sound;
	}

	public String getSoundfile()
	{
		return soundfile;
	}

	public int getSize()
	{
		return size;
	}

	public int getBorder()
	{
		return border;
	}

	public boolean isDesperate()
	{
		return desperate;
	}

	public boolean isBirthdayToday()
	{
		return birthdayToday;
	}

	public boolean isKarodayToday()
	{
		return karodayToday;
	}

	public int getPlannedGames()
	{
		return plannedGames;
	}

	public List<Game> getGames()
	{
		return games;
	}

	public void setId(int id)
	{
		this.id = id;

		// this.games // load the games?!
	}

	public void setLogin(String login)
	{
		this.login = login;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public void setLastVisit(int lastVisit)
	{
		this.lastVisit = lastVisit;
	}

	public void setSignup(int signup)
	{
		this.signup = signup;
	}

	public void setDran(int dran)
	{
		this.dran = dran;
	}

	public void setActiveGames(int activeGames)
	{
		this.activeGames = activeGames;
	}

	public void setMaxGames(int maxGames)
	{
		this.maxGames = maxGames;
	}

	public void setSound(int sound)
	{
		this.sound = sound;
	}

	public void setSoundfile(String soundfile)
	{
		this.soundfile = soundfile;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public void setBorder(int border)
	{
		this.border = border;
	}

	public void setDesperate(boolean desperate)
	{
		this.desperate = desperate;
	}

	public void setBirthdayToday(boolean birthdayToday)
	{
		this.birthdayToday = birthdayToday;
	}

	public void setKarodayToday(boolean karodayToday)
	{
		this.karodayToday = karodayToday;
	}

	public void setPlannedGames(int plannedGames)
	{
		this.plannedGames = plannedGames;
	}

	public void setGames(List<Game> games)
	{
		this.games = games;
	}

	public boolean isAcceptDayGames()
	{
		return acceptDayGames;
	}

	public void setAcceptDayGames(boolean acceptDayGames)
	{
		this.acceptDayGames = acceptDayGames;
	}

	public boolean isAcceptNightGames()
	{
		return acceptNightGames;
	}

	public void setAcceptNightGames(boolean acceptNightGames)
	{
		this.acceptNightGames = acceptNightGames;
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

	// derived from other information
	public boolean isInvitable()
	{
		return acceptDayGames && (activeGames < maxGames || maxGames <= 0);
	}

	public boolean isInvitableNight()
	{
		return acceptNightGames && (activeGames < maxGames || maxGames <= 0);
	}
}
