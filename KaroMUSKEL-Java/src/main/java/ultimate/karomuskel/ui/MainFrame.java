package ultimate.karomuskel.ui;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.exceptions.GameSeriesException;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.Launcher;
import ultimate.karomuskel.ui.screens.StartScreen;

public class MainFrame extends JFrame implements WindowListener, ActionListener
{
	private static final long			serialVersionUID	= 1L;

	protected transient final Logger	logger				= LogManager.getLogger(getClass());

	private static final Dimension		size				= new Dimension(1200, 900);
	private static final Dimension		aboutSize			= new Dimension(700, 500);

	private KaroAPICache				karoAPICache;

	private BorderLayout				layout;

	private JLabel						descriptionLabel;
	private JPanel						screenPanel;
	private JPanel						navigationPanel;

	private Screen						currentScreen;
	private Screen						startScreen;

	private JButton						previousButton;
	private JButton						nextButton;
	private JButton						aboutButton;

	private GameSeries					gameSeries;

	public MainFrame(String titleKey, KaroAPICache karoAPICache)
	{
		super(Language.getString(titleKey) + (karoAPICache.getKaroAPI() == null ? " - DEBUG-MODE" : ""));
		this.karoAPICache = karoAPICache;

		this.layout = new BorderLayout();
		this.getContentPane().setLayout(this.layout);

		this.descriptionLabel = new JLabel();
		addTitle(this.descriptionLabel, "mainframe.descriptionTitle", 5, 5);
		this.getContentPane().add(this.descriptionLabel, BorderLayout.NORTH);

		this.screenPanel = new JPanel();
		this.screenPanel.setLayout(new GridLayout(1, 1));
		addTitle(this.screenPanel, "mainframe.contentTitle", 5, 5);
		this.getContentPane().add(this.screenPanel, BorderLayout.CENTER);

		this.navigationPanel = new JPanel();
		this.navigationPanel.setLayout(new GridLayout(1, 5));
		addTitle(this.navigationPanel, "mainframe.navigationTitle", 5, 5);
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
		this.startScreen = new StartScreen(this, this.karoAPICache, this.previousButton, this.nextButton);
	}

	private void setStart()
	{
		this.setScreen(startScreen, EnumNavigation.next);
	}

	public void setScreen(Screen screen, EnumNavigation direction)
	{
		Screen.Message message = screen.updateBeforeShow(this.gameSeries, direction);
		this.currentScreen = screen;
		this.screenPanel.removeAll();
		this.screenPanel.add(screen);
		this.descriptionLabel.setText(Language.getString(screen.getHeaderKey()));
		this.previousButton.setEnabled(screen.getPrevious() != null);
		this.previousButton.setText(Language.getString("navigation.previous"));
		this.nextButton.setText(Language.getString(screen.getNextKey()));
		repaint();
		if(message != null)
			notify(message);
	}

	public boolean confirm(String messageKey)
	{
		if(messageKey == null)
			return true;
		int result = JOptionPane.showConfirmDialog(this, Language.getString(messageKey, Screen.totalWidth * 2 / 3),
				Language.getString("navigation.sure"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if(result == JOptionPane.OK_OPTION)
			return true;
		return false;
	}

	private void notify(String text, int type)
	{
		String titleKey;
		switch(type)
		{
			case JOptionPane.INFORMATION_MESSAGE:
				titleKey = "information.title";
				break;
			case JOptionPane.WARNING_MESSAGE:
				titleKey = "warning.title";
				break;
			case JOptionPane.QUESTION_MESSAGE:
				titleKey = "question.title";
				break;
			case JOptionPane.ERROR_MESSAGE:
			default:
				titleKey = "error.title";
				break;
		}

		JOptionPane.showMessageDialog(this, text, Language.getString(titleKey), type);
	}

	public void notify(Screen.Message message)
	{
		if(message == null)
			return;
		notify(message.text, message.type);
	}

	public void notify(GameSeriesException e, int type)
	{
		String message = Language.getString(e.getMessage(), e.getValue()) + (e.getSpecification() == null ? "" : "\n -> " + e.getSpecification());
		notify(message, type);
	}

	private void navigate(EnumNavigation direction)
	{
		if(!confirm(this.currentScreen.confirm(direction)))
			return;
		try
		{
			this.gameSeries = this.currentScreen.applySettings(this.gameSeries, direction);

			GameSeriesManager.autosave(this.gameSeries);

			Screen newScreen = this.currentScreen;

			if(direction == EnumNavigation.previous)
			{
				do
				{
					newScreen = newScreen.getPrevious();
				} while(newScreen.isSkip());
			}
			else if(direction == EnumNavigation.next)
			{
				do
				{
					newScreen = newScreen.getNext();
				} while(newScreen.isSkip());
			}

			if(newScreen.getNext() == null)
				throw new GameSeriesException("navigation.error");

			setScreen(newScreen, direction);
		}
		catch(GameSeriesException e)
		{
			notify(e, JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	/**
	 * F�gt einen Rahmen mit Titel zu einer Komponente hinzu
	 * 
	 * @param component - die zu rahmende Komponente
	 * @param titleKey - der Titel
	 * @param outersize - der Abstand au�erhalb des Rahmens zu anderen
	 *            Komponenten
	 * @param innersize - der Abstand innerhalb des Rahmens zum Inhalt der
	 *            Komponente
	 */
	private void addTitle(JComponent component, String titleKey, int outersize, int innersize)
	{
		Border b = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(outersize, outersize, outersize, outersize),
				BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Language.getString(titleKey)),
						BorderFactory.createEmptyBorder(innersize, innersize, innersize, innersize)));
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
		Launcher.exit();
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
			navigate(EnumNavigation.next);
		}
		else if(e.getSource().equals(this.previousButton))
		{
			navigate(EnumNavigation.previous);
		}
		else if(e.getSource().equals(this.aboutButton))
		{
			JLabel aboutLabel = new JLabel(Language.getAbout());
			JScrollPane aboutSP = new JScrollPane(aboutLabel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			aboutSP.setPreferredSize(aboutSize);
			aboutSP.setSize(aboutSize);
			aboutSP.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			JOptionPane.showMessageDialog(this, aboutSP, Language.getString("mainframe.aboutbutton"), JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
