package ultimate.karomuskel.ui.screens;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.exceptions.GameSeriesException;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.Planner;
import ultimate.karomuskel.ui.EnumNavigation;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.Language.Label;
import ultimate.karomuskel.ui.MainFrame;
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.components.AllCombinationsNumberModel;
import ultimate.karomuskel.ui.components.BooleanModel;
import ultimate.karomuskel.ui.components.KORoundNumberModel;
import ultimate.karomuskel.ui.components.TagEditor;

public class SettingsScreen extends Screen implements ChangeListener
{
	private static final long			serialVersionUID	= 1L;

	private GridBagConstraints			gbc;

	private GameSeries					gameSeries;

	private JLabel						titleLabel;
	private JTextField					titleTF;
	private JLabel						titleDescLabel;

	private TagEditor					tagEditor;

	private JTextField					numberOfGamesTF;
	private JSpinner					numberOfGamesSpinner;
	private JSpinner					numberOfMapsSpinner;
	private JLabel						numberLabel;
	private JLabel						numberDescLabel;

	private JLabel						minPlayersPerGameLabel;
	private JSpinner					minPlayersPerGameSpinner;
	private JLabel						maxPlayersPerGameLabel;
	private JSpinner					maxPlayersPerGameSpinner;

	private JLabel						numberOfTeamsLabel;
	private JSpinner					numberOfTeamsSpinner;
	private JLabel						numberOfGamesPerPairLabel;
	private JSpinner					numberOfGamesPerPairSpinner;

	private JLabel						playersDescLabel;

	private JLabel						useHomeMapsLabel;
	private JComboBox<Label<Boolean>>	useHomeMapsCB;

	private JLabel						shuffleTeamsLabel;
	private JComboBox<Label<Boolean>>	shuffleTeamsCB;

	private JLabel						autoNameTeamsLabel;
	private JComboBox<Label<Boolean>>	autoNameTeamsCB;

	private JLabel						multipleTeamsLabel;
	private JComboBox<Label<Boolean>>	multipleTeamsCB;

	private JLabel						creatorTeamLabel;
	private JComboBox<Label<Boolean>>	creatorTeamCB;

	private JLabel						smallFinalLabel;
	private JComboBox<Label<Boolean>>	smallFinalCB;

	private JLabel						dummyMatchesLabel;
	private JComboBox<Label<Boolean>>	dummyMatchesCB;

	private JLabel						numberOfTeamsPerMatchLabel;
	private JSpinner					numberOfTeamsPerMatchSpinner;

	private JLabel						minPlayersPerTeamLabel;
	private JSpinner					minPlayersPerTeamSpinner;
	private JLabel						maxPlayersPerTeamLabel;
	private JSpinner					maxPlayersPerTeamSpinner;
	
	private JLabel						minFreeSlotsLabel;
	private JSpinner					minFreeSlotsSpinner;
	
	public SettingsScreen(MainFrame gui, Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(gui, previous, karoAPICache, previousButton, nextButton, "screen.settings.header");

		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);
		this.gbc = new GridBagConstraints();
		this.gbc.anchor = GridBagConstraints.LINE_START;
		this.gbc.insets = new Insets(insetsV, insetsH, insetsV, insetsH);
	}

	@Override
	public String getNextKey()
	{
		return "screen.settings.next";
	}

	@Override
	public Message updateBeforeShow(GameSeries gameSeries, EnumNavigation direction)
	{
		if(this.gameSeries != gameSeries)
		{
			this.gameSeries = gameSeries;

			int gridwidth;
			switch(gameSeries.getType())
			{
				case KO:
				case League:
					gridwidth = 6;
					break;
				default:
					gridwidth = 5;
			}

			this.titleLabel = new JLabel(Language.getString("screen.settings.title"));
			this.titleTF = new JTextField();// (int) (totalWidth / 8.5));
			this.titleTF.setText(gameSeries.getTitle() != null ? gameSeries.getTitle() : GameSeriesManager.getDefaultTitle(this.gameSeries));
			this.titleTF.setToolTipText(Language.getString("titlepatterns"));
			this.titleTF.setMinimumSize(new Dimension(totalWidth, lineHeight));
			this.titleTF.setPreferredSize(new Dimension(totalWidth * 31 / 24, lineHeight)); // don't know why factor is necessary, but otherwise it's not full width
			gbc.gridwidth = gridwidth;
			gbc.gridx = 0;
			gbc.gridy = 0;
			this.add(titleLabel, gbc);
			gbc.gridy = 1;
			this.add(titleTF, gbc);

			gbc.gridwidth = gridwidth;
			gbc.gridx = 0;
			gbc.gridy = 2;
			this.tagEditor = new TagEditor(karoAPICache.getSuggestedTags());
			this.tagEditor.setSelectedTags(gameSeries.getTags());
			this.add(tagEditor, gbc);

			gbc.gridy = 3;
			this.titleDescLabel = new JLabel(Language.getString("screen.settings.title.description", totalWidth));
			this.add(titleDescLabel, gbc);

			gbc.gridwidth = 1;

			JComponent numberComp = null;
			if(!(gameSeries.getType() == EnumGameSeriesType.KLC))
			{
				if(!(gameSeries.getType() == EnumGameSeriesType.Balanced))
				{
					if(gameSeries.getType() == EnumGameSeriesType.Simple)
					{
						int numberOfGamesInit = (gameSeries.get(GameSeries.NUMBER_OF_GAMES) != null ? (int) gameSeries.get(GameSeries.NUMBER_OF_GAMES) : 10);
						numberOfGamesSpinner = new JSpinner(new SpinnerNumberModel(numberOfGamesInit, 1, GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_MAX_GAMES), 1));
						((DefaultEditor) this.numberOfGamesSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
						numberComp = numberOfGamesSpinner;
					}
					else if(GameSeriesManager.isTeamBased(gameSeries))
					{
						numberOfGamesTF = new JTextField();// spinnerColumns + 2);
						numberOfGamesTF.setEditable(false);
						numberOfGamesTF.setHorizontalAlignment(SwingConstants.RIGHT);
						numberOfGamesTF.setMinimumSize(new Dimension(cellWidth / 2, lineHeight));
						numberOfGamesTF.setPreferredSize(new Dimension(cellWidth, lineHeight));
						numberComp = numberOfGamesTF;
					}
					numberLabel = new JLabel(Language.getString("screen.settings.numberofgames", cellWidth));
					numberDescLabel = new JLabel(Language.getString("screen.settings.numberofgames.description" + (GameSeriesManager.isTeamBased(gameSeries) ? "team" : ""), totalWidth));
				}
				else
				{
					int numberOfMapsInit = (gameSeries.get(GameSeries.NUMBER_OF_MAPS) != null ? (int) gameSeries.get(GameSeries.NUMBER_OF_MAPS) : 5);
					numberOfMapsSpinner = new JSpinner(new SpinnerNumberModel(numberOfMapsInit, 1, GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_MAX_MAPS), 1));
					((DefaultEditor) this.numberOfMapsSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
					numberComp = numberOfMapsSpinner;

					numberLabel = new JLabel(Language.getString("screen.settings.numberofmaps", cellWidth));
					numberDescLabel = new JLabel(Language.getString("screen.settings.numberofmaps.description", totalWidth));
				}

				gbc.gridx = 0;
				gbc.gridy = 6;
				this.add(numberLabel, gbc);
				gbc.gridy = 7;
				this.add(numberComp, gbc);

				gbc.gridwidth = gridwidth;
				gbc.gridx = 0;
				gbc.gridy = 8;
				this.add(numberDescLabel, gbc);

				if(GameSeriesManager.isTeamBased(gameSeries))
				{
					int numberOfTeamsInit = (gameSeries.get(GameSeries.NUMBER_OF_TEAMS) != null ? (int) gameSeries.get(GameSeries.NUMBER_OF_TEAMS) : 8);
					numberOfTeamsLabel = new JLabel(Language.getString("screen.settings.numberofteams", cellWidth));
					int stepSize = GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_TEAM_STEP_SIZE);
					if(gameSeries.getType() == EnumGameSeriesType.KO)
						numberOfTeamsSpinner = new JSpinner(new KORoundNumberModel(numberOfTeamsInit, GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_MAX_TEAMS)));
					else
						numberOfTeamsSpinner = new JSpinner(new SpinnerNumberModel(numberOfTeamsInit, 4, GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_MAX_TEAMS), stepSize));
					((DefaultEditor) this.numberOfTeamsSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
					((DefaultEditor) this.numberOfTeamsSpinner.getEditor()).getTextField().setEditable(true);

					if(gameSeries.getType() == EnumGameSeriesType.AllCombinations)
					{
						((DefaultEditor) this.numberOfTeamsSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
						((DefaultEditor) this.numberOfTeamsSpinner.getEditor()).getTextField().setEditable(true);

						int numberOfTeamsPerMatchInit = (gameSeries.get(GameSeries.NUMBER_OF_TEAMS_PER_MATCH) != null ? (int) gameSeries.get(GameSeries.NUMBER_OF_TEAMS_PER_MATCH) : 3);
						numberOfTeamsPerMatchSpinner = new JSpinner(new AllCombinationsNumberModel(numberOfTeamsPerMatchInit, numberOfTeamsSpinner));
						((DefaultEditor) this.numberOfTeamsPerMatchSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
						((DefaultEditor) this.numberOfTeamsPerMatchSpinner.getEditor()).getTextField().setEditable(true);

						numberOfTeamsPerMatchSpinner.addChangeListener(this);
					}
					numberOfTeamsSpinner.addChangeListener(this);

					gbc.gridwidth = 1;
					gbc.gridx = 1;
					gbc.gridy = 6;
					this.add(numberOfTeamsLabel, gbc);
					gbc.gridy = 7;
					this.add(numberOfTeamsSpinner, gbc);

					int numberOfGamesPerPairInit = (gameSeries.get(GameSeries.NUMBER_OF_GAMES_PER_PAIR) != null ? (int) gameSeries.get(GameSeries.NUMBER_OF_GAMES_PER_PAIR) : 2);
					numberOfGamesPerPairLabel = new JLabel(Language.getString("screen.settings.numberofgamesperpair", cellWidth));
					numberOfGamesPerPairSpinner = new JSpinner(new SpinnerNumberModel(numberOfGamesPerPairInit, 1, GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_MAX_ROUNDS), 1));
					numberOfGamesPerPairSpinner.addChangeListener(this);
					((NumberEditor) this.numberOfGamesPerPairSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
					gbc.gridwidth = 1;
					gbc.gridx = 2;
					gbc.gridy = 6;
					this.add(numberOfGamesPerPairLabel, gbc);
					gbc.gridy = 7;
					this.add(numberOfGamesPerPairSpinner, gbc);

					if(gameSeries.getType() == EnumGameSeriesType.AllCombinations)
					{
						numberOfTeamsPerMatchLabel = new JLabel(Language.getString("screen.settings.numberofteamspermatch", cellWidth));
						gbc.gridwidth = 1;
						gbc.gridx = 3;
						gbc.gridy = 6;
						this.add(numberOfTeamsPerMatchLabel, gbc);
						gbc.fill = GridBagConstraints.HORIZONTAL;
						gbc.gridy = 7;
						this.add(numberOfTeamsPerMatchSpinner, gbc);
					}
					else
					{
						useHomeMapsLabel = new JLabel(Language.getString("screen.settings.usehomemaps", cellWidth));
						boolean useHomeMapsInit = (gameSeries.get(GameSeries.USE_HOME_MAPS) != null ? (boolean) gameSeries.get(GameSeries.USE_HOME_MAPS) : true);
						useHomeMapsCB = new JComboBox<>(new BooleanModel(useHomeMapsInit, false));
						gbc.gridwidth = 1;
						gbc.gridx = 3;
						gbc.gridy = 6;
						this.add(useHomeMapsLabel, gbc);
						gbc.fill = GridBagConstraints.HORIZONTAL;
						gbc.gridy = 7;
						this.add(useHomeMapsCB, gbc);
					}

					shuffleTeamsLabel = new JLabel(Language.getString("screen.settings.shuffleteams", cellWidth));
					boolean shuffleTeamsInit = (gameSeries.get(GameSeries.SHUFFLE_TEAMS) != null ? (boolean) gameSeries.get(GameSeries.SHUFFLE_TEAMS) : false);
					shuffleTeamsCB = new JComboBox<>(new BooleanModel(shuffleTeamsInit, false));
					gbc.gridwidth = 1;
					gbc.gridx = 4;
					gbc.gridy = 6;
					this.add(shuffleTeamsLabel, gbc);
					gbc.fill = GridBagConstraints.HORIZONTAL;
					gbc.gridy = 7;
					this.add(shuffleTeamsCB, gbc);

					if(gameSeries.getType() == EnumGameSeriesType.KO)
					{
						smallFinalLabel = new JLabel(Language.getString("screen.settings.smallFinal", cellWidth));
						boolean smallFinalInit = (gameSeries.get(GameSeries.SMALL_FINAL) != null ? (boolean) gameSeries.get(GameSeries.SMALL_FINAL) : false);
						smallFinalCB = new JComboBox<>(new BooleanModel(smallFinalInit, false));
						gbc.gridwidth = 1;
						gbc.gridx = 5;
						gbc.gridy = 6;
						this.add(smallFinalLabel, gbc);
						gbc.fill = GridBagConstraints.HORIZONTAL;
						gbc.gridy = 7;
						this.add(smallFinalCB, gbc);
					}
					else if(gameSeries.getType() == EnumGameSeriesType.League)
					{
						dummyMatchesLabel = new JLabel(Language.getString("screen.settings.dummyMatches", cellWidth));
						boolean dummyMatchesInit = (gameSeries.get(GameSeries.DUMMY_MATCHES) != null ? (boolean) gameSeries.get(GameSeries.DUMMY_MATCHES) : false);
						dummyMatchesCB = new JComboBox<>(new BooleanModel(dummyMatchesInit, false));
						dummyMatchesCB.addActionListener(e -> { stateChanged(null); });
						gbc.gridwidth = 1;
						gbc.gridx = 5;
						gbc.gridy = 6;
						this.add(dummyMatchesLabel, gbc);
						gbc.fill = GridBagConstraints.HORIZONTAL;
						gbc.gridy = 7;
						this.add(dummyMatchesCB, gbc);
					}

					this.stateChanged(null);
				}

				if(gameSeries.getType() == EnumGameSeriesType.Simple)
				{
					minPlayersPerGameLabel = new JLabel(Language.getString("screen.settings.playerspergame.min", cellWidth));
					int minPlayersPerGameInit = (gameSeries.get(GameSeries.MIN_PLAYERS_PER_GAME) != null ? (int) gameSeries.get(GameSeries.MIN_PLAYERS_PER_GAME) : 6);
					minPlayersPerGameSpinner = new JSpinner(new SpinnerNumberModel(minPlayersPerGameInit, 1, Integer.MAX_VALUE, 1));
					((NumberEditor) this.minPlayersPerGameSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
					gbc.gridwidth = 1;
					gbc.gridx = 0;
					gbc.gridy = 9;
					this.add(minPlayersPerGameLabel, gbc);
					gbc.gridy = 10;
					this.add(minPlayersPerGameSpinner, gbc);

					maxPlayersPerGameLabel = new JLabel(Language.getString("screen.settings.playerspergame.max", cellWidth));
					int maxPlayersPerGameInit = (gameSeries.get(GameSeries.MAX_PLAYERS_PER_GAME) != null ? (int) gameSeries.get(GameSeries.MAX_PLAYERS_PER_GAME) : 8);
					maxPlayersPerGameSpinner = new JSpinner(new SpinnerNumberModel(maxPlayersPerGameInit, 2, Integer.MAX_VALUE, 1));
					((NumberEditor) this.maxPlayersPerGameSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
					gbc.gridwidth = 1;
					gbc.gridx = 1;
					gbc.gridy = 9;
					this.add(maxPlayersPerGameLabel, gbc);
					gbc.gridy = 10;
					this.add(maxPlayersPerGameSpinner, gbc);

					minFreeSlotsLabel = new JLabel(Language.getString("screen.settings.minfreeslots", cellWidth));
					int minFreeSlotsInit = (gameSeries.get(GameSeries.MIN_FREE_SLOTS) != null ? (int) gameSeries.get(GameSeries.MIN_FREE_SLOTS) : 0);
					minFreeSlotsSpinner = new JSpinner(new SpinnerNumberModel(minFreeSlotsInit, 0, GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_MAX_FREE_SLOTS), 1));
					((NumberEditor) this.minFreeSlotsSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
					gbc.gridwidth = 1;
					gbc.gridx = 2;
					gbc.gridy = 9;
					this.add(minFreeSlotsLabel, gbc);
					gbc.gridy = 10;
					this.add(minFreeSlotsSpinner, gbc);

					this.playersDescLabel = new JLabel(Language.getString("screen.settings.playerspergame.description", totalWidth));
					gbc.gridwidth = gridwidth;
					gbc.gridx = 0;
					gbc.gridy = 11;
					this.add(playersDescLabel, gbc);
				}
				else if(GameSeriesManager.isTeamBased(gameSeries))
				{
					minPlayersPerTeamLabel = new JLabel(Language.getString("screen.settings.playersperteam.min", cellWidth));
					int minPlayersPerTeamInit = (gameSeries.get(GameSeries.MIN_PLAYERS_PER_TEAM) != null ? (int) gameSeries.get(GameSeries.MIN_PLAYERS_PER_TEAM) : 1);
					minPlayersPerTeamSpinner = new JSpinner(new SpinnerNumberModel(minPlayersPerTeamInit, 1, Integer.MAX_VALUE, 1));
					((NumberEditor) this.minPlayersPerTeamSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
					gbc.gridwidth = 1;
					gbc.gridx = 0;
					gbc.gridy = 9;
					this.add(minPlayersPerTeamLabel, gbc);
					gbc.gridy = 10;
					this.add(minPlayersPerTeamSpinner, gbc);

					maxPlayersPerTeamLabel = new JLabel(Language.getString("screen.settings.playersperteam.max", cellWidth));
					int maxPlayersPerTeamInit = (gameSeries.get(GameSeries.MAX_PLAYERS_PER_TEAM) != null ? (int) gameSeries.get(GameSeries.MAX_PLAYERS_PER_TEAM) : 1);
					maxPlayersPerTeamSpinner = new JSpinner(new SpinnerNumberModel(maxPlayersPerTeamInit, 1, Integer.MAX_VALUE, 1));
					((NumberEditor) this.maxPlayersPerTeamSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
					gbc.gridwidth = 1;
					gbc.gridx = 1;
					gbc.gridy = 9;
					this.add(maxPlayersPerTeamLabel, gbc);
					gbc.gridy = 10;
					this.add(maxPlayersPerTeamSpinner, gbc);

					autoNameTeamsLabel = new JLabel(Language.getString("screen.settings.autonameteams", cellWidth));
					boolean autoNameTeamsInit = (gameSeries.get(GameSeries.AUTO_NAME_TEAMS) != null ? (boolean) gameSeries.get(GameSeries.AUTO_NAME_TEAMS) : true);
					autoNameTeamsCB = new JComboBox<>(new BooleanModel(autoNameTeamsInit, false));
					gbc.gridwidth = 1;
					gbc.gridx = 2;
					gbc.gridy = 9;
					this.add(autoNameTeamsLabel, gbc);
					gbc.fill = GridBagConstraints.HORIZONTAL;
					gbc.gridy = 10;
					this.add(autoNameTeamsCB, gbc);

					multipleTeamsLabel = new JLabel(Language.getString("screen.settings.multipleteams", cellWidth));
					boolean multipleTeamsInit = (gameSeries.get(GameSeries.ALLOW_MULTIPLE_TEAMS) != null ? (boolean) gameSeries.get(GameSeries.ALLOW_MULTIPLE_TEAMS) : false);
					multipleTeamsCB = new JComboBox<>(new BooleanModel(multipleTeamsInit, false));
					gbc.gridwidth = 1;
					gbc.gridx = 3;
					gbc.gridy = 9;
					this.add(multipleTeamsLabel, gbc);
					gbc.fill = GridBagConstraints.HORIZONTAL;
					gbc.gridy = 10;
					this.add(multipleTeamsCB, gbc);

					creatorTeamLabel = new JLabel(Language.getString("screen.settings.creatorteam", cellWidth));
					boolean creatorTeamInit = (gameSeries.get(GameSeries.USE_CREATOR_TEAM) != null ? (boolean) gameSeries.get(GameSeries.USE_CREATOR_TEAM) : true);
					creatorTeamCB = new JComboBox<>(new BooleanModel(creatorTeamInit, false));
					gbc.gridwidth = 1;
					gbc.gridx = 4;
					gbc.gridy = 9;
					this.add(creatorTeamLabel, gbc);
					gbc.fill = GridBagConstraints.HORIZONTAL;
					gbc.gridy = 10;
					this.add(creatorTeamCB, gbc);

					this.playersDescLabel = new JLabel(Language.getString("screen.settings.playersperteam.description", totalWidth));
					gbc.gridwidth = gridwidth;
					gbc.gridx = 0;
					gbc.gridy = 11;
					this.add(playersDescLabel, gbc);
				}
			}
		}
		this.titleTF.requestFocus();
		
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public GameSeries applySettings(GameSeries gameSeries, EnumNavigation direction) throws GameSeriesException
	{
		gameSeries.setTitle(titleTF.getText());
		if(gameSeries.getTitle() == null || gameSeries.getTitle().isEmpty())
			throw new GameSeriesException("screen.settings.notitle");
		gameSeries.setTags(this.tagEditor.getSelectedTags());

		if(gameSeries.getType() == EnumGameSeriesType.Simple)
		{
			gameSeries.set(GameSeries.NUMBER_OF_GAMES, (Integer) numberOfGamesSpinner.getValue());
			gameSeries.set(GameSeries.MIN_PLAYERS_PER_GAME, (Integer) minPlayersPerGameSpinner.getValue());
			gameSeries.set(GameSeries.MAX_PLAYERS_PER_GAME, (Integer) maxPlayersPerGameSpinner.getValue());
			gameSeries.set(GameSeries.MIN_FREE_SLOTS, (Integer) minFreeSlotsSpinner.getValue());
			if((int) gameSeries.get(GameSeries.MIN_PLAYERS_PER_GAME) > (int) gameSeries.get(GameSeries.MAX_PLAYERS_PER_GAME))
				throw new GameSeriesException("screen.settings.minabovemax");
		}
		else if(GameSeriesManager.isTeamBased(gameSeries))
		{
			gameSeries.set(GameSeries.NUMBER_OF_TEAMS, (Integer) numberOfTeamsSpinner.getValue());
			gameSeries.set(GameSeries.MIN_PLAYERS_PER_TEAM, (Integer) minPlayersPerTeamSpinner.getValue());
			gameSeries.set(GameSeries.MAX_PLAYERS_PER_TEAM, (Integer) maxPlayersPerTeamSpinner.getValue());
			if((int) gameSeries.get(GameSeries.MIN_PLAYERS_PER_TEAM) > (int) gameSeries.get(GameSeries.MAX_PLAYERS_PER_TEAM))
				throw new GameSeriesException("screen.settings.minabovemax");
			gameSeries.set(GameSeries.NUMBER_OF_GAMES_PER_PAIR, (Integer) numberOfGamesPerPairSpinner.getValue());
			if(gameSeries.getType() == EnumGameSeriesType.AllCombinations)
			{
				gameSeries.set(GameSeries.USE_HOME_MAPS, false); // always false for AllCombinations, required so we don't run into an error
				gameSeries.set(GameSeries.NUMBER_OF_TEAMS_PER_MATCH, (Integer) numberOfTeamsPerMatchSpinner.getValue());
			}
			else
			{
				if(gameSeries.getType() == EnumGameSeriesType.KO && (int) gameSeries.get(GameSeries.NUMBER_OF_GAMES_PER_PAIR) == 1)
					gameSeries.set(GameSeries.USE_HOME_MAPS, false); // home maps do not make sense here
				else
					gameSeries.set(GameSeries.USE_HOME_MAPS, ((Label<Boolean>) useHomeMapsCB.getSelectedItem()).getValue());
			}
			gameSeries.set(GameSeries.SHUFFLE_TEAMS, ((Label<Boolean>) shuffleTeamsCB.getSelectedItem()).getValue());
			if(gameSeries.getType() == EnumGameSeriesType.KO)
			{
				gameSeries.set(GameSeries.SMALL_FINAL, ((Label<Boolean>) smallFinalCB.getSelectedItem()).getValue());
				gameSeries.set(GameSeries.CURRENT_ROUND, gameSeries.get(GameSeries.NUMBER_OF_TEAMS));
			}
			else if(gameSeries.getType() == EnumGameSeriesType.League)
			{
				if((int) gameSeries.get(GameSeries.NUMBER_OF_TEAMS) % 2 == 1)
					gameSeries.set(GameSeries.DUMMY_MATCHES, ((Label<Boolean>) dummyMatchesCB.getSelectedItem()).getValue());
				else
					gameSeries.set(GameSeries.DUMMY_MATCHES, false);
			}
			gameSeries.set(GameSeries.AUTO_NAME_TEAMS, ((Label<Boolean>) autoNameTeamsCB.getSelectedItem()).getValue());
			gameSeries.set(GameSeries.USE_CREATOR_TEAM, !((Label<Boolean>) creatorTeamCB.getSelectedItem()).getValue());
			gameSeries.set(GameSeries.ALLOW_MULTIPLE_TEAMS, ((Label<Boolean>) multipleTeamsCB.getSelectedItem()).getValue());

			int numberOfGamesPerPair = (int) gameSeries.get(GameSeries.NUMBER_OF_GAMES_PER_PAIR);
			boolean homeMaps = ((boolean) gameSeries.get(GameSeries.USE_HOME_MAPS)) && (numberOfGamesPerPair > 1);
			boolean otherMaps = (numberOfGamesPerPair % 2 == 1) || (!homeMaps);

			findScreen(s -> { return s instanceof HomeMapsScreen; }, EnumNavigation.next).setSkip(!homeMaps);
			findScreen(s -> { return s instanceof MapsScreen; }, EnumNavigation.next).setSkip(!otherMaps);

			if(gameSeries.getType() == EnumGameSeriesType.KO)
			{
				int maxTeams = GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_MAX_TEAMS);
				int teams = (int) gameSeries.get(GameSeries.NUMBER_OF_TEAMS);
				// skip the screens those who are too many for the number of teams
				Screen cursor = findScreen(s -> { return s instanceof KOWinnersScreen; }, EnumNavigation.next);
				while(cursor != null && !(cursor instanceof StartScreen))
				{
					cursor.setSkip(maxTeams > teams); // skip KO
					cursor = cursor.getNext();
					cursor.setSkip(maxTeams > teams); // skip Summary
					cursor = cursor.getNext();
					maxTeams /= 2;
				}
			}
		}
		else if(gameSeries.getType() == EnumGameSeriesType.Balanced)
		{
			gameSeries.set(GameSeries.NUMBER_OF_MAPS, (Integer) numberOfMapsSpinner.getValue());
		}
		else if(gameSeries.getType() == EnumGameSeriesType.KLC)
		{
			// set current round after player selection
		}
		return gameSeries;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void stateChanged(ChangeEvent e)
	{
		if(GameSeriesManager.isTeamBased(gameSeries))
		{
			int numberOfGames = 0;
			int numberOfTeams = (Integer) numberOfTeamsSpinner.getValue();
			int numberOfGamesPerPair = (Integer) numberOfGamesPerPairSpinner.getValue();
			if(gameSeries.getType() == EnumGameSeriesType.League)
			{
				dummyMatchesCB.setEnabled(numberOfTeams % 2 == 1);
				boolean dummyMatches = ((Label<Boolean>) dummyMatchesCB.getSelectedItem()).getValue();
				int tmp = numberOfTeams;
				if(tmp % 2 == 1)
					tmp++;
				numberOfGames = (tmp - 1) * (tmp / 2) * numberOfGamesPerPair;
				if(numberOfTeams % 2 == 1 && !dummyMatches)
					numberOfGames -= numberOfTeams * numberOfGamesPerPair;
			}
			else if(gameSeries.getType() == EnumGameSeriesType.KO)
			{
				boolean smallFinal = ((Label<Boolean>) smallFinalCB.getSelectedItem()).getValue();
				numberOfGames = (numberOfTeams - (smallFinal ? 0 : 1)) * numberOfGamesPerPair;
				useHomeMapsCB.setEnabled(numberOfGamesPerPair > 1);
			}
			else if(gameSeries.getType() == EnumGameSeriesType.AllCombinations)
			{
				int numberOfTeamsPerMatch = (Integer) numberOfTeamsPerMatchSpinner.getValue();
				numberOfGames = Planner.calculateNumberOfMatches(numberOfTeams, numberOfTeamsPerMatch) * numberOfGamesPerPair;
			}
			numberOfGamesTF.setText("" + numberOfGames);
		}
	}
}
