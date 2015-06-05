package muskel2.gui.screens;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import muskel2.model.help.Team;
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

		return gameSeries;
	}

	@Override
	public void updateBeforeShow(GameSeries gameSeries)
	{
		if(this.firstCall)
		{
			this.firstCall = false;
			int teamsBefore = ((TeamBasedGameSeries) gameSeries).getTeams().size();
			List<Team> teams = ((TeamBasedGameSeries) gameSeries).getTeams();
			this.winners = new boolean[teamsBefore];

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
			for(int i = 0; i < teamsBefore; i = i + 2)
			{
				gbc.gridy = i;
				
				buttonGroup = new ButtonGroup();
				
				radioButton = new JRadioButton(teams.get(i).getName());
				radioButton.setActionCommand("" + i);
				radioButton.addActionListener(this);
				setIcons(radioButton, 30);
				buttonGroup.add(radioButton);
				gbc.gridx = 0;
				contentPanel.add(radioButton, gbc);
				
				vsLabel = new JLabel(Language.getString("screen.kowinners.versus"));
				gbc.gridx = 1;
				contentPanel.add(vsLabel, gbc);

				radioButton = new JRadioButton(teams.get(i+1).getName());
				radioButton.setActionCommand("" + (i+1));
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
