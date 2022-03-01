package ultimate.karopapier.eval.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karopapier.eval.Eval;

public class DummyEval implements Eval
{

	@Override
	public String doEvaluation() throws Exception
	{
		String content = "Test fuer Automatische Auswertung " + Math.random() + " --~~~~";
		writeFile(new File("dummy.txt"), content);
		return content;
	}

	public static void writeFile(File target, String content) throws IOException
	{
		if(!target.getAbsoluteFile().getParentFile().exists())
			target.getAbsoluteFile().getParentFile().mkdirs();
		BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(target));
		try
		{
			byte[] bytes = content.getBytes();
			fos.write(bytes);
			fos.flush();
		}
		catch(IOException e)
		{
			throw e;
		}
		finally
		{
			if(fos != null)
				fos.close();
		}
	}

	@Override
	public void prepare(GameSeries gs, int execution)
	{
		// do nothing
	}
}
