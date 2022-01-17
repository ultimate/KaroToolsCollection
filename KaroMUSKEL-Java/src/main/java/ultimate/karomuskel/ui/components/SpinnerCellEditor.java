package ultimate.karomuskel.ui.components;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerModel;
import javax.swing.table.TableCellEditor;

public class SpinnerCellEditor extends AbstractCellEditor implements TableCellEditor
{
	private static final long	serialVersionUID	= 1L;
	
	private JSpinner	spinner;

	public SpinnerCellEditor(SpinnerModel model)
	{
		this.spinner = new JSpinner(model);
	}

	@Override
	public Object getCellEditorValue()
	{
		return spinner.getValue();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		spinner.setValue(value);
		return spinner;
	}
}
