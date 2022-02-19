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
import ultimate.karoapi4j.model.official.Map;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.components.GenericListModel;
import ultimate.karomuskel.ui.components.MapRenderer;

public class MapsScreen extends Screen implements ActionListener
{
	private static final long		serialVersionUID	= 1L;

	private JList<Map>				allMapsLI;
	private JList<Map>				selectedMapsLI;
	private JButton					addButton;
	private JButton					removeButton;

	private int						minSupportedPlayersPerMap;

	private TreeMap<Integer, Map>	maps;

	public MapsScreen(Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(previous, karoAPICache, previousButton, nextButton, "screen.maps.header", "screen.maps.next");

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

	@SuppressWarnings("unchecked")
	@Override
	public GameSeries applySettings(GameSeries gameSeries) throws GameSeriesException
	{
		Object[] maps = ((GenericListModel<Integer, Map>) selectedMapsLI.getModel()).getEntryArray();
		if(maps.length == 0)
			throw new GameSeriesException("screen.maps.nomap");
		gameSeries.getMaps().clear();
		for(Object map : maps)
		{
			gameSeries.getMaps().add((Map) map);
		}
		return gameSeries;
	}

	@Override
	public void updateBeforeShow(GameSeries gameSeries)
	{
		int minSupportedPlayersPerMapTmp = gameSeries.getMinSupportedPlayersPerMap();

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
			this.allMapsLI.setCellRenderer(new MapRenderer());
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
			this.selectedMapsLI.setCellRenderer(new MapRenderer());
			JScrollPane selectedMapsSP = new JScrollPane(this.selectedMapsLI, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			selectedMapsPanel.add(new JLabel(Language.getString("screen.maps.selectedmaps")), BorderLayout.NORTH);
			selectedMapsPanel.add(selectedMapsSP, BorderLayout.CENTER);

			this.maps = new TreeMap<Integer, Map>(karoAPICache.getMapsById());
			this.maps.values().removeIf(map -> { return map.getPlayers() < this.minSupportedPlayersPerMap; });

			this.allMapsLI.setModel(new GenericListModel<Integer, Map>(Map.class, this.maps));
			this.selectedMapsLI.setModel(new GenericListModel<Integer, Map>(Map.class, new TreeMap<Integer, Map>()));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().startsWith("add") || e.getActionCommand().startsWith("rem"))
		{
			boolean add = e.getActionCommand().startsWith("add");

			JList<Map> addLI, remLI;
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
			List<Map> maps = remLI.getSelectedValuesList();
			Integer key;
			for(Map m : maps)
			{
				key = m.getId();
				((GenericListModel<Integer, Map>) remLI.getModel()).removeElement(key);
				((GenericListModel<Integer, Map>) addLI.getModel()).addElement(key, m);
			}
			repaint();
		}
	}
}
