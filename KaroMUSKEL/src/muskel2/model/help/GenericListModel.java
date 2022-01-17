package muskel2.model.help;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class GenericListModel<K, V> implements ListModel
{
	private List<ListDataListener> listDataListeners;
	
	private TreeMap<K, V> entries;
	private Object[] entryArray;
	
	public GenericListModel(Map<K, V> entries)
	{
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
	
	private void fireListeners()
	{
		for(ListDataListener l: listDataListeners)
		{
			l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, entries.size()-1));
		}
	}

	@SuppressWarnings("unchecked")
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
	
	public Object[] getEntryArray()
	{
		return entryArray;
	}

	private void mapToArray() {
		this.entryArray = new Object[entries.size()];
		
		int i = 0;
		for(V v: entries.values())
		{
			entryArray[i++] = v;
		}		
	}
}
