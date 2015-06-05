package muskel2.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;

import muskel2.Main;
import muskel2.core.exceptions.GameSeriesException;
import muskel2.gui.screens.StartScreen;
import muskel2.gui.screens.SummaryScreen;
import muskel2.model.GameSeries;
import muskel2.model.Karopapier;
import muskel2.util.Language;

public class MainFrame extends JFrame implements WindowListener, ActionListener
{
	private static final long	serialVersionUID	= 1L;
	
	private static final Dimension size = new Dimension(1024, 768);
	private static final Dimension aboutSize = new Dimension(700, 500);

	private Karopapier			karopapier;

	private BorderLayout		layout;

	private JLabel				descriptionLabel;
	private JPanel				screenPanel;
	private JPanel				navigationPanel;

	private Screen				currentScreen;
	private Screen				startScreen;
	
	private JButton				previousButton;
	private JButton				nextButton;
	private JButton				aboutButton;
	
	private GameSeries			gameSeries;

	public MainFrame(String titleKey, Karopapier karopapier)
	{        
		super(Language.getString(titleKey));        
		this.karopapier = karopapier;

		this.layout = new BorderLayout();
		this.getContentPane().setLayout(this.layout);

		this.descriptionLabel = new JLabel();
		addTitle(this.descriptionLabel, "mainframe.descriptionTitle", 5, 5);
		this.getContentPane().add(this.descriptionLabel, BorderLayout.NORTH);

		this.screenPanel = new JPanel();
		this.screenPanel.setLayout(new GridLayout(1,1));
		addTitle(this.screenPanel, "mainframe.descriptionTitle", 5, 5);
		this.getContentPane().add(this.screenPanel, BorderLayout.CENTER);

		this.navigationPanel = new JPanel();
		this.navigationPanel.setLayout(new GridLayout(1, 5));
		addTitle(this.navigationPanel, "mainframe.descriptionTitle", 5, 5);
		this.getContentPane().add(this.navigationPanel, BorderLayout.SOUTH);
		
		this.previousButton = new JButton();
		this.previousButton.addActionListener(this);
		this.aboutButton = new JButton(Language.getString("mainframe.aboutbutton"));
		this.aboutButton.addActionListener(this);
		this.nextButton = new JButton();
		this.nextButton.addActionListener(this);
		this.navigationPanel.add(this.previousButton);
		this.navigationPanel.add(new JPanel());
		this.navigationPanel.add(this.aboutButton);
		this.navigationPanel.add(new JPanel());
		this.navigationPanel.add(this.nextButton);

		initializeStartScreen();
		setStart();

		this.setLocation(0, 0);
		this.setMinimumSize(size);
		this.setMaximumSize(size);
		this.setSize(size);
		this.setPreferredSize(size);
		this.setVisible(true);
		this.addWindowListener(this);
		
		// ToolTipManager konfigurieren
        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setReshowDelay(100);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
	}

	private void initializeStartScreen()
	{
		this.startScreen = new StartScreen(this.karopapier, this.previousButton, this.nextButton);
	}

	private void setStart()
	{
		this.setScreen(startScreen);
	}

	public void setScreen(Screen screen)
	{
		screen.updateBeforeShow(this.gameSeries);		
		this.currentScreen = screen;
		this.screenPanel.removeAll();
		this.screenPanel.add(screen);
		this.descriptionLabel.setText(Language.getString(screen.getHeaderKey()));
		this.previousButton.setEnabled(screen.getPrevious() != null);
		this.previousButton.setText(Language.getString("navigation.previous"));
		this.nextButton.setText(Language.getString(screen.getNextKey()));
		repaint();
	}

	private void previous()
	{
		if(this.currentScreen instanceof SummaryScreen)
		{
			int result = JOptionPane.showConfirmDialog(this, Language.getString("navigation.summaryprevious"), Language.getString("navigation.sure"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(result != JOptionPane.OK_OPTION)
				return;
		}
		try
		{
			this.gameSeries = this.currentScreen.applySettings(this.gameSeries);
		}
		catch(GameSeriesException e)
		{
		}
		setScreen(this.currentScreen.getPrevious());
	}

	private void next()
	{
		if(this.currentScreen instanceof SummaryScreen)
		{
			if(((SummaryScreen) this.currentScreen).gamesToCreate())
			{
				int result = JOptionPane.showConfirmDialog(this, Language.getString("navigation.summarynext"), Language.getString("navigation.sure"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(result != JOptionPane.OK_OPTION)
					return;
			}
		}
		try
		{
			this.gameSeries = this.currentScreen.applySettings(this.gameSeries);
		}
		catch(GameSeriesException e)
		{
			showError(e);
			return;
		}
		
		if(this.currentScreen.getNext() != null)
		{
			setScreen(this.currentScreen.getNext());
		}
	}
	
	private void showError(GameSeriesException e)
	{
		JOptionPane.showMessageDialog(this, Language.getString(e.getMessage()) + (e.getSpecification() == null ? "" : "\n -> " + e.getSpecification()), Language.getString("error.title"), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Fügt einen Rahmen mit Titel zu einer Komponente hinzu
	 * 
	 * @param component - die zu rahmende Komponente
	 * @param titleKey - der Titel
	 * @param outersize - der Abstand außerhalb des Rahmens zu anderen
	 *            Komponenten
	 * @param innersize - der Abstand innerhalb des Rahmens zum Inhalt der
	 *            Komponente
	 */
	private void addTitle(JComponent component, String titleKey, int outersize, int innersize)
	{
		Border b = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(outersize, outersize, outersize, outersize), BorderFactory
				.createCompoundBorder(BorderFactory.createTitledBorder(Language.getString(titleKey)), BorderFactory.createEmptyBorder(innersize,
						innersize, innersize, innersize)));
		component.setBorder(b);
	}

	@Override
	public void windowActivated(WindowEvent e)
	{
	}

	@Override
	public void windowClosed(WindowEvent e)
	{
	}

	@Override
	public void windowClosing(WindowEvent e)
	{
		Main.exit();
	}

	@Override
	public void windowDeactivated(WindowEvent e)
	{
	}

	@Override
	public void windowDeiconified(WindowEvent e)
	{
	}

	@Override
	public void windowIconified(WindowEvent e)
	{
	}

	@Override
	public void windowOpened(WindowEvent e)
	{
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(this.nextButton))
		{
			next();
		}
		else if(e.getSource().equals(this.previousButton))
		{
			previous();
		}
		else if(e.getSource().equals(this.aboutButton))
		{
			JLabel aboutLabel = new JLabel(Language.getString("mainframe.about"));
			JScrollPane aboutSP = new JScrollPane(aboutLabel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			aboutSP.setPreferredSize(aboutSize);
			aboutSP.setSize(aboutSize);
			aboutSP.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			JOptionPane.showMessageDialog(this, aboutSP, Language.getString("mainframe.aboutbutton"), JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
