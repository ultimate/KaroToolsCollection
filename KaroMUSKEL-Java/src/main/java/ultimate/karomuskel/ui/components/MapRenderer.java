package ultimate.karomuskel.ui.components;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import ultimate.karoapi4j.model.official.Map;
import ultimate.karomuskel.ui.Language;

@Deprecated
public class MapRenderer extends JLabel implements ListCellRenderer<Map>
{
	private static final long						serialVersionUID	= 1L;

	public static final int							imageWidth			= 100;
	public static final int							imageHeight			= 50;

	private static final HashMap<Map, ImageIcon>	images				= new HashMap<Map, ImageIcon>();

	public MapRenderer()
	{
		setOpaque(true);
		setHorizontalAlignment(LEFT);
		setVerticalAlignment(CENTER);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Map> list, Map value, int index, boolean isSelected, boolean cellHasFocus)
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

		// Set the icon and text. If icon was null, say so.
		String name = value.toString();
		if(!images.containsKey(value) && (value.getImage() != null))
		{
			Image image = value.getImage();
			BufferedImage bi = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g2d = bi.createGraphics();

			double dH = (double) imageHeight / (double) image.getHeight(null);
			double dW = (double) imageWidth / (double) image.getWidth(null);
			int hOrg = image.getHeight(null);
			int wOrg = image.getWidth(null);

			if(image != null)
			{
				if(dH < dW)
				{
					g2d.drawImage(image, (int) (imageWidth - dH * wOrg) / 2, 0, (int) (dH * wOrg), (int) (dH * hOrg), null);
				}
				else
				{
					g2d.drawImage(image, 0, (int) (imageHeight - dW * hOrg) / 2, (int) (dW * wOrg), (int) (dW * hOrg), null);
				}
			}
			images.put(value, new ImageIcon(bi));
		}
		ImageIcon icon = images.get(value);
		if(icon == null)
			name = "[" + Language.getString("image.notavailable") + "] " + name;
		setIcon(icon);
		setText(name);
		setFont(list.getFont());
		return this;
	}
}
