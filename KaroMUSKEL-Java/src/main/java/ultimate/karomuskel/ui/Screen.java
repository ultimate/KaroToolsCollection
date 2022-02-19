package ultimate.karomuskel.ui;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.exceptions.GameSeriesException;
import ultimate.karoapi4j.model.extended.GameSeries;

public abstract class Screen extends JPanel
{
	private static final long	serialVersionUID	= 1L;

	protected static final int	spinnerColumns		= 14;
	protected static final int	insetsV				= 3;
	protected static final int	insetsH				= 10;
	protected static final int	columnWidth			= 350;
	protected static final int	totalHeight			= 450;

	protected KaroAPICache		karoAPICache;

	protected JButton			previousButton;
	protected JButton			nextButton;

	protected Screen			previous;
	protected Screen			next;

	protected String			nextKey;

	protected String			headerKey;

	public Screen(Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton, String headerKey, String nextKey)
	{
		super();
		this.previous = previous;
		if(previous != null)
			previous.next = this;
		this.karoAPICache = karoAPICache;
		this.previousButton = previousButton;
		this.nextButton = nextButton;
		if(nextKey == null)
			this.nextKey = "navigation.next";
		else
			this.nextKey = nextKey;
		this.headerKey = headerKey;
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
		return nextKey;
	}

	public void setNextKey(String nextKey)
	{
		this.nextKey = nextKey;
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

	public abstract void updateBeforeShow(GameSeries gameSeries);

	public abstract GameSeries applySettings(GameSeries gameSeries) throws GameSeriesException;

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
		ImageIcon imageIcon = new ImageIcon(src);
		if(size == null || imageIcon.getIconHeight() == size)
			return imageIcon;
		return new ImageIcon(imageIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
	}
}
