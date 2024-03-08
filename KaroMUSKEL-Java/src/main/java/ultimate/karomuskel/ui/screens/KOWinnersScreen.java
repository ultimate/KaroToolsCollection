package ultimate.karomuskel.ui.screens;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.exceptions.GameSeriesException;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.Team;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.ui.EnumNavigation;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.MainFrame;
import ultimate.karomuskel.ui.Screen;

public class KOWinnersScreen extends Screen implements ActionListener
{
	private static final long	serialVersionUID	= 1L;

	private boolean[]			winners;

	private List<JRadioButton>	buttonList;
	private List<ButtonGroup>	buttonGroupList;

	public KOWinnersScreen(MainFrame gui, Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(gui, previous, karoAPICache, previousButton, nextButton, "screen.kowinners.header");
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

	@Override
	public String getNextKey()
	{
		return "screen.kowinners.next";
	}

	@Override
	public GameSeries applySettings(GameSeries gameSeries, EnumNavigation direction) throws GameSeriesException
	{
		if(direction == EnumNavigation.next)
		{
			int previousRound = (int) gameSeries.get(GameSeries.CURRENT_ROUND);
			int round = previousRound / 2;
			gameSeries.set(GameSeries.CURRENT_ROUND, round);

			if(GameSeriesManager.isTeamBased(gameSeries))
			{
				List<Team> teamList;
				if(gameSeries.getType() == EnumGameSeriesType.KO)
					teamList = gameSeries.getTeamsByKey().get(GameSeries.KEY_ROUND + previousRound);
				else
					teamList = gameSeries.getTeams();

				List<Team> winnerTeams = new LinkedList<Team>();
				for(int i = 0; i < this.winners.length; i++)
				{
					if(this.winners[i])
					{
						winnerTeams.add(teamList.get(i));
					}
				}
				if(winnerTeams.size() != round)
					throw new GameSeriesException("screen.kowinners.notenoughwinners");

				if(gameSeries.getTeamsByKey().get(GameSeries.KEY_ROUND + round) == null)
					gameSeries.getTeamsByKey().put(GameSeries.KEY_ROUND + round, new ArrayList<>(round));
				else
					gameSeries.getTeamsByKey().get(GameSeries.KEY_ROUND + round).clear();
				gameSeries.getTeamsByKey().get(GameSeries.KEY_ROUND + round).addAll(winnerTeams);
				gameSeries.set(GameSeries.SHUFFLE_TEAMS, false);
			}
			else if(gameSeries.getType() == EnumGameSeriesType.KLC)
			{
				List<User> players = new ArrayList<User>(previousRound);
				// see issue #131:
				// we cannot just take the players from the "roundOfX" list, since they have been shuffled on creation
				// (players are converted to temporary teams and they are shuffled, but the shuffling is not reflected back to the player list)
				// hence we need to take the players from the actually created games instead
				for(PlannedGame g : gameSeries.getGames().get(gameSeries.getType().toString() + "." + GameSeries.KEY_ROUND + previousRound))
				{
					// add the 2 players from each match (but without the creator)
					if(g.getPlayers().size() > 2)
						g.getPlayers().forEach(u -> {
							if(u != gameSeries.getCreator())
								players.add(u);
						});
					else // the creator is participating, too
						players.addAll(g.getPlayers());
				}

				List<User> winnerPlayers = new LinkedList<User>();
				for(int i = 0; i < this.winners.length; i++)
				{
					if(this.winners[i])
					{
						winnerPlayers.add(players.get(i));
					}
				}
				if(winnerPlayers.size() != round)
					throw new GameSeriesException("screen.kowinners.notenoughwinners");

				if(gameSeries.getPlayersByKey().get(GameSeries.KEY_ROUND + round) == null)
					gameSeries.getPlayersByKey().put(GameSeries.KEY_ROUND + round, new ArrayList<>(round));
				else
					gameSeries.getPlayersByKey().get(GameSeries.KEY_ROUND + round).clear();
				gameSeries.getPlayersByKey().get(GameSeries.KEY_ROUND + round).addAll(winnerPlayers);
			}
		}

		return gameSeries;
	}

	@Override
	public Message updateBeforeShow(GameSeries gameSeries, EnumNavigation direction)
	{
		if(direction == EnumNavigation.previous)
		{
			int round = (int) gameSeries.get(GameSeries.CURRENT_ROUND);
			int previousRound = round * 2;
			if(gameSeries.getType() == EnumGameSeriesType.KLC)
			{
				int leagues = GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_KLC_LEAGUES);
				List<User> allPlayers = new LinkedList<>();
				// Ligen durchmischen & zur Liste hinzufügen
				List<User> leaguePlayers;
				for(int l = 1; l <= leagues; l++)
				{
					leaguePlayers = gameSeries.getPlayersByKey().get(GameSeries.KEY_LEAGUE + l);
					allPlayers.addAll(leaguePlayers);
				}
				if(allPlayers.size() < previousRound)
					previousRound = allPlayers.size();
			}
			gameSeries.set(GameSeries.CURRENT_ROUND, previousRound);
		}

		if(this.firstShow)
		{
			int numBefore = (int) gameSeries.get(GameSeries.CURRENT_ROUND);
			List<String> names = new ArrayList<String>(numBefore);
			if(GameSeriesManager.isTeamBased(gameSeries))
			{
				for(Team t : gameSeries.getTeamsByKey().get(GameSeries.KEY_ROUND + numBefore))
					names.add(t.getName());
			}
			else if(gameSeries.getType() == EnumGameSeriesType.KLC)
			{
				// see issue #131:
				// we cannot just take the players from the "roundOfX" list, since they have been shuffled on creation
				// (players are converted to temporary teams and they are shuffled, but the shuffling is not reflected back to the player list)
				// hence we need to take the players from the actually created games instead
				for(PlannedGame g : gameSeries.getGames().get(gameSeries.getType().toString() + "." + GameSeries.KEY_ROUND + numBefore))
				{
					// add the 2 players from each match (but without the creator)
					if(g.getPlayers().size() > 2) // creator is a separate player
						g.getPlayers().forEach(u -> {
							if(u != gameSeries.getCreator())
								names.add(u.getLogin());
						});
					else // the creator is participating in KLC, too
						g.getPlayers().forEach(u -> {
							names.add(u.getLogin());
						});
				}
			}

			this.winners = new boolean[numBefore];
			this.buttonList = new ArrayList<>(numBefore);
			this.buttonGroupList = new ArrayList<>(numBefore / 2);

			JPanel contentPanel = new JPanel();
			JScrollPane contentSP = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			this.add(contentSP);

			GridBagLayout layout = new GridBagLayout();
			contentPanel.setLayout(layout);

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.fill = GridBagConstraints.HORIZONTAL;

			ButtonGroup buttonGroup;
			JRadioButton radioButton;
			JLabel vsLabel;
			for(int i = 0; i < numBefore; i = i + 2)
			{
				gbc.gridy = i;

				buttonGroup = new ButtonGroup();

				radioButton = new JRadioButton(names.get(i));
				radioButton.setActionCommand("" + i);
				radioButton.addActionListener(this);
				setIcons(radioButton, 30);
				buttonList.add(radioButton);
				buttonGroup.add(radioButton);
				gbc.gridx = 0;
				contentPanel.add(radioButton, gbc);

				vsLabel = new JLabel(Language.getString("screen.kowinners.versus"));
				gbc.gridx = 1;
				contentPanel.add(vsLabel, gbc);

				radioButton = new JRadioButton(names.get(i + 1));
				radioButton.setActionCommand("" + (i + 1));
				radioButton.addActionListener(this);
				setIcons(radioButton, 30);
				buttonList.add(radioButton);
				buttonGroup.add(radioButton);
				gbc.gridx = 2;
				contentPanel.add(radioButton, gbc);

				buttonGroupList.add(buttonGroup);
			}

			// preselect values from gameseries
			int nextRound = numBefore / 2;
			List<String> namesNextRound = new ArrayList<String>(nextRound);
			if(GameSeriesManager.isTeamBased(gameSeries))
			{
				List<Team> teamList;
				if(gameSeries.getType() == EnumGameSeriesType.KO)
					teamList = gameSeries.getTeamsByKey().get(GameSeries.KEY_ROUND + nextRound);
				else
					teamList = gameSeries.getTeams();
				if(teamList != null)
					for(Team t : teamList)
						namesNextRound.add(t.getName());
			}
			else if(gameSeries.getType() == EnumGameSeriesType.KLC)
			{
				if(gameSeries.getPlayersByKey().containsKey(GameSeries.KEY_ROUND + nextRound))
					for(User p : gameSeries.getPlayersByKey().get(GameSeries.KEY_ROUND + nextRound))
						namesNextRound.add(p.getLogin());
			}

			ButtonGroup bg;
			Enumeration<AbstractButton> bgButtons;
			AbstractButton b;
			for(String name : namesNextRound)
			{
				for(int i = 0; i < numBefore / 2; i++)
				{
					bg = buttonGroupList.get(i);
					bgButtons = bg.getElements();
					int bi = 0;
					while(bgButtons.hasMoreElements())
					{
						b = bgButtons.nextElement();
						if(b.getText().equals(name))
						{
							b.setSelected(true);
							actionPerformed(new ActionEvent(b, i, "" + (i * 2 + bi)));
						}
						bi++;
					}
				}
			}
		}
		this.firstShow = false;
		
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		int otherIndex;
		if(index % 2 == 0)
			otherIndex = index + 1;
		else
			otherIndex = index - 1;

		this.winners[index] = true;
		this.winners[otherIndex] = false;
	}
}
