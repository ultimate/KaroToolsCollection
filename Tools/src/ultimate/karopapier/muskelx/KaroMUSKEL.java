package ultimate.karopapier.muskelx;

import ultimate.karopapier.muskelx.model.Karopapier;
import ultimate.karopapier.muskelx.model.Profile;
import ultimate.karopapier.muskelx.model.Settings;

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
