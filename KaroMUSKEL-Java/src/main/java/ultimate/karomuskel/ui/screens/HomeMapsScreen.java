package ultimate.karomuskel.ui.screens;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
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
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.User;
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.ui.EnumNavigation;
import ultimate.karomuskel.ui.MainFrame;
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.components.MapRenderer;

public class HomeMapsScreen extends Screen
{
	private static final long		serialVersionUID	= 1L;

	private List<JLabel>			teamNameLabelList;
	private List<JComboBox<Map>>	mapCBList;

	private int						numberOfTeams;
	private int						minSupportedPlayersPerMap;

	private TreeMap<Integer, Map>	maps;

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
		if(GameSeriesManager.isTeamBased(gameSeries))
		{
			Map homeMap;
			for(int i = 0; i < this.numberOfTeams; i++)
			{
				homeMap = (Map) this.mapCBList.get(i).getSelectedItem();
				gameSeries.getTeams().get(i).setHomeMap(homeMap);
			}
		}
		else if(gameSeries.getType() == EnumGameSeriesType.KLC)
		{
			User player;
			Map homeMap;
			for(int i = 0; i < this.numberOfTeams; i++)
			{
				homeMap = (Map) this.mapCBList.get(i).getSelectedItem();
				player = gameSeries.getPlayers().get(i);
				gameSeries.getMapsByKey().put("" + player.getId(), Arrays.asList(homeMap));
			}
		}
		return gameSeries;
	}

	@Override
	public void updateBeforeShow(GameSeries gameSeries, EnumNavigation direction)
	{
		int numberOfTeamsTmp = 0;
		int minSupportedPlayersPerMapTmp = GameSeriesManager.getMinSupportedPlayersPerMap(gameSeries);

		if(GameSeriesManager.isTeamBased(gameSeries))
		{
			numberOfTeamsTmp = (int) gameSeries.get(GameSeries.NUMBER_OF_TEAMS);
		}
		else if(gameSeries.getType() == EnumGameSeriesType.KLC)
		{
			int groups = GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_KLC_GROUPS);
			int leagues = GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_KLC_LEAGUES);
			numberOfTeamsTmp = groups * leagues;
		}

		if(this.firstShow)
		{
			this.numberOfTeams = numberOfTeamsTmp;
			this.minSupportedPlayersPerMap = minSupportedPlayersPerMapTmp;

			this.teamNameLabelList = new LinkedList<JLabel>();
			this.mapCBList = new LinkedList<JComboBox<Map>>();
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
			JComboBox<Map> mapCB;

			for(int i = 0; i < this.numberOfTeams; i++)
			{
				gbc.gridy = i;

				teamLabel = new JLabel();
				gbc.gridx = 0;
				contentPanel.add(teamLabel, gbc);

				mapCB = new JComboBox<>();
				mapCB.setRenderer(new MapRenderer());
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

			this.maps = new TreeMap<>(karoAPICache.getMapsById());

			List<Integer> removeList = new LinkedList<Integer>();
			Map map;
			for(Integer key : this.maps.keySet())
			{
				map = this.maps.get(key);
				if(map.getPlayers() < this.minSupportedPlayersPerMap)
				{
					removeList.add(key);
				}
			}
			for(Integer key : removeList)
			{
				this.maps.remove(key);
			}

			for(int i = 0; i < this.mapCBList.size(); i++)
			{
				this.mapCBList.get(i).setModel(new DefaultComboBoxModel<Map>(maps.values().toArray(new Map[0])));
			}
		}

		boolean enabled;
		String label;
		for(int i = 0; i < this.mapCBList.size(); i++)
		{
			enabled = (i < this.numberOfTeams);

			if(enabled && GameSeriesManager.isTeamBased(gameSeries))
				label = gameSeries.getTeams().get(i).getName();
			else if(enabled && gameSeries.getType() == EnumGameSeriesType.KLC)
				label = gameSeries.getPlayers().get(i).getLogin();
			else
				label = "Team " + (i + 1);

			this.teamNameLabelList.get(i).setText(label);
			this.mapCBList.get(i).setEnabled(enabled);
		}

		// preselect values from gameseries
		if(this.firstShow)
		{
			// preselect values from gameseries
			if(GameSeriesManager.isTeamBased(gameSeries))
			{
				for(int i = 0; i < gameSeries.getTeams().size(); i++)
				{
					if(gameSeries.getTeams().get(i).getHomeMap() == null)
						continue;
					preselectMap(gameSeries.getTeams().get(i).getHomeMap(), i);
				}
			}
			else if(gameSeries.getType() == EnumGameSeriesType.KLC)
			{
				String playerKey;
				for(int i = 0; i < gameSeries.getPlayers().size(); i++)
				{
					playerKey = "" + gameSeries.getPlayers().get(i).getId();
					if(!gameSeries.getMapsByKey().containsKey(playerKey) || gameSeries.getMapsByKey().get(playerKey).isEmpty())
						continue;
					preselectMap(gameSeries.getMapsByKey().get(playerKey).get(0), i);
				}
			}
		}

		this.firstShow = false;
	}

	private void preselectMap(Map map, int index)
	{
		logger.debug("preselect map: " + map.getId() + " for team " + this.teamNameLabelList.get(index).getText());
		// check map is present in model, if not, add first
		if(((DefaultComboBoxModel<Map>) this.mapCBList.get(index).getModel()).getIndexOf(map) == -1)
		{
			logger.warn("map not present in list: " + map.getId() + " -> adding");

			for(int i = 0; i < this.mapCBList.size(); i++)
			{
				((DefaultComboBoxModel<Map>) this.mapCBList.get(i).getModel()).addElement(map);
			}
		}
		// select map, then add
		((DefaultComboBoxModel<Map>) this.mapCBList.get(index).getModel()).setSelectedItem(map);
	}
}
