package ultimate.karopapier.eval.model;

import java.util.Map;
import java.util.TreeMap;

public abstract class Record<R extends Record<R, C>, C extends Comparable<C>> implements Comparable<R>, Cloneable
{
	protected String[]				labels;

	protected String				sortLabel;

	protected String				player;

	protected Map<String, Object>	values;

	protected Record()
	{
		this.values = new TreeMap<String, Object>();
	}

	public Record(String player)
	{
		this(null, null, player);
	}

	public Record(String[] labels, String sortLabel, String player)
	{
		this();
		this.labels = labels;
		this.sortLabel = sortLabel;
		this.player = player;
	}

	public String[] getLabels()
	{
		return this.labels;
	}

	public String getSortLabel()
	{
		return sortLabel;
	}

	public String getPlayer()
	{
		return this.player;
	}

	public Object[] getValues()
	{
		Object[] ret = new Object[labels.length];
		for(int i = 0; i < ret.length; i++)
		{
			ret[i] = values.get(labels[i]);
		}
		return ret;
	}

	public void setValue(String label, Object value)
	{
		boolean validLabel = false;
		for(String l : labels)
		{
			if(l.equals(label))
			{
				validLabel = true;
				break;
			}
		}
		if(!validLabel)
			throw new IllegalArgumentException("invalid label: '" + label + "'");
		if(label.equals(sortLabel))
		{
			// check if value is of type C
			@SuppressWarnings({ "unchecked", "unused" })
			C cast = (C) value;
		}
		values.put(label, value);
	}

	public Object getValue(String label)
	{
		boolean validLabel = false;
		for(String l : labels)
		{
			if(l.equals(label))
			{
				validLabel = true;
				break;
			}
		}
		if(!validLabel)
			throw new IllegalArgumentException("invalid label: '" + label + "'");
		return values.get(label);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(R record)
	{
		if(this.getValue(sortLabel) instanceof Number)
			return ((C) record.getValue(sortLabel)).compareTo((C) this.getValue(sortLabel));
		return ((C) this.getValue(sortLabel)).compareTo((C) record.getValue(sortLabel));
	}
	
	@Override
	public R clone()
	{
		try
		{
			@SuppressWarnings("unchecked")
			R r = (R) this.getClass().newInstance();
			r.player = this.player;
			r.labels = this.labels.clone();
			r.sortLabel = this.sortLabel;
			r.values.putAll(this.values);
			return r;
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
