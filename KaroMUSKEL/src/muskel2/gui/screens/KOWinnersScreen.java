package muskel2.gui.screens;

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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import muskel2.core.exceptions.GameSeriesException;
import muskel2.gui.Screen;
import muskel2.model.GameSeries;
import muskel2.model.Karopapier;
import muskel2.model.Player;
import muskel2.model.help.Team;
import muskel2.model.series.KLCGameSeries;
import muskel2.model.series.KOGameSeries;
import muskel2.model.series.TeamBasedGameSeries;
import muskel2.util.Language;

public class KOWinnersScreen extends Screen implements ActionListener
{
	private static final long	serialVersionUID	= 1L;

	private boolean[]			winners;

	private boolean				firstCall			= true;

	public KOWinnersScreen(Screen previous, Karopapier karopapier, JButton previousButton, JButton nextButton)
	{
		super(previous, karopapier, previousButton, nextButton, "screen.kowinners.header", "screen.kowinners.next");
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

	@Override
	public GameSeries applySettings(GameSeries gameSeries) throws GameSeriesException
	{
		if(gameSeries instanceof TeamBasedGameSeries)
		{
			int teamsBefore = ((TeamBasedGameSeries) gameSeries).getTeams().size();
			List<Team> teams = ((TeamBasedGameSeries) gameSeries).getTeams();

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

			((KOGameSeries) gameSeries).setTeams(winnerTeams);
			((KOGameSeries) gameSeries).setShuffleTeams(false);
			((KOGameSeries) gameSeries).setNumberOfTeams(winnerTeams.size());
		}
		else if(gameSeries instanceof KLCGameSeries)
		{
			int playersBefore = ((KLCGameSeries) gameSeries).getRound() * 2;
			List<Player> players = ((KLCGameSeries) gameSeries).getPlayersRoundOfX(playersBefore);

			List<Player> winnerPlayers = new LinkedList<Player>();
			for(int i = 0; i < this.winners.length; i++)
			{
				if(this.winners[i])
				{
					winnerPlayers.add(players.get(i));
				}
			}
			if(winnerPlayers.size() != ((KLCGameSeries) gameSeries).getRound())
				throw new GameSeriesException("screen.kowinners.notenoughwinners");

			((KLCGameSeries) gameSeries).getPlayersGroupX(((KLCGameSeries) gameSeries).getRound()).addAll(winnerPlayers);
		}
		return gameSeries;
	}

	@Override
	public void updateBeforeShow(GameSeries gameSeries)
	{
		if(this.firstCall)
		{
			this.firstCall = false;
			int numBefore = 0;
			List<String> names = null;
			if(gameSeries instanceof TeamBasedGameSeries)
			{
				numBefore = ((TeamBasedGameSeries) gameSeries).getTeams().size();
				names = new ArrayList<String>(((TeamBasedGameSeries) gameSeries).getTeams().size());
				for(Team t: ((TeamBasedGameSeries) gameSeries).getTeams())
				{
					names.add(t.getName());
				}
			}
			else if(gameSeries instanceof KLCGameSeries)
			{
				numBefore = ((KLCGameSeries) gameSeries).getRound() * 2;
				names = new ArrayList<String>(numBefore);
				for(Player p: ((KLCGameSeries) gameSeries).getPlayersRoundOfX(((KLCGameSeries) gameSeries).getRound()))
				{
					names.add(p.getName());
				}
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
