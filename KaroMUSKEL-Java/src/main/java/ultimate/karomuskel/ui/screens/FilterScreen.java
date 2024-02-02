package ultimate.karomuskel.ui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.MainFrame;
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.components.GenericListModel;

public abstract class FilterScreen<K, V> extends Screen
{
	private static final long				serialVersionUID	= 1L;

	private JPanel							filterPanel;
	private JPanel							contentPanel;

	private List<Function<V, Boolean>>	filters;

	private GenericListModel<K, V>			model;

	public FilterScreen(MainFrame gui, Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton, String headerKey)
	{
		super(gui, previous, karoAPICache, previousButton, nextButton, "screen.maps.header");

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBackground(Color.red);
//		this.setLayout(new BorderLayout(5, 5));

		this.add(new JLabel(Language.getString("screen.filter")));//, BorderLayout.NORTH);

		this.filterPanel = new JPanel();
		this.filterPanel.setBackground(Color.green);
		this.filterPanel.setAlignmentX(LEFT_ALIGNMENT);
		this.filterPanel.setLayout(new BoxLayout(this.filterPanel, BoxLayout.X_AXIS));
		this.add(this.filterPanel);//, BorderLayout.CENTER);
		

		this.contentPanel = new JPanel();
		this.contentPanel.setLayout(new BoxLayout(this.contentPanel, BoxLayout.X_AXIS));
		this.add(this.contentPanel);//, BorderLayout.SOUTH);

		this.filters = new LinkedList<Function<V, Boolean>>();
	}

	protected JPanel getFilterPanel()
	{
		return this.filterPanel;
	}

	protected JPanel getContentPanel()
	{
		return this.contentPanel;
	}

	protected GenericListModel<K, V> getModel()
	{
		return model;
	}

	protected void setModel(GenericListModel<K, V> model)
	{
		this.model = model;
		this.model.setFilter((t) -> {
			for(Function<V, Boolean> filter : this.filters)
			{
				boolean result = filter.apply(t);
				logger.debug("item " + t + " ? " + result);
				if(!result)
					return false;
			}
			return true;
		});
	}

	/**
	 * Event that is fired when the input components added to this {@link Screen} are changed.
	 * 
	 * @param component
	 */
	protected void filtersChanged(JComponent component)
	{
		this.model.refresh();
	}

	/**
	 * 
	 * Add a component and a matching filter to this {@link Screen}
	 * This will allow you to retrieve the compound filter for all components via
	 * {@link FilterScreen#getCompoundFilter()} and listen to change events via
	 * {@link FilterScreen#filtersChanged(JComponent)}
	 * 
	 * @param labelKey
	 * @param component
	 * @param filter
	 */
	protected void addFilterComponent(String labelKey, JComponent component, BiFunction<Object, V, Boolean> filter)
	{
		Function<V, Boolean> internalFilter;
		if(component instanceof JSpinner)
		{
			((JSpinner) component).addChangeListener(e -> filtersChanged(component));
			internalFilter = item -> filter.apply(((JSpinner) component).getValue(), item);
		}
		else if(component instanceof JComboBox)
		{
			((JComboBox<?>) component).addItemListener(e -> filtersChanged(component));
			internalFilter = item -> filter.apply(((JComboBox<?>) component).getSelectedItem(), item);
		}
		else if(component instanceof JTextComponent)
		{
			((JTextComponent) component).getDocument().addDocumentListener(new DocumentChangeListener(component));
			internalFilter = item -> filter.apply(((JTextComponent) component).getText(), item);
		}
		else
			throw new IllegalArgumentException("unsupported component type: " + component.getClass());

		JLabel label = new JLabel(Language.getString(labelKey));
		label.setMaximumSize(new Dimension(100, 20));
		this.filterPanel.add(label);
		component.setMaximumSize(new Dimension(100, 20));
		this.filterPanel.add(component);
		this.filters.add(internalFilter);
	}

	/**
	 * Internal helper class to wrap {@link DocumentEvent}s to
	 * {@link FilterScreen#filtersChanged(JComponent)}
	 */
	private class DocumentChangeListener implements DocumentListener
	{
		private JComponent component;

		private DocumentChangeListener(JComponent component)
		{
			super();
			this.component = component;
		}

		@Override
		public void insertUpdate(DocumentEvent e)
		{
			changedUpdate(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e)
		{
			changedUpdate(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e)
		{
			FilterScreen.this.filtersChanged(this.component);
		}
	};
}
