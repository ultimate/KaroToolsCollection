package ultimate.karopapier.eval.model;

public class TableRecord extends Record<TableRecord, Integer>
{
	protected TableRecord()
	{
	}

	public TableRecord(String[] labels, String sortLabel, String player)
	{
		super(labels, sortLabel, player);
	}

	public TableRecord(String sortLabel, PlayerRecord record)
	{
		super(record.labels, sortLabel, record.player);
		for(String l : record.labels)
			this.setValue(l, record.getValue(l));
	}
}
