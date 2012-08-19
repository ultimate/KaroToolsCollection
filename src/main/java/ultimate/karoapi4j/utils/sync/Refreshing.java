package ultimate.karoapi4j.utils.sync;

public interface Refreshing<T>
{
	public void addRefreshable(Refreshable<T> refreshable);
	
	public boolean removeRefreshable(Refreshable<T> refreshable);
}
