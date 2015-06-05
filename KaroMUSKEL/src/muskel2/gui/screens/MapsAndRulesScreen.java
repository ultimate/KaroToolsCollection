package muskel2.gui.screens;

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
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import muskel2.core.exceptions.GameSeriesException;
import muskel2.gui.Screen;
import muskel2.gui.help.MapRenderer;
import muskel2.model.Direction;
import muskel2.model.GameSeries;
import muskel2.model.Karopapier;
import muskel2.model.Map;
import muskel2.model.Rules;
import muskel2.model.help.BooleanModel;
import muskel2.model.help.DirectionModel;
import muskel2.model.help.Label;
import muskel2.model.series.BalancedGameSeries;
import muskel2.util.Language;

public class MapsAndRulesScreen extends Screen implements ActionListener
{
	private static final long	serialVersionUID	= 1L;

	private List<JComboBox>		mapCBList;
	private List<JSpinner>		gamesPerPlayerSpinnerList;
	private List<JSpinner>		numberOfPlayersSpinnerList;
	private List<JSpinner>		minZzzSpinnerList;
	private List<JSpinner>		maxZzzSpinnerList;
	private List<JComboBox>		crashingAllowedCBList;
	private List<JComboBox>		checkpointsActivatedCBList;
	private List<JComboBox>		directionCBList;

	private int					numberOfMaps;
	private boolean				firstCall			= true;

	private static final int	rowsPerMap			= 5;

	private GameSeries			gameSeries;

	public MapsAndRulesScreen(Screen previous, Karopapier karopapier, JButton previousButton, JButton nextButton)
	{
		super(previous, karopapier, previousButton, nextButton, "screen.mapsAndRules.header", "screen.mapsAndRules.next");

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

	@Override
	public GameSeries applySettings(GameSeries gameSeries) throws GameSeriesException
	{
		Map map;
		Rules rules;
		for(int i = 0; i < this.numberOfMaps; i++)
		{
			map = (Map) this.mapCBList.get(i).getSelectedItem();
			rules = createRules(i);
			((BalancedGameSeries) gameSeries).setMap(i, map, rules);
		}
		return gameSeries;
	}

	@Override
	public void updateBeforeShow(GameSeries gameSeries)
	{
		this.gameSeries = gameSeries;

		int numberOfMapsTmp = ((BalancedGameSeries) gameSeries).getNumberOfMaps();
		if(this.firstCall || numberOfMapsTmp != this.numberOfMaps)
		{
			this.numberOfMaps = numberOfMapsTmp;

			this.mapCBList = new LinkedList<JComboBox>();
			this.gamesPerPlayerSpinnerList = new LinkedList<JSpinner>();
			this.numberOfPlayersSpinnerList = new LinkedList<JSpinner>();
			this.minZzzSpinnerList = new LinkedList<JSpinner>();
			this.maxZzzSpinnerList = new LinkedList<JSpinner>();
			this.crashingAllowedCBList = new LinkedList<JComboBox>();
			this.checkpointsActivatedCBList = new LinkedList<JComboBox>();
			this.directionCBList = new LinkedList<JComboBox>();
			this.removeAll();

			JPanel contentPanel = new JPanel();
			JScrollPane contentSP = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			this.add(contentSP);

			GridBagLayout layout = new GridBagLayout();
			contentPanel.setLayout(layout);

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.fill = GridBagConstraints.HORIZONTAL;

			JLabel label;
			JComboBox mapCB;
			JSpinner gamesPerPlayerSpinner;
			JSpinner numberOfPlayersSpinner;
			JSpinner minZzzSpinner;
			JSpinner maxZzzSpinner;
			JComboBox crashingAllowedCB;
			JComboBox checkpointsActivatedCB;
			JComboBox directionCB;

			Map map;
			Rules rules;
			int gamesPerPlayer;
			int numberOfPlayers;
			Integer maxZzz;
			Integer minZzz;
			Direction direction;
			Boolean crashingAllowed;
			Boolean checkpointsActivated;

			for(int i = 0; i < this.numberOfMaps; i++)
			{
				map = ((BalancedGameSeries) gameSeries).getMap(i);
				if(map == null)
					map = karopapier.getMaps().firstEntry().getValue();

				rules = ((BalancedGameSeries) gameSeries).getRules(i);
				if(rules != null)
				{
					checkpointsActivated = rules.getCheckpointsActivated();
					crashingAllowed = rules.getCrashingAllowed();
					direction = rules.getDirection();
					maxZzz = rules.getMaxZzz();
					minZzz = rules.getMinZzz();
					numberOfPlayers = rules.getNumberOfPlayers();
					gamesPerPlayer = rules.getGamesPerPlayer();
				}
				else
				{
					checkpointsActivated = null;
					crashingAllowed = null;
					direction = null;
					maxZzz = null;
					minZzz = null;
					numberOfPlayers = map.getMaxPlayers()-1;
					gamesPerPlayer = numberOfPlayers;
				}

				gbc.gridy = rowsPerMap * i;

				label = new JLabel(Language.getString("screen.mapsAndRules.map") + (i + 1));
				gbc.gridx = 0;
				contentPanel.add(label, gbc);

				mapCB = new JComboBox();
				mapCB.setModel(new DefaultComboBoxModel(karopapier.getMaps().values().toArray(new Map[0])));
				mapCB.setRenderer(new MapRenderer());
				mapCB.setSelectedItem(map);
				mapCB.addActionListener(this);
				mapCB.setActionCommand("mapSelect" + i);
				gbc.gridwidth = 4;
				gbc.gridx = 1;
				contentPanel.add(mapCB, gbc);

				gbc.gridwidth = 1;

				gbc.gridy++;

				label = new JLabel(Language.getString("screen.mapsAndRules.gamesPerPlayer"));
				gbc.gridx = 1;
				contentPanel.add(label, gbc);
				gamesPerPlayerSpinner = new JSpinner(new SpinnerNumberModel(Math.min(gamesPerPlayer, BalancedGameSeries.MAX_GAMES_PER_PLAYER), 1, BalancedGameSeries.MAX_GAMES_PER_PLAYER, 1));
				gbc.gridx++;
				contentPanel.add(gamesPerPlayerSpinner, gbc);

				label = new JLabel(Language.getString("screen.mapsAndRules.numberOfPlayers"));
				gbc.gridx++;
				contentPanel.add(label, gbc);
				numberOfPlayersSpinner = new JSpinner(new SpinnerNumberModel(Math.min(gameSeries.getPlayers().size() + 1, numberOfPlayers), 1, Math
						.min(gameSeries.getPlayers().size()+1, map.getMaxPlayers()-1), 1));
				gbc.gridx++;
				contentPanel.add(numberOfPlayersSpinner, gbc);

				gbc.gridy++;

				label = new JLabel(Language.getString("screen.rules.crashs"));
				gbc.gridx = 1;
				contentPanel.add(label, gbc);
				crashingAllowedCB = new JComboBox(new BooleanModel(crashingAllowed, true));
				gbc.gridx++;
				contentPanel.add(crashingAllowedCB, gbc);

				gbc.gridy++;

				label = new JLabel(Language.getString("screen.rules.minzzz"));
				gbc.gridx = 1;
				contentPanel.add(label, gbc);
				minZzzSpinner = new JSpinner(new SpinnerNumberModel((minZzz == null ? 2 : minZzz), 0, Integer.MAX_VALUE, 1));
				gbc.gridx++;
				contentPanel.add(minZzzSpinner, gbc);

				label = new JLabel(Language.getString("screen.rules.maxzzz"));
				gbc.gridx++;
				contentPanel.add(label, gbc);
				maxZzzSpinner = new JSpinner(new SpinnerNumberModel((maxZzz == null ? 2 : maxZzz), 0, Integer.MAX_VALUE, 1));
				gbc.gridx++;
				contentPanel.add(maxZzzSpinner, gbc);

				gbc.gridy++;

				label = new JLabel(Language.getString("screen.rules.cps"));
				gbc.gridx = 1;
				contentPanel.add(label, gbc);
				checkpointsActivatedCB = new JComboBox(new BooleanModel(checkpointsActivated, true));
				gbc.gridx++;
				contentPanel.add(checkpointsActivatedCB, gbc);

				label = new JLabel(Language.getString("screen.rules.direction"));
				gbc.gridx++;
				contentPanel.add(label, gbc);
				directionCB = new JComboBox(new DirectionModel(direction, true));
				gbc.gridx++;
				contentPanel.add(directionCB, gbc);

				this.gamesPerPlayerSpinnerList.add(gamesPerPlayerSpinner);
				this.numberOfPlayersSpinnerList.add(numberOfPlayersSpinner);
				this.minZzzSpinnerList.add(minZzzSpinner);
				this.maxZzzSpinnerList.add(maxZzzSpinner);
				this.crashingAllowedCBList.add(crashingAllowedCB);
				this.checkpointsActivatedCBList.add(checkpointsActivatedCB);
				this.directionCBList.add(directionCB);
				this.mapCBList.add(mapCB);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Rules createRules(int i) throws GameSeriesException
	{
		int gamesPerPlayer = (Integer) gamesPerPlayerSpinnerList.get(i).getValue();
		int numberOfPlayers = (Integer) numberOfPlayersSpinnerList.get(i).getValue();
		int minZzz = (Integer) minZzzSpinnerList.get(i).getValue();
		int maxZzz = (Integer) maxZzzSpinnerList.get(i).getValue();
		if(maxZzz < minZzz)
		{
			throw new GameSeriesException("screen.rules.invalidzzz");
		}

		Boolean crashingAllowed = ((Label<Boolean>) crashingAllowedCBList.get(i).getSelectedItem()).getValue();
		Boolean checkpointsActivated = ((Label<Boolean>) checkpointsActivatedCBList.get(i).getSelectedItem()).getValue();
		Direction direction;
		if(directionCBList.get(i).getSelectedItem() instanceof Label)
			direction = null;
		else
			direction = (Direction) directionCBList.get(i).getSelectedItem();
		boolean creatorGiveUp = gameSeries.getRules().isCreatorGiveUp();
		boolean ignoreInvitable = gameSeries.getRules().isIgnoreInvitable();
		return new Rules(minZzz, maxZzz, crashingAllowed, checkpointsActivated, direction, creatorGiveUp, ignoreInvitable, gamesPerPlayer,
				numberOfPlayers);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().startsWith("mapSelect"))
		{
			int mapNumber = Integer.parseInt(e.getActionCommand().substring("mapSelect".length()));
			int max = Math.min(gameSeries.getPlayers().size()+1, ((Map) ((JComboBox) e.getSource()).getSelectedItem()).getMaxPlayers()-1);
			((SpinnerNumberModel) this.numberOfPlayersSpinnerList.get(mapNumber).getModel()).setMaximum(max);
			if(((Integer) ((SpinnerNumberModel) this.numberOfPlayersSpinnerList.get(mapNumber).getModel()).getValue()) > max)
				((SpinnerNumberModel) this.numberOfPlayersSpinnerList.get(mapNumber).getModel()).setValue(max);
		}
	}
}
