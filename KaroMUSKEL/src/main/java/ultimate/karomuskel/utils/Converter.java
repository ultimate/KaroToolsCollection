package ultimate.karomuskel.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import muskel2.model.GameSeries;

public abstract class Converter
{
	public static void main(String[] args) throws Exception
	{
		File f = new File("I:/Karopapier/other/CraZZZy Crash Challenge/czzzcc1ready.muskel");
		
		FileInputStream fis = new FileInputStream(f);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ObjectInputStream ois = new ObjectInputStream(bis);
		
		GameSeries gs = (GameSeries) ois.readObject();
		
		System.out.println(gs);
		System.out.println(gs.getTitle());
		
		ois.close();
	}
}
