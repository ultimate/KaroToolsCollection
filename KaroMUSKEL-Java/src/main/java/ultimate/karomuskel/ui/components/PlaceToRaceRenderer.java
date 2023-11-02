package ultimate.karomuskel.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;

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

	public static final int									imageWidth			= 100;
	public static final int									imageHeight			= 50;

	private static final HashMap<PlaceToRace, ImageIcon>	images				= new HashMap<PlaceToRace, ImageIcon>();

	public PlaceToRaceRenderer()
	{
		setOpaque(true);
		setHorizontalAlignment(LEFT);
		setVerticalAlignment(CENTER);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends PlaceToRace> list, PlaceToRace value, int index, boolean isSelected, boolean cellHasFocus)
	{
		if(isSelected)
		{
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		}
		else
		{
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		setFont(list.getFont());

		// Set the icon and text. If icon was null, say so.
		String name = null;
		ImageIcon icon = null;
		if(value instanceof Map)
		{
			Map map = (Map) value;

			name = map.toString();

			if(!images.containsKey(map) && (map.getImage() != null))
			{
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
				images.put(value, new ImageIcon(bi));
			}
			icon = images.get(value);
			if(icon == null)
				name = "[" + Language.getString("image.notavailable") + "] " + name;

		}
		else if(value instanceof Generator)
		{
			Generator generator = (Generator) value;
			
			name = generator.getName();
			
			char symbol = (name != null ? name.charAt(0) : (char) 0);
			icon = new ImageIcon(ImageUtil.createSpecialImage(ImageUtil.createSingleColorImage(imageWidth, imageHeight, Color.white), symbol));	
		}
		else
		{
			logger.error("unknown PlaceToRace type: " + value);
		}

		setText(name);
		setIcon(icon);
		return this;
	}
}
