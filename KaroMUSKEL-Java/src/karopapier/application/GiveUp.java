package karopapier.application;

import java.io.IOException;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class GiveUp {
	
	private static String kick = "http://www.karopapier.de/kickplayer.php";
	private static String kickParam = "GID=%GID&UID=1476&sicher=1";
	
	public static void main(String[] args) {
		String user, password;
		
		String individual = "";
		if(args.length > 0) 
			individual = args[0];
		
		JLabel label = new JLabel("Bitte Benutzerdaten eingeben");
		JLabel tfL = new JLabel("Benutzer:");
		JTextField tf = new JTextField();
		JLabel pwL = new JLabel("Passwort:");
		JPasswordField pw = new JPasswordField();
		
		while(true) {
			int result = JOptionPane.showConfirmDialog(null, new Object[]{label, tfL, tf, pwL, pw}, "login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
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
				System.out.println("Steige aus...");				

				//ThreadQueue urlLoadQ = new ThreadQueue(50);
				
				String page = KaropapierLoader.readPage(new URL("http://www.karopapier.de/showgames.php"), "nurmeine=1&finished_limit=0" + individual);
				while(page.contains("kickplayer.php?GID=")) {
					int start = 0;
					int end = 0;
					String id;
					while(true) {
						start = page.indexOf("kickplayer.php?GID=", end);
						if(start == -1)
							break;
						start = start + "kickplayer.php?GID=".length();
						end = page.indexOf("&UID=", start);
						id = page.substring(start, end);
						
						URL url = new URL(kick);
						String parameter = kickParam.replace("%GID", id);
						
						KaropapierLoader.readPage(url, parameter);
						System.out.println(id + "   ");

//						URLLoaderThread th = new URLLoaderThread(url, parameter);
//						urlLoadQ.addThread(th);
					}					
//					try {
//						urlLoadQ.waitForFinisched();
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
					page = KaropapierLoader.readPage(new URL("http://www.karopapier.de/showgames.php"), "nurmeine=1&finished_limit=0" + individual);
				}
				

				break;
			} catch (IOException e) {
				System.out.println("Konnte Karopapier nicht ausseteigen...:\n" + e.toString());
			}
		}
	}
}
