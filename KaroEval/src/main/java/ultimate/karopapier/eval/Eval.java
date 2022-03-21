package ultimate.karopapier.eval;

import ultimate.karoapi4j.model.extended.GameSeries;

public interface Eval
{
	public String doEvaluation() throws Exception; // TODO check meaningful return type
	
	public void prepare(GameSeries gs, int execution);
}
