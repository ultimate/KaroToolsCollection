package ultimate.karomuskel.ui.components;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class GroupWinnersRenderer extends DefaultListCellRenderer
{
	private static final long serialVersionUID = 1L;
	private int numberOfWinnersPerGroup;
	public static final int ALPHA = 40;

	public GroupWinnersRenderer(int numberOfWinnersPerGroup)
	{
		this.numberOfWinnersPerGroup = numberOfWinnersPerGroup;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if(index < numberOfWinnersPerGroup)
			c.setBackground(new Color(0, 255, 0, ALPHA));
		else
			c.setBackground(new Color(255, 0, 0, ALPHA));
		return c;
	}
}
