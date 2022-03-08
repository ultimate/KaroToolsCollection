package ultimate.karomuskel.ui.screens;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

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

public class KOWinnersScreen extends Screen implements ActionListener
{
	private static final long	serialVersionUID	= 1L;

	private boolean[]			winners;

	public KOWinnersScreen(JFrame gui, Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(gui, previous, karoAPICache, previousButton, nextButton, "screen.kowinners.header", "screen.kowinners.next");
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

	@Override
	public GameSeries applySettings(GameSeries gameSeries, EnumNavigation direction) throws GameSeriesException
	{
		if(GameSeriesManager.isTeamBased(gameSeries))
		{
			int previousRound = (int) gameSeries.get(GameSeries.CURRENT_ROUND);
			int round = previousRound / 2;
			gameSeries.set(GameSeries.CURRENT_ROUND, round);
			
			int teamsBefore = gameSeries.getTeams().size();
			List<Team> teams = gameSeries.getTeams();

			List<Team> winnerTeams = new LinkedList<Team>();
			for(int i = 0; i < this.winners.length; i++)
			{
				if(this.winners[i])
				{
					winnerTeams.add(teams.get(i));
				}
			}
			if(winnerTeams.size() != teamsBefore / 2)
				throw new GameSeriesException("screen.kowinners.notenoughwinners");

			gameSeries.setTeams(winnerTeams);
			gameSeries.set(GameSeries.SHUFFLE_TEAMS, false);
			gameSeries.set(GameSeries.NUMBER_OF_TEAMS, winnerTeams.size());
		}
		else if(gameSeries.getType() == EnumGameSeriesType.KLC)
		{
			int previousRound = (int) gameSeries.get(GameSeries.CURRENT_ROUND);
			int round = previousRound / 2;
			gameSeries.set(GameSeries.CURRENT_ROUND, round);

			List<User> players = gameSeries.getPlayersByKey().get(GameSeries.KEY_ROUND + previousRound);

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
			gameSeries.getPlayersByKey().get(GameSeries.KEY_ROUND + round).clear();
			gameSeries.getPlayersByKey().get(GameSeries.KEY_ROUND + round).addAll(winnerPlayers);
		}
		return gameSeries;
	}

	@Override
	public void updateBeforeShow(GameSeries gameSeries, EnumNavigation direction)
	{
		if(direction == EnumNavigation.previous)
		{
			int round = (int) gameSeries.get(GameSeries.CURRENT_ROUND);
			int previousRound = round * 2;
			gameSeries.set(GameSeries.CURRENT_ROUND, previousRound);
		}
		
		if(this.firstShow)
		{
			this.firstShow = false;
			int numBefore = 0;
			List<String> names = null;
			if(GameSeriesManager.isTeamBased(gameSeries))
			{
				numBefore = gameSeries.getTeams().size();
				names = new ArrayList<String>(gameSeries.getTeams().size());
				for(Team t : gameSeries.getTeams())
					names.add(t.getName());
			}
			else if(gameSeries.getType() == EnumGameSeriesType.KLC)
			{
				int round = (int) gameSeries.get(GameSeries.CURRENT_ROUND);
				numBefore = round * 2;
				names = new ArrayList<String>(numBefore);
				for(User p : gameSeries.getPlayersByKey().get(GameSeries.KEY_ROUND + numBefore))
					names.add(p.getLogin());
			}
			this.winners = new boolean[numBefore];

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
				buttonGroup.add(radioButton);
				gbc.gridx = 0;
				contentPanel.add(radioButton, gbc);

				vsLabel = new JLabel(Language.getString("screen.kowinners.versus"));
				gbc.gridx = 1;
				contentPanel.add(vsLabel, gbc);

				radioButton = new JRadioButton(names.get(i + 1));
				radioButton.setActionCommand("" + (i + 1));
				radioButton.addActionListener(this);
				radioButton.setHorizontalTextPosition(SwingConstants.LEFT);
				setIcons(radioButton, 30);
				buttonGroup.add(radioButton);
				gbc.gridx = 2;
				contentPanel.add(radioButton, gbc);
			}
		}
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
