package ultimate.karomuskel.ui.components;

import javax.swing.DefaultComboBoxModel;

import ultimate.karomuskel.utils.Language;
import ultimate.karomuskel.utils.lang.Label;

public class BooleanModel extends DefaultComboBoxModel
{
	private static final long	serialVersionUID	= 1L;

	@SuppressWarnings("unchecked")
	public BooleanModel(Boolean value, boolean randomEnabled)
	{
		super();
		this.addElement(new Label<Boolean>(Language.getString("boolean.true"), true));
		this.addElement(new Label<Boolean>(Language.getString("boolean.false"), false));
		if(randomEnabled)
			this.addElement(new Label<Boolean>(Language.getString("boolean.random"), null));
		
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
