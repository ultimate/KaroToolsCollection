package ultimate.karomuskel.ui.components;

import javax.swing.DefaultComboBoxModel;

import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.Language.Label;

public class BooleanModel extends DefaultComboBoxModel<Label<Boolean>>
{
	private static final long	serialVersionUID	= 1L;

	public BooleanModel(Boolean value)
	{
		this(value, false);
	}
	
	public BooleanModel(Boolean value, boolean nullEnabled)
	{
		this(value, "option.boolean.null", -1);
	}
	
	public BooleanModel(Boolean value, String nullLabel, int nullPosition)
	{
		super();	
		
		Label<Boolean> nullEntry = new Label<Boolean>(Language.getString(nullLabel), null);
		
		if(nullPosition == 0)
			this.addElement(nullEntry);
		this.addElement(new Label<Boolean>(Language.getString("option.boolean.true"), true));
		if(nullPosition == 1)
			this.addElement(nullEntry);
		this.addElement(new Label<Boolean>(Language.getString("option.boolean.false"), false));
		if(nullPosition == 2)
			this.addElement(nullEntry);
		
		Boolean vi;
		for(int i = 0; i < this.getSize(); i++)
		{
			vi = ((Label<Boolean>) this.getElementAt(i)).getValue();
			if(vi == null && value == null)
			{
				this.setSelectedItem(this.getElementAt(i));
				break;
			}
			else if(vi != null && vi.equals(value))
			{
				this.setSelectedItem(this.getElementAt(i));
				break;
			}
		}
	}
}
