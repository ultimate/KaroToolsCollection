package ultimate.karomuskel.ui.dialog;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.JTextComponent;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Generator;
import ultimate.karomuskel.ui.Language;

/**
 * This class provides an abstraction for a dialog to edit the settings of a {@link Generator}.<br>
 * 
 * @author ultimate
 */
public class GeneratorDialog
{
	private static GeneratorDialog instance = new GeneratorDialog();

	public static GeneratorDialog getInstance()
	{
		return instance;
	}

	private String				key;
	private Map<String, Object>	settings;

	private final String		dialogtitle;

	private GeneratorDialog()
	{
		dialogtitle = Language.getString("generator.edit.title");
	}

	public int showEdit(Component parent, Generator generator)
	{
		this.key = generator.getKey();
		this.settings = new HashMap<>(generator.getSettings());

		List<String> sortedKeys = new ArrayList<>(this.settings.keySet());
		Collections.sort(sortedKeys);

		// align settings in 2 columns
		List<String> column1 = new LinkedList<>();
		List<String> column2 = new LinkedList<>();
		for(String settingsKey : sortedKeys)
		{
			if(column1.contains(settingsKey))
				continue;
			if(column2.contains(settingsKey))
				continue;

			column1.add(settingsKey);
			String sibling = KaroAPI.getStringProperty("generator." + this.key + "." + settingsKey + ".sibling");
			if(sibling != null)
				column2.add(sibling);
			else
				column2.add(null);
		}

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		HashMap<String, JComponent> components = new HashMap<>();

		JComponent c;
		for(int i = 0; i < column1.size(); i++)
		{
			c = addSetting(panel, column1.get(i), this.settings.get(column1.get(i)), i, 0);
			if(c != null)
				components.put(column1.get(i), c);

			c = addSetting(panel, column2.get(i), this.settings.get(column2.get(i)), i, 1);
			if(c != null)
				components.put(column2.get(i), c);
		}

		int result = JOptionPane.showConfirmDialog(parent, panel, dialogtitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

		if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION)
		{
			// do nothing
		}
		else
		{
			Object value;
			for(Entry<String, JComponent> ce : components.entrySet())
			{
				if(ce.getValue() instanceof JTextComponent)
					value = ((JTextField) ce.getValue()).getText();
				else if(ce.getValue() instanceof JSpinner)
					value = ((JSpinner) ce.getValue()).getValue();
				else if(ce.getValue() instanceof JCheckBox)
					value = ((JCheckBox) ce.getValue()).isSelected();
				else
					value = null;
				if(ce.getKey() != null)
					this.settings.put(ce.getKey(), value);
			}
		}
		return result;
	}

	private JComponent addSetting(JPanel panel, String setting, Object value, int row, int column)
	{
		if(setting != null)
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = column * 2;
			gbc.gridy = row;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(5, 5, 5, 5);
			panel.add(new JLabel(setting), gbc);

			JComponent component = null;
			gbc.gridx++;
			gbc.anchor = GridBagConstraints.EAST;
			if(value instanceof Integer)
			{
				int min = KaroAPI.getIntProperty("generator." + this.key + "." + setting + ".min", 0);
				int max = KaroAPI.getIntProperty("generator." + this.key + "." + setting + ".max", 99);
				int step = KaroAPI.getIntProperty("generator." + this.key + "." + setting + ".step", 1);
				component = new JSpinner(new SpinnerNumberModel((int) value, min, max, step));
				component.setEnabled(min != max);
			}
			else if(value instanceof String)
			{
				component = new JTextField((String) value);// , 20);
				gbc.gridwidth = 3;
			}
			else if(value instanceof Boolean)
			{
				component = new JCheckBox("", (boolean) value);
			}
			panel.add(component, gbc);

			return component;
		}
		else
		{
			return null;
		}
	}

	public Map<String, Object> getSettings()
	{
		return settings;
	}
}
