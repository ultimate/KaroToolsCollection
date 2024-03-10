package ultimate.karopapier;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class MapTransformer extends JFrame implements DocumentListener, ChangeListener
{
	// @formatter:off
	public static final String MAP_1  = "PXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"+
										"PXXXXOOOOOOOFSOOOOOOOOOOOOOOOOOOOOOO1OOOOOOOOOOOOOOOXXXXXXXX\n"+
										"PXXOOOOOOOOOFSOOOOOOOOOOOOOOOOOOOOO1O1OOOOOOOOOOOOOOOOOXXXXX\n"+
										"PXOOOOOOOOOOFSOOOOOOOOOOOOOOOOOOOO1O1O1OOOOOOOOOOOOOOOOOOXXX\n"+
										"PXOOOOOOOOOOFSOOOOOOOOOOOOOOOOOOO1O1O1O1OOOOOOOOOOOOOOOOOOOX\n"+
										"XOOOOOOOOOOOFSOOOOOOOOOOOOOOOOOO1O1O1O1O1OOOOOOOOOOOOOOOOOOX\n"+
										"XXOOOOOOOOXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOOX\n"+
										"XOOOOOOOXXXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOOXXXXXXXOOOOOOOOX\n"+
										"XOOOOOOXXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOOOOOXXXXXXOOOOOOOXX\n"+
										"X77777XXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOOOOOOOOXXXOOOOOOOOXX\n"+
										"XOOOOOXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOXXOOOOOOO33XXX2O2O2O2XX\n"+
										"XOOOOOOXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOXXXXXOO3333OXX2222222XX\n"+
										"XOOOOOOOOXXXXXXXXXXXXXXXXXXXXOOOOOOOXXXXXXXX333OOOOOOOOOOXXX\n"+
										"XOOOOOOOOXXXXXXXXXXXXXXXXXXXXX444444XXXXXXXXXOOOOOOOOOOOOXXX\n"+
										"XXOOOOOOOOOXXXXXXXXXXXXXXXXXXXXOOOOOOXXXXXXXXOOOOOOOOOOOXXXX\n"+
										"XXXOOOOOOO666XXXXXXXXXXXXXXXXXXXOOOOOOOXXXXXXXXOOOOOOOOXXXXX\n"+
										"XXXOOOOOO66OOOOOOOOOOXXXXXXXXXXXXOOOOOOOXXXXXXXXXXXXXXXXXXXX\n"+
										"XXXOOOOOO6OOOOOOOOOOOOOOXXXXXXXXOOOOOOOOXXXXXXXXXXXXXXXXXXXX\n"+
										"XXXXXXXXX6OOOOOOOOOOOOOOOOXXXOOOOOOOOOOOXXXXXXXXXXXXXXXXXXXX\n"+
										"XXXXXXXXXXXXXXXXOOOOOOOOOO555OOOOOOOOOOXXXXXXXXXXXXXXXXXXXXX\n"+
										"XXXXXXXXXXXXXXXXXXXOOOOOOO555OOOOOOOOXXXXXXXXXXXXXXXXXXXXXXX\n"+
										"XXXXXXXXXXXXXXXXXXXXXOOOOO555OOOOOOXXXXXXXXXXXXXXXXXXXXXXXXX\n"+
										"XXXXXXXXXXXXXXXXXXXXXXXXXO555OOOXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"+
										"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"+
										"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
	public static final String MAP_28 = "PXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"+
										"PXXXXXXXXXXXXXXXXXXXXXOOOOOOOOOOOOOOOOOOOOOOOOOXXXXXXXOOOOOOOOOOOOOOOOOOOOOOOOOXXXXXXXXXOXXXXXXXX\n"+
										"PXOXXXXXXXXXXXXXXXOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOXXXOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOXXXXXOOOXXXXXXX\n"+
										"PXOOXXXXXXXXXXXOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOXOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOXXXOOOOOXXXXXX\n"+
										"PXOOOXXXXXXXXXOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOXOXOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOXOOOOOOOXXXXX\n"+
										"PXOOOOXXXXXXXOOOOOOOOOOOOOXXXXXXXXXXXXXXOOOOOOOOXOXOXOOOOOXXXXXXXXXXXXXXXXXXXOOOOOOXOXOOOOOOOXXXX\n"+
										"PXOOOOOXXXXXOOOOOOOOOOOXXXXXXXXXXXXXXXXXXXXXXOOXOXOXOXOOXXXXXXXXXXXXXXXXXXXXXXXOOOXOXOXOOOOOOOXXX\n"+
										"PXOOOOOOXXXSOOOOOOOOOXXXXXXXOOOXXOOOXXXXXXXXXXXOXOXOXOXXXXXXXXXXXXXXXXXXXXXXXXXXOXOXOXOXOOOOOOOXX\n"+
										"PXOOOOOOOXFSSOOOOOOXXXXXXXXXXXOXXOXXXXXXXXXXXXOXOXOXOXOXXXXXXXXXXXXXXXXXXXXXXXXXXOXOXOXOX1111111X\n"+
										"XXOOOOOOXOXFSSOOOOXXXXXXXXXXXXXOOXXXXXXXXXXXXOOOXOXOXOOOXXXXXXXXXXXXXXXXXXXXXXXXOXOXOXOXOOOOOOOXX\n"+
										"XXOOOOOXOXOXFSSOOXXXXXXXXXXXXXXOOXXXXXXXXXXXOOOOOXOXOOOOOXXXXXXXXXXXXXXXXXXXXXXOOOXOXOXOOOOOOOXXX\n"+
										"XXOOOOXOXOXOXFSSXXXXXXXXXXXXXXOOOOXXXXXXXXXOOOOOOOXOOOOOOOXXXXXXXXXXXXXXXXXXXXOOOOOXOXOOOOOOOXXXX\n"+
										"XX2222OXOXOXOXXXXXXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOXXXOOOOOOOOXXXXXXXXXXXXXXXXOOOOOOOOXOOOOOOOXXXXX\n"+
										"XXOOOOXOXOXOXOXXXXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOXXXXXOOOOOOOOOXXXXXXXXXXXXOOOOOOOOOXXXOOOOOXXXXXX\n"+
										"XXOOOOOXOXOXOOOXXXXXXXXXXXXXXXXXXXXXXXXOOOOOOOOXXXXXXXXOOOOOOOOOOOOOOOOOOOOOOOOOOOXXXXXOOOXXXXXXX\n"+
										"XXOOOOOOXOXOOOOOOXXXXXXXXXXXXXXXXXXXOOOOOOOOOOXXXXXXXXXXXOOOOOOOOOOOOOOOOOOOOOOOOXXXXXXXOXXXXXXXX\n"+
										"XXOOOOOOOXOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOXXXXXXXXXXXXXXXOOOOOOOOOOOOOOOOOOXXXXXXXXXXXXXXXXXXX\n"+
										"XXOOOOOOXXXOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOXXXXXXXXXXXXXXXXXXXXOOOOOOOOOOOXXXXXXXXXXXXXXXXXXXXXXX\n"+
										"XXOOOOOXXXXXOOOOOOOOOOOOOOOOOOOOOOOOOOOOXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"+
										"XXOOOOXXXXXXXXOOOOOOOOOOOOOOOOOOOOOOOXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"+
										"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"+
										"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
	
	public static final String LINE_SEPARATOR = "\n";
	public static final int DRAW_SCALE = 9;
	// @formatter:on

	public static void main(String[] args)
	{
		MapTransformer frame = new MapTransformer(MAP_28);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		new Thread() {
			public void run() {
				try
				{
					Thread.sleep(100);
				}
				catch(InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				frame.redraw();
			}
		}.start();
	}

	// @formatter:off
	public static double[][] createScaleMatrix(double scale)
	{
		return new double[][] {
			{ scale, 0, 0 },
			{ 0, scale, 0 },
			{ 0, 0, 1 } 
		};
	}
	
	public static double[][] invertMatrix(double[][] matrix)
	{
		double a = matrix[0][0];
		double b = matrix[0][1];
		double c = matrix[0][2];
		double d = matrix[1][0];
		double e = matrix[1][1];
		double f = matrix[1][2];
		double g = matrix[2][0];
		double h = matrix[2][1];
		double i = matrix[2][2];
		
		// https://studyflix.de/mathematik/determinante-3x3-2326
		double det = a*e*i+b*f*g+c*d*h-g*e*c-h*f*a-i*d*b;
		
		// https://de.wikipedia.org/wiki/Inverse_Matrix#Explizite_Formeln
		return new double[][] {
			{ (e*i-f*h)/det, (c*h-b*i)/det, (b*f-c*e)/det },
			{ (f*g-d*i)/det, (a*i-c*g)/det, (c*d-a*f)/det },
			{ (d*h-e*g)/det, (b*b-a*h)/det, (a*e-b*d)/det } 
		};
	}
	// @formatter:on

	public static char[][] toArray(String mapCode)
	{
		String[] lines = mapCode.split(LINE_SEPARATOR);
		char[][] array = new char[lines.length][];

		for(int i = 0; i < lines.length; i++)
			array[i] = lines[i].toCharArray();

		return array;
	}
	
	public static void printMatrix(double[][] matrix)
	{
		System.out.println(matrix[0][0] + "\t" + matrix[0][1] + "\t" + matrix[0][2]);
		System.out.println(matrix[1][0] + "\t" + matrix[1][1] + "\t" + matrix[1][2]);
		System.out.println(matrix[2][0] + "\t" + matrix[2][1] + "\t" + matrix[2][2]);
	}

	public static char[][] transform(char[][] original, double[][] matrix)
	{
//		System.out.println("matrix = ");
//		printMatrix(matrix);
		double[][] inv = invertMatrix(matrix);
//		System.out.println("inv = ");
//		printMatrix(inv);
		
		int oldSizeY = original.length;
		int oldSizeX = original[oldSizeY-1].length;
		int newSizeX = (int) (matrix[0][0] * oldSizeX + matrix[0][1] * oldSizeY);
		int newSizeY = (int) (matrix[1][0] * oldSizeX + matrix[1][1] * oldSizeY);
		
		char[][] scaled = new char[newSizeY][];
		
		int originX, originY;
		for(int y = 0; y < newSizeY; y++)
		{
			scaled[y] = new char[newSizeX];
			for(int x = 0; x < newSizeX; x++)
			{
				originX = (int)(inv[0][0] * x + inv[0][1] * y + inv[0][2]);
				originY = (int)(inv[1][0] * x + inv[1][1] * y + inv[1][2]);
				
				originX = Math.min(Math.max(0,  originX), oldSizeX);
				originY = Math.min(Math.max(0,  originY), oldSizeY);
				
				scaled[y][x] = original[originY][originX];
			}
		}
		return scaled;
	}

	public static void drawKaro(Graphics g, int x, int y, char c)
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
				g.setColor(Color.YELLOW);
				break;
			case '7':
			case '8':
				g.setColor(Color.RED);
				break;
			case '9':
				g.setColor(Color.PINK);
				break;
		}

		g.fillRect(x * DRAW_SCALE, y * DRAW_SCALE, DRAW_SCALE, DRAW_SCALE);

		switch(c)
		{
			case 'X':
			case 'O':
				return;
			case 'S':
				g.setColor(Color.BLACK);
				g.fillRect(x * DRAW_SCALE + DRAW_SCALE / 3, y * DRAW_SCALE + DRAW_SCALE / 3, DRAW_SCALE / 3, DRAW_SCALE / 3);
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
		}

		g.fillRect(x * DRAW_SCALE, y * DRAW_SCALE, DRAW_SCALE / 4, DRAW_SCALE / 4);
		g.fillRect(x * DRAW_SCALE + DRAW_SCALE / 2, y * DRAW_SCALE, DRAW_SCALE / 4, DRAW_SCALE / 4);

		g.fillRect(x * DRAW_SCALE + DRAW_SCALE / 4, y * DRAW_SCALE + DRAW_SCALE / 4, DRAW_SCALE / 4, DRAW_SCALE / 4);
		g.fillRect(x * DRAW_SCALE + 3 * DRAW_SCALE / 4, y * DRAW_SCALE + DRAW_SCALE / 4, DRAW_SCALE / 4, DRAW_SCALE / 4);

		g.fillRect(x * DRAW_SCALE, y * DRAW_SCALE + DRAW_SCALE / 2, DRAW_SCALE / 4, DRAW_SCALE / 4);
		g.fillRect(x * DRAW_SCALE + DRAW_SCALE / 2, y * DRAW_SCALE + DRAW_SCALE / 2, DRAW_SCALE / 4, DRAW_SCALE / 4);

		g.fillRect(x * DRAW_SCALE + DRAW_SCALE / 4, y * DRAW_SCALE + 3 * DRAW_SCALE / 4, DRAW_SCALE / 4, DRAW_SCALE / 4);
		g.fillRect(x * DRAW_SCALE + 3 * DRAW_SCALE / 4, y * DRAW_SCALE + 3 * DRAW_SCALE / 4, DRAW_SCALE / 4, DRAW_SCALE / 4);
	}

	private JTextField		scaleTF;
	private JSlider		scaleSlider;
	private Canvas		canvas;
	private JTextArea	codeArea;

	public MapTransformer(String mapCode)
	{ 
		scaleTF = new JTextField("1.0");
		scaleTF.setEditable(false);
		scaleSlider = new JSlider(1, 100, 10);
		canvas = new Canvas();
		codeArea = new JTextArea(mapCode);
		codeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

		this.getContentPane().setLayout(new BorderLayout());
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.add(scaleSlider);
		topPanel.add(scaleTF);
		this.getContentPane().add(topPanel, BorderLayout.NORTH);
		this.getContentPane().add(canvas, BorderLayout.CENTER);
		this.getContentPane().add(codeArea, BorderLayout.SOUTH);

		codeArea.getDocument().addDocumentListener(this);
		scaleSlider.addChangeListener(this);

		this.setSize(1920, 1080);
		this.setVisible(true);
		this.requestFocus();
	}

	public void redraw()
	{
		String mapCode = codeArea.getText();

		double scale = scaleSlider.getValue() / 10.0;
		
		System.out.println(scale);

		double[][] matrix = createScaleMatrix(scale);

		char[][] original = toArray(mapCode);

		char[][] scaled = transform(original, matrix);

		Graphics g = canvas.getGraphics();
		g.clearRect(0, 0, 10000, 10000);
		for(int y = 0; y < scaled.length; y++)
		{
			for(int x = 0; x < scaled[y].length; x++)
			{
				drawKaro(g, x, y, scaled[y][x]);
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		scaleTF.setText("" + (scaleSlider.getValue() / 10.0));
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
}
