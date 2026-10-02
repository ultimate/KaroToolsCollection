package ultimate.karopapier.eval.impl;

import ultimate.karopapier.eval.CCCEvalNew;
import ultimate.karopapier.utils.WikiUtil;

public class CCC7Eval extends CCCEvalNew
{
	public CCC7Eval()
	{
		super(7);
	}
	
	protected String gameToLink(int challenge, int game)
	{
		return WikiUtil.createLink(getGame(challenge, game), (challenge + 1) + "." + ((game + 1) / 5) + "." + ((game + 1) % 5) );
	}
}
