package ultimate.karomuskel.ui.components;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class GenericListModel<K, V> implements ListModel<V>
{
	private List<ListDataListener>	listDataListeners;

	private Class<V>				valueClass;
	private TreeMap<K, V>			entries;
	private V[]						entryArray;

	private Function<V, Boolean>	filter;

	public GenericListModel(Class<V> valueClass, Map<K, V> entries)
	{
		this.valueClass = valueClass;
		this.entries = new TreeMap<K, V>(entries);
		mapToArray();
		this.listDataListeners = new LinkedList<ListDataListener>();
	}

	@Override
	public void addListDataListener(ListDataListener l)
	{
		this.listDataListeners.add(l);
	}

	public void addElement(K k, V v)
	{
		this.entries.put(k, v);
		mapToArray();
		fireListeners();
	}

	public V removeElement(K k)
	{
		V v = this.entries.remove(k);
		mapToArray();
		fireListeners();
		return v;
	}

	public boolean containsElement(V v)
	{
		return this.entries.containsValue(v);
	}

	public boolean containsKey(K k)
	{
		return this.entries.containsKey(k);
	}

	private void fireListeners()
	{
		for(ListDataListener l : listDataListeners)
		{
			l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, entries.size() - 1));
		}
	}

	@Override
	public V getElementAt(int index)
	{
		return (V) entryArray[index];
	}

	@Override
	public int getSize()
	{
		return entryArray.length;
	}

	@Override
	public void removeListDataListener(ListDataListener l)
	{
		this.listDataListeners.remove(l);
	}

	public V[] getEntryArray()
	{
		return entryArray;
	}

	@SuppressWarnings("unchecked")
	private void mapToArray()
	{
		List<V> tmp = new LinkedList<V>(entries.values());
		if(this.filter != null)
			tmp.removeIf(v -> !this.filter.apply(v));
		
		this.entryArray = tmp.toArray((V[]) Array.newInstance(valueClass, tmp.size()));
	}	

	public Function<V, Boolean> getFilter()
	{
		return filter;
	}

	public void setFilter(Function<V, Boolean> filter)
	{
		this.filter = filter;
		refresh();
	}

	/**
	 * Refresh this model and recreate the internal entryArray for example if the filter changed
	 */
	public void refresh()
	{
		// recreate array based on the filter set
		mapToArray();
		// fire listeners to update view
		fireListeners();
	}
}
