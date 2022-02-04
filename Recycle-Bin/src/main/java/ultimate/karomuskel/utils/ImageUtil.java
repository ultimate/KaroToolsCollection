package ultimate.karomuskel.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public abstract class ImageUtil
{
	public static final int				DEFAULT_IMAGE_SIZE		= 100;
	public static final int				DEFAULT_IMAGE_WIDTH		= DEFAULT_IMAGE_SIZE;
	public static final int				DEFAULT_IMAGE_HEIGHT	= DEFAULT_IMAGE_SIZE / 2;
	public static final BufferedImage	DEFAULT_IMAGE_WHITE;
	public static final BufferedImage	DEFAULT_IMAGE_GRAY;
	public static final BufferedImage	DEFAULT_IMAGE_BLACK;

	static
	{
		DEFAULT_IMAGE_WHITE = new BufferedImage(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2dw = DEFAULT_IMAGE_WHITE.createGraphics();
		g2dw.setColor(Color.white);
		g2dw.fillRect(0, 0, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);

		DEFAULT_IMAGE_GRAY = new BufferedImage(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2dg = DEFAULT_IMAGE_GRAY.createGraphics();
		g2dg.setColor(Color.gray);
		g2dg.fillRect(0, 0, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);

		DEFAULT_IMAGE_BLACK = new BufferedImage(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2db = DEFAULT_IMAGE_BLACK.createGraphics();
		g2db.setColor(Color.black);
		g2db.fillRect(0, 0, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
	}

	/**
	 * Create a special image by drawing a "don't sign" on top of the given image...
	 * 
	 * @param image - the original image
	 * @return the specialized image
	 */
	public static Image transformToSpecialImage(Image image)
	{
		BufferedImage image2 = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2d = image2.createGraphics();
		g2d.drawImage(image, 0, 0, null);
		g2d.setColor(Color.red);
		int size = (int) Math.min(image2.getWidth() * 0.7F, image2.getHeight() * 0.7F);
		g2d.setStroke(new BasicStroke(size / 7));
		g2d.drawOval((image2.getWidth() - size) / 2, (image2.getHeight() - size) / 2, size, size);
		int delta = (int) (size / 2 * 0.707F);
		g2d.drawLine(image2.getWidth() / 2 - delta, image2.getHeight() / 2 + delta, image2.getWidth() / 2 + delta, image2.getHeight() / 2 - delta);
		return image2;
	}
}
