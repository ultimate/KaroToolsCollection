package ultimate.karomuskel.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.function.Function;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.model.extended.PlaceToRace;
import ultimate.karoapi4j.model.official.Generator;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.utils.ImageUtil;
import ultimate.karomuskel.ui.Language;

public class PlaceToRaceRenderer extends JLabel implements ListCellRenderer<PlaceToRace>
{
	private static final long								serialVersionUID	= 1L;

	protected transient final Logger						logger				= LogManager.getLogger(getClass());

	public static final int									IMAGE_WIDTH_LARGE	= 100;
	public static final int									IMAGE_HEIGHT_LARGE	= 50;
	public static final int									IMAGE_WIDTH_SMALL	= 40;
	public static final int									IMAGE_HEIGHT_SMALL	= 20;

	private static final HashMap<PlaceToRace, ImageIcon>	IMAGES_LARGE		= new HashMap<PlaceToRace, ImageIcon>();
	private static final HashMap<PlaceToRace, ImageIcon>	IMAGES_SMALL		= new HashMap<PlaceToRace, ImageIcon>();

	private boolean											small;

	private Function<PlaceToRace, String>					conditionalMessage;

	public PlaceToRaceRenderer()
	{
		this(null);
	}

	public PlaceToRaceRenderer(Function<PlaceToRace, String> conditionalMessage)
	{
		this(conditionalMessage, false);
	}

	public PlaceToRaceRenderer(Function<PlaceToRace, String> conditionalMessage, boolean small)
	{
		this.conditionalMessage = conditionalMessage;
		this.small = small;
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends PlaceToRace> list, PlaceToRace value, int index, boolean isSelected, boolean cellHasFocus)
	{
		this.setOpaque(true);
		this.setHorizontalAlignment(JLabel.LEFT);
		this.setVerticalAlignment(JLabel.CENTER);

		if(isSelected)
		{
			this.setBackground(list.getSelectionBackground());
			this.setForeground(list.getSelectionForeground());
		}
		else
		{
			this.setBackground(list.getBackground());
			this.setForeground(list.getForeground());
		}
		this.setFont(list.getFont());

		// Set the icon
		ImageIcon icon = null;
		HashMap<PlaceToRace, ImageIcon> images = (small ? IMAGES_SMALL : IMAGES_LARGE);
		if(!images.containsKey(value))
			images.put(value, createIcon(value, small));
		icon = images.get(value);
		this.setIcon(icon);

		// Set the text
		this.setText((icon == null ? "[" + Language.getString("image.notavailable") + "] " : "") + value.toString());

		// Set tooltip
		String msg;
		if(this.conditionalMessage != null && (msg = this.conditionalMessage.apply(value)) != null)
			this.setToolTipText(msg);
		else
			this.setToolTipText(null);

//		logger.debug("name = " + name);
//		logger.debug("icon = " + icon);

		return this;
	}

	private ImageIcon createIcon(PlaceToRace ptr, boolean small)
	{
		int imageWidth, imageHeight;
		if(small)
		{
			imageWidth = IMAGE_WIDTH_SMALL;
			imageHeight = IMAGE_HEIGHT_SMALL;
		}
		else
		{
			imageWidth = IMAGE_WIDTH_LARGE;
			imageHeight = IMAGE_HEIGHT_LARGE;
		}

		if(ptr instanceof Map)
		{
			Map map = (Map) ptr;

			Image image = map.getImage();
			BufferedImage bi = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g2d = bi.createGraphics();

			double dH = (double) imageHeight / (double) image.getHeight(null);
			double dW = (double) imageWidth / (double) image.getWidth(null);
			int hOrg = image.getHeight(null);
			int wOrg = image.getWidth(null);

			if(image != null)
			{
				if(dH < dW)
					g2d.drawImage(image, (int) (imageWidth - dH * wOrg) / 2, 0, (int) (dH * wOrg), (int) (dH * hOrg), null);
				else
					g2d.drawImage(image, 0, (int) (imageHeight - dW * hOrg) / 2, (int) (dW * wOrg), (int) (dW * hOrg), null);
			}
			return new ImageIcon(bi);
		}
		else if(ptr instanceof Generator)
		{
			String name = ((Generator) ptr).toSettingsString(true);
			char symbol = (name != null ? name.charAt(0) : (char) 0);
			if(((Generator) ptr).isNight())
				return new ImageIcon(ImageUtil.createSpecialImage(ImageUtil.createSingleColorImage(imageWidth, imageHeight, Color.black), symbol, Color.white));
			else
				return new ImageIcon(ImageUtil.createSpecialImage(ImageUtil.createSingleColorImage(imageWidth, imageHeight, Color.white), symbol, Color.black));
		}
		else
		{
			logger.error("unknown PlaceToRace type: " + ptr);
			return null;
		}
	}
}
