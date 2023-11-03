package ultimate.karomuskel.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.model.extended.PlaceToRace;
import ultimate.karoapi4j.model.official.Generator;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.utils.ImageUtil;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.dialog.GeneratorDialog;

public class PlaceToRaceRenderer implements ListCellRenderer<PlaceToRace>
{

	protected transient final Logger						logger			= LogManager.getLogger(getClass());

	public static final int									imageWidth		= 100;
	public static final int									imageHeight		= 50;
	public static final int									buttonPosition	= 250;
	public static final int									buttonWidth		= 30;
	public static final int									buttonHeight	= 30;

	private static final HashMap<PlaceToRace, ImageIcon>	images			= new HashMap<PlaceToRace, ImageIcon>();

	public PlaceToRaceRenderer()
	{
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends PlaceToRace> list, PlaceToRace value, int index, boolean isSelected, boolean cellHasFocus)
	{
		final JLabel label = new JLabel();
		label.setOpaque(true);
		label.setHorizontalAlignment(JLabel.LEFT);
		label.setVerticalAlignment(JLabel.CENTER);
		label.setSize(imageWidth * 3, imageHeight);

		if(isSelected)
		{
			label.setBackground(list.getSelectionBackground());
			label.setForeground(list.getSelectionForeground());
		}
		else
		{
			label.setBackground(list.getBackground());
			label.setForeground(list.getForeground());
		}
		label.setFont(list.getFont());

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

			if(generator.getUniqueId() > 0)
				name += " (" + generator.getUniqueId() + ")";

			if(generator.isEditable())
			{
				try
				{
					final JButton editButton = new JButton(new String(new byte[] { (byte) 0xE2, (byte) 0x9A, (byte) 0x99 }, "UTF-8"));
					final int margin = (imageHeight - buttonHeight) / 2;
					editButton.setLocation(buttonPosition - buttonWidth - margin, margin);
					editButton.setSize(buttonWidth, buttonHeight);
					editButton.addMouseListener(new MouseListener() {
						
						@Override
						public void mouseReleased(MouseEvent e)
						{
							logger.debug("released");
						}
						
						@Override
						public void mousePressed(MouseEvent e)
						{
							logger.debug("pressed");
						}
						
						@Override
						public void mouseExited(MouseEvent e)
						{
							logger.debug("exited");
						}
						
						@Override
						public void mouseEntered(MouseEvent e)
						{
							logger.debug("entered");
						}
						
						@Override
						public void mouseClicked(MouseEvent e)
						{
							logger.debug("clicked");
						}
					});
					editButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e)
						{
							logger.debug("showing generator settings...");
							int result = GeneratorDialog.getInstance().show(editButton, generator);
							if(result == JOptionPane.OK_OPTION)
							{
								logger.debug("updating generator settings...");
								logger.debug("from = " + generator.getSettings());
								generator.getSettings().putAll(GeneratorDialog.getInstance().getSettings());
								logger.debug("to   = " + generator.getSettings());
							}
						}
					});
					label.add(editButton);
				}
				catch(UnsupportedEncodingException e)
				{
					logger.error(e);
				}
			}

			char symbol = (name != null ? name.charAt(0) : (char) 0);
			icon = new ImageIcon(ImageUtil.createSpecialImage(ImageUtil.createSingleColorImage(imageWidth, imageHeight, Color.white), symbol));
		}
		else
		{
			logger.error("unknown PlaceToRace type: " + value);
		}

//		logger.debug("name = " + name);
//		logger.debug("icon = " + icon);

		label.setText(name);
		label.setIcon(icon);
		return label;
	}
}
