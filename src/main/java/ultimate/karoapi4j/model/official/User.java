package ultimate.karoapi4j.model.official;

import java.awt.Color;
import java.util.List;

public class User
{
	/*
	 * user/1/info.json
	 * "id" : 1,
	 * "login" : "Didi",
	 * "color" : "FFFFFF",
	 * "lastVisit" : 17,
	 * "signup" : 3798,
	 * "dran" : 68,
	 * "activeGames" : 102,
	 * "maxGames" : 123,
	 * "sound" : 10,
	 * "soundfile" : "\/mp3\/brumm.mp3",
	 * "size" : 12,
	 * "border" : 1,
	 * "desperate" : false,
	 * "birthdayToday" : false,
	 * "karodayToday" : false,
	 * "gravatar" :
	 * "http:\/\/www.gravatar.com\/avatar\/bb493dfa04160c4c284b8740a5b23557?default=http%3A%2F%2Fwww.karopapier.de%2Ffavicon.gif&size=40"
	 */
	// Standard JSON Fields
	private int				id;
	private String			login;
	private Color			color;
	private int				lastVisit;
	private int				signup;
	private int				dran;
	private int				activeGames;
	private int				maxGames;
	private int				sound;
	private String			soundfile;
	private int				size;
	private int				border;
	private boolean			desperate;
	private boolean			birthdayToday;
	private boolean			karodayToday;
	private String			gravatar;
	// additional Fields
	private int				plannedGames;
	// references
	private List<Blocker>	blocker;
	private List<Game>		games;

	// TODO private boolean invitableNormal;
	// TODO private boolean invitableNight;

	public User()
	{
		super();
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

	public String getGravatar()
	{
		return gravatar;
	}

	public int getPlannedGames()
	{
		return plannedGames;
	}

	public List<Blocker> getBlocker()
	{
		return blocker;
	}

	public List<Game> getGames()
	{
		return games;
	}

	public void setId(int id)
	{
		this.id = id;
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

	public void setGravatar(String gravatar)
	{
		this.gravatar = gravatar;
	}

	public void setPlannedGames(int plannedGames)
	{
		this.plannedGames = plannedGames;
	}

	public void setBlocker(List<Blocker> blocker)
	{
		this.blocker = blocker;
	}

	public void setGames(List<Game> games)
	{
		this.games = games;
	}
}
