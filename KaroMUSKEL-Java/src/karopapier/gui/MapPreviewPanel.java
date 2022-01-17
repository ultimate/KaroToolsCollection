package karopapier.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import karopapier.model.Map;

public class MapPreviewPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private InnerMapPreviewPanel innerP;

	public MapPreviewPanel() {
		this.setBorder(BorderFactory.createLineBorder(Color.gray));
		this.setLayout(null);
		innerP = new InnerMapPreviewPanel();
		this.add(innerP, null);
	}
	
	public void setBounds(Rectangle r) {
		innerP.setBounds(2, 2, (int)r.getWidth()-4, (int)r.getHeight()-4);
		this.remove(innerP);
		this.add(innerP);
		super.setBounds(r);
		repaint();
	}
	
	public void setBounds(int x, int y, int width, int height) {
		innerP.setBounds(2, 2, width-4, height-4);
		this.remove(innerP);
		this.add(innerP);
		super.setBounds(x, y, width, height);
		repaint();
	}
	
	public void displayMap(Map m) {
		if(m == null) {
			innerP.setImage(null);
		} else {
			innerP.setImage(m.getImage());
		}
		repaint();
	}
	
	private class InnerMapPreviewPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		private Image image;

		public void setImage(Image image) {
			this.image = image;
		}

		public void paint(Graphics g) {
		    if(image!=null)
		    	g.drawImage( image, 0, 0, (int)this.getSize().getWidth(), (int)this.getSize().getHeight(), null);
		    else {
		    	g.setColor(Color.WHITE);
		    	g.fillRect(0, 0, (int)this.getSize().getWidth(), (int)this.getSize().getHeight());
		    	g.setColor(Color.black);

		    	int y = (int)this.getSize().getHeight()/2+6;
		    	int x = (int)this.getSize().getWidth()/2-3;
		    	
		    	drawQuestionMark(g, x, y);
		    	drawQuestionMark(g, x-27, y);
		    	drawQuestionMark(g, x+27, y);
		    }
		    
		}
		
		private void drawQuestionMark(Graphics g, int x, int y) {
	    	g.drawString("*", x  , y  );
	    	g.drawString("*", x+3, y-3);
	    	g.drawString("*", x+6, y-6);
	    	g.drawString("*", x+9, y-9);
	    	g.drawString("*", x+9, y-12);
	    	g.drawString("*", x+9, y-15);
	    	g.drawString("*", x+6, y-18);
	    	g.drawString("*", x+3, y-21);
	    	g.drawString("*", x  , y-21);
	    	g.drawString("*", x-3, y-21);
	    	g.drawString("*", x-6, y-18);
	    	g.drawString("*", x-9, y-15);
	    	g.drawString("*", x  , y+3);
	    	g.drawString("*", x  , y+6);
	    	g.drawString("*", x  , y+9);
	    	g.drawString("*", x  , y+15);
	    	g.drawString("*", x  , y+18);
	    	g.drawString("*", x-3, y+18);
	    	g.drawString("*", x+3, y+18);
	    	g.drawString("*", x  , y+21);
		}
	}
}
