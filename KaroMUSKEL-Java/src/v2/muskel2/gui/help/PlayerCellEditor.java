package muskel2.gui.help;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import muskel2.Main;
import muskel2.gui.screens.SummaryScreen.SummaryModel;
import muskel2.model.Game;
import muskel2.model.Karopapier;
import muskel2.model.Map;
import muskel2.model.Player;
import muskel2.util.Language;
import muskel2.util.SortUtil;

// TODO Filterung ermöglichen, nach der in der linken Liste nur die Spieler angezeigt werden, die
// auch für die Serie ausgewählt wurden
public class PlayerCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener, TableCellRenderer
{
	private static final long	serialVersionUID	= 1L;

	private JButton				button;
	private JLabel				label;

	private PlayerChooser		chooser;

	private List<Player>		players;
	private List<Player>		allPlayers;

	private Karopapier			karopapier;
	private SummaryModel		model;
	private Game				game;

	public PlayerCellEditor(SummaryModel model, Karopapier karopapier)
	{
		this.karopapier = karopapier;
		this.model = model;

		this.button = new JButton();
		this.button.setHorizontalAlignment(JButton.LEFT);
		this.button.setVerticalAlignment(JButton.CENTER);
		this.button.setActionCommand("edit");
		this.button.addActionListener(this);

		this.label = new JLabel();
		this.label.setOpaque(true);
		this.label.setHorizontalAlignment(JLabel.LEFT);
		this.label.setVerticalAlignment(JLabel.CENTER);

		this.allPlayers = new LinkedList<Player>(karopapier.getPlayers().values());

		this.chooser = new PlayerChooser(Main.getGui());
	}

	@Override
	public List<Player> getCellEditorValue()
	{
		return this.players;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		this.players = (List<Player>) value;
		this.game = model.getRow(row);
		this.button.setText(playerListToString((List<Player>) value));
		return this.button;
	}

	private String playerListToString(List<Player> players)
	{
		StringBuilder sb = new StringBuilder();
		for(Player player : players)
		{
			if(!sb.toString().isEmpty())
				sb.append(", ");
			sb.append(player.getName());
		}
		return sb.toString();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equalsIgnoreCase("edit"))
		{
			this.chooser.setPlayers(this.game, this.players);
			this.chooser.setVisible(true);
			fireEditingStopped();
		}
		else if(e.getActionCommand().equalsIgnoreCase("add"))
		{
			Object[] tmpPlayers = this.chooser.notSelectedPlayersLI.getSelectedValues();
			this.chooser.notSelectedPlayersLI.clearSelection();
			Map map = this.chooser.game.getMap();
			for(Object o : tmpPlayers)
			{
				if(this.chooser.players.size() >= map.getMaxPlayers())
				{
					JOptionPane.showMessageDialog(Main.getGui(), Language.getString("screen.summary.playeredit.maplimit").replace("%N",
							"" + map.getMaxPlayers()), Language.getString("screen.summary.playeredit.errortitle"), JOptionPane.ERROR_MESSAGE);
					break;
				}
				this.chooser.notSelectedPlayers.remove((Player) o);
				this.chooser.players.add((Player) o);
				SortUtil.sortListAscending(this.chooser.players, "getNameLowerCase");
			}
			this.chooser.fireContentChanged();
		}
		else if(e.getActionCommand().equalsIgnoreCase("remove"))
		{
			Object[] tmpPlayers = this.chooser.playersLI.getSelectedValues();
			this.chooser.playersLI.clearSelection();
			for(Object o : tmpPlayers)
			{
				if(o.equals(karopapier.getCurrentPlayer()))
				{
					JOptionPane.showMessageDialog(Main.getGui(), Language.getString("screen.summary.playeredit.creatorremove"), Language
							.getString("screen.summary.playeredit.errortitle"), JOptionPane.ERROR_MESSAGE);
					continue;
				}
				this.chooser.players.remove((Player) o);
				this.chooser.notSelectedPlayers.add((Player) o);
				SortUtil.sortListAscending(this.chooser.notSelectedPlayers, "getNameLowerCase");
			}
			this.chooser.fireContentChanged();
		}
		else if(e.getActionCommand().equalsIgnoreCase("ok"))
		{
			this.players = new LinkedList<Player>(this.chooser.getPlayers());
			this.chooser.setVisible(false);
		}
		else if(e.getActionCommand().equalsIgnoreCase("cancel"))
		{
			this.players = new LinkedList<Player>(this.chooser.getPlayersBackup());
			this.chooser.setVisible(false);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		if(isSelected)
		{
			this.label.setBackground(table.getSelectionBackground());
			this.label.setForeground(table.getSelectionForeground());
		}
		else
		{
			this.label.setBackground(table.getBackground());
			this.label.setForeground(table.getForeground());
		}
		this.label.setText(playerListToString((List<Player>) value));
		return this.label;
	}

	private class PlayerChooser extends JDialog implements WindowListener
	{
		private static final long				serialVersionUID		= 1L;

		private static final int				listFixedCellWidth		= 250;

		private static final int				width					= 700;
		private static final int				height					= 400;

		private JList							notSelectedPlayersLI;
		private JList							playersLI;

		private JButton							okButton;
		private JButton							cancelButton;
		private JButton							addButton;
		private JButton							removeButton;

		private Game							game;

		private final List<Player>				players					= new LinkedList<Player>();
		private final List<Player>				notSelectedPlayers		= new LinkedList<Player>();
		private final List<Player>				playersBackup			= new LinkedList<Player>();

		private final PlayersModel				playersModel			= new PlayersModel();
		private final NotSelectedPlayersModel	notSelectedPlayersModel	= new NotSelectedPlayersModel();

		public PlayerChooser(JFrame frame)
		{
			super(frame);

			this.setModal(true);
			this.addWindowListener(this);

			this.setSize(new Dimension(width, height));
			this.setMinimumSize(new Dimension(width, height));
			this.setMaximumSize(new Dimension(width, height));
			this.setPreferredSize(new Dimension(width, height));

			this.setLayout(new BorderLayout());

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(5, 5, 5, 5);

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridBagLayout());

			okButton = new JButton(Language.getString("option.ok"));
			okButton.setActionCommand("ok");
			okButton.addActionListener(PlayerCellEditor.this);
			gbc.gridx = 0;
			gbc.gridy = 0;
			buttonPanel.add(okButton, gbc);

			cancelButton = new JButton(Language.getString("option.cancel"));
			cancelButton.setActionCommand("cancel");
			cancelButton.addActionListener(PlayerCellEditor.this);
			gbc.gridx = 1;
			gbc.gridy = 0;
			buttonPanel.add(cancelButton, gbc);

			this.add(buttonPanel, BorderLayout.SOUTH);

			JPanel contentPanel = new JPanel();
			this.add(contentPanel, BorderLayout.CENTER);
			contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));

			JPanel allPlayersPanel = new JPanel();
			allPlayersPanel.setLayout(new BorderLayout(5, 5));
			contentPanel.add(allPlayersPanel);

			this.notSelectedPlayersLI = new JList(this.notSelectedPlayersModel);
			this.notSelectedPlayersLI.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			this.notSelectedPlayersLI.setFixedCellWidth(listFixedCellWidth);
			JScrollPane notSelectedPlayersSP = new JScrollPane(this.notSelectedPlayersLI, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			allPlayersPanel.add(new JLabel(Language.getString("screen.players.allplayers")), BorderLayout.NORTH);
			allPlayersPanel.add(notSelectedPlayersSP, BorderLayout.CENTER);

			buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridBagLayout());
			contentPanel.add(buttonPanel);

			gbc.fill = GridBagConstraints.HORIZONTAL;

			addButton = new JButton(Language.getString("option.add"));
			addButton.setActionCommand("add");
			addButton.addActionListener(PlayerCellEditor.this);
			gbc.gridx = 0;
			gbc.gridy = 0;
			buttonPanel.add(addButton, gbc);

			removeButton = new JButton(Language.getString("option.remove"));
			removeButton.setActionCommand("remove");
			removeButton.addActionListener(PlayerCellEditor.this);
			gbc.gridx = 0;
			gbc.gridy = 1;
			buttonPanel.add(removeButton, gbc);

			JPanel gamePlayersPanel = new JPanel();
			gamePlayersPanel.setLayout(new BorderLayout(5, 5));
			contentPanel.add(gamePlayersPanel);

			this.playersLI = new JList(this.playersModel);
			this.playersLI.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			this.playersLI.setFixedCellWidth(listFixedCellWidth);
			JScrollPane playersSP = new JScrollPane(this.playersLI, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			gamePlayersPanel.add(new JLabel(Language.getString("screen.players.selectedplayers")), BorderLayout.NORTH);
			gamePlayersPanel.add(playersSP, BorderLayout.CENTER);
		}

		public void fireContentChanged()
		{
			this.playersModel.fireContentChanged();
			this.notSelectedPlayersModel.fireContentChanged();
		}

		public void setPlayers(Game game, List<Player> players)
		{
			this.game = game;

			this.players.clear();
			this.players.addAll(players);

			this.playersBackup.clear();
			this.playersBackup.addAll(players);

			this.notSelectedPlayers.clear();
			this.notSelectedPlayers.addAll(allPlayers);
			this.notSelectedPlayers.removeAll(this.players);

			this.fireContentChanged();
		}

		public List<Player> getPlayers()
		{
			return players;
		}

		public List<Player> getPlayersBackup()
		{
			return playersBackup;
		}

		private class PlayersModel extends AbstractListModel
		{
			private static final long	serialVersionUID	= 1L;

			@Override
			public int getSize()
			{
				return players.size();
			}

			@Override
			public Object getElementAt(int index)
			{
				return players.get(index);
			}

			public void fireContentChanged()
			{
				fireContentsChanged(this, 0, players.size() - 1);
			}
		}

		private class NotSelectedPlayersModel extends AbstractListModel
		{
			private static final long	serialVersionUID	= 1L;

			@Override
			public int getSize()
			{
				return notSelectedPlayers.size();
			}

			@Override
			public Object getElementAt(int index)
			{
				return notSelectedPlayers.get(index);
			}

			public void fireContentChanged()
			{
				fireContentsChanged(this, 0, notSelectedPlayers.size() - 1);
			}
		}

		@Override
		public void windowActivated(WindowEvent e)
		{
		}

		@Override
		public void windowClosed(WindowEvent e)
		{
		}

		@Override
		public void windowClosing(WindowEvent e)
		{
			actionPerformed(new ActionEvent(this, 0, "cancel"));
		}

		@Override
		public void windowDeactivated(WindowEvent e)
		{
		}

		@Override
		public void windowDeiconified(WindowEvent e)
		{
		}

		@Override
		public void windowIconified(WindowEvent e)
		{
		}

		@Override
		public void windowOpened(WindowEvent e)
		{
		}
	}
}
