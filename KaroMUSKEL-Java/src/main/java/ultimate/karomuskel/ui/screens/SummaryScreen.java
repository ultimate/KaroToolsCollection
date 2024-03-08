package ultimate.karomuskel.ui.screens;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumCreatorParticipation;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameStatus;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.exceptions.GameSeriesException;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.PlaceToRace;
import ultimate.karoapi4j.model.official.Generator;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.Tag;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.Watchdog;
import ultimate.karomuskel.Creator;
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.Planner;
import ultimate.karomuskel.ui.EnumNavigation;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.Language.Label;
import ultimate.karomuskel.ui.MainFrame;
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.components.FilterModel;
import ultimate.karomuskel.ui.components.GenericEnumModel;
import ultimate.karomuskel.ui.components.PlaceToRaceRenderer;
import ultimate.karomuskel.ui.components.SpinnerCellEditor;
import ultimate.karomuskel.ui.components.TagCellEditor;
import ultimate.karomuskel.ui.components.TagEditor;
import ultimate.karomuskel.ui.components.UserCellEditor;
import ultimate.karomuskel.ui.dialog.FileDialog;
import ultimate.karomuskel.ui.dialog.GeneratorDialog;

public class SummaryScreen extends FilterScreen<PlannedGame> implements ActionListener
{
	private static final long			serialVersionUID		= 1L;

	private Creator						creator;

	private GameSeries					gameSeries;
	private Screen						startScreen;

	private boolean						skipPlan;
	private String						key;

	private List<PlannedGame>			gamesCreated;
	private List<PlannedGame>			gamesLeft;

	private List<PlannedGame>			gamesToCreate;
	private List<PlannedGame>			gamesToCreateTmp;
	private List<PlannedGame>			gamesToLeave;
	private List<PlannedGame>			gamesToLeaveTmp;
	private List<PlannedGame>			gamesWithExceptions;

	private JTable						table;
	private JScrollPane					tableSP;

	private JButton						createButton;
	private JButton						leaveButton;
	private JButton						saveButton;

	private SummaryModel				model;

	private boolean						inProgress;
	private CompletableFuture<Void>		creatorCF;
	private Watchdog					watchdog;
	private static final int			WATCHDOG_TIME_FACTOR	= 1000;

	private AtomicBoolean				batchUpdate				= new AtomicBoolean(false);
	private HashMap<String, Integer>	batchUpdateMessages		= new HashMap<>();

	public SummaryScreen(MainFrame gui, Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton, boolean skipPlan,
			String key)
	{
		super(gui, previous, karoAPICache, previousButton, nextButton, "screen.summary.header");
		this.startScreen = this;
		while(startScreen.getPrevious() != null)
		{
			startScreen = startScreen.getPrevious();
		}

		this.gamesCreated = new LinkedList<PlannedGame>();
		this.gamesLeft = new LinkedList<PlannedGame>();

		this.gamesToCreate = new LinkedList<PlannedGame>();
		this.gamesToLeave = new LinkedList<PlannedGame>();

		this.gamesWithExceptions = new LinkedList<PlannedGame>();

		this.skipPlan = skipPlan;
		this.key = key;

		this.creator = new Creator(karoAPICache);

		int interval = GameSeriesManager.getIntConfig(null, "watchdog.interval", 1);
		int timeout = GameSeriesManager.getIntConfig(null, "watchdog.timeout", 30);

		this.watchdog = new Watchdog(interval * WATCHDOG_TIME_FACTOR, timeout * WATCHDOG_TIME_FACTOR, (msg) -> {
			notifyTimeout(msg);
		});
	}

	@Override
	public String getNextKey()
	{
		if(this.next instanceof GroupWinnersScreen)
			return "screen.summary.nextround";
		else if(this.next instanceof KOWinnersScreen)
			return "screen.summary.nextround";
		else if(this.next instanceof SummaryScreen)
			return "screen.summary.nextrepeat";
		else // if(this.next instanceof StartScreen)
			return "screen.summary.next";
	}

	public void resetPlannedGames()
	{
		this.gameSeries.getGames().put(this.key, null);
		Planner.resetPlannedGames(this.gameSeries.getPlayers());
	}

	@Override
	public Message updateBeforeShow(GameSeries gameSeries, EnumNavigation direction)
	{
		this.gameSeries = gameSeries;

		this.gamesCreated.clear();
		this.gamesLeft.clear();
		this.gamesToCreate.clear();
		this.gamesToLeave.clear();
		
		if(this.firstShow)
		{
			// initialize search
			this.addTextFilter("screen.summary.filter.name", game -> game.getName(), false);
			this.addTextFilter("screen.summary.filter.tags", game -> game.getTags().toString(), true);
			this.addTextFilter("screen.summary.filter.map", game -> game.getMap().toString(), true);
			this.addTextFilter("screen.summary.filter.players", game -> game.getPlayers().toString(), true);
			this.nextFilterLine();
			this.addNumberFilter("screen.summary.filter.zzz.min", game -> game.getOptions().getZzz(), NumberFilterMode.gteq, 0, 0, 999);
			this.addNumberFilter("screen.summary.filter.zzz.max", game -> game.getOptions().getZzz(), NumberFilterMode.lteq, 999, 0, 999);
			this.addEnumFilter("screen.summary.filter.crashallowed", game -> game.getOptions().getCrashallowed(), EnumGameTC.class);
			this.addBooleanFilter("screen.summary.filter.checkpoints", game -> game.getOptions().isCps());
			this.addEnumFilter("screen.summary.filter.direction", game -> game.getOptions().getStartdirection(), EnumGameDirection.class);
			this.nextFilterLine();
			this.addBooleanFilter("screen.summary.filter.toBeCreated", game -> (gamesToCreate.contains(game) || game.isCreated()));
			this.addBooleanFilter("screen.summary.filter.toBeLeft", game -> (gamesToLeave.contains(game) || game.isLeft()));
			this.addEnumFilter("screen.summary.filter.status", game -> game.getStatus(), EnumGameStatus.class);
		}

		if(!this.skipPlan && direction == EnumNavigation.next)
		{
			if(this.key.contains(GameSeries.KEY_REPEAT))
				this.gameSeries.set(GameSeries.CURRENT_REPEAT,
						Integer.parseInt(this.key.substring(this.key.indexOf(GameSeries.KEY_REPEAT) + GameSeries.KEY_REPEAT.length())));

			if(!firstShow)
				resetPlannedGames();

			List<PlannedGame> games = Planner.planSeries(gameSeries);
			this.gameSeries.getGames().put(this.key, games);

			for(PlannedGame pg : games)
			{
				if(pg.hasException())
					this.gamesWithExceptions.add(pg);
			}
			if(this.gamesWithExceptions.size() > 0)
				notifyExceptions();
		}
		else
		{
			if(gameSeries.getGames().get(this.key) != null)
				for(PlannedGame game : gameSeries.getGames().get(this.key))
				{
					if(game.isCreated())
						this.gamesCreated.add(game);
					if(game.isLeft())
						this.gamesLeft.add(game);
				}
		}

		GameSeriesManager.autosave(this.gameSeries);

		this.getContentPanel().removeAll();
		this.getContentPanel().setLayout(new BorderLayout(5, 5));
		
		JLabel titleLabel = new JLabel(Language.getString("screen.summary.title"));
		this.getContentPanel().add(titleLabel, BorderLayout.NORTH);		

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 3, 5, 5));
		this.getContentPanel().add(buttonPanel, BorderLayout.SOUTH);

		this.createButton = new JButton(); // Text will be set in enable buttons
											// Language.getString("screen.summary.create"));
		this.createButton.setActionCommand("create");
		this.createButton.addActionListener(this);
		buttonPanel.add(this.createButton);

		this.leaveButton = new JButton(); // Text will be set in enable buttons
											// Language.getString("screen.summary.leave"));
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
		this.getContentPanel().add(this.tableSP, BorderLayout.CENTER);

		enableButtons();

		this.firstShow = false;

		return null;
	}

	@Override
	public String confirm(EnumNavigation direction)
	{
		if(direction == EnumNavigation.previous && !this.skipPlan)
			return "navigation.summary.previous";
		else if(gamesToCreate() && next instanceof StartScreen)
			return "navigation.summary.final";
		else if(gamesToCreate())
			return "navigation.summary.next";
		else
			return null;
	}

	@Override
	public GameSeries applySettings(GameSeries gameSeries, EnumNavigation direction)
	{
		if(direction == EnumNavigation.previous)
		{
			resetPlannedGames();
			this.skipPlan = false;
		}
		return gameSeries;
	}

	public void createGames()
	{
		GameSeriesManager.autosave(this.gameSeries);

		this.gamesToCreateTmp = new LinkedList<PlannedGame>(this.gamesToCreate);
		int amount = this.gamesToCreateTmp.size();
		logger.info("Spiele zu erstellen: " + amount);

		String message = Language.getString("screen.summary.create.confirm", amount);

		// show a message in case map generators are used (because processing might need more time)
		int mapGeneratorsUsed = 0;
		for(PlannedGame pg : this.gamesToCreateTmp)
		{
			if(pg.getMap() instanceof Generator)
				mapGeneratorsUsed++;
		}
		if(mapGeneratorsUsed > 0)
			message += "\n\n" + Language.getString("screen.summary.create.generators", mapGeneratorsUsed);

		int result = JOptionPane.showConfirmDialog(this, message);
		if(result != JOptionPane.OK_OPTION)
		{
			enableButtons();
			return;
		}

		this.inProgress = true;

		if(this.gamesToCreateTmp.size() > 0)
		{
			synchronized(this.gamesToCreateTmp)
			{
				this.gamesToCreateTmp.forEach(pg -> this.model.setStatus(pg, EnumGameStatus.creating));
				this.watchdog.cancel();
				CompletableFuture.runAsync(this.watchdog);
				this.creatorCF = this.creator.createGames(this.gamesToCreateTmp, pg -> this.notifyGameCreated(pg));
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
		GameSeriesManager.autosave(this.gameSeries);

		this.gamesToLeaveTmp = new LinkedList<PlannedGame>();
		for(PlannedGame game : this.gamesToLeave)
		{
			if(game.isCreated() && game.getGame() != null)
				this.gamesToLeaveTmp.add(game);
		}

		int amount = this.gamesToLeaveTmp.size();
		logger.info("Spiele zu verlassen: " + amount);

		String message = Language.getString("screen.summary.leave.confirm", amount);

		int result = JOptionPane.showConfirmDialog(this, message);
		if(result != JOptionPane.OK_OPTION)
		{
			enableButtons();
			return;
		}

		this.inProgress = true;

		if(this.gamesToLeaveTmp.size() > 0)
		{
			synchronized(this.gamesToLeaveTmp)
			{
				this.gamesToLeaveTmp.forEach(pg -> this.model.setStatus(pg, EnumGameStatus.leaving));
				this.watchdog.cancel();
				CompletableFuture.runAsync(this.watchdog);
				this.creatorCF = this.creator.leaveGames(this.gamesToLeaveTmp, pg -> this.notifyGameLeft(pg));
			}
		}
		else
		{
			enableButtons();
			inProgress = false;
		}
	}

	public void notifyGameCreated(PlannedGame game)
	{
		synchronized(this.gamesToCreateTmp)
		{
			if(game != null)
			{
				this.gamesCreated.add(game);
				this.gamesToCreateTmp.remove(game);
				if(!game.hasException())
				{
					this.gamesToCreate.remove(game);
					this.model.setStatus(game, EnumGameStatus.created);
					this.watchdog.notifyActive("game created GID=" + game.getGame().getId());
				}
				else
				{
					this.model.setStatus(game, EnumGameStatus.exception);
					this.watchdog.notifyActive("game creation failed " + game.getException());
					this.gamesWithExceptions.add(game);
				}
				logger.info("Spiele verbleibend: " + this.gamesToCreateTmp.size());
			}
			if(this.gamesToCreateTmp.size() == 0)
			{
				notifyFinished();
			}
		}
	}

	public void notifyGameLeft(PlannedGame game)
	{
		synchronized(this.gamesToLeaveTmp)
		{
			if(game != null)
			{
				this.gamesLeft.add(game);
				this.gamesToLeaveTmp.remove(game);
				if(!game.hasException())
				{
					this.gamesToLeave.remove(game);
					this.model.setStatus(game, EnumGameStatus.left);
					this.watchdog.notifyActive("game left GID=" + game.getGame().getId());
				}
				else
				{
					this.model.setStatus(game, EnumGameStatus.exception);
					this.watchdog.notifyActive("game creation failed " + game.getException());
					this.gamesWithExceptions.add(game);
				}
				logger.info("Spiele verbleibend: " + this.gamesToLeaveTmp.size());
			}
			if(this.gamesToLeaveTmp.size() == 0)
				notifyFinished();
		}
	}

	private void notifyFinished()
	{
		this.inProgress = false;
		this.watchdog.cancel();

		GameSeriesManager.autosave(this.gameSeries);

		enableButtons();

		if(this.gamesWithExceptions.size() > 0)
			notifyExceptions();
	}

	private void notifyExceptions()
	{
		if(this.gamesWithExceptions.size() == 0)
			return;

		StringBuilder msgSpec = new StringBuilder();
		msgSpec.append("\n");
		for(PlannedGame game : this.gamesWithExceptions)
		{
			msgSpec.append("\u2022 ");
			msgSpec.append(game.getName());
			msgSpec.append(" -> ");
			msgSpec.append(getExceptionMessage(game.getException()));
			msgSpec.append("\n");

			game.clearException();
		}

		GameSeriesException ex = new GameSeriesException("screen.summary.errors", msgSpec.toString(), "" + this.gamesWithExceptions.size());

		this.gui.notify(ex, JOptionPane.ERROR_MESSAGE);

		// clear the exception
		this.gamesWithExceptions.forEach(pg -> pg.clearException());
		this.gamesWithExceptions.clear();
	}

	public void notifyTimeout(String lastMessage)
	{
		logger.warn("timeout! last event = " + lastMessage);
		try
		{
			// cancel all CFs by applying a hard timeout
			creatorCF.get(1, TimeUnit.MILLISECONDS);
		}
		catch(InterruptedException | ExecutionException e)
		{
			logger.error(e);
		}
		catch(TimeoutException e)
		{
			// this is the expected
		}
		String message = Language.getString("watchdog.timeout").replace("%M", "" + lastMessage).replace("%T",
				"" + (this.watchdog.getTimeout() / 1000));
		String title = Language.getString("error.title");
		JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);

		GameSeriesManager.autosave(this.gameSeries);

		enableButtons();
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

	public boolean gamesToCreate()
	{
		return (this.gamesCreated.size() < this.gameSeries.getGames().size());
	}

	private void enableButtons()
	{
		createButton.setEnabled(!this.inProgress && gamesSelectedToCreate());
		createButton.setText(Language.getString("screen.summary.create", "" + this.gamesToCreate.size()));
		leaveButton.setEnabled(!this.inProgress && gamesSelectedToLeave());
		leaveButton.setText(Language.getString("screen.summary.leave", "" + this.gamesToLeave.size()));
		saveButton.setEnabled(!this.inProgress);
		if(previousButton != null)
			previousButton.setEnabled(!this.inProgress);
		if(nextButton != null)
			nextButton.setEnabled(!this.inProgress);
	}

	private void initTable(final JTable table)
	{
		table.setRowHeight(20);

		TableColumn col;
		for(int i = 0; i < table.getColumnCount(); i++)
		{
			col = table.getColumnModel().getColumn(i);
			if(this.model.getColumnWidth(i) > 0)
			{
				col.setMinWidth(this.model.getColumnWidth(i));
				col.setMaxWidth(this.model.getColumnWidth(i));
				col.setPreferredWidth(this.model.getColumnWidth(i));
			}
			else
			{
				col.setPreferredWidth(1000);
			}

			if(table.getColumnClass(i).equals(Integer.class))
			{
				col.setCellEditor(new SpinnerCellEditor(new SpinnerNumberModel(2, 0, Integer.MAX_VALUE, 1)));
			}
			else if(table.getColumnClass(i).equals(EnumGameTC.class))
			{
				// issue #138 don't set a selected value here or otherwise the combobox will always
				// start with that value no matter what is already selected
				col.setCellEditor(
						new DefaultCellEditor(new JComboBox<Label<EnumGameTC>>(new GenericEnumModel<EnumGameTC>(EnumGameTC.class, null, false))));
			}
			else if(table.getColumnClass(i).equals(EnumGameDirection.class))
			{
				// issue #138 don't set a selected value here or otherwise the combobox will always
				// start with that value no matter what is already selected
				col.setCellEditor(new DefaultCellEditor(
						new JComboBox<Label<EnumGameDirection>>(new GenericEnumModel<EnumGameDirection>(EnumGameDirection.class, null, false))));
			}
			else if(table.getColumnClass(i).equals(PlaceToRace.class))
			{
				List<PlaceToRace> maps = this.karoAPICache.getPlacesToRace();

				JComboBox<PlaceToRace> cb = new JComboBox<PlaceToRace>(new DefaultComboBoxModel<PlaceToRace>(maps.toArray(new PlaceToRace[0])));
				cb.setRenderer(new PlaceToRaceRenderer(null, true));
				col.setCellEditor(new DefaultCellEditor(cb));
			}
			else if(table.getColumnClass(i).equals(User.class))
			{
				UserCellEditor editor = new UserCellEditor(this.gui, this.model, karoAPICache);
				col.setCellEditor(editor);
				col.setCellRenderer(editor);
			}
			else if(table.getColumnClass(i).equals(Tag.class))
			{
				TagCellEditor editor = new TagCellEditor(this.gui, this.model, karoAPICache);
				col.setCellEditor(editor);
				col.setCellRenderer(editor);
			}
		}

		// Batch-Update-Support
		table.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				synchronized(batchUpdate)
				{
					batchUpdate.set(true);
					batchUpdateMessages.clear();

					int col = table.columnAtPoint(e.getPoint());
					if(col == 0) // Title
						batchUpdateString(col, Language.getString("screen.summary.table.name"),
								Language.getString("screen.summary.batchUpdate.note.name"));
					else if(col == 1) // Tags
						batchUpdateTags(col, Language.getString("screen.summary.table.tags"));
					else if(col == 2) // PlaceToRace
						batchUpdatePlaceToRace(col, Language.getString("screen.summary.table.map"));
					else if(col == 3) // Players
						batchUpdatePlayers(col, Language.getString("screen.summary.table.players"));
					else if(col == 4) // ZZZ
						batchUpdateInt(col, Language.getString("screen.summary.table.zzz"), new SpinnerNumberModel(2, 0, Integer.MAX_VALUE, 1));
					else if(col == 5) // TC
						batchUpdateSelection(col, Language.getString("screen.summary.table.crashs"),
								new GenericEnumModel<EnumGameTC>(EnumGameTC.class, EnumGameTC.free, false), null);
					else if(col == 6) // CPs
						batchUpdateBoolean(col, Language.getString("screen.summary.table.cps"));
					else if(col == 7) // Direction
						batchUpdateSelection(col, Language.getString("screen.summary.table.direction"),
								new GenericEnumModel<EnumGameDirection>(EnumGameDirection.class, EnumGameDirection.free, false), null);
					else if(col == 8) // Create
						batchUpdateBoolean(col, Language.getString("screen.summary.table.createstatus"));
					else if(col == 9) // Leave
						batchUpdateBoolean(col, Language.getString("screen.summary.table.leavestatus"));

					if(batchUpdateMessages.size() > 0)
					{
						StringBuilder message = new StringBuilder();
						for(Entry<String, Integer> entry : batchUpdateMessages.entrySet())
						{
							message.append(Language.getString(entry.getKey()));
							// if(entry.getValue() > 1)
							message.append(" (" + entry.getValue() + " mal)");
							message.append("\n");
						}
						JOptionPane.showMessageDialog(SummaryScreen.this, message);
					}
					batchUpdate.set(false);
				}
			}
		});

		if(this.gameSeries.getGames().get(this.key) != null)
		{
			for(PlannedGame game : this.gameSeries.getGames().get(this.key))
				this.model.addGame(game);
			this.model.refresh();
		}
		this.setModel(this.model);
	}

	private <V> void batchUpdateGeneric(int column, String label, Object message, Supplier<V> valueSupplier,
			BiFunction<PlannedGame, V, V> preprocessor)
	{
		int result = JOptionPane.showConfirmDialog(SummaryScreen.this, message, Language.getString("screen.summary.batchUpdate"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

		if(result == JOptionPane.OK_OPTION)
		{
			V value = valueSupplier.get();
			logger.info("Setze " + label + "=" + value);
			for(int row = 0; row < model.getRowCount(); row++)
			{
				if(model.isCellEditable(row, column))
				{
					if(preprocessor != null)
						value = preprocessor.apply(model.getGame(row), value);
					model.setValueAt(value, row, column);
				}
			}
		}
	}

	private void batchUpdateBoolean(int column, String label)
	{
		JCheckBox checkbox = new JCheckBox(label);
		Supplier<Boolean> valueSupplier = () -> {
			return checkbox.isSelected();
		};
		batchUpdateGeneric(column, label, checkbox, valueSupplier, null);
	}

	private void batchUpdateInt(int column, String label, SpinnerModel spinnerModel)
	{
		JPanel panel = new JPanel();
		JSpinner spinner = new JSpinner(spinnerModel);
		panel.add(new JLabel(label));
		panel.add(spinner);
		panel.setLayout(new FlowLayout());
		((FlowLayout) panel.getLayout()).setAlignment(FlowLayout.LEFT);

		Supplier<Integer> valueSupplier = () -> {
			return (Integer) spinner.getValue();
		};
		batchUpdateGeneric(column, label, panel, valueSupplier, null);
	}

	private void batchUpdateString(int column, String label, String note)
	{
		JPanel panel = new JPanel();
		JTextField textfield = new JTextField(50);
		panel.add(new JLabel(label));
		panel.add(textfield);

		Supplier<String> valueSupplier = () -> {
			return textfield.getText();
		};
		BiFunction<PlannedGame, String, String> preprocessor = (plannedGame, value) -> {
			return Planner.applyPlaceholders(value, plannedGame.getPlaceHolderValues());
		};
		batchUpdateGeneric(column, label, new Object[] { panel, note }, valueSupplier, preprocessor);
	}

	private <T> void batchUpdateSelection(int column, String label, ComboBoxModel<T> comboBoxModel, ListCellRenderer<T> aRenderer)
	{
		JComboBox<T> combobox = new JComboBox<>(comboBoxModel);
		if(aRenderer != null)
			combobox.setRenderer(aRenderer);

		Supplier<Object> valueSupplier = () -> {
			return combobox.getSelectedItem();
		};
		batchUpdateGeneric(column, label, combobox, valueSupplier, null);
	}

	private void batchUpdatePlaceToRace(int column, String label)
	{
		JComboBox<PlaceToRace> combobox = new JComboBox<>(
				new DefaultComboBoxModel<PlaceToRace>(karoAPICache.getPlacesToRace().toArray(new PlaceToRace[0])));
		combobox.setRenderer(new PlaceToRaceRenderer());

		JButton mapEditButton = new JButton(Language.getString("option.edit"));

		combobox.addActionListener(e -> {
			mapEditButton.setEnabled(combobox.getSelectedItem() instanceof Generator);
		});

		mapEditButton.addActionListener(e -> {
			PlaceToRace ptr = (PlaceToRace) combobox.getSelectedItem();
			if(ptr instanceof Generator)
			{
				Generator g = (Generator) ptr;
				int result = GeneratorDialog.getInstance().showEdit(this, g);
				if(result == JOptionPane.OK_OPTION)
				{
					if(g.getUniqueId() == 0)
					{
						Generator original = g;
						// create a copy first
						g = original.copy();
						this.karoAPICache.cache(g);
						// add the copy to the combobox
						int originalIndex = ((DefaultComboBoxModel<PlaceToRace>) combobox.getModel()).getIndexOf(original);
						((DefaultComboBoxModel<PlaceToRace>) combobox.getModel()).insertElementAt(g, originalIndex + 1);
						// now select
						combobox.setSelectedItem(g);
					}
					// apply settings
					logger.debug("updating settings for generator " + g.getUniqueKey());
					g.getSettings().putAll(GeneratorDialog.getInstance().getSettings());
					// update all combobox (to show the updated generator)
					combobox.repaint();
				}
			}
		});

		Supplier<Object> valueSupplier = () -> {
			return combobox.getSelectedItem();
		};
		batchUpdateGeneric(column, label, new Object[] { combobox, mapEditButton }, valueSupplier, null);
	}

	private void batchUpdateTags(int column, String label)
	{
		List<String> tagOptions = new ArrayList<>();
		this.karoAPICache.getSuggestedTags().forEach(tag -> {
			tagOptions.add(tag.getLabel());
		});
		String other = Language.getString("screen.summary.batchUpdate.tags.other");
		tagOptions.add(other);

		final JComboBox<String> combobox = new JComboBox<>(new DefaultComboBoxModel<String>(tagOptions.toArray(new String[tagOptions.size()])));
		final JTextField textField = new JTextField();
		textField.setEditable(false);

		combobox.addActionListener(e -> {
			textField.setEditable(other.equals(combobox.getSelectedItem()));
		});

		Object[] options = new Object[] { Language.getString("screen.summary.batchUpdate.tags.add"),
				Language.getString("screen.summary.batchUpdate.tags.remove"), Language.getString("option.cancel") };

		int result = JOptionPane.showOptionDialog(SummaryScreen.this, new Object[] { combobox, textField },
				Language.getString("screen.summary.batchUpdate.tags"), 0, JOptionPane.QUESTION_MESSAGE, null, options, null);

		Set<String> updatedTags;
		if(result == 0) // add
		{
			String value = (other.equals(combobox.getSelectedItem()) ? textField.getText() : (String) combobox.getSelectedItem());
			Set<String> tagsToChange = TagEditor.parseString(value);
			logger.info("adding tags: " + tagsToChange);
			for(int row = 0; row < model.getRowCount(); row++)
			{
				if(model.isCellEditable(row, column))
				{
					updatedTags = new LinkedHashSet<>(model.getGame(row).getTags());
					updatedTags.addAll(tagsToChange);
					model.setValueAt(updatedTags, row, column);
				}
			}
		}
		else if(result == 1) // remove
		{
			String value = (other.equals(combobox.getSelectedItem()) ? textField.getText() : (String) combobox.getSelectedItem());
			Set<String> tagsToChange = TagEditor.parseString(value);
			logger.info("removing tags: " + tagsToChange);
			for(int row = 0; row < model.getRowCount(); row++)
			{
				if(model.isCellEditable(row, column))
				{
					updatedTags = new LinkedHashSet<>(model.getGame(row).getTags());
					updatedTags.removeAll(tagsToChange);
					model.setValueAt(updatedTags, row, column);
				}
			}
		}
	}

	private void batchUpdatePlayers(int column, String label)
	{
		List<User> players = new ArrayList<>(karoAPICache.getUsersByLogin().values());
		// players.remove(gameSeries.getCreator()); // don't prohibit removing the creator, display
		// a warning instead
		JComboBox<User> combobox = new JComboBox<>(new DefaultComboBoxModel<User>(players.toArray(new User[0])));

		Object[] options = new Object[] { Language.getString("screen.summary.batchUpdate.players.add"),
				Language.getString("screen.summary.batchUpdate.players.remove"), Language.getString("option.cancel") };

		int result = JOptionPane.showOptionDialog(SummaryScreen.this, new Object[] { combobox },
				Language.getString("screen.summary.batchUpdate.players"), 0, JOptionPane.QUESTION_MESSAGE, null, options, null);

		Set<User> updatedPlayers;
		if(result == 0) // add
		{
			User value = (User) combobox.getSelectedItem();
			logger.info("adding player: " + value);
			for(int row = 0; row < model.getRowCount(); row++)
			{
				if(model.isCellEditable(row, column))
				{
					updatedPlayers = new LinkedHashSet<User>(model.getGame(row).getPlayers());
					if(updatedPlayers.contains(value))
					{
						addBatchUpdateMessage("screen.summary.batchUpdate.players.duplicate");
						continue;
					}
					updatedPlayers.add(value);
					model.setValueAt(updatedPlayers, row, column);
				}
			}
		}
		else if(result == 1) // remove
		{
			User value = (User) combobox.getSelectedItem();
			if(value == gameSeries.getCreator())
			{
				// show warning
				if(!this.gui.confirm("screen.summary.batchUpdate.players.warnCreator"))
					return;
			}
			logger.info("removing player: " + value);
			for(int row = 0; row < model.getRowCount(); row++)
			{
				if(model.isCellEditable(row, column))
				{
					updatedPlayers = new LinkedHashSet<User>(model.getGame(row).getPlayers());
					updatedPlayers.remove(value);
					model.setValueAt(updatedPlayers, row, column);
				}
			}
		}
	}

	private void addBatchUpdateMessage(String msgKey)
	{
		if(!batchUpdateMessages.containsKey(msgKey))
			batchUpdateMessages.put(msgKey, 1);
		else
			batchUpdateMessages.put(msgKey, batchUpdateMessages.get(msgKey) + 1);
	}

	public class SummaryModel extends AbstractTableModel implements FilterModel<PlannedGame>
	{
		private static final long			serialVersionUID	= 1L;

		private List<String>				columnNames;
		private List<Class<?>>				columnClasses;
		private List<Integer>				columnWidths;

		private List<PlannedGame>			allGames;
		private List<PlannedGame>			visibleGames;

		private Function<PlannedGame, Boolean>	filter;

		public SummaryModel()
		{
			this.columnNames = new ArrayList<String>();
			this.columnClasses = new ArrayList<Class<?>>();
			this.columnWidths = new ArrayList<Integer>();

			this.allGames = new ArrayList<PlannedGame>(gameSeries.getGames().size());
			this.visibleGames = new ArrayList<PlannedGame>(gameSeries.getGames().size());

			this.addColumn(Language.getString("screen.summary.table.name"), String.class, 0);
			this.addColumn(Language.getString("screen.summary.table.tags"), Tag.class, 0);
			this.addColumn(Language.getString("screen.summary.table.map"), PlaceToRace.class, 0);
			this.addColumn(Language.getString("screen.summary.table.players"), User.class, 0);
			this.addColumn(Language.getString("screen.summary.table.zzz"), Integer.class, 40);
			this.addColumn(Language.getString("screen.summary.table.crashs"), EnumGameTC.class, 90);
			this.addColumn(Language.getString("screen.summary.table.cps"), Boolean.class, 40);
			this.addColumn(Language.getString("screen.summary.table.direction"), EnumGameDirection.class, 90);
			this.addColumn(Language.getString("screen.summary.table.createstatus"), Boolean.class, 70);
			this.addColumn(Language.getString("screen.summary.table.leavestatus"), Boolean.class, 70);
			this.addColumn(Language.getString("screen.summary.table.status"), String.class, 200);
		}
		
		public void addGame(PlannedGame game)
		{
			this.allGames.add(game);
			
			if(!game.isCreated())
				gamesToCreate.add(game);
			if(!game.isLeft() && (gameSeries.getCreatorParticipation() == EnumCreatorParticipation.leave))
				gamesToLeave.add(game);

			EnumGameStatus status = EnumGameStatus.open;
			if(game.isLeft())
				status = EnumGameStatus.left;
			else if(game.isCreated())
				status = EnumGameStatus.created;
			setStatus(game, status);
		}
		
		public PlannedGame getGame(int rowIndex)
		{
			return this.visibleGames.get(rowIndex);
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
			return this.visibleGames.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			PlannedGame game = this.visibleGames.get(rowIndex);
			switch(columnIndex)
			{
				case 0:
					return game.getName();
				case 1:
					return game.getTags();
				case 2:
					return game.getMap();
				case 3:
					return game.getPlayers();
				case 4:
					return game.getOptions().getZzz();
				case 5:
					return new Label<>(Language.getString(EnumGameTC.class, game.getOptions().getCrashallowed()), game.getOptions().getCrashallowed());
				case 6:
					return game.getOptions().isCps();
				case 7:
					return new Label<>(Language.getString(EnumGameDirection.class, game.getOptions().getStartdirection()), game.getOptions().getStartdirection());
				case 8:
					return gamesToCreate.contains(game) || game.isCreated();
				case 9:
					return gamesToLeave.contains(game) || game.isLeft();
				case 10:
					String genKey = "" + (game.getMap() instanceof Generator ? ((Generator) game.getMap()).getKey() : "???");
					String mapId = "" + (game.getMap() instanceof Map ? ((Map) game.getMap()).getId() : "???");
					switch(game.getStatus())
					{
						case open: 			return Language.getString("EnumGameStatus.open");
						case generatingMap:	return Language.getString("EnumGameStatus.generatingMap") + " (GENERATOR=" + genKey + ")";
						case generatedMap:	return Language.getString("EnumGameStatus.generatedMap") + " (MID=" + mapId + ")";
						case creating:		return Language.getString("EnumGameStatus.creating") + " (MID=" + mapId + ")";
						case created:		return Language.getString("EnumGameStatus.created") + " (GID=" + game.getGame().getId() + ", MID=" + mapId + ")";
						case leaving:		return Language.getString("EnumGameStatus.leaving") + " (GID=" + game.getGame().getId() + ", MID=" + mapId + ")";
						case left:			return Language.getString("EnumGameStatus.left") + " (GID=" + game.getGame().getId() + ", MID=" + mapId + ")";
						case exception:		return Language.getString("EnumGameStatus.exception") + " (" + getExceptionMessage(game.getException()) + ")";
						default:			return "unknown";
					}
				default:
					break;
			}
			return "invalid column";
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

			if(this.visibleGames.get(rowIndex).isLeft())
				return false;

			if(this.visibleGames.get(rowIndex).isCreated() && columnIndex < getColumnCount() - 2)
				return false;

			if(columnIndex == getColumnCount() - 2) // leave
				return this.visibleGames.get(rowIndex).getPlayers().contains(karoAPICache.getCurrentUser());

			return true;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			if(inProgress && columnIndex != getColumnCount() - 1)
				return;

			PlannedGame game = this.visibleGames.get(rowIndex);
			switch(columnIndex)
			{
				case 0:
					game.setName((String) aValue);
					break;
				case 1:
					game.getTags().clear();
					game.getTags().addAll((Collection<String>) aValue);
					break;
				case 2:
					if(((PlaceToRace) aValue).getPlayers() < game.getPlayers().size())
					{
						if(batchUpdate.get())
							addBatchUpdateMessage("screen.summary.maptosmall");
						else
							JOptionPane.showMessageDialog(SummaryScreen.this, Language.getString("screen.summary.maptosmall"));
						return;
					}
					game.setMap((PlaceToRace) aValue);
					break;
				case 3:
					if(game.getMap().getPlayers() < ((Collection<User>) aValue).size())
					{
						if(batchUpdate.get())
							addBatchUpdateMessage("screen.summary.maptosmall");
						else
							JOptionPane.showMessageDialog(SummaryScreen.this, Language.getString("screen.summary.maptosmall"));
						return;
					}
					game.getPlayers().clear();
					game.getPlayers().addAll((Collection<User>) aValue);
					break;
				case 4:
					game.getOptions().setZzz((Integer) aValue);
					break;
				case 5:
					game.getOptions().setCrashallowed(((Label<EnumGameTC>) aValue).getValue());
					break;
				case 6:
					game.getOptions().setCps((Boolean) aValue);
					break;
				case 7:
					game.getOptions().setStartdirection(((Label<EnumGameDirection>) aValue).getValue());
					break;
				case 8:
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
				case 9:
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
				case 10:
					setStatus(game, (EnumGameStatus) aValue);
				default:
					break;
			}

			fireTableCellUpdated(rowIndex, columnIndex);
			if(!inProgress)
			{
				enableButtons();
			}
		}

		public void setStatus(PlannedGame game, EnumGameStatus status)
		{
			game.setStatus(status);
			if(status == EnumGameStatus.created)
				game.setCreated(true);
			if(status == EnumGameStatus.left)
				game.setLeft(true);

			if(visibleGames.contains(game))
				fireTableCellUpdated(visibleGames.indexOf(game), 10);
			if(!inProgress)
			{
				enableButtons();
			}
		}

		@Override
		public Function<PlannedGame, Boolean> getFilter()
		{
			return this.filter;
		}

		@Override
		public void setFilter(Function<PlannedGame, Boolean> filter)
		{
			logger.debug("set filter: " + filter);
			this.filter = filter;
		}

		@Override
		public void refresh()
		{
			this.visibleGames = new LinkedList<>(this.allGames);
			if(this.filter != null)
				this.visibleGames.removeIf(v -> !this.filter.apply(v));
			this.fireTableDataChanged();
		}
	}

	private static final String HTTP_ERROR_PREFIX = "Server returned HTTP response code: ";

	private String getExceptionMessage(Throwable t)
	{
		// unwrap exception
		while(t.getCause() != null)
			t = t.getCause();

		String msg = t.getMessage();

		if(msg.startsWith(HTTP_ERROR_PREFIX))
		{
			int status = Integer.parseInt(t.getMessage().substring(HTTP_ERROR_PREFIX.length(), HTTP_ERROR_PREFIX.length() + 3));
			msg = "HTTP " + status;
		}

		return msg;
	}
}
