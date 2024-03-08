package ultimate.karopapier;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ImageToMapCode
{
	public static void main(String[] args)
	{
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG images", "png");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION)
		{
			System.out.println("Loading image: " + chooser.getSelectedFile().getAbsolutePath());
			
			try
			{
				BufferedImage image = ImageIO.read(chooser.getSelectedFile());
				
				StringBuilder sb = new StringBuilder();
				char field;
				Map<Character, Integer> counts = new HashMap<>();
			
				for(int y = 0; y < image.getHeight(); y++)
				{
					for(int x = 0; x < image.getWidth(); x++)
					{
						field = colorToChar(image.getRGB(x, y));
						
						if(counts.containsKey(field))
							counts.put(field, counts.get(field)+1);
						else
							counts.put(field, 1);
						
						sb.append(field);
					}
					sb.append("\n");
				}
				System.out.println("");
				System.out.println("--------------------------------------------------------------------------------------------");
				System.out.println("");
				System.out.println(sb.toString());
				System.out.println("");
				System.out.println("--------------------------------------------------------------------------------------------");
				System.out.println("");
				
				for(Entry<Character, Integer> e: counts.entrySet())
					System.out.println(e.getKey() + " = " + e.getValue());
				
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(chooser.getSelectedFile().getAbsolutePath().replace(".png", ".txt"))));
				bos.write(sb.toString().getBytes());
				bos.flush();
				bos.close();
			}
			catch(IOException e)
			{
				System.out.println("IOException: " + e.getMessage());
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Canceled");
		}
	}
	
	private static char colorToChar(int pixel)
	{
		// using MS paint standard colors
		//   TV7L56XW1S
		//   NSZ8GY4329
		// * are colors not present in the standard palette but added for convenience
		switch(pixel)
		{
			case 0xFF000000: 	return 'T'; // black
			case 0xFFFFFFFF:	return 'N'; // white
			case 0xFF3F3F3F:			    // * dark grey
			case 0xFF7F7F7F:	return 'V'; // medium grey
			case 0xFFC3C3C3:	return 'O'; // light grey
			case 0xFF880015:	return '7'; // dark reddish brown
			case 0xFFB97A57:	return 'Z'; // light brown
			case 0xFFFF0000:				// * pure red
			case 0xFFED1C24:	return 'L'; // red
			case 0xFFFFAEC9:	return '8'; // light red / rose
			case 0xFFFF7F27:	return '5'; // orange
			case 0xFFFFC90E:	return 'G'; // gold
			case 0xFFFFF200:	return '6'; // yellow
			case 0xFFEFE4B0:	return 'Y'; // beige
			case 0xFF00FF00:				// * pure green
			case 0xFF22B14C:	return 'X'; // green
			case 0xFFB5E61D:	return '4'; // light green
			case 0xFF0000FF:				// * pure blue
			case 0xFF00A2E8:	return 'W'; // blue
			case 0xFF99D9EA:	return '3'; // light blue
			case 0xFF3F48CC:	return '1'; // dark blue
			case 0xFF7092BE:	return '2'; // pale blue
			case 0xFFA349A4:	return 'S'; // purple
			case 0xFFC8BFE7:	return '9'; // pale purple
			default:		 	return '?';
		}
	}
}
