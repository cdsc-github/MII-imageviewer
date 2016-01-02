/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.graphics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;

// ============================================================================	

public class DropShadowRenderer extends GraphicRenderer {

	private static final AlphaComposite SHADOW_AC=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.35f);

	private static final int DEFAULT_RADIUS=9;
	private static final int DEFAULT_OFFSET_X=5;
	private static final int DEFAULT_OFFSET_Y=5;

	// ============================================================================	

	public static void paintDropShadow(Graphics g, Shape s) {
		paintDropShadow(g,s,DEFAULT_RADIUS,DEFAULT_OFFSET_X,DEFAULT_OFFSET_Y,SHADOW_AC);
	}

	public static void paintDropShadow(Graphics g, Shape s, int offsetX, int offsetY) {
		paintDropShadow(g,s,DEFAULT_RADIUS,offsetX,offsetY,SHADOW_AC);
	}

	public static void paintDropShadow(Graphics g, Shape s, int radius, int offsetX, int offsetY) {
		paintDropShadow(g,s,radius,offsetX,offsetY,SHADOW_AC);
	}
	
	public static void paintDropShadow(Graphics g, Shape s, int radius, int offsetX, int offsetY, AlphaComposite ac) {

		// Paint a drop shadow for a given shape, using a specified
		// radius, and vertical/horizontal offsets.  This effect is
		// performed by first creating a buffered image that is larger
		// than the shape itself, and creating a newly translated version
		// of the shape in terms of an area.  The area is then painted
		// gray.
		//
		// A convolution operation containing a gaussian kernel of the
		// given radius is used to draw the area image onto the given
		// graphic instance, translating the object back to the required
		// offset coordinates.

		Rectangle r=s.getBounds();
		int w=(int)r.getWidth()+(4*radius+4);
		int h=(int)r.getHeight()+(4*radius+4);
		
		Color currentColor=g.getColor();

		BufferedImage bi=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
		Graphics2D gbi=(Graphics2D)bi.getGraphics();
		gbi.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		gbi.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		Area a=new Area(s);
		AffineTransform at=AffineTransform.getTranslateInstance(-r.getX()+(2*radius+2),-r.getY()+(2*radius+2));
		Area newArea=a.createTransformedArea(at);

		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		Color bgColor=g2.getBackground();
		gbi.setBackground(new Color(bgColor.getRed(),bgColor.getGreen(),bgColor.getBlue(),0));
		gbi.clearRect(0,0,w,h);
		gbi.setColor(Color.black);
		gbi.fill(newArea);

		ConvolveOp blurOp=new ConvolveOp(computeGaussianKernel(radius),ConvolveOp.EDGE_NO_OP,null);
		g2.setComposite(ac);
		g2.drawImage(bi,blurOp,(int)r.getX()+offsetX-(2*radius+2),(int)r.getY()+offsetY-(2*radius+2));
		g2.dispose();
	}
}
