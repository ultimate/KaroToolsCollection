package ultimate.karoapi4j.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Util class for image operations
 * 
 * @author ultimate
 */
public abstract class ImageUtil
{
	public enum Symbol
	{
		DONT,
		GENERATOR
	}
	
	public static BufferedImage createSingleColorImage(int width, int height, Color color)
	{
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = image.createGraphics();
		g.setColor(color);
		g.fillRect(0, 0, width, height);
		return image;
	}

	/**
	 * Create a special image by drawing a symbol on top of the given image...
	 * 
	 * @param image - the original image
	 * @return the specialized image
	 */
	public static BufferedImage createSpecialImage(BufferedImage image, Symbol symbol)
	{
		BufferedImage image2 = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2d = image2.createGraphics();
		g2d.drawImage(image, 0, 0, null);

		int size = (int) Math.min(image2.getWidth() * 0.7F, image2.getHeight() * 0.7F);
		int centerX = image2.getWidth() / 2;
		int centerY = image2.getHeight() / 2;
				
		switch(symbol)
		{
			case DONT:
				drawDont(g2d, size, centerX, centerY);
				break;
			case GENERATOR:
				drawGenerator(g2d, size, centerX, centerY);
				break;
			default:
				break;
		}

		return image2;
	}
	
	private static void drawDont(Graphics2D g2d, int size, int centerX, int centerY)
	{
		g2d.setColor(Color.red);
		g2d.setStroke(new BasicStroke(size / 7));
		g2d.drawOval(centerX - size/2, centerY - size/2, size, size);
		int delta = (int) (size / 2 * 0.707F);
		g2d.drawLine(centerX - delta, centerY + delta, centerX + delta, centerY - delta);
	}
	
	private static void drawGenerator(Graphics2D g2d, int size, int centerX, int centerY)
	{
		g2d.setFont(Font.getFont("Arial").deriveFont(size));
		g2d.drawString("\uF09F8EA8", centerX, centerY); // palette
		g2d.drawString("\uF09F968C", centerX, centerY); // brush
	}
}