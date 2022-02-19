package ultimate.karomuskel.ui.screens;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.exceptions.GameSeriesException;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.Screen;

public class StartScreen extends Screen implements ActionListener
{
	private static final long	serialVersionUID	= 1L;
	
	public static final int BUTTON_SIZE = 60;

	private ButtonGroup			buttonGroup;
	
	private GameSeries 			gameSeries;

	public StartScreen(KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(null, karoAPICache, previousButton, nextButton, "screen.start.header", "screen.start.next");

		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		this.buttonGroup = new ButtonGroup();

		JRadioButton radioButton;

		radioButton = new JRadioButton(Language.getString("gameseries.simple"));
		radioButton.setActionCommand("muskel2.model.series.SimpleGameSeries");
		radioButton.addActionListener(this);
		setIcons(radioButton, BUTTON_SIZE);
		
		gbc.gridy = 0;
		gbc.gridx = 0;
		this.add(radioButton, gbc);
		this.buttonGroup.add(radioButton);

		radioButton = new JRadioButton(Language.getString("gameseries.balanced"));
		radioButton.setActionCommand("muskel2.model.series.BalancedGameSeries");
		radioButton.addActionListener(this);
		setIcons(radioButton, BUTTON_SIZE);
		
		gbc.gridy++;
		gbc.gridx = 0;
		this.add(radioButton, gbc);
		this.buttonGroup.add(radioButton);

		radioButton = new JRadioButton(Language.getString("gameseries.league"));
		radioButton.setActionCommand("muskel2.model.series.LeagueGameSeries");
		radioButton.addActionListener(this);
		setIcons(radioButton, BUTTON_SIZE);
		
		gbc.gridy++;
		gbc.gridx = 0;
		this.add(radioButton, gbc);
		this.buttonGroup.add(radioButton);

		radioButton = new JRadioButton(Language.getString("gameseries.allcombinations"));
		radioButton.setActionCommand("muskel2.model.series.AllCombinationsGameSeries");
		radioButton.addActionListener(this);
		setIcons(radioButton, BUTTON_SIZE);
		
		gbc.gridy++;
		gbc.gridx = 0;
		this.add(radioButton, gbc);
		this.buttonGroup.add(radioButton);

		radioButton = new JRadioButton(Language.getString("gameseries.ko"));
		radioButton.setActionCommand("muskel2.model.series.KOGameSeries");
		radioButton.addActionListener(this);
		setIcons(radioButton, BUTTON_SIZE);

		gbc.gridy++;
		gbc.gridx = 0;
		this.add(radioButton, gbc);
		this.buttonGroup.add(radioButton);

		radioButton = new JRadioButton(Language.getString("gameseries.klc"));
		radioButton.setActionCommand("muskel2.model.series.KLCGameSeries");
		radioButton.addActionListener(this);
		setIcons(radioButton, BUTTON_SIZE);

		gbc.gridy++;
		gbc.gridx = 0;
		this.add(radioButton, gbc);
		this.buttonGroup.add(radioButton);

		radioButton = new JRadioButton(Language.getString("gameseries.load"));
		radioButton.setActionCommand("load");
		radioButton.addActionListener(this);
		setIcons(radioButton, BUTTON_SIZE);

		gbc.gridy++;
		gbc.gridx = 0;
		this.add(radioButton, gbc);
		this.buttonGroup.add(radioButton);
	}
	
	@Override
	public void updateBeforeShow(GameSeries gameSeries)
	{
		// nothing
	}

	@Override
	public GameSeries applySettings(GameSeries gameSeries)
	{
		if(this.gameSeries == null)
			throw new GameSeriesException("screen.start.noselection");
		System.out.println("Spiel-Serie initialisiert: " + this.gameSeries.getClass());
		return this.gameSeries;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		boolean loaded = false;
		if(e.getActionCommand().equals("load"))
		{
			JFileChooser fc = new JFileChooser();
			int action = fc.showOpenDialog(this);
			try
			{
				if(action == JFileChooser.APPROVE_OPTION)
				{
					File file = fc.getSelectedFile();
					this.gameSeries = GameSeriesManager.load(file);
					loaded = true;
				}
				else if(action == JFileChooser.ERROR_OPTION)
				{
					throw new IOException("unknown");
				}
			}
			catch(ClassNotFoundException | ClassCastException ex)
			{
				JOptionPane.showMessageDialog(this, Language.getString("error.loadcast"), Language.getString("error.title"), JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
			catch(IOException ex)
			{
				JOptionPane.showMessageDialog(this, Language.getString("error.load") + ex.getLocalizedMessage(), Language.getString("error.title"), JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		}
		else
		{
			try
			{
				this.gameSeries = GameSeriesManager.create(e.getActionCommand());
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		LinkedList<Screen> screens = GameSeriesManager.initScreens(this.gameSeries, karoAPICache, this, previousButton, nextButton, loaded);
		// this.setNext(screens.getFirst()); // already set in initScreens
		screens.getLast().setNext(this);
	}
}
