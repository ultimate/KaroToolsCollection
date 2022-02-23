package ultimate.karomuskel.ui.screens;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import ultimate.karoapi4j.KaroAPICache;
import ultimate.karoapi4j.enums.EnumGameDirection;
import ultimate.karoapi4j.enums.EnumGameSeriesType;
import ultimate.karoapi4j.enums.EnumGameTC;
import ultimate.karoapi4j.exceptions.GameSeriesException;
import ultimate.karoapi4j.model.extended.GameSeries;
import ultimate.karoapi4j.model.extended.Rules;
import ultimate.karomuskel.ui.Language;
import ultimate.karomuskel.ui.Language.Label;
import ultimate.karomuskel.ui.Screen;
import ultimate.karomuskel.ui.components.BooleanModel;
import ultimate.karomuskel.ui.components.GenericEnumModel;

public class RulesScreen extends Screen
{
	private static final long					serialVersionUID	= 1L;

	private GridBagConstraints					gbc;

	private JSpinner							minZzzSpinner;
	private JSpinner							maxZzzSpinner;
	private JComboBox<Label<EnumGameTC>>		crashingAllowedCB;
	private JComboBox<Label<Boolean>>			checkpointsActivatedCB;
	private JComboBox<Label<EnumGameDirection>>	directionCB;
	private JComboBox<Label<Boolean>>			creatorGiveUpCB;
	private JComboBox<Label<Boolean>>			ignoreInvitableCB;

	public RulesScreen(JFrame gui, Screen previous, KaroAPICache karoAPICache, JButton previousButton, JButton nextButton)
	{
		super(gui, previous, karoAPICache, previousButton, nextButton, "screen.rules.header", "screen.rules.next");

		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);
		this.gbc = new GridBagConstraints();
		this.gbc.anchor = GridBagConstraints.LINE_START;
		this.gbc.insets = new Insets(insetsV, insetsH, insetsV, insetsH);
		this.gbc.fill = GridBagConstraints.HORIZONTAL;

		this.minZzzSpinner = new JSpinner(new SpinnerNumberModel(2, 0, Integer.MAX_VALUE, 1));
		this.maxZzzSpinner = new JSpinner(new SpinnerNumberModel(2, 0, Integer.MAX_VALUE, 1));
		this.crashingAllowedCB = new JComboBox<>(new GenericEnumModel<EnumGameTC>(EnumGameTC.class, EnumGameTC.forbidden, true));
		this.checkpointsActivatedCB = new JComboBox<>(new BooleanModel(true, true));
		this.directionCB = new JComboBox<>(new GenericEnumModel<EnumGameDirection>(EnumGameDirection.class, EnumGameDirection.classic, true));
		this.creatorGiveUpCB = new JComboBox<>(new BooleanModel(false, false));
		this.creatorGiveUpCB.setEnabled(this.karoAPICache.getCurrentUser().isSuperCreator());
		this.ignoreInvitableCB = new JComboBox<>(new BooleanModel(false, false));
		this.ignoreInvitableCB.setEnabled(this.karoAPICache.getCurrentUser().isSuperCreator());

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

		EnumGameTC crashingAllowed = ((Label<EnumGameTC>) crashingAllowedCB.getSelectedItem()).getValue();
		Boolean checkpointsActivated = ((Label<Boolean>) checkpointsActivatedCB.getSelectedItem()).getValue();
		EnumGameDirection direction = ((Label<EnumGameDirection>) directionCB.getSelectedItem()).getValue();
		
		return new Rules(minZzz, maxZzz, crashingAllowed, checkpointsActivated, direction);
	}

	@SuppressWarnings("unchecked")
	@Override
	public GameSeries applySettings(GameSeries gameSeries) throws GameSeriesException
	{
		Rules rules = createRules();
		gameSeries.setRules(rules);

		boolean creatorGiveUp = ((Label<Boolean>) creatorGiveUpCB.getSelectedItem()).getValue();
		gameSeries.setCreatorGiveUp(creatorGiveUp);

		boolean ignoreInvitable = ((Label<Boolean>) ignoreInvitableCB.getSelectedItem()).getValue();
		gameSeries.setIgnoreInvitable(ignoreInvitable);

		return gameSeries;
	}

	@Override
	public void updateBeforeShow(GameSeries gameSeries)
	{
		this.removeAll();

		gbc.gridy = 0;

		if(!(gameSeries.getType() == EnumGameSeriesType.Balanced))
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
