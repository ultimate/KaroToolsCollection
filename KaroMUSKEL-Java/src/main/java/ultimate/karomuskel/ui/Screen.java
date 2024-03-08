package ultimate.karomuskel.ui;

import java.awt.Image;
import java.util.function.Predicate;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.exceptions.GameSeriesException;
import ultimate.karoapi4j.model.extended.GameSeries;

public abstract class Screen extends JPanel
{
	private static final long			serialVersionUID	= 1L;

	protected transient final Logger	logger				= LogManager.getLogger(getClass());

	protected static final int			spinnerColumns		= 14;
	protected static final int			insetsV				= 3;
	protected static final int			insetsH				= 10;

	protected static final int			cellWidth			= 200;
	protected static final int			totalWidth			= 800;
	protected static final int			lineHeight			= 20;

	protected KaroAPICache				karoAPICache;

	protected MainFrame					gui;

	protected JButton					previousButton;
	protected JButton					nextButton;

	protected Screen					previous;
	protected Screen					next;

	protected String					headerKey;

	protected boolean					skip				= false;
	protected boolean					firstShow			= true;

	public Screen(MainFrame gui, Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton, String headerKey)
	{
		super();
		this.gui = gui;
		this.previous = previous;
		if(previous != null)
			previous.next = this;
		this.karoAPICache = karoAPICache;
		this.previousButton = previousButton;
		this.nextButton = nextButton;
		this.headerKey = headerKey;
	}

	public MainFrame getGui()
	{
		return gui;
	}

	public Screen getPrevious()
	{
		return previous;
	}

	public Screen getNext()
	{
		return next;
	}

	public String getNextKey()
	{
		return "navigation.next";
	}

	public String getHeaderKey()
	{
		return headerKey;
	}

	public void setPrevious(Screen previous)
	{
		this.previous = previous;
	}

	public void setNext(Screen next)
	{
		this.next = next;
	}

	public boolean isSkip()
	{
		return skip;
	}

	public void setSkip(boolean skip)
	{
		this.skip = skip;
	}

	public String confirm(EnumNavigation direction)
	{
		return null;
	}

	/**
	 * Update the screen before showing it.
	 * The method may optionally return a String message to show. If null is returned no message is shown.
	 * 
	 * @param gameSeries
	 * @param direction
	 * @return
	 */
	public abstract Message updateBeforeShow(GameSeries gameSeries, EnumNavigation direction);

	/**
	 * Apply the settings from this screen to the gameseries before changing the screen
	 * 
	 * @param gameSeries
	 * @param direction
	 * @return
	 * @throws GameSeriesException
	 */
	public abstract GameSeries applySettings(GameSeries gameSeries, EnumNavigation direction) throws GameSeriesException;

	/**
	 * Find a screen for the given criteria in the navigation direction
	 * 
	 * @param predicate
	 * @param direction
	 * @return
	 */
	public Screen findScreen(Predicate<Screen> predicate, EnumNavigation direction)
	{
		Screen cursor = this;
		if(direction == EnumNavigation.next)
		{
			do
			{
				cursor = cursor.getNext();
				if(predicate.test(cursor))
					return cursor;
			} while(cursor.getNext() != null && cursor != this);
		}
		else if(direction == EnumNavigation.previous)
		{
			do
			{
				cursor = cursor.getPrevious();
				if(predicate.test(cursor))
					return cursor;
			} while(cursor.getNext() != null && cursor != this);
		}
		return null;
	}

	protected void setIcons(JRadioButton radioButton, Integer size)
	{
		radioButton.setIcon(createIcon("img/Radio-Normal.gif", size));
		radioButton.setPressedIcon(createIcon("img/Radio-Pressed.gif", size));
		radioButton.setRolloverIcon(createIcon("img/Radio-Normal-Hover.gif", size));
		radioButton.setRolloverSelectedIcon(createIcon("img/Radio-Active-Hover.gif", size));
		radioButton.setSelectedIcon(createIcon("img/Radio-Active.gif", size));
	}

	protected ImageIcon createIcon(String src, Integer size)
	{
		ImageIcon imageIcon = new ImageIcon(getClass().getClassLoader().getResource(src));
		if(size == null || imageIcon.getIconHeight() == size)
			return imageIcon;
		return new ImageIcon(imageIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
	}

	public class Message
	{
		public String	text;
		public int		type;

		public Message(String text, int type)
		{
			super();
			this.text = text;
			this.type = type;
		}
	}
}
