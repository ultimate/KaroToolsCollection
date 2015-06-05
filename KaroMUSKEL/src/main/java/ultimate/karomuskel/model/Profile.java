package ultimate.karomuskel.model;

public class Profile
{
	private String	karoUsername;
	private String	karoPassword;
	private String	wikiUsername;
	private String	wikiPassword;
	private String	key;

	public Profile()
	{
		this("", "", "", "", "");
	}

	public Profile(String karoUsername, String karoPassword, String wikiUsername, String wikiPassword, String key)
	{
		super();
		this.karoUsername = karoUsername;
		this.karoPassword = karoPassword;
		this.wikiUsername = wikiUsername;
		this.wikiPassword = wikiPassword;
		this.key = key;
	}

	public String getKaroUsername()
	{
		return karoUsername;
	}

	public String getKaroPassword()
	{
		return karoPassword;
	}

	public String getWikiUsername()
	{
		return wikiUsername;
	}

	public String getWikiPassword()
	{
		return wikiPassword;
	}

	public String getKey()
	{
		return key;
	}

	public void setKaroUsername(String karoUsername)
	{
		this.karoUsername = karoUsername;
	}

	public void setKaroPassword(String karoPassword)
	{
		this.karoPassword = karoPassword;
	}

	public void setWikiUsername(String wikiUsername)
	{
		this.wikiUsername = wikiUsername;
	}

	public void setWikiPassword(String wikiPassword)
	{
		this.wikiPassword = wikiPassword;
	}

	public void setKey(String key)
	{
		this.key = key;
	}
}
