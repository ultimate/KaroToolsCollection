package ultimate.karomuskel.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;

/**
 * Some UI helper methods
 * 
 * @author ultimate
 */
public abstract class UIUtil
{
	/**
	 * prevent instantiation
	 */
	private UIUtil()
	{

	}

	/**
	 * When removing items from a {@link JList} it can happen that indeces are still selected which are out of the new range of items.
	 * This method fixes the selection by moving the selected indeces back into the range.
	 * 
	 * @param list
	 */
	public static void fixSelection(JList<?> list)
	{
		List<Integer> newSelection = new ArrayList<>();
		int lastPossibleSelection = list.getModel().getSize() - 1;
		for(int i = list.getSelectedIndices().length - 1; i >= 0; i--)
		{
			if(list.getSelectedIndices()[i] > lastPossibleSelection)
			{
				newSelection.add(lastPossibleSelection);
				lastPossibleSelection--;
			}
			if(lastPossibleSelection < 0)
				break;
		}
		int[] newSelectionArray = new int[newSelection.size()];
		for(int i = 0; i < newSelectionArray.length; i++)
			newSelectionArray[i] = newSelection.get(i);
		list.setSelectedIndices(newSelectionArray);
	}
}
