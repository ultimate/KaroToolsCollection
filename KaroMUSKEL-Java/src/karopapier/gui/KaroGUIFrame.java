package karopapier.gui;

import java.awt.Color;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;

import karopapier.model.Karopapier;

public class KaroGUIFrame extends JFrame implements WindowListener{
	private static final long serialVersionUID = 1L;
	
	private JPanel scriptPanel, mapsPanel;
	
	public KaroGUIFrame(String title, Karopapier karopapier) {
		super(title);

		this.setLocation(0,0);
		this.setSize(1020,775);
		this.setExtendedState(MAXIMIZED_BOTH);
		
		Border borderForAll = BorderFactory.createLineBorder(Color.black);
		
		scriptPanel = new ScriptPanel(karopapier);
		scriptPanel.setBorder(borderForAll);
		scriptPanel.setBounds(1, 1, this.getWidth()-20, this.getHeight()-87);
		
		mapsPanel = new MapsPanel(karopapier);
		mapsPanel.setBorder(borderForAll);
		mapsPanel.setBounds(1, this.getHeight()-85, this.getWidth()-20, 45);
		
		this.getContentPane().setLayout(null);
		
		this.getContentPane().add(scriptPanel, null);
		this.getContentPane().add(mapsPanel, null);

		this.setSize(1020,740);
		this.setVisible(true);
		this.repaint();
		
		this.addWindowListener(this);
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
		synchronized (this) {
			this.notifyAll();			
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		synchronized (this) {
			this.notifyAll();			
		}
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}
	
	

}
