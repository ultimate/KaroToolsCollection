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
		if(ptr instanceof Generator)
		{
			Generator g = (Generator) ptr;
			int result = GeneratorDialog.getInstance().showEdit(this, g);
			if(result == JOptionPane.OK_OPTION)
			{
				if(g.getUniqueId() == 0)
				{
					Generator original = g;
					// create a copy first
					g = original.copy();
					this.karoAPICache.cache(g);
					// add the copy to all comboboxes
					for(JComboBox<PlaceToRace> mapCB : this.mapCBList)
					{
						int originalIndex = ((DefaultComboBoxModel<PlaceToRace>) mapCB.getModel()).getIndexOf(original);
						((DefaultComboBoxModel<PlaceToRace>) mapCB.getModel()).insertElementAt(g, originalIndex + 1);
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

	protected void preselectMap(PlaceToRace map, int index)
	{
		String key = this.karoAPICache.getPlaceToRaceKey(map);
		logger.debug("preselect place to race: " + key);
		// check map is present in list, if not, add first
		if(((DefaultComboBoxModel<PlaceToRace>) this.mapCBList.get(index).getModel()).getIndexOf(map) == -1)
		{
			logger.warn("entry not present in list: " + key + " -> adding");
			for(int i = 0; i < this.mapCBList.size(); i++)
			{
				((DefaultComboBoxModel<PlaceToRace>) this.mapCBList.get(i).getModel()).addElement(map);
			}
		}
		// select map, then add
		((DefaultComboBoxModel<PlaceToRace>) this.mapCBList.get(index).getModel()).setSelectedItem(map);
		this.mapEditButtonList.get(index).setEnabled(map instanceof Generator);
	}
}
