package ultimate.karoapi4j.utils.sync;

public interface Loader<T>
{
	public void load(Refreshable<T> refreshable);
	
	public void load();
	
	public T getLoadedContent();
}
