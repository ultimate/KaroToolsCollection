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
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.User;
import ultimate.karomuskel.GameSeriesManager;
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
	private boolean					firstCall			= true;

	public HomeMapsScreen(Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(previous, karoAPICache, previousButton, nextButton, "screen.homemaps.header", "screen.homemaps.next");
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

	@Override
	public GameSeries applySettings(GameSeries gameSeries) throws GameSeriesException
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
				gameSeries.getMapsByKey().get("" + player.getId()).add(homeMap);
			}
		}
		return gameSeries;
	}

	@Override
	public void updateBeforeShow(GameSeries gameSeries)
	{
		int numberOfTeamsTmp = 0;
		int minSupportedPlayersPerMapTmp = GameSeriesManager.getMinSupportedPlayersPerMap(gameSeries);

		if(GameSeriesManager.isTeamBased(gameSeries))
		{
			numberOfTeamsTmp = (int) gameSeries.get(GameSeries.NUMBER_OF_TEAMS);
		}
		else if(gameSeries.getType() == EnumGameSeriesType.KLC)
		{
			int groups = GameSeriesManager.getIntConfig(GameSeries.CONF_KLC_GROUPS);
			int leagues = GameSeriesManager.getIntConfig(GameSeries.CONF_KLC_LEAGUES);
			numberOfTeamsTmp = groups * leagues;
		}

		if(this.firstCall)
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

			int maxTeams = 32;
			if(gameSeries.getType() == EnumGameSeriesType.KO)
				maxTeams = GameSeriesManager.getIntConfig(GameSeries.CONF_MAX_TEAMS);
			else if(gameSeries.getType() == EnumGameSeriesType.League)
				maxTeams = GameSeriesManager.getIntConfig(GameSeries.CONF_MAX_TEAMS);
			else if(gameSeries.getType() == EnumGameSeriesType.KLC)
				maxTeams = numberOfTeamsTmp;

			for(int i = 0; i < maxTeams; i++)
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

		if(this.firstCall || this.numberOfTeams != numberOfTeamsTmp || this.minSupportedPlayersPerMap != minSupportedPlayersPerMapTmp)
		{
			this.firstCall = false;
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
	}
}
