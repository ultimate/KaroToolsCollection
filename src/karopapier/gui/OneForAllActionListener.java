package karopapier.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JComboBox;

import karopapier.application.KaropapierLoader;
import karopapier.model.*;

public class OneForAllActionListener implements ActionListener {
	
	private Karopapier karopapier;
	private Object o;
	private Object o2;

	public OneForAllActionListener(Karopapier karopapier, Object o, Object o2) {
		this.karopapier = karopapier;
		this.o = o;
		this.o2 = o2;
	}

	public Karopapier getKaropapier() {
		return karopapier;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("mapsComboChanged")) {
			Map m = (Map)((JComboBox)o).getSelectedItem();
			m.setBestFirstMove(KaropapierLoader.loadMapsFirstMove(m.getId()));
			((MapsPanel)o2).displayMap(m);
		} else if (e.getActionCommand().equals("mapSaveClicked")) {
			((Map)((JComboBox)o).getSelectedItem()).setBestFirstMove(((MapsPanel)o2).getDisplayedPoint());
			try {
				KaropapierLoader.saveMapsFirstMove((Map)((JComboBox)o).getSelectedItem());
			} catch (IOException e1) {
				e1.printStackTrace();
			}		
		} else if(	e.getActionCommand().equals("addA") ||
					e.getActionCommand().equals("addB") ||
					e.getActionCommand().equals("addC") ||
					e.getActionCommand().equals("remA") ||
					e.getActionCommand().equals("remB") ||
					e.getActionCommand().equals("remC")	) {
			((ScriptPanel)o).updatePlayerDisplayment(e.getActionCommand());
		} else if(e.getActionCommand().equals("maxPlChanged")) {
			((ScriptPanel)o).updateRandomPlayerStatus();
		} else if(e.getActionCommand().equals("randomChanged")) {
			((ScriptPanel)o).updateRandomMapStatus();
		} else if(e.getActionCommand().equals("mapChanged")) {
			((ScriptPanel)o).updateMapDisplayment();
		} else if(e.getActionCommand().equals("create")) {
			((ScriptPanel)o).createGameSeries();
		}
	}
	
}
