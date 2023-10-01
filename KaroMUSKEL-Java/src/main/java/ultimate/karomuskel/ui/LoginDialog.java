package ultimate.karomuskel.ui;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class LoginDialog
{
	private static LoginDialog instance = new LoginDialog();

	public static LoginDialog getInstance()
	{
		return instance;
	}

	private String					user;
	private String					password;

	private final String			logintitle;

	private final JLabel			label;
	private final JLabel			tfL;
	private final JTextField		tf;
	private final JLabel			pwL;
	private final JPasswordField	pw;

	private LoginDialog()
	{
		logintitle = Language.getString("login.title");

		label = new JLabel(Language.getString("login.description"));
		tfL = new JLabel(Language.getString("login.username"));
		tf = new JTextField();
		pwL = new JLabel(Language.getString("login.password"));
		pw = new JPasswordField();

		// request Focus on username-TF when dialog is shown
		tfL.addAncestorListener(new AncestorListener() {

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

	public int show()
	{
		int result = JOptionPane.showConfirmDialog(null, new Object[] { label, tfL, tf, pwL, pw }, logintitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

		if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION)
		{
			user = null;
			password = null;
		}
		else
		{
			user = tf.getText();
			password = new String(pw.getPassword());
		}
		return result;
	}

	public String getUser()
	{
		return user;
	}

	public String getPassword()
	{
		return password;
	}
}
