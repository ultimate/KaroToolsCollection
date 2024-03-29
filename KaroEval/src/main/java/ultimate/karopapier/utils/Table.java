package ultimate.karopapier.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Table
{
	protected List<Cell[]>	headers;
	final int				columns;
	protected List<Cell[]>	rows;

	public Table(Object[]... headers)
	{
		this.headers = new ArrayList<>();
		for(Object[] h : headers)
			this.headers.add(toCells(h));
		this.columns = headers[0].length;
		this.rows = new ArrayList<>();
	}

	public Table(int columns)
	{
		this.headers = new ArrayList<>();
		this.columns = columns;
		this.rows = new ArrayList<>();
	}
	
	private Cell[] toCells(Object... values)
	{
		Cell[] cells = new Cell[values.length];
		for(int c = 0; c < values.length; c++)
			cells[c] = new Cell(values[c]);
		return cells;
	}

	public boolean addRow(Object... row)
	{
		if(row.length != columns)
			throw new IllegalArgumentException("invalid number of columns: " + row.length + ", expected: " + columns);
		return this.rows.add(toCells(row));
	}

	public List<Cell[]> getHeaders()
	{
		return headers;
	}

	public Cell[] getHeader(int header)
	{
		return headers.get(header);
	}

	public int getColumns()
	{
		return columns;
	}

	public List<Cell[]> getRows()
	{
		return rows;
	}

	public Cell[] getRow(int row)
	{
		return rows.get(row);
	}

	public void setBold(int row, int column, boolean bold)
	{
		rows.get(row)[column].bold = bold;
	}

	public boolean isBold(int row, int column)
	{
		return rows.get(row)[column].bold;
	}

	public void setStyle(int row, int column, String style)
	{
		rows.get(row)[column].style = style;
	}

	public String getStyle(int row, int column)
	{
		return rows.get(row)[column].style;
	}

	public Object getValue(int row, int column)
	{
		return getRow(row)[column].value;
	}

	public void setValue(int row, int column, Object value)
	{
		getRow(row)[column].value = value;
	}

	@SuppressWarnings("unchecked")
	public <T> void sort(int column, Comparator<T> comparator)
	{
		Collections.sort(this.rows, (row1, row2) -> {
			return comparator.compare((T) row1[column].value, (T) row2[column].value);
		});
	}

	public static class Cell
	{
		public Object	value;
		public boolean	bold;
		public int		colspan	= 1;
		public String 	style;

		public Cell()
		{
			this(null);
		}

		public Cell(Object value)
		{
			this.value = value;
			this.bold = false;
			this.style = null;
		}
	}
}
