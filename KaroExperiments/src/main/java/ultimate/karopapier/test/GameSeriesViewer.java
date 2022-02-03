package ultimate.karopapier.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.swing.JFileChooser;

import muskel2.model.GameSeries;
import ultimate.karomuskel.Launcher;

public class GameSeriesViewer
{
	public static void main(String[] args) throws Exception
	{
		JFileChooser fileChooser = new JFileChooser();
		int result = fileChooser.showOpenDialog(null);
		if(result != JFileChooser.APPROVE_OPTION)
			return;
		File file = fileChooser.getSelectedFile();

		Launcher.main(new String[] { "-l=debug" });
		Launcher.getGui().setVisible(false);

		GameSeries gs = loadGameseries(file);
		
		System.out.println(gs);
		
		System.out.println("gameSeries loaded");
	}

	private static GameSeries loadGameseries(File file) throws IOException, ClassNotFoundException
	{
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ObjectInputStream ois = new ObjectInputStream(bis);

		GameSeries gameSeries = (GameSeries) ois.readObject();

		ois.close();
		bis.close();
		fis.close();

		return gameSeries;
	}
}
