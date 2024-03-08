package ultimate.karomuskel.ui.components;

import java.util.function.Function;

public interface FilterModel<V>
{
	Function<V, Boolean> getFilter();
	
	void setFilter(Function<V, Boolean> filter);

	void refresh();
}
