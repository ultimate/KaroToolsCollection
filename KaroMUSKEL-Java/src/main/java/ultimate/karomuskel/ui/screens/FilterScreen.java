package ultimate.karomuskel.ui.screens;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.Language.Label;
import ultimate.karomuskel.ui.MainFrame;
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.components.BooleanModel;
import ultimate.karomuskel.ui.components.FilterModel;
import ultimate.karomuskel.ui.components.GenericEnumModel;

public abstract class FilterScreen<V> extends Screen
{
	private static final long			serialVersionUID	= 1L;

	private Dimension					labelSize			= new Dimension(100, 20);
	private Dimension					spacerSize			= new Dimension(10, 20);

	private JPanel						filterPanel;
	private JPanel						contentPanel;

	private List<Function<V, Boolean>>	filters;

	private FilterModel<V>				model;
	
	public enum NumberFilterMode
	{
		eq,
		lt,
		lteq,
		gt,
		gteq
	}

	public FilterScreen(MainFrame gui, Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton, String headerKey)
	{
		super(gui, previous, karoAPICache, previousButton, nextButton, "screen.maps.header");

		this.setLayout(new BorderLayout(5, 5));

		this.filterPanel = new JPanel();
		this.filterPanel.setLayout(new BoxLayout(this.filterPanel, BoxLayout.X_AXIS));
		this.add(this.filterPanel, BorderLayout.NORTH);

		JLabel label = new JLabel(Language.getString("screen.filter"));
		label.setPreferredSize(this.labelSize);
		label.setMaximumSize(this.labelSize);
		this.filterPanel.add(label);

		// TODO support multiple lines for the filters

		this.contentPanel = new JPanel();
		this.contentPanel.setLayout(new BoxLayout(this.contentPanel, BoxLayout.X_AXIS));
		this.add(this.contentPanel, BorderLayout.CENTER);

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

	protected FilterModel<V> getModel()
	{
		return model;
	}

	protected void setModel(FilterModel<V> model)
	{
		this.model = model;
		this.model.setFilter((t) -> {
			for(Function<V, Boolean> filter : this.filters)
			{
				boolean result = filter.apply(t);
				// logger.debug("item " + t + " ? " + result);
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
	 * Add a text filter to this {@link FilterScreen} that is defined by a {@link Function} to
	 * extract the text value from the items in the {@link FilterModel}.
	 * The text filter will add a JTextField plus the given label to the screen
	 * 
	 * @param labelKey
	 * @param valueExtractor 
	 * @param multiSearch
	 */
	protected void addTextFilter(String labelKey, Function<V, String> valueExtractor, boolean multiSearch)
	{
		JTextField component = new JTextField();
		component.getDocument().addDocumentListener(new DocumentChangeListener(component));
		Function<V, Boolean> filter = item -> {
			String haystack = valueExtractor.apply(item).toLowerCase();
			String needle = component.getText().toLowerCase();

			if(!multiSearch)
				return haystack.contains(needle);

			String[] needles = needle.split(",");
			for(String n : needles)
			{
				if(haystack.contains(n.trim()))
					return true;
			}
			return false;
		};
		this.addFilter(labelKey, component, filter);
	}

	/**
	 * Add a boolean filter to this {@link FilterScreen} that is defined by a {@link Function} to
	 * extract the boolean value from the items in the {@link FilterModel}.
	 * The text filter will add a JComboBox plus the given label to the screen
	 * 
	 * @param labelKey
	 * @param valueExtractor
	 * @param multiSearch
	 */
	protected void addBooleanFilter(String labelKey, Function<V, Boolean> valueExtractor)
	{
		addDropdownFilter(labelKey, valueExtractor, new BooleanModel(null, "option.boolean.empty", 0));
	}

	/**
	 * Add a enum filter to this {@link FilterScreen} that is defined by a {@link Function} to
	 * extract the enum value from the items in the {@link FilterModel}.
	 * The text filter will add a JComboBox plus the given label to the screen
	 * 
	 * @param labelKey
	 * @param valueExtractor
	 * @param multiSearch
	 */
	protected <E extends Enum<E>> void addEnumFilter(String labelKey, Function<V, E> valueExtractor, Class<E> enumType)
	{
		addDropdownFilter(labelKey, valueExtractor, new GenericEnumModel<E>(enumType, null, false, true));
	}	

	/**
	 * Add a dropdown filter to this {@link FilterScreen} that is defined by a {@link Function} to
	 * extract the dropdown value from the items in the {@link FilterModel}.
	 * The text filter will add a JComboBox plus the given label to the screen
	 * 
	 * @param labelKey
	 * @param valueExtractor
	 * @param multiSearch
	 */
	protected <T> void addDropdownFilter(String labelKey, Function<V, T> valueExtractor, ComboBoxModel<Label<T>> comboboxModel)
	{
		JComboBox<Label<T>> component = new JComboBox<>(comboboxModel);
		component.addItemListener(e -> filtersChanged(component));
		Function<V, Boolean> filter = item -> {
			@SuppressWarnings("unchecked")
			Label<T> filterItem = (Label<T>) component.getSelectedItem();
			T itemValue = valueExtractor.apply(item);

			if(filterItem == null || filterItem.getValue() == null)
				return true;
			return filterItem.getValue() == itemValue;
		};
		this.addFilter(labelKey, component, filter);		
	}

	/**
	 * Add a number filter to this {@link FilterScreen} that is defined by a {@link Function} to
	 * extract the number value from the items in the {@link FilterModel}.
	 * The text filter will add a JComboBox plus the given label to the screen
	 * 
	 * @param labelKey
	 * @param valueExtractor
	 * @param multiSearch
	 */
	protected void addNumberFilter(String labelKey, Function<V, Integer> valueExtractor, NumberFilterMode mode, int initialValue, int min, int max)
	{
		JSpinner component = new JSpinner(new SpinnerNumberModel(initialValue, min, max, 1));
		component.addChangeListener(e -> filtersChanged(component));
		Function<V, Boolean> filter = item -> {
			int filterValue = (int) component.getValue();
			int itemValue = valueExtractor.apply(item);

			switch(mode)
			{
				case eq: 	return itemValue == filterValue;
				case gt:	return itemValue > filterValue;
				case gteq: 	return itemValue >= filterValue;
				case lt:	return itemValue < filterValue;
				case lteq:	return itemValue <= filterValue;
			}
			return true;			
		};
		this.addFilter(labelKey, component, filter);		
	}
	
	
	/**
	 * Add a component and a matching filter to this {@link Screen}
	 * 
	 * @param labelKey
	 * @param component
	 * @param filter
	 */
	private void addFilter(String labelKey, JComponent component, Function<V, Boolean> filter)
	{
		this.filterPanel.add(Box.createRigidArea(this.spacerSize));
		this.filterPanel.add(new JLabel(Language.getString(labelKey) + ": "));
		this.filterPanel.add(component);
		this.filters.add(filter);
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
