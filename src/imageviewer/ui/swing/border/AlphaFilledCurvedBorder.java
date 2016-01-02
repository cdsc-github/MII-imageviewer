/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.border;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Double;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.border.AbstractBorder;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

import imageviewer.ui.graphics.DropShadowRenderer;
import imageviewer.ui.swing.BackgroundPainter;
import imageviewer.ui.swing.BasicPopupFactory;

// =======================================================================

public class AlphaFilledCurvedBorder extends AbstractBorder {

	protected static final AlphaComposite ALPHA_MENU_AC=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.90f);
	protected static final AlphaComposite DEFAULT_AC=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);

	private static final RoundRectangle2D.Double rrect=new RoundRectangle2D.Double();
	private static final RoundRectangle2D.Double rrect2=new RoundRectangle2D.Double();

	protected int distance=2, shadowSize=3, xOffset=3, yOffset=3, arc=10;
	protected Color borderColor=Color.black;
	protected boolean isOpaque=false;
	protected Insets i=new Insets(2,2,2+distance,2+distance);
	
	public AlphaFilledCurvedBorder() {}
	public AlphaFilledCurvedBorder(int arc) {this.arc=arc;}
	public AlphaFilledCurvedBorder(Color borderColor, int arc) {this.arc=arc; this.borderColor=borderColor;}
	public AlphaFilledCurvedBorder(Color borderColor, int arc, boolean isOpaque) {this.arc=arc; this.borderColor=borderColor; this.isOpaque=isOpaque;}

	public AlphaFilledCurvedBorder(Color borderColor, int arc, boolean isOpaque, Insets i) {

		this.arc=arc; 
		this.borderColor=borderColor; 
		this.isOpaque=isOpaque;
		this.i=i;
	}

	// =======================================================================

	public int getDistance() {return distance;}
	public int getShadowSize() {return shadowSize;}
	public int getXOffset() {return xOffset;}
	public int getYOffset() {return yOffset;}
	public int getArc() {return arc;}

	public void setDistance(int x) {distance=x;}
	public void setShadowSize(int x) {shadowSize=x;}
	public void setXOffset(int x) {xOffset=x;}
	public void setYOffset(int x) {yOffset=x;}
	public void setArc(int x) {arc=x;}

	// =======================================================================
	// Updated to handle the case when the component and this border go
	// outside of the window and must represent a heavyweight object. In
	// this case, we check to see if a buffered image was set on the
	// object, from which we redraw the background. Probably could be
	// more efficient with clipping, but I think it works for now.

	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
		
		Graphics2D g2=(Graphics2D)g.create();	
		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);			
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);			
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);			
		if (!isOpaque) g2.setComposite(ALPHA_MENU_AC);
		rrect.setRoundRect(0,2,w-distance-1,h-distance-3,arc,arc);
		rrect2.setRoundRect(0,2,w-distance-1,h-distance-3,arc,arc);

		JComponent jc=(JComponent)c;	
		BufferedImage bi=(BufferedImage)jc.getClientProperty(BasicPopupFactory.BACKGROUND);
		if (bi!=null) {
			g2.drawImage(bi,0,0,null);
			g2.setClip(new RoundRectangle2D.Double(-5,-5,bi.getWidth()+5,bi.getHeight()+5,arc,arc));
		}
		DropShadowRenderer.paintDropShadow(g2,rrect2,shadowSize,xOffset,yOffset);
		ComponentUI cui=UIManager.getUI(jc);
		if (cui instanceof BackgroundPainter) {
			((BackgroundPainter)cui).paintBackground(jc,g,x,y,w,h);
		} else {
			g2.setPaint(c.getBackground()); 
			g2.fill(rrect);
		}
		g2.setColor(borderColor);
		if (!isOpaque) g2.setComposite(DEFAULT_AC);
		g2.draw(rrect);
		g2.dispose();
	}
		
	public Insets getBorderInsets(Component c) {return i;}
	public boolean isBorderOpaque() {return false;}
}
