package karopapier.application;

import java.net.URL;

public class Test {

	public static void main(String[] args) throws Exception{
		ThreadQueue q;
		long time;

		System.out.println("TEST 1 - 100 urls hintereinander Limit 1");
		time = System.currentTimeMillis();
		q = new ThreadQueue(1, true);
		for(int i = 0; i < 100; i++) {
			URL url = new URL("http://www.karopapier.de/showmap.php");
			String param = "GID=" + (21880 + i) + "&pixel=10&karoborder=1";
			URLLoaderThread th = new URLLoaderThread(url, param); 			
			q.addThread(th);
			System.out.println(i + "-ter Thread added");
		}
		q.waitForFinisched();
		time = System.currentTimeMillis()-time;
		System.out.println(time);
		
		Thread.sleep(1000);
		
		System.out.println("TEST 2 - 100 urls hintereinander Limit 10");
		time = System.currentTimeMillis();
		q = new ThreadQueue(5, true);
		for(int i = 0; i < 100; i++) {
			URL url = new URL("http://www.karopapier.de/showmap.php");
			String param = "GID=" + (21880 + i) + "&pixel=10&karoborder=1";
			URLLoaderThread th = new URLLoaderThread(url, param); 			
			q.addThread(th);
			System.out.println(i + "-ter Thread added");
		}
		q.waitForFinisched();
		time = System.currentTimeMillis()-time;
		System.out.println(time);
		
		Thread.sleep(1000);
		
		System.out.println("TEST 2 - 100 urls hintereinander Limit 100");
		time = System.currentTimeMillis();
		q = new ThreadQueue(5, true);
		for(int i = 0; i < 100; i++) {
			URL url = new URL("http://www.karopapier.de/showmap.php");
			String param = "GID=" + (21880 + i) + "&pixel=10&karoborder=1";
			URLLoaderThread th = new URLLoaderThread(url, param); 			
			q.addThread(th);
			System.out.println(i + "-ter Thread added");
		}
		q.waitForFinisched();
		time = System.currentTimeMillis()-time;
		System.out.println(time);
	}
}
