package ultimate.karomuskel.ui.screens;

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import muskel2.model.series.AllCombinationsGameSeries;
import muskel2.model.series.BalancedGameSeries;
import muskel2.model.series.KOGameSeries;
import muskel2.model.series.LeagueGameSeries;
import muskel2.model.series.SimpleGameSeries;
import muskel2.model.series.TeamBasedGameSeries;
import muskel2.util.LeaguePlanner;
import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.exceptions.GameSeriesException;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.Language.Label;
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.components.AllCombinationsNumberModel;
import ultimate.karomuskel.ui.components.BooleanModel;
import ultimate.karomuskel.ui.components.KORoundNumberModel;

public class SettingsScreen extends Screen implements ChangeListener
{
	private static final long	serialVersionUID	= 1L;

	private static final int	gridwidth			= 5;

	private GridBagConstraints	gbc;

	private GameSeries			gameSeries;

	private JLabel				titleLabel;
	private JTextField			titleTF;
	private JLabel				titleDescLabel;

	private JTextField			numberOfGamesTF;
	private JSpinner			numberOfGamesSpinner;
	private JSpinner			numberOfMapsSpinner;
	private JLabel				numberLabel;
	private JLabel				numberDescLabel;

	private JLabel				minPlayersPerGameLabel;
	private JSpinner			minPlayersPerGameSpinner;
	private JLabel				maxPlayersPerGameLabel;
	private JSpinner			maxPlayersPerGameSpinner;

	private JLabel				numberOfTeamsLabel;
	private JSpinner			numberOfTeamsSpinner;
	private JLabel				numberOfGamesPerPairLabel;
	private JSpinner			numberOfGamesPerPairSpinner;

	private JLabel				playersDescLabel;

	private JLabel				useHomeMapsLabel;
	private JComboBox			useHomeMapsCB;

	private JLabel				shuffleTeamsLabel;
	private JComboBox			shuffleTeamsCB;

	private JLabel				autoNameTeamsLabel;
	private JComboBox			autoNameTeamsCB;

	private JLabel				multipleTeamsLabel;
	private JComboBox			multipleTeamsCB;

	private JLabel				creatorTeamLabel;
	private JComboBox			creatorTeamCB;

	private JLabel				numberOfTeamsPerMatchLabel;
	private JSpinner			numberOfTeamsPerMatchSpinner;

	private JLabel				minPlayersPerTeamLabel;
	private JSpinner			minPlayersPerTeamSpinner;
	private JLabel				maxPlayersPerTeamLabel;
	private JSpinner			maxPlayersPerTeamSpinner;

	private Screen				homeMapsScreenPrevious;
	private Screen				homeMapsScreen;
	private Screen				mapsScreen;
	private Screen				mapsScreenNext;

	public SettingsScreen(Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(previous, karoAPICache, previousButton, nextButton, "screen.settings.header", "screen.settings.next");

		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);
		this.gbc = new GridBagConstraints();
		this.gbc.anchor = GridBagConstraints.LINE_START;
		this.gbc.insets = new Insets(insetsV, insetsH, insetsV, insetsH);
	}

	@Override
	public void updateBeforeShow(GameSeries gameSeries)
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
				if(!(gameSeries.getType() == BalancedGameSeries))
				{
					if(gameSeries.getType() == SimpleGameSeries)
					{
						numberOfGamesSpinner = new JSpinner(new SpinnerNumberModel(10, 1, SimpleGameSeries.MAX_GAMES, 1));
						((DefaultEditor) this.numberOfGamesSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
						numberComp = numberOfGamesSpinner;
					}
					else if(gameSeries.getType() == TeamBasedGameSeries)
					{
						numberOfGamesTF = new JTextField();
						numberOfGamesTF.setEditable(false);
						numberOfGamesTF.setColumns(spinnerColumns + 2);
						numberComp = numberOfGamesTF;
					}
					numberLabel = new JLabel(Language.getString("screen.settings.numberofgames"));
					numberDescLabel = new JLabel(Language
							.getString("screen.settings.numberofgames.description" + (gameSeries.getType() == TeamBasedGameSeries ? "team" : "")));
				}
				else
				{
					numberOfMapsSpinner = new JSpinner(new SpinnerNumberModel(5, 1, BalancedGameSeries.MAX_MAPS, 1));
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

				if(gameSeries.getType() == TeamBasedGameSeries)
				{
					numberOfTeamsLabel = new JLabel(Language.getString("screen.settings.numberofteams"));
					if(gameSeries.getType() == LeagueGameSeries)
					{
						numberOfTeamsSpinner = new JSpinner(new SpinnerNumberModel(8, 4, LeagueGameSeries.MAX_TEAMS, 2));
						((DefaultEditor) this.numberOfTeamsSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
						((DefaultEditor) this.numberOfTeamsSpinner.getEditor()).getTextField().setEditable(true);

						numberOfTeamsSpinner.addChangeListener(this);
					}
					else if(gameSeries.getType() == KOGameSeries)
					{
						numberOfTeamsSpinner = new JSpinner(new KORoundNumberModel(8, KOGameSeries.MAX_TEAMS));
						((DefaultEditor) this.numberOfTeamsSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
						((DefaultEditor) this.numberOfTeamsSpinner.getEditor()).getTextField().setEditable(true);

						numberOfTeamsSpinner.addChangeListener(this);
					}
					else if(gameSeries.getType() == AllCombinationsGameSeries)
					{
						numberOfTeamsSpinner = new JSpinner(new SpinnerNumberModel(8, 3, AllCombinationsGameSeries.MAX_TEAMS, 1));
						((DefaultEditor) this.numberOfTeamsSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
						((DefaultEditor) this.numberOfTeamsSpinner.getEditor()).getTextField().setEditable(true);

						numberOfTeamsPerMatchSpinner = new JSpinner(new AllCombinationsNumberModel(3, numberOfTeamsSpinner));
						((DefaultEditor) this.numberOfTeamsPerMatchSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
						((DefaultEditor) this.numberOfTeamsPerMatchSpinner.getEditor()).getTextField().setEditable(true);

						numberOfTeamsSpinner.addChangeListener(this);
						numberOfTeamsPerMatchSpinner.addChangeListener(this);
					}
					gbc.gridwidth = 1;
					gbc.gridx = 1;
					gbc.gridy = 4;
					this.add(numberOfTeamsLabel, gbc);
					gbc.gridy = 5;
					this.add(numberOfTeamsSpinner, gbc);

					numberOfGamesPerPairLabel = new JLabel(Language.getString("screen.settings.numberofgamesperpair"));
					if(gameSeries.getType() == LeagueGameSeries)
					{
						numberOfGamesPerPairSpinner = new JSpinner(new SpinnerNumberModel(2, 1, LeagueGameSeries.MAX_ROUNDS, 1));
					}
					else if(gameSeries.getType() == KOGameSeries)
					{
						numberOfGamesPerPairSpinner = new JSpinner(new SpinnerNumberModel(1, 1, KOGameSeries.MAX_ROUNDS, 1));
					}
					else if(gameSeries.getType() == AllCombinationsGameSeries)
					{
						numberOfGamesPerPairSpinner = new JSpinner(new SpinnerNumberModel(1, 1, AllCombinationsGameSeries.MAX_ROUNDS, 1));
					}
					numberOfGamesPerPairSpinner.addChangeListener(this);
					((NumberEditor) this.numberOfGamesPerPairSpinner.getEditor()).getTextField().setColumns(spinnerColumns);
					gbc.gridwidth = 1;
					gbc.gridx = 2;
					gbc.gridy = 4;
					this.add(numberOfGamesPerPairLabel, gbc);
					;
					gbc.gridy = 5;
					this.add(numberOfGamesPerPairSpinner, gbc);

					if(gameSeries.getType() == AllCombinationsGameSeries)
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
						useHomeMapsCB = new JComboBox(new BooleanModel(true, false));
						gbc.gridwidth = 1;
						gbc.gridx = 3;
						gbc.gridy = 4;
						this.add(useHomeMapsLabel, gbc);
						gbc.fill = GridBagConstraints.HORIZONTAL;
						gbc.gridy = 5;
						this.add(useHomeMapsCB, gbc);
					}

					shuffleTeamsLabel = new JLabel(Language.getString("screen.settings.shuffleteams"));
					shuffleTeamsCB = new JComboBox(new BooleanModel(false, false));
					gbc.gridwidth = 1;
					gbc.gridx = 4;
					gbc.gridy = 4;
					this.add(shuffleTeamsLabel, gbc);
					gbc.fill = GridBagConstraints.HORIZONTAL;
					gbc.gridy = 5;
					this.add(shuffleTeamsCB, gbc);

					this.stateChanged(null);
				}

				if(gameSeries.getType() == SimpleGameSeries)
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
				else if(gameSeries.getType() == TeamBasedGameSeries)
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
					autoNameTeamsCB = new JComboBox(new BooleanModel(true, false));
					gbc.gridwidth = 1;
					gbc.gridx = 2;
					gbc.gridy = 7;
					this.add(autoNameTeamsLabel, gbc);
					gbc.fill = GridBagConstraints.HORIZONTAL;
					gbc.gridy = 8;
					this.add(autoNameTeamsCB, gbc);

					multipleTeamsLabel = new JLabel(Language.getString("screen.settings.multipleteams"));
					multipleTeamsCB = new JComboBox(new BooleanModel(false, false));
					gbc.gridwidth = 1;
					gbc.gridx = 3;
					gbc.gridy = 7;
					this.add(multipleTeamsLabel, gbc);
					gbc.fill = GridBagConstraints.HORIZONTAL;
					gbc.gridy = 8;
					this.add(multipleTeamsCB, gbc);

					creatorTeamLabel = new JLabel(Language.getString("screen.settings.creatorteam"));
					creatorTeamCB = new JComboBox(new BooleanModel(true, false));
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
		this.titleTF.requestFocus();
	}

	@SuppressWarnings("unchecked")
	@Override
	public GameSeries applySettings(GameSeries gameSeries) throws GameSeriesException
	{
		gameSeries.setTitle(titleTF.getText());
		if(gameSeries.getTitle() == null || gameSeries.getTitle().isEmpty())
			throw new GameSeriesException("screen.settings.notitle");
		if(gameSeries.getType() == EnumGameSeriesType.Simple)
		{
			((SimpleGameSeries) gameSeries).setNumberOfGames((Integer) numberOfGamesSpinner.getValue());
			((SimpleGameSeries) gameSeries).setMinPlayersPerGame((Integer) minPlayersPerGameSpinner.getValue());
			((SimpleGameSeries) gameSeries).setMaxPlayersPerGame((Integer) maxPlayersPerGameSpinner.getValue());
			if(((SimpleGameSeries) gameSeries).getMinPlayersPerGame() > ((SimpleGameSeries) gameSeries).getMaxPlayersPerGame())
				throw new GameSeriesException("screen.settings.minabovemax");
		}
		else if(gameSeries.isTeamBased())
		{
			((TeamBasedGameSeries) gameSeries).setNumberOfTeams((Integer) numberOfTeamsSpinner.getValue());
			((TeamBasedGameSeries) gameSeries).setMinPlayersPerTeam((Integer) minPlayersPerTeamSpinner.getValue());
			((TeamBasedGameSeries) gameSeries).setMaxPlayersPerTeam((Integer) maxPlayersPerTeamSpinner.getValue());
			if(((TeamBasedGameSeries) gameSeries).getMinPlayersPerTeam() > ((TeamBasedGameSeries) gameSeries).getMaxPlayersPerTeam())
				throw new GameSeriesException("screen.settings.minabovemax");
			((TeamBasedGameSeries) gameSeries).setNumberOfGamesPerPair((Integer) numberOfGamesPerPairSpinner.getValue());
			if(gameSeries.getType() == AllCombinationsGameSeries)
				((AllCombinationsGameSeries) gameSeries).setNumberOfTeamsPerMatch((Integer) numberOfTeamsPerMatchSpinner.getValue());
			else
				((TeamBasedGameSeries) gameSeries).setUseHomeMaps(((Label<Boolean>) useHomeMapsCB.getSelectedItem()).getValue());
			((TeamBasedGameSeries) gameSeries).setShuffleTeams(((Label<Boolean>) shuffleTeamsCB.getSelectedItem()).getValue());
			((TeamBasedGameSeries) gameSeries).setAutoNameTeams(((Label<Boolean>) autoNameTeamsCB.getSelectedItem()).getValue());
			((TeamBasedGameSeries) gameSeries).setCreatorTeam(!((Label<Boolean>) creatorTeamCB.getSelectedItem()).getValue());
			((TeamBasedGameSeries) gameSeries).setMultipleTeams(((Label<Boolean>) multipleTeamsCB.getSelectedItem()).getValue());

			int numberOfGamesPerPair = ((TeamBasedGameSeries) gameSeries).getNumberOfGamesPerPair();
			boolean homeMaps = ((TeamBasedGameSeries) gameSeries).isUseHomeMaps() && numberOfGamesPerPair > 1;
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
			((BalancedGameSeries) gameSeries).setNumberOfMaps((Integer) numberOfMapsSpinner.getValue());
		}
		return gameSeries;
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		if(gameSeries.isTeamBased())
		{
			int numberOfGames = 0;
			int numberOfTeams = (Integer) numberOfTeamsSpinner.getValue();
			int numberOfGamesPerPair = (Integer) numberOfGamesPerPairSpinner.getValue();
			if(gameSeries.getType() == LeagueGameSeries)
			{
				numberOfGames = (numberOfTeams - 1) * (numberOfTeams / 2) * numberOfGamesPerPair;
			}
			else if(gameSeries.getType() == KOGameSeries)
			{
				numberOfGames = (numberOfTeams / 2) * numberOfGamesPerPair;
			}
			else if(gameSeries.getType() == AllCombinationsGameSeries)
			{
				int numberOfTeamsPerMatch = (Integer) numberOfTeamsPerMatchSpinner.getValue();
				numberOfGames = LeaguePlanner.calculateNumberOfMatches(numberOfTeams, numberOfTeamsPerMatch);
			}
			numberOfGamesTF.setText("" + numberOfGames);
		}
	}
}
