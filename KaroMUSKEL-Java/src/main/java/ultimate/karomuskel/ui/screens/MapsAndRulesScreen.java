package ultimate.karomuskel.ui.screens;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
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
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumCreatorParticipation;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.exceptions.GameSeriesException;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.Planner;
import ultimate.karomuskel.ui.EnumNavigation;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.Language.Label;
import ultimate.karomuskel.ui.MainFrame;
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.components.BooleanModel;
import ultimate.karomuskel.ui.components.GenericEnumModel;
import ultimate.karomuskel.ui.components.MapRenderer;

public class MapsAndRulesScreen extends Screen implements ActionListener, ChangeListener
{
	private static final long							serialVersionUID				= 1L;

	private static final String							ACTION_MAP_SELECT				= "mapSelect";
	private static final String							ACTION_RECALC_NUMBER_OF_GAMES	= "recalcNumberOfGames";

	private List<JComboBox<Map>>						mapCBList;
	private List<JSpinner>								gamesPerPlayerSpinnerList;
	private List<JSpinner>								numberOfPlayersSpinnerList;
	private List<JSpinner>								minZzzSpinnerList;
	private List<JSpinner>								maxZzzSpinnerList;
	private List<JTextField>							numberOfGamesTFList;
	private List<JComboBox<Label<EnumGameTC>>>			crashingAllowedCBList;
	private List<JComboBox<Label<Boolean>>>				checkpointsActivatedCBList;
	private List<JComboBox<Label<EnumGameDirection>>>	directionCBList;

	private int											numberOfMaps;

	private static final int							rowsPerMap						= 5;

	private GameSeries									gameSeries;

	public MapsAndRulesScreen(MainFrame gui, Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(gui, previous, karoAPICache, previousButton, nextButton, "screen.mapsAndRules.header");

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

	@Override
	public String getNextKey()
	{
		return "screen.mapsAndRules.next";
	}

	@Override
	public GameSeries applySettings(GameSeries gameSeries, EnumNavigation direction) throws GameSeriesException
	{
		Map map;
		Rules rules;
		for(int i = 0; i < this.numberOfMaps; i++)
		{
			map = (Map) this.mapCBList.get(i).getSelectedItem();
			rules = createRules(i);

			gameSeries.getMapsByKey().put("" + i, Arrays.asList(map));
			gameSeries.getRulesByKey().put("" + i, rules);
		}
		return gameSeries;
	}

	@Override
	public void updateBeforeShow(GameSeries gameSeries, EnumNavigation direction)
	{
		this.gameSeries = gameSeries;

		int numberOfMapsTmp = (int) gameSeries.get(GameSeries.NUMBER_OF_MAPS);
		if(this.firstShow || numberOfMapsTmp != this.numberOfMaps)
		{
			this.numberOfMaps = numberOfMapsTmp;

			this.mapCBList = new LinkedList<>();
			this.gamesPerPlayerSpinnerList = new LinkedList<>();
			this.numberOfPlayersSpinnerList = new LinkedList<>();
			this.minZzzSpinnerList = new LinkedList<>();
			this.maxZzzSpinnerList = new LinkedList<>();
			this.numberOfGamesTFList = new LinkedList<>();
			this.crashingAllowedCBList = new LinkedList<>();
			this.checkpointsActivatedCBList = new LinkedList<>();
			this.directionCBList = new LinkedList<>();
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
			JComboBox<Map> mapCB;
			JSpinner gamesPerPlayerSpinner;
			JSpinner numberOfPlayersSpinner;
			JSpinner minZzzSpinner;
			JSpinner maxZzzSpinner;
			JTextField numberOfGamesTF;
			JComboBox<Label<EnumGameTC>> crashingAllowedCB;
			JComboBox<Label<Boolean>> checkpointsActivatedCB;
			JComboBox<Label<EnumGameDirection>> directionCB;

			Map map;
			Rules rules;
			int gamesPerPlayer;
			int numberOfPlayers;
			Integer maxZzz;
			Integer minZzz;
			EnumGameTC crashingAllowed;
			Boolean checkpointsActivated;
			EnumGameDirection startDirection;

			int maxGamesPerPlayer = GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_MAX_GAMES_PER_PLAYER);

			for(int i = 0; i < this.numberOfMaps; i++)
			{
				final int j = i;

				// remove maps with only less then 3 players (since only races with creator + 2 others make sense)
				LinkedList<Map> maps = new LinkedList<Map>(karoAPICache.getMaps());
				maps.removeIf(m -> {
					return m.getPlayers() < 3;
				});

				if(gameSeries.getMapsByKey().containsKey("" + i) && gameSeries.getMapsByKey().get("" + i).size() > 0)
					map = gameSeries.getMapsByKey().get("" + i).get(0);
				else
					map = maps.getFirst();

				rules = gameSeries.getRulesByKey().get("" + i);
				if(rules != null)
				{
					// preselect values from gameseries
					checkpointsActivated = rules.getCps();
					crashingAllowed = rules.getCrashallowed();
					startDirection = rules.getStartdirection();
					maxZzz = rules.getMaxZzz();
					minZzz = rules.getMinZzz();
					numberOfPlayers = rules.getNumberOfPlayers();
					gamesPerPlayer = rules.getGamesPerPlayer();
				}
				else
				{
					checkpointsActivated = null;
					crashingAllowed = null;
					startDirection = null;
					maxZzz = null;
					minZzz = null;
					numberOfPlayers = (gameSeries.getCreatorParticipation() == EnumCreatorParticipation.not_participating ? map.getPlayers() : map.getPlayers() - 1);
					gamesPerPlayer = numberOfPlayers;
				}

				gbc.gridy = rowsPerMap * i;

				label = new JLabel(Language.getString("screen.mapsAndRules.map") + (i + 1));
				gbc.gridx = 0;
				contentPanel.add(label, gbc);

				mapCB = new JComboBox<>();
				mapCB.setModel(new DefaultComboBoxModel<Map>(maps.toArray(new Map[0])));
				mapCB.setRenderer(new MapRenderer());
				mapCB.setSelectedItem(map);
				mapCB.addActionListener(this);
				mapCB.setActionCommand(ACTION_MAP_SELECT + i);
				gbc.gridwidth = 4;
				gbc.gridx = 1;
				contentPanel.add(mapCB, gbc);

				gbc.gridwidth = 1;

				gbc.gridy++;

				label = new JLabel(Language.getString("screen.mapsAndRules.gamesPerPlayer"));
				gbc.gridx = 1;
				contentPanel.add(label, gbc);
				gamesPerPlayerSpinner = new JSpinner(new SpinnerNumberModel(Math.min(gamesPerPlayer, maxGamesPerPlayer), 1, maxGamesPerPlayer, 1));
				gamesPerPlayerSpinner.addChangeListener(e -> {
					actionPerformed(new ActionEvent(e.getSource(), j, ACTION_RECALC_NUMBER_OF_GAMES + j));
				});
				gbc.gridx++;
				contentPanel.add(gamesPerPlayerSpinner, gbc);

				label = new JLabel(Language.getString("screen.mapsAndRules.numberOfPlayers"));
				gbc.gridx++;
				contentPanel.add(label, gbc);
				int value = Math.min(gameSeries.getPlayers().size() + 1, numberOfPlayers);
				int max = Math.min(gameSeries.getPlayers().size() + 1, (gameSeries.getCreatorParticipation() == EnumCreatorParticipation.not_participating ? map.getPlayers() : map.getPlayers() - 1));
				value = Math.min(value, max); // usually value should not be > max; but with manipulation this can lead to an error, so handle it here
				numberOfPlayersSpinner = new JSpinner(new SpinnerNumberModel(value, 2, max, 1));
				numberOfPlayersSpinner.addChangeListener(e -> {
					actionPerformed(new ActionEvent(e.getSource(), j, ACTION_RECALC_NUMBER_OF_GAMES + j));
				});
				gbc.gridx++;
				contentPanel.add(numberOfPlayersSpinner, gbc);

				gbc.gridy++;

				label = new JLabel(Language.getString("screen.rules.crashs"));
				gbc.gridx = 1;
				contentPanel.add(label, gbc);
				crashingAllowedCB = new JComboBox<>(new GenericEnumModel<EnumGameTC>(EnumGameTC.class, crashingAllowed, true));
				gbc.gridx++;
				contentPanel.add(crashingAllowedCB, gbc);

				label = new JLabel(Language.getString("screen.settings.numberofgames"));
				gbc.gridx++;
				contentPanel.add(label, gbc);
				numberOfGamesTF = new JTextField();
				numberOfGamesTF.setEditable(false);
				numberOfGamesTF.setHorizontalAlignment(SwingConstants.RIGHT);
				gbc.gridx++;
				contentPanel.add(numberOfGamesTF, gbc);

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
				checkpointsActivatedCB = new JComboBox<>(new BooleanModel(checkpointsActivated, true));
				gbc.gridx++;
				contentPanel.add(checkpointsActivatedCB, gbc);

				label = new JLabel(Language.getString("screen.rules.direction"));
				gbc.gridx++;
				contentPanel.add(label, gbc);
				directionCB = new JComboBox<>(new GenericEnumModel<EnumGameDirection>(EnumGameDirection.class, startDirection, true));
				gbc.gridx++;
				contentPanel.add(directionCB, gbc);

				this.gamesPerPlayerSpinnerList.add(gamesPerPlayerSpinner);
				this.numberOfPlayersSpinnerList.add(numberOfPlayersSpinner);
				this.minZzzSpinnerList.add(minZzzSpinner);
				this.maxZzzSpinnerList.add(maxZzzSpinner);
				this.numberOfGamesTFList.add(numberOfGamesTF);
				this.crashingAllowedCBList.add(crashingAllowedCB);
				this.checkpointsActivatedCBList.add(checkpointsActivatedCB);
				this.directionCBList.add(directionCB);
				this.mapCBList.add(mapCB);

				actionPerformed(new ActionEvent(gamesPerPlayerSpinner, j, ACTION_RECALC_NUMBER_OF_GAMES + j));
			}
		}

		this.firstShow = false;
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

		EnumGameTC crashingAllowed = ((Label<EnumGameTC>) crashingAllowedCBList.get(i).getSelectedItem()).getValue();
		Boolean checkpointsActivated = ((Label<Boolean>) checkpointsActivatedCBList.get(i).getSelectedItem()).getValue();
		EnumGameDirection direction = ((Label<EnumGameDirection>) directionCBList.get(i).getSelectedItem()).getValue();

		return new Rules(minZzz, maxZzz, crashingAllowed, checkpointsActivated, direction, gamesPerPlayer, numberOfPlayers);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().startsWith(ACTION_MAP_SELECT))
		{
			int mapNumber = Integer.parseInt(e.getActionCommand().substring(ACTION_MAP_SELECT.length()));
			int mapMax = ((Map) ((JComboBox<Map>) e.getSource()).getSelectedItem()).getPlayers();
			int max = Math.min(gameSeries.getPlayers().size() + 1, (gameSeries.getCreatorParticipation() == EnumCreatorParticipation.not_participating ? mapMax : mapMax - 1));
			((SpinnerNumberModel) this.numberOfPlayersSpinnerList.get(mapNumber).getModel()).setMaximum(max);
			if(((Integer) ((SpinnerNumberModel) this.numberOfPlayersSpinnerList.get(mapNumber).getModel()).getValue()) > max)
				((SpinnerNumberModel) this.numberOfPlayersSpinnerList.get(mapNumber).getModel()).setValue(max);
		}
		else if(e.getActionCommand().startsWith(ACTION_RECALC_NUMBER_OF_GAMES))
		{
			int index = e.getID();
			// calculation of number of games
			int totalNumberOfPlayers = gameSeries.getPlayers().size();
			int gamesPerPlayer = ((Integer) ((SpinnerNumberModel) this.gamesPerPlayerSpinnerList.get(index).getModel()).getValue());
			int numberOfPlayersPerGame = ((Integer) ((SpinnerNumberModel) this.numberOfPlayersSpinnerList.get(index).getModel()).getValue());
			int expectedGames = Planner.calculateNumberOfGames(totalNumberOfPlayers, gamesPerPlayer, numberOfPlayersPerGame);
			// warning, when its not working out evenly
			boolean warning = !Planner.checkNumberOfGamesWorksOutEvenly(totalNumberOfPlayers, gamesPerPlayer, numberOfPlayersPerGame);
			this.numberOfGamesTFList.get(index).setText("" + expectedGames + (warning ? " " + Language.getString("screen.mapsAndRules.warning") : ""));
		}
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
	}
}
