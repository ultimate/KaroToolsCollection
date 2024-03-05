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
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.model.official.PlannedGame;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.screens.SummaryScreen.SummaryModel;

public class TagCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener, TableCellRenderer
{
	protected transient final Logger	logger				= LogManager.getLogger(getClass());

	private static final long	serialVersionUID	= 1L;

	private JButton				button;
	private JLabel				label;

	private TagChooser			chooser;

	private Collection<String>	tags;

	private JFrame				gui;
	private SummaryModel		model;
	private KaroAPICache		karoAPICache;

	private PlannedGame			game;

	public TagCellEditor(JFrame gui, SummaryModel model, KaroAPICache karoAPICache)
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

		this.chooser = new TagChooser(this.gui);
	}

	@Override
	public Collection<String> getCellEditorValue()
	{
		return this.tags;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		logger.debug("getTableCellEditorComponent: " + value);
		this.tags = (Collection<String>) value;
		this.game = model.getGame(row);
		this.button.setText(tagCollectionToString(this.tags));
		return this.button;
	}

	private static String tagCollectionToString(Collection<String> tags)
	{
		StringBuilder sb = new StringBuilder();	
		if(tags != null)
		{
			for(String tag: tags)
			{
				if(!sb.toString().isEmpty())
				sb.append(", ");
				sb.append(tag);
			}
		}
		return sb.toString();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equalsIgnoreCase("edit"))
		{
			logger.debug("edit");
			logger.debug("this.tags:              " + this.tags);
			logger.debug("this.game.getTags():    " + this.game.getTags());
			logger.debug("this.chooser.getTags(): " + this.chooser.getTags());
			this.chooser.setTags(this.tags);
			this.chooser.setVisible(true);
			fireEditingStopped();
		}
		else if(e.getActionCommand().equalsIgnoreCase("ok"))
		{
			logger.debug("ok");
			logger.debug("this.tags:              " + this.tags);
			logger.debug("this.game.getTags():    " + this.game.getTags());
			logger.debug("this.chooser.getTags(): " + this.chooser.getTags());
			this.tags = new LinkedHashSet<String>(this.chooser.getTags());
			this.chooser.setVisible(false);
		}
		else if(e.getActionCommand().equalsIgnoreCase("cancel"))
		{
			logger.debug("cancel");
			logger.debug("this.tags:              " + this.tags);
			logger.debug("this.game.getTags():    " + this.game.getTags());
			logger.debug("this.chooser.getTags(): " + this.chooser.getTags());
			this.tags = new LinkedHashSet<String>(this.game.getTags());
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
		this.label.setText(tagCollectionToString((Collection<String>) value));
		return this.label;
	}

	private class TagChooser extends JDialog implements WindowListener
	{
		private static final long	serialVersionUID		= 1L;

		private static final int	width					= 400;
		private static final int	height					= 250;

		private TagEditor 			tagEditor;
		private JButton				okButton;
		private JButton				cancelButton;

		public TagChooser(JFrame frame)
		{
			super(frame);

			this.setTitle(Language.getString("screen.summary.edit.tags"));
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
			okButton.addActionListener(TagCellEditor.this);
			gbc.gridx = 0;
			gbc.gridy = 0;
			buttonPanel.add(okButton, gbc);

			cancelButton = new JButton(Language.getString("option.cancel"));
			cancelButton.setActionCommand("cancel");
			cancelButton.addActionListener(TagCellEditor.this);
			gbc.gridx = 1;
			gbc.gridy = 0;
			buttonPanel.add(cancelButton, gbc);

			this.add(buttonPanel, BorderLayout.SOUTH);

			this.tagEditor = new TagEditor(karoAPICache.getSuggestedTags());
			this.add(tagEditor, BorderLayout.CENTER);
		}

		public void setTags(Collection<String> tags)
		{
			this.tagEditor.setSelectedTags(tags);
		}

		public Set<String> getTags()
		{
			return this.tagEditor.getSelectedTags();
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
