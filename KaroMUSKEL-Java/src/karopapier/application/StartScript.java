package karopapier.application;

import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import karopapier.gui.KaroGUIFrame;
import karopapier.model.Karopapier;

public class StartScript {
	
	public static void main(String[] args) {
		if(args.length > 0 && args[0].equals("debug")) {
			System.out.println("-------------------------------------------------------------------------");
			System.out.println("-------------------------------------------------------------------------");
			System.out.println("      Das ultimative Karopapier-Spiel-Ersteller-Script von ultimate      ");
			System.out.println("-------------------------------------------------------------------------");
			System.out.println("-------------------------------------------------------------------------");
			System.out.println("                             DEBUG - MODE                                ");
			System.out.println("-------------------------------------------------------------------------");
			System.out.println("-------------------------------------------------------------------------");
			
			String title = "KaroScript GUI - DEBUG MODE";
			
			Karopapier karopapier = new Karopapier();
			KaroGUIFrame gui = null;
			
			System.out.println("Initialisiere Karopapier...");
			gui = new KaroGUIFrame(title, karopapier);
			gui.requestFocus();
			
			System.out.println("Initialisierung abgeschlossen!");
			System.out.println("-------------------------------------------------------------------------");
			
			try {
				synchronized (gui) {
					gui.wait();				
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			System.out.println("-------------------------------------------------------------------------");
			System.out.println("Beende Programm...");
			
			gui.setVisible(false);
			gui.dispose();	
			
			System.out.println("Programm beendet!");
			System.out.println("-------------------------------------------------------------------------");
			System.out.println("-------------------------------------------------------------------------");
			
			System.exit(0);
		} else {
			System.out.println("-------------------------------------------------------------------------");
			System.out.println("-------------------------------------------------------------------------");
			System.out.println("      Das ultimative Karopapier-Spiel-Ersteller-Script von ultimate      ");
			System.out.println("-------------------------------------------------------------------------");
			System.out.println("-------------------------------------------------------------------------");
			
			String user, password;
			
			String title = "KaroScript GUI";
			String logintitle = "Anmelden...";
	
			Karopapier karopapier = null;
			KaroGUIFrame gui = null;
			
			JLabel label = new JLabel("Bitte Benutzerdaten eingeben");
			JLabel tfL = new JLabel("Benutzer:");
			JTextField tf = new JTextField();
			JLabel pwL = new JLabel("Passwort:");
			JPasswordField pw = new JPasswordField();
			
			while(true) {
				int result = JOptionPane.showConfirmDialog(gui, new Object[]{label, tfL, tf, pwL, pw}, logintitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
					System.out.println("Anmeldung abgebrochen");
					return;
				}
				
				user = tf.getText();
				password = new String(pw.getPassword());
				System.out.print("Melde user an: \"" + user + "\" ... ");
				
				try {
					if(!KaropapierLoader.login(user, password)) {
						System.out.println(" Fehlgeschlagen!");
						continue;
					}
					System.out.println(" Erfolgreich!");
					System.out.println("-------------------------------------------------------------------------");
					System.out.println("Initialisiere Karopapier...");
					karopapier = KaropapierLoader.initiateKaropapier();
					break;
				} catch (IOException e) {
					System.out.println("Konnte Karopapier nicht initialisieren...:\n" + e.toString());
				}
			}
			gui = new KaroGUIFrame(title, karopapier);
			gui.requestFocus();
			
			System.out.println("Initialisierung abgeschlossen!");
			System.out.println("-------------------------------------------------------------------------");
			
			try {
				synchronized (gui) {
					gui.wait();				
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			System.out.println("-------------------------------------------------------------------------");
			System.out.println("Beende Programm...");
			
			gui.setVisible(false);
			gui.dispose();	
			
			System.out.println("Programm beendet!");
			System.out.println("-------------------------------------------------------------------------");
			System.out.println("-------------------------------------------------------------------------");
			
			System.exit(0);
		}
	}

}
