package ultimate.karoapi4j.utils.sync;

public interface Refreshable<T>
{
	public void onRefresh(T refreshed);
}
