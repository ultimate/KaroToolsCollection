package ultimate.karomuskel.ui.components;

import javax.swing.DefaultComboBoxModel;

import ultimate.karomuskel.utils.Language;
import ultimate.karomuskel.utils.lang.Label;

public class GenericEnumModel<E extends Enum<E>> extends DefaultComboBoxModel
{
	private static final long	serialVersionUID	= 1L;

	public GenericEnumModel(Class<E> enumType, E value, boolean randomEnabled)
	{
		super();
		
		for(E e: enumType.getEnumConstants())
		{
			if(!e.toString().equalsIgnoreCase("random") || randomEnabled)
				this.addElement(new Label<E>(Language.getString(enumType.getSimpleName() + "." + e), e));;
		}
		
		for(int i = 0; i < this.getSize(); i++)
		{
			if(this.getElementAt(i) == null && value == null)
			{
				this.setSelectedItem(this.getElementAt(i));
				break;				
			}
			if(this.getElementAt(i).equals(value))
			{
				this.setSelectedItem(this.getElementAt(i));
				break;
			}
		}
	}
}
