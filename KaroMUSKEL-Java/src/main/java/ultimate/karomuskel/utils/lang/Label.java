package ultimate.karomuskel.utils.lang;

public class Label<V>
{
	private String label;
	private V value;
	
	public Label(String label, V value)
	{
		super();
		this.label = label;
		this.value = value;
	}

	public String getLabel()
	{
		return label;
	}

	public V getValue()
	{
		return value;
	}
	
	public String toString()
	{
		return label;
	}
}
