package ultimate.karopapier.muskelx.core.karoaccess;

import muskel2.model.Game;

public interface Notifyable
{
	public void notifyGameCreated(Game game);

	public void notifyGameLeft(Game game);
}
