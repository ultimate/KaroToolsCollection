package ultimate.karopapier.utils;

import java.util.ArrayList;
import java.util.List;

public class Table
{
	protected String[]			header;
	final int					columns;
	protected List<Object[]>	rows;

	public Table(String[] header)
	{
		this.header = header;
		this.columns = header.length;
		this.rows = new ArrayList<>();
	}

	public Table(int columns)
	{
		this.header = null;
		this.columns = columns;
		this.rows = new ArrayList<>();
	}

	public void addRow(Object... row)
	{
		if(row.length != columns)
			throw new IllegalArgumentException("invalid number of columns: " + row.length + ", expected: " + columns);
		this.rows.add(row);
	}

	public String[] getHeader()
	{
		return header;
	}

	public int getColumns()
	{
		return columns;
	}

	public List<Object[]> getRows()
	{
		return rows;
	}

	public Object[] getRow(int row)
	{
		return rows.get(row);
	}

	public Object getValue(int row, int column)
	{
		return getRow(row)[column];
	}
}
