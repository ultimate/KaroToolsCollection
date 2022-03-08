package ultimate.karomuskel.ui.screens;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
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
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.components.AllCombinationsNumberModel;
import ultimate.karomuskel.ui.components.BooleanModel;

public class SettingsScreen extends Screen implements ChangeListener
{
	private static final long			serialVersionUID	= 1L;

	private static final int			gridwidth			= 5;

	private GridBagConstraints			gbc;

	private GameSeries					gameSeries;

	private JLabel						titleLabel;
	private JTextField					titleTF;
	private JLabel						titleDescLabel;

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

	private JLabel						numberOfTeamsPerMatchLabel;
	private JSpinner					numberOfTeamsPerMatchSpinner;

	private JLabel						minPlayersPerTeamLabel;
	private JSpinner					minPlayersPerTeamSpinner;
	private JLabel						maxPlayersPerTeamLabel;
	private JSpinner					maxPlayersPerTeamSpinner;

	private Screen						homeMapsScreenPrevious;
	private Screen						homeMapsScreen;
	private Screen						mapsScreen;
	private Screen						mapsScreenNext;

	public SettingsScreen(JFrame gui, Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(gui, previous, karoAPICache, previousButton, nextButton, "screen.settings.header", "screen.settings.next");

		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);
		this.gbc = new GridBagConstraints();
		this.gbc.anchor = GridBagConstraints.LINE_START;
		this.gbc.insets = new Insets(insetsV, insetsH, insetsV, insetsH);
	}

	@Override
	public void updateBeforeShow(GameSeries gameSeries, EnumNavigation direction)
	{
		if(this.gameSeries != gameSeries)
		{
			this.gameSeries = gameSeries;

			this.titleLabel = new JLabel(Language.getString("screen.settings.title"));
			this.titleTF = new JTextField(84);
			this.titleTF.setText(GameSeriesManager.getDefaultTitle(this.gameSeries));
			this.titleTF.setToolTipText(Language.getString("titlepatterns"));
			gbc.gridwidth = gridwidth;
			gbc.gridx = 0;
			gbc.gridy = 0;
			this.add(titleLabel, gbc);
			gbc.gridy = 1;
			this.add(titleTF, gbc);

			gbc.gridy = 2;
			this.titleDescLabel = new JLabel(Language.getString("screen.settings.title.description"));
			this.add(titleDescLabel, gbc);

			gbc.gridwidth = 1;

			JComponent numberComp = null;
			if(!(gameSeries.getType() == EnumGameSeriesType.KLC))
			{
				if(!(gameSeries.getType() == EnumGameSeriesType.Balanced))
				{
					if(gameSeries.getType() == EnumGameSeriesType.Simple)
					{
						numberOfGamesSpinner = new JSpinner(new SpinnerNumberModel(10, 1, GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_MAX_GAMES), 1));
						((DefaultEditor) this.numberOfGamesSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
						numberComp = numberOfGamesSpinner;
					}
					else if(GameSeriesManager.isTeamBased(gameSeries))
					{
						numberOfGamesTF = new JTextField();
						numberOfGamesTF.setEditable(false);
						numberOfGamesTF.setHorizontalAlignment(SwingConstants.RIGHT);
						numberOfGamesTF.setColumns(spinnerColumns + 2);
						numberComp = numberOfGamesTF;
					}
					numberLabel = new JLabel(Language.getString("screen.settings.numberofgames"));
					numberDescLabel = new JLabel(Language.getString("screen.settings.numberofgames.description" + (GameSeriesManager.isTeamBased(gameSeries) ? "team" : "")));
				}
				else
				{
					numberOfMapsSpinner = new JSpinner(new SpinnerNumberModel(5, 1, GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_MAX_MAPS), 1));
					((DefaultEditor) this.numberOfMapsSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
					numberComp = numberOfMapsSpinner;

					numberLabel = new JLabel(Language.getString("screen.settings.numberofmaps"));
					numberDescLabel = new JLabel(Language.getString("screen.settings.numberofmaps.description"));
				}

				gbc.gridx = 0;
				gbc.gridy = 4;
				this.add(numberLabel, gbc);
				gbc.gridy = 5;
				this.add(numberComp, gbc);

				gbc.gridwidth = gridwidth;
				gbc.gridx = 0;
				gbc.gridy = 6;
				this.add(numberDescLabel, gbc);

				if(GameSeriesManager.isTeamBased(gameSeries))
				{
					numberOfTeamsLabel = new JLabel(Language.getString("screen.settings.numberofteams"));
					numberOfTeamsSpinner = new JSpinner(new SpinnerNumberModel(8, 4, GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_MAX_TEAMS), 2));
					((DefaultEditor) this.numberOfTeamsSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
					((DefaultEditor) this.numberOfTeamsSpinner.getEditor()).getTextField().setEditable(true);

					if(gameSeries.getType() == EnumGameSeriesType.AllCombinations)
					{
						((DefaultEditor) this.numberOfTeamsSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
						((DefaultEditor) this.numberOfTeamsSpinner.getEditor()).getTextField().setEditable(true);

						numberOfTeamsPerMatchSpinner = new JSpinner(new AllCombinationsNumberModel(3, numberOfTeamsSpinner));
						((DefaultEditor) this.numberOfTeamsPerMatchSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
						((DefaultEditor) this.numberOfTeamsPerMatchSpinner.getEditor()).getTextField().setEditable(true);

						numberOfTeamsPerMatchSpinner.addChangeListener(this);
					}
					numberOfTeamsSpinner.addChangeListener(this);

					gbc.gridwidth = 1;
					gbc.gridx = 1;
					gbc.gridy = 4;
					this.add(numberOfTeamsLabel, gbc);
					gbc.gridy = 5;
					this.add(numberOfTeamsSpinner, gbc);

					numberOfGamesPerPairLabel = new JLabel(Language.getString("screen.settings.numberofgamesperpair"));
					numberOfGamesPerPairSpinner = new JSpinner(new SpinnerNumberModel(2, 1, GameSeriesManager.getIntConfig(gameSeries, GameSeries.CONF_MAX_ROUNDS), 1));
					numberOfGamesPerPairSpinner.addChangeListener(this);
					((NumberEditor) this.numberOfGamesPerPairSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
					gbc.gridwidth = 1;
					gbc.gridx = 2;
					gbc.gridy = 4;
					this.add(numberOfGamesPerPairLabel, gbc);
					;
					gbc.gridy = 5;
					this.add(numberOfGamesPerPairSpinner, gbc);

					if(gameSeries.getType() == EnumGameSeriesType.AllCombinations)
					{
						numberOfTeamsPerMatchLabel = new JLabel(Language.getString("screen.settings.numberofteamspermatch"));
						gbc.gridwidth = 1;
						gbc.gridx = 3;
						gbc.gridy = 4;
						this.add(numberOfTeamsPerMatchLabel, gbc);
						gbc.fill = GridBagConstraints.HORIZONTAL;
						gbc.gridy = 5;
						this.add(numberOfTeamsPerMatchSpinner, gbc);
					}
					else
					{
						useHomeMapsLabel = new JLabel(Language.getString("screen.settings.usehomemaps"));
						useHomeMapsCB = new JComboBox<>(new BooleanModel(true, false));
						gbc.gridwidth = 1;
						gbc.gridx = 3;
						gbc.gridy = 4;
						this.add(useHomeMapsLabel, gbc);
						gbc.fill = GridBagConstraints.HORIZONTAL;
						gbc.gridy = 5;
						this.add(useHomeMapsCB, gbc);
					}

					shuffleTeamsLabel = new JLabel(Language.getString("screen.settings.shuffleteams"));
					shuffleTeamsCB = new JComboBox<>(new BooleanModel(false, false));
					gbc.gridwidth = 1;
					gbc.gridx = 4;
					gbc.gridy = 4;
					this.add(shuffleTeamsLabel, gbc);
					gbc.fill = GridBagConstraints.HORIZONTAL;
					gbc.gridy = 5;
					this.add(shuffleTeamsCB, gbc);

					this.stateChanged(null);
				}

				if(gameSeries.getType() == EnumGameSeriesType.Simple)
				{
					minPlayersPerGameLabel = new JLabel(Language.getString("screen.settings.minplayerspergame"));
					minPlayersPerGameSpinner = new JSpinner(new SpinnerNumberModel(6, 1, Integer.MAX_VALUE, 1));
					((NumberEditor) this.minPlayersPerGameSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
					gbc.gridwidth = 1;
					gbc.gridx = 0;
					gbc.gridy = 7;
					this.add(minPlayersPerGameLabel, gbc);
					gbc.gridy = 8;
					this.add(minPlayersPerGameSpinner, gbc);

					maxPlayersPerGameLabel = new JLabel(Language.getString("screen.settings.maxplayerspergame"));
					maxPlayersPerGameSpinner = new JSpinner(new SpinnerNumberModel(8, 2, Integer.MAX_VALUE, 1));
					((NumberEditor) this.maxPlayersPerGameSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
					gbc.gridwidth = 1;
					gbc.gridx = 1;
					gbc.gridy = 7;
					this.add(maxPlayersPerGameLabel, gbc);
					gbc.gridy = 8;
					this.add(maxPlayersPerGameSpinner, gbc);

					this.playersDescLabel = new JLabel(Language.getString("screen.settings.minplayerspergame.description"));
					gbc.gridwidth = gridwidth;
					gbc.gridx = 0;
					gbc.gridy = 9;
					this.add(playersDescLabel, gbc);
				}
				else if(GameSeriesManager.isTeamBased(gameSeries))
				{
					minPlayersPerTeamLabel = new JLabel(Language.getString("screen.settings.minplayersperteam"));
					minPlayersPerTeamSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
					((NumberEditor) this.minPlayersPerTeamSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
					gbc.gridwidth = 1;
					gbc.gridx = 0;
					gbc.gridy = 7;
					this.add(minPlayersPerTeamLabel, gbc);
					gbc.gridy = 8;
					this.add(minPlayersPerTeamSpinner, gbc);

					maxPlayersPerTeamLabel = new JLabel(Language.getString("screen.settings.maxplayersperteam"));
					maxPlayersPerTeamSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
					((NumberEditor) this.maxPlayersPerTeamSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
					gbc.gridwidth = 1;
					gbc.gridx = 1;
					gbc.gridy = 7;
					this.add(maxPlayersPerTeamLabel, gbc);
					gbc.gridy = 8;
					this.add(maxPlayersPerTeamSpinner, gbc);

					autoNameTeamsLabel = new JLabel(Language.getString("screen.settings.autonameteams"));
					autoNameTeamsCB = new JComboBox<>(new BooleanModel(true, false));
					gbc.gridwidth = 1;
					gbc.gridx = 2;
					gbc.gridy = 7;
					this.add(autoNameTeamsLabel, gbc);
					gbc.fill = GridBagConstraints.HORIZONTAL;
					gbc.gridy = 8;
					this.add(autoNameTeamsCB, gbc);

					multipleTeamsLabel = new JLabel(Language.getString("screen.settings.multipleteams"));
					multipleTeamsCB = new JComboBox<>(new BooleanModel(false, false));
					gbc.gridwidth = 1;
					gbc.gridx = 3;
					gbc.gridy = 7;
					this.add(multipleTeamsLabel, gbc);
					gbc.fill = GridBagConstraints.HORIZONTAL;
					gbc.gridy = 8;
					this.add(multipleTeamsCB, gbc);

					creatorTeamLabel = new JLabel(Language.getString("screen.settings.creatorteam"));
					creatorTeamCB = new JComboBox<>(new BooleanModel(true, false));
					gbc.gridwidth = 1;
					gbc.gridx = 4;
					gbc.gridy = 7;
					this.add(creatorTeamLabel, gbc);
					gbc.fill = GridBagConstraints.HORIZONTAL;
					gbc.gridy = 8;
					this.add(creatorTeamCB, gbc);

					this.playersDescLabel = new JLabel(Language.getString("screen.settings.minplayersperteam.description"));
					gbc.gridwidth = gridwidth;
					gbc.gridx = 0;
					gbc.gridy = 9;
					this.add(playersDescLabel, gbc);
				}
			}
		}
		// TODO NAVIGATION preselect values from gameseries
		this.titleTF.requestFocus();
	}

	@SuppressWarnings("unchecked")
	@Override
	public GameSeries applySettings(GameSeries gameSeries, EnumNavigation direction) throws GameSeriesException
	{
		gameSeries.setTitle(titleTF.getText());
		if(gameSeries.getTitle() == null || gameSeries.getTitle().isEmpty())
			throw new GameSeriesException("screen.settings.notitle");
		if(gameSeries.getType() == EnumGameSeriesType.Simple)
		{
			gameSeries.set(GameSeries.NUMBER_OF_GAMES, (Integer) numberOfGamesSpinner.getValue());
			gameSeries.set(GameSeries.MIN_PLAYERS_PER_GAME, (Integer) minPlayersPerGameSpinner.getValue());
			gameSeries.set(GameSeries.MAX_PLAYERS_PER_GAME, (Integer) maxPlayersPerGameSpinner.getValue());
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
				gameSeries.set(GameSeries.USE_HOME_MAPS, ((Label<Boolean>) useHomeMapsCB.getSelectedItem()).getValue());
			}
			gameSeries.set(GameSeries.SHUFFLE_TEAMS, ((Label<Boolean>) shuffleTeamsCB.getSelectedItem()).getValue());
			gameSeries.set(GameSeries.AUTO_NAME_TEAMS, ((Label<Boolean>) autoNameTeamsCB.getSelectedItem()).getValue());
			gameSeries.set(GameSeries.USE_CREATOR_TEAM, !((Label<Boolean>) creatorTeamCB.getSelectedItem()).getValue());
			gameSeries.set(GameSeries.ALLOW_MULTIPLE_TEAMS, ((Label<Boolean>) multipleTeamsCB.getSelectedItem()).getValue());

			int numberOfGamesPerPair = (int) gameSeries.get(GameSeries.NUMBER_OF_GAMES_PER_PAIR);
			boolean homeMaps = ((boolean) gameSeries.get(GameSeries.USE_HOME_MAPS)) && (numberOfGamesPerPair > 1);
			boolean otherMaps = (numberOfGamesPerPair % 2 == 1) || (!homeMaps);

			Screen tmp;
			if(mapsScreen == null)
			{
				// screens suchen
				tmp = this;
				while(!(tmp instanceof HomeMapsScreen))
				{
					tmp = tmp.getNext();
				}
				homeMapsScreenPrevious = tmp.getPrevious();
				homeMapsScreen = tmp;
				mapsScreen = tmp.getNext();
				mapsScreenNext = tmp.getNext().getNext();
			}
			else
			{
				// screens wieder herstellen
				homeMapsScreen.setPrevious(homeMapsScreenPrevious);
				homeMapsScreenPrevious.setNext(homeMapsScreen);
				homeMapsScreen.setNextKey("screen.homemaps.next");

				homeMapsScreen.setNext(mapsScreen);
				mapsScreen.setPrevious(homeMapsScreen);

				mapsScreen.setNext(mapsScreenNext);
				mapsScreenNext.setPrevious(mapsScreen);
			}

			if(!homeMaps)
			{
				homeMapsScreenPrevious.setNext(mapsScreen);
				mapsScreen.setPrevious(homeMapsScreenPrevious);
			}
			if(!otherMaps)
			{
				homeMapsScreen.setNext(mapsScreenNext);
				homeMapsScreen.setNextKey("screen.homemaps.nextskip");
				mapsScreenNext.setPrevious(homeMapsScreen);
			}
		}
		else if(gameSeries.getType() == EnumGameSeriesType.Balanced)
		{
			gameSeries.set(GameSeries.NUMBER_OF_MAPS, (Integer) numberOfMapsSpinner.getValue());
		}
		return gameSeries;
	}

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
				numberOfGames = (numberOfTeams - 1) * (numberOfTeams / 2) * numberOfGamesPerPair;
			}
			else if(gameSeries.getType() == EnumGameSeriesType.KO)
			{
				numberOfGames = (numberOfTeams / 2) * numberOfGamesPerPair;
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
