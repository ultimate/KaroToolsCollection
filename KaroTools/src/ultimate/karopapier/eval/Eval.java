package ultimate.karopapier.eval;

import muskel2.model.GameSeries;

public interface Eval
{
	public String doEvaluation() throws Exception;
	
	public void prepare(GameSeries gs, int execution);
}
