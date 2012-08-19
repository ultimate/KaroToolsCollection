package ultimate.karoapi4j.utils.web;

import ultimate.karoapi4j.utils.sync.Loader;

public interface URLLoader<T> extends Loader<T>
{
	public T prepare(String refreshed);
}
