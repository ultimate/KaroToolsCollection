package ultimate.karomuskel.ui.screens;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

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
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.ui.EnumNavigation;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.MainFrame;
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.components.PlaceToRaceRenderer;

public class HomeMapsScreen extends MapComboBoxScreen implements ActionListener
{
	private static final long	serialVersionUID		= 1L;

	private static final String	ACTION_MAP_CONFIGURE	= "mapConfigure";

	private List<JLabel>		teamNameLabelList;

	private int					numberOfTeams;
	private int					minSupportedPlayersPerMap;

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
	public Message updateBeforeShow(GameSeries gameSeries, EnumNavigation direction)
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
			this.mapCBList = new LinkedList<JComboBox<PlaceToRace>>(); // from super class
			this.mapEditButtonList = new LinkedList<>(); // from super class
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

			for(int i = 0; i < maxTeams; i++)
			{
				gbc.gridy = i;

				teamLabel = new JLabel();
				gbc.gridx = 0;
				contentPanel.add(teamLabel, gbc);

				JComboBox<PlaceToRace> mapCB = new JComboBox<>();
				mapCB.setRenderer(new PlaceToRaceRenderer());
				gbc.gridx = 1;
				contentPanel.add(mapCB, gbc);

				JButton mapEditButton = new JButton(Language.getString("option.edit"));
				mapEditButton.addActionListener(this);
				mapEditButton.setActionCommand(ACTION_MAP_CONFIGURE + i);
				gbc.gridx = 2;
				contentPanel.add(mapEditButton, gbc);
				
				mapCB.addActionListener(e -> {
					mapEditButton.setEnabled(mapCB.getSelectedItem() instanceof Generator);
				});

				this.teamNameLabelList.add(teamLabel);
				this.mapCBList.add(mapCB);
				this.mapEditButtonList.add(mapEditButton);
			}
		}

		if(this.firstShow || this.numberOfTeams != numberOfTeamsTmp || this.minSupportedPlayersPerMap != minSupportedPlayersPerMapTmp)
		{
			this.numberOfTeams = numberOfTeamsTmp;
			this.minSupportedPlayersPerMap = minSupportedPlayersPerMapTmp;

			List<PlaceToRace> maps = this.karoAPICache.getPlacesToRace();
			maps.removeIf(map -> {
				return map.getPlayersMax() < this.minSupportedPlayersPerMap;
			});

			for(int i = 0; i < this.mapCBList.size(); i++)
			{
				this.mapCBList.get(i).setModel(new DefaultComboBoxModel<PlaceToRace>(maps.toArray(new PlaceToRace[0])));
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
			this.mapEditButtonList.get(i).setEnabled(enabled);
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
		
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().startsWith(ACTION_MAP_CONFIGURE))
		{
			int mapNumber = Integer.parseInt(e.getActionCommand().substring(ACTION_MAP_CONFIGURE.length()));
			super.handleGeneratorConfigurationEvent(mapNumber);
		}
	}
}
