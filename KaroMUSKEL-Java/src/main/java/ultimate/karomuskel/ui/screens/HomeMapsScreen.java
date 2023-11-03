package ultimate.karomuskel.ui.screens;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.exceptions.GameSeriesException;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.PlaceToRace;
import ultimate.karoapi4j.model.official.Generator;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.utils.StringUtil;
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.ui.EnumNavigation;
import ultimate.karomuskel.ui.MainFrame;
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.components.PlaceToRaceRenderer;

public class HomeMapsScreen extends Screen
{
	private static final long				serialVersionUID	= 1L;

	private List<JLabel>					teamNameLabelList;
	private List<JComboBox<PlaceToRace>>	mapCBList;

	private int								numberOfTeams;
	private int								minSupportedPlayersPerMap;

	private TreeMap<String, PlaceToRace>	maps;

	public HomeMapsScreen(MainFrame gui, Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(gui, previous, karoAPICache, previousButton, nextButton, "screen.homemaps.header");
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

	@Override
	public String getNextKey()
	{
		if(this.next.isSkip() || this.next instanceof SummaryScreen)
			return "screen.homemaps.nextskip";
		else
			return "screen.homemaps.next";
	}

	@Override
	public GameSeries applySettings(GameSeries gameSeries, EnumNavigation direction) throws GameSeriesException
	{
		if(GameSeriesManager.isTeamBased(gameSeries) || gameSeries.getType() == EnumGameSeriesType.KLC)
		{
			PlaceToRace homeMap;
			for(int i = 0; i < this.numberOfTeams; i++)
			{
				homeMap = (PlaceToRace) this.mapCBList.get(i).getSelectedItem();
				gameSeries.getTeams().get(i).setHomeMap(homeMap);
			}
		}
		return gameSeries;
	}

	@Override
	public void updateBeforeShow(GameSeries gameSeries, EnumNavigation direction)
	{
		int maxTeams = 0;
		int numberOfTeamsTmp = 0;
		int minSupportedPlayersPerMapTmp = GameSeriesManager.getMinSupportedPlayersPerMap(gameSeries);

		if(GameSeriesManager.isTeamBased(gameSeries))
		{
			numberOfTeamsTmp = (int) gameSeries.get(GameSeries.NUMBER_OF_TEAMS);
			maxTeams = GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_MAX_TEAMS);
		}
		else if(gameSeries.getType() == EnumGameSeriesType.KLC)
		{
			numberOfTeamsTmp = (int) gameSeries.get(GameSeries.CURRENT_ROUND);
			maxTeams = numberOfTeamsTmp;
		}

		if(this.firstShow)
		{
			this.numberOfTeams = numberOfTeamsTmp;
			this.minSupportedPlayersPerMap = minSupportedPlayersPerMapTmp;

			this.teamNameLabelList = new LinkedList<JLabel>();
			this.mapCBList = new LinkedList<JComboBox<PlaceToRace>>();
			this.removeAll();

			JPanel contentPanel = new JPanel();
			JScrollPane contentSP = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			this.add(contentSP);

			GridBagLayout layout = new GridBagLayout();
			contentPanel.setLayout(layout);

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.fill = GridBagConstraints.HORIZONTAL;

			JLabel teamLabel;
			JComboBox<PlaceToRace> mapCB;

			for(int i = 0; i < maxTeams; i++)
			{
				gbc.gridy = i;

				teamLabel = new JLabel();
				gbc.gridx = 0;
				contentPanel.add(teamLabel, gbc);

				mapCB = new JComboBox<>();
				mapCB.setRenderer(new PlaceToRaceRenderer());
				gbc.gridx = 1;
				contentPanel.add(mapCB, gbc);

				this.teamNameLabelList.add(teamLabel);
				this.mapCBList.add(mapCB);
			}
		}

		if(this.firstShow || this.numberOfTeams != numberOfTeamsTmp || this.minSupportedPlayersPerMap != minSupportedPlayersPerMapTmp)
		{
			this.numberOfTeams = numberOfTeamsTmp;
			this.minSupportedPlayersPerMap = minSupportedPlayersPerMapTmp;

			this.maps = new TreeMap<String, PlaceToRace>();
			for(Generator g: karoAPICache.getGenerators())
				this.maps.put(getKey(g), g);
			for(Map m: karoAPICache.getMaps())
				this.maps.put(getKey(m), m);
			this.maps.values().removeIf(map -> {
				return map.getPlayersMax() < this.minSupportedPlayersPerMap;
			});

			for(int i = 0; i < this.mapCBList.size(); i++)
			{
				this.mapCBList.get(i).setModel(new DefaultComboBoxModel<PlaceToRace>(maps.values().toArray(new PlaceToRace[0])));
			}
		}

		boolean enabled;
		String label;
		for(int i = 0; i < this.mapCBList.size(); i++)
		{
			enabled = (i < this.numberOfTeams);

			if(enabled && (GameSeriesManager.isTeamBased(gameSeries) || gameSeries.getType() == EnumGameSeriesType.KLC))
				label = gameSeries.getTeams().get(i).getName();
			else
				label = "Team " + (i + 1);

			this.teamNameLabelList.get(i).setText(label);
			this.mapCBList.get(i).setEnabled(enabled);
		}

		// preselect values from gameseries
		if(this.firstShow)
		{
			// preselect values from gameseries
			if(GameSeriesManager.isTeamBased(gameSeries) || gameSeries.getType() == EnumGameSeriesType.KLC)
			{
				for(int i = 0; i < gameSeries.getTeams().size(); i++)
				{
					if(gameSeries.getTeams().get(i).getHomeMap() == null)
						continue;
					preselectMap(gameSeries.getTeams().get(i).getHomeMap(), i);
				}
			}
		}

		this.firstShow = false;
	}

	private String getKey(PlaceToRace ptr)
	{
		logger.debug(ptr);
		if(ptr instanceof Map)
			return "map#" + StringUtil.toString(((Map) ptr).getId(), 5);
		else if(ptr instanceof Generator)
			return "generator#" + ((Generator) ptr).getKey();
		return null;
	}

	private void preselectMap(PlaceToRace map, int index)
	{
		String key = getKey(map);
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
	}
}
