package ultimate.karoapi4j.utils.sync;

import ultimate.karoapi4j.enums.EnumRefreshMode;
import ultimate.karoapi4j.utils.web.URLLoader;
import ultimate.karoapi4j.utils.sync.Refreshable;

public interface Synchronized extends Refreshable<String>
{
	public void setURLLoader(URLLoader urlLoader);
	
	public URLLoader getURLLoader();
	
	public void refresh();

	public void setRefreshMode(EnumRefreshMode refreshMode);
	
	public EnumRefreshMode getRefreshMode();
	
	public void stopRefreshing();
}
