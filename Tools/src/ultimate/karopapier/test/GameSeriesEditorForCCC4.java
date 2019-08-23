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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import muskel2.Main;
import muskel2.model.Game;
import muskel2.model.GameSeries;
import muskel2.model.Rules;
import muskel2.model.series.BalancedGameSeries;
import ultimate.karoapi4j.utils.PropertiesUtil;

public class GameSeriesEditorForCCC4
{
	private static class Change 
	{
		private int challenge;
		private int zzz;

		public Change(int challenge, int zzz)
		{
			super();
			this.challenge = challenge;
			this.zzz = zzz;
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		JFileChooser fileChooser = new JFileChooser();
		int result = fileChooser.showOpenDialog(null);
		if(result != JFileChooser.APPROVE_OPTION)
			return;
		File file = fileChooser.getSelectedFile();

		Main.main(new String[] { "-l=debug" });
		Main.getGui().setVisible(false);

		BalancedGameSeries gs = (BalancedGameSeries) loadGameseries(file);

		System.out.println("gameSeries loaded");

		result = JOptionPane.showConfirmDialog(null, "aendern?");
		if(result != JOptionPane.OK_OPTION)
			return;
		
		int option = 2;

		boolean changed = false;
		if(option == 1)
		{
			
			Change[] changes = new Change[] {
				new Change(6, 0),
				new Change(7, 1),
				new Change(10, 2),
				new Change(11, 1),
				new Change(12, 1),
				new Change(16, 0),
				new Change(18, 0),
				new Change(20, 1),
			};
			
			Field f = Rules.class.getDeclaredField("zzz");
			f.setAccessible(true);
			
			for(Change change: changes)
			{
				Rules r = gs.getRules(change.challenge-1);
				System.out.println("Challenge #" + change.challenge);
				System.out.println("current ZZZ = null ( " + r.getMinZzz() + " - " + r.getMaxZzz() + " )");
				System.out.println("Games: ");
				
				List<Game> gamesToChange = new ArrayList<Game>();
				for(Game g: gs.getGames())
				{
					if(g.getName().contains("Challenge " + change.challenge + ".") && !g.isCreated())
					{
						gamesToChange.add(g);
						System.out.println("- " + g.getName() + " @@@ ZZZ = " + g.getRules().getZzz() + " ( " + g.getRules().getMinZzz() + " - " + g.getRules().getMaxZzz() + " )");
					}
				}
	
				result = JOptionPane.showConfirmDialog(null, "ZZZ für angezeigte Spiele ändern? " + r.getMinZzz() + " ===> " + change.zzz);
				if(result == JOptionPane.OK_OPTION)
				{
					for(Game g: gamesToChange)
					{
						g.getRules().setZzz(change.zzz);
						f.set(g.getRules(), change.zzz);
						g.setName(g.getName().replace("ZZZ=" + r.getMinZzz(), "ZZZ=" + change.zzz));
						System.out.println("- " + g.getName() + " @@@ ZZZ = " + g.getRules().getZzz() + " ( " + g.getRules().getMinZzz() + " - " + g.getRules().getMaxZzz() + " )");
					}
					r.setZzz(change.zzz);
					changed = true;
				}
			}
		}
		else if(option == 2)
		{
			Properties gids = PropertiesUtil.loadProperties(new File(file.getParentFile().getAbsolutePath() + "/czzzcc4-gid.properties.63"));
			
			String cid;
			int gid;
			for(Game g: gs.getGames())
			{
				if(g.getName().contains("Challenge 10."))
				{
					cid = g.getName().substring("CraZZZy Crash Challenge 4 - Challenge ".length(), "CraZZZy Crash Challenge 4 - Challenge ".length()+5).trim();
					gid = Integer.parseInt(gids.getProperty(cid));
					System.out.println(cid + " : " + g.getId() + " => " + gid);
					g.setId(gid);
					changed = true;
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
