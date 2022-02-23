package ultimate.karomuskel.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karomuskel.GameSeriesManager;

public class FileDialog
{
	private static FileDialog	instance	= new FileDialog();

	private JFileChooser		fileChooser	= new JFileChooser();
	
	private FileFilter 			jsonFilter = new FileNameExtensionFilter("JSON-File", "json");

	public static FileDialog getInstance()
	{
		return instance;
	}

	private File currentDirectory;

	private FileDialog()
	{
		this.currentDirectory = new File(".");
	}

	public boolean showSave(Component parent, GameSeries gameSeries)
	{
		this.fileChooser.setCurrentDirectory(this.currentDirectory);
		this.fileChooser.resetChoosableFileFilters();
		this.fileChooser.addChoosableFileFilter(jsonFilter);
		this.fileChooser.setAcceptAllFileFilterUsed(true);
		
		int action = this.fileChooser.showSaveDialog(parent);

		File file = this.fileChooser.getSelectedFile();
		this.currentDirectory = file.isDirectory() ? file : file.getParentFile();

		if(action != JFileChooser.APPROVE_OPTION)
			return false;

		if(file.exists())
		{
			int overwrite = JOptionPane.showConfirmDialog(parent, Language.getString("option.overwrite"));
			if(overwrite != JOptionPane.OK_OPTION)
				return false;
		}

		try
		{
			GameSeriesManager.store(gameSeries, file);
			return true;
		}
		catch(IOException ex)
		{
			JOptionPane.showMessageDialog(parent, Language.getString("error.save") + ex.getLocalizedMessage(), Language.getString("error.title"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public GameSeries showLoad(Component parent, KaroAPICache karoAPICache)
	{
		this.fileChooser.setCurrentDirectory(this.currentDirectory);
		this.fileChooser.resetChoosableFileFilters();
		this.fileChooser.addChoosableFileFilter(jsonFilter);
		this.fileChooser.setAcceptAllFileFilterUsed(true);

		int action = this.fileChooser.showOpenDialog(parent);

		File file = this.fileChooser.getSelectedFile();
		this.currentDirectory = file.isDirectory() ? file : file.getParentFile();

		if(action != JFileChooser.APPROVE_OPTION)
			return null;
		if(!file.exists())
		{
			JOptionPane.showMessageDialog(parent, Language.getString("error.load") + Language.getString("error.load.notExisting"), Language.getString("error.title"), JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		try
		{
			return GameSeriesManager.load(file, karoAPICache);
		}
		catch(ClassCastException ex)
		{
			JOptionPane.showMessageDialog(parent, Language.getString("error.load") +  Language.getString("error.load.notAGameSeries"), Language.getString("error.title"), JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
		catch(IOException ex)
		{
			JOptionPane.showMessageDialog(parent, Language.getString("error.load") + ex.getLocalizedMessage(), Language.getString("error.title"), JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
		return null;
	}
}
