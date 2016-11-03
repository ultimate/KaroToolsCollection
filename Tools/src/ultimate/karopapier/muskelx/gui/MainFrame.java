package ultimate.karopapier.muskelx.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import muskel2.util.Language;
import ultimate.karopapier.muskelx.KaroMUSKEL;
import ultimate.karopapier.muskelx.Launcher;

public class MainFrame extends JFrame implements WindowListener, ActionListener
{
	private static final long	serialVersionUID	= 1L;

	public MainFrame()
	{
		super();
		
		this.updateTitle();
		
		// make visible and maximize
		this.setState(MAXIMIZED_BOTH);
		this.setVisible(true);
	}

	public void updateTitle()
	{
		String app = Language.getString("application.name") + " " + Language.getString("application.version");
		String user = getMuskel().getActiveProfile().getKaroUsername();
		String debug = getMuskel().getKaropapier().isInDebugMode() ? " ***DEBUG-MODE***" : "";
		this.setTitle(app + " (" + user + ")" + debug);
		
	}

	private KaroMUSKEL getMuskel()
	{
		return Launcher.getMuskel();
	}

	private void initMenu()
	{

	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// TODO Auto-generated method stub

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
		// TODO Dialog zum Bestaetigen
		Launcher.exit();
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
