package ultimate.karopapier.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import muskel2.Main;
import muskel2.model.Game;
import muskel2.model.GameSeries;
import muskel2.model.Rules;
import muskel2.model.series.BalancedGameSeries;
import ultimate.karoapi4j.utils.PropertiesUtil;

public class GameSeriesEditorForCCC5
{	
	public static void main(String[] args) throws Exception
	{
		JFileChooser fileChooser = new JFileChooser();
		int result = fileChooser.showOpenDialog(null);
		if(result != JFileChooser.APPROVE_OPTION)
			return;
		File file = fileChooser.getSelectedFile();

		Main.main(new String[] { "-l=debug" });
		Main.getGui().setVisible(false);
		
		Properties gids = PropertiesUtil.loadProperties(new File(file.getParentFile().getAbsolutePath() + "/czzzcc5-gid.properties"));

		BalancedGameSeries gs = (BalancedGameSeries) loadGameseries(file);

		System.out.println("gameSeries loaded");

		result = JOptionPane.showConfirmDialog(null, "aendern?");
		if(result != JOptionPane.OK_OPTION)
			return;
		
		
		Field f = Rules.class.getDeclaredField("zzz");
		f.setAccessible(true);
		
		String key;
		int expectedGid;
		boolean changed = false;
		for(Game g: gs.getGames())
		{
			key = g.getName().substring("CraZZZy Crash Challenge 5 - Challenge ".length(), g.getName().indexOf(" ", "CraZZZy Crash Challenge 5 - Challenge ".length()+1));
			if(gids.containsKey(key))
				expectedGid = Integer.parseInt(gids.getProperty(key));
			else
				expectedGid = -1;
			System.out.println("[" + key + "] " + g.getName() + " created=" + g.isCreated() + " left=" + g.isLeft() + "\tgid=" + g.getId() + " expected=" + expectedGid);
			
			if(expectedGid != -1 && g.getId() < 0)
			{
				result = JOptionPane.showConfirmDialog(null, "Status ändern für " + g.getName());
				if(result == JOptionPane.OK_OPTION)
				{
					g.setCreated(true);
					g.setLeft(true);
					g.setId(expectedGid);
					changed = true;
					System.out.println("\t\t\t\t\t\t\t\t\t\t\t\t\t\tgid=" + g.getId() + " expected=" + expectedGid);
				}
			}
		}

		if(changed)
		{
			saveGameseries(new File(file.getPath() + ".edited"), gs);
			System.out.println("gameSeries saved");
		}
		else
		{
			System.out.println("nothing to save");
		}
	}

	protected static GameSeries loadGameseries(File file) throws IOException, ClassNotFoundException
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

	protected static void saveGameseries(File file, GameSeries gameSeries) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		ObjectOutputStream oos = new ObjectOutputStream(bos);

		oos.writeObject(gameSeries);

		oos.flush();
		bos.flush();
		fos.flush();

		oos.close();
		bos.close();
		fos.close();
	}
}
