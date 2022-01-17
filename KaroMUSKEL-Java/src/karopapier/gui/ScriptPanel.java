package karopapier.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import karopapier.application.GameCreator;
import karopapier.model.GameSeries;
import karopapier.model.Karopapier;
import karopapier.model.Map;
import karopapier.model.Player;

public class ScriptPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	
	private Karopapier karopapier;
	
	// TITLE ZEILE - START
	private JLabel titleL,currentPlayerL;
	private JButton createB;
	// TITLE ZEILE - ENDE
	
	// SPIEL NAME, ANZAHL UND EINSTELLUNGEN - START		
	private JLabel gameL, gameNameL, nogL, minDigitsL, countL, startCountL, generatedL, generatedLL, zzzL, checkL, tcL, dirL;
	private JTextField gameNameTF, nogTF, minDigitsTF, countTF, startCountTF, generatedTF, generatedLTF, zzzTF;
	private JComboBox checkCB, tcCB, dirCB;	
	// SPIEL NAME, ANZAHL UND EINSTELLUNGEN - ENDE		
	
	// KARTEN AUSWAHL - START
	private JLabel mapMainL, mapL, randomL, mapTypeL, randomDescL;
	private JComboBox mapCB;
	private MapPreviewPanel mapPreviewP;
	private JComboBox mapTypeCB, randomCB;
	// KARTEN AUSWAHL - ENDE	
	
	// SPIELER ADDEN ZUFALL - START
	private JLabel addPlMainL, addPlL, minPlL;
	private JComboBox maxPlCB;
	private JTextField minPlTF, maxPlTF;
	// SPIELER ADDEN ZUFALL - ENDE
	
	// SPIELER AUSWAHL - START
	private JLabel allPlayersL, katAPlayersL, katBPlayersL, katCPlayersL;
	private JLabel allPlayersLNum, katAPlayersLNum, katBPlayersLNum, katCPlayersLNum;
	private JList allPlayersLI, katAPlayersLI, katBPlayersLI, katCPlayersLI;
	private JScrollPane allPlayersLISP, katAPlayersLISP, katBPlayersLISP, katCPlayersLISP;
	private JTextArea allPlayersDesc, katAPlayersDesc, katBPlayersDesc, katCPlayersDesc;
	private JScrollPane allPlayersDescSP, katAPlayersDescSP, katBPlayersDescSP, katCPlayersDescSP;
	private JButton playerAddA, playerAddB, playerAddC;
	private JButton playerRemA, playerRemB, playerRemC;
	
	private TreeMap<String, Player> allPlayers = null, katAPlayers = null, katBPlayers = null, katCPlayers = null;
	
	private int playerLY = 345;
	private int playerLIY = playerLY + 35;
	private int playerBW = 100;
	private int playerW = 210;
	private int playerWNum = 50;
	private int playerWL = playerW-playerWNum;
	private int playerH = 150;
	private int playerDescH = 100;
	private int playerYBA = playerLIY + 1*(playerH-75)/4 + 0*25;
	private int playerYBB = playerLIY + 2*(playerH-75)/4 + 1*25;
	private int playerYBC = playerLIY + 3*(playerH-75)/4 + 2*25;
	private int playerX0 = 10;
	private int playerX1 = playerX0 + playerW + 10;
	private int playerXA = playerX1 + playerBW + 10;
	private int playerXB = playerXA + playerW + 10;
	private int playerXC = playerXB + playerW + 10;
	private int playerRemBY = playerLIY + playerH + 10;
	private int playerDescY = playerRemBY + 35;
	
	private String descAll = "Diese Liste enthält alle verfügbaren Spieler nach Name sortiert. Jeder der Spieler kann," +
								" soweit die Id bekannt ist einem Rennen hinzugefügt werden!\n" +
								"Die Daten der Spieler werden wie folgt ermittelt:\n" +
								"Zunächst werden alle Spieler der Login-Seite geladen. Anschließend wird diese List mit den Informationen" +
								" der Spiel-Erstellen-Seite ergänzt. Sollte zusätzlich die gespeicherte user.php im Verzeichnis vorliegen" +
								" werden die Daten dieser Seite ebenfalls hinzugefügt. Der letzte Punkt funktioniert allerdings nur bei" +
								" gespeicherter Seite, da diese Seite nicht öffentlich, und somit nicht ohne Login aufrufbar ist.";
	private String descKatA = "Spieler der Kategorie A werden in jedem Fall dem Spiel hinzugefügt!\nDie Einladbarkeit wird dabei ignoriert!\n" +
								"Die selbst oder vom Zufallsgenerator ausgewählt Karte muss daher mindestens so viele Startplätze haben," +
								" wie sich Spieler in dieser Liste befinden!";
	private String descKatB = "Spieler der Kategorie B werden sofern noch nicht die maximale Spielerzahl für ein Rennen erreicht ist" +
								", zufällig, jedoch prorisiert dem Spiel hinzugefügt, bis die maximale Spielerzahl (Dies" +
								" ist entweder eine beschränkte Zahl durch den Benutzer oder das maximum der aktuellen Karte.)" +
								" erreicht ist.\n" + 
								"Die Einladbarkeit der Spieler wird hier beachtet!";
	private String descKatC = "Spieler der Kategorie C werden sofern noch nicht die maximale Spielerzahl für ein Rennen erreicht ist" +
								", zufällig, jedoch nachrangig gegenüber Kategorie B dem Spiel hinzugefügt, bis die maximale Spielerzahl (Dies" +
								" ist entweder eine beschränkte Zahl durch den Benutzer oder das maximum der aktuellen Karte.)" +
								" erreicht ist. Das bedeutet, es werden erst Spieler dieser Kategorie ausgewählt, sobald alle Spieler der" +
								" Kategorie B ausgewählt wurden.\n" +
								"Die Einladbarkeit der Spieler wird hier beachtet!";
	// SPIELER AUSWAHL - ENDE

	public ScriptPanel(Karopapier karopapier) {
		super();
		this.karopapier = karopapier;
		
		allPlayers = new TreeMap<String, Player>();
		for(String name: karopapier.getPlayers().keySet())
			allPlayers.put(name, karopapier.getPlayers().get(name));
		katAPlayers = new TreeMap<String, Player>();
		katBPlayers = new TreeMap<String, Player>();
		katCPlayers = new TreeMap<String, Player>();
		
		Border borderForAll = BorderFactory.createLineBorder(Color.black);		

		this.setLayout(null);
		
		// TITLE ZEILE - START
		titleL = new JLabel("Scripting-Einstellungen");
		titleL.setBounds(10,10,240,25);		
		currentPlayerL = new JLabel("Spiele werden erstellt für: \"" + karopapier.getCurrentUser() + "\"");
		currentPlayerL.setBounds(240, 10, 500, 25);		
		createB = new JButton("Spiele erstellen!");
		createB.setBounds(750, 10, 240, 25);
		createB.setActionCommand("create");
		createB.addActionListener(new OneForAllActionListener(karopapier, this, null));
		// TITLE ZEILE - ENDE
		
		// SPIEL NAME, ANZAHL UND EINSTELLUNGEN - START		
		gameL = new JLabel("Spiel-Einstellungen");
		gameL.setBounds(10, 65, 140, 25);
		gameNameL = new JLabel("Spielname");
		gameNameL.setBounds(160, 65, 70, 25);
		gameNameTF = new JTextField("Mehr Spiele für " + karopapier.getCurrentUser() + " %I");
		gameNameTF.setBounds(240, 65, 580, 25);		
		nogL = new JLabel("Anzahl");
		nogL.setBounds(830, 65, 100, 25);
		nogTF = new JTextField("10");
		nogTF.setBounds(940, 65, 50, 25);
		
		countL = new JLabel("Ersetze folgendes Pattern durch Nummer");
		countL.setBounds(240, 100, 290, 25);
		countTF = new JTextField("%I");
		countTF.setBounds(540, 100, 50, 25);
		startCountL = new JLabel("Beginne zu zählen mit");
		startCountL.setBounds(610, 100, 150, 25);
		startCountTF = new JTextField("1");
		startCountTF.setBounds(770, 100, 50, 25);
		minDigitsL = new JLabel("Minimale Ziffern");
		minDigitsL.setBounds(830, 100, 100, 25);
		minDigitsTF = new JTextField("2");
		minDigitsTF.setBounds(940, 100, 50, 25);

		generatedL = new JLabel("Ersetze folgendes Pattern durch Zufallszeichen");
		generatedL.setBounds(240, 135, 290, 25);
		generatedTF = new JTextField("%RAND");
		generatedTF.setBounds(540, 135, 50, 25);
		generatedLL = new JLabel("Anzahl der Zeichen");
		generatedLL.setBounds(610, 135, 150, 25);
		generatedLTF = new JTextField("50");
		generatedLTF.setBounds(770, 135, 50, 25);
		
		tcL = new JLabel("Taktische Crashen");
		tcL.setBounds(240, 170, 120, 25);
		tcCB = new JComboBox(GameCreator.tacticalCrashes);
		tcCB.setBounds(370, 170, 220, 25);
		checkL = new JLabel("Checkpoints aktiviert?");
		checkL.setBounds(610, 170, 150, 25);
		checkCB = new JComboBox(GameCreator.checkpointsOption);
		checkCB.setBounds(770, 170, 50, 25);
		
		dirL = new JLabel("Startrichtung");
		dirL.setBounds(240, 205, 120, 25);
		dirCB = new JComboBox(GameCreator.direction);
		dirCB.setBounds(370, 205, 220, 25);
		zzzL = new JLabel("ZZZ");
		zzzL.setBounds(610, 205, 150, 25);
		zzzTF = new JTextField("2");
		zzzTF.setBounds(770, 205, 50, 25);
		// SPIEL NAME, ANZAHL UND EINSTELLUNGEN - ENDE		
		
		// KARTEN AUSWAHL - START	
		mapMainL = new JLabel("Karten-Einstellungen");
		mapMainL.setBounds(10, 240, 140, 25);
		mapL = new JLabel("Karte");
		mapL.setBounds(160, 240, 70, 25);
		mapCB = new JComboBox(treeMapToArrayMap(karopapier.getMaps()));
		mapCB.setBounds(240, 240, 580, 25);		
		mapCB.setActionCommand("mapChanged");
		mapCB.addActionListener(new OneForAllActionListener(karopapier, this, null));
		mapPreviewP = new MapPreviewPanel();
		mapPreviewP.setBounds(830, 240, 160, 95);
		mapPreviewP.displayMap(null);
		randomL = new JLabel("Zufallskarte");
		randomL.setBounds(160, 275, 70, 25);
		randomCB = new JComboBox(new String[]{"ja", "nein"});
		randomCB.setBounds(240, 275, 50, 25);
		randomCB.setActionCommand("randomChanged");
		randomCB.addActionListener(new OneForAllActionListener(karopapier, this, null));
		mapTypeL = new JLabel("Kartentyp");
		mapTypeL.setBounds(320, 275, 80, 25);
		mapTypeCB = new JComboBox(GameCreator.mapTypes);
		mapTypeCB.setBounds(410, 275, 70, 25);
		randomDescL = new JLabel("Es werden nur Karten mit genügend Spielern ausgewählt.");
		randomDescL.setBounds(490, 275, 430, 25);
		// KARTEN AUSWAHL - ENDE	
		
		// SPIELER ADDEN ZUFALL - START
		addPlMainL = new JLabel("Spieler-Einstellungen");
		addPlMainL.setBounds(10, 310, 140, 25);
		addPlL = new JLabel("Spieler?");
		addPlL.setBounds(160, 310, 70, 25);
		maxPlCB = new JComboBox(GameCreator.addPLayersOption);
		maxPlCB.setBounds(240, 310, 290, 25);
		maxPlCB.setActionCommand("maxPlChanged");
		maxPlCB.addActionListener(new OneForAllActionListener(karopapier, this, null));
		maxPlTF = new JTextField("0");
		maxPlTF.setBounds(540, 310, 50, 25);
		minPlL = new JLabel("Minimale Spielerzahl");
		minPlL.setBounds(610, 310, 150, 25);
		minPlTF = new JTextField("0");
		minPlTF.setBounds(770, 310, 50, 25);
		// SPIELER ADDEN ZUFALL - ENDE
		
		// SPIELER AUSWAHL - START
		allPlayersL = new JLabel("Verfügbare Spieler");
		allPlayersL.setBounds(playerX0, playerLY, playerWL, 25);
		allPlayersLNum = new JLabel();
		allPlayersLNum.setBounds(playerX0+playerWL, playerLY, playerWNum, 25);
		allPlayersLNum.setHorizontalAlignment(SwingConstants.RIGHT);
		allPlayersLI = new JList();
		allPlayersLI.setBorder(borderForAll);
		allPlayersLISP = new JScrollPane(allPlayersLI, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		allPlayersLISP.setBounds(playerX0, playerLIY, playerW, playerH);
		allPlayersDesc = new JTextArea(descAll);
		allPlayersDesc.setEditable(false);
		allPlayersDesc.setBorder(borderForAll);
		allPlayersDesc.setLineWrap(true);
		allPlayersDesc.setWrapStyleWord(true);
		allPlayersDescSP = new JScrollPane(allPlayersDesc, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		allPlayersDescSP.setBounds(playerX0, playerDescY, playerW, playerDescH);
		
		playerAddA = new JButton("Kat. A -->");
		playerAddA.setBounds(playerX1, playerYBA, playerBW, 25);
		playerAddA.setActionCommand("addA");
		playerAddA.addActionListener(new OneForAllActionListener(karopapier, this, null));
		playerAddB = new JButton("Kat. B -->");
		playerAddB.setBounds(playerX1, playerYBB, playerBW, 25);
		playerAddB.setActionCommand("addB");
		playerAddB.addActionListener(new OneForAllActionListener(karopapier, this, null));
		playerAddC = new JButton("Kat. C -->");
		playerAddC.setBounds(playerX1, playerYBC, playerBW, 25);
		playerAddC.setActionCommand("addC");
		playerAddC.addActionListener(new OneForAllActionListener(karopapier, this, null));
		
		katAPlayersL = new JLabel("Spielerauswahl Kat.A");
		katAPlayersL.setBounds(playerXA, playerLY, playerWL, 25);
		katAPlayersLNum = new JLabel();
		katAPlayersLNum.setBounds(playerXA+playerWL, playerLY, playerWNum, 25);
		katAPlayersLNum.setHorizontalAlignment(SwingConstants.RIGHT);
		katAPlayersLI = new JList();
		katAPlayersLI.setBorder(borderForAll);
		katAPlayersLISP = new JScrollPane(katAPlayersLI, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		katAPlayersLISP.setBounds(playerXA, playerLIY, playerW, playerH);
		playerRemA = new JButton("Entferne Auswahl");
		playerRemA.setBounds(playerXA, playerRemBY, playerW, 25);
		playerRemA.setActionCommand("remA");
		playerRemA.addActionListener(new OneForAllActionListener(karopapier, this, null));
		katAPlayersDesc = new JTextArea(descKatA);
		katAPlayersDesc.setEditable(false);
		katAPlayersDesc.setBorder(borderForAll);
		katAPlayersDesc.setLineWrap(true);
		katAPlayersDesc.setWrapStyleWord(true);
		katAPlayersDescSP = new JScrollPane(katAPlayersDesc, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		katAPlayersDescSP.setBounds(playerXA, playerDescY, playerW, playerDescH);
		
		katBPlayersL = new JLabel("Spielerauswahl Kat.B");
		katBPlayersL.setBounds(playerXB, playerLY, playerWL, 25);
		katBPlayersLNum = new JLabel();
		katBPlayersLNum.setBounds(playerXB+playerWL, playerLY, playerWNum, 25);
		katBPlayersLNum.setHorizontalAlignment(SwingConstants.RIGHT);
		katBPlayersLI = new JList();
		katBPlayersLI.setBorder(borderForAll);
		katBPlayersLISP = new JScrollPane(katBPlayersLI, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		katBPlayersLISP.setBounds(playerXB, playerLIY, playerW, playerH);
		playerRemB = new JButton("Entferne Auswahl");
		playerRemB.setBounds(playerXB, playerRemBY, playerW, 25);
		playerRemB.setActionCommand("remB");
		playerRemB.addActionListener(new OneForAllActionListener(karopapier, this, null));
		katBPlayersDesc = new JTextArea(descKatB);
		katBPlayersDesc.setEditable(false);
		katBPlayersDesc.setBorder(borderForAll);
		katBPlayersDesc.setLineWrap(true);
		katBPlayersDesc.setWrapStyleWord(true);
		katBPlayersDescSP = new JScrollPane(katBPlayersDesc, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		katBPlayersDescSP.setBounds(playerXB, playerDescY, playerW, playerDescH);
		
		katCPlayersL = new JLabel("Spielerauswahl Kat.C");
		katCPlayersL.setBounds(playerXC, playerLY, playerWL, 25);
		katCPlayersLNum = new JLabel();
		katCPlayersLNum.setBounds(playerXC+playerWL, playerLY, playerWNum, 25);
		katCPlayersLNum.setHorizontalAlignment(SwingConstants.RIGHT);
		katCPlayersLI = new JList();
		katCPlayersLI.setBorder(borderForAll);
		katCPlayersLISP = new JScrollPane(katCPlayersLI, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		katCPlayersLISP.setBounds(playerXC, playerLIY, playerW, playerH);
		playerRemC = new JButton("Entferne Auswahl");
		playerRemC.setBounds(playerXC, playerRemBY, playerW, 25);
		playerRemC.setActionCommand("remC");
		playerRemC.addActionListener(new OneForAllActionListener(karopapier, this, null));
		katCPlayersDesc = new JTextArea(descKatC);
		katCPlayersDesc.setEditable(false);
		katCPlayersDesc.setBorder(borderForAll);
		katCPlayersDesc.setLineWrap(true);
		katCPlayersDesc.setWrapStyleWord(true);
		katCPlayersDescSP = new JScrollPane(katCPlayersDesc, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		katCPlayersDescSP.setBounds(playerXC, playerDescY, playerW, playerDescH);
		// SPIELER AUSWAHL - ENDE
	
		this.add(titleL, null);
		this.add(currentPlayerL, null);
		this.add(createB, null);
		this.add(allPlayersL, null);
		this.add(katAPlayersL, null);
		this.add(katBPlayersL, null);
		this.add(katCPlayersL, null);
		this.add(allPlayersLNum, null);
		this.add(katAPlayersLNum, null);
		this.add(katBPlayersLNum, null);
		this.add(katCPlayersLNum, null);
		this.add(allPlayersLISP, null);
		this.add(katAPlayersLISP, null);
		this.add(katBPlayersLISP, null);
		this.add(katCPlayersLISP, null);
		this.add(allPlayersDescSP, null);
		this.add(katAPlayersDescSP, null);
		this.add(katBPlayersDescSP, null);
		this.add(katCPlayersDescSP, null);
		this.add(playerAddA, null);
		this.add(playerAddB, null);
		this.add(playerAddC, null);
		this.add(playerRemA, null);
		this.add(playerRemB, null);
		this.add(playerRemC, null);		
		this.add(gameL, null);
		this.add(gameNameL, null);
		this.add(nogL, null);
		this.add(minDigitsL, null);
		this.add(countL, null);
		this.add(startCountL, null);
		this.add(generatedL, null);
		this.add(generatedLL, null);
		this.add(zzzL, null);
		this.add(checkL, null);
		this.add(tcL, null);
		this.add(dirL, null);
		this.add(gameNameTF, null);
		this.add(nogTF, null);
		this.add(minDigitsTF, null);
		this.add(countTF, null);
		this.add(startCountTF, null);
		this.add(generatedTF, null);
		this.add(generatedLTF, null);
		this.add(zzzTF, null);
		this.add(checkCB, null);
		this.add(tcCB, null);
		this.add(dirCB, null);
		this.add(mapMainL, null);
		this.add(mapL, null);
		this.add(randomL, null);
		this.add(randomDescL, null);
		this.add(mapTypeL, null);
		this.add(mapCB, null);
		this.add(mapPreviewP, null);
		this.add(randomCB, null);
		this.add(mapTypeCB, null);
		this.add(addPlMainL, null);
		this.add(addPlL, null);
		this.add(maxPlCB, null);
		this.add(maxPlTF, null);
		this.add(minPlL, null);
		this.add(minPlTF, null);
		
		updatePlayerDisplayment();
		updateRandomMapStatus();
		updateRandomPlayerStatus();
	}
	
	public void updatePlayerDisplayment(String actionCommand) {
		if(actionCommand.startsWith("add")) {
			Object[] ps = allPlayersLI.getSelectedValues();
			for(Object o: ps) {
				Player p = (Player)o;
				allPlayers.remove(p.getName().toLowerCase());
				if(actionCommand.endsWith("addA")) {
					katAPlayers.put(p.getName().toLowerCase(), p);
				} else if(actionCommand.endsWith("addB")) {
					katBPlayers.put(p.getName().toLowerCase(), p);
				} else if(actionCommand.endsWith("addC")) {
					katCPlayers.put(p.getName().toLowerCase(), p);
				} else {
					
				}
			}
		} else if(actionCommand.startsWith("rem")) {
			Object[] ps = null;
			if(actionCommand.endsWith("remA")) {
				ps = katAPlayersLI.getSelectedValues();
				for(Object o: ps) {
					Player p = (Player)o;
					katAPlayers.remove(p.getName().toLowerCase());	
					allPlayers.put(p.getName().toLowerCase(), p);
				}
			} else if(actionCommand.endsWith("remB")) {
				ps = katBPlayersLI.getSelectedValues();
				for(Object o: ps) {
					Player p = (Player)o;
					katBPlayers.remove(p.getName().toLowerCase());	
					allPlayers.put(p.getName().toLowerCase(), p);			
				}
			} else if(actionCommand.endsWith("remC")) {
				ps = katCPlayersLI.getSelectedValues();
				for(Object o: ps) {
					Player p = (Player)o;
					katCPlayers.remove(p.getName().toLowerCase());	
					allPlayers.put(p.getName().toLowerCase(), p);			
				}
			} else {
				
			}
		} else {
			
		}
		updatePlayerDisplayment();
	}
	
	private void updatePlayerDisplayment() {
		allPlayersLI.setListData(treeMapToArrayPlayer(allPlayers));
		katAPlayersLI.setListData(treeMapToArrayPlayer(katAPlayers));
		katBPlayersLI.setListData(treeMapToArrayPlayer(katBPlayers));
		katCPlayersLI.setListData(treeMapToArrayPlayer(katCPlayers));
		
		allPlayersLNum.setText("" + allPlayers.size());
		katAPlayersLNum.setText("" + katAPlayers.size());
		katBPlayersLNum.setText("" + katBPlayers.size());
		katCPlayersLNum.setText("" + katCPlayers.size());
		
		repaint();
	}	
	
	private Player[] treeMapToArrayPlayer(TreeMap<String, Player> players) {
		Player[] playersA = new Player[players.size()];
		
		int i = 0;
		for(String name: players.keySet()) {
			playersA[i++] = players.get(name);
		}
		
		return playersA;
	}
	
	private Map[] treeMapToArrayMap(TreeMap<Integer, Map> maps) {
		Map[] mapsA = new Map[maps.size()];
		
		int i = 0;
		for(Integer name: maps.keySet()) {
			mapsA[i++] = maps.get(name);
		}
		
		return mapsA;
	}
	
	public void createGameSeries() {
		ArrayList<String> error = checkTextFields();
		if(error.size() > 0) {
			StringBuilder errors = new StringBuilder();
			for(int i = 0; i < error.size(); i++) {
				errors.append("\"" + error.get(i) + "\"");
				if(i < error.size() -1)
					errors.append(", ");
			}
			System.out.println(errors);
			JOptionPane.showMessageDialog(this,
					new Object[]{"Folgende Eingaben sind keine nicht negativen Zahlen , obwohl sie es sein müssten:", errors.toString()},
					"Falsches Zahlenformat!",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String confirmTitle = "Bitte Daten bestätigen";
		int noG = Integer.parseInt(nogTF.getText());
		int maxLoadThreads = 80;
		// Popup für Bestätigung und maxLoadThreads
		JLabel label = new JLabel("Es sollen " + noG + " Spiele erstellt werden!!! Bitte bestätigen damit es losgehen kann...");
		JLabel notL = new JLabel("Bitte wähle die maximal Anzahl paralleler LadeThreads aus. (Default ist 80)");
		JTextField notTF = new JTextField("" + maxLoadThreads);
		while(true) {
			int ret = JOptionPane.showConfirmDialog(this, new Object[]{label, notL, notTF}, confirmTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(ret == JOptionPane.CANCEL_OPTION || ret == JOptionPane.CLOSED_OPTION) {
				System.out.println("Spielerstellung abgebrochen");
				return;
			}
			try {
				maxLoadThreads = Integer.parseInt(notTF.getText());
				if(maxLoadThreads <= 0) 
					throw new NumberFormatException("Kleiner 0");
				break;
			} catch(NumberFormatException e) {
				JOptionPane.showMessageDialog(this,
						"Die Threadanzahl muss eine positive Zahl sein!",
						"Falsches Zahlenformat!",
						JOptionPane.ERROR_MESSAGE);
				System.out.println("Spielerstellung abgebrochen");
			}
		}
		
		GameSeries gs = new GameSeries();
		gs.setKaropapier(this.karopapier);
		// felder füllen - START
		gs.setName(gameNameTF.getText());
		gs.setNoG(noG);
		gs.setNumberPattern(countTF.getText());
		gs.setCountStart(startCountTF.getText().equals("") ? 0 : Integer.parseInt(startCountTF.getText()));
		gs.setMinDigits(minDigitsTF.getText().equals("") ? 0 : Integer.parseInt(minDigitsTF.getText()));
		gs.setRandomPattern(generatedTF.getText());
		gs.setRandomChars(generatedLTF.getText().equals("") ? 0 : Integer.parseInt(generatedLTF.getText()));
		String tcS = (String)tcCB.getSelectedItem();
		int tc = 2;
		for(int i = 0; i < GameCreator.tacticalCrashes.length; i++) {
			if(tcS.equals(GameCreator.tacticalCrashes[i])) {
				tc = GameCreator.tacticalCrashesI[i];
				break;
			}
		}
		gs.setCrashs(tc);
			String dirS = (String)dirCB.getSelectedItem();
			int dir = 2;
			for(int i = 0; i < GameCreator.direction.length; i++) {
				if(dirS.equals(GameCreator.direction[i])) {
					dir = GameCreator.directionI[i];
					break;
				}
			}
		gs.setDirection(dir);
		gs.setChecks(((String)checkCB.getSelectedItem()).equalsIgnoreCase("ja"));
		gs.setZzz(zzzTF.getText().equals("") ? 0 : Integer.parseInt(zzzTF.getText()));
		gs.setMap((Map)mapCB.getSelectedItem());
		gs.setRandomMap(((String)randomCB.getSelectedItem()).equalsIgnoreCase("ja"));
		gs.setMapType((String)mapTypeCB.getSelectedItem());
		gs.setAddPlayer((String)maxPlCB.getSelectedItem());
		gs.setMaxPlayers(maxPlTF.getText().equals("") ? 0 : Integer.parseInt(maxPlTF.getText()));
		gs.setMinPlayers(minPlTF.getText().equals("") ? 0 : Integer.parseInt(minPlTF.getText()));
		gs.setKatA(this.katAPlayers);
		gs.setKatB(this.katBPlayers);
		gs.setKatC(this.katCPlayers);
		// felder füllen - ENDE
		
		if(!gs.isRandomMap() && gs.getKatA().size()+1>gs.getMap().getMaxPlayers()) {
			JOptionPane.showMessageDialog(this,
					"Die ausgewählte Strecke hat nicht genügend Platz für alle Kategorie A Spieler.",
					"Hast du berücksichtigt, dass der Spielersteller noch hinzu kommt?",
					JOptionPane.ERROR_MESSAGE);
			System.out.println("Spielerstellung abgebrochen");
			return;
		}
		// für Fortschritt...
		ProgressFrame progressFrame = new ProgressFrame();	
		gs.setProgressFrame(progressFrame);	
		
		GameCreator gc = new GameCreator();		
		this.karopapier = gc.createGameSeries(gs, maxLoadThreads);
		
		// veränderte Einladbarkeit...
		allPlayers = new TreeMap<String, Player>();
		for(String name: karopapier.getPlayers().keySet())
			allPlayers.put(name, karopapier.getPlayers().get(name));
		katAPlayers = new TreeMap<String, Player>();
		katBPlayers = new TreeMap<String, Player>();
		katCPlayers = new TreeMap<String, Player>();
		updatePlayerDisplayment();
	}
	
	public ArrayList<String> checkTextFields() {
		ArrayList<String> ret = new ArrayList<String>();
		JTextField[] tfs = {nogTF, minDigitsTF, startCountTF, generatedLTF, zzzTF, minPlTF, maxPlTF};
		for(JTextField tf: tfs) {
			if(tf.getText().equals(""))
				continue;
			try {
				if(Integer.parseInt(tf.getText()) < 0) 
					throw new NumberFormatException("Kleiner 0");
			} catch(NumberFormatException e) {
				ret.add(tf.getText());
			}
		}
		return ret;
	}
	
	public void updateMapDisplayment() {
		if(randomCB.getSelectedItem().equals("ja"))
			mapPreviewP.displayMap(null);
		else
			mapPreviewP.displayMap((Map)mapCB.getSelectedItem());
	}
	
	public void updateRandomMapStatus() {
		boolean b = randomCB.getSelectedItem().equals("ja");
		mapCB.setEnabled(!b);
		mapTypeCB.setEnabled(b);
		mapTypeL.setEnabled(b);
		randomDescL.setEnabled(b);
	}

	public void updateRandomPlayerStatus() {
		boolean b = maxPlCB.getSelectedItem().equals(GameCreator.addPLayersOption[2]);
		maxPlTF.setEnabled(b);
		b = b || maxPlCB.getSelectedItem().equals(GameCreator.addPLayersOption[1]);
		minPlL.setEnabled(b);
		minPlTF.setEnabled(b);
		updateMapDisplayment();
	}
}
