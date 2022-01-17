package muskel2.model.help;

import javax.swing.DefaultComboBoxModel;

import muskel2.util.Language;

public class BooleanModel extends DefaultComboBoxModel
{
	private static final long	serialVersionUID	= 1L;

	@SuppressWarnings("unchecked")
	public BooleanModel(Boolean value, boolean randomEnabled)
	{
		super();
		this.addElement(new Label<Boolean>(Language.getString("option.boolean.true"), true));
		this.addElement(new Label<Boolean>(Language.getString("option.boolean.false"), false));
		if(randomEnabled)
			this.addElement(new Label<Boolean>(Language.getString("option.boolean.random"), null));
		
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
