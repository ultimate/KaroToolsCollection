package ultimate.karoapi4j.model.extended;

public class AddictInfo
{
	private String	login;
	private int		signup;
	private int		gamesTotal;
	private int		movesTotal;
	private double	movesPerDay;
	private int		wollustMax;
	private int		wollust;
	private int		karoMeter;
	private int		karoMilliMeterPerHour;

	public AddictInfo()
	{
		super();
	}

	public AddictInfo(String login)
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

	public int getSignup()
	{
		return signup;
	}

	public void setSignup(int signup)
	{
		this.signup = signup;
	}

	public int getGamesTotal()
	{
		return gamesTotal;
	}

	public void setGamesTotal(int gamesTotal)
	{
		this.gamesTotal = gamesTotal;
	}

	public int getMovesTotal()
	{
		return movesTotal;
	}

	public void setMovesTotal(int movesTotal)
	{
		this.movesTotal = movesTotal;
	}

	public double getMovesPerDay()
	{
		return movesPerDay;
	}

	public void setMovesPerDay(double movesPerDay)
	{
		this.movesPerDay = movesPerDay;
	}

	public int getWollustMax()
	{
		return wollustMax;
	}

	public void setWollustMax(int wollustMax)
	{
		this.wollustMax = wollustMax;
	}

	public int getWollust()
	{
		return wollust;
	}

	public void setWollust(int wollust)
	{
		this.wollust = wollust;
	}

	public int getKaroMeter()
	{
		return karoMeter;
	}

	public void setKaroMeter(int karoMeter)
	{
		this.karoMeter = karoMeter;
	}

	public int getKaroMilliMeterPerHour()
	{
		return karoMilliMeterPerHour;
	}

	public void setKaroMilliMeterPerHour(int karoMilliMeterPerHour)
	{
		this.karoMilliMeterPerHour = karoMilliMeterPerHour;
	}
}
