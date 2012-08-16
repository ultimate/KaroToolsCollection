package ultimate.karoapi4j.utils.web;

import  ultimate.karoapi4j.utils.sync.Refreshable;

public interface URLLoader
{
	public void loadURL(Refreshable<String> refreshable);
	
	public void loadURL();
	
	public String getLoadedURLContent();
}
