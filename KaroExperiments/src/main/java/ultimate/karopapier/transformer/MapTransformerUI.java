package ultimate.karopapier.transformer;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
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

public class MapTransformerUI extends JFrame implements DocumentListener, ChangeListener, ActionListener, ComponentListener
{
	private static final long	serialVersionUID	= 1L;

	private static final int	GAP					= 10;
	private static final int	ROW					= 30;
	private static final int	COL					= 100;

	private JLabel				drawSizeLabel;
	private JSlider				drawSizeSlider;
	private JTextField			drawSizeTF;

	private JLabel				scaleLabel;
	private JSlider				scaleSlider;
	private JTextField			scaleTF;

	private JLabel				rotationLabel;
	private JSlider				rotationSlider;
	private JTextField			rotationTF;

	private JComboBox<Map>		mapCB;
	private JTextArea			codeArea;

	private Canvas				canvas1;
	private Canvas				canvas2;

	public MapTransformerUI(List<Map> maps)
	{
		this.getContentPane().setLayout(null);

		// draw size
		drawSizeLabel = new JLabel("Draw Size:");
		drawSizeLabel.setBounds(GAP * 1 + COL * 0, GAP * 1 + ROW * 0, COL, ROW);
		this.getContentPane().add(drawSizeLabel);
		drawSizeSlider = new JSlider(1, 20, 9);
		drawSizeSlider.setBounds(GAP * 2 + COL * 1, GAP * 1 + ROW * 0, 2 * COL, ROW);
		drawSizeSlider.addChangeListener(this);
		this.getContentPane().add(drawSizeSlider);
		drawSizeTF = new JTextField(drawSizeSlider.getValue() + " Pixel/Karo");
		drawSizeTF.setBounds(GAP * 3 + COL * 3, GAP * 1 + ROW * 0, COL, ROW);
		drawSizeTF.setEditable(false);
		this.getContentPane().add(drawSizeTF);

		// scaling
		scaleLabel = new JLabel("Scale:");
		scaleLabel.setBounds(GAP * 1 + COL * 0, GAP * 1 + ROW * 1, COL, ROW);
		this.getContentPane().add(scaleLabel);
		scaleSlider = new JSlider(1, 100, 10);
		scaleSlider.setBounds(GAP * 2 + COL * 1, GAP * 1 + ROW * 1, 2 * COL, ROW);
		scaleSlider.addChangeListener(this);
		this.getContentPane().add(scaleSlider);
		scaleTF = new JTextField(scaleSlider.getValue() / 10.0 + "");
		scaleTF.setBounds(GAP * 3 + COL * 3, GAP * 1 + ROW * 1, COL, ROW);
		scaleTF.setEditable(false);
		this.getContentPane().add(scaleTF);

		// rotation
		rotationLabel = new JLabel("Rotation (CCW):");
		rotationLabel.setBounds(GAP * 1 + COL * 0, GAP * 2 + ROW * 2, COL, ROW);
		this.getContentPane().add(rotationLabel);
		rotationSlider = new JSlider(0, 360, 0);
		rotationSlider.setBounds(GAP * 2 + COL * 1, GAP * 2 + ROW * 2, 2 * COL, ROW);
		rotationSlider.addChangeListener(this);
		this.getContentPane().add(rotationSlider);
		rotationTF = new JTextField(rotationSlider.getValue() + " deg");
		rotationTF.setBounds(GAP * 3 + COL * 3, GAP * 2 + ROW * 2, COL, ROW);
		rotationTF.setEditable(false);
		this.getContentPane().add(rotationTF);

		// map & code
		DefaultComboBoxModel<Map> model = new DefaultComboBoxModel<Map>(maps.toArray(new Map[0]));
		mapCB = new JComboBox<>(model);
		mapCB.addActionListener(this);
		mapCB.setBounds(GAP * 1 + COL * 0, GAP * 3 + ROW * 3, 4 * COL + 2 * GAP, ROW);
		this.getContentPane().add(mapCB);

		// code
		codeArea = new JTextArea("");
		codeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
		codeArea.getDocument().addDocumentListener(this);
		JScrollPane scroll = new JScrollPane(codeArea);
		scroll.setBounds(GAP * 1 + COL * 0, GAP * 4 + ROW * 4, 4 * COL + 2 * GAP, ROW * 20);
		this.getContentPane().add(scroll);

		// canvases
		canvas1 = new Canvas();
		canvas1.setBounds(GAP * 4 + COL * 4, GAP * 1 + ROW * 0, COL * 20, ROW * 20);
		this.getContentPane().add(canvas1);
		canvas2 = new Canvas();
		canvas2.setBounds(GAP * 4 + COL * 4, GAP * 2 + ROW * 20, COL * 20, ROW * 20);
		this.getContentPane().add(canvas2);

		// frame settings
		this.addComponentListener(this);
		this.setSize(2560, 1440);
		this.setVisible(true);
		this.requestFocus();
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

	@Override
	public void repaint(long time, int x, int y, int width, int height)
	{
		// TODO Auto-generated method stub
		super.repaint(time, x, y, width, height);
	}

	public void redraw()
	{
		String mapCode = codeArea.getText();

		double scale = scaleSlider.getValue() / 10.0;
		int rotation = rotationSlider.getValue();

		// System.out.println("scale = " + scale + "\trotation=" + rotation);

		char[][] original = MapTransformer.toArray(mapCode);

		double[][] matrix = MapTransformer.createMatrix(scale, rotation, original[0].length, original.length);

		char[][] scaled1 = MapTransformer.transform(original, matrix, false);
		
		int drawSize = drawSizeSlider.getValue();

		Graphics g1 = canvas1.getGraphics();
		g1.clearRect(0, 0, 10000, 10000);
		for(int y = 0; y < scaled1.length; y++)
		{
			for(int x = 0; x < scaled1[y].length; x++)
			{
				drawKaro(g1, x, y, scaled1[y][x], drawSize);
			}
		}

		char[][] scaled2 = MapTransformer.transform(original, matrix, true);

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

	private void drawKaro(Graphics g, int x, int y, char c, int drawSize)
	{
		switch(c)
		{
			case 'X':
				g.setColor(Color.GREEN);
				break;
			case 'O':
				g.setColor(Color.GRAY);
				break;
			case 'S':
				g.setColor(Color.GRAY);
				break;
			case 'P':
				g.setColor(Color.LIGHT_GRAY);
				break;
			case 'F':
				g.setColor(Color.WHITE);
				break;
			case '1':
			case '2':
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
		this.drawSizeTF.setText("" + this.drawSizeSlider.getValue() + " Pixel/Karo");
		this.scaleTF.setText("" + (this.scaleSlider.getValue() / 10.0));
		this.rotationTF.setText(this.rotationSlider.getValue() + " deg");
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

		List<Map> maps = new LinkedList<Map>(api.getMaps(true).get());
		Map testMap = new Map(0);
		testMap.setCode(MapTransformer.TEST_CODE);
		maps.add(0, testMap);

		MapTransformerUI ui = new MapTransformerUI(maps);
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
