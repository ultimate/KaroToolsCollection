package ultimate.karomuskel.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
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
	protected static final int			columnWidth			= 350;
	protected static final int			totalHeight			= 450;

	protected KaroAPICache				karoAPICache;

	protected JFrame					gui;

	protected JButton					previousButton;
	protected JButton					nextButton;

	protected Screen					previous;
	protected Screen					next;

	protected String					nextKey;

	protected String					headerKey;

	public Screen(JFrame gui, Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton, String headerKey, String nextKey)
	{
		super();
		this.gui = gui;
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

	public JFrame getGui()
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
		// radioButton.setPressedIcon(createIcon("img/Radio-Pressed.gif", size));
		// radioButton.setRolloverIcon(createIcon("img/Radio-Normal-Hover.gif", size));
		// radioButton.setRolloverSelectedIcon(createIcon("img/Radio-Active-Hover.gif", size));
		// radioButton.setSelectedIcon(createIcon("img/Radio-Active.gif", size));
	}

	public static double rot = -Math.PI;

	protected ImageIcon createIcon(String src, Integer size)
	{
		int drawSize = 100;
		BufferedImage image = new BufferedImage(drawSize, drawSize, BufferedImage.TYPE_4BYTE_ABGR);

		Color cBase = Color.red.darker();
		Color c;

		int rButton = drawSize * 4 / 5 / 2;
		int rTop = drawSize / 4;
		int cx = image.getWidth() / 2;
		int cy = image.getHeight() / 2;
		double rPixel;
		double thetaPixel;
		int r, g, b;
		double rotation = rot;
		rot += Math.PI/2;
		double deltaRot;
		
		for(int x = 0; x < drawSize; x++)
		{
			for(int y = 0; y < drawSize; y++)
			{
				rPixel = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
				thetaPixel = Math.atan2(y - cy, x - cx);
				deltaRot = (Math.abs(thetaPixel - rotation) / (Math.PI));
				while(deltaRot > 1)
					deltaRot -= 2;
				while(deltaRot < -1)
					deltaRot += 2;

				// logger.info(x+ "|" + y + ", \tr=" + rPixel + ", \ttheta=" + thetaPixel + ",\t" + deltaRot);
				if(rPixel < rButton)
				{
//					if(deltaRot > 0)
//					{
//						r = Math.min((int) (cBase.getRed() + deltaRot * (255 - cBase.getRed())), 255);
//						g = Math.min((int) (cBase.getGreen() + deltaRot * (255 - cBase.getGreen())), 255);
//						b = Math.min((int) (cBase.getBlue() + deltaRot * (255 - cBase.getBlue())), 255);
//					}
//					else
//					{
//						r = Math.max((int) (cBase.getRed() + deltaRot * (cBase.getRed())), 0);
//						g = Math.max((int) (cBase.getGreen() + deltaRot * (cBase.getGreen())), 0);
//						b = Math.max((int) (cBase.getBlue() + deltaRot * (cBase.getBlue())), 0);
//					}
					r = g = b = (int) (deltaRot * 100 + 100);
					
					c = new Color(r, g, b, cBase.getAlpha());
					if(rPixel < rTop)
						image.setRGB(x, y, cBase.getRGB());
					else
						image.setRGB(x, y, c.getRGB());
				}
			}
		}

		// int d = drawSize * 4 / 5;
		// int gap = (drawSize - d) / 2;
		// int shadowSize = gap;
		// int shadowStep = 255 / shadowSize;
		//
		// logger.info("drawSize = " + drawSize);
		// logger.info("d = " + d);
		// logger.info("gap = " + gap);
		// logger.info("shadowStep = " + shadowStep);
		//
		// g.setColor(new Color(0, 0, 0, shadowStep));
		// for(int i = 0; i < shadowSize; i++)
		// g.fillOval(gap + i, gap + i, d, d);
		//
		// g.setColor(Color.red.darker());
		// g.fillOval(gap, gap, d, d);

		return new ImageIcon(image.getScaledInstance(size, size, Image.SCALE_SMOOTH));

		 ImageIcon imageIcon = new ImageIcon(src);
		 if(size == null || imageIcon.getIconHeight() == size)
		 return imageIcon;
		 return new ImageIcon(imageIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
	}
}
