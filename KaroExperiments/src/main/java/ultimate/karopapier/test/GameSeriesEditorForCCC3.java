package ultimate.karopapier.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import muskel2.core.karoaccess.GameCreator;
import muskel2.core.karoaccess.KaropapierLoader;
import muskel2.model.Game;
import muskel2.model.GameSeries;
import muskel2.model.Player;
import ultimate.karomuskel.Main;

public class GameSeriesEditorForCCC3
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

		GameSeries gs = loadGameseries(file);

		System.out.println("gameSeries loaded");

		result = JOptionPane.showConfirmDialog(null, "aendern?");
		if(result != JOptionPane.OK_OPTION)
			return;

		Player newPlayer = null;
		Player oldPlayer = null;
		for(Player p : Main.getKaropapier().getPlayers().values())
		{
			if(p.getName().equalsIgnoreCase("sash1501"))
			{
				oldPlayer = p;
				System.out.println("old player found: " + p.getName() + " - ID=" + p.getId());
				continue;
			}
			else if(p.getName().equalsIgnoreCase("MM"))
			{
				newPlayer = p;
				System.out.println("new player found: " + p.getName() + " - ID=" + p.getId());
				continue;
			}
		}

		if(newPlayer == null || oldPlayer == null)
		{
			System.out.println("not all players found! exiting");
			return;
		}

		List<Game> replacementGames = new ArrayList<Game>();

		int replacements = 0;
		for(Game g : gs.getGames())
		{
			if(g.getPlayers().contains(oldPlayer))
			{
				g.getPlayers().remove(oldPlayer);
				g.getPlayers().add(newPlayer);
				System.out.println("replaced " + oldPlayer.getName() + " with " + newPlayer.getName() + " in game: " + g.getName());
				replacements++;

				if(g.isCreated())
				{
					g.setCreated(false);
					g.setLeft(false);
					g.setId(-1);
					g.setName(g.getName() + " - Ersatzspiel");

					replacementGames.add(g);
				}
			}
		}

		System.out.println("replacements: " + replacements);

		if(replacementGames.size() != 0)
		{
			result = JOptionPane.showConfirmDialog(null, "Fuer bereits erstellte Spiele Ersatzspiele erstellen? " + replacementGames.size());
			if(result == JOptionPane.OK_OPTION)
			{
				GameCreator gc = new GameCreator(Main.getKaropapier(), null);
				gc.createGames(replacementGames);
				gc.waitForFinished();
				System.out.println("OK");

				for(Game g : replacementGames)
					g.setCreated(true);

				System.out.println("finding game ids... ");
				KaropapierLoader.findIds(replacementGames);
				System.out.println("OK");
			}
		}

		saveGameseries(new File(file.getPath() + ".edited"), gs);

		System.out.println("gameSeries saved");
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
