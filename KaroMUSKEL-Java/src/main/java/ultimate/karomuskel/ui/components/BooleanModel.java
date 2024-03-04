package ultimate.karomuskel.ui.components;

import javax.swing.DefaultComboBoxModel;

import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.Language.Label;

public class BooleanModel extends DefaultComboBoxModel<Label<Boolean>>
{
	private static final long	serialVersionUID	= 1L;

	public BooleanModel(Boolean value)
	{
		this(value, false, null);
	}
	
	public BooleanModel(Boolean value, boolean nullEnabled)
	{
		this(value, nullEnabled, "option.boolean.null");
	}
	
	public BooleanModel(Boolean value, boolean nullEnabled, String nullLabelKey)
	{
		super();
		this.addElement(new Label<Boolean>(Language.getString("option.boolean.true"), true));
		this.addElement(new Label<Boolean>(Language.getString("option.boolean.false"), false));
		if(nullEnabled)
			this.addElement(new Label<Boolean>(Language.getString(nullLabelKey), null));
		
		for(int i = 0; i < this.getSize(); i++)
		{
			if(((Label<Boolean>) this.getElementAt(i)).getValue() == null && value == null)
			{
				this.setSelectedItem(this.getElementAt(i));
				break;				
			}
			if(((Label<Boolean>) this.getElementAt(i)).getValue().equals(value))
			{
				this.setSelectedItem(this.getElementAt(i));
				break;
			}
		}
	}
}
