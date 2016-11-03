package ultimate.karopapier.muskelx.gui.help;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import muskel2.gui.screens.SummaryScreen;
import muskel2.model.Direction;
import muskel2.model.Game;
import muskel2.model.GameSeries;
import muskel2.model.Map;
import muskel2.model.Player;
import muskel2.util.Language;
import ultimate.karopapier.muskelx.Launcher;

public class SummaryModel extends AbstractTableModel implements TableModel
{
	private static final long	serialVersionUID	= 1L;

	private GameSeries			gameSeries;

	private boolean				inProgress;

	private List<String>		columnNames;
	private List<Class<?>>		columnClasses;
	private List<Integer>		columnWidths;

	private List<Object[]>		rows;

	private List<Game>			games;

	public static final int		OPEN				= 0;
	public static final int		CREATING			= 1;
	public static final int		CREATED				= 2;
	public static final int		LEAVING				= 3;
	public static final int		LEFT				= 4;

	public SummaryModel(GameSeries gameSeries)
	{
		this.gameSeries = gameSeries;
		this.inProgress = false;

		this.columnNames = new ArrayList<String>();
		this.columnClasses = new ArrayList<Class<?>>();
		this.columnWidths = new ArrayList<Integer>();

		this.games = new ArrayList<Game>(gameSeries.getGames().size());
		this.rows = new ArrayList<Object[]>(gameSeries.getGames().size());

		this.addColumn(Language.getString("screen.summary.table.name"), String.class, 150);
		this.addColumn(Language.getString("screen.summary.table.map"), Map.class, 150);
		this.addColumn(Language.getString("screen.summary.table.players"), Player.class, 100);
		this.addColumn(Language.getString("screen.summary.table.zzz"), Integer.class, 30);
		this.addColumn(Language.getString("screen.summary.table.crashs"), Boolean.class, 30);
		this.addColumn(Language.getString("screen.summary.table.cps"), Boolean.class, 30);
		this.addColumn(Language.getString("screen.summary.table.direction"), Direction.class, 70);
		this.addColumn(Language.getString("screen.summary.table.createstatus"), Boolean.class, 40);
		this.addColumn(Language.getString("screen.summary.table.leavestatus"), Boolean.class, 40);
		this.addColumn(Language.getString("screen.summary.table.status"), String.class, 70);
	}

	public boolean isInProgress()
	{
		return inProgress;
	}

	public void setInProgress(boolean inProgress)
	{
		this.inProgress = inProgress;
	}

	public void addRow(Game game)
	{
		Object[] row = new Object[getColumnCount()];

		row[0] = game.getName();
		row[1] = game.getMap();
		row[2] = game.getPlayers();
		row[3] = game.getRules().getZzz();
		row[4] = game.getRules().getCrashingAllowed();
		row[5] = game.getRules().getCheckpointsActivated();
		row[6] = game.getRules().getDirection();
		row[7] = true;
		row[8] = game.getRules().isCreatorGiveUp() || game.isLeft();

		if(!game.isCreated())
			gamesToCreate.add(game);
		if(!game.isLeft() && game.getRules().isCreatorGiveUp())
			gamesToLeave.add(game);

		this.rows.add(row);
		this.games.add(game);

		int status = OPEN;
		if(game.isLeft())
			status = LEFT;
		else if(game.isCreated())
			status = CREATED;
		setStatus(game, status);
	}

	public Game getRow(int rowIndex)
	{
		return this.games.get(rowIndex);
	}

	public int getRowIndex(Game game)
	{
		return this.games.indexOf(game);
	}

	private void addColumn(String title, Class<?> cls, int width)
	{
		this.columnNames.add(title);
		this.columnClasses.add(cls);
		this.columnWidths.add(width);
	}

	@Override
	public int getColumnCount()
	{
		return this.columnNames.size();
	}

	@Override
	public int getRowCount()
	{
		return this.games.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		return this.rows.get(rowIndex)[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return this.columnClasses.get(columnIndex);
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return this.columnNames.get(columnIndex);
	}

	public int getColumnWidth(int columnIndex)
	{
		return columnWidths.get(columnIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		if(inProgress)
			return false;

		if(columnIndex == getColumnCount() - 1) // status
			return false;

		if(getRow(rowIndex).isLeft())
			return false;

		if(getRow(rowIndex).isCreated() && columnIndex < 8)
			return false;

		if(columnIndex == getColumnCount() - 2)
			return Launcher.getMuskel().getActiveProfile().isUnlocked();

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		if(inProgress && columnIndex != 9)
			return;

		Game game = getRow(rowIndex);		
		switch(columnIndex)
		{
			case 0:
				game.setName((String) aValue);
				break;
			case 1:
				if(((Map) aValue).getMaxPlayers() < game.getPlayers().size())
				{
					JOptionPane.showMessageDialog(SummaryScreen.this, Language.getString("screen.summary.maptosmall"));
					return;
				}
				game.setMap((Map) aValue);
				break;
			case 2:
				game.getPlayers().clear();
				game.getPlayers().addAll((List<Player>) aValue);
				break;
			case 3:
				game.getRules().setZzz((Integer) aValue);
				break;
			case 4:
				game.getRules().setCrashingAllowed((Boolean) aValue);
				break;
			case 5:
				game.getRules().setCheckpointsActivated((Boolean) aValue);
				break;
			case 6:
				game.getRules().setDirection((Direction) aValue);
				break;
			case 7:
				// TODO assure the value has changed, otherwise the update operation on lists may result in inconsitent lists :-(
				if((Boolean) aValue)
				{
					gamesToCreate.add(game);
				}
				else
				{
					gamesToCreate.remove(game);
				}
				break;
			case 8:							
				// TODO assure the value has changed, otherwise the update operation on lists may result in inconsitent lists :-(
				game.getRules().setCreatorGiveUp((Boolean) aValue);
				if((Boolean) aValue)
				{
					gamesToLeave.add(game);
				}
				else
				{
					gamesToLeave.remove(game);
				}
				break;
			default:
				break;
		}
		this.rows.get(rowIndex)[columnIndex] = aValue;

		fireTableCellUpdated(rowIndex, columnIndex);
		if(!inProgress)
		{
			enableButtons();
		}
	}

	public void setStatus(Game game, int status)
	{
		int rowIndex = this.getRowIndex(game);
		int columnIndex = 9;
		switch(status)
		{
			case OPEN:
				setValueAt(Language.getString("screen.summary.table.status.open"), rowIndex, columnIndex);
				break;
			case CREATING:
				setValueAt(Language.getString("screen.summary.table.status.creating"), rowIndex, columnIndex);
				break;
			case CREATED:
				game.setCreated(true);
				setValueAt(Language.getString("screen.summary.table.status.created"), rowIndex, columnIndex);
				break;
			case LEAVING:
				setValueAt(Language.getString("screen.summary.table.status.leaving"), rowIndex, columnIndex);
				break;
			case LEFT:
				game.setLeft(true);
				setValueAt(Language.getString("screen.summary.table.status.left"), rowIndex, columnIndex);
				break;
		}
	}
}