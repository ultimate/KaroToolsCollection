package ultimate.karomuskel.ui.screens;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.exceptions.GameSeriesException;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.PlaceToRace;
import ultimate.karoapi4j.model.official.Generator;
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.ui.EnumNavigation;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.MainFrame;
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.UIUtil;
import ultimate.karomuskel.ui.Language.Label;
import ultimate.karomuskel.ui.components.BooleanModel;
import ultimate.karomuskel.ui.components.GenericListModel;
import ultimate.karomuskel.ui.components.PlaceToRaceRenderer;
import ultimate.karomuskel.ui.dialog.GeneratorDialog;

public class MapsScreen extends FilterScreen<PlaceToRace> implements ActionListener, MouseListener
{
	private static final long	serialVersionUID	= 1L;

	private JList<PlaceToRace>	allMapsLI;
	private JList<PlaceToRace>	selectedMapsLI;
	private JButton				addButton;
	private JButton				removeButton;

	private int					minSupportedPlayersPerMap;

	public MapsScreen(MainFrame gui, Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(gui, previous, karoAPICache, previousButton, nextButton, "screen.maps.header");
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
	public Message updateBeforeShow(GameSeries gameSeries, EnumNavigation direction)
	{
		Message message = null;

		int minSupportedPlayersPerMapTmp = GameSeriesManager.getMinSupportedPlayersPerMap(gameSeries);

		if(this.firstShow)
		{
			// initialize search
			this.addFilterComponent("screen.maps.filter.name", new JTextField(),
					(name, ptr) -> ptr.getName().toLowerCase().contains(((String) name).toLowerCase()));
			this.addFilterComponent("screen.maps.filter.players.min", new JSpinner(new SpinnerNumberModel(minSupportedPlayersPerMapTmp, minSupportedPlayersPerMapTmp, 999, 1)),
					(minPlayers, ptr) -> ptr.getPlayers() >= (Integer) minPlayers);
			this.addFilterComponent("screen.maps.filter.players.max", new JSpinner(new SpinnerNumberModel(999, minSupportedPlayersPerMapTmp, 999, 1)),
					(maxPlayers, ptr) -> ptr.getPlayers() <= (Integer) maxPlayers);
			this.addFilterComponent("screen.maps.filter.night", new JComboBox<Label<Boolean>>(new BooleanModel(null, "option.boolean.empty", 0)),
					(night, ptr) -> {
						@SuppressWarnings("unchecked")
						Boolean value = ((Label<Boolean>) night).getValue();
						if(value == null) return true;
						else return ptr.isNight() == value;						
					});

			// initialize content
			JPanel allMapsPanel = new JPanel();
			allMapsPanel.setLayout(new BorderLayout(5, 5));
			this.getContentPanel().add(allMapsPanel);

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridBagLayout());
			this.getContentPanel().add(buttonPanel);

			JPanel selectedMapsPanel = new JPanel();
			selectedMapsPanel.setLayout(new BorderLayout(5, 5));
			this.getContentPanel().add(selectedMapsPanel);

			this.allMapsLI = new JList<>();
			this.allMapsLI.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			this.allMapsLI.setFixedCellWidth(1500);
			this.allMapsLI.setCellRenderer(new PlaceToRaceRenderer(ptr -> {
				if(ptr instanceof Generator)
				{
					if(((Generator) ptr).getUniqueId() == 0)
						return Language.getString("screen.maps.addGenerator");
					else
						return Language.getString("screen.maps.editGenerator");
				}
				return null;
			}));
			this.allMapsLI.addMouseListener(this);
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
			this.selectedMapsLI.setCellRenderer(new PlaceToRaceRenderer(ptr -> {
				if(ptr instanceof Generator && ((Generator) ptr).getUniqueId() != 0)
					return Language.getString("screen.maps.editGenerator");
				return null;
			}));
			this.selectedMapsLI.addMouseListener(this);
			JScrollPane selectedMapsSP = new JScrollPane(this.selectedMapsLI, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			selectedMapsPanel.add(new JLabel(Language.getString("screen.maps.selectedmaps")), BorderLayout.NORTH);
			selectedMapsPanel.add(selectedMapsSP, BorderLayout.CENTER);

			java.util.Map<String, PlaceToRace> maps = this.karoAPICache.getPlacesToRaceByKey();
			maps.values().removeIf(map -> {
				return map.getPlayersMax() < minSupportedPlayersPerMapTmp;
			});

			// create a model for filtering
			GenericListModel<String, PlaceToRace> model = new GenericListModel<String, PlaceToRace>(PlaceToRace.class, maps);
			this.setModel(model);

			this.allMapsLI.setModel(model);
			this.selectedMapsLI.setModel(new GenericListModel<String, PlaceToRace>(PlaceToRace.class, new TreeMap<String, PlaceToRace>()));

			// preselect values from gameseries
			for(PlaceToRace map : gameSeries.getMaps())
				preselectMap(map);
		}

		if(!firstShow && minSupportedPlayersPerMapTmp != this.minSupportedPlayersPerMap)
		{
			if(minSupportedPlayersPerMapTmp > this.minSupportedPlayersPerMap)
			{
				logger.debug("removing all maps which do not have enough places anymore");

				// remove all maps which do not have enough places from allMapsLI
				GenericListModel<String, PlaceToRace> allModel = (GenericListModel<String, PlaceToRace>) this.allMapsLI.getModel();
				for(PlaceToRace map : allModel.getEntryArray())
				{
					if(map.getPlayersMax() < minSupportedPlayersPerMapTmp)
						allModel.removeElement(karoAPICache.getPlaceToRaceKey(map));
				}

				// remove all maps which do not have enough places from selectedMapsLI
				GenericListModel<String, PlaceToRace> selectedModel = (GenericListModel<String, PlaceToRace>) this.selectedMapsLI.getModel();
				for(PlaceToRace map : selectedModel.getEntryArray())
				{
					if(map.getPlayersMax() < minSupportedPlayersPerMapTmp)
						selectedModel.removeElement(karoAPICache.getPlaceToRaceKey(map));
				}

				message = new Message(Language.getString("screen.maps.minPlayers.increased"), JOptionPane.WARNING_MESSAGE);
			}
			else
			{
				logger.debug("adding new maps which now have enough places");

				// get all maps which didn't have enough places before
				java.util.Map<String, PlaceToRace> maps = this.karoAPICache.getPlacesToRaceByKey();
				maps.values().removeIf(map -> {
					return map.getPlayersMax() < minSupportedPlayersPerMapTmp || map.getPlayersMax() >= this.minSupportedPlayersPerMap;
				});

				// add these maps to the allMapLI
				GenericListModel<String, PlaceToRace> allModel = (GenericListModel<String, PlaceToRace>) this.allMapsLI.getModel();
				for(Entry<String, PlaceToRace> entry : maps.entrySet())
				{
					allModel.addElement(entry.getKey(), entry.getValue());
				}
				message = new Message(Language.getString("screen.maps.minPlayers.decreased"), JOptionPane.INFORMATION_MESSAGE);
			}
		}

		this.minSupportedPlayersPerMap = minSupportedPlayersPerMapTmp;
		this.firstShow = false;

		return message;
	}

	private void preselectMap(PlaceToRace map)
	{
		String key = karoAPICache.getPlaceToRaceKey(map);
		logger.debug("preselect place to race: " + key);
		// check map is present in list, if not, add first
		if(!((GenericListModel<String, PlaceToRace>) allMapsLI.getModel()).containsKey(key))
		{
			logger.warn("entry not present in list: " + key + " -> adding");
			((GenericListModel<String, PlaceToRace>) allMapsLI.getModel()).addElement(key, map);
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
				key = karoAPICache.getPlaceToRaceKey(m);
				if(m instanceof Generator && add && ((Generator) m).getUniqueId() == 0)
				{
					// create a copy to add to the list to the right
					m = ((Generator) m).copy();
					this.karoAPICache.cache((Generator) m);
					key = karoAPICache.getPlaceToRaceKey(m);
				}
				((GenericListModel<String, PlaceToRace>) remLI.getModel()).removeElement(key);
				((GenericListModel<String, PlaceToRace>) addLI.getModel()).addElement(key, m);
			}

			// make sure to shift selection after removing items so we don't end up with
			// index out of bounds on next action
			UIUtil.fixSelection(remLI);

			repaint();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void mouseClicked(MouseEvent e)
	{
		if(e.getSource() instanceof JList)
		{
			JList<PlaceToRace> list = (JList<PlaceToRace>) e.getSource();
			if(e.getClickCount() == 2)
			{
				// Double-click detected
				int index = list.locationToIndex(e.getPoint());
				PlaceToRace ptr = list.getModel().getElementAt(index);
				if(ptr instanceof Generator)
				{
					Generator g = (Generator) ptr;
					if(g.getUniqueId() != 0)
					{
						int result = GeneratorDialog.getInstance().showEdit(this, g);
						if(result == JOptionPane.OK_OPTION)
						{
							logger.debug("updating settings for generator " + g.getUniqueKey());
							g.getSettings().putAll(GeneratorDialog.getInstance().getSettings());
						}
					}
				}
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}
}
