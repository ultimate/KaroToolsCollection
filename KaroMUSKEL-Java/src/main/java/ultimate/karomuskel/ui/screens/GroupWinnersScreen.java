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

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.exceptions.GameSeriesException;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.official.User;
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.Screen;

public class GroupWinnersScreen extends Screen implements ActionListener
{
	private static final long	serialVersionUID	= 1L;

	private JList<String>[]				groupLists;
	// TODO IDEA make numberOfWinnersPerGroup selectable
	@SuppressWarnings("unused")
	private JSpinner			numberOfWinnersPerGroupSpinner;

	private boolean				firstCall			= true;

	public GroupWinnersScreen(Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(previous, karoAPICache, previousButton, nextButton, "screen.groupwinners.header", "screen.groupwinners.next");
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

	@Override
	public GameSeries applySettings(GameSeries gameSeries) throws GameSeriesException
	{
		if(GameSeriesManager.isTeamBased(gameSeries))
		{
			// TODO IDEA for future use
		}
		else if(gameSeries.getType() == EnumGameSeriesType.KLC)
		{
			List<User> winnerPlayers = new LinkedList<User>();

			int groups = GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_KLC_GROUPS);
			int firstKORound = GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_KLC_FIRST_KO_ROUND);
			// TODO IDEA make numberOfWinnersPerGroup selectable
			int winnersPerGroup = firstKORound / groups;
			for(int g = 1; g <= groups; g++)
			{
				DefaultListModel<String> model = (DefaultListModel<String>) groupLists[g - 1].getModel();
				String name;
				User player;
				
				for(int i = 0; i < winnersPerGroup; i++)
				{
					name = model.get(i);
					player = null;
					for(User p: gameSeries.getPlayersByKey().get(GameSeries.KEY_GROUP + g))
					{
						if(p.getLogin().equalsIgnoreCase(name))
						{
							player = p;
							break;
						}
					}
					winnerPlayers.add(player);
				}
			}
			
			int round = (int) gameSeries.get(GameSeries.CURRENT_ROUND);
			gameSeries.getPlayersByKey().get(GameSeries.KEY_ROUND + round).addAll(winnerPlayers);
		}
		return gameSeries;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateBeforeShow(GameSeries gameSeries)
	{
		if(this.firstCall)
		{
			this.firstCall = false;
			int groups = 0;
			List<List<String>> names = null;
			List<String> namesInGroup;
			if(GameSeriesManager.isTeamBased(gameSeries))
			{
				// TODO IDEA for future use
			}
			else if(gameSeries.getType() == EnumGameSeriesType.KLC)
			{
				groups = GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_KLC_GROUPS);
				names = new ArrayList<List<String>>(groups);
				for(int g = 1; g <= groups; g++)
				{
					namesInGroup = new ArrayList<String>(gameSeries.getPlayersByKey().get(GameSeries.KEY_GROUP + g).size());
					for(User p : gameSeries.getPlayersByKey().get(GameSeries.KEY_GROUP + g))
						namesInGroup.add(p.getLogin());
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

			// TODO IDEA make numberOfWinnersPerGroup selectable

			groupLists = (JList<String>[]) new JList<?>[groups];

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

				groupLists[i] = new JList<>(model);
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
