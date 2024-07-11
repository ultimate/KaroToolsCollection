package ultimate.karomuskel.ui.dialog;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import ultimate.karoapi4j.utils.StringUtil;
import ultimate.karomuskel.ui.Language;

public class GIDSelectionDialog
{
	private static GIDSelectionDialog instance = new GIDSelectionDialog();

	public static GIDSelectionDialog getInstance()
	{
		return instance;
	}

	private int[] gids;

	private final String			title;

	private final JLabel			label;
	private final JTextField		tf;

	private GIDSelectionDialog()
	{
		title = Language.getString("gidselection.title");

		label = new JLabel(Language.getString("gidselection.description"));
		tf = new JTextField();

		// request Focus on username-TF when dialog is shown
		tf.addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent arg0)
			{
				tf.requestFocusInWindow();
			}

			@Override
			public void ancestorMoved(AncestorEvent arg0)
			{
			}

			@Override
			public void ancestorRemoved(AncestorEvent arg0)
			{
			}
		});
	}

	public int[] show()
	{
		while(true)
		{
			int result = JOptionPane.showConfirmDialog(null, new Object[] { label, tf }, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	
			if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION)
			{
				gids = null;
				break;
			}
			else
			{
				try
				{
					gids = StringUtil.parseRanges(tf.getText());
					break;
				}
				catch(Exception e)
				{
					JOptionPane.showMessageDialog(null, Language.getString("gidselection.error"), Language.getString("error.title"), JOptionPane.WARNING_MESSAGE);
					continue;
				}
			}
		}
		return gids;
	}

	public int[] getGIDs()
	{
		return gids;
	}
}
