package ultimate.karopapier.muskelx;

import com.sun.scenario.Settings;

import sun.java2d.cmm.Profile;
import ultimate.karopapier.muskelx.model.Karopapier;

public class KaroMUSKEL
{
	private Karopapier	karopapier;
	private Settings	settings;
	private Profile		activeProfile;

	public KaroMUSKEL(Karopapier karopapier, Settings settings, Profile activeProfile)
	{
		super();
		this.karopapier = karopapier;
		this.settings = settings;
		this.activeProfile = activeProfile;
	}

	public Karopapier getKaropapier()
	{
		return karopapier;
	}

	public Settings getSettings()
	{
		return settings;
	}

	public Profile getActiveProfile()
	{
		return activeProfile;
	}
}
