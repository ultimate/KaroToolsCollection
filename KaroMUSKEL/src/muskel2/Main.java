package muskel2;

import java.awt.Color;
import java.io.IOException;
import java.util.TreeMap;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import muskel2.core.karoaccess.GameCreator;
import muskel2.core.karoaccess.KaropapierLoader;
import muskel2.gui.MainFrame;
import muskel2.model.Karopapier;
import muskel2.model.Map;
import muskel2.model.Player;
import muskel2.util.Language;
import muskel2.util.RequestLogger;

public class Main
{
	private static MainFrame	gui;

	private static Karopapier	karopapier;

	public static void main(String[] args)
	{
		System.out.println("------------------------------------------------------------------------");
		System.out.println("------------------------------------------------------------------------");
		System.out.println("                               KaroMUSKEL                               ");
		System.out.println("  Maschinelle-Ultimative-Spielserien-f�r-Karopapier-Erstellungs-L�sung  ");
		System.out.println("------------------------------------------------------------------------");
		System.out.println("------------------------------------------------------------------------");

		karopapier = null;

		boolean debug = false;
		boolean unlock = false;

		String language = Language.getDefault();
		if(args.length > 0)
		{
			for(String arg : args)
			{
				if(arg.equalsIgnoreCase("-d"))
					debug = true;
				else if(arg.toLowerCase().startsWith("-l="))
					language = arg.substring(3);
				else if(arg.toLowerCase().startsWith("-t="))
				{
					String maxThreadsS = null;
					try
					{
						maxThreadsS = arg.substring(3);
						int maxThreads = Integer.parseInt(maxThreadsS);
						if(maxThreads > 0)
						{
							System.out.println("Setze GameCreator.MAX_LOAD_THREADS = " + maxThreads);
							GameCreator.MAX_LOAD_THREADS = maxThreads;
						}
						else
						{
							System.out.println("Ung�ltiger Wert f�r GameCreator.MAX_LOAD_THREADS: " + maxThreads);
						}
					}
					catch(NumberFormatException e)
					{
						System.out.println("Ung�ltiger Wert f�r GameCreator.MAX_LOAD_THREADS: " + maxThreadsS);
					}
				}
				else if(arg.toLowerCase().equalsIgnoreCase("-unlock"))
					unlock = true;
			}
		}

		Language.load(language);

		if(unlock)
		{
			String userToUnlock = JOptionPane.showInputDialog(null, Language.getString("unlock.message"), Language.getString("unlock.title"),
					JOptionPane.QUESTION_MESSAGE);

			System.out.println("Benutzer zum freischalten: " + userToUnlock);
			if(userToUnlock != null && !KaropapierLoader.checkUnlockFile(userToUnlock))
			{
				try
				{
					KaropapierLoader.createUnlockFile(userToUnlock);
					System.out.println("Benutzer freigeschaltet!");
				}
				catch(IOException e)
				{
					System.out.println("Konnte benutzer nicht freischalten:");
					e.printStackTrace();
				}
			}
			exit();
		}
		else if(debug)
		{
			System.out.println("                              DEBUG - MODE                              ");
			System.out.println("------------------------------------------------------------------------");
			System.out.println("------------------------------------------------------------------------");

			TreeMap<Integer, Map> maps = new TreeMap<Integer, Map>();
			for(int i = 0; i < 100; i++)
			{
				maps.put(i, new Map(i, "map" + i, "by anybody" + i, false, i % 20 + 2, null));
			}
			TreeMap<String, Player> players = new TreeMap<String, Player>();
			for(int i = 0; i < 100; i++)
			{
				players.put("p" + i, new Player(i, "p" + i, true, true, i, 100 - i, 0, 0, Color.black, 0, null));
			}

			karopapier = new Karopapier(maps, players, "p0", false);
			karopapier.setInDebugMode(true);

			System.out.println("Initialisiere ..");
			gui = new MainFrame("mainframe.debugtitle", karopapier);
			gui.requestFocus();
		}
		else
		{
			String user, password;

			String logintitle = Language.getString("login.title");

			final JLabel label = new JLabel(Language.getString("login.description"));
			final JLabel tfL = new JLabel(Language.getString("login.username"));
			final JTextField tf = new JTextField();
			final JLabel pwL = new JLabel(Language.getString("login.password"));
			final JPasswordField pw = new JPasswordField();

			// request Focus on username-TF when dialog is shown
			tfL.addAncestorListener(new AncestorListener() {
				@Override
				public void ancestorAdded(AncestorEvent arg0)
				{
					tf.requestFocusInWindow();
				}

				@Override
				public void ancestorMoved(AncestorEvent arg0)
				{
				}

				@Override
				public void ancestorRemoved(AncestorEvent arg0)
				{
				}
			});

			while(true)
			{
				int result = JOptionPane.showConfirmDialog(gui, new Object[] { label, tfL, tf, pwL, pw }, logintitle, JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION)
				{
					System.out.println("Anmeldung abgebrochen");
					return;
				}

				user = tf.getText();
				password = new String(pw.getPassword());
				System.out.print("Melde user an: \"" + user + "\" ... ");

				try
				{
					if(!KaropapierLoader.login(user, password))
					{
						System.out.println(" Fehlgeschlagen!");
						continue;
					}
					System.out.println(" Erfolgreich!");
					System.out.println("-------------------------------------------------------------------------");
					System.out.println("Initialisiere...");
					karopapier = KaropapierLoader.initiateKaropapier();
					break;
				}
				catch(IOException e)
				{
					System.out.println("Konnte Karopapier nicht initialisieren...:\n" + e.toString());
				}
			}
			gui = new MainFrame("mainframe.title", karopapier);
			gui.requestFocus();
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run()
			{
				cleanUpLoggers();
			}
		});

		System.out.println("Initialisierung abgeschlossen!");
		System.out.println("-------------------------------------------------------------------------");
	}

	public static MainFrame getGui()
	{
		return gui;
	}

	public static Karopapier getKaropapier()
	{
		return karopapier;
	}

	public static void setKaropapier(Karopapier karopapier)
	{
		Main.karopapier = karopapier;
	}
	
	public static void cleanUpLoggers()
	{
		System.out.println("Beende Logging...");
		RequestLogger.cleanUp();
		System.out.println("Logging beendet!");
	}

	public static void exit()
	{
		System.out.println("-------------------------------------------------------------------------");
		System.out.println("Beende Programm...");

		// TODO store settings

		if(gui != null)
		{
			gui.setVisible(false);
			gui.dispose();
		}		

		System.out.println("Programm beendet!");
		System.out.println("-------------------------------------------------------------------------");
		System.out.println("-------------------------------------------------------------------------");

		System.exit(0);
	}
}
