package ultimate.karomuskel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.swing.JButton;

import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karomuskel.ui.Screen;

public abstract class GameSeriesManager
{
	/**
	 * Prevent instantiation
	 */
	private GameSeriesManager()
	{

	}

	public static GameSeries create(String type)
	{
		// TODO
		return null;
	}

	public static GameSeries load(File file) throws IOException, ClassNotFoundException, ClassCastException
	{
		// TODO check for json vs. old
		boolean v2 = true;
		if(v2)
		{
			return convert(loadV2(file));
		}
		else
		{
			// setLoaded(true);
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	public static muskel2.model.GameSeries loadV2(File file) throws IOException, ClassNotFoundException, ClassCastException
	{
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ObjectInputStream ois = new ObjectInputStream(bis);

		muskel2.model.GameSeries gs2 = (muskel2.model.GameSeries) ois.readObject();
		gs2.setLoaded(true);

		ois.close();
		bis.close();
		fis.close();

		return gs2;
	}

	@SuppressWarnings("deprecation")
	public static GameSeries convert(muskel2.model.GameSeries gs2)
	{
		// TODO
		return null;
	}

	public static boolean store(GameSeries gameSeries, File file)
	{
		// TODO
		return false;
	}

	public static Screen initScreens(GameSeries gameSeries, KaroAPICache karoAPICache, Screen startScreen, JButton previousButton, JButton nextButton, boolean loaded)
	{
		// TODO
		return null;
	}
}
