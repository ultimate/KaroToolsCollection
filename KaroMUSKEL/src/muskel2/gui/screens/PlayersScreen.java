package muskel2.gui.screens;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

import muskel2.core.exceptions.GameSeriesException;
import muskel2.gui.Screen;
import muskel2.model.GameSeries;
import muskel2.model.Karopapier;
import muskel2.model.Player;
import muskel2.model.help.GenericListModel;
import muskel2.model.help.Team;
import muskel2.model.series.BalancedGameSeries;
import muskel2.model.series.KOGameSeries;
import muskel2.model.series.LeagueGameSeries;
import muskel2.model.series.SimpleGameSeries;
import muskel2.model.series.TeamBasedGameSeries;
import muskel2.util.Language;

public class PlayersScreen extends Screen implements ActionListener
{
	private static final long		serialVersionUID	= 1L;

	private static final int		listFixedCellWidth	= 250;

	private JList					allPlayersLI;
	private TreeMap<String, Player>	allPlayers;

	private List<JList>				teamLIList;
	private List<JTextField>		teamNameTFList;
	private List<JButton>			addButtonList;
	private List<JButton>			removeButtonList;

	private boolean					firstCall			= true;
	private int						teams;
	private int						maxPlayersPerTeam;
	private Boolean					ignoreInvitable;

	private boolean					autoNameTeams		= false;
	private boolean					multipleTeams		= false;

	public PlayersScreen(Screen previous, Karopapier karopapier, JButton previousButton, JButton nextButton)
	{
		super(previous, karopapier, previousButton, nextButton, "screen.players.header", "screen.players.next");

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

	@SuppressWarnings("unchecked")
	@Override
	public GameSeries applySettings(GameSeries gameSeries) throws GameSeriesException
	{
		if(gameSeries instanceof TeamBasedGameSeries)
		{
			Team team;
			List<Player> playerList;
			((TeamBasedGameSeries) gameSeries).getTeams().clear();
			for(int i = 0; i < this.teams; i++)
			{
				String teamName = this.teamNameTFList.get(i).getText();
				playerList = new LinkedList<Player>();
				Object[] players = ((GenericListModel<String, Player>) this.teamLIList.get(i).getModel()).getEntryArray();
				if(players.length < ((TeamBasedGameSeries) gameSeries).getMinPlayersPerTeam())
					throw new GameSeriesException("screen.players.minplayersperteam", teamName);
				if(players.length > ((TeamBasedGameSeries) gameSeries).getMaxPlayersPerTeam())
					throw new GameSeriesException("screen.players.maxplayersperteam", teamName);
				for(Object player : players)
				{
					playerList.add((Player) player);
				}
				team = new Team(teamName, playerList);
				((TeamBasedGameSeries) gameSeries).getTeams().add(team);
			}
		}
		else if(gameSeries instanceof SimpleGameSeries)
		{
			Object[] players = ((GenericListModel<String, Player>) this.teamLIList.get(0).getModel()).getEntryArray();
			if(players.length < ((SimpleGameSeries) gameSeries).getMinPlayersPerGame()-1)
				throw new GameSeriesException("screen.players.notenoughplayers");
			gameSeries.getPlayers().clear();
			for(Object player : players)
			{
				gameSeries.getPlayers().add((Player) player);
			}
		}
		else if(gameSeries instanceof BalancedGameSeries)
		{
			Object[] players = ((GenericListModel<String, Player>) this.teamLIList.get(0).getModel()).getEntryArray();
			gameSeries.getPlayers().clear();
			for(Object player : players)
			{
				gameSeries.getPlayers().add((Player) player);
			}
		}
		return gameSeries;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateBeforeShow(GameSeries gameSeries)
	{
		int teamsTmp = 1;
		int maxPlayersPerTeamTmp = 25;
		boolean multipleTeamsTmp = false;
		if(gameSeries instanceof TeamBasedGameSeries)
		{
			teamsTmp = ((TeamBasedGameSeries) gameSeries).getNumberOfTeams();
			maxPlayersPerTeamTmp = ((TeamBasedGameSeries) gameSeries).getMaxPlayersPerTeam();
			this.autoNameTeams = ((TeamBasedGameSeries) gameSeries).isAutoNameTeams();
			multipleTeamsTmp = ((TeamBasedGameSeries) gameSeries).isMultipleTeams();
		}

		boolean changed = false;

		if(this.firstCall)
		{
			this.firstCall = false;
			this.teamLIList = new LinkedList<JList>();
			this.teamNameTFList = new LinkedList<JTextField>();
			this.addButtonList = new LinkedList<JButton>();
			this.removeButtonList = new LinkedList<JButton>();

			this.teams = teamsTmp;
			this.maxPlayersPerTeam = maxPlayersPerTeamTmp;

			this.removeAll();

			JPanel allPlayersPanel = new JPanel();
			allPlayersPanel.setLayout(new BorderLayout(5, 5));
			this.add(allPlayersPanel);

			this.allPlayersLI = new JList();
			this.allPlayersLI.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			this.allPlayersLI.setFixedCellWidth(listFixedCellWidth);
			JScrollPane allMapsSP = new JScrollPane(this.allPlayersLI, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			allPlayersPanel.add(new JLabel(Language.getString("screen.players.allplayers")), BorderLayout.NORTH);
			allPlayersPanel.add(allMapsSP, BorderLayout.CENTER);

			JList teamPlayersLI;
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

				teamPlayersLI = new JList(new GenericListModel<String, Player>(new HashMap<String, Player>()));
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
				JScrollPane innerTeamsSP = new JScrollPane(innerTeamsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
						JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

				outerTeamsPanel.add(new JLabel(Language.getString("screen.players.selectedplayers")), BorderLayout.NORTH);
				outerTeamsPanel.add(innerTeamsSP, BorderLayout.CENTER);

				int maxTeams = 32;
				if(gameSeries instanceof KOGameSeries)
					maxTeams = KOGameSeries.MAX_TEAMS;
				else if(gameSeries instanceof LeagueGameSeries)
					maxTeams = LeagueGameSeries.MAX_TEAMS;

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
					teamNameTF.setEnabled(enabled);

					teamPlayersLI = new JList(new GenericListModel<String, Player>(new HashMap<String, Player>()));
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
			JList teamPlayersLI;
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
				teamNameTF.setEnabled(i < this.teams);
				addButton.setEnabled(i < this.teams);
				removeButton.setEnabled(i < this.teams);

				if(i >= this.teams)
				{
					// remove all entries
					Object[] players = ((GenericListModel<String, Player>) teamPlayersLI.getModel()).getEntryArray();
					Player player;
					String key;
					for(Object o : players)
					{
						player = (Player) o;
						key = player.getName().toLowerCase();
						((GenericListModel<String, Player>) teamPlayersLI.getModel()).removeElement(key);
						teamPlayersLI.setModel(teamPlayersLI.getModel());
						((GenericListModel<String, Player>) this.allPlayersLI.getModel()).addElement(key, player);
						this.allPlayersLI.setModel(this.allPlayersLI.getModel());
					}
				}
			}
			changed = true;
		}
		else if(this.maxPlayersPerTeam != maxPlayersPerTeamTmp)
		{
			this.maxPlayersPerTeam = maxPlayersPerTeamTmp;
			for(JList teamPlayersLI : this.teamLIList)
			{
				teamPlayersLI.setVisibleRowCount(this.maxPlayersPerTeam);
			}
		}

		boolean ignoreInvitableTmp = gameSeries.getRules().isIgnoreInvitable();

		if(this.ignoreInvitable == null || this.ignoreInvitable != ignoreInvitableTmp || this.multipleTeams != multipleTeamsTmp)
		{
			changed = true;
			this.ignoreInvitable = ignoreInvitableTmp;
			this.multipleTeams = multipleTeamsTmp;
			this.allPlayers = new TreeMap<String, Player>(gameSeries.getKaropapier().getPlayers());

			if(!this.ignoreInvitable)
			{
				for(JList teamPlayersLI : this.teamLIList)
				{
					// remove uninvitable players
					Object[] players = ((GenericListModel<String, Player>) teamPlayersLI.getModel()).getEntryArray();
					Player player;
					String key;
					for(Object o : players)
					{
						player = (Player) o;
						if(!this.ignoreInvitable && !player.isInvitable(false))
						{
							key = player.getName().toLowerCase();
							((GenericListModel<String, Player>) teamPlayersLI.getModel()).removeElement(key);
							teamPlayersLI.setModel(teamPlayersLI.getModel());
							((GenericListModel<String, Player>) allPlayersLI.getModel()).addElement(key, player);
							allPlayersLI.setModel(allPlayersLI.getModel());
						}
					}
				}
				List<String> removeList = new LinkedList<String>();
				Player player;
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
		if(gameSeries instanceof TeamBasedGameSeries)
		{
			String key = gameSeries.getCreator().getName().toLowerCase();
			if(((TeamBasedGameSeries) gameSeries).isCreatorTeam())
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
			TreeMap<String, Player> allPlayersTmp = new TreeMap<String, Player>(this.allPlayers);
			JList teamPlayersLI;
			if(!this.multipleTeams)
			{
				for(int i = 0; i < this.teamLIList.size(); i++)
				{
					teamPlayersLI = this.teamLIList.get(i);
	
					Object[] players = ((GenericListModel<String, Player>) teamPlayersLI.getModel()).getEntryArray();
					Player player;
					String key;
					for(Object o : players)
					{
						player = (Player) o;
						key = player.getName().toLowerCase();
						allPlayersTmp.remove(key);
					}
				}
			}
			this.allPlayersLI.setModel(new GenericListModel<String, Player>(allPlayersTmp));
		}
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

			JList teamLI = teamLIList.get(teamIndex);

			if(add)
			{
				Object[] players = allPlayersLI.getSelectedValues();
				Player player;
				String key;
				for(Object o : players)
				{
					player = (Player) o;
					key = player.getName().toLowerCase();
					if(!this.multipleTeams)
						((GenericListModel<String, Player>) allPlayersLI.getModel()).removeElement(key);
					((GenericListModel<String, Player>) teamLI.getModel()).addElement(key, player);
				}
			}
			else
			{
				Object[] players = teamLI.getSelectedValues();
				Player player;
				String key;
				for(Object o : players)
				{
					player = (Player) o;
					key = player.getName().toLowerCase();
					((GenericListModel<String, Player>) teamLI.getModel()).removeElement(key);
					if(!this.multipleTeams)
						((GenericListModel<String, Player>) allPlayersLI.getModel()).addElement(key, player);
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
		Object[] teamPlayers = ((GenericListModel<String, Player>) teamLIList.get(teamIndex).getModel()).getEntryArray();
		StringBuilder tmp = new StringBuilder();
		for(int p = 0; p < teamPlayers.length; p++)
		{
			if((p != 0) && (p != teamPlayers.length - 1))
				tmp.append(", ");
			else if((p != 0) && (p == teamPlayers.length - 1))
				tmp.append(" und ");
			tmp.append(((Player) teamPlayers[p]).getName());
		}
		return tmp.toString();
	}
}
