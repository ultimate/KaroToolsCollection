package karopapier.gui;

import java.awt.Point;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import karopapier.model.Karopapier;
import karopapier.model.Map;

public class MapsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JComboBox mapsCombo;	
	private JLabel titleL;
	private JLabel mapNameL, mapIdL;
	private JTextField mapFirstMoveXTF, mapFirstMoveYTF;
	private JButton mapSaveB;	

	public MapsPanel(Karopapier karopapier) {
		super();
		
		Map[] mapsA = new Map[karopapier.getMaps().size()];
		int i = 0;
		for(Integer id: karopapier.getMaps().keySet()) {
			Map m = karopapier.getMaps().get(id);
			mapsA[i++] = m;
		}
		
		titleL = new JLabel("Kartenverwaltung");
		titleL.setBounds(10, 10, 110, 25);
		
		mapsCombo = new JComboBox(mapsA);
		mapsCombo.setActionCommand("mapsComboChanged");
		mapsCombo.addActionListener(new OneForAllActionListener(karopapier, mapsCombo, this));
		mapsCombo.setBounds(130, 10, 360, 25);
		
		mapIdL = new JLabel();
		mapIdL.setBounds(510, 10, 30, 25);
		
		mapNameL = new JLabel();
		mapNameL.setBounds(550, 10, 210, 25);

		mapFirstMoveXTF = new JTextField();
		mapFirstMoveXTF.setBounds(770, 10, 50, 25);
		
		mapFirstMoveYTF = new JTextField();
		mapFirstMoveYTF.setBounds(830, 10, 50, 25);
		
		mapSaveB = new JButton("Save Map");
		mapSaveB.setActionCommand("mapSaveClicked");
		mapSaveB.addActionListener(new OneForAllActionListener(karopapier, mapsCombo, this));
		mapSaveB.setBounds(890, 10, 100, 25);
		
		this.setLayout(null);
		
		this.add(titleL, null);
		this.add(mapsCombo, null);
		this.add(mapNameL, null);
		this.add(mapIdL, null);
		this.add(mapFirstMoveXTF, null);
		this.add(mapFirstMoveYTF, null);
		this.add(mapSaveB, null);
		
		if (mapsA.length > 0)
			this.displayMap(mapsA[0]);
	}
	
	public void displayMap(Map m) {
		mapIdL.setText("" + m.getId());
		mapNameL.setText(m.getName());
		if(m.getBestFirstMove() != null) {
			mapFirstMoveXTF.setText((int)m.getBestFirstMove().getX() + "");
			mapFirstMoveYTF.setText((int)m.getBestFirstMove().getY() + "");
		} else {
			mapFirstMoveXTF.setText("");
			mapFirstMoveYTF.setText("");
		}
	}
	
	public Point getDisplayedPoint() {
		try {
			int x = Integer.parseInt(mapFirstMoveXTF.getText());
			int y = Integer.parseInt(mapFirstMoveYTF.getText());
			return new Point(x,y);
		} catch(NumberFormatException e) {
			return null;
		}
	}
}
