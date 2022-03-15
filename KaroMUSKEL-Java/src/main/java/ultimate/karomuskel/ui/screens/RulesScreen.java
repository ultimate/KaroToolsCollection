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
import ultimate.karomuskel.GameSeriesManager;
import ultimate.karomuskel.ui.EnumNavigation;
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
		super(gui, previous, karoAPICache, previousButton, nextButton, "screen.rules.header");

		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);
		this.gbc = new GridBagConstraints();
		this.gbc.anchor = GridBagConstraints.LINE_START;
		this.gbc.insets = new Insets(insetsV, insetsH, insetsV, insetsH);
		this.gbc.fill = GridBagConstraints.HORIZONTAL;
	}

	@Override
	public String getNextKey()
	{
		return "screen.rules.next";
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
	public GameSeries applySettings(GameSeries gameSeries, EnumNavigation direction) throws GameSeriesException
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
	public void updateBeforeShow(GameSeries gameSeries, EnumNavigation direction)
	{
		this.removeAll();

		int minZzzInit = (gameSeries.getRules() != null ? gameSeries.getRules().getMinZzz() : 2);
		this.minZzzSpinner = new JSpinner(new SpinnerNumberModel(minZzzInit, 0, Integer.MAX_VALUE, 1));

		int maxZzzInit = (gameSeries.getRules() != null ? gameSeries.getRules().getMaxZzz() : 2);
		this.maxZzzSpinner = new JSpinner(new SpinnerNumberModel(maxZzzInit, 0, Integer.MAX_VALUE, 1));

		EnumGameTC crashingAllowedInit = (gameSeries.getRules() != null ? gameSeries.getRules().getCrashallowed() : EnumGameTC.forbidden);
		this.crashingAllowedCB = new JComboBox<>(new GenericEnumModel<EnumGameTC>(EnumGameTC.class, crashingAllowedInit, true));
		
		Boolean checkpointsActivatedInit = (gameSeries.getRules() != null ? gameSeries.getRules().getCps() : Boolean.TRUE);
		this.checkpointsActivatedCB = new JComboBox<>(new BooleanModel(checkpointsActivatedInit, true));

		EnumGameDirection directionInit = (gameSeries.getRules() != null ? gameSeries.getRules().getStartdirection() : EnumGameDirection.classic);
		this.directionCB = new JComboBox<>(new GenericEnumModel<EnumGameDirection>(EnumGameDirection.class, directionInit, true));

		boolean creatorGiveUpInit = gameSeries.isCreatorGiveUp();
		this.creatorGiveUpCB = new JComboBox<>(new BooleanModel(creatorGiveUpInit, false));
		this.creatorGiveUpCB.setEnabled(GameSeriesManager.getBooleanConfig(GameSeriesManager.CONFIG_ALLOW_CREATOR_GIVE_UP));

		boolean ignoreInvitableInit = gameSeries.isIgnoreInvitable();
		this.ignoreInvitableCB = new JComboBox<>(new BooleanModel(ignoreInvitableInit, false));
		this.ignoreInvitableCB.setEnabled(this.karoAPICache.getCurrentUser().isSuperCreator() || GameSeriesManager.getBooleanConfig(GameSeriesManager.CONFIG_ALLOW_IGNORE_INVITABLE));

		gbc.gridy = 0;

		if(!(gameSeries.getType() == EnumGameSeriesType.Balanced))
		{
			gbc.gridx = 0;
			gbc.gridwidth = 1;
			this.add(new JLabel(Language.getString("screen.rules.minzzz", cellWidth)), gbc);
			gbc.gridx = 1;
			gbc.gridwidth = 1;
			this.add(this.minZzzSpinner, gbc);
			gbc.gridx = 2;
			gbc.gridwidth = 1;
			this.add(new JLabel(Language.getString("screen.rules.maxzzz", cellWidth)), gbc);
			gbc.gridx = 3;
			gbc.gridwidth = 1;
			this.add(this.maxZzzSpinner, gbc);
			gbc.gridy++;
			gbc.gridx = 0;
			gbc.gridwidth = 4;
			this.add(new JLabel(Language.getString("screen.rules.zzz.description", totalWidth)), gbc);
			gbc.gridy++;
			gbc.gridx = 0;
			gbc.gridwidth = 1;
			this.add(new JLabel(Language.getString("screen.rules.crashs", cellWidth)), gbc);
			gbc.gridx = 1;
			gbc.gridwidth = 3;
			this.add(this.crashingAllowedCB, gbc);
			gbc.gridy++;
			gbc.gridx = 0;
			gbc.gridwidth = 1;
			this.add(new JLabel(Language.getString("screen.rules.cps", cellWidth)), gbc);
			gbc.gridx = 1;
			gbc.gridwidth = 3;
			this.add(this.checkpointsActivatedCB, gbc);
			gbc.gridy++;
			gbc.gridx = 0;
			gbc.gridwidth = 1;
			this.add(new JLabel(Language.getString("screen.rules.direction", cellWidth)), gbc);
			gbc.gridx = 1;
			gbc.gridwidth = 3;
			this.add(this.directionCB, gbc);
		}
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		this.add(new JLabel(Language.getString("screen.rules.creatorgiveup", cellWidth)), gbc);
		gbc.gridx = 1;
		gbc.gridwidth = 3;
		this.add(this.creatorGiveUpCB, gbc);
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 4;
		this.add(new JLabel(Language.getString("screen.rules.creatorgiveup.description", totalWidth)), gbc);
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		this.add(new JLabel(Language.getString("screen.rules.ignoreinvitable", cellWidth)), gbc);
		gbc.gridx = 1;
		gbc.gridwidth = 3;
		this.add(this.ignoreInvitableCB, gbc);
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 4;
		this.add(new JLabel(Language.getString("screen.rules.ignoreinvitable.description", totalWidth)), gbc);
	}
}
