package ultimate.karomuskel.ui.components;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class GenericListModel<K, V> implements ListModel<V>
{
	private List<ListDataListener>	listDataListeners;

	private Class<V>				valueClass;
	private TreeMap<K, V>			entries;
	private V[]						entryArray;

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
		this.entryArray = (V[]) Array.newInstance(valueClass, entries.size());

		int i = 0;
		for(V v : entries.values())
		{
			entryArray[i++] = v;
		}
	}
}
