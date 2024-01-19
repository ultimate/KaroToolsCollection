package ultimate.karomuskel.ui.components;

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
import java.util.Collection;
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

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.model.extended.PlaceToRace;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karoapi4j.model.official.User;
import ultimate.karoapi4j.utils.CollectionsUtil;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.screens.SummaryScreen.SummaryModel;

// TODO IDEA Filterung ermöglichen, nach der in der linken Liste nur die Spieler angezeigt werden, die auch für die Serie ausgewählt wurden
public class UserCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener, TableCellRenderer
{
	private static final long	serialVersionUID	= 1L;

	private JButton				button;
	private JLabel				label;

	private UserChooser			chooser;

	private Collection<User>	users;
	private List<User>			allUsers;

	private JFrame				gui;
	private SummaryModel		model;
	private KaroAPICache		karoAPICache;

	private PlannedGame			game;

	public UserCellEditor(JFrame gui, SummaryModel model, KaroAPICache karoAPICache)
	{
		this.gui = gui;
		this.model = model;
		this.karoAPICache = karoAPICache;

		this.button = new JButton();
		this.button.setHorizontalAlignment(JButton.LEFT);
		this.button.setVerticalAlignment(JButton.CENTER);
		this.button.setActionCommand("edit");
		this.button.addActionListener(this);

		this.label = new JLabel();
		this.label.setOpaque(true);
		this.label.setHorizontalAlignment(JLabel.LEFT);
		this.label.setVerticalAlignment(JLabel.CENTER);

		this.allUsers = new LinkedList<User>(karoAPICache.getUsers());

		this.chooser = new UserChooser(this.gui);
	}

	@Override
	public Collection<User> getCellEditorValue()
	{
		return this.users;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		this.users = (Collection<User>) value;
		this.game = model.getRow(row);
		this.button.setText(userCollectionToString((Collection<User>) value));
		return this.button;
	}

	private static String userCollectionToString(Collection<User> users)
	{
		StringBuilder sb = new StringBuilder();
		for(User user : users)
		{
			if(!sb.toString().isEmpty())
				sb.append(", ");
			sb.append(user.getLogin());
		}
		return sb.toString();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equalsIgnoreCase("edit"))
		{
			this.chooser.setUsers(this.game, this.users);
			this.chooser.setVisible(true);
			fireEditingStopped();
		}
		else if(e.getActionCommand().equalsIgnoreCase("add"))
		{
			List<User> tmpUsers = this.chooser.notSelectedUsersLI.getSelectedValuesList();
			this.chooser.notSelectedUsersLI.clearSelection();
			PlaceToRace map = this.chooser.game.getMap();
			for(User u : tmpUsers)
			{
				if(this.chooser.users.size() >= map.getPlayers())
				{
					JOptionPane.showMessageDialog(this.gui, Language.getString("screen.summary.useredit.maplimit", map.getPlayers()), Language.getString("screen.summary.useredit.errortitle"), JOptionPane.ERROR_MESSAGE);
					break;
				}
				this.chooser.notSelectedUsers.remove(u);
				this.chooser.users.add(u);
//				CollectionsUtil.sortAscending(this.chooser.users, "getLoginLowerCase"); // keep the original order
			}
			this.chooser.fireContentChanged();
		}
		else if(e.getActionCommand().equalsIgnoreCase("remove"))
		{
			List<User> tmpUsers = this.chooser.usersLI.getSelectedValuesList();
			this.chooser.usersLI.clearSelection();
			for(User u : tmpUsers)
			{
				if(u.equals(karoAPICache.getCurrentUser()))
				{
					JOptionPane.showMessageDialog(this.gui, Language.getString("screen.summary.useredit.creatorremove"), Language.getString("screen.summary.useredit.errortitle"), JOptionPane.ERROR_MESSAGE);
					continue;
				}
				this.chooser.users.remove(u);
				this.chooser.notSelectedUsers.add(u);
				CollectionsUtil.sortAscending(this.chooser.notSelectedUsers, "getLoginLowerCase");
			}
			this.chooser.fireContentChanged();
		}
		else if(e.getActionCommand().equalsIgnoreCase("ok"))
		{
			this.users = new LinkedList<User>(this.chooser.getUsers());
			this.chooser.setVisible(false);
		}
		else if(e.getActionCommand().equalsIgnoreCase("cancel"))
		{
			this.users = new LinkedList<User>(this.chooser.getUsersBackup());
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
		this.label.setText(userCollectionToString((Collection<User>) value));
		return this.label;
	}

	private class UserChooser extends JDialog implements WindowListener
	{
		private static final long	serialVersionUID		= 1L;

		private static final int	listFixedCellWidth		= 250;

		private static final int	width					= 700;
		private static final int	height					= 400;

		private JList<User>			notSelectedUsersLI;
		private JList<User>			usersLI;

		private JButton				okButton;
		private JButton				cancelButton;
		private JButton				addButton;
		private JButton				removeButton;

		private PlannedGame			game;

		private final List<User>	users					= new LinkedList<User>();
		private final List<User>	notSelectedUsers		= new LinkedList<User>();
		private final List<User>	usersBackup				= new LinkedList<User>();

		private final UserModel		usersModel				= new UserModel(users);
		private final UserModel		notSelectedUsersModel	= new UserModel(notSelectedUsers);

		public UserChooser(JFrame frame)
		{
			super(frame);

			this.setTitle(Language.getString("screen.summary.edit.players"));
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
			okButton.addActionListener(UserCellEditor.this);
			gbc.gridx = 0;
			gbc.gridy = 0;
			buttonPanel.add(okButton, gbc);

			cancelButton = new JButton(Language.getString("option.cancel"));
			cancelButton.setActionCommand("cancel");
			cancelButton.addActionListener(UserCellEditor.this);
			gbc.gridx = 1;
			gbc.gridy = 0;
			buttonPanel.add(cancelButton, gbc);

			this.add(buttonPanel, BorderLayout.SOUTH);

			JPanel contentPanel = new JPanel();
			this.add(contentPanel, BorderLayout.CENTER);
			contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));

			JPanel allUsersPanel = new JPanel();
			allUsersPanel.setLayout(new BorderLayout(5, 5));
			contentPanel.add(allUsersPanel);

			this.notSelectedUsersLI = new JList<>(this.notSelectedUsersModel);
			this.notSelectedUsersLI.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			this.notSelectedUsersLI.setFixedCellWidth(listFixedCellWidth);
			JScrollPane notSelectedUsersSP = new JScrollPane(this.notSelectedUsersLI, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			allUsersPanel.add(new JLabel(Language.getString("screen.users.allusers")), BorderLayout.NORTH);
			allUsersPanel.add(notSelectedUsersSP, BorderLayout.CENTER);

			buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridBagLayout());
			contentPanel.add(buttonPanel);

			gbc.fill = GridBagConstraints.HORIZONTAL;

			addButton = new JButton(Language.getString("option.add"));
			addButton.setActionCommand("add");
			addButton.addActionListener(UserCellEditor.this);
			gbc.gridx = 0;
			gbc.gridy = 0;
			buttonPanel.add(addButton, gbc);

			removeButton = new JButton(Language.getString("option.remove"));
			removeButton.setActionCommand("remove");
			removeButton.addActionListener(UserCellEditor.this);
			gbc.gridx = 0;
			gbc.gridy = 1;
			buttonPanel.add(removeButton, gbc);

			JPanel gameUsersPanel = new JPanel();
			gameUsersPanel.setLayout(new BorderLayout(5, 5));
			contentPanel.add(gameUsersPanel);

			this.usersLI = new JList<>(this.usersModel);
			this.usersLI.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			this.usersLI.setFixedCellWidth(listFixedCellWidth);
			JScrollPane usersSP = new JScrollPane(this.usersLI, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			gameUsersPanel.add(new JLabel(Language.getString("screen.users.selectedusers")), BorderLayout.NORTH);
			gameUsersPanel.add(usersSP, BorderLayout.CENTER);
		}

		public void fireContentChanged()
		{
			this.usersModel.fireContentChanged();
			this.notSelectedUsersModel.fireContentChanged();
		}

		public void setUsers(PlannedGame game, Collection<User> users)
		{
			this.game = game;

			this.users.clear();
			this.users.addAll(users);

			this.usersBackup.clear();
			this.usersBackup.addAll(users);

			this.notSelectedUsers.clear();
			this.notSelectedUsers.addAll(allUsers);
			this.notSelectedUsers.removeAll(this.users);
			CollectionsUtil.sortAscending(this.notSelectedUsers, "getLoginLowerCase");

			this.fireContentChanged();
		}

		public List<User> getUsers()
		{
			return users;
		}

		public List<User> getUsersBackup()
		{
			return usersBackup;
		}

		private class UserModel extends AbstractListModel<User>
		{
			private static final long	serialVersionUID	= 1L;

			private List<User>			userList;

			public UserModel(List<User> userList)
			{
				this.userList = userList;
			}

			@Override
			public int getSize()
			{
				return this.userList.size();
			}

			@Override
			public User getElementAt(int index)
			{
				return this.userList.get(index);
			}

			public void fireContentChanged()
			{
				fireContentsChanged(this, 0, this.userList.size() - 1);
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
