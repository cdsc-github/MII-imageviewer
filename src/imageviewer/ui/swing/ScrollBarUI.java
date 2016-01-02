/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.io.File;

import javax.imageio.ImageIO;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.UIManager;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalScrollBarUI;

// =======================================================================

public class ScrollBarUI extends MetalScrollBarUI {

	private static final BufferedImage BACKGROUND_ENABLED=loadImage(UIManager.getString("ScrollBar.backgroundEnabled"));
	private static final BufferedImage BACKGROUND_DISABLED=loadImage(UIManager.getString("ScrollBar.backgroundDisabled"));

	private static final BufferedImage SCROLL_DEC_NORMAL=loadImage(UIManager.getString("ScrollBar.decreaseNormal"));
	private static final BufferedImage SCROLL_DEC_OVER=loadImage(UIManager.getString("ScrollBar.decreaseOver"));
	private static final BufferedImage SCROLL_DEC_PRESSED=loadImage(UIManager.getString("ScrollBar.decreasePressed"));
	private static final BufferedImage SCROLL_INC_NORMAL=loadImage(UIManager.getString("ScrollBar.increaseNormal"));
	private static final BufferedImage SCROLL_INC_OVER=loadImage(UIManager.getString("ScrollBar.increaseOver"));
	private static final BufferedImage SCROLL_INC_PRESSED=loadImage(UIManager.getString("ScrollBar.increasePressed"));
	private static final BufferedImage SCROLL_THUMB_NORMAL=loadImage(UIManager.getString("ScrollBar.thumbNormal"));

	private static final BufferedImage LIGHT_BACKGROUND_ENABLED=loadImage(UIManager.getString("ScrollBar.lightBackgroundEnabled"));
	private static final BufferedImage LIGHT_BACKGROUND_DISABLED=loadImage(UIManager.getString("ScrollBar.lightBackgroundDisabled"));
	private static final BufferedImage LIGHT_SCROLL_DEC_NORMAL=loadImage(UIManager.getString("ScrollBar.lightDecreaseNormal"));
	private static final BufferedImage LIGHT_SCROLL_DEC_OVER=loadImage(UIManager.getString("ScrollBar.lightDecreaseOver"));
	private static final BufferedImage LIGHT_SCROLL_DEC_PRESSED=loadImage(UIManager.getString("ScrollBar.lightDecreasePressed"));
	private static final BufferedImage LIGHT_SCROLL_INC_NORMAL=loadImage(UIManager.getString("ScrollBar.lightIncreaseNormal"));
	private static final BufferedImage LIGHT_SCROLL_INC_OVER=loadImage(UIManager.getString("ScrollBar.lightIncreaseOver"));
	private static final BufferedImage LIGHT_SCROLL_INC_PRESSED=loadImage(UIManager.getString("ScrollBar.lightIncreasePressed"));
	private static final BufferedImage LIGHT_SCROLL_THUMB_NORMAL=loadImage(UIManager.getString("ScrollBar.lightThumbNormal"));

	private static final BufferedImage SCROLL_THUMB_OVER=loadImage(UIManager.getString("ScrollBar.thumbOver"));
	private static final BufferedImage SCROLL_THUMB_PRESSED=loadImage(UIManager.getString("ScrollBar.thumbPressed"));

	private static final int BUTTON_WIDTH=21;
	private static final int THUMB_WIDTH=32;
	private static final int SCROLL_SIZE=11;

	// =======================================================================

	boolean incBtnMouseOver, incBtnMousePressed;
	boolean decBtnMouseOver, decBtnMousePressed;
	boolean thumbMousePressed;
	boolean isLight=false;

	// =======================================================================

	public static ComponentUI createUI(JComponent c) {return new ScrollBarUI();}

	private static BufferedImage loadImage(String filename) {try {return ImageIO.read(new File(filename));} catch (Exception exc) {exc.printStackTrace();} return null;}

	// =======================================================================

	public void installUI(JComponent c) {

		super.installUI(c);
		c.setOpaque(false);
		c.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {if (isThumbRollover()) {thumbMousePressed=true; scrollbar.repaint();}}
			public void mouseReleased(MouseEvent e) {thumbMousePressed=false;}
		});
	}

	// =======================================================================

	public Dimension getPreferredSize(JComponent c) {

		Dimension d=super.getPreferredSize(c);
		return (c.getBorder()!=null) ? d : new Dimension((int)d.getWidth()-2,(int)d.getHeight());
	}

	protected Dimension getMinimumThumbSize() {return new Dimension(29,29);}

	// =======================================================================

	protected JButton createDecreaseButton(int orientation) {

		decreaseButton=new ScrollArrowButton(orientation,scrollBarWidth,isFreeStanding);
		decreaseButton.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {decBtnMouseOver=true;}
			public void mouseExited(MouseEvent e) {decBtnMouseOver=false;}
			public void mousePressed(MouseEvent e) {decBtnMousePressed=true;}
			public void mouseReleased(MouseEvent e) {decBtnMousePressed=false;}
		});
		return decreaseButton;
	}

	// =======================================================================

	protected JButton createIncreaseButton(int orientation) {

		increaseButton=new ScrollArrowButton(orientation,scrollBarWidth,isFreeStanding);
		increaseButton.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {incBtnMouseOver=true;}
			public void mouseExited(MouseEvent e) {incBtnMouseOver=false;}
			public void mousePressed(MouseEvent e) {incBtnMousePressed=true;}
			public void mouseReleased(MouseEvent e) {incBtnMousePressed=false;}
		});
		return increaseButton;
	}

	// =======================================================================

	protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {

		isLight=(c.getClientProperty("__lightColorScrollbar")!=null) ? true : false;
		BufferedImage decImg=(isLight) ? (decBtnMousePressed ? LIGHT_SCROLL_DEC_PRESSED : decBtnMouseOver ? LIGHT_SCROLL_DEC_OVER : LIGHT_SCROLL_DEC_NORMAL) : 
			(decBtnMousePressed ? SCROLL_DEC_PRESSED : decBtnMouseOver ? SCROLL_DEC_OVER : SCROLL_DEC_NORMAL);
		BufferedImage incImg=(isLight) ? (incBtnMousePressed ? LIGHT_SCROLL_INC_PRESSED : incBtnMouseOver ? LIGHT_SCROLL_INC_OVER : LIGHT_SCROLL_INC_NORMAL) :
			(incBtnMousePressed ? SCROLL_INC_PRESSED : incBtnMouseOver ? SCROLL_INC_OVER : SCROLL_INC_NORMAL);
		Graphics2D g2=(Graphics2D)g;
		AffineTransform origTransform=g2.getTransform();
		int scrollWidth=scrollbar.getWidth();
		if (scrollbar.getOrientation()==JScrollBar.VERTICAL) {
			scrollWidth=scrollbar.getHeight();
			g2.scale(1,-1);
			g2.rotate(-Math.PI/2,0,0);
		}

		if (scrollbar.isEnabled()) {
			g.drawImage(decImg,0,0,scrollbar);
			g.drawImage(BACKGROUND_ENABLED,BUTTON_WIDTH,0,scrollWidth-BUTTON_WIDTH,SCROLL_SIZE,0,0,1,SCROLL_SIZE,scrollbar);
			g.drawImage(incImg,scrollWidth-BUTTON_WIDTH,0,scrollbar);
		} else {
			g.drawImage(BACKGROUND_DISABLED,0,0,scrollWidth,SCROLL_SIZE,0,0,1,SCROLL_SIZE,scrollbar);
		}
		g2.setTransform(origTransform);
	}

	// =======================================================================

	protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {

		if (scrollbar.isEnabled()) {
			isLight=(c.getClientProperty("__lightColorScrollbar")!=null) ? true : false;
			BufferedImage thumbImg=thumbMousePressed ? SCROLL_THUMB_PRESSED :	isThumbRollover() ? SCROLL_THUMB_OVER : ((isLight) ? LIGHT_SCROLL_THUMB_NORMAL : SCROLL_THUMB_NORMAL);
			Graphics2D g2=(Graphics2D)g;
			AffineTransform origTransform=g2.getTransform();
			Rectangle b=thumbBounds;
			if (scrollbar.getOrientation()==JScrollBar.VERTICAL) {
				b=new Rectangle(thumbBounds.y,thumbBounds.x,thumbBounds.height,thumbBounds.width);
				g2.scale(1,-1);
				g2.rotate(-Math.PI/2,0,0);
			}
			g.drawImage(thumbImg,b.x,b.y,b.x+10,b.y+SCROLL_SIZE,0,0,10,11,scrollbar);
			g.drawImage(thumbImg,b.x+10,b.y,b.x+b.width-11,b.y+SCROLL_SIZE,13,0,14,SCROLL_SIZE,scrollbar);
			g.drawImage(thumbImg,b.x+b.width-11,b.y,b.x+b.width,b.y+SCROLL_SIZE,21,0,32,SCROLL_SIZE,scrollbar);
			g2.setTransform(origTransform);
		}
	}
}
