package ultimate.karopapier.utils;

import java.util.ArrayList;
import java.util.List;

public class Table
{
	protected Object[]			header;
	final int					columns;
	protected List<Object[]>	rows;
	protected List<boolean[]>	highlights;

	public Table(Object[] header)
	{
		this.header = header;
		this.columns = header.length;
		this.rows = new ArrayList<>();
		this.highlights = new ArrayList<>();
	}

	public Table(int columns)
	{
		this.header = null;
		this.columns = columns;
		this.rows = new ArrayList<>();
		this.highlights = new ArrayList<>();
	}

	public void addRow(Object... row)
	{
		if(row.length != columns)
			throw new IllegalArgumentException("invalid number of columns: " + row.length + ", expected: " + columns);
		this.rows.add(row);
		this.highlights.add(new boolean[row.length]);
	}

	public Object[] getHeader()
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

	public void setHighlight(int row, int column, boolean highlight)
	{
		highlights.get(row)[column] = highlight;
	}

	public boolean isHighlight(int row, int column)
	{
		return highlights.get(row)[column];
	}

	public Object getValue(int row, int column)
	{
		return getRow(row)[column];
	}

	public void setValue(int row, int column, Object value)
	{
		getRow(row)[column] = value;
	}
}
