import java.io.*;
import java.util.*;

public class FilterPlayerIDs
{
	public static void main(String[] args) throws Exception
  {
  	TreeMap<Integer, String> players = new TreeMap<Integer, String>();
  	String inFileName = "playerIDsFULL.txt";
    String outFileName = "playerIDsFILTERED.txt";

    System.out.println("Creating InputStreams");
    FileInputStream fis = new FileInputStream(inFileName);
    BufferedInputStream bis = new BufferedInputStream(fis);
    System.out.println("Reading File");
    while(true)
    {
      String id = "", name = "";
    	int currChar = bis.read();
      for(int i = 0; i < 3; i++)
      {
      	while((char)currChar != '=')
        {
        	currChar = bis.read();
        }
        currChar = bis.read();
      }
      while((char)currChar != '>')
      {
        id = id + (char)currChar;
        currChar = bis.read();
      }
      //System.out.print(id + " ");
      currChar = bis.read();
      currChar = bis.read();
      currChar = bis.read();
      currChar = bis.read();
      while((char)currChar != '<')
      {
        name = name + (char)currChar;
        currChar = bis.read();
      }
		 	//System.out.println(name);
      while( ((char)currChar != '\n') && (currChar != -1))
      {
      	currChar = bis.read();
      }
      players.put( Integer.parseInt(id), name);
      if(currChar == -1)
      	break;
    }
    System.out.println("Finished Reading");
    bis.close();
    fis.close();
    System.out.println("Closed InputStreams");
    System.out.println("Creating OutputStreams");
    FileOutputStream fos = new FileOutputStream(outFileName);
    BufferedOutputStream bos = new BufferedOutputStream(fos);
    System.out.println("Writing new File");
    String firstLine = "var allPlayers = new Array(	";
    for(int i = 0; i < firstLine.length(); i++)
    	bos.write(firstLine.charAt(i));
    Iterator<Integer> iter = players.keySet().iterator();
    boolean first = true;
    while(iter.hasNext())
    {
    	String line = "";
      int index = iter.next();
      String name = players.get(index);
      line = "new Player(" + index + ", \"" + name + "\")";
      if(first)
      	first = false;
      else
      {
	    	for(int i = 0; i < 14; i++)
	      	bos.write('\t');
      }
	    for(int i = 0; i < line.length(); i++)
	      bos.write(line.charAt(i));
      if(iter.hasNext())
      	bos.write(',');
      bos.write('\n');
    }
    for(int i = 0; i < 14; i++)
      bos.write('\t');
    String lastLine = ");";
    for(int i = 0; i < lastLine.length(); i++)
    	bos.write(lastLine.charAt(i));
    System.out.println("Finished Writing");
    bos.flush();
    bos.close();
    fos.close();
    System.out.println("Closed OutputStreams");
  }
}