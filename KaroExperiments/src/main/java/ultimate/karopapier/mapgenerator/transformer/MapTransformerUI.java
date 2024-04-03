package ultimate.karopapier.mapgenerator.transformer;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ultimate.karoapi4j.KaroAPI;
import ultimate.karoapi4j.model.official.Map;
import ultimate.karoapi4j.utils.PropertiesUtil;
import ultimate.karopapier.mapgenerator.MapGeneratorUtil;

public class MapTransformerUI extends JFrame implements DocumentListener, ChangeListener, ActionListener, ComponentListener, ItemListener
{
	private static final long	serialVersionUID	= 1L;

	private static final int	GAP					= 10;
	private static final int	ROW					= 20;

	private boolean				comparisonMode;

	private JPanel				controlPanel;
	private JPanel				canvasPanel;

	private JLabel				drawSizeLabel;
	private JSlider				drawSizeSlider;
	private JTextField			drawSizeTF;

	private JLabel				scaleXLabel;
	private JSlider				scaleXSlider;
	private JTextField			scaleXTF;

	private JLabel				scaleYLabel;
	private JSlider				scaleYSlider;
	private JTextField			scaleYTF;

	private JLabel				scaleFlagsLabel;
	private JCheckBox			scaleSmartCB;
	private JCheckBox			scaleSyncCB;

	private JLabel				mirrorLabel;
	private JCheckBox			mirrorXCB;
	private JCheckBox			mirrorYCB;

	private JLabel				rotationLabel;
	private JSlider				rotationSlider;
	private JTextField			rotationTF;

	private JLabel				modLabel;
	private JCheckBox			modSwapSFCB;
	private JCheckBox			modTrimCB;

	private JLabel				matrixLabel;
	private JTextField			m00TF;
	private JTextField			m01TF;
	private JTextField			m10TF;
	private JTextField			m11TF;

	private JLabel				mapLabel;
	private JComboBox<Map>		mapCB;
	private JTextArea			codeArea;

	private Canvas				canvas1;
	private Canvas				canvas2;

	public MapTransformerUI(List<Map> maps, int canvasOrientation)
	{
		this.comparisonMode = (canvasOrientation >= 0);

		this.getContentPane().setLayout(new BorderLayout());

		controlPanel = new JPanel();
		controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
		controlPanel.setLayout(new GridBagLayout());
		this.getContentPane().add(controlPanel, BorderLayout.WEST);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.ipadx = GAP;
		gbc.ipady = GAP;
		gbc.fill = GridBagConstraints.BOTH;

		// draw size
		gbc.gridy = 0;
		drawSizeLabel = new JLabel("Draw Size:");
		controlPanel.add(drawSizeLabel, gbc);
		drawSizeSlider = new JSlider(1, 20, 9);
		drawSizeSlider.addChangeListener(this);
		gbc.gridwidth = 2;
		controlPanel.add(drawSizeSlider, gbc);
		drawSizeTF = new JTextField();
		drawSizeTF.setEditable(false);
		gbc.gridwidth = 1;
		controlPanel.add(drawSizeTF, gbc);

		// scaling X
		gbc.gridy++;
		scaleXLabel = new JLabel("Scale X:");
		controlPanel.add(scaleXLabel, gbc);
		scaleXSlider = new JSlider(1, 100, 10);
		scaleXSlider.addChangeListener(this);
		gbc.gridwidth = 2;
		controlPanel.add(scaleXSlider, gbc);
		scaleXTF = new JTextField();
		scaleXTF.setEditable(false);
		gbc.gridwidth = 1;
		controlPanel.add(scaleXTF, gbc);

		// scaling Y
		gbc.gridy++;
		scaleYLabel = new JLabel("Scale Y:");
		controlPanel.add(scaleYLabel, gbc);
		scaleYSlider = new JSlider(1, 100, 10);
		scaleYSlider.addChangeListener(this);
		gbc.gridwidth = 2;
		controlPanel.add(scaleYSlider, gbc);
		scaleYTF = new JTextField();
		scaleYTF.setEditable(false);
		gbc.gridwidth = 1;
		controlPanel.add(scaleYTF, gbc);

		// scale flags
		gbc.gridy++;
		scaleFlagsLabel = new JLabel("");
		controlPanel.add(scaleFlagsLabel, gbc);
		scaleSmartCB = new JCheckBox("Smart Scale");
		scaleSmartCB.addItemListener(this);
		controlPanel.add(scaleSmartCB, gbc);
		scaleSyncCB = new JCheckBox("🔗 Sync Axes");
		scaleSyncCB.addItemListener(this);
		controlPanel.add(scaleSyncCB, gbc);

		// mirroring
		gbc.gridy++;
		mirrorLabel = new JLabel("Mirror:");
		controlPanel.add(mirrorLabel, gbc);
		mirrorXCB = new JCheckBox("X");
		mirrorXCB.addItemListener(this);
		controlPanel.add(mirrorXCB, gbc);
		mirrorYCB = new JCheckBox("Y");
		mirrorYCB.addItemListener(this);
		controlPanel.add(mirrorYCB, gbc);

		// rotation
		gbc.gridy++;
		rotationLabel = new JLabel("Rotation (CCW):");
		controlPanel.add(rotationLabel, gbc);
		rotationSlider = new JSlider(0, 360, 0);
		rotationSlider.addChangeListener(this);
		gbc.gridwidth = 2;
		controlPanel.add(rotationSlider, gbc);
		rotationTF = new JTextField(rotationSlider.getValue() + " deg");
		rotationTF.setEditable(false);
		gbc.gridwidth = 1;
		controlPanel.add(rotationTF, gbc);

		// modification
		gbc.gridy++;
		modLabel = new JLabel("Modification:");
		controlPanel.add(modLabel, gbc);
		modSwapSFCB = new JCheckBox("Swap S <-> F");
		modSwapSFCB.addItemListener(this);
		controlPanel.add(modSwapSFCB, gbc);
		modTrimCB = new JCheckBox("Trim");
		modTrimCB.addItemListener(this);
		controlPanel.add(modTrimCB, gbc);

		// matrix
		gbc.gridy++;
		matrixLabel = new JLabel("Matrix:");
		controlPanel.add(matrixLabel, gbc);
		JPanel matrix = new JPanel(new GridLayout(2, 2));
		m00TF = new JTextField();
		m00TF.setEditable(false);
		m00TF.setHorizontalAlignment(JTextField.RIGHT);
		matrix.add(m00TF);
		m01TF = new JTextField();
		m01TF.setEditable(false);
		m01TF.setHorizontalAlignment(JTextField.RIGHT);
		matrix.add(m01TF);
		m10TF = new JTextField();
		m10TF.setEditable(false);
		m10TF.setHorizontalAlignment(JTextField.RIGHT);
		matrix.add(m10TF);
		m11TF = new JTextField();
		m11TF.setEditable(false);
		m11TF.setHorizontalAlignment(JTextField.RIGHT);
		matrix.add(m11TF);
		gbc.gridwidth = 2;
		controlPanel.add(matrix, gbc);
		gbc.gridwidth = 1;

		// map & code
		gbc.gridy++;
		mapLabel = new JLabel("Map:");
		controlPanel.add(mapLabel, gbc);
		DefaultComboBoxModel<Map> model = new DefaultComboBoxModel<Map>(maps.toArray(new Map[0]));
		mapCB = new JComboBox<>(model);
		mapCB.addActionListener(this);
		mapCB.setPreferredSize(new Dimension((int) controlPanel.getSize().getWidth() / 2, ROW));
		gbc.gridwidth = 3;
		controlPanel.add(mapCB, gbc);

		// code
		gbc.gridy++;
		codeArea = new JTextArea("");
		codeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
		codeArea.getDocument().addDocumentListener(this);
		JScrollPane scroll = new JScrollPane(codeArea);
		scroll.setPreferredSize(new Dimension((int) controlPanel.getSize().getWidth() / 2, ROW));
		gbc.gridwidth = 4;
		gbc.weighty = 20;
		controlPanel.add(scroll, gbc);

		// canvases
		canvasPanel = new JPanel();
		canvasPanel.setBorder(BorderFactory.createTitledBorder("Map View"));
		canvasPanel.setLayout(new BoxLayout(canvasPanel, (canvasOrientation < 0 ? 0 : canvasOrientation)));
		this.getContentPane().add(canvasPanel);

		canvas1 = new Canvas();
		scroll = new JScrollPane(canvas1);
		canvasPanel.add(scroll);

		if(canvasOrientation >= 0)
		{
			canvas2 = new Canvas();
			scroll = new JScrollPane(canvas2);
			canvasPanel.add(scroll);
		}

		// frame settings
		this.addComponentListener(this);
		this.setSize(2560, 1440);
		this.setVisible(true);
		this.requestFocus();
		this.stateChanged(null);
	}

	public void selectMap(int mapId)
	{
		for(int i = 0; i < mapCB.getModel().getSize(); i++)
		{
			if(mapCB.getModel().getElementAt(i).getId() == mapId)
			{
				mapCB.setSelectedItem(mapCB.getModel().getElementAt(i));
				return;
			}
		}
	}

	@Override
	public void update(Graphics g)
	{
		redraw();
		super.update(g);
	}
	
	private String toRoundedString(double number, int digitsAfterComma)
	{
		double factor = Math.pow(10 , digitsAfterComma);
		double rounded = Math.round(number * factor) / factor;
		String s = "" + rounded;
		int commaAt = s.indexOf(".");
		if(commaAt >= 0 && s.length() - commaAt - 1> digitsAfterComma)
			s = s.substring(0, commaAt + digitsAfterComma + 1);
		return s;
	}

	public void redraw()
	{
		try
		{
			String mapCode = codeArea.getText();

			int drawSize = drawSizeSlider.getValue();
			double scaleX = scaleXSlider.getValue() / 10.0;
			double scaleY = scaleYSlider.getValue() / 10.0;
			int rotation = rotationSlider.getValue();

			if(mirrorXCB.isSelected())
				scaleX *= -1;
			if(mirrorYCB.isSelected())
				scaleY *= -1;

			boolean smart = scaleSmartCB.isSelected();
			boolean swapSF = modSwapSFCB.isSelected();
			boolean trim = modTrimCB.isSelected();

			char[][] original = MapGeneratorUtil.toArray(mapCode);

			if(swapSF)
				MapGeneratorUtil.swapChars(original, 'S', 'F');

			double[][] matrix = MapTransformer.createMatrix(scaleX, scaleY, rotation, original[0].length, original.length);

			int digits = 3;
			m00TF.setText(toRoundedString(matrix[0][0], digits));
			m01TF.setText(toRoundedString(matrix[0][1], digits));
			m10TF.setText(toRoundedString(matrix[1][0], digits));
			m11TF.setText(toRoundedString(matrix[1][1], digits));
			
			MapTransformer transformer;
			if(smart)
				transformer = new UltimateScaler(matrix);
			else
				transformer = new NearestNeighborScaler(matrix);

			char[][] scaled1 = transformer.transform(original);
			if(trim)
				scaled1 = MapGeneratorUtil.trim(scaled1, 1, 1);

			Graphics g1 = canvas1.getGraphics();
			g1.clearRect(0, 0, 10000, 10000);
			for(int y = 0; y < scaled1.length; y++)
			{
				for(int x = 0; x < scaled1[y].length; x++)
				{
					drawKaro(g1, x, y, scaled1[y][x], drawSize);
				}
			}

			if(comparisonMode)
			{
				transformer = new NearestNeighborScaler(matrix);
				char[][] scaled2 = transformer.transform(original);
				if(trim)
					scaled2 = MapGeneratorUtil.trim(scaled2, 1, 1);

				Graphics g2 = canvas2.getGraphics();
				g2.clearRect(0, 0, 10000, 10000);
				for(int y = 0; y < scaled2.length; y++)
				{
					for(int x = 0; x < scaled2[y].length; x++)
					{
						drawKaro(g2, x, y, scaled2[y][x], drawSize);
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static Color	SAND	= new Color(228, 230, 127);
	private static Color	MUD		= new Color(97, 70, 16);

	private void drawKaro(Graphics g, int x, int y, char c, int drawSize)
	{
		switch(c)
		{
			case 'X':
				g.setColor(Color.GREEN);
				break;
			case 'O':
			case 'S':
				g.setColor(Color.GRAY);
				break;
			case 'P':
				g.setColor(Color.LIGHT_GRAY);
				break;
			case 'V':
				g.setColor(Color.DARK_GRAY);
				break;
			case 'T':
				g.setColor(Color.BLACK);
				break;
			case 'F':
			case 'N':
				g.setColor(Color.WHITE);
				break;
			case '1':
			case '2':
			case 'W':
				g.setColor(Color.BLUE);
				break;
			case '3':
			case '4':
				g.setColor(Color.GREEN);
				break;
			case '5':
			case '6':
			case 'G':
				g.setColor(Color.YELLOW);
				break;
			case '7':
			case '8':
			case 'L':
				g.setColor(Color.RED);
				break;
			case '9':
				g.setColor(Color.PINK);
				break;
			case 'Y':
				g.setColor(SAND);
				break;
			case 'Z':
				g.setColor(MUD);
				break;
			default:
				System.out.println("invalid char = '" + c + "'");
		}

		g.fillRect(x * drawSize, y * drawSize, drawSize, drawSize);

		switch(c)
		{
			case 'S':
				g.setColor(Color.BLACK);
				g.fillRect(x * drawSize + drawSize / 3, y * drawSize + drawSize / 3, drawSize / 3, drawSize / 3);
				return;
			case 'F':
			case '1':
			case '3':
			case '5':
			case '7':
			case '9':
				g.setColor(Color.BLACK);
				break;
			case '2':
			case '4':
			case '6':
			case '8':
				g.setColor(Color.WHITE);
				break;
			case 'X':
			case 'O':
			default:
				return;
		}

		g.fillRect(x * drawSize, y * drawSize, drawSize / 4, drawSize / 4);
		g.fillRect(x * drawSize + drawSize / 2, y * drawSize, drawSize / 4, drawSize / 4);

		g.fillRect(x * drawSize + drawSize / 4, y * drawSize + drawSize / 4, drawSize / 4, drawSize / 4);
		g.fillRect(x * drawSize + 3 * drawSize / 4, y * drawSize + drawSize / 4, drawSize / 4, drawSize / 4);

		g.fillRect(x * drawSize, y * drawSize + drawSize / 2, drawSize / 4, drawSize / 4);
		g.fillRect(x * drawSize + drawSize / 2, y * drawSize + drawSize / 2, drawSize / 4, drawSize / 4);

		g.fillRect(x * drawSize + drawSize / 4, y * drawSize + 3 * drawSize / 4, drawSize / 4, drawSize / 4);
		g.fillRect(x * drawSize + 3 * drawSize / 4, y * drawSize + 3 * drawSize / 4, drawSize / 4, drawSize / 4);
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		boolean syncScale = scaleSyncCB.isSelected();
		if(syncScale)
			this.scaleYSlider.setValue(this.scaleXSlider.getValue());

		this.drawSizeTF.setText("" + this.drawSizeSlider.getValue() + " Pixel/Karo");
		this.scaleXTF.setText("" + (this.scaleXSlider.getValue() / 10.0));
		this.scaleYTF.setText("" + (this.scaleYSlider.getValue() / 10.0));
		this.rotationTF.setText(this.rotationSlider.getValue() + " deg");

		redraw();
	}

	@Override
	public void itemStateChanged(ItemEvent e)
	{
		boolean syncScale = scaleSyncCB.isSelected();
		this.scaleYSlider.setEnabled(!syncScale);
		if(syncScale)
			this.scaleYSlider.setValue(this.scaleXSlider.getValue());

		redraw();
	}

	@Override
	public void insertUpdate(DocumentEvent e)
	{
		redraw();
	}

	@Override
	public void removeUpdate(DocumentEvent e)
	{
		redraw();
	}

	@Override
	public void changedUpdate(DocumentEvent e)
	{
		redraw();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		codeArea.setText(((Map) mapCB.getSelectedItem()).getCode());
		redraw();
	}

	@Override
	public void componentResized(ComponentEvent e)
	{
		// redraw();
	}

	@Override
	public void componentMoved(ComponentEvent e)
	{
	}

	@Override
	public void componentShown(ComponentEvent e)
	{
	}

	@Override
	public void componentHidden(ComponentEvent e)
	{
	}

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		Properties properties = PropertiesUtil.loadProperties(new File(args[0]));
		KaroAPI api = new KaroAPI(properties.getProperty("karoAPI.user"), properties.getProperty("karoAPI.password"));
		int canvasOrientation = -1;
		if(args.length > 1)
		{
			if(args[1].equalsIgnoreCase("-compareX"))
				canvasOrientation = BoxLayout.X_AXIS;
			else if(args[1].equalsIgnoreCase("-compareY"))
				canvasOrientation = BoxLayout.Y_AXIS;
		}

		List<Map> maps = new LinkedList<Map>(api.getMaps(true).get());
		Map testMap = new Map(0);
		testMap.setCode(MapTransformer.TEST_CODE);
		maps.add(0, testMap);

		MapTransformerUI ui = new MapTransformerUI(maps, canvasOrientation);
		ui.selectMap(testMap.getId());
		ui.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		new Thread() {
			public void run()
			{
				try
				{
					Thread.sleep(100);
				}
				catch(InterruptedException e)
				{
				}
				ui.redraw();
			}
		}.start();
	}
}
