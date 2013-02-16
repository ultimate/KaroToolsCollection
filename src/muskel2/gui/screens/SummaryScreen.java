package muskel2.gui.screens;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import muskel2.core.karoaccess.GameCreator;
import muskel2.gui.Screen;
import muskel2.gui.help.PlayerCellEditor;
import muskel2.gui.help.SpinnerCellEditor;
import muskel2.model.Direction;
import muskel2.model.Game;
import muskel2.model.GameSeries;
import muskel2.model.Karopapier;
import muskel2.model.Map;
import muskel2.model.Player;
import muskel2.model.help.DirectionModel;
import muskel2.util.Language;

public class SummaryScreen extends Screen implements ActionListener
{
	private static final long	serialVersionUID	= 1L;

	private GameSeries			gameSeries;
	private Screen				startScreen;
	
	private boolean 			skipPlan;

	private List<Game>			gamesCreated;
	private List<Game>			gamesLeft;

	private List<Game>			gamesToCreate;
	private List<Game>			gamesToLeave;
	private List<Game>			gamesToLeaveTmp;

	private JTable				table;
	private JScrollPane			tableSP;

	private JButton				createButton;
	private JButton				leaveButton;
	private JButton				saveButton;

	private SummaryModel		model;

	private boolean				inProgress;

	private static final int	OPEN				= 0;
	private static final int	CREATING			= 1;
	private static final int	CREATED				= 2;
	private static final int	LEAVING				= 3;
	private static final int	LEFT				= 4;

	public SummaryScreen(Screen previous, Karopapier karopapier, JButton previousButton, JButton nextButton)
	{
		super(previous, karopapier, previousButton, nextButton, "screen.summary.header", "screen.summary.next");
		this.startScreen = this;
		while(startScreen.getPrevious() != null)
		{
			startScreen = startScreen.getPrevious();
		}

		this.gamesCreated = new LinkedList<Game>();
		this.gamesLeft = new LinkedList<Game>();

		this.gamesToCreate = new LinkedList<Game>();
		this.gamesToLeave = new LinkedList<Game>();
		
		this.skipPlan = false;
	}

	public boolean isSkipPlan()
	{
		return skipPlan;
	}

	public void setSkipPlan(boolean skipPlan)
	{
		this.skipPlan = skipPlan;
	}

	@Override
	public void updateBeforeShow(GameSeries gameSeries)
	{
		this.gameSeries = gameSeries;
		if(!this.skipPlan)
		{
			this.gameSeries.planGames();
			this.gamesCreated.clear();
			this.gamesLeft.clear();
			this.gamesToCreate.clear();
			this.gamesToLeave.clear();
		}
		else
		{
			for(Game game: gameSeries.getGames())
			{
				if(game.isCreated())
					this.gamesCreated.add(game);
				if(game.isLeft())
					this.gamesLeft.add(game);
			}
		}

		this.removeAll();
		this.setLayout(new BorderLayout());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 3, 5, 5));
		this.add(buttonPanel, BorderLayout.SOUTH);

		this.createButton = new JButton(Language.getString("screen.summary.create"));
		this.createButton.setActionCommand("create");
		this.createButton.addActionListener(this);
		buttonPanel.add(this.createButton);

		this.leaveButton = new JButton(Language.getString("screen.summary.leave"));
		this.leaveButton.setActionCommand("leave");
		this.leaveButton.addActionListener(this);
		buttonPanel.add(this.leaveButton);

		this.saveButton = new JButton(Language.getString("screen.summary.save"));
		this.saveButton.setActionCommand("save");
		this.saveButton.addActionListener(this);
		buttonPanel.add(this.saveButton);

		this.model = new SummaryModel();
		this.table = new JTable(this.model);
		this.table.setFillsViewportHeight(true);
		this.initTable(this.table);
		this.tableSP = new JScrollPane(this.table);
		this.add(this.tableSP, BorderLayout.CENTER);
		
		enableButtons();
	}

	@Override
	public GameSeries applySettings(GameSeries gameSeries)
	{
		return gameSeries;
	}

	public void createGames()
	{
		this.inProgress = true;
		GameCreator gc = new GameCreator(karopapier, this);
		for(Game game : this.gamesToCreate)
		{
			this.model.setStatus(game, CREATING);
		}
		System.out.println("Spiele zu erstellen: " + this.gamesToCreate.size());
		if(this.gamesToCreate.size() > 0)
		{
			gc.createGames(this.gamesToCreate);
		}
		else
		{
			enableButtons();
			inProgress = false;
		}
	}

	public void leaveGames()
	{
		this.inProgress = true;
		GameCreator gc = new GameCreator(karopapier, this);
		this.gamesToLeaveTmp = new LinkedList<Game>();
		for(Game game : this.gamesToLeave)
		{
			if(game.isCreated())
			{
				this.gamesToLeaveTmp.add(game);
				this.model.setStatus(game, LEAVING);
			}
		}
		System.out.println("Spiele zu verlassen: " + gamesToLeaveTmp.size());
		if(this.gamesToLeaveTmp.size() > 0)
		{
			gc.leaveGames(gamesToLeaveTmp, this.gameSeries.getCreator());
		}
		else
		{
			enableButtons();
			inProgress = false;
		}
	}

	public synchronized void notifyGameCreated(Game game)
	{
		if(game != null)
		{
			this.gamesToCreate.remove(game);
			this.gamesCreated.add(game);
			this.model.setStatus(game, CREATED);
			System.out.println("Spiele verbleibend: " + this.gamesToCreate.size());
		}
		if(this.gamesToCreate.size() == 0)
		{
			this.inProgress = false;
			enableButtons();
		}
	}

	public synchronized void notifyGameLeft(Game game)
	{
		if(game != null)
		{
			this.gamesToLeave.remove(game);
			this.gamesToLeaveTmp.remove(game);
			this.gamesLeft.add(game);
			this.model.setStatus(game, LEFT);
			System.out.println("Spiele verbleibend: " + this.gamesToLeaveTmp.size());
		}
		if(this.gamesToLeaveTmp.size() == 0)
		{
			this.inProgress = false;
			enableButtons();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		disableButtons();
		if(e.getActionCommand().equals("create"))
		{
			createGames();
		}
		else if(e.getActionCommand().equals("leave"))
		{
			leaveGames();
		}
		else if(e.getActionCommand().equals("save"))
		{
			JFileChooser fc = new JFileChooser();
			int action = fc.showSaveDialog(this);
			try
			{
				if(action == JFileChooser.APPROVE_OPTION)
				{
					File file = fc.getSelectedFile();

					FileOutputStream fos = new FileOutputStream(file);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					ObjectOutputStream oos = new ObjectOutputStream(bos);

					oos.writeObject(this.gameSeries);

					oos.flush();
					bos.flush();
					fos.flush();

					oos.close();
					bos.close();
					fos.close();
				}
				else if(action == JFileChooser.ERROR_OPTION)
				{
					throw new IOException("unknown");
				}
			}
			catch(IOException ex)
			{
				JOptionPane.showMessageDialog(this, Language.getString("error.save") + ex.getLocalizedMessage(), Language.getString("error.title"),
						JOptionPane.ERROR_MESSAGE);
			}
			enableButtons();
		}
	}

	private void disableButtons()
	{
		createButton.setEnabled(false);
		leaveButton.setEnabled(false);
		saveButton.setEnabled(false);
		if(previousButton != null)
			previousButton.setEnabled(false);
		if(nextButton != null)
			nextButton.setEnabled(false);
	}

	private boolean gamesSelectedToCreate()
	{
		return this.gamesToCreate.size() > 0;
	}

	private boolean gamesSelectedToLeave()
	{
		return this.gamesToLeave.size() > 0;
	}

	private boolean gamesCreated()
	{
		return (this.gamesCreated.size() > 0);
	}

	public boolean gamesToCreate()
	{
		return (this.gamesCreated.size() < this.gameSeries.getGames().size());
	}

	private void enableButtons()
	{
		createButton.setEnabled(gamesSelectedToCreate());
		leaveButton.setEnabled(gamesSelectedToLeave());
		saveButton.setEnabled(true);
		if(previousButton != null)
			previousButton.setEnabled(!gamesCreated());
		if(nextButton != null)
			nextButton.setEnabled(true);
	}

	private void initTable(JTable table)
	{
		table.setRowHeight(20);

		TableColumn col;
		for(int i = 0; i < table.getColumnCount(); i++)
		{
			col = table.getColumnModel().getColumn(i);
			col.setPreferredWidth(this.model.getColumnWidth(i));
			if(table.getColumnClass(i).equals(Integer.class))
			{
				col.setCellEditor(new SpinnerCellEditor(new SpinnerNumberModel(2, 0, Integer.MAX_VALUE, 1)));
			}
			else if(table.getColumnClass(i).equals(Direction.class))
			{
				col.setCellEditor(new DefaultCellEditor(new JComboBox(new DirectionModel(Direction.egal, false))));
			}
			else if(table.getColumnClass(i).equals(Map.class))
			{
				Map[] maps = karopapier.getMaps().values().toArray(new Map[0]);
				col.setCellEditor(new DefaultCellEditor(new JComboBox(new DefaultComboBoxModel(maps))));
			}
			else if(table.getColumnClass(i).equals(Player.class))
			{
				PlayerCellEditor editor = new PlayerCellEditor(this.model, karopapier);
				col.setCellEditor(editor);
				col.setCellRenderer(editor);
			}
		}

		for(Game game : this.gameSeries.getGames())
		{
			this.model.addRow(game);
		}
	}

	public class SummaryModel extends AbstractTableModel implements TableModel
	{
		private static final long	serialVersionUID	= 1L;

		private List<String>		columnNames;
		private List<Class<?>>		columnClasses;
		private List<Integer>		columnWidths;

		private List<Object[]>		rows;

		private List<Game>			games;

		public SummaryModel()
		{
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
			
			if(columnIndex == getColumnCount() -2)
				return karopapier.isUnlocked();

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
}
