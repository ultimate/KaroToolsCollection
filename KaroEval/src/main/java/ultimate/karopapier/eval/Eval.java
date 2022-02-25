package ultimate.karopapier.eval;

import ultimate.karoapi4j.model.extended.GameSeries;

public interface Eval
{
	public String doEvaluation() throws Exception;
	
	public void prepare(GameSeries gs, int execution);
}
