package ultimate.karopapier.test;

import java.net.MalformedURLException;
import java.net.URL;

import ultimate.karoapi4j.utils.URLLoaderUtil;

public class TestSomething
{
	public static void main(String[] args) throws MalformedURLException
	{
		for(int i = 96000; i > 0; i--)
		{
			URLLoaderUtil.load(new URL("http://www.wlg.de/karotools/index.php5"), "action=ersterzug&GameID=" + i);
			System.out.println("ID "+  i + " loaded!");
		}
	}
}
