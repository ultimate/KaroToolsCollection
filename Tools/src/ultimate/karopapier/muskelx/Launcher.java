package ultimate.karopapier.muskelx;

import java.io.File;
import java.util.Properties;

import com.sun.scenario.Settings;

import muskel2.util.Language;
import ultimate.karopapier.muskelx.core.karoaccess.KaropapierLoader;
import ultimate.karopapier.muskelx.gui.MainFrame;
import ultimate.karopapier.muskelx.model.Karopapier;
import ultimate.karopapier.muskelx.model.Profile;

public class Launcher
{
	private static KaroMUSKEL	muskel;
	private static MainFrame	gui;

	private static final String	SETTINGS_LOCATION	= ".settings";

	public static void main(String[] args)
	{
		System.out.println("------------------------------------------------------------------------");
		System.out.println("------------------------------------------------------------------------");
		System.out.println("                               KaroMUSKEL                               ");
		System.out.println("  Maschinelle-Ultimative-Spielserien-für-Karopapier-Erstellungs-Lösung  ");
		System.out.println("------------------------------------------------------------------------");
		System.out.println("------------------------------------------------------------------------");
		
		Object[] argsParsed = parseArgs(args);
		boolean debug = (Boolean) argsParsed[0];

		System.out.println("Initialisiere... " + (debug ? "DEBUG-MODUS aktiv!" : ""));

		Settings settings = null;
		Karopapier karopapier = null;
		Profile activeProfile = null;

		if(debug)
		{
			Properties p = new Properties();
			p.setProperty("language", Language.getDefault());
			p.setProperty("profile.1.karoUsername", "DEBUGGER");
			p.setProperty("profile.1.karoPassword", "DEBUGGER");
			p.setProperty("profile.1.wikiUsername", "DEBUGGER");
			p.setProperty("profile.1.wikiPassword", "DEBUGGER");
			settings = Settings.fromProperties(p);
			activeProfile = settings.getProfiles().get(0);
			karopapier = KaropapierLoader.debugInstance();
		}
		else
		{
			settings = Settings.load(new File(SETTINGS_LOCATION));
			if(settings.getProfiles().size() == 1)
			{
				activeProfile = settings.getProfiles().get(0);
			}
			else if(settings.getProfiles().size() == 0)
			{
				// create new Profile
			}
			else
			{
				// select active Profile
			}
			
			karopapier = KaropapierLoader.initiate(activeProfile);
			if(karopapier == null)
				exit();
		}		

		muskel = new KaroMUSKEL(karopapier, settings, activeProfile);

		gui = new MainFrame();
		gui.requestFocus();

		System.out.println("Initialisierung abgeschlossen!");
		System.out.println("-------------------------------------------------------------------------");
	}

	public static KaroMUSKEL getMuskel()
	{
		return muskel;
	}

	public static MainFrame getGui()
	{
		return gui;
	}

	private static Object[] parseArgs(String[] args)
	{
		boolean debug = false;
		if(args.length > 0)
		{
			for(String arg : args)
			{
				if(arg.equalsIgnoreCase("-d"))
					debug = true;
			}
		}
		return new Object[] { debug };
	}

	public static void exit()
	{
		System.out.println("-------------------------------------------------------------------------");
		System.out.println("Beende Programm...");

		if(muskel.getSettings() != null)
			Settings.store(new File(SETTINGS_LOCATION), muskel.getSettings());

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

	// TODO sortierfunktion spieler
	// TODO spieler namen tippen (ID vorne weg)
	// TODO suchfunktion
	// TODO zwischendurch speichern
	// TODO batch änderung aller spiele
	// TODO voreinstellung für alle spiele
	// TODO standard spiele serie: max spiele pro spieler
	// TODO ZZZ Placeholder ohne ZZZ-Text7
	// TODO Spieleserie auf jmd. anderes übertragen

}
