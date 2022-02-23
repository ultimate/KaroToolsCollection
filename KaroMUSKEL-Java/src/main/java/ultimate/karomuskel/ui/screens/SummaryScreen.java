package ultimate.karomuskel.ui.screens;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.json.JSONObject;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.JSONUtil;
import ultimate.karomuskel.Planner;
import ultimate.karomuskel.ui.FileDialog;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.Language.Label;
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.components.GenericEnumModel;
import ultimate.karomuskel.ui.components.SpinnerCellEditor;
import ultimate.karomuskel.ui.components.UserCellEditor;

public class SummaryScreen extends Screen implements ActionListener
{
	private static final long	serialVersionUID	= 1L;

	private GameSeries			gameSeries;
	private Screen				startScreen;

	private Planner				planner;

	private boolean				firstCall			= true;
	private boolean				skipPlan;

	private List<PlannedGame>	gamesBackup;

	private List<PlannedGame>	gamesCreated;
	private List<PlannedGame>	gamesLeft;

	private List<PlannedGame>	gamesToCreate;
	private List<PlannedGame>	gamesToLeave;
	private List<PlannedGame>	gamesToLeaveTmp;

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

	public SummaryScreen(JFrame gui, Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(gui, previous, karoAPICache, previousButton, nextButton, "screen.summary.header", "screen.summary.next");
		this.startScreen = this;
		while(startScreen.getPrevious() != null)
		{
			startScreen = startScreen.getPrevious();
		}

		this.gamesCreated = new LinkedList<PlannedGame>();
		this.gamesLeft = new LinkedList<PlannedGame>();

		this.gamesToCreate = new LinkedList<PlannedGame>();
		this.gamesToLeave = new LinkedList<PlannedGame>();

		this.skipPlan = false;
		this.planner = new Planner(karoAPICache);
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

		if(this.firstCall)
		{
			this.firstCall = false;
			this.gamesBackup = new ArrayList<>(gameSeries.getGames());
		}

		if(!this.skipPlan)
		{
			if(!firstCall)
				gameSeries.getGames().removeIf(g -> { return !gamesBackup.contains(g); });
			gameSeries.getGames().addAll(planner.planSeries(gameSeries));

			Planner.resetPlannedGames(gameSeries.getPlayers());

			this.gamesCreated.clear();
			this.gamesLeft.clear();
			this.gamesToCreate.clear();
			this.gamesToLeave.clear();
		}
		else
		{
			for(PlannedGame game : gameSeries.getGames())
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
		int amount = this.gamesToCreate.size();
		System.out.println("Spiele zu erstellen: " + amount);

		int result = JOptionPane.showConfirmDialog(this, Language.getString("screen.summary.create.confirm").replace("%N", "" + amount));
		if(result != JOptionPane.OK_OPTION)
		{
			enableButtons();
			return;
		}

		this.inProgress = true;

		if(this.gamesToCreate.size() > 0)
		{
			for(PlannedGame plannedGame : this.gamesToCreate)
			{
				this.model.setStatus(plannedGame, CREATING);
				//@formatter:off
				this.karoAPICache.getKaroAPI()
					.createGame(plannedGame)
					.thenAcceptAsync(createdGame -> {
						plannedGame.setCreated(true);
						plannedGame.setGame(createdGame);
						notifyGameCreated(plannedGame);
				});
				//@formatter:on
			}
		}
		else
		{
			enableButtons();
			inProgress = false;
		}
	}

	public void leaveGames()
	{
		this.gamesToLeaveTmp = new LinkedList<PlannedGame>();
		for(PlannedGame game : this.gamesToLeave)
		{
			if(game.isCreated())
				this.gamesToLeaveTmp.add(game);
		}

		int amount = this.gamesToLeaveTmp.size();
		System.out.println("Spiele zu verlassen: " + amount);

		int result = JOptionPane.showConfirmDialog(this, Language.getString("screen.summary.leave.confirm").replace("%N", "" + amount));
		if(result != JOptionPane.OK_OPTION)
		{
			enableButtons();
			return;
		}

		this.inProgress = true;
		
		if(this.gamesToLeaveTmp.size() > 0)
		{
			for(PlannedGame plannedGame : this.gamesToLeaveTmp)
			{
				if(plannedGame.getGame() == null)
					continue;
				
				this.model.setStatus(plannedGame, LEAVING);
				//@formatter:off
				this.karoAPICache.getKaroAPI()
					.kick(plannedGame.getGame().getId(), karoAPICache.getCurrentUser().getId())
					.thenAcceptAsync(leftGame -> {
						plannedGame.setLeft(true);
						notifyGameLeft(plannedGame);
				});
				//@formatter:on
			}
		}
		else
		{
			enableButtons();
			inProgress = false;
		}
	}

	public synchronized void notifyGameCreated(PlannedGame game)
	{
		if(game != null)
		{
			this.gamesToCreate.remove(game);
			this.gamesCreated.add(game);
			this.model.setStatus(game, CREATED);
			logger.info("Spiele verbleibend: " + this.gamesToCreate.size());
		}
		if(this.gamesToCreate.size() == 0)
		{
			this.inProgress = false;
			enableButtons();
		}
	}

	public synchronized void notifyGameLeft(PlannedGame game)
	{
		if(game != null)
		{
			this.gamesToLeave.remove(game);
			this.gamesToLeaveTmp.remove(game);
			this.gamesLeft.add(game);
			this.model.setStatus(game, LEFT);
			logger.info("Spiele verbleibend: " + this.gamesToLeaveTmp.size());
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
			boolean saved = FileDialog.getInstance().showSave(this, gameSeries);
			logger.info("GameSeries saved? -> " + saved);
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

	private void initTable(final JTable table)
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
			else if(table.getColumnClass(i).equals(EnumGameDirection.class))
			{
				col.setCellEditor(new DefaultCellEditor(new JComboBox<Label<EnumGameDirection>>(new GenericEnumModel<EnumGameDirection>(EnumGameDirection.class, EnumGameDirection.free, false))));
			}
			else if(table.getColumnClass(i).equals(Map.class))
			{
				Map[] maps = karoAPICache.getMaps().toArray(new Map[0]);
				col.setCellEditor(new DefaultCellEditor(new JComboBox<Map>(new DefaultComboBoxModel<Map>(maps))));
			}
			else if(table.getColumnClass(i).equals(User.class))
			{
				UserCellEditor editor = new UserCellEditor(this.gui, this.model, karoAPICache);
				col.setCellEditor(editor);
				col.setCellRenderer(editor);
			}
		}

		// Batch-Update-Support
		table.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int col = table.columnAtPoint(e.getPoint());
				if(col == 0) // Title
					batchUpdateString(col, Language.getString("screen.summary.table.name"), Language.getString("screen.summary.batchUpdate.note.name"));
				else if(col == 1) // Map
					batchUpdateEnum(col, Language.getString("screen.summary.table.map"), new DefaultComboBoxModel(karoAPICache.getMaps().values().toArray(new Map[0])));
				else if(col == 2) // Players
					batchUpdatePlayers(col, Language.getString("screen.summary.table.players"));
				else if(col == 3) // ZZZ
					batchUpdateInt(col, Language.getString("screen.summary.table.zzz"), new SpinnerNumberModel(2, 0, Integer.MAX_VALUE, 1));
				else if(col == 4) // TC
					batchUpdateBoolean(col, Language.getString("screen.summary.table.crashs"));
				else if(col == 5) // CPs
					batchUpdateBoolean(col, Language.getString("screen.summary.table.cps"));
				else if(col == 6) // Direction
					batchUpdateEnum(col, Language.getString("screen.summary.table.direction"), new DirectionModel(Direction.egal, false));
				else if(col == 7) // Create
					batchUpdateBoolean(col, Language.getString("screen.summary.table.createstatus"));
				else if(col == 8) // Leave
					batchUpdateBoolean(col, Language.getString("screen.summary.table.leavestatus"));
			}
		});

		for(PlannedGame game : this.gameSeries.getGames())
		{
			this.model.addRow(game);
		}
	}

	private void batchUpdateBoolean(int column, String label)
	{
		JCheckBox checkbox = new JCheckBox(label);
		int result = JOptionPane.showConfirmDialog(SummaryScreen.this, new Object[] { checkbox }, Language.getString("screen.summary.batchUpdate"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		if(result == JOptionPane.OK_OPTION)
		{
			boolean value = checkbox.isSelected();
			System.out.println("Setze " + label + "=" + value);
			for(int row = 0; row < model.getRowCount(); row++)
			{
				if(model.isCellEditable(row, column))
					model.setValueAt(value, row, column);
			}
		}
	}

	private void batchUpdateInt(int column, String label, SpinnerModel spinnerModel)
	{
		JPanel panel = new JPanel();
		JSpinner spinner = new JSpinner(spinnerModel);
		panel.add(new JLabel(label));
		panel.add(spinner);
		panel.setLayout(new FlowLayout());
		((FlowLayout) panel.getLayout()).setAlignment(FlowLayout.LEFT);

		int result = JOptionPane.showConfirmDialog(SummaryScreen.this, new Object[] { panel }, Language.getString("screen.summary.batchUpdate"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		if(result == JOptionPane.OK_OPTION)
		{
			Integer value = (Integer) spinner.getValue();
			System.out.println("Setze " + label + "=" + value);
			for(int row = 0; row < model.getRowCount(); row++)
			{
				if(model.isCellEditable(row, column))
					model.setValueAt(value, row, column);
			}
		}
	}

	private void batchUpdateString(int column, String label, String note)
	{
		JPanel panel = new JPanel();
		JTextField textfield = new JTextField(50);
		panel.add(new JLabel(label));
		panel.add(textfield);
		int result = JOptionPane.showConfirmDialog(SummaryScreen.this, new Object[] { panel, note }, Language.getString("screen.summary.batchUpdate"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		if(result == JOptionPane.OK_OPTION)
		{
			String value = (String) textfield.getText();
			System.out.println("Setze " + label + "=" + value);
			for(int row = 0; row < model.getRowCount(); row++)
			{
				String updateValue = PlaceholderFactory.applyPlaceholders(this.karoAPICache, value, model.getRow(row), row);
				if(model.isCellEditable(row, column))
					model.setValueAt(updateValue, row, column);
			}
		}
	}

	private void batchUpdateEnum(int column, String label, ComboBoxModel comboBoxModel)
	{
		JComboBox combobox = new JComboBox(comboBoxModel);
		int result = JOptionPane.showConfirmDialog(SummaryScreen.this, new Object[] { combobox }, Language.getString("screen.summary.batchUpdate"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		if(result == JOptionPane.OK_OPTION)
		{
			Object value = combobox.getSelectedItem();
			System.out.println("Setze " + label + "=" + value);
			for(int row = 0; row < model.getRowCount(); row++)
			{
				if(model.isCellEditable(row, column))
					model.setValueAt(value, row, column);
			}
		}
	}

	private void batchUpdatePlayers(int column, String label)
	{
		Collection<User> players = karoAPICache.getUsers();
		players.remove(gameSeries.getCreator());
		JComboBox combobox = new JComboBox(new DefaultComboBoxModel(players.toArray(new User[0])));

		Object[] options = new Object[] { Language.getString("screen.summary.batchUpdate.players.add"), Language.getString("screen.summary.batchUpdate.players.remove"),
				Language.getString("option.cancel") };

		int result = JOptionPane.showOptionDialog(SummaryScreen.this, new Object[] { combobox }, Language.getString("screen.summary.batchUpdate.players"), 0, JOptionPane.QUESTION_MESSAGE, null,
				options, null);

		List<Player> updatedPlayers;
		if(result == 0) // add
		{
			Player value = (Player) combobox.getSelectedItem();
			System.out.println("Füge Spieler " + value + " hinzu");
			for(int row = 0; row < model.getRowCount(); row++)
			{
				updatedPlayers = new ArrayList<Player>(model.getRow(row).getPlayers());
				updatedPlayers.add(value);
				if(model.isCellEditable(row, column))
					model.setValueAt(updatedPlayers, row, column);
			}
		}
		else if(result == 1) // remove
		{
			Player value = (Player) combobox.getSelectedItem();
			System.out.println("Entferne Spieler " + value);
			for(int row = 0; row < model.getRowCount(); row++)
			{
				updatedPlayers = new ArrayList<Player>(model.getRow(row).getPlayers());
				updatedPlayers.remove(value);
				if(model.isCellEditable(row, column))
					model.setValueAt(updatedPlayers, row, column);
			}
		}
	}

	public class SummaryModel extends AbstractTableModel implements TableModel
	{
		private static final long	serialVersionUID	= 1L;

		private List<String>		columnNames;
		private List<Class<?>>		columnClasses;
		private List<Integer>		columnWidths;

		private List<Object[]>		rows;

		private List<PlannedGame>	games;

		public SummaryModel()
		{
			this.columnNames = new ArrayList<String>();
			this.columnClasses = new ArrayList<Class<?>>();
			this.columnWidths = new ArrayList<Integer>();

			this.games = new ArrayList<PlannedGame>(gameSeries.getGames().size());
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

		public void addRow(PlannedGame game)
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

		public PlannedGame getRow(int rowIndex)
		{
			return this.games.get(rowIndex);
		}

		public int getRowIndex(PlannedGame game)
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
				return karoAPICache.isUnlocked();

			return true;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			if(inProgress && columnIndex != 9)
				return;

			PlannedGame game = getRow(rowIndex);
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
						if(!gamesToCreate.contains(game))
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
						if(!gamesToLeave.contains(game))
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

		public void setStatus(PlannedGame game, int status)
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
