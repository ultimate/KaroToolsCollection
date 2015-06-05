package muskel2.gui.screens;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import muskel2.core.exceptions.GameSeriesException;
import muskel2.gui.Screen;
import muskel2.model.GameSeries;
import muskel2.model.Karopapier;
import muskel2.util.Language;

public class StartScreen extends Screen implements ActionListener
{
	private static final long	serialVersionUID	= 1L;

	private ButtonGroup			buttonGroup;
	
	private GameSeries 			gameSeries;

	public StartScreen(Karopapier karopapier, JButton previousButton, JButton nextButton)
	{
		super(null, karopapier, previousButton, nextButton, "screen.start.header", "screen.start.next");

		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		this.buttonGroup = new ButtonGroup();

		JRadioButton radioButton;

		radioButton = new JRadioButton(Language.getString("gameseries.simple"));
		radioButton.setActionCommand("muskel2.model.series.SimpleGameSeries");
		radioButton.addActionListener(this);
		setIcons(radioButton, 80);
		
		gbc.gridy = 0;
		gbc.gridx = 0;
		this.add(radioButton, gbc);
		this.buttonGroup.add(radioButton);

		radioButton = new JRadioButton(Language.getString("gameseries.balanced"));
		radioButton.setActionCommand("muskel2.model.series.BalancedGameSeries");
		radioButton.addActionListener(this);
		setIcons(radioButton, 80);
		
		gbc.gridy = 1;
		gbc.gridx = 0;
		this.add(radioButton, gbc);
		this.buttonGroup.add(radioButton);

		radioButton = new JRadioButton(Language.getString("gameseries.league"));
		radioButton.setActionCommand("muskel2.model.series.LeagueGameSeries");
		radioButton.addActionListener(this);
		setIcons(radioButton, 80);
		
		gbc.gridy = 2;
		gbc.gridx = 0;
		this.add(radioButton, gbc);
		this.buttonGroup.add(radioButton);

		radioButton = new JRadioButton(Language.getString("gameseries.ko"));
		radioButton.setActionCommand("muskel2.model.series.KOGameSeries");
		radioButton.addActionListener(this);
		setIcons(radioButton, 80);

		gbc.gridy = 3;
		gbc.gridx = 0;
		this.add(radioButton, gbc);
		this.buttonGroup.add(radioButton);

		radioButton = new JRadioButton(Language.getString("gameseries.load"));
		radioButton.setActionCommand("load");
		radioButton.addActionListener(this);
		setIcons(radioButton, 80);

		gbc.gridy = 4;
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
		if(e.getActionCommand().equals("load"))
		{
			JFileChooser fc = new JFileChooser();
			int action = fc.showOpenDialog(this);
			try
			{
				if(action == JFileChooser.APPROVE_OPTION)
				{
					File file = fc.getSelectedFile();
					
					FileInputStream fis = new FileInputStream(file);
					BufferedInputStream bis = new BufferedInputStream(fis);
					ObjectInputStream ois = new ObjectInputStream(bis);
					
					this.gameSeries = (GameSeries) ois.readObject();
					this.gameSeries.setLoaded(true);
					Screen last = this.gameSeries.initOnLoad(this, karopapier, previousButton, nextButton);
					while(last.getNext() != null)
					{
						last = last.getNext();
					}
					last.setNext(this);
					
					ois.close();
					bis.close();
					fis.close();
				}
				else if(action == JFileChooser.ERROR_OPTION)
				{
					throw new IOException("unknown");
				}
			}
			catch(ClassNotFoundException ex)
			{
				JOptionPane.showMessageDialog(this, Language.getString("error.loadcast"), Language.getString("error.title"), JOptionPane.ERROR_MESSAGE);
			}
			catch(ClassCastException ex)
			{
				JOptionPane.showMessageDialog(this, Language.getString("error.loadcast"), Language.getString("error.title"), JOptionPane.ERROR_MESSAGE);
			}
			catch(IOException ex)
			{
				JOptionPane.showMessageDialog(this, Language.getString("error.load") + ex.getLocalizedMessage(), Language.getString("error.title"), JOptionPane.ERROR_MESSAGE);
			}
		}
		else
		{
			try
			{
				this.gameSeries = (GameSeries) Class.forName(e.getActionCommand()).newInstance();
				this.setNext(this.gameSeries.init(this, karopapier, previousButton, nextButton));
				Screen last = this;
				while(last.getNext() != null)
				{
					last = last.getNext();
				}
				last.setNext(this);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
}
