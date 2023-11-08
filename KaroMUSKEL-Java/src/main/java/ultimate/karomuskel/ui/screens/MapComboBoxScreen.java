package ultimate.karomuskel.ui.screens;

import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.model.extended.PlaceToRace;
import ultimate.karoapi4j.model.official.Generator;
import ultimate.karomuskel.ui.MainFrame;
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.dialog.GeneratorDialog;

public abstract class MapComboBoxScreen extends Screen
{
	private static final long				serialVersionUID		= 1L;

	protected List<JComboBox<PlaceToRace>>	mapCBList;
	protected List<JButton>					mapEditButtonList;
	
	public MapComboBoxScreen(MainFrame gui, Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton, String headerKey)
	{
		super(gui, previous, karoAPICache, previousButton, nextButton, headerKey);
	}
	
	protected void handleGeneratorConfigurationEvent(int index)
	{
		if(this.mapCBList == null)
			throw new IllegalStateException("mapCBList not initialized");
		if(this.mapEditButtonList == null)
			throw new IllegalStateException("mapEditButtonList not initialized");
		if(index >= this.mapCBList.size())
			throw new IllegalStateException("mapCBList does not contain enough entries: index=" + index + ", size=" + this.mapCBList.size());
		if(index >= this.mapEditButtonList.size())
			throw new IllegalStateException("mapEditButtonList does not contain enough entries: index=" + index + ", size=" + this.mapEditButtonList.size());
		
		PlaceToRace ptr = (PlaceToRace) this.mapCBList.get(index).getSelectedItem();
		int selectedIndex = this.mapCBList.get(index).getSelectedIndex();
		if(ptr instanceof Generator)
		{
			Generator g = (Generator) ptr;
			int result = GeneratorDialog.getInstance().showEdit(this, g);
			if(result == JOptionPane.OK_OPTION)
			{
				if(g.getUniqueId() == 0)
				{
					// create a copy first
					g = g.copy();
					// add the copy to all comboboxes
					for(JComboBox<PlaceToRace> mapCB : this.mapCBList)
					{
						((DefaultComboBoxModel<PlaceToRace>) mapCB.getModel()).insertElementAt(g, selectedIndex + 1);
					}
					this.mapCBList.get(index).setSelectedItem(g);
				}
				// apply settings
				logger.debug("updating settings for generator " + g.getUniqueKey());
				g.getSettings().putAll(GeneratorDialog.getInstance().getSettings());
				// update all combobox (to show the updated generator)
				for(JComboBox<PlaceToRace> mapCB : this.mapCBList)
				{
					mapCB.repaint();
				}
			}
		}
	}
}
