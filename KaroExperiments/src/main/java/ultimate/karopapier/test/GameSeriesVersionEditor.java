package ultimate.karopapier.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.swing.JFileChooser;

import muskel2.Main;
import muskel2.model.GameSeries;

public class GameSeriesVersionEditor
{
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) throws Exception
	{
		JFileChooser fileChooser = new JFileChooser();
		int result = fileChooser.showOpenDialog(null);
		if(result != JFileChooser.APPROVE_OPTION)
			return;
		File file = fileChooser.getSelectedFile();

		Main.main(new String[] { "-l=debug" });
		Main.getGui().setVisible(false);

		GameSeries gs = loadGameseries(file);		

		final Field f = GameSeries.class.getDeclaredField("serialVersionUID");
		
		AccessController.doPrivileged(new PrivilegedAction() {
            @Override
            public Object run() {
            	try
            	{
	        		Field modifiersField = Field.class.getDeclaredField("modifiers");
	        	    modifiersField.setAccessible(true);
	        	    modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
	        	    f.setAccessible(true);

	        		f.setLong(null, 2L);	
            	}
            	catch(Exception e)
            	{
            		
            	}
                return null;
            }
        });			
			

		System.out.println("gameSeries loaded");
		
		saveGameseries(new File(file.getAbsolutePath() + ".mod"), gs);

		System.out.println("gameSeries saved");
	}

	private static GameSeries loadGameseries(File file) throws IOException, ClassNotFoundException
	{
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ObjectInputStream ois = new ObjectInputStream(bis);

		GameSeries gameSeries = (GameSeries) ois.readObject();

		ois.close();
		bis.close();
		fis.close();

		return gameSeries;
	}	


	protected static void saveGameseries(File file, GameSeries gameSeries) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		ObjectOutputStream oos = new ObjectOutputStream(bos);

		oos.writeObject(gameSeries);

		oos.flush();
		bos.flush();
		fos.flush();

		oos.close();
		bos.close();
		fos.close();
	}
}
