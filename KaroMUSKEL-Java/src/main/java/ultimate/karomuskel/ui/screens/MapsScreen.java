package ultimate.karomuskel.ui.screens;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.exceptions.GameSeriesException;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.PlaceToRace;
import ultimate.karoapi4j.model.official.Generator;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.utils.StringUtil;
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.ui.EnumNavigation;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.MainFrame;
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.components.GenericListModel;
import ultimate.karomuskel.ui.components.PlaceToRaceRenderer;

public class MapsScreen extends Screen implements ActionListener
{
	private static final long				serialVersionUID	= 1L;

	private JList<PlaceToRace>				allMapsLI;
	private JList<PlaceToRace>				selectedMapsLI;
	private JButton							addButton;
	private JButton							removeButton;

	private int								minSupportedPlayersPerMap;

	private TreeMap<String, PlaceToRace>	maps;

	public MapsScreen(MainFrame gui, Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(gui, previous, karoAPICache, previousButton, nextButton, "screen.maps.header");

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

	@Override
	public String getNextKey()
	{
		return "screen.maps.next";
	}

	@Override
	public GameSeries applySettings(GameSeries gameSeries, EnumNavigation direction) throws GameSeriesException
	{
		PlaceToRace[] maps = ((GenericListModel<String, PlaceToRace>) selectedMapsLI.getModel()).getEntryArray();
		if(maps.length == 0)
			throw new GameSeriesException("screen.maps.nomap");
		gameSeries.getMaps().clear();
		for(PlaceToRace map : maps)
		{
			gameSeries.getMaps().add(map);
		}
		return gameSeries;
	}

	@Override
	public void updateBeforeShow(GameSeries gameSeries, EnumNavigation direction)
	{
		int minSupportedPlayersPerMapTmp = GameSeriesManager.getMinSupportedPlayersPerMap(gameSeries);

		if(this.minSupportedPlayersPerMap != minSupportedPlayersPerMapTmp)
		{
			this.minSupportedPlayersPerMap = minSupportedPlayersPerMapTmp;

			this.removeAll();

			JPanel allMapsPanel = new JPanel();
			allMapsPanel.setLayout(new BorderLayout(5, 5));
			this.add(allMapsPanel);

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridBagLayout());
			this.add(buttonPanel);

			JPanel selectedMapsPanel = new JPanel();
			selectedMapsPanel.setLayout(new BorderLayout(5, 5));
			this.add(selectedMapsPanel);

			this.allMapsLI = new JList<>();
			this.allMapsLI.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			this.allMapsLI.setFixedCellWidth(1500);
			this.allMapsLI.setCellRenderer(new PlaceToRaceRenderer());
			JScrollPane allMapsSP = new JScrollPane(this.allMapsLI, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			allMapsPanel.add(new JLabel(Language.getString("screen.maps.allmaps")), BorderLayout.NORTH);
			allMapsPanel.add(allMapsSP, BorderLayout.CENTER);

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.fill = GridBagConstraints.HORIZONTAL;

			this.addButton = new JButton(Language.getString("option.add"));
			this.addButton.setActionCommand("add");
			this.addButton.addActionListener(this);
			gbc.gridx = 0;
			gbc.gridy = 0;
			buttonPanel.add(addButton, gbc);

			this.removeButton = new JButton(Language.getString("option.remove"));
			this.removeButton.setActionCommand("rem");
			this.removeButton.addActionListener(this);
			gbc.gridx = 0;
			gbc.gridy = 1;
			buttonPanel.add(removeButton, gbc);

			this.selectedMapsLI = new JList<>();
			this.selectedMapsLI.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			this.selectedMapsLI.setFixedCellWidth(1500);
			this.selectedMapsLI.setCellRenderer(new PlaceToRaceRenderer());
			JScrollPane selectedMapsSP = new JScrollPane(this.selectedMapsLI, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			selectedMapsPanel.add(new JLabel(Language.getString("screen.maps.selectedmaps")), BorderLayout.NORTH);
			selectedMapsPanel.add(selectedMapsSP, BorderLayout.CENTER);

			this.maps = new TreeMap<String, PlaceToRace>();
			for(Generator g : karoAPICache.getGenerators())
				this.maps.put(getKey(g), g);
			for(Map m : karoAPICache.getMaps())
				this.maps.put(getKey(m), m);
			this.maps.values().removeIf(map -> {
				return map.getPlayersMax() < this.minSupportedPlayersPerMap;
			});

			this.allMapsLI.setModel(new GenericListModel<String, PlaceToRace>(PlaceToRace.class, this.maps));
			this.selectedMapsLI.setModel(new GenericListModel<String, PlaceToRace>(PlaceToRace.class, new TreeMap<String, PlaceToRace>()));
		}

		if(this.firstShow)
		{
			// preselect values from gameseries
			for(PlaceToRace map : gameSeries.getMaps())
				preselectMap(map);
		}

		this.firstShow = false;
	}

	private String getKey(PlaceToRace ptr)
	{
		logger.debug(ptr);
		if(ptr instanceof Map)
			return "map#" + StringUtil.toString(((Map) ptr).getId(), 5);
		else if(ptr instanceof Generator)
			return "generator#" + ((Generator) ptr).getUniqueKey();
		return null;
	}

	private void preselectMap(PlaceToRace map)
	{
		String key = getKey(map);
		logger.debug("preselect place to race: " + key);
		// check map is present in list, if not, add first
		if(!((GenericListModel<String, PlaceToRace>) allMapsLI.getModel()).containsKey(key))
		{
			logger.warn("entry not present in list: " + key + " -> adding");
			((GenericListModel<String, PlaceToRace>) allMapsLI.getModel()).addElement(key, map);
			allMapsLI.setModel(allMapsLI.getModel());
		}
		// select map, then add
		allMapsLI.setSelectedValue(map, false);
		actionPerformed(new ActionEvent(this, 0, "add"));
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().startsWith("add") || e.getActionCommand().startsWith("rem"))
		{
			boolean add = e.getActionCommand().startsWith("add");

			JList<PlaceToRace> addLI, remLI;
			if(add)
			{
				addLI = selectedMapsLI;
				remLI = allMapsLI;
			}
			else
			{
				addLI = allMapsLI;
				remLI = selectedMapsLI;
			}
			List<PlaceToRace> maps = remLI.getSelectedValuesList();
			String key;
			for(PlaceToRace m : maps)
			{
				key = getKey(m);
				if(m instanceof Generator && add)
				{
					// create a copy to add to the list to the right
					m = ((Generator) m).copy();
					key = getKey(m);
				}
				((GenericListModel<String, PlaceToRace>) remLI.getModel()).removeElement(key);
				((GenericListModel<String, PlaceToRace>) addLI.getModel()).addElement(key, m);
			}
			repaint();
		}
	}
}
