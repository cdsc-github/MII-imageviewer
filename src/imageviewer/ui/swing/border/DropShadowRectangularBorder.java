/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.border;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;

import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.border.AbstractBorder;

import imageviewer.ui.graphics.DropShadowRenderer;
import imageviewer.ui.swing.BasicPopupFactory;

import java.io.File;
import javax.imageio.ImageIO;

// =======================================================================

public class DropShadowRectangularBorder extends AbstractBorder {

	private static final DropShadowRectangularBorder INSTANCE=new DropShadowRectangularBorder();
	private static final Rectangle rect=new Rectangle();

	protected int distance=5, shadowSize=5, xOffset=3, yOffset=3;

	public static DropShadowRectangularBorder getInstance() {return INSTANCE;}
	
	public DropShadowRectangularBorder() {}

	// =======================================================================

	public int getDistance() {return distance;}
	public int getShadowSize() {return shadowSize;}
	public int getXOffset() {return xOffset;}
	public int getYOffset() {return yOffset;}

	public void setDistance(int x) {distance=x;}
	public void setShadowSize(int x) {shadowSize=x;}
	public void setXOffset(int x) {xOffset=x;}
	public void setYOffset(int x) {yOffset=x;}

	// =======================================================================

	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {

		// Grab the background for that region.

		JComponent jc=(JComponent)c;	
		BufferedImage bi=(BufferedImage)jc.getClientProperty(BasicPopupFactory.BACKGROUND);
		if (bi==null) return;

		// Create the clip region (i.e., the lower-right portion); depends
		// on the captured image size, because of potential heavyweight
		// components.

		int x1=0;
		int x2=c.getWidth()-(3+distance);
		int x3=x2+(3+distance);
		if (x3<x1+bi.getWidth()) x3=x1+bi.getWidth();
		int y1=c.getHeight()-(3+distance);
		int y2=0;
		int y3=y1+(3+distance);
		if (y3<x1+bi.getHeight()) y3=y1+bi.getHeight();
		Polygon p1=new Polygon(new int[] {x1,x2,x2,x3,x3,x1},new int[] {y1,y1,y2,y2,y3,y3},6);
		Shape s=g.getClip();
		g.setClip(p1);

		g.drawImage(bi,0,0,null);
		rect.setRect(c.getX(),c.getY(),c.getWidth()-(6+distance),c.getHeight()-(6+distance));
		DropShadowRenderer.paintDropShadow(g,rect,shadowSize,xOffset,yOffset);
		g.setClip(s);
	}
		
	public Insets getBorderInsets(Component c) {return new Insets(0,0,3+distance,3+distance);}
	public boolean isBorderOpaque() {return false;}
}
