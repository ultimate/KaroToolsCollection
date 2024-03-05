package ultimate.karomuskel.ui.components;

import java.util.function.Predicate;

import javax.swing.DefaultComboBoxModel;

import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.Language.Label;

public class GenericEnumModel<E extends Enum<E>> extends DefaultComboBoxModel<Label<E>>
{
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param enumType
	 * @param selectedValue
	 * @param randomEnabled
	 */
	public GenericEnumModel(Class<E> enumType, E selectedValue, boolean randomEnabled)
	{
		this(enumType, selectedValue, randomEnabled, false);
	}
	
	/**
	 * 
	 * @param enumType
	 * @param selectedValue
	 * @param randomEnabled
	 * @param emptyEnabled - add empty option?
	 */
	public GenericEnumModel(Class<E> enumType, E selectedValue, boolean randomEnabled, boolean emptyEnabled)
	{
		this(enumType, selectedValue, e -> {
			return !e.toString().equalsIgnoreCase("random") || randomEnabled;
		}, emptyEnabled);
	}

	/** 
	 * @param enumType - the Enum type
	 * @param selectedValue - the initial value selected
	 * @param predicate - an optional filter used to control adding the enum values. Only values for which the predicate returns true are added to the model
	 */
	public GenericEnumModel(Class<E> enumType, E selectedValue, Predicate<E> predicate)
	{
		this(enumType, selectedValue, predicate, false);
	}

	/**
	 * Create a new GenericEnumModel
	 * 
	 * @param enumType - the Enum type
	 * @param selectedValue - the initial value selected
	 * @param predicate - an optional filter used to control adding the enum values. Only values for which the predicate returns true are added to the model
	 * @param emptyEnabled - add empty option?
	 */
	public GenericEnumModel(Class<E> enumType, E selectedValue, Predicate<E> predicate, boolean emptyEnabled)
	{
		super();
		
		if(emptyEnabled)
			this.addElement(null);

		for(E e : enumType.getEnumConstants())
		{
			if(predicate == null || predicate.test(e))
				this.addElement(new Label<E>(Language.getString(enumType, e), e));
		}

		for(int i = 0; i < this.getSize(); i++)
		{
			if(this.getElementAt(i) == null && selectedValue == null)
			{
				this.setSelectedItem(this.getElementAt(i));
				break;
			}
			if(this.getElementAt(i).getValue().equals(selectedValue))
			{
				this.setSelectedItem(this.getElementAt(i));
				break;
			}
		}
	}
}
