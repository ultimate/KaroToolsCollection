package ultimate.karomuskel.ui.components;

import javax.swing.AbstractSpinnerModel;
import javax.swing.JSpinner;

public class AllCombinationsNumberModel extends AbstractSpinnerModel
{
	private static final long serialVersionUID = 1L;
	private int value;
	private int min = 2;
	private JSpinner numberOfTeamsSpinner;
	
	public AllCombinationsNumberModel(int value, JSpinner numberOfTeamsSpinner)
	{
		this.setValue(value);
		this.numberOfTeamsSpinner = numberOfTeamsSpinner;
	}

	public int getMin()
	{
		return min;
	}

	public int getMax()
	{
		return (Integer) numberOfTeamsSpinner.getValue();
	}

	@Override
	public Object getNextValue()
	{
		if(value == getMax())
			return getMax();
		return value+1;
	}

	@Override
	public Object getPreviousValue()
	{
		if(value == min)
			return min;
		return value-1;
	}

	@Override
	public Object getValue()
	{
		return value;
	}

	@Override
	public void setValue(Object value)
	{
		this.value = (Integer) value;
		fireStateChanged();
	}
}
