package ultimate.karomuskel.ui.screens;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumCreatorParticipation;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.exceptions.GameSeriesException;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.Team;
import ultimate.karoapi4j.model.official.User;
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.ui.EnumNavigation;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.MainFrame;
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.UIUtil;
import ultimate.karomuskel.ui.components.GenericListModel;

public class PlayersScreen extends FilterScreen<User> implements ActionListener
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

	public PlayersScreen(MainFrame gui, Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(gui, previous, karoAPICache, previousButton, nextButton, "screen.players.header");

		this.getContentPanel().setLayout(new BoxLayout(this.getContentPanel(), BoxLayout.X_AXIS));
	}

	@Override
	public String getNextKey()
	{
		return "screen.players.next";
	}

	@Override
	public GameSeries applySettings(GameSeries gameSeries, EnumNavigation direction) throws GameSeriesException
	{
		if(GameSeriesManager.isTeamBased(gameSeries))
		{
			Team team;
			Set<User> playerSet;
			gameSeries.getTeams().clear();
			for(int i = 0; i < this.teams; i++)
			{
				String teamName = this.teamNameTFList.get(i).getText();
				User[] players = ((GenericListModel<String, User>) this.teamLIList.get(i).getModel()).getEntryArray();
				if(direction == EnumNavigation.next && players.length < (int) gameSeries.get(GameSeries.MIN_PLAYERS_PER_TEAM))
					throw new GameSeriesException("screen.players.minplayersperteam", teamName);
				if(direction == EnumNavigation.next && players.length > (int) gameSeries.get(GameSeries.MAX_PLAYERS_PER_TEAM))
					throw new GameSeriesException("screen.players.maxplayersperteam", teamName);
				playerSet = new LinkedHashSet<User>(Arrays.asList(players));
				team = new Team(teamName, playerSet);
				gameSeries.getTeams().add(team);
			}

			if(gameSeries.getType() == EnumGameSeriesType.KO)
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
			int minPlayers = GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_BALANCED_MIN_PLAYERS);
			if(direction == EnumNavigation.next && players.length < minPlayers)
				throw new GameSeriesException("screen.players.notenoughplayers.balanced", null, "" + minPlayers);
			gameSeries.getPlayers().clear();
			for(User player : players)
			{
				gameSeries.getPlayers().add(player);
			}
		}
		else if(gameSeries.getType() == EnumGameSeriesType.KLC)
		{
			if(direction == EnumNavigation.next)
			{
				// count & check amount of players first
				int totalPlayers = 0;
				for(int i = 0; i < this.teams; i++)
					totalPlayers += ((GenericListModel<String, User>) this.teamLIList.get(i).getModel()).getEntryArray().length;
				if(totalPlayers < GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_KLC_FIRST_KO_ROUND))
					throw new GameSeriesException("screen.players.notenoughplayers.KLC", null, GameSeriesManager.getStringConfig(gameSeries, GameSeries.CONF_KLC_FIRST_KO_ROUND));

				boolean skipGroups = (totalPlayers == GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_KLC_FIRST_KO_ROUND));
				// skip one SummaryScreen and the GroupWinnersScreen
				findScreen(s -> {
					return s instanceof SummaryScreen;
				}, EnumNavigation.next).setSkip(skipGroups);
				findScreen(s -> {
					return s instanceof GroupWinnersScreen;
				}, EnumNavigation.next).setSkip(skipGroups);

				gameSeries.set(GameSeries.CURRENT_ROUND, totalPlayers);

				gameSeries.getPlayers().clear();
				gameSeries.getTeams().clear();
				for(int i = 0; i < this.teams; i++)
				{
					if(gameSeries.getPlayersByKey().containsKey(GameSeries.KEY_LEAGUE + (i + 1)))
						gameSeries.getPlayersByKey().get(GameSeries.KEY_LEAGUE + (i + 1)).clear();
					else
						gameSeries.getPlayersByKey().put(GameSeries.KEY_LEAGUE + (i + 1), new ArrayList<>());

					User[] players = ((GenericListModel<String, User>) this.teamLIList.get(i).getModel()).getEntryArray();
					for(User player : players)
					{
						gameSeries.getPlayersByKey().get(GameSeries.KEY_LEAGUE + (i + 1)).add(player);
						gameSeries.getPlayers().add(player);
						gameSeries.getTeams().add(new Team(player.getLogin(), player));
					}
				}
			}
		}
		return gameSeries;
	}

	@Override
	public Message updateBeforeShow(GameSeries gameSeries, EnumNavigation direction)
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
			maxPlayersPerTeamTmp = 12; // only for displaying
			this.autoNameTeams = false;
			multipleTeamsTmp = false;
		}

		boolean changed = false;

		if(this.firstShow)
		{
			// initialize search
			this.addTextFilter("screen.players.filter.loginOrId", user -> user.getId() + ":" + user.getLogin(), true);
			this.addNumberFilter("screen.players.filter.freeGames", user -> (user.getMaxGames() <= 0 ? Integer.MAX_VALUE : Math.max(0, user.getMaxGames() - user.getActiveGames())), NumberFilterMode.gteq, 0, 0, 999);
			this.addBooleanFilter("screen.players.filter.nightGames", user -> user.isAcceptsNightGames());
			this.nextFilterLine();
			
			this.teamLIList = new LinkedList<>();
			this.teamNameTFList = new LinkedList<>();
			this.addButtonList = new LinkedList<>();
			this.removeButtonList = new LinkedList<>();

			this.teams = teamsTmp;
			this.maxPlayersPerTeam = maxPlayersPerTeamTmp;

			this.getContentPanel().removeAll();

			JPanel allPlayersPanel = new JPanel();
			allPlayersPanel.setLayout(new BorderLayout(5, 5));
			this.getContentPanel().add(allPlayersPanel);

			this.allPlayersLI = new JList<>();
			this.allPlayersLI.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			this.allPlayersLI.setFixedCellWidth(listFixedCellWidth);
			JScrollPane allPlayerssSP = new JScrollPane(this.allPlayersLI, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			allPlayersPanel.add(new JLabel(Language.getString("screen.players.allplayers")), BorderLayout.NORTH);
			allPlayersPanel.add(allPlayerssSP, BorderLayout.CENTER);

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
				this.getContentPanel().add(buttonPanel);

				teamPanel = new JPanel();
				teamPanel.setLayout(new BorderLayout(5, 5));
				this.getContentPanel().add(teamPanel);

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
				teamPlayersLI.setFixedCellWidth(listFixedCellWidth);
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
				this.getContentPanel().add(outerTeamsPanel);

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
							((GenericListModel<String, User>) allPlayersLI.getModel()).addElement(key, player);
						}
					}
					// refresh the model
					teamPlayersLI.setModel(teamPlayersLI.getModel());
					allPlayersLI.setModel(allPlayersLI.getModel());
				}
				if(!this.ignoreInvitable)
				{
					this.allPlayers.values().removeIf(player -> {
						return !player.isInvitable(false);
					});
				}
			}
		}
		// add or remove the creator
		if((GameSeriesManager.isTeamBased(gameSeries) && (boolean) gameSeries.get(GameSeries.USE_CREATOR_TEAM)) || gameSeries.getCreatorParticipation() == EnumCreatorParticipation.not_participating)
		{
			if(!this.allPlayers.containsKey(gameSeries.getCreator().getLoginLowerCase()))
			{
				this.allPlayers.put(gameSeries.getCreator().getLoginLowerCase(), gameSeries.getCreator());
				changed = true;
			}
		}
		else
		{
			if(this.allPlayers.containsKey(gameSeries.getCreator().getLoginLowerCase()))
			{
				this.allPlayers.remove(gameSeries.getCreator().getLoginLowerCase());
				changed = true;
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
			// create a model for filtering
			GenericListModel<String, User> model = new GenericListModel<String, User>(User.class, allPlayersTmp);
			this.setModel(model);
			this.allPlayersLI.setModel(model);
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
						if(playerList != null)
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
					if(teamList != null)
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
		
		return null;
	}

	private void preselectPlayer(User player, int teamIndex)
	{
		logger.debug("preselect player: " + player.getLogin());
		// check user is present in list, if not (not invitable), add first
		if(!((GenericListModel<String, User>) allPlayersLI.getModel()).containsKey(player.getLoginLowerCase()))
		{
			logger.warn("player not present in list: " + player.getLogin() + " -> adding");
			((GenericListModel<String, User>) allPlayersLI.getModel()).addElement(player.getLoginLowerCase(), player);
			allPlayersLI.setModel(allPlayersLI.getModel());
		}
		// select player, then add
		allPlayersLI.setSelectedValue(player, false);
		actionPerformed(new ActionEvent(this, 0, "add" + teamIndex));
	}

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
				
				// make sure to shift selection after removing items so we don't end up with index out of bounds on next action
				UIUtil.fixSelection(allPlayersLI);
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
				
				// make sure to shift selection after removing items so we don't end up with index out of bounds on next action
				UIUtil.fixSelection(teamLI);
			}

			if(this.autoNameTeams && oldNameGenerated)
			{
				teamNameTFList.get(teamIndex).setText(createTeamName(teamIndex));
			}

			repaint();
		}
	}

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
