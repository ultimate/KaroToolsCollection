package karopapier.application;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class URLLoaderThread extends QueuableThread{

	private URL url;
	private String parameter;
	private String result;
	
	public URLLoaderThread(URL url, String parameter) {
		super("URLLoaderThread for " + url + (parameter.equals("") ? "" : "?" + parameter));
		if(url == null)
			throw new IllegalArgumentException("The URL must not be null.");
		if(parameter == null)
			throw new IllegalArgumentException("The parameters must not be null. May be \"\".");
		this.url = url;
		this.parameter = parameter;
		this.result = null;
	}
	
	@Override
	public void innerRun(){		
		try {
			StringBuilder site = new StringBuilder();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		  	connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setAllowUserInteraction(false);
			connection.setRequestMethod("POST");
			
			PrintWriter out = new PrintWriter(connection.getOutputStream());
			out.print(parameter);
			out.close();
			
			connection.connect();
			InputStream is = connection.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			int curr = bis.read();
			while(curr != -1) {
				site.append((char)curr);
				curr = bis.read();
			}
			bis.close();
			is.close();
			
			result = site.toString();
		} catch(IOException e) {
			result = e.toString();
		}
	}
	
	public String getLoadedURLContent() {
		return result;
	}
}
