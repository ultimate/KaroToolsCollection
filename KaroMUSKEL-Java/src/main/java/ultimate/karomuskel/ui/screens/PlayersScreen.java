package ultimate.karomuskel.ui.screens;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.exceptions.GameSeriesException;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.Team;
import ultimate.karoapi4j.model.official.User;
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.ui.EnumNavigation;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.components.GenericListModel;

public class PlayersScreen extends Screen implements ActionListener
{
	private static final long		serialVersionUID	= 1L;

	private static final int		listFixedCellWidth	= 250;

	private JList<User>				allPlayersLI;
	private TreeMap<String, User>	allPlayers;

	private List<JList<User>>		teamLIList;
	private List<JTextField>		teamNameTFList;
	private List<JButton>			addButtonList;
	private List<JButton>			removeButtonList;

	private int						teams;
	private int						maxPlayersPerTeam;
	private Boolean					ignoreInvitable;

	private boolean					autoNameTeams		= false;
	private boolean					multipleTeams		= false;

	public PlayersScreen(JFrame gui, Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(gui, previous, karoAPICache, previousButton, nextButton, "screen.players.header");

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

	@Override
	public String getNextKey()
	{
		return "screen.players.next";
	}

	@SuppressWarnings("unchecked")
	@Override
	public GameSeries applySettings(GameSeries gameSeries, EnumNavigation direction) throws GameSeriesException
	{
		if(GameSeriesManager.isTeamBased(gameSeries))
		{
			Team team;
			List<User> playerList;
			gameSeries.getTeams().clear();
			for(int i = 0; i < this.teams; i++)
			{
				String teamName = this.teamNameTFList.get(i).getText();
				playerList = new LinkedList<User>();
				User[] players = ((GenericListModel<String, User>) this.teamLIList.get(i).getModel()).getEntryArray();
				if(direction == EnumNavigation.next && players.length < (int) gameSeries.get(GameSeries.MIN_PLAYERS_PER_TEAM))
					throw new GameSeriesException("screen.players.minplayersperteam", teamName);
				if(direction == EnumNavigation.next && players.length > (int) gameSeries.get(GameSeries.MAX_PLAYERS_PER_TEAM))
					throw new GameSeriesException("screen.players.maxplayersperteam", teamName);
				for(User player : players)
				{
					playerList.add(player);
				}
				team = new Team(teamName, playerList);
				gameSeries.getTeams().add(team);
			}
			gameSeries.getTeamsByKey().put(GameSeries.KEY_ROUND + (this.teams), gameSeries.getTeams());
		}
		else if(gameSeries.getType() == EnumGameSeriesType.Simple)
		{
			User[] players = ((GenericListModel<String, User>) this.teamLIList.get(0).getModel()).getEntryArray();
			if(direction == EnumNavigation.next && players.length < (int) gameSeries.get(GameSeries.MIN_PLAYERS_PER_GAME) - 1)
				throw new GameSeriesException("screen.players.notenoughplayers");
			gameSeries.getPlayers().clear();
			for(User player : players)
			{
				gameSeries.getPlayers().add(player);
			}
		}
		else if(gameSeries.getType() == EnumGameSeriesType.Balanced)
		{
			User[] players = ((GenericListModel<String, User>) this.teamLIList.get(0).getModel()).getEntryArray();
			gameSeries.getPlayers().clear();
			for(User player : players)
			{
				gameSeries.getPlayers().add(player);
			}
		}
		else if(gameSeries.getType() == EnumGameSeriesType.KLC)
		{
			List<User> playerList;
			for(int i = 0; i < this.teams; i++)
			{
				String teamName = this.teamNameTFList.get(i).getText();
				if(gameSeries.getPlayersByKey().containsKey(GameSeries.KEY_LEAGUE + (i + 1)))
				{
					playerList = gameSeries.getPlayersByKey().get(GameSeries.KEY_LEAGUE + (i + 1));
					playerList.clear();
				}
				else
				{
					playerList = new ArrayList<>(GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_KLC_PLAYERS_PER_LEAGUE));
					gameSeries.getPlayersByKey().put(GameSeries.KEY_LEAGUE + (i + 1), playerList);
				}
				User[] players = ((GenericListModel<String, User>) this.teamLIList.get(i).getModel()).getEntryArray();
				if(direction == EnumNavigation.next && players.length != GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_KLC_PLAYERS_PER_LEAGUE))
					throw new GameSeriesException("screen.players.invalidplayersperleague", teamName, GameSeriesManager.getStringConfig(gameSeries, GameSeries.CONF_KLC_PLAYERS_PER_LEAGUE));
				for(User player : players)
				{
					playerList.add(player);
					gameSeries.getPlayers().add(player);
				}
			}
		}
		return gameSeries;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateBeforeShow(GameSeries gameSeries, EnumNavigation direction)
	{
		int teamsTmp = 1;
		int maxPlayersPerTeamTmp = 25;
		boolean multipleTeamsTmp = false;
		if(GameSeriesManager.isTeamBased(gameSeries))
		{
			teamsTmp = (int) gameSeries.get(GameSeries.NUMBER_OF_TEAMS);
			maxPlayersPerTeamTmp = (int) gameSeries.get(GameSeries.MAX_PLAYERS_PER_TEAM);
			this.autoNameTeams = (boolean) gameSeries.get(GameSeries.AUTO_NAME_TEAMS);
			multipleTeamsTmp = (boolean) gameSeries.get(GameSeries.ALLOW_MULTIPLE_TEAMS);
		}
		else if(gameSeries.getType() == EnumGameSeriesType.KLC)
		{
			teamsTmp = GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_KLC_LEAGUES);
			maxPlayersPerTeamTmp = GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_KLC_PLAYERS_PER_LEAGUE);
			this.autoNameTeams = false;
			multipleTeamsTmp = false;
		}

		boolean changed = false;

		if(this.firstShow)
		{
			this.teamLIList = new LinkedList<>();
			this.teamNameTFList = new LinkedList<>();
			this.addButtonList = new LinkedList<>();
			this.removeButtonList = new LinkedList<>();

			this.teams = teamsTmp;
			this.maxPlayersPerTeam = maxPlayersPerTeamTmp;

			this.removeAll();

			JPanel allPlayersPanel = new JPanel();
			allPlayersPanel.setLayout(new BorderLayout(5, 5));
			this.add(allPlayersPanel);

			this.allPlayersLI = new JList<>();
			this.allPlayersLI.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			this.allPlayersLI.setFixedCellWidth(listFixedCellWidth);
			JScrollPane allMapsSP = new JScrollPane(this.allPlayersLI, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			allPlayersPanel.add(new JLabel(Language.getString("screen.players.allplayers")), BorderLayout.NORTH);
			allPlayersPanel.add(allMapsSP, BorderLayout.CENTER);

			JList<User> teamPlayersLI;
			JScrollPane teamPlayersSP;
			JTextField teamNameTF;
			JButton addButton;
			JButton removeButton;
			JPanel buttonPanel;
			JPanel teamPanel;
			JPanel innerTeamPanel;

			if(this.teams == 1)
			{
				buttonPanel = new JPanel();
				buttonPanel.setLayout(new GridBagLayout());
				this.add(buttonPanel);

				teamPanel = new JPanel();
				teamPanel.setLayout(new BorderLayout(5, 5));
				this.add(teamPanel);

				GridBagConstraints gbc = new GridBagConstraints();
				gbc.insets = new Insets(5, 5, 5, 5);
				gbc.fill = GridBagConstraints.HORIZONTAL;

				addButton = new JButton(Language.getString("option.add"));
				addButton.setActionCommand("add" + 0);
				addButton.addActionListener(this);
				gbc.gridx = 0;
				gbc.gridy = 0;
				buttonPanel.add(addButton, gbc);

				removeButton = new JButton(Language.getString("option.remove"));
				removeButton.setActionCommand("rem" + 0);
				removeButton.addActionListener(this);
				gbc.gridx = 0;
				gbc.gridy = 1;
				buttonPanel.add(removeButton, gbc);

				teamPlayersLI = new JList<>(new GenericListModel<String, User>(User.class, new HashMap<String, User>()));
				teamPlayersLI.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				this.allPlayersLI.setFixedCellWidth(1500);
				teamPlayersLI.setFixedCellWidth(1500);
				teamPlayersSP = new JScrollPane(teamPlayersLI, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

				teamPanel.add(new JLabel(Language.getString("screen.players.selectedplayers")), BorderLayout.NORTH);
				teamPanel.add(teamPlayersSP, BorderLayout.CENTER);

				this.addButtonList.add(addButton);
				this.removeButtonList.add(removeButton);
				this.teamLIList.add(teamPlayersLI);
			}
			else
			{
				JPanel outerTeamsPanel = new JPanel();
				outerTeamsPanel.setLayout(new BorderLayout(5, 5));
				this.add(outerTeamsPanel);

				JPanel innerTeamsPanel = new JPanel();
				innerTeamsPanel.setLayout(new BoxLayout(innerTeamsPanel, BoxLayout.Y_AXIS));
				JScrollPane innerTeamsSP = new JScrollPane(innerTeamsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

				outerTeamsPanel.add(new JLabel(Language.getString("screen.players.selectedplayers")), BorderLayout.NORTH);
				outerTeamsPanel.add(innerTeamsSP, BorderLayout.CENTER);

				int maxTeams = Math.max(this.teams, GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_MAX_TEAMS));

				boolean enabled;
				for(int i = 0; i < maxTeams; i++)
				{
					enabled = (i < this.teams);

					innerTeamPanel = new JPanel();
					innerTeamPanel.setLayout(new BoxLayout(innerTeamPanel, BoxLayout.X_AXIS));
					innerTeamsPanel.add(innerTeamPanel);

					buttonPanel = new JPanel();
					buttonPanel.setLayout(new GridBagLayout());
					innerTeamPanel.add(buttonPanel);

					teamPanel = new JPanel();
					teamPanel.setLayout(new BoxLayout(teamPanel, BoxLayout.Y_AXIS));
					innerTeamPanel.add(teamPanel);

					GridBagConstraints gbc2 = new GridBagConstraints();
					gbc2.fill = GridBagConstraints.HORIZONTAL;

					addButton = new JButton(Language.getString("option.add"));
					addButton.setActionCommand("add" + i);
					addButton.addActionListener(this);
					addButton.setEnabled(enabled);
					gbc2.anchor = GridBagConstraints.PAGE_END;
					gbc2.gridx = 0;
					gbc2.gridy = 0;
					buttonPanel.add(addButton, gbc2);

					removeButton = new JButton(Language.getString("option.remove"));
					removeButton.setActionCommand("rem" + i);
					removeButton.addActionListener(this);
					removeButton.setEnabled(enabled);
					gbc2.anchor = GridBagConstraints.PAGE_START;
					gbc2.gridx = 0;
					gbc2.gridy = 1;
					buttonPanel.add(removeButton, gbc2);

					teamNameTF = new JTextField("Team " + (i + 1), 15);
					if(gameSeries.getType() == EnumGameSeriesType.KLC)
						teamNameTF.setText("Liga " + (i + 1));
					teamNameTF.setEnabled(enabled);

					teamPlayersLI = new JList<>(new GenericListModel<String, User>(User.class, new HashMap<String, User>()));
					teamPlayersLI.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					teamPlayersLI.setFixedCellWidth(listFixedCellWidth);
					teamPlayersLI.setVisibleRowCount(Math.max(3, this.maxPlayersPerTeam));
					teamPlayersLI.setEnabled(enabled);
					teamPlayersSP = new JScrollPane(teamPlayersLI, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

					teamPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					teamPanel.add(teamNameTF);
					teamPanel.add(teamPlayersSP);

					this.teamNameTFList.add(teamNameTF);
					this.addButtonList.add(addButton);
					this.removeButtonList.add(removeButton);
					this.teamLIList.add(teamPlayersLI);
				}
			}
		}
		if(this.teams != teamsTmp)
		{
			this.teams = teamsTmp;
			JList<User> teamPlayersLI;
			JTextField teamNameTF;
			JButton addButton;
			JButton removeButton;
			for(int i = 0; i < this.teamLIList.size(); i++)
			{
				teamPlayersLI = this.teamLIList.get(i);
				teamNameTF = this.teamNameTFList.get(i);
				addButton = this.addButtonList.get(i);
				removeButton = this.removeButtonList.get(i);

				teamPlayersLI.setEnabled(i < this.teams);
				addButton.setEnabled(i < this.teams);
				removeButton.setEnabled(i < this.teams);

				if(gameSeries.getType() == EnumGameSeriesType.KLC)
					teamNameTF.setText("Liga " + (i + 1));
				else
					teamNameTF.setEnabled(i < this.teams);

				if(i >= this.teams)
				{
					// remove all entries
					User[] players = ((GenericListModel<String, User>) teamPlayersLI.getModel()).getEntryArray();
					String key;
					for(User player : players)
					{
						key = player.getLoginLowerCase();
						((GenericListModel<String, User>) teamPlayersLI.getModel()).removeElement(key);
						teamPlayersLI.setModel(teamPlayersLI.getModel());
						((GenericListModel<String, User>) this.allPlayersLI.getModel()).addElement(key, player);
						this.allPlayersLI.setModel(this.allPlayersLI.getModel());
					}
				}
			}
			changed = true;
		}
		else if(this.maxPlayersPerTeam != maxPlayersPerTeamTmp)
		{
			this.maxPlayersPerTeam = maxPlayersPerTeamTmp;
			for(JList<User> teamPlayersLI : this.teamLIList)
			{
				teamPlayersLI.setVisibleRowCount(this.maxPlayersPerTeam);
			}
		}

		boolean ignoreInvitableTmp = gameSeries.isIgnoreInvitable();

		if(this.ignoreInvitable == null || this.ignoreInvitable != ignoreInvitableTmp || this.multipleTeams != multipleTeamsTmp)
		{
			changed = true;
			this.ignoreInvitable = ignoreInvitableTmp;
			this.multipleTeams = multipleTeamsTmp;
			this.allPlayers = new TreeMap<String, User>(karoAPICache.getUsersByLogin());

			if(!this.ignoreInvitable)
			{
				for(JList<User> teamPlayersLI : this.teamLIList)
				{
					// remove uninvitable players
					User[] players = ((GenericListModel<String, User>) teamPlayersLI.getModel()).getEntryArray();
					String key;
					for(User player : players)
					{
						if(!this.ignoreInvitable && !player.isInvitable(false))
						{
							key = player.getLoginLowerCase();
							((GenericListModel<String, User>) teamPlayersLI.getModel()).removeElement(key);
							teamPlayersLI.setModel(teamPlayersLI.getModel());
							((GenericListModel<String, User>) allPlayersLI.getModel()).addElement(key, player);
							allPlayersLI.setModel(allPlayersLI.getModel());
						}
					}
				}
				List<String> removeList = new LinkedList<String>();
				User player;
				for(String key : this.allPlayers.keySet())
				{
					player = this.allPlayers.get(key);
					if(!this.ignoreInvitable && !player.isInvitable(false))
					{
						removeList.add(key);
					}
				}
				for(String key : removeList)
				{
					this.allPlayers.remove(key);
				}
			}
		}
		if(GameSeriesManager.isTeamBased(gameSeries))
		{
			String key = gameSeries.getCreator().getLoginLowerCase();
			if((boolean) gameSeries.get(GameSeries.USE_CREATOR_TEAM))
			{
				if(!this.allPlayers.containsKey(key))
				{
					this.allPlayers.put(key, gameSeries.getCreator());
					changed = true;
				}
			}
			else
			{
				if(this.allPlayers.containsKey(key))
				{
					this.allPlayers.remove(key);
					changed = true;
				}
			}
		}
		if(changed)
		{
			TreeMap<String, User> allPlayersTmp = new TreeMap<String, User>(this.allPlayers);
			JList<User> teamPlayersLI;
			if(!this.multipleTeams)
			{
				for(int i = 0; i < this.teamLIList.size(); i++)
				{
					teamPlayersLI = this.teamLIList.get(i);

					User[] players = ((GenericListModel<String, User>) teamPlayersLI.getModel()).getEntryArray();
					String key;
					for(User player : players)
					{
						key = player.getLoginLowerCase();
						allPlayersTmp.remove(key);
					}
				}
			}
			this.allPlayersLI.setModel(new GenericListModel<String, User>(User.class, allPlayersTmp));
		}

		if(this.firstShow)
		{
			// preselect values from gameseries
			if(teams == 1)
			{
				for(User player : gameSeries.getPlayers())
					preselectPlayer(player, 0);
			}
			else
			{
				if(gameSeries.getType() == EnumGameSeriesType.KLC)
				{
					List<User> playerList;
					for(int l = 0; l < teams; l++)
					{
						playerList = gameSeries.getPlayersByKey().get(GameSeries.KEY_LEAGUE + (l + 1));
						for(User player : playerList)
							preselectPlayer(player, l);
					}
				}
				else if(GameSeriesManager.isTeamBased(gameSeries))
				{
					Team team;
					List<Team> teamList;
					if(gameSeries.getType() == EnumGameSeriesType.KO)
						teamList = gameSeries.getTeamsByKey().get(GameSeries.KEY_ROUND + teams);
					else
						teamList = gameSeries.getTeams();
					for(int t = 0; t < teams && t < teamList.size(); t++)
					{
						team = teamList.get(t);
						for(User player : team.getMembers())
							preselectPlayer(player, t);
						teamNameTFList.get(t).setText(team.getName());
					}
				}
			}
		}

		this.firstShow = false;
	}

	@SuppressWarnings("unchecked")
	private void preselectPlayer(User player, int teamIndex)
	{
		logger.debug("preselect player: " + player.getLogin());
		// check user is present in list, if not (not invitable), add first
		if(!((GenericListModel<String, User>) allPlayersLI.getModel()).containsKey(player.getLogin()))
		{
			logger.warn("player  not present in list: " + player.getLogin() + " -> adding");
			((GenericListModel<String, User>) allPlayersLI.getModel()).addElement(player.getLoginLowerCase(), player);
			allPlayersLI.setModel(allPlayersLI.getModel());
		}
		// select player, then add
		allPlayersLI.setSelectedValue(player, false);
		actionPerformed(new ActionEvent(this, 0, "add" + teamIndex));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().startsWith("add") || e.getActionCommand().startsWith("rem"))
		{
			boolean add = e.getActionCommand().startsWith("add");
			int teamIndex = Integer.parseInt(e.getActionCommand().substring(3));

			boolean oldNameGenerated = false;
			if(this.autoNameTeams)
			{
				if(teamNameTFList.get(teamIndex).getText().equals("Team " + (teamIndex + 1)))
					oldNameGenerated = true;
				else if(teamNameTFList.get(teamIndex).getText().equals(createTeamName(teamIndex)))
					oldNameGenerated = true;
			}

			JList<User> teamLI = teamLIList.get(teamIndex);

			if(add)
			{
				List<User> players = allPlayersLI.getSelectedValuesList();
				String key;
				for(User player : players)
				{
					key = player.getLoginLowerCase();
					if(!this.multipleTeams)
						((GenericListModel<String, User>) allPlayersLI.getModel()).removeElement(key);
					((GenericListModel<String, User>) teamLI.getModel()).addElement(key, player);
				}
			}
			else
			{
				List<User> players = teamLI.getSelectedValuesList();
				String key;
				for(User player : players)
				{
					key = player.getLoginLowerCase();
					((GenericListModel<String, User>) teamLI.getModel()).removeElement(key);
					if(!this.multipleTeams)
						((GenericListModel<String, User>) allPlayersLI.getModel()).addElement(key, player);
				}
			}

			if(this.autoNameTeams && oldNameGenerated)
			{
				teamNameTFList.get(teamIndex).setText(createTeamName(teamIndex));
			}

			repaint();
		}
	}

	@SuppressWarnings("unchecked")
	private String createTeamName(int teamIndex)
	{
		User[] teamPlayers = ((GenericListModel<String, User>) teamLIList.get(teamIndex).getModel()).getEntryArray();
		StringBuilder tmp = new StringBuilder();
		for(int p = 0; p < teamPlayers.length; p++)
		{
			if((p != 0) && (p != teamPlayers.length - 1))
				tmp.append(", ");
			else if((p != 0) && (p == teamPlayers.length - 1))
				tmp.append(" und ");
			tmp.append(teamPlayers[p].getLogin());
		}
		return tmp.toString();
	}
}
