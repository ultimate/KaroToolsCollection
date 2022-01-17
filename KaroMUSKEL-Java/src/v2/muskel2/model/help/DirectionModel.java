package muskel2.model.help;

import javax.swing.DefaultComboBoxModel;

import muskel2.model.Direction;
import muskel2.util.Language;

public class DirectionModel extends DefaultComboBoxModel
{
	private static final long	serialVersionUID	= 1L;

	public DirectionModel(Direction value, boolean randomEnabled)
	{
		super();
		this.addElement(Direction.klassisch);
		this.addElement(Direction.Formula_1);
		this.addElement(Direction.egal);
		if(randomEnabled)
			this.addElement(new Label<Direction>(Language.getString("option.direction.random"), null));
		
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
