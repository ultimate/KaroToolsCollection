package ultimate.karomuskel.ui.screens;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.exceptions.GameSeriesException;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.ui.EnumNavigation;
import ultimate.karomuskel.ui.FileDialog;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.MainFrame;
import ultimate.karomuskel.ui.Screen;

public class StartScreen extends Screen implements ActionListener
{
	private static final long	serialVersionUID	= 1L;

	public static final int		BUTTON_SIZE			= 60;

	private ButtonGroup			buttonGroup;

	private GameSeries			gameSeries;

	public StartScreen(MainFrame gui, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(gui, null, karoAPICache, previousButton, nextButton, "screen.start.header");

		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		this.buttonGroup = new ButtonGroup();

		JRadioButton radioButton;

		radioButton = new JRadioButton(Language.getString("gameseries.simple", totalWidth));
		radioButton.setActionCommand(EnumGameSeriesType.Simple.toString());
		radioButton.addActionListener(this);
		setIcons(radioButton, BUTTON_SIZE);

		gbc.gridy = 0;
		gbc.gridx = 0;
		this.add(radioButton, gbc);
		this.buttonGroup.add(radioButton);

		radioButton = new JRadioButton(Language.getString("gameseries.balanced", totalWidth));
		radioButton.setActionCommand(EnumGameSeriesType.Balanced.toString());
		radioButton.addActionListener(this);
		setIcons(radioButton, BUTTON_SIZE);

		gbc.gridy++;
		gbc.gridx = 0;
		this.add(radioButton, gbc);
		this.buttonGroup.add(radioButton);

		radioButton = new JRadioButton(Language.getString("gameseries.league", totalWidth));
		radioButton.setActionCommand(EnumGameSeriesType.League.toString());
		radioButton.addActionListener(this);
		setIcons(radioButton, BUTTON_SIZE);

		gbc.gridy++;
		gbc.gridx = 0;
		this.add(radioButton, gbc);
		this.buttonGroup.add(radioButton);

		radioButton = new JRadioButton(Language.getString("gameseries.allcombinations", totalWidth));
		radioButton.setActionCommand(EnumGameSeriesType.AllCombinations.toString());
		radioButton.addActionListener(this);
		setIcons(radioButton, BUTTON_SIZE);

		gbc.gridy++;
		gbc.gridx = 0;
		this.add(radioButton, gbc);
		this.buttonGroup.add(radioButton);

		radioButton = new JRadioButton(Language.getString("gameseries.ko", totalWidth));
		radioButton.setActionCommand(EnumGameSeriesType.KO.toString());
		radioButton.addActionListener(this);
		setIcons(radioButton, BUTTON_SIZE);

		gbc.gridy++;
		gbc.gridx = 0;
		this.add(radioButton, gbc);
		this.buttonGroup.add(radioButton);

		radioButton = new JRadioButton(Language.getString("gameseries.klc", totalWidth));
		radioButton.setActionCommand(EnumGameSeriesType.KLC.toString());
		radioButton.addActionListener(this);
		setIcons(radioButton, BUTTON_SIZE);

		gbc.gridy++;
		gbc.gridx = 0;
		this.add(radioButton, gbc);
		this.buttonGroup.add(radioButton);

		radioButton = new JRadioButton(Language.getString("gameseries.load", totalWidth));
		radioButton.setActionCommand("load");
		radioButton.addActionListener(this);
		setIcons(radioButton, BUTTON_SIZE);

		gbc.gridy++;
		gbc.gridx = 0;
		this.add(radioButton, gbc);
		this.buttonGroup.add(radioButton);
	}

	@Override
	public String getNextKey()
	{
		return "screen.start.next";
	}

	@Override
	public void updateBeforeShow(GameSeries gameSeries, EnumNavigation direction)
	{
		// nothing
		this.firstShow = false;
	}

	@Override
	public GameSeries applySettings(GameSeries gameSeries, EnumNavigation direction)
	{
		if(direction == EnumNavigation.previous)
			throw new GameSeriesException("navigation.beginning");
		if(this.gameSeries == null)
			throw new GameSeriesException("screen.start.noselection");
		logger.info("Spiel-Serie initialisiert: " + this.gameSeries.getType());
		return this.gameSeries;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals("load"))
		{
			this.gameSeries = FileDialog.getInstance().showLoad(this, karoAPICache);
			if(this.gameSeries == null)
				this.buttonGroup.clearSelection();
			else if(this.gameSeries.getCreator() != karoAPICache.getCurrentUser())
				this.gui.notify(new GameSeriesException("error.load.wrongCreator", null, this.gameSeries.getCreator().toShortString()), JOptionPane.WARNING_MESSAGE);
		}
		else
		{
			this.gameSeries = new GameSeries(EnumGameSeriesType.valueOf(e.getActionCommand()));
			this.gameSeries.setCreator(karoAPICache.getCurrentUser());
		}
		if(this.gameSeries != null)
		{
			LinkedList<Screen> screens = GameSeriesManager.initScreens(this.gameSeries, karoAPICache, this, previousButton, nextButton);
			// this.setNext(screens.getFirst()); // already set in initScreens
			screens.getLast().setNext(this);
		}
	}
}
