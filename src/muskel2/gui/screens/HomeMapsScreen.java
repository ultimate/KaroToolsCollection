package muskel2.gui.screens;

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

import muskel2.core.exceptions.GameSeriesException;
import muskel2.gui.Screen;
import muskel2.gui.help.MapRenderer;
import muskel2.model.GameSeries;
import muskel2.model.Karopapier;
import muskel2.model.Map;
import muskel2.model.series.KOGameSeries;
import muskel2.model.series.LeagueGameSeries;
import muskel2.model.series.TeamBasedGameSeries;

public class HomeMapsScreen extends Screen
{
	private static final long		serialVersionUID	= 1L;

	private List<JLabel>			teamNameLabelList;
	private List<JComboBox>			mapCBList;

	private int						numberOfTeams;
	private int						minSupportedPlayersPerMap;

	private TreeMap<Integer, Map>	maps;
	private boolean					firstCall			= true;

	public HomeMapsScreen(Screen previous, Karopapier karopapier, JButton previousButton, JButton nextButton)
	{
		super(previous, karopapier, previousButton, nextButton, "screen.homemaps.header", "screen.homemaps.next");
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

	@Override
	public GameSeries applySettings(GameSeries gameSeries) throws GameSeriesException
	{
		Map homeMap;
		for(int i = 0; i < this.numberOfTeams; i++)
		{
			homeMap = (Map) this.mapCBList.get(i).getSelectedItem();
			((TeamBasedGameSeries) gameSeries).getTeams().get(i).setHomeMap(homeMap);
		}
		return gameSeries;
	}

	@Override
	public void updateBeforeShow(GameSeries gameSeries)
	{
		int numberOfTeamsTmp = ((TeamBasedGameSeries) gameSeries).getNumberOfTeams();
		int minSupportedPlayersPerMapTmp = ((TeamBasedGameSeries) gameSeries).getMinSupportedPlayersPerMap();

		if(this.firstCall)
		{
			this.numberOfTeams = numberOfTeamsTmp;
			this.minSupportedPlayersPerMap = minSupportedPlayersPerMapTmp;

			this.teamNameLabelList = new LinkedList<JLabel>();
			this.mapCBList = new LinkedList<JComboBox>();
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
			JComboBox mapCB;

			int maxTeams = 32;
			if(gameSeries instanceof KOGameSeries)
				maxTeams = KOGameSeries.MAX_TEAMS;
			else if(gameSeries instanceof LeagueGameSeries)
				maxTeams = LeagueGameSeries.MAX_TEAMS;

			for(int i = 0; i < maxTeams; i++)
			{
				gbc.gridy = i;
				
				teamLabel = new JLabel();
				gbc.gridx = 0;
				contentPanel.add(teamLabel, gbc);

				mapCB = new JComboBox();
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

			this.maps = karopapier.getMaps();

			List<Integer> removeList = new LinkedList<Integer>();
			Map map;
			for(Integer key : this.maps.keySet())
			{
				map = this.maps.get(key);
				if(map.getMaxPlayers() < this.minSupportedPlayersPerMap)
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
				this.mapCBList.get(i).setModel(new DefaultComboBoxModel(maps.values().toArray(new Map[0])));
			}
		}
		
		boolean enabled;
		String label;
		for(int i = 0; i < this.mapCBList.size(); i++)
		{
			enabled = (i < this.numberOfTeams);
			
			if(enabled)
				label = ((TeamBasedGameSeries) gameSeries).getTeams().get(i).getName();
			else
				label = "Team " + (i + 1);
			
			this.teamNameLabelList.get(i).setText(label);
			this.mapCBList.get(i).setEnabled(enabled);
		}
	}
}
