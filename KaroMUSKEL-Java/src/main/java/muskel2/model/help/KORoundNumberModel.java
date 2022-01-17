package muskel2.model.help;

import javax.swing.AbstractSpinnerModel;

public class KORoundNumberModel extends AbstractSpinnerModel
{
	private int value;
	private int min = 2;
	private int max;
	
	public KORoundNumberModel(int value, int max)
	{
		this.setValue(value);
		this.max = max;
	}

	public int getMin()
	{
		return min;
	}

	public int getMax()
	{
		return max;
	}

	@Override
	public Object getNextValue()
	{
		if(value == max)
			return max;
		return value*2;
	}

	@Override
	public Object getPreviousValue()
	{
		if(value == min)
			return min;
		return value/2;
	}

	@Override
	public Object getValue()
	{
		return value;
	}

	@Override
	public void setValue(Object value)
	{
		int i = 1;
		while(true)
		{
			i *= 2;
			if(i > (Integer) value)
			{
				throw new IllegalArgumentException("value must be a power of 2");
			}
			if(i == (Integer) value)
				break;
		}
		this.value = (Integer) value;
		fireStateChanged();
	}

}
