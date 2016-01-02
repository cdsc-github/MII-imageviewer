/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Double;

import javax.swing.border.AbstractBorder;
import javax.swing.UIManager;

import imageviewer.ui.graphics.DropShadowRenderer;

// =======================================================================

public class CurvedBorder extends AbstractBorder {

	protected int arc=10, inset=2;
	protected Color borderColor=Color.darkGray;
	protected boolean shouldFill=false;
	
	public CurvedBorder() {}
	public CurvedBorder(int arc) {this.arc=arc;}
	public CurvedBorder(Color borderColor, int arc) {this.borderColor=borderColor; this.arc=arc;}
	public CurvedBorder(int arc, int inset) {this.arc=arc; this.inset=inset;}
	public CurvedBorder(Color borderColor, int arc, int inset, boolean shouldFill) {this.borderColor=borderColor; this.arc=arc; this.inset=inset; this.shouldFill=shouldFill;}

	// =======================================================================

	public int getArc() {return arc;}
	public int getInset() {return inset;}

	public void setArc(int x) {arc=x;}
	public void setInset(int x) {inset=x;}
	public void setFill(boolean x) {shouldFill=x;}

	// =======================================================================

	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {

		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);			
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);			
		RoundRectangle2D.Double rr=new RoundRectangle2D.Double(0,0,w-1,h-1,arc,arc);
		if (shouldFill) {g2.setColor(borderColor); g2.fill(rr);}
		g2.setColor(borderColor);
		g2.draw(rr);
	}
		
	public Insets getBorderInsets(Component c) {return new Insets(inset,inset,inset,inset);}
	public boolean isBorderOpaque() {return false;}
}
