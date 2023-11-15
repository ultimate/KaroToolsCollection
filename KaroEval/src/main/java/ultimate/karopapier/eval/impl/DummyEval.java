package ultimate.karopapier.eval.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karopapier.eval.Eval;

public class DummyEval extends Eval<GameSeries>
{
	@Override
	public List<File> evaluate()
	{
		logger.info("dummy eval...");
		return new ArrayList<>();
	}
}
