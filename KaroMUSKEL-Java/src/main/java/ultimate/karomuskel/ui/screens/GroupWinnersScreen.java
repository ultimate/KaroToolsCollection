package ultimate.karomuskel.ui.screens;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;

import muskel2.model.GameSeries;
import muskel2.model.Player;
import muskel2.model.series.KLCGameSeries;
import muskel2.model.series.TeamBasedGameSeries;
import ultimate.karoapi4j.exceptions.GameSeriesException;
import ultimate.karomuskel.KaroAPICache;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.Screen;

public class GroupWinnersScreen extends Screen implements ActionListener
{
	private static final long	serialVersionUID	= 1L;

	private JList[]				groupLists;
	private JSpinner			numberOfWinnersPerGroupSpinner;

	private boolean				firstCall			= true;

	public GroupWinnersScreen(Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(previous, karoAPICache, previousButton, nextButton, "screen.groupwinners.header", "screen.groupwinners.next");
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

	@SuppressWarnings("unchecked")
	@Override
	public GameSeries applySettings(GameSeries gameSeries) throws GameSeriesException
	{
		if(gameSeries instanceof TeamBasedGameSeries)
		{
			// TODO not implemented
			// List<Team> winnerTeams = new LinkedList<Team>();
			//
			// for(int g = 1; g <= KLCGameSeries.GROUPS; g++)
			// {
			// for(int i = 0; i < (Integer) numberOfWinnersPerGroupSpinner.getValue(); i++)
			// {
			// winnerTeams.add(((KLCGameSeries) gameSeries).getTeamsGroupX(g).get(i));
			// }
			// }
			//
			// ((TeamBasedGameSeries) gameSeries).getTeamsRoundX(((TeamBasedGameSeries)
			// gameSeries).getRound()).addAll(winnerTeams);
		}
		else if(gameSeries instanceof KLCGameSeries)
		{
			List<Player> winnerPlayers = new LinkedList<Player>();

			for(int g = 1; g <= KLCGameSeries.GROUPS; g++)
			{
				DefaultListModel<String> model = (DefaultListModel<String>) groupLists[g - 1].getModel();
				String name;
				Player player;
				for(int i = 0; i < KLCGameSeries.WINNERS_PER_GROUP; i++)
				{
					name = model.get(i);
					player = null;
					for(Player p: ((KLCGameSeries) gameSeries).getPlayersGroupX(g))
					{
						if(p.getName().equalsIgnoreCase(name))
						{
							player = p;
							break;
						}
					}
					winnerPlayers.add(player);
				}
			}

			((KLCGameSeries) gameSeries).getPlayersRoundOfX(((KLCGameSeries) gameSeries).getRound()).addAll(winnerPlayers);
		}
		return gameSeries;
	}

	@Override
	public void updateBeforeShow(GameSeries gameSeries)
	{
		if(this.firstCall)
		{
			this.firstCall = false;
			int groups = 0;
			List<List<String>> names = null;
			List<String> namesInGroup;
			if(gameSeries instanceof TeamBasedGameSeries)
			{
				// TODO not implemented
				// groups = ((TeamBasedGameSeries) gameSeries).getTeams().size();
			}
			else if(gameSeries instanceof KLCGameSeries)
			{
				groups = KLCGameSeries.GROUPS;
				names = new ArrayList<List<String>>(groups);
				for(int g = 0; g < groups; g++)
				{
					namesInGroup = new ArrayList<String>(((KLCGameSeries) gameSeries).getPlayersGroupX(g + 1).size());
					for(Player p : ((KLCGameSeries) gameSeries).getPlayersGroupX(g + 1))
						namesInGroup.add(p.getName());
					names.add(namesInGroup);
				}
			}

			JPanel contentPanel = new JPanel();
			JScrollPane contentSP = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			this.add(contentSP);

			GridBagLayout layout = new GridBagLayout();
			contentPanel.setLayout(layout);

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			
			// TODO numberOfWinnersPerGroup

			groupLists = new JList[groups];

			JLabel groupLabel;
			JButton upButton, downButton;
			for(int i = 0; i < groups; i++)
			{
				gbc.gridy = i * 2;

				groupLabel = new JLabel(Language.getString("screen.groupwinners.groupX").replace("%X", "" + (i + 1)));
				gbc.gridx = 0;
				gbc.gridheight = 2;
				contentPanel.add(groupLabel, gbc);
				
				DefaultListModel<String> model = new DefaultListModel<String>();
				for(String name: names.get(i))
					model.addElement(name);

				groupLists[i] = new JList<String>(model);
				groupLists[i].setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
				gbc.gridx = 1;
				gbc.gridheight = 2;
				contentPanel.add(groupLists[i], gbc);

				upButton = new JButton(Language.getString("screen.groupwinners.up"));
				upButton.setActionCommand("up_" + (i + 1));
				upButton.addActionListener(this);
				gbc.gridx = 2;
				gbc.gridheight = 1;
				contentPanel.add(upButton, gbc);

				downButton = new JButton(Language.getString("screen.groupwinners.down"));
				downButton.setActionCommand("down_" + (i + 1));
				downButton.addActionListener(this);
				gbc.gridx = 2;
				gbc.gridy++;
				gbc.gridheight = 1;
				contentPanel.add(downButton, gbc);

			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e)
	{
		// sort list of players per group according to box
		int group = Integer.parseInt(e.getActionCommand().substring(e.getActionCommand().indexOf("_") + 1));
		if(e.getActionCommand().startsWith("up"))
		{
			int[] indices = groupLists[group - 1].getSelectedIndices();
			DefaultListModel<String> model = (DefaultListModel<String>) groupLists[group - 1].getModel();
			for(int si = 0; si < indices.length; si++)
			{
				int index = indices[si];
				if(index == 0)
					break;
				String tmp = model.get(index - 1);
				model.set(index - 1, model.get(index));
				model.set(index, tmp);
				indices[si]--;
			}
			groupLists[group - 1].setSelectedIndices(indices);
		}
		else if(e.getActionCommand().startsWith("down"))
		{
			int[] indices = groupLists[group - 1].getSelectedIndices();
			DefaultListModel<String> model = (DefaultListModel<String>) groupLists[group - 1].getModel();
			for(int si = indices.length-1; si >= 0; si--)
			{
				int index = indices[si];
				if(index == model.getSize() - 1)
					break;
				String tmp = model.get(index + 1);
				model.set(index + 1, model.get(index));
				model.set(index, tmp);
				indices[si]++;
			}
			groupLists[group - 1].setSelectedIndices(indices);
		}
	}
}
