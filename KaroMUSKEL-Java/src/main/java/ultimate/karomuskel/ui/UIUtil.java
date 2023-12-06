package ultimate.karomuskel.ui;

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
		int[] indeces = list.getSelectedIndices();
		int lastPossibleSelection = list.getModel().getSize() - 1;
		for(int i = indeces.length - 1; i >= 0; i--)
		{
			if(indeces[i] > lastPossibleSelection)
			{
				indeces[i] = lastPossibleSelection;
				lastPossibleSelection--;
			}
		}
		list.setSelectedIndices(indeces);
	}
}
