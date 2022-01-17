package karopapier.gui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;


public class ProgressFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private String text = "Fortschritt beim Spiele Erstellen...";
	
	private JProgressBar progressBar;
	private JLabel label;

	public ProgressFrame() {
		this.setLayout(null);
		this.setSize(320, 150);
		this.setLocation(400,300);
		this.setVisible(true);
		this.setTitle("0% " + text);
		
		label = new JLabel(text);
		label.setBounds(50, 20, 200, 30);
		progressBar = new JProgressBar();
		progressBar.setBounds(50, 60, 200, 30);
		progressBar.setStringPainted(true);
		
		this.add(label, null);
		this.add(progressBar, null);
	}
	
	public void setProgress(int value) {
		this.progressBar.setValue(value);
		this.setTitle(value + "% " + text);
		if(value == 100) {
			long c = System.currentTimeMillis();
			while(System.currentTimeMillis() - c < 3000);
			this.setVisible(false);
			this.dispose();
		}
	}
}
