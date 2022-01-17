package muskel2.gui.screens;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import muskel2.core.exceptions.GameSeriesException;
import muskel2.gui.Screen;
import muskel2.model.Direction;
import muskel2.model.GameSeries;
import muskel2.model.Karopapier;
import muskel2.model.Rules;
import muskel2.model.help.BooleanModel;
import muskel2.model.help.DirectionModel;
import muskel2.model.help.Label;
import muskel2.model.series.BalancedGameSeries;
import muskel2.util.Language;

public class RulesScreen extends Screen
{
	private static final long	serialVersionUID	= 1L;
	
	private GridBagConstraints  gbc;
	
	private JSpinner minZzzSpinner;
	private JSpinner maxZzzSpinner;	
	private JComboBox crashingAllowedCB;	
	private JComboBox checkpointsActivatedCB;	
	private JComboBox directionCB;	
	private JComboBox creatorGiveUpCB;	
	private JComboBox ignoreInvitableCB;
	
	public RulesScreen(Screen previous, Karopapier karopapier, JButton previousButton, JButton nextButton)
	{
		super(previous, karopapier, previousButton, nextButton, "screen.rules.header", "screen.rules.next");

		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);
		this.gbc = new GridBagConstraints();
		this.gbc.anchor = GridBagConstraints.LINE_START;
		this.gbc.insets = new Insets(insetsV,insetsH,insetsV,insetsH);
		this.gbc.fill = GridBagConstraints.HORIZONTAL;
		
		this.minZzzSpinner = new JSpinner(new SpinnerNumberModel(2, 0, Integer.MAX_VALUE, 1));
		this.maxZzzSpinner = new JSpinner(new SpinnerNumberModel(2, 0, Integer.MAX_VALUE, 1));
		this.crashingAllowedCB = new JComboBox(new BooleanModel(false, true));
		this.checkpointsActivatedCB = new JComboBox(new BooleanModel(true, true));
		this.directionCB = new JComboBox(new DirectionModel(Direction.klassisch, true));
		this.creatorGiveUpCB = new JComboBox(new BooleanModel(false, false));
		this.creatorGiveUpCB.setEnabled(this.karopapier.isUnlocked());
		this.ignoreInvitableCB = new JComboBox(new BooleanModel(false, false));
		this.ignoreInvitableCB.setEnabled(this.karopapier.isUnlocked());

	}

	@SuppressWarnings("unchecked")
	private Rules createRules() throws GameSeriesException
	{
		int minZzz = (Integer) minZzzSpinner.getValue(); 
		int maxZzz = (Integer) maxZzzSpinner.getValue(); 		
		if(maxZzz < minZzz)
		{
			throw new GameSeriesException("screen.rules.invalidzzz");
		}
		
		Boolean crashingAllowed = ((Label<Boolean>) crashingAllowedCB.getSelectedItem()).getValue();
		Boolean checkpointsActivated = ((Label<Boolean>) checkpointsActivatedCB.getSelectedItem()).getValue();
		Direction direction;
		if(directionCB.getSelectedItem() instanceof Label)
			direction = null;
		else
			direction = (Direction) directionCB.getSelectedItem();
		boolean creatorGiveUp = ((Label<Boolean>) creatorGiveUpCB.getSelectedItem()).getValue();
		boolean ignoreInvitable = ((Label<Boolean>) ignoreInvitableCB.getSelectedItem()).getValue();
		return new Rules(minZzz, maxZzz, crashingAllowed, checkpointsActivated, direction, creatorGiveUp, ignoreInvitable);
	}

	@Override
	public GameSeries applySettings(GameSeries gameSeries) throws GameSeriesException
	{
		Rules rules = createRules();
		gameSeries.setRules(rules);
		return gameSeries;
	}

	@Override
	public void updateBeforeShow(GameSeries gameSeries)
	{
		this.removeAll();
		
		gbc.gridy = 0;
		
		if(!(gameSeries instanceof BalancedGameSeries))
		{		
			gbc.gridx = 0; 
			gbc.gridwidth = 1;
			this.add(new JLabel(Language.getString("screen.rules.minzzz")), gbc);
			gbc.gridx = 1; 
			gbc.gridwidth = 1;
			this.add(this.minZzzSpinner, gbc);
			gbc.gridx = 2; 
			gbc.gridwidth = 1;
			this.add(new JLabel(Language.getString("screen.rules.maxzzz")), gbc);
			gbc.gridx = 3; 
			gbc.gridwidth = 1;
			this.add(this.maxZzzSpinner, gbc);
			gbc.gridy++;
			gbc.gridx = 0; 
			gbc.gridwidth = 4;
			this.add(new JLabel(Language.getString("screen.rules.zzz.description")), gbc);
			gbc.gridy++;
			gbc.gridx = 0; 
			gbc.gridwidth = 1;
			this.add(new JLabel(Language.getString("screen.rules.crashs")), gbc);
			gbc.gridx = 1; 
			gbc.gridwidth = 3;
			this.add(this.crashingAllowedCB, gbc);
			gbc.gridy++;
			gbc.gridx = 0; 
			gbc.gridwidth = 1;
			this.add(new JLabel(Language.getString("screen.rules.cps")), gbc);
			gbc.gridx = 1; 
			gbc.gridwidth = 3;
			this.add(this.checkpointsActivatedCB, gbc);
			gbc.gridy++;
			gbc.gridx = 0; 
			gbc.gridwidth = 1;
			this.add(new JLabel(Language.getString("screen.rules.direction")), gbc);
			gbc.gridx = 1; 
			gbc.gridwidth = 3;
			this.add(this.directionCB, gbc);
		}		
		gbc.gridy++;
		gbc.gridx = 0; 
		gbc.gridwidth = 1;
		this.add(new JLabel(Language.getString("screen.rules.creatorgiveup")), gbc);
		gbc.gridx = 1; 
		gbc.gridwidth = 3;
		this.add(this.creatorGiveUpCB, gbc);
		gbc.gridy++;
		gbc.gridx = 0; 
		gbc.gridwidth = 4;
		this.add(new JLabel(Language.getString("screen.rules.creatorgiveup.description")), gbc);
		gbc.gridy++;
		gbc.gridx = 0; 
		gbc.gridwidth = 1;
		this.add(new JLabel(Language.getString("screen.rules.ignoreinvitable")), gbc);
		gbc.gridx = 1; 
		gbc.gridwidth = 3;
		this.add(this.ignoreInvitableCB, gbc);
		gbc.gridy++;
		gbc.gridx = 0; 
		gbc.gridwidth = 4;
		this.add(new JLabel(Language.getString("screen.rules.ignoreinvitable.description")), gbc);
	}
}
